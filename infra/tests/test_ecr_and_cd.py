import os
import re
import unittest
import json

class TestECRModuleAndCDPipeline(unittest.TestCase):
    def setUp(self):
        self.root_dir = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
        self.ecr_module_dir = os.path.join(self.root_dir, "infra", "modules", "ecr")
        self.staging_env_dir = os.path.join(self.root_dir, "infra", "environments", "staging")
        self.cd_workflow_path = os.path.join(self.root_dir, ".github", "workflows", "cd-staging.yml")

    def test_ecr_module_files_exist(self):
        """ECR module must have main.tf, variables.tf, and outputs.tf"""
        for filename in ["main.tf", "variables.tf", "outputs.tf"]:
            path = os.path.join(self.ecr_module_dir, filename)
            self.assertTrue(os.path.isfile(path), f"Expected {filename} in {self.ecr_module_dir}")

    def test_ecr_module_resources_and_lifecycle(self):
        """ECR module must define backend and frontend repositories with 5-image lifecycle policy"""
        main_tf_path = os.path.join(self.ecr_module_dir, "main.tf")
        self.assertTrue(os.path.isfile(main_tf_path), "main.tf does not exist in ECR module")

        with open(main_tf_path, "r", encoding="utf-8") as f:
            content = f.read()

        # Check repository resources
        self.assertIn('resource "aws_ecr_repository" "backend"', content)
        self.assertIn('resource "aws_ecr_repository" "frontend"', content)

        # Check image scanning
        self.assertIn("scan_on_push", content)

        # Check lifecycle policies
        self.assertIn('resource "aws_ecr_lifecycle_policy" "backend"', content)
        self.assertIn('resource "aws_ecr_lifecycle_policy" "frontend"', content)
        self.assertIn("imageCountMoreThan", content)
        self.assertIn("expire", content)

    def test_ecr_module_outputs(self):
        """ECR module must output repository URLs and ARNs"""
        outputs_tf_path = os.path.join(self.ecr_module_dir, "outputs.tf")
        self.assertTrue(os.path.isfile(outputs_tf_path), "outputs.tf does not exist in ECR module")

        with open(outputs_tf_path, "r", encoding="utf-8") as f:
            content = f.read()

        self.assertIn("backend_repository_url", content)
        self.assertIn("frontend_repository_url", content)

    def test_staging_environment_integrates_ecr(self):
        """Staging environment main.tf must reference ecr module and expose repository outputs"""
        staging_main_path = os.path.join(self.staging_env_dir, "main.tf")
        with open(staging_main_path, "r", encoding="utf-8") as f:
            content = f.read()

        self.assertIn('module "ecr"', content)
        self.assertIn('"../../modules/ecr"', content)

        staging_outputs_path = os.path.join(self.staging_env_dir, "outputs.tf")
        with open(staging_outputs_path, "r", encoding="utf-8") as f:
            out_content = f.read()

        self.assertIn("ecr_backend_repository_url", out_content)
        self.assertIn("ecr_frontend_repository_url", out_content)

    def test_cd_staging_workflow_parallel_build_and_push(self):
        """cd-staging.yml must have parallel jobs for building and pushing backend and frontend"""
        self.assertTrue(os.path.isfile(self.cd_workflow_path), "cd-staging.yml does not exist")

        with open(self.cd_workflow_path, "r", encoding="utf-8") as f:
            workflow = f.read()

        # Check for both build-and-push jobs
        self.assertRegex(workflow, r"build.*backend", "Missing backend build job")
        self.assertRegex(workflow, r"build.*frontend", "Missing frontend build job")

        # Check AWS credentials using Access Keys (as agreed in commit f261c13)
        self.assertIn("AWS_ACCESS_KEY_ID", workflow)
        self.assertIn("AWS_SECRET_ACCESS_KEY", workflow)

        # Check ECR login action
        self.assertIn("aws-actions/amazon-ecr-login", workflow)

        # Check Docker build and push commands or actions
        self.assertIn("gym-backend", workflow)
        self.assertIn("gym-frontend", workflow)
        self.assertIn("docker push", workflow)

if __name__ == "__main__":
    unittest.main()
