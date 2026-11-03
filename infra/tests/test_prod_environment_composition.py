import os
import unittest
import re

class TestProdEnvironmentComposition(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.root_dir = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
        cls.prod_dir = os.path.join(cls.root_dir, "infra", "environments", "prod")

    def _read_prod_file(self, filename: str) -> str:
        filepath = os.path.join(self.prod_dir, filename)
        self.assertTrue(os.path.isfile(filepath), f"Expected file {filename} in {self.prod_dir}")
        with open(filepath, "r", encoding="utf-8") as f:
            return f.read()

    def test_prod_directory_and_required_files_exist(self):
        """Production environment directory must contain backend.tf, main.tf, variables.tf, and outputs.tf"""
        self.assertTrue(os.path.isdir(self.prod_dir), f"Directory {self.prod_dir} must exist")
        for filename in ["backend.tf", "main.tf", "variables.tf", "outputs.tf"]:
            path = os.path.join(self.prod_dir, filename)
            self.assertTrue(os.path.isfile(path), f"File {filename} must exist in {self.prod_dir}")

    def test_prod_remote_backend_shared_storage_and_prod_key(self):
        """Production remote backend must use the existing S3 bucket, lock table, and prod/terraform.tfstate key"""
        content = self._read_prod_file("backend.tf")
        self.assertIn('backend "s3"', content)
        self.assertIn('"gym-management-tfstate-staging"', content)
        self.assertIn('"prod/terraform.tfstate"', content)
        self.assertIn('"gym-management-tflock-staging"', content)
        self.assertIn("encrypt        = true", content)

    def test_prod_shares_singleton_ecr_via_data_sources(self):
        """Production environment must reference shared ECR repositories via data lookups, not create duplicate modules"""
        content = self._read_prod_file("main.tf")
        self.assertIn('data "aws_ecr_repository" "backend"', content)
        self.assertIn('data "aws_ecr_repository" "frontend"', content)
        self.assertIn('"gym-backend"', content)
        self.assertIn('"gym-frontend"', content)
        self.assertNotIn('module "ecr"', content)

    def test_prod_shares_account_oidc_provider(self):
        """Production CI OIDC module must set create_oidc_provider = false to reuse existing account OIDC provider"""
        content = self._read_prod_file("main.tf")
        self.assertIn('module "ci_oidc"', content)
        self.assertIn("create_oidc_provider = false", content)

    def test_prod_omits_duplicate_budget_resource(self):
        """Production environment must omit cost_monitoring module / budget resource to stay within 2-budget Free Tier ceiling"""
        content = self._read_prod_file("main.tf")
        self.assertNotIn('module "cost_monitoring"', content)
        self.assertNotIn("aws_budgets_budget", content)

    def test_prod_networking_isolated_vpc_and_subnets(self):
        """Production networking must use 10.1.0.0/16 VPC with 2 public subnets and 2 isolated database subnets"""
        content = self._read_prod_file("main.tf")
        self.assertIn('module "networking"', content)
        self.assertIn('"10.1.0.0/16"', content)
        self.assertIn("enable_database_subnets = true", content)
        self.assertIn("10.1.1.0/24", content)
        self.assertIn("10.1.2.0/24", content)
        self.assertIn("10.1.10.0/24", content)
        self.assertIn("10.1.11.0/24", content)

    def test_prod_alb_and_rds_module_wiring(self):
        """Production environment must wire ALB and Single-AZ RDS MySQL (gym-prod-mysql) to isolated database subnets"""
        content = self._read_prod_file("main.tf")
        self.assertIn('module "alb"', content)
        self.assertIn('module "rds"', content)
        self.assertTrue("gym-${var.environment}-mysql" in content or "gym-prod-mysql" in content)
        self.assertIn("module.networking.database_subnet_group_name", content)
        self.assertIn("module.networking.database_security_group_id", content)

    def test_prod_ecs_downsized_and_external_rds_wiring(self):
        """Production ECS module must configure 512 CPU / 1024 MiB for backend (preventing OOM 137), 256 CPU / 512 MiB for frontend, spring_profile='prod', enable_mysql_sidecar=false, and wire RDS host"""
        content = self._read_prod_file("main.tf")
        self.assertIn('module "ecs"', content)
        self.assertIn("enable_mysql_sidecar                 = false", content)
        self.assertIn('spring_profile                       = "prod"', content)
        self.assertIn("backend_cpu                          = 512", content)
        self.assertIn("backend_memory                       = 1024", content)
        self.assertIn("backend_container_memory_reservation = 1024", content)
        self.assertIn("frontend_cpu                         = 256", content)
        self.assertIn("frontend_memory                      = 512", content)
        self.assertIn("module.rds.db_instance_address", content)
        self.assertIn("module.rds.db_username", content)

    def test_ecs_backend_service_health_check_grace_period_and_db_pool_env(self):
        """ECS backend service must configure health_check_grace_period_seconds and bounded DB pool env vars for external RDS"""
        ecs_main_path = os.path.join(self.root_dir, "infra", "modules", "ecs", "main.tf")
        with open(ecs_main_path, "r", encoding="utf-8") as f:
            ecs_content = f.read()
        self.assertIn("health_check_grace_period_seconds", ecs_content)
        self.assertIn("DB_POOL_SIZE", ecs_content)
        self.assertIn("DB_MIN_IDLE", ecs_content)

    def test_backend_dockerfile_log_dir_ownership_and_metaspace_bound(self):
        """Backend Dockerfile must create /app/logs for non-root appuser and bound JVM metaspace to prevent OOM"""
        dockerfile_path = os.path.join(self.root_dir, "backend", "Dockerfile")
        with open(dockerfile_path, "r", encoding="utf-8") as f:
            dockerfile_content = f.read()
        self.assertIn("/app/logs", dockerfile_content)
        self.assertIn("MaxMetaspaceSize", dockerfile_content)

    def test_prod_outputs_exported(self):
        """Production outputs must expose ALB DNS name, RDS endpoint, ECS cluster/service names, and CloudWatch log groups"""
        content = self._read_prod_file("outputs.tf")
        required_outputs = [
            "alb_dns_name",
            "rds_endpoint",
            "rds_address",
            "ecs_cluster_name",
            "backend_service_name",
            "frontend_service_name",
            "cloudwatch_backend_log_group_name",
            "cloudwatch_frontend_log_group_name",
            "cloudwatch_log_groups",
        ]
        for out in required_outputs:
            self.assertIn(f'output "{out}"', content, f"Output {out} missing from prod outputs.tf")

        # Staging cost monitoring budget outputs must not be in prod outputs
        self.assertNotIn('output "budget_name"', content)
        self.assertNotIn('output "budget_limit_amount"', content)

if __name__ == "__main__":
    unittest.main()
