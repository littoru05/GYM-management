import os
import unittest

class TestCloudWatchLogging(unittest.TestCase):
    def setUp(self):
        self.root_dir = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
        self.ecs_dir = os.path.join(self.root_dir, "infra", "modules", "ecs")
        self.staging_dir = os.path.join(self.root_dir, "infra", "environments", "staging")

        self.ecs_main_content = self._read_file(os.path.join(self.ecs_dir, "main.tf"))
        self.ecs_vars_content = self._read_file(os.path.join(self.ecs_dir, "variables.tf"))
        self.ecs_outputs_content = self._read_file(os.path.join(self.ecs_dir, "outputs.tf"))
        self.staging_main_content = self._read_file(os.path.join(self.staging_dir, "main.tf"))
        self.staging_vars_content = self._read_file(os.path.join(self.staging_dir, "variables.tf"))
        self.staging_outputs_content = self._read_file(os.path.join(self.staging_dir, "outputs.tf"))

    def _read_file(self, path):
        with open(path, "r", encoding="utf-8") as f:
            return f.read()

    def test_log_retention_variable_declared(self):
        """Variable log_retention_in_days must be defined with default = 7 in variables.tf"""
        self.assertIn('variable "log_retention_in_days"', self.ecs_vars_content)
        self.assertIn("default     = 7", self.ecs_vars_content)

    def test_staging_environment_wires_log_retention(self):
        """Staging environment must expose and wire log_retention_in_days to ecs module"""
        self.assertIn('variable "log_retention_in_days"', self.staging_vars_content)
        self.assertIn("log_retention_in_days     = var.log_retention_in_days", self.staging_main_content)

    def test_cloudwatch_log_groups_use_variable_retention(self):
        """CloudWatch log groups for backend, mysql, and frontend must use var.log_retention_in_days"""
        self.assertIn('resource "aws_cloudwatch_log_group" "backend"', self.ecs_main_content)
        self.assertIn('resource "aws_cloudwatch_log_group" "mysql"', self.ecs_main_content)
        self.assertIn('resource "aws_cloudwatch_log_group" "frontend"', self.ecs_main_content)
        self.assertIn("retention_in_days = var.log_retention_in_days", self.ecs_main_content)

    def test_task_definition_awslogs_driver_and_options(self):
        """Backend task definition must configure awslogs driver with stream prefixes for backend and mysql"""
        self.assertIn('logDriver = "awslogs"', self.ecs_main_content)
        self.assertIn('"awslogs-group"', self.ecs_main_content)
        self.assertIn('"awslogs-region"', self.ecs_main_content)
        self.assertIn('"awslogs-stream-prefix" = "mysql"', self.ecs_main_content)
        self.assertIn('"awslogs-stream-prefix" = "backend"', self.ecs_main_content)

    def test_spring_boot_ansi_color_disabled_for_cloudwatch(self):
        """Backend container environment must configure SPRING_OUTPUT_ANSI_ENABLED=NEVER for clean CloudWatch logs"""
        self.assertIn("SPRING_OUTPUT_ANSI_ENABLED", self.ecs_main_content)
        self.assertIn("NEVER", self.ecs_main_content)

    def test_execution_role_has_cloudwatch_log_permissions(self):
        """ECS execution role must attach AmazonECSTaskExecutionRolePolicy for CloudWatch logs push"""
        self.assertIn("AmazonECSTaskExecutionRolePolicy", self.ecs_main_content)

    def test_cloudwatch_log_outputs_exported(self):
        """ECS module and staging environment must export log group names, ARNs, and consolidated map"""
        for prefix in ["backend", "mysql", "frontend"]:
            name_output = f"cloudwatch_{prefix}_log_group_name"
            arn_output = f"cloudwatch_{prefix}_log_group_arn"

            self.assertIn(name_output, self.ecs_outputs_content)
            self.assertIn(arn_output, self.ecs_outputs_content)
            self.assertIn(name_output, self.staging_outputs_content)
            self.assertIn(arn_output, self.staging_outputs_content)

        # Consolidated map output for structured access and data clump mitigation
        self.assertIn('output "cloudwatch_log_groups"', self.ecs_outputs_content)
        self.assertIn('output "cloudwatch_log_groups"', self.staging_outputs_content)

if __name__ == "__main__":
    unittest.main()
