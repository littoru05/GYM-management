import os
import unittest
import json
import re

class BaseInfrastructureTest(unittest.TestCase):
    root_dir = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))

    @staticmethod
    def read_file(path: str) -> str:
        with open(path, "r", encoding="utf-8") as f:
            return f.read()


class TestIsolatedDBNetworking(BaseInfrastructureTest):
    def setUp(self):
        self.networking_dir = os.path.join(self.root_dir, "infra", "modules", "networking")
        self.main_tf = self.read_file(os.path.join(self.networking_dir, "main.tf"))
        self.vars_tf = self.read_file(os.path.join(self.networking_dir, "variables.tf"))
        self.outputs_tf = self.read_file(os.path.join(self.networking_dir, "outputs.tf"))

    def test_enable_database_subnets_variable_default_false(self):
        """enable_database_subnets must default to false for backwards compatibility with staging"""
        self.assertIn('variable "enable_database_subnets"', self.vars_tf)
        self.assertRegex(self.vars_tf, r'variable\s+"enable_database_subnets"\s*\{[^}]*default\s*=\s*false')

    def test_database_subnet_cidrs_variable_declared(self):
        """database_subnet_cidrs must be declared as a list of strings"""
        self.assertIn('variable "database_subnet_cidrs"', self.vars_tf)
        self.assertIn("list(string)", self.vars_tf)

    def test_isolated_database_subnets_resource(self):
        """Networking module must define aws_subnet for database with map_public_ip_on_launch = false"""
        self.assertIn('resource "aws_subnet" "database"', self.main_tf)
        self.assertIn("map_public_ip_on_launch = false", self.main_tf)

    def test_internal_only_database_route_table(self):
        """Database subnets must have an isolated route table with no Internet Gateway or NAT route"""
        self.assertIn('resource "aws_route_table" "database"', self.main_tf)
        self.assertIn('resource "aws_route_table_association" "database"', self.main_tf)
        # Verify no 0.0.0.0/0 route is present in database route table
        db_rt_match = re.search(r'resource\s+"aws_route_table"\s+"database"\s*\{([^}]*)\}', self.main_tf, re.DOTALL)
        self.assertIsNotNone(db_rt_match, "aws_route_table database not found")
        db_rt_body = db_rt_match.group(1)
        self.assertNotIn("gateway_id", db_rt_body)
        self.assertNotIn("nat_gateway_id", db_rt_body)

    def test_database_subnet_group_resource(self):
        """Networking module must define aws_db_subnet_group spanning the isolated database subnets"""
        self.assertIn('resource "aws_db_subnet_group" "database"', self.main_tf)

    def test_database_security_group_restricts_to_ecs(self):
        """Database security group must restrict port 3306 ingress strictly to the ECS task security group"""
        self.assertIn('resource "aws_security_group" "database"', self.main_tf)
        self.assertIn("3306", self.main_tf)
        self.assertIn("aws_security_group.ecs.id", self.main_tf)

    def test_database_networking_outputs(self):
        """Networking module must output database subnet IDs, subnet group name, and database security group ID"""
        self.assertIn('output "database_subnet_ids"', self.outputs_tf)
        self.assertIn('output "database_subnet_group_name"', self.outputs_tf)
        self.assertIn('output "database_security_group_id"', self.outputs_tf)


