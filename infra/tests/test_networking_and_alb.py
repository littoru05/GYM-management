import os
import unittest

class TestNetworkingAndALBModules(unittest.TestCase):
    def setUp(self):
        self.root_dir = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
        self.networking_dir = os.path.join(self.root_dir, "infra", "modules", "networking")
        self.alb_dir = os.path.join(self.root_dir, "infra", "modules", "alb")
        self.staging_env_dir = os.path.join(self.root_dir, "infra", "environments", "staging")

    def test_networking_module_files_exist(self):
        """Networking module must have main.tf, variables.tf, and outputs.tf"""
        for filename in ["main.tf", "variables.tf", "outputs.tf"]:
            path = os.path.join(self.networking_dir, filename)
            self.assertTrue(os.path.isfile(path), f"Expected {filename} in {self.networking_dir}")

    def test_networking_module_resources(self):
        """Networking module must define VPC, 2 public subnets, IGW, and Security Groups"""
        main_tf_path = os.path.join(self.networking_dir, "main.tf")
        self.assertTrue(os.path.isfile(main_tf_path), "main.tf not found in networking module")
        with open(main_tf_path, "r", encoding="utf-8") as f:
            content = f.read()

        self.assertIn('resource "aws_vpc"', content)
        self.assertIn('resource "aws_internet_gateway"', content)
        self.assertIn('resource "aws_subnet"', content)
        self.assertIn('resource "aws_route_table"', content)
        self.assertIn('resource "aws_route_table_association"', content)
        self.assertIn('resource "aws_security_group" "alb"', content)
        self.assertIn('resource "aws_security_group" "ecs"', content)

    def test_networking_module_outputs(self):
        """Networking module outputs must export vpc_id, public_subnet_ids, and security groups"""
        outputs_tf_path = os.path.join(self.networking_dir, "outputs.tf")
        self.assertTrue(os.path.isfile(outputs_tf_path), "outputs.tf not found in networking module")
        with open(outputs_tf_path, "r", encoding="utf-8") as f:
            content = f.read()

        self.assertIn("vpc_id", content)
        self.assertIn("public_subnet_ids", content)
        self.assertIn("alb_security_group_id", content)
        self.assertIn("ecs_security_group_id", content)

    def test_alb_module_files_exist(self):
        """ALB module must have main.tf, variables.tf, and outputs.tf"""
        for filename in ["main.tf", "variables.tf", "outputs.tf"]:
            path = os.path.join(self.alb_dir, filename)
            self.assertTrue(os.path.isfile(path), f"Expected {filename} in {self.alb_dir}")

    def test_alb_module_resources_and_routing(self):
        """ALB module must define ALB, backend & frontend target groups (target_type=ip), listener, and path rule"""
        main_tf_path = os.path.join(self.alb_dir, "main.tf")
        self.assertTrue(os.path.isfile(main_tf_path), "main.tf not found in alb module")
        with open(main_tf_path, "r", encoding="utf-8") as f:
            content = f.read()

        self.assertIn('resource "aws_lb" ', content)
        self.assertIn('resource "aws_lb_target_group" "backend"', content)
        self.assertIn('resource "aws_lb_target_group" "frontend"', content)
        self.assertIn('target_type', content)
        self.assertIn('"ip"', content)
        vars_path = os.path.join(self.alb_dir, "variables.tf")
        with open(vars_path, "r", encoding="utf-8") as vf:
            vars_content = vf.read()

        self.assertTrue('"/api/health"' in content or '"/api/health"' in vars_content)
        self.assertIn('resource "aws_lb_listener"', content)
        self.assertIn('resource "aws_lb_listener_rule"', content)
        self.assertIn('"/api/*', content)

    def test_alb_module_outputs(self):
        """ALB module outputs must export alb_dns_name, alb_arn, and target group ARNs"""
        outputs_tf_path = os.path.join(self.alb_dir, "outputs.tf")
        self.assertTrue(os.path.isfile(outputs_tf_path), "outputs.tf not found in alb module")
        with open(outputs_tf_path, "r", encoding="utf-8") as f:
            content = f.read()

        self.assertIn("alb_dns_name", content)
        self.assertIn("backend_target_group_arn", content)
        self.assertIn("frontend_target_group_arn", content)

    def test_staging_environment_integrates_networking_and_alb(self):
        """Staging main.tf and outputs.tf must integrate networking and alb modules"""
        main_tf_path = os.path.join(self.staging_env_dir, "main.tf")
        with open(main_tf_path, "r", encoding="utf-8") as f:
            content = f.read()

        self.assertIn('module "networking"', content)
        self.assertIn('"../../modules/networking"', content)
        self.assertIn('module "alb"', content)
        self.assertIn('"../../modules/alb"', content)

        outputs_tf_path = os.path.join(self.staging_env_dir, "outputs.tf")
        with open(outputs_tf_path, "r", encoding="utf-8") as f:
            out_content = f.read()

        self.assertIn("alb_dns_name", out_content)

if __name__ == "__main__":
    unittest.main()
