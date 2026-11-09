import os
import re
import unittest
import subprocess

class TestTerraformSecurityAndDocs(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.root_dir = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
        cls.infra_dir = os.path.join(cls.root_dir, "infra")
        cls.infra_readme_path = os.path.join(cls.infra_dir, "README.md")
        cls.terraform_readme_path = os.path.join(cls.root_dir, "terraform", "README.md")
        cls.infra_gitignore_path = os.path.join(cls.infra_dir, ".gitignore")
        cls.root_gitignore_path = os.path.join(cls.root_dir, ".gitignore")

    def _read_file(self, path: str) -> str:
        self.assertTrue(os.path.isfile(path), f"File not found: {path}")
        with open(path, "r", encoding="utf-8") as f:
            return f.read()

    def _iter_infra_files(self, extensions=(".tf", ".tfvars", ".sh", ".ps1", ".json")):
        """Helper generator yielding (filepath, content) across infra/ skipping .terraform/"""
        for root, dirs, files in os.walk(self.infra_dir):
            if ".terraform" in root:
                continue
            for file in files:
                if file.endswith(extensions):
                    filepath = os.path.join(root, file)
                    yield filepath, self._read_file(filepath)

    # --- SEAM 1: KIỂM TOÁN BẢO MẬT MÃ NGUỒN IAC ---

    def test_no_hardcoded_aws_access_keys_in_infra(self):
        """No file in infra/ may contain hardcoded AWS access keys (AKIA...)"""
        key_pattern = re.compile(r'AKIA[0-9A-Z]{16}')
        for filepath, content in self._iter_infra_files():
            match = key_pattern.search(content)
            self.assertIsNone(match, f"Found hardcoded AWS key in {filepath}: {match.group(0) if match else ''}")

    def test_no_hardcoded_github_tokens_in_infra(self):
        """No file in infra/ may contain hardcoded GitHub personal access tokens"""
        token_pattern = re.compile(r'gh[pousr]_[0-9a-zA-Z]{36}')
        for filepath, content in self._iter_infra_files():
            match = token_pattern.search(content)
            self.assertIsNone(match, f"Found hardcoded GitHub token in {filepath}")

    def test_sensitive_flag_configured_for_secrets(self):
        """All password and secret variables in modules and environments must declare sensitive = true"""
        files_to_check = [
            os.path.join(self.infra_dir, "modules", "rds", "variables.tf"),
            os.path.join(self.infra_dir, "modules", "ecs", "variables.tf"),
            os.path.join(self.infra_dir, "environments", "prod", "variables.tf"),
        ]
        for filepath in files_to_check:
            content = self._read_file(filepath)
            var_blocks = re.findall(r'variable\s+"([^"]+)"\s*\{([^}]*)\}', content)
            for var_name, var_body in var_blocks:
                if any(k in var_name.lower() for k in ["password", "secret", "token"]):
                    self.assertIn(
                        "sensitive",
                        var_body,
                        f"Variable '{var_name}' in {filepath} must declare 'sensitive = true'"
                    )
                    self.assertRegex(
                        var_body,
                        r'sensitive\s*=\s*true',
                        f"Variable '{var_name}' in {filepath} must have sensitive = true"
                    )

    def test_no_plaintext_defaults_for_production_and_rds_secrets(self):
        """Production and RDS sensitive variables must not have plaintext defaults committed to Git"""
        strict_files = [
            os.path.join(self.infra_dir, "environments", "prod", "variables.tf"),
            os.path.join(self.infra_dir, "modules", "rds", "variables.tf"),
        ]
        for filepath in strict_files:
            content = self._read_file(filepath)
            var_blocks = re.findall(r'variable\s+"([^"]+)"\s*\{([^}]*)\}', content)
            for var_name, var_body in var_blocks:
                if any(k in var_name.lower() for k in ["password", "secret"]):
                    self.assertNotRegex(
                        var_body,
                        r'default\s*=',
                        f"Sensitive variable '{var_name}' in {filepath} must NOT have a default value"
                    )

        # Check ECS variables: db_password and jwt_secret must only have empty string default or no default
        ecs_vars = self._read_file(os.path.join(self.infra_dir, "modules", "ecs", "variables.tf"))
        var_blocks = re.findall(r'variable\s+"([^"]+)"\s*\{([^}]*)\}', ecs_vars)
        for var_name, var_body in var_blocks:
            if any(k in var_name.lower() for k in ["password", "secret"]):
                default_match = re.search(r'default\s*=\s*("[^"]*")', var_body)
                if default_match:
                    self.assertEqual(
                        default_match.group(1),
                        '""',
                        f"Sensitive variable '{var_name}' in ECS module must not have non-empty default"
                    )

    def test_secrets_wired_to_ssm_parameter_store(self):
        """Sensitive credentials in ECS must be provisioned via SecureString in SSM Parameter Store"""
        ecs_main = self._read_file(os.path.join(self.infra_dir, "modules", "ecs", "main.tf"))
        self.assertIn('resource "aws_ssm_parameter" "db_password"', ecs_main)
        self.assertIn('resource "aws_ssm_parameter" "jwt_secret"', ecs_main)
        self.assertIn('type        = "SecureString"', ecs_main)

    # --- SEAM 2: KIỂM TOÁN GITIGNORE & KHÔNG LƯU VẾT SECRETS TRONG GIT ---

    def test_infra_and_root_gitignore_contain_required_patterns(self):
        """Both infra/.gitignore and root .gitignore must ignore .terraform, tfstate, tfvars, backup, and lock info"""
        required_patterns = [
            ".terraform",
            "*.tfstate",
            "*.tfvars",
            "*.backup",
            ".terraform.tfstate.lock.info",
        ]
        infra_ignore = self._read_file(self.infra_gitignore_path)
        root_ignore = self._read_file(self.root_gitignore_path)

        for pattern in required_patterns:
            self.assertIn(pattern, infra_ignore, f"Pattern '{pattern}' missing from infra/.gitignore")
            self.assertIn(pattern, root_ignore, f"Pattern '{pattern}' missing from root .gitignore")

    def test_git_tracked_files_do_not_contain_secrets_or_state(self):
        """Git must not track any .tfstate, .tfvars, .env, or secret credential files"""
        result = subprocess.run(
            ["git", "ls-files"],
            cwd=self.root_dir,
            capture_output=True,
            text=True,
            check=True
        )
        tracked_files = result.stdout.splitlines()
        forbidden_extensions = (".tfstate", ".tfstate.backup", ".tfvars", ".tfvars.json", ".env")
        for f in tracked_files:
            for ext in forbidden_extensions:
                self.assertFalse(f.endswith(ext), f"Forbidden file type tracked in git: {f}")
            self.assertNotIn("/.terraform/", f, f"Local terraform directory tracked in git: {f}")

    # --- SEAM 3: TÀI LIỆU VẬN HÀNH INFRA/README.MD ---

    def test_infra_readme_exists_and_substantial(self):
        """infra/README.md must exist and be comprehensive (> 3000 bytes)"""
        self.assertTrue(os.path.isfile(self.infra_readme_path), f"Missing {self.infra_readme_path}")
        file_size = os.path.getsize(self.infra_readme_path)
        self.assertGreater(file_size, 3000, f"infra/README.md too small ({file_size} bytes)")

    def test_infra_readme_covers_architecture_and_modules(self):
        """infra/README.md must contain architecture overview and module structure"""
        content = self._read_file(self.infra_readme_path)
        self.assertIn("modules/alb", content)
        self.assertIn("modules/ecs", content)
        self.assertIn("modules/rds", content)
        self.assertIn("modules/networking", content)
        self.assertIn("modules/ci-oidc", content)
        self.assertIn("bootstrap", content)

    def test_infra_readme_covers_prerequisites(self):
        """infra/README.md must specify Terraform CLI >= 1.5, AWS CLI v2, and IAM permissions"""
        content = self._read_file(self.infra_readme_path)
        self.assertRegex(content, r'(?i)terraform\s*(cli)?\s*(v?>=?\s*1\.5|1\.5\+)')
        self.assertRegex(content, r'(?i)aws\s*cli\s*(v?2|v2)')
        self.assertRegex(content, r'(?i)iam')

    def test_infra_readme_covers_remote_state_bootstrap(self):
        """infra/README.md must describe remote state bootstrap process (S3 + DynamoDB)"""
        content = self._read_file(self.infra_readme_path)
        self.assertIn("infra/bootstrap", content)
        self.assertIn("gym-management-tfstate-staging", content)
        self.assertIn("gym-management-tflock-staging", content)

    def test_infra_readme_covers_staging_and_prod_commands(self):
        """infra/README.md must detail init, plan, apply for both staging and prod"""
        content = self._read_file(self.infra_readme_path)
        for env in ["staging", "prod"]:
            self.assertIn(f"environments/{env}", content)
        self.assertIn("terraform init", content)
        self.assertIn("terraform plan", content)
        self.assertIn("terraform apply", content)

    def test_infra_readme_covers_secrets_and_destroy(self):
        """infra/README.md must detail sensitive variable handling (TF_VAR_*, SSM) and controlled destroy"""
        content = self._read_file(self.infra_readme_path)
        self.assertIn("TF_VAR_", content)
        self.assertIn("SSM Parameter Store", content)
        self.assertIn("terraform destroy", content)

    # --- SEAM 4: ĐIỀU HƯỚNG TƯƠNG THÍCH TERRAFORM/README.MD ---

    def test_terraform_compatibility_readme_exists_and_routes_to_infra(self):
        """terraform/README.md must exist and route to infra/ with explanation of CI/CD backwards compatibility"""
        self.assertTrue(os.path.isfile(self.terraform_readme_path), f"Missing {self.terraform_readme_path}")
        content = self._read_file(self.terraform_readme_path)
        self.assertIn("infra/", content)
        self.assertIn("README.md", content)
        self.assertRegex(content, r'(?i)ci[/-]?cd')

if __name__ == "__main__":
    unittest.main()
