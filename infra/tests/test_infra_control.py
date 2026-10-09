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
        """Workflow must trigger on workflow_dispatch and provide pause and resume actions"""
        with open(self.workflow_path, "r", encoding="utf-8") as f:
            content = f.read()

        self.assertIn("workflow_dispatch:", content)
        self.assertIn("action:", content)
        self.assertIn("pause", content)
        self.assertIn("resume", content)

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

if __name__ == "__main__":
    unittest.main()
