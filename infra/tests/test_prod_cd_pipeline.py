import os
import re
import unittest

class TestProdCDPipeline(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.root_dir = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
        cls.prod_workflow_path = os.path.join(cls.root_dir, ".github", "workflows", "cd-prod.yml")
        cls.staging_workflow_path = os.path.join(cls.root_dir, ".github", "workflows", "cd-staging.yml")

    def _read_workflow(self, path: str) -> str:
        self.assertTrue(os.path.isfile(path), f"Workflow file not found: {path}")
        with open(path, "r", encoding="utf-8") as f:
            return f.read()

    def test_prod_cd_workflow_file_exists(self):
        """cd-prod.yml workflow file must exist under .github/workflows"""
        self.assertTrue(os.path.isfile(self.prod_workflow_path), "cd-prod.yml does not exist")

    def test_prod_cd_triggers_and_permissions(self):
        """cd-prod.yml must trigger on push to main branch and workflow_dispatch, and set appropriate permissions"""
        content = self._read_workflow(self.prod_workflow_path)
        self.assertIn("workflow_dispatch:", content)
        self.assertIn("push:", content)
        self.assertIn("main", content)
        self.assertIn("id-token: write", content)
        self.assertIn("contents: read", content)

    def test_prod_cd_terraform_init_and_validate_job(self):
        """cd-prod.yml must run fmt -check, init, and validate on infra/environments/prod"""
        content = self._read_workflow(self.prod_workflow_path)
        self.assertIn("terraform-init:", content)
        self.assertIn("infra/environments/prod", content)
        self.assertIn("terraform -chdir=infra/environments/prod fmt -check", content)
        self.assertIn("terraform -chdir=infra/environments/prod init", content)
        self.assertIn("terraform -chdir=infra/environments/prod validate", content)

    def test_prod_cd_parallel_build_and_push_jobs(self):
        """cd-prod.yml must build and push backend and frontend images in parallel with prod-<sha> and prod-latest tags"""
        content = self._read_workflow(self.prod_workflow_path)
        self.assertIn("build-and-push-backend:", content)
        self.assertIn("build-and-push-frontend:", content)

        # Parallelism: both depend on terraform-init
        self.assertRegex(content, r"build-and-push-backend:[\s\S]*?needs:\s*\[?terraform-init\]?")
        self.assertRegex(content, r"build-and-push-frontend:[\s\S]*?needs:\s*\[?terraform-init\]?")

        # Check prod tagging and ECR repositories
        self.assertIn("gym-backend", content)
        self.assertIn("gym-frontend", content)
        self.assertIn("prod-latest", content)
        self.assertRegex(content, r"prod-\$?\{\{\s*github\.sha\s*\}\}|prod-\$IMAGE_TAG")

    def test_prod_cd_deploy_ecs_job_configuration(self):
        """cd-prod.yml deploy-ecs job must target gym-prod-cluster and prod service/task names"""
        content = self._read_workflow(self.prod_workflow_path)
        self.assertIn("deploy-ecs:", content)
        self.assertIn("gym-prod-cluster", content)
        self.assertIn("gym-prod-backend", content)
        self.assertIn("gym-prod-frontend", content)

        # deploy-ecs depends on both build jobs
        self.assertRegex(
            content,
            r"deploy-ecs:[\s\S]*?needs:\s*\[\s*build-and-push-backend\s*,\s*build-and-push-frontend\s*\]"
        )

    def test_prod_cd_task_definition_registration_with_prod_image(self):
        """cd-prod.yml must update task definitions with prod-<sha> image URI and register new revisions"""
        content = self._read_workflow(self.prod_workflow_path)
        self.assertIn("aws ecs describe-task-definition", content)
        self.assertIn("aws ecs register-task-definition", content)
        self.assertIn("prod-$IMAGE_TAG", content)

    def test_prod_cd_graceful_hibernate_bypass(self):
        """cd-prod.yml must inspect ACTIVE status of services with exact boundary check and write Hibernate notice to GITHUB_STEP_SUMMARY if inactive"""
        content = self._read_workflow(self.prod_workflow_path)
        self.assertIn("describe-services", content)
        self.assertIn("ACTIVE", content)
        # Ensure exact word/line boundary check so INACTIVE services do not erroneously match
        self.assertIn('grep -q "^ACTIVE$"', content)
        self.assertIn("GITHUB_STEP_SUMMARY", content)
        self.assertIn("Hibernate", content)
        self.assertIn("update-service", content)

    def test_staging_cd_workflow_tags_staging_sha(self):
        """cd-staging.yml must tag images with staging-<sha> to cleanly separate staging from prod in shared ECR"""
        content = self._read_workflow(self.staging_workflow_path)
        self.assertRegex(
            content,
            r"staging-\$?\{\{\s*github\.sha\s*\}\}|staging-\$IMAGE_TAG",
            "cd-staging.yml must include staging-<sha> image tag"
        )


if __name__ == "__main__":
    unittest.main()
