import os
import unittest

class TestInfraControlWorkflow(unittest.TestCase):
    def setUp(self):
        self.root_dir = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
        self.workflow_path = os.path.join(self.root_dir, ".github", "workflows", "infra-control.yml")

    def test_workflow_file_exists(self):
        """infra-control.yml must exist under .github/workflows"""
        self.assertTrue(os.path.isfile(self.workflow_path), f"File not found: {self.workflow_path}")

    def test_workflow_dispatch_and_inputs(self):
        """Workflow must trigger on workflow_dispatch and provide pause, hibernate, and resume actions"""
        with open(self.workflow_path, "r", encoding="utf-8") as f:
            content = f.read()

        self.assertIn("workflow_dispatch:", content)
        self.assertIn("action:", content)
        self.assertIn("pause", content)
        self.assertIn("hibernate", content)
        self.assertIn("resume", content)

    def test_workflow_hibernate_logic(self):
        """Workflow must support hibernate with targeted destruction of ALB and ECS services"""
        with open(self.workflow_path, "r", encoding="utf-8") as f:
            content = f.read()

        self.assertIn("terraform destroy", content)
        self.assertIn("-target=module.ecs.aws_ecs_service.backend", content)
        self.assertIn("-target=module.ecs.aws_ecs_service.frontend", content)
        self.assertIn("-target=module.alb", content)
        self.assertIn("-auto-approve", content)
        self.assertIn("GITHUB_STEP_SUMMARY", content)

    def test_workflow_aws_auth(self):
        """Workflow must configure AWS credentials"""
        with open(self.workflow_path, "r", encoding="utf-8") as f:
            content = f.read()

        self.assertIn("aws-actions/configure-aws-credentials", content)
        self.assertIn("AWS_ACCESS_KEY_ID", content)
        self.assertIn("AWS_SECRET_ACCESS_KEY", content)

    def test_workflow_scaling_logic(self):
        """Workflow must scale desired-count to 0 on pause and 1 on resume for both services"""
        with open(self.workflow_path, "r", encoding="utf-8") as f:
            content = f.read()

        self.assertIn("desired-count", content)
        self.assertIn("update-service", content)
        # Ensure it targets both services
        self.assertTrue("backend" in content.lower() and "frontend" in content.lower())
        # Ensure 0 and 1 are handled
        self.assertIn("0", content)
        self.assertIn("1", content)

    def test_workflow_resume_state_aware_logic(self):
        """Workflow must conditionally detect ALB existence and branch between fast pause resume and terraform apply"""
        with open(self.workflow_path, "r", encoding="utf-8") as f:
            content = f.read()

        # Check ALB existence via describe-load-balancers
        self.assertIn("describe-load-balancers", content)
        self.assertIn("alb_exists", content)
        # Terraform apply for hibernate recovery
        self.assertIn("terraform apply", content)
        self.assertIn("-auto-approve", content)

    def test_workflow_duckdns_and_dual_link_logic(self):
        """Workflow must bind ALB IP to DuckDNS with retry loop and render dual links in summary"""
        with open(self.workflow_path, "r", encoding="utf-8") as f:
            content = f.read()

        # Secret references
        self.assertIn("secrets.DUCKDNS_DOMAIN", content)
        self.assertIn("secrets.DUCKDNS_TOKEN", content)
        # DuckDNS API and DNS resolution logic
        self.assertIn("duckdns.org/update", content)
        self.assertIn("dig +short", content)
        # Dual links rendered to summary
        self.assertIn("duckdns.org", content)
        self.assertIn("GITHUB_STEP_SUMMARY", content)

    def test_workflow_environment_input_and_dynamic_resolution(self):
        """Workflow must support environment input (staging/prod, default staging) and dynamic naming"""
        with open(self.workflow_path, "r", encoding="utf-8") as f:
            content = f.read()

        # Environment input definition
        self.assertIn("environment:", content)
        self.assertIn("staging", content)
        self.assertIn("prod", content)
        self.assertIn("default: 'staging'", content)

        # Dynamic resource names and working dir
        self.assertIn("gym-${{ github.event.inputs.environment || 'staging' }}-cluster", content)
        self.assertIn("gym-${{ github.event.inputs.environment || 'staging' }}-backend", content)
        self.assertIn("gym-${{ github.event.inputs.environment || 'staging' }}-frontend", content)
        self.assertIn("gym-${{ github.event.inputs.environment || 'staging' }}-alb", content)
        self.assertIn("gym-${{ github.event.inputs.environment || 'staging' }}-mysql", content)
        self.assertIn("infra/environments/${{ github.event.inputs.environment || 'staging' }}", content)

    def test_workflow_prod_hibernate_stops_rds(self):
        """Hibernate mode must execute stop-db-instance for prod RDS while destroying ALB and ECS services"""
        with open(self.workflow_path, "r", encoding="utf-8") as f:
            content = f.read()

        self.assertIn("aws rds describe-db-instances", content)
        self.assertIn("aws rds stop-db-instance", content)
        self.assertIn("--db-instance-identifier", content)

    def test_workflow_prod_resume_starts_rds_and_waits(self):
        """Resume mode must start RDS and wait for db-instance-available before ALB inspection/reconstruction"""
        with open(self.workflow_path, "r", encoding="utf-8") as f:
            content = f.read()

        self.assertIn("aws rds start-db-instance", content)
        self.assertIn("aws rds wait db-instance-available", content)

        # Verify RDS start step appears before check-alb / terraform apply
        rds_start_idx = content.find("aws rds start-db-instance")
        check_alb_idx = content.find("check-alb")
        self.assertTrue(rds_start_idx != -1 and check_alb_idx != -1, "Both steps must exist in workflow")
        self.assertTrue(rds_start_idx < check_alb_idx, "RDS start must occur before check-alb in resume mode")

    def test_workflow_duckdns_prod_domain_with_fallback(self):
        """Resume Dynamic DNS Binding must support DUCKDNS_PROD_DOMAIN with fallback to DUCKDNS_DOMAIN"""
        with open(self.workflow_path, "r", encoding="utf-8") as f:
            content = f.read()

        self.assertIn("secrets.DUCKDNS_PROD_DOMAIN", content)
        self.assertIn("secrets.DUCKDNS_DOMAIN", content)
        self.assertIn("secrets.DUCKDNS_TOKEN", content)

    def test_workflow_terraform_version_and_latest_task_def_on_resume(self):
        """Workflow must use Terraform 1.9.5 (matching CD) and update ECS services to LATEST_TASK_DEF on resume"""
        with open(self.workflow_path, "r", encoding="utf-8") as f:
            content = f.read()

        self.assertIn('terraform_version: "1.9.5"', content)
        self.assertIn("LATEST_TASK_DEF", content)
        self.assertIn('--task-definition "$LATEST_TASK_DEF"', content)

if __name__ == "__main__":
    unittest.main()
