import os
import re
import shutil
import subprocess
import unittest

class TestHAScriptsAndPlaybook(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.root_dir = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
        cls.scripts_dir = os.path.join(cls.root_dir, "scripts")
        cls.docs_dir = os.path.join(cls.root_dir, "docs")
        cls.runbooks_dir = os.path.join(cls.docs_dir, "runbooks")

        cls.check_ha_sh = os.path.join(cls.scripts_dir, "check-ha-health.sh")
        cls.check_ha_ps1 = os.path.join(cls.scripts_dir, "check-ha-health.ps1")
        cls.quick_restart_sh = os.path.join(cls.scripts_dir, "quick-restart-prod.sh")
        cls.quick_restart_ps1 = os.path.join(cls.scripts_dir, "quick-restart-prod.ps1")
        cls.playbook_path = os.path.join(cls.runbooks_dir, "defense-day-ha-playbook.md")

        # Find bash executable (supporting Git Bash on Windows)
        cls.bash_exe = shutil.which("bash")
        if not cls.bash_exe or not os.path.exists(cls.bash_exe) or "system32" in cls.bash_exe.lower():
            for git_bash in [
                r"C:\Program Files\Git\bin\bash.exe",
                r"C:\Program Files\Git\usr\bin\bash.exe",
            ]:
                if os.path.isfile(git_bash):
                    cls.bash_exe = git_bash
                    break

        cls.pwsh_exe = shutil.which("pwsh") or shutil.which("powershell") or r"C:\Windows\System32\WindowsPowerShell\v1.0\powershell.exe"

    def _read_file(self, path: str) -> str:
        self.assertTrue(os.path.isfile(path), f"File not found: {path}")
        with open(path, "r", encoding="utf-8") as f:
            return f.read()

    def test_target_files_exist(self):
        """All required scripts and the defense day playbook must exist."""
        self.assertTrue(os.path.isfile(self.check_ha_sh), "Missing scripts/check-ha-health.sh")
        self.assertTrue(os.path.isfile(self.check_ha_ps1), "Missing scripts/check-ha-health.ps1")
        self.assertTrue(os.path.isfile(self.quick_restart_sh), "Missing scripts/quick-restart-prod.sh")
        self.assertTrue(os.path.isfile(self.quick_restart_ps1), "Missing scripts/quick-restart-prod.ps1")
        self.assertTrue(os.path.isfile(self.playbook_path), "Missing docs/runbooks/defense-day-ha-playbook.md")

    def test_bash_scripts_syntax(self):
        """All bash scripts must pass bash -n syntax check."""
        if not self.bash_exe or not os.path.isfile(self.bash_exe):
            self.skipTest("Bash executable not found")

        for script in [self.check_ha_sh, self.quick_restart_sh]:
            if not os.path.isfile(script):
                self.skipTest(f"Script {script} does not exist yet")
            result = subprocess.run([self.bash_exe, "-n", script], capture_output=True, text=True)
            self.assertEqual(result.returncode, 0, f"{os.path.basename(script)} syntax error: {result.stderr}")

    def test_powershell_scripts_syntax(self):
        """All PowerShell scripts must pass AST parser validation without syntax errors."""
        if not self.pwsh_exe or not os.path.isfile(self.pwsh_exe):
            self.skipTest("PowerShell executable not found")

        for script in [self.check_ha_ps1, self.quick_restart_ps1]:
            if not os.path.isfile(script):
                self.skipTest(f"Script {script} does not exist yet")
            clean_path = script.replace("\\", "/")
            cmd = (
                f"$errors = $null; "
                f"[System.Management.Automation.Language.Parser]::ParseFile('{clean_path}', [ref]$null, [ref]$errors); "
                f"if ($errors.Count -gt 0) {{ $errors | ForEach-Object {{ Write-Error $_.Message }}; exit 1 }} else {{ exit 0 }}"
            )
            result = subprocess.run(
                [self.pwsh_exe, "-NoProfile", "-NonInteractive", "-Command", cmd],
                capture_output=True,
                text=True
            )
            self.assertEqual(result.returncode, 0, f"{os.path.basename(script)} AST parse error: {result.stderr}")

    def test_check_ha_health_sh_help_flag(self):
        """check-ha-health.sh must exit 0 and print usage with --help."""
        if not self.bash_exe or not os.path.isfile(self.bash_exe) or not os.path.isfile(self.check_ha_sh):
            self.skipTest("Bash or script not available")

        result = subprocess.run([self.bash_exe, self.check_ha_sh, "--help"], capture_output=True, text=True)
        self.assertEqual(result.returncode, 0)
        self.assertIn("Usage:", result.stdout)
        self.assertIn("--http-only", result.stdout)

    def test_check_ha_health_ps1_help_flag(self):
        """check-ha-health.ps1 must exit 0 and print usage with -Help."""
        if not self.pwsh_exe or not os.path.isfile(self.pwsh_exe) or not os.path.isfile(self.check_ha_ps1):
            self.skipTest("PowerShell or script not available")

        clean_path = self.check_ha_ps1.replace("\\", "/")
        result = subprocess.run(
            [self.pwsh_exe, "-NoProfile", "-NonInteractive", "-File", clean_path, "-Help"],
            capture_output=True,
            text=True
        )
        self.assertEqual(result.returncode, 0)
        self.assertIn("Usage", result.stdout)
        self.assertIn("HttpOnly", result.stdout)

    def test_quick_restart_prod_sh_help_flag(self):
        """quick-restart-prod.sh must exit 0 and print usage with --help."""
        if not self.bash_exe or not os.path.isfile(self.bash_exe) or not os.path.isfile(self.quick_restart_sh):
            self.skipTest("Bash or script not available")

        result = subprocess.run([self.bash_exe, self.quick_restart_sh, "--help"], capture_output=True, text=True)
        self.assertEqual(result.returncode, 0)
        self.assertIn("Usage:", result.stdout)
        self.assertIn("--dry-run", result.stdout)

    def test_quick_restart_prod_ps1_help_flag(self):
        """quick-restart-prod.ps1 must exit 0 and print usage with -Help."""
        if not self.pwsh_exe or not os.path.isfile(self.pwsh_exe) or not os.path.isfile(self.quick_restart_ps1):
            self.skipTest("PowerShell or script not available")

        clean_path = self.quick_restart_ps1.replace("\\", "/")
        result = subprocess.run(
            [self.pwsh_exe, "-NoProfile", "-NonInteractive", "-File", clean_path, "-Help"],
            capture_output=True,
            text=True
        )
        self.assertEqual(result.returncode, 0)
        self.assertIn("Usage", result.stdout)
        self.assertIn("DryRun", result.stdout)

    def test_check_ha_health_sh_content_contracts(self):
        """check-ha-health.sh must implement 3-tier health checks and support http-only mode."""
        content = self._read_file(self.check_ha_sh)

        # Tier 1: AWS Infra ALB & ECS
        self.assertIn("describe-target-health", content)
        self.assertIn("describe-services", content)
        self.assertIn("Target Group", content)
        self.assertIn("ECS Service", content)

        # Tier 2: Deep Backend Actuator
        self.assertIn("/actuator/health", content)
        self.assertIn("db", content)
        self.assertIn("diskSpace", content)

        # Tier 3: Frontend Web
        self.assertIn("/version.json", content)

        # Mode contracts
        self.assertIn("http-only", content)
        self.assertIn("HTTP_ONLY", content)

    def test_check_ha_health_ps1_content_contracts(self):
        """check-ha-health.ps1 must implement 3-tier health checks and support HttpOnly parameter."""
        content = self._read_file(self.check_ha_ps1)

        # Tier 1: AWS Infra ALB & ECS
        self.assertIn("describe-target-health", content)
        self.assertIn("describe-services", content)
        self.assertIn("Target Group", content)
        self.assertIn("ECS", content)

        # Tier 2: Deep Backend Actuator
        self.assertIn("/actuator/health", content)
        self.assertIn("db", content)
        self.assertIn("diskSpace", content)

        # Tier 3: Frontend Web
        self.assertIn("/version.json", content)

        # Mode contracts
        self.assertIn("HttpOnly", content)

    def test_quick_restart_prod_sh_content_contracts(self):
        """quick-restart-prod.sh must implement RDS check/wake, ECS rolling force update, and DuckDNS sync."""
        content = self._read_file(self.quick_restart_sh)

        # RDS Auto-Wake
        self.assertIn("describe-db-instances", content)
        self.assertIn("start-db-instance", content)
        self.assertIn("db-instance-available", content)

        # ECS Rolling Force Update
        self.assertIn("update-service", content)
        self.assertIn("--force-new-deployment", content)

        # DuckDNS Dynamic DNS Re-sync
        self.assertIn("duckdns.org/update", content)

        # Dry-run support
        self.assertIn("dry-run", content)
        self.assertIn("DRY_RUN", content)

    def test_quick_restart_prod_ps1_content_contracts(self):
        """quick-restart-prod.ps1 must implement RDS check/wake, ECS rolling force update, and DuckDNS sync."""
        content = self._read_file(self.quick_restart_ps1)

        # RDS Auto-Wake
        self.assertIn("describe-db-instances", content)
        self.assertIn("start-db-instance", content)
        self.assertIn("available", content)

        # ECS Rolling Force Update
        self.assertIn("update-service", content)
        self.assertIn("--force-new-deployment", content)

        # DuckDNS Dynamic DNS Re-sync
        self.assertIn("duckdns.org/update", content)

        # Dry-run support
        self.assertIn("DryRun", content)

    def test_defense_day_ha_playbook_content_contracts(self):
        """defense-day-ha-playbook.md must contain URLs table, pre-flight checklist, incident matrix, and one-liners."""
        content = self._read_file(self.playbook_path)

        # Must include live URLs
        self.assertIn("duckdns.org", content)
        self.assertIn("elb.amazonaws.com", content)
        self.assertIn("/actuator/health", content)
        self.assertIn("swagger", content.lower())

        # Must include Pre-flight Checklist with timeline
        self.assertIn("Pre-flight Checklist", content)
        self.assertRegex(content, r"T-15")
        self.assertRegex(content, r"T-5")

        # Must include Incident Response Matrix
        self.assertTrue("incident" in content.lower() or "ma trận" in content.lower())
        self.assertIn("DNS", content)
        self.assertIn("RDS", content)
        self.assertIn("ECS", content)

        # Must include One-Liner Emergency commands
        self.assertIn("check-ha-health", content)
        self.assertIn("quick-restart-prod", content)
        self.assertIn("aws ecs", content)
        self.assertIn("aws rds", content)

        # Domain language from GLOSSARY.md
        self.assertIn("Resume", content)
        self.assertIn("Dynamic DNS", content)
        self.assertIn("Artifact Version", content)

if __name__ == "__main__":
    unittest.main()