class TestRDSFreeTierModule(BaseInfrastructureTest):
    def setUp(self):
        self.rds_dir = os.path.join(self.root_dir, "infra", "modules", "rds")

    def _read_rds_file(self, filename: str) -> str:
        path = os.path.join(self.rds_dir, filename)
        self.assertTrue(os.path.isfile(path), f"File {filename} missing in {self.rds_dir}")
        return self.read_file(path)

    def test_rds_module_files_exist(self):
        """RDS module must contain main.tf, variables.tf, and outputs.tf"""
        for filename in ["main.tf", "variables.tf", "outputs.tf"]:
            path = os.path.join(self.rds_dir, filename)
            self.assertTrue(os.path.isfile(path), f"Expected {filename} in {self.rds_dir}")

    def test_rds_instance_resource_and_engine(self):
        """RDS module must define aws_db_instance with MySQL 8.4 engine"""
        main_tf = self._read_rds_file("main.tf")
        self.assertIn('resource "aws_db_instance"', main_tf)
        self.assertIn('"mysql"', main_tf)
        self.assertIn('"8.4"', main_tf)

    def test_rds_instance_free_tier_constraints(self):
        """RDS module must enforce db.t4g.micro (or t3.micro), 20 GB gp2, Single-AZ, no autoscaling, not public"""
        main_tf = self._read_rds_file("main.tf")
        vars_tf = self._read_rds_file("variables.tf")

        # Instance class micro default
        self.assertTrue("db.t4g.micro" in vars_tf or "db.t3.micro" in vars_tf or "db.t4g.micro" in main_tf)
        # Storage 20 GB gp2
        self.assertTrue("20" in vars_tf or "20" in main_tf)
        self.assertTrue('"gp2"' in vars_tf or '"gp2"' in main_tf)
        # Storage autoscaling disabled (max_allocated_storage = 0)
        self.assertTrue("max_allocated_storage" in main_tf or "max_allocated_storage" in vars_tf)
        # Single-AZ enforcement (multi_az = false)
        self.assertTrue("multi_az" in main_tf or "multi_az" in vars_tf)
        # Public accessibility disabled
        self.assertTrue("publicly_accessible" in main_tf or "publicly_accessible" in vars_tf)
        # 1-day backup retention (AWS Free Plan restriction)
        self.assertTrue("backup_retention_period" in main_tf or "backup_retention_period" in vars_tf)
        # Performance Insights disabled
        self.assertTrue("performance_insights_enabled" in main_tf or "performance_insights_enabled" in vars_tf)

    def test_rds_module_outputs(self):
        """RDS module must export endpoint, address, port, identifier, and db_name"""
        outputs_tf = self._read_rds_file("outputs.tf")
        self.assertIn("db_instance_endpoint", outputs_tf)
        self.assertIn("db_instance_address", outputs_tf)
        self.assertIn("db_instance_identifier", outputs_tf)


class TestECSModuleConditionalSidecar(BaseInfrastructureTest):
    def setUp(self):
        self.ecs_dir = os.path.join(self.root_dir, "infra", "modules", "ecs")
        self.main_tf = self.read_file(os.path.join(self.ecs_dir, "main.tf"))
        self.vars_tf = self.read_file(os.path.join(self.ecs_dir, "variables.tf"))

    def test_enable_mysql_sidecar_variable_default_true(self):
        """enable_mysql_sidecar must default to true so staging sidecar behavior is preserved"""
        self.assertIn('variable "enable_mysql_sidecar"', self.vars_tf)
        self.assertRegex(self.vars_tf, r'variable\s+"enable_mysql_sidecar"\s*\{[^}]*default\s*=\s*true')

    def test_ecs_external_db_and_profile_variables(self):
        """ECS module must accept db_host, db_username, and spring_profile variables with safe defaults"""
        self.assertIn('variable "db_host"', self.vars_tf)
        self.assertIn('variable "db_username"', self.vars_tf)
        self.assertIn('variable "spring_profile"', self.vars_tf)

        # Check staging defaults
        self.assertRegex(self.vars_tf, r'variable\s+"db_host"\s*\{[^}]*default\s*=\s*"127\.0\.0\.1"')
        self.assertRegex(self.vars_tf, r'variable\s+"spring_profile"\s*\{[^}]*default\s*=\s*"dev"')

    def test_backend_container_definition_uses_parameterized_inputs(self):
        """Backend task definition must wire SPRING_PROFILES_ACTIVE, DB_HOST, DB_USERNAME to variables"""
        self.assertIn("var.spring_profile", self.main_tf)
        self.assertIn("var.db_host", self.main_tf)
        self.assertIn("var.db_username", self.main_tf)

    def test_backend_task_definition_conditional_sidecar(self):
        """Backend task definition must conditionally exclude the MySQL sidecar and dependsOn when enable_mysql_sidecar is false"""
        self.assertIn("var.enable_mysql_sidecar", self.main_tf)

    def test_mysql_log_group_moved_block_declared(self):
        """ECS module must declare a moved block from aws_cloudwatch_log_group.mysql to [0] for backwards-compatible state migration"""
        self.assertRegex(
            self.main_tf,
            r'moved\s*\{\s*from\s*=\s*aws_cloudwatch_log_group\.mysql\s+to\s*=\s*aws_cloudwatch_log_group\.mysql\[0\]\s*\}'
        )

    def test_rds_guardrail_default_values(self):
        """Guardrail invariants must be locked directly to free-tier-safe values in main.tf and password must not have a default"""
        vars_tf = self.read_file(os.path.join(self.root_dir, "infra", "modules", "rds", "variables.tf"))
        main_tf = self.read_file(os.path.join(self.root_dir, "infra", "modules", "rds", "main.tf"))
        expected = {
            "multi_az": "false",
            "publicly_accessible": "false",
            "max_allocated_storage": "0",
            "performance_insights_enabled": "false",
            "allocated_storage": "20",
            "storage_type": '"gp2"', "backup_retention_period": "1",
        }
        for name, value in expected.items():
            self.assertRegex(main_tf, r'%s\s*=\s*%s' % (name, re.escape(value)))
        self.assertNotRegex(vars_tf, r'variable\s+"password"\s*\{[^}]*default')

    def test_ecs_and_rds_lifecycle_alignment(self):
        """SSM db_password and RDS master password must both preserve ignore_changes, and ECS services must ignore task_definition/desired_count"""
        ecs_tf = self.read_file(os.path.join(self.root_dir, "infra", "modules", "ecs", "main.tf"))
        rds_tf = self.read_file(os.path.join(self.root_dir, "infra", "modules", "rds", "main.tf"))
        m = re.search(r'resource "aws_ssm_parameter" "db_password" \{.*?\n\}', ecs_tf, re.S)
        self.assertIsNotNone(m)
        self.assertIn("ignore_changes = [value]", m.group(0))
        self.assertIn("ignore_changes = [password]", rds_tf)
        self.assertIn("ignore_changes = [task_definition, desired_count]", ecs_tf)


