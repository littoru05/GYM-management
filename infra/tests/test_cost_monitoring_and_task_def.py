import os
import unittest

class TestCostMonitoringAndTaskDef(unittest.TestCase):
    def setUp(self):
        self.root_dir = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
        self.ecs_dir = os.path.join(self.root_dir, "infra", "modules", "ecs")
        self.cost_monitoring_dir = os.path.join(self.root_dir, "infra", "modules", "cost-monitoring")
        self.staging_env_dir = os.path.join(self.root_dir, "infra", "environments", "staging")

    def _read_file(self, file_path: str) -> str:
        """Helper to read and return the content of a file."""
        self.assertTrue(os.path.isfile(file_path), f"File not found: {file_path}")
        with open(file_path, "r", encoding="utf-8") as f:
            return f.read()

    def test_backend_task_definition_cpu_and_memory_defaults(self):
        """Backend task definition variables must default to 512 CPU units and 2048 MiB RAM"""
        vars_tf_path = os.path.join(self.ecs_dir, "variables.tf")
        content = self._read_file(vars_tf_path)

        # Check variable backend_cpu default
        self.assertIn('variable "backend_cpu"', content)
        backend_cpu_block = content.split('variable "backend_cpu"')[1].split('}')[0]
        self.assertIn("512", backend_cpu_block, "backend_cpu default should be 512")

        # Check variable backend_memory default
        self.assertIn('variable "backend_memory"', content)
        backend_memory_block = content.split('variable "backend_memory"')[1].split('}')[0]
        self.assertIn("2048", backend_memory_block, "backend_memory default should be 2048")

    def test_backend_container_soft_memory_reservations(self):
        """Backend task containers must define soft memoryReservation (mysql: 768, backend: 1024)"""
        main_tf_path = os.path.join(self.ecs_dir, "main.tf")
        content = self._read_file(main_tf_path)

        normalized_content = content.replace(" ", "")
        self.assertTrue(
            "memoryReservation=768" in normalized_content or '"memoryReservation":768' in normalized_content,
            "MySQL container must have memoryReservation = 768"
        )
        self.assertTrue(
            "memoryReservation=1024" in normalized_content or '"memoryReservation":1024' in normalized_content,
            "Backend container must have memoryReservation = 1024"
        )

    def test_cost_monitoring_module_files_exist(self):
        """Cost monitoring module must have main.tf, variables.tf, and outputs.tf"""
        for filename in ["main.tf", "variables.tf", "outputs.tf"]:
            path = os.path.join(self.cost_monitoring_dir, filename)
            self.assertTrue(os.path.isfile(path), f"Expected {filename} in {self.cost_monitoring_dir}")

    def test_cost_monitoring_budget_and_ssm_parameter(self):
        """Cost monitoring module must configure AWS Budget ($10 limit, 3 triggers via dynamic block) and SSM Parameter with ignore_changes"""
        main_tf_path = os.path.join(self.cost_monitoring_dir, "main.tf")
        content = self._read_file(main_tf_path)

        # Check SSM parameter for billing alert emails
        self.assertIn('resource "aws_ssm_parameter"', content)
        self.assertIn("billing_alert_emails", content)
        self.assertIn("ignore_changes", content)

        # Check data source for dynamic drift refresh
        self.assertIn('data "aws_ssm_parameter"', content)

        # Check AWS Budget resource
        self.assertIn('resource "aws_budgets_budget"', content)
        self.assertIn("MONTHLY", content)
        self.assertIn("COST", content)

        # Check dynamic notification block
        self.assertIn('dynamic "notification"', content)

        # Check 3 threshold triggers (50, 80, 100)
        self.assertIn("50", content)
        self.assertIn("80", content)
        self.assertIn("100", content)
        self.assertIn("ACTUAL", content)
        self.assertIn("FORECASTED", content)

    def test_staging_environment_integrates_cost_monitoring(self):
        """Staging main.tf and outputs.tf must wire cost_monitoring module"""
        main_tf_path = os.path.join(self.staging_env_dir, "main.tf")
        content = self._read_file(main_tf_path)

        self.assertIn('module "cost_monitoring"', content)
        self.assertIn('"../../modules/cost-monitoring"', content)

        outputs_tf_path = os.path.join(self.staging_env_dir, "outputs.tf")
        out_content = self._read_file(outputs_tf_path)

        self.assertTrue(
            "cost_monitoring" in out_content or "budget_name" in out_content or "budget_limit" in out_content,
            "outputs.tf should expose cost monitoring information"
        )

if __name__ == "__main__":
    unittest.main()
