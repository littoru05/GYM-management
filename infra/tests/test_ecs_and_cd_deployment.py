import os
import unittest

class TestECSAndCDDeployment(unittest.TestCase):
    def setUp(self):
        self.root_dir = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
        self.ecs_dir = os.path.join(self.root_dir, "infra", "modules", "ecs")
        self.staging_env_dir = os.path.join(self.root_dir, "infra", "environments", "staging")
        self.cd_workflow_path = os.path.join(self.root_dir, ".github", "workflows", "cd-staging.yml")

    def test_ecs_module_files_exist(self):
        """ECS module must have main.tf, variables.tf, and outputs.tf"""
        for filename in ["main.tf", "variables.tf", "outputs.tf"]:
            path = os.path.join(self.ecs_dir, filename)
            self.assertTrue(os.path.isfile(path), f"Expected {filename} in {self.ecs_dir}")

    def test_ecs_cluster_and_fargate_spot(self):
        """ECS module must define an ECS cluster and use FARGATE_SPOT"""
        main_tf_path = os.path.join(self.ecs_dir, "main.tf")
        self.assertTrue(os.path.isfile(main_tf_path), "main.tf not found in ECS module")
        with open(main_tf_path, "r", encoding="utf-8") as f:
            content = f.read()

        self.assertIn('resource "aws_ecs_cluster"', content)
        self.assertIn("FARGATE_SPOT", content)

    def test_backend_task_definition_with_mysql_sidecar_and_ssm(self):
        """Backend task definition must include backend and mysql sidecar containers, and SSM secrets"""
        main_tf_path = os.path.join(self.ecs_dir, "main.tf")
        with open(main_tf_path, "r", encoding="utf-8") as f:
            content = f.read()

        self.assertIn('resource "aws_ecs_task_definition" "backend"', content)
        # Check containers
        self.assertIn('"backend"', content)
        self.assertIn('"mysql"', content)
        self.assertIn("mysql:8.4", content)
        # Check health check and dependency
        self.assertIn("mysqladmin", content)
        self.assertIn("dependsOn", content)
        # Check SSM secrets
        self.assertIn("DB_PASSWORD", content)
        self.assertIn("JWT_SECRET", content)
        self.assertIn("secrets", content)
        # Check dev profile
        self.assertIn("SPRING_PROFILES_ACTIVE", content)

    def test_frontend_task_definition(self):
        """Frontend task definition must configure the frontend container on port 80"""
        main_tf_path = os.path.join(self.ecs_dir, "main.tf")
        with open(main_tf_path, "r", encoding="utf-8") as f:
            content = f.read()

        self.assertIn('resource "aws_ecs_task_definition" "frontend"', content)
        self.assertIn('"frontend"', content)
        self.assertIn("containerPort", content)

    def test_ecs_services_and_alb_integration(self):
        """ECS module must define backend and frontend services wired to target groups"""
        main_tf_path = os.path.join(self.ecs_dir, "main.tf")
        with open(main_tf_path, "r", encoding="utf-8") as f:
            content = f.read()

        self.assertIn('resource "aws_ecs_service" "backend"', content)
        self.assertIn('resource "aws_ecs_service" "frontend"', content)
        self.assertIn("assign_public_ip", content)
        self.assertIn("load_balancer", content)

    def test_staging_environment_integrates_ecs(self):
        """Staging environment main.tf and outputs.tf must integrate ecs module"""
        main_tf_path = os.path.join(self.staging_env_dir, "main.tf")
        with open(main_tf_path, "r", encoding="utf-8") as f:
            content = f.read()

        self.assertIn('module "ecs"', content)
        self.assertIn('"../../modules/ecs"', content)

        outputs_tf_path = os.path.join(self.staging_env_dir, "outputs.tf")
        with open(outputs_tf_path, "r", encoding="utf-8") as f:
            out_content = f.read()

        self.assertIn("ecs_cluster_name", out_content)

    def test_cd_staging_workflow_includes_ecs_deployment(self):
        """cd-staging.yml must include a step or job to deploy/update ECS services"""
        with open(self.cd_workflow_path, "r", encoding="utf-8") as f:
            workflow = f.read()

        self.assertIn("ecs", workflow.lower())
        self.assertTrue(
            "update-service" in workflow or "amazon-ecs-deploy" in workflow or "deploy" in workflow.lower()
        )

if __name__ == "__main__":
    unittest.main()