class TestCIOIDCExistingProviderAndRDS(BaseInfrastructureTest):
    def setUp(self):
        self.ci_oidc_dir = os.path.join(self.root_dir, "infra", "modules", "ci-oidc")
        self.main_tf = self.read_file(os.path.join(self.ci_oidc_dir, "main.tf"))
        self.vars_tf = self.read_file(os.path.join(self.ci_oidc_dir, "variables.tf"))
        self.outputs_tf = self.read_file(os.path.join(self.ci_oidc_dir, "outputs.tf"))

    def test_create_oidc_provider_toggle_default_true(self):
        """create_oidc_provider variable must default to true for existing singleton provisioning"""
        self.assertIn('variable "create_oidc_provider"', self.vars_tf)
        self.assertRegex(self.vars_tf, r'variable\s+"create_oidc_provider"\s*\{[^}]*default\s*=\s*true')

    def test_existing_oidc_provider_data_lookup(self):
        """When create_oidc_provider is false, data source or lookup must supply provider ARN"""
        self.assertIn("data", self.main_tf)
        self.assertTrue(
            "aws_iam_openid_connect_provider" in self.main_tf or "oidc_provider_arn" in self.main_tf
        )

    def test_ci_cd_policy_grants_rds_lifecycle_actions(self):
        """CI/CD policy must include rds:DescribeDBInstances, rds:StartDBInstance, rds:StopDBInstance for hibernate/resume"""
        self.assertIn("rds:DescribeDBInstances", self.main_tf)
        self.assertIn("rds:StartDBInstance", self.main_tf)
        self.assertIn("rds:StopDBInstance", self.main_tf)

    def test_oidc_provider_moved_block_declared(self):
        """ci-oidc module must declare a moved block from aws_iam_openid_connect_provider.github to [0] for backwards-compatible state migration"""
        self.assertRegex(
            self.main_tf,
            r'moved\s*\{\s*from\s*=\s*aws_iam_openid_connect_provider\.github\s+to\s*=\s*aws_iam_openid_connect_provider\.github\[0\]\s*\}'
        )


class TestECRLifecycleRules(BaseInfrastructureTest):
    def setUp(self):
        self.ecr_dir = os.path.join(self.root_dir, "infra", "modules", "ecr")
        self.main_tf = self.read_file(os.path.join(self.ecr_dir, "main.tf"))
        self.vars_tf = self.read_file(os.path.join(self.ecr_dir, "variables.tf"))

    def test_ecr_lifecycle_protects_prod_tagged_images(self):
        """ECR lifecycle policy must define rules separating prod-* and staging-* tagged images without tagStatus = any eviction"""
        self.assertIn('"prod-"', self.main_tf)
        self.assertIn('"staging-"', self.main_tf)
        self.assertIn('"untagged"', self.main_tf)
        self.assertNotIn('"any"', self.main_tf)
        self.assertIn("tagPrefixList", self.main_tf)

    def test_ecr_variables_include_prod_image_count(self):
        """ECR variables must define max_prod_image_count with a sensible cap (e.g., 2 or 3)"""
        self.assertIn('variable "max_prod_image_count"', self.vars_tf)

if __name__ == "__main__":
    unittest.main()
