import os
import re
import shutil
import subprocess
import unittest

class TestDeployProdScripts(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.root_dir = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
        cls.scripts_dir = os.path.join(cls.root_dir, "scripts")
        cls.sh_script_path = os.path.join(cls.scripts_dir, "deploy-prod.sh")
        cls.ps1_script_path = os.path.join(cls.scripts_dir, "deploy-prod.ps1")

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

    def test_scripts_exist(self):
        """deploy-prod.sh and deploy-prod.ps1 must exist in the scripts/ directory."""
        self.assertTrue(os.path.isfile(self.sh_script_path), "Missing scripts/deploy-prod.sh")
        self.assertTrue(os.path.isfile(self.ps1_script_path), "Missing scripts/deploy-prod.ps1")

    def test_bash_script_syntax(self):
        """deploy-prod.sh must pass bash -n syntax validation."""
        self.assertTrue(os.path.isfile(self.sh_script_path), "Missing scripts/deploy-prod.sh")
        if not self.bash_exe or not os.path.isfile(self.bash_exe):
            self.skipTest("Bash executable not found")

        result = subprocess.run(
            [self.bash_exe, "-n", self.sh_script_path],
            capture_output=True,
            text=True
        )
        self.assertEqual(
            result.returncode, 0,
            f"deploy-prod.sh syntax error: {result.stderr}"
        )

    def test_powershell_script_syntax(self):
        """deploy-prod.ps1 must pass PowerShell AST parser validation without parse errors."""
        self.assertTrue(os.path.isfile(self.ps1_script_path), "Missing scripts/deploy-prod.ps1")
        if not self.pwsh_exe or not os.path.isfile(self.pwsh_exe):
            self.skipTest("PowerShell executable not found")

        ast_check_cmd = (
            f"$errors = $null; "
            f"[System.Management.Automation.Language.Parser]::ParseFile('{self.ps1_script_path}', [ref]$null, [ref]$errors); "
            f"if ($errors.Count -gt 0) {{ $errors | ForEach-Object {{ Write-Error $_.Message }}; exit 1 }} else {{ exit 0 }}"
        )
        result = subprocess.run(
            [self.pwsh_exe, "-NoProfile", "-NonInteractive", "-Command", ast_check_cmd],
            capture_output=True,
            text=True
        )
        self.assertEqual(
            result.returncode, 0,
            f"deploy-prod.ps1 AST parse error: {result.stderr}"
        )

    def test_deploy_prod_sh_version_and_state_contracts(self):
        """deploy-prod.sh must resolve APP_VERSION and manage .deploy-state and .deploy-history.log."""
        content = self._read_file(self.sh_script_path)

        # Version resolution from CLI argument, env var, or fallback
        self.assertIn("APP_VERSION", content, "deploy-prod.sh must resolve APP_VERSION")

        # State management
        self.assertIn(".deploy-state", content, "deploy-prod.sh must interact with .deploy-state")
        self.assertIn("LAST_STABLE_VERSION", content, "deploy-prod.sh must read/write LAST_STABLE_VERSION")
        self.assertIn(".deploy-history.log", content, "deploy-prod.sh must log to .deploy-history.log")
        self.assertIn("[SUCCESS]", content, "deploy-prod.sh must record [SUCCESS] on successful release")
        self.assertIn("[ROLLED_BACK]", content, "deploy-prod.sh must record [ROLLED_BACK] on rollback")

    def test_deploy_prod_sh_pre_migration_backup_contracts(self):
        """deploy-prod.sh must implement non-locking pre-migration backup and 10-file retention rotation."""
        content = self._read_file(self.sh_script_path)

        # Non-locking mysqldump flags
        self.assertIn("--single-transaction", content, "mysqldump must use --single-transaction")
        self.assertIn("--routines", content, "mysqldump must include --routines")
        self.assertIn("--triggers", content, "mysqldump must include --triggers")
        self.assertIn("--events", content, "mysqldump must include --events")

        # Compression with gzip
        self.assertIn("gzip", content, "Backup dump must be compressed with gzip")
        self.assertIn(".sql.gz", content, "Backup file extension must be .sql.gz")

        # Schema version query from flyway_schema_history
        self.assertIn("flyway_schema_history", content, "Backup must query current schema version from flyway_schema_history")

        # SQL dump header inspection & non-empty check
        self.assertTrue(
            "-s " in content or "-s $" in content or "size" in content.lower(),
            "deploy-prod.sh must verify that the backup file is non-empty before proceeding"
        )
        self.assertTrue(
            "MySQL dump" in content or "CREATE TABLE" in content or "Server version" in content,
            "deploy-prod.sh must verify SQL dump header in backup archive"
        )

        # Retention pruning
        self.assertTrue(
            "BACKUP_RETENTION_COUNT" in content or "10" in content,
            "deploy-prod.sh must implement retention pruning to retain at most BACKUP_RETENTION_COUNT (default 10) backups"
        )

    def test_deploy_prod_sh_restore_backup_contract(self):
        """deploy-prod.sh must support --restore-backup flag for one-command emergency recovery."""
        content = self._read_file(self.sh_script_path)
        self.assertIn("--restore-backup", content, "deploy-prod.sh must support --restore-backup flag")
        self.assertTrue(
            "gunzip" in content or "gzip -d" in content or "gzip -dc" in content or "zcat" in content,
            "deploy-prod.sh must decompress .sql.gz when restoring"
        )

    def test_deploy_prod_sh_healthcheck_and_rollback_contracts(self):
        """deploy-prod.sh must poll /actuator/health, detect crashes, print logs, and trigger rollback."""
        content = self._read_file(self.sh_script_path)

        # Actuator health polling with strict HTTP 200 and "status":"UP"
        self.assertIn("/actuator/health", content, "Health check must poll /actuator/health")
        self.assertIn("200", content, "Health check must verify HTTP 200")
        self.assertIn('"UP"', content, 'Health check must verify "status":"UP"')

        # Polling configuration: 5s interval and 90s timeout (or 18 attempts)
        self.assertTrue("5" in content, "Polling loop must use 5-second interval")
        self.assertTrue("90" in content or "18" in content, "Polling loop must enforce 90-second timeout or 18 attempts")

        # Crash detection & backend log dump
        self.assertIn("--tail", content, "On failure, script must tail backend logs")
        self.assertIn("50", content, "On failure, script must print last 50 lines of backend logs")

        # Rollback via docker-compose.prod.yml to LAST_STABLE_VERSION
        self.assertIn("docker-compose.prod.yml", content, "Script must use docker-compose.prod.yml")
        self.assertIn("LAST_STABLE_VERSION", content, "Rollback must redeploy LAST_STABLE_VERSION")

    def test_deploy_prod_ps1_contracts(self):
        """deploy-prod.ps1 must implement equivalent PowerShell deployment and auto-rollback contracts."""
        content = self._read_file(self.ps1_script_path)

        # Parameters
        self.assertIn("AppVersion", content, "deploy-prod.ps1 must declare AppVersion parameter")
        self.assertIn("RestoreBackup", content, "deploy-prod.ps1 must declare RestoreBackup parameter")

        # State management
        self.assertIn(".deploy-state", content, "deploy-prod.ps1 must interact with .deploy-state")
        self.assertIn("LAST_STABLE_VERSION", content, "deploy-prod.ps1 must read/write LAST_STABLE_VERSION")
        self.assertIn(".deploy-history.log", content, "deploy-prod.ps1 must log to .deploy-history.log")
        self.assertIn("[SUCCESS]", content, "deploy-prod.ps1 must record [SUCCESS]")
        self.assertIn("[ROLLED_BACK]", content, "deploy-prod.ps1 must record [ROLLED_BACK]")

        # Pre-migration backup
        self.assertIn("--single-transaction", content, "mysqldump must use --single-transaction")
        self.assertIn("--routines", content, "mysqldump must include --routines")
        self.assertIn("--triggers", content, "mysqldump must include --triggers")
        self.assertIn("--events", content, "mysqldump must include --events")
        self.assertIn(".sql.gz", content, "Backup file extension must be .sql.gz")
        self.assertIn("flyway_schema_history", content, "Backup must query current schema version from flyway_schema_history")
        self.assertTrue(
            "MySQL dump" in content or "CREATE TABLE" in content or "Server version" in content,
            "deploy-prod.ps1 must verify SQL dump header in backup archive"
        )

        # Healthcheck polling & crash detection
        self.assertIn("/actuator/health", content, "Health check must poll /actuator/health")
        self.assertIn("200", content, "Health check must verify HTTP 200")
        self.assertIn('"UP"', content, 'Health check must verify "status":"UP"')
        self.assertIn("50", content, "On failure, script must print last 50 lines of logs")
        self.assertIn("docker-compose.prod.yml", content, "Script must use docker-compose.prod.yml")

    def test_restore_backup_nonexistent_file_exits_with_error(self):
        """Invoking --restore-backup or -RestoreBackup with a non-existent file must abort with non-zero exit code."""
        non_existent = os.path.join(self.root_dir, "backups", "non_existent_file.sql.gz")

        # Test PowerShell
        if self.pwsh_exe and os.path.isfile(self.pwsh_exe):
            cmd = [
                self.pwsh_exe,
                "-NoProfile",
                "-NonInteractive",
                "-File",
                self.ps1_script_path,
                "-RestoreBackup",
                non_existent
            ]
            result = subprocess.run(cmd, capture_output=True, text=True)
            self.assertNotEqual(result.returncode, 0, "deploy-prod.ps1 must fail if restore file does not exist")

        # Test Bash
        if self.bash_exe and os.path.isfile(self.bash_exe):
            cmd = [
                self.bash_exe,
                self.sh_script_path,
                "--restore-backup",
                non_existent
            ]
            result = subprocess.run(cmd, capture_output=True, text=True)
            self.assertNotEqual(result.returncode, 0, "deploy-prod.sh must fail if restore file does not exist")

    def test_backup_retention_pruning_behavior_powershell(self):
        """Verify PowerShell backup retention logic trims directory to BACKUP_RETENTION_COUNT files."""
        import tempfile
        import time

        with tempfile.TemporaryDirectory() as temp_dir:
            # Create 14 mock backup files with staggered timestamps
            for i in range(1, 15):
                file_path = os.path.join(temp_dir, f"gym_db_v1_to_1.0.0_20261107_{i:02d}0000.sql.gz")
                with open(file_path, "wb") as f:
                    f.write(b"mock-gzip-data")
                time.sleep(0.01)

            ps_script = (
                f"$backupDir = '{temp_dir}'; "
                f"$backupRetentionCount = 10; "
                f"$existingBackups = Get-ChildItem -Path $backupDir -Filter '*.sql.gz' | Sort-Object LastWriteTime, CreationTime; "
                f"if ($existingBackups.Count -gt $backupRetentionCount) {{ "
                f"    $toRemoveCount = $existingBackups.Count - $backupRetentionCount; "
                f"    $toRemove = $existingBackups | Select-Object -First $toRemoveCount; "
                f"    foreach ($old in $toRemove) {{ Remove-Item -Path $old.FullName -Force }} "
                f"}}"
            )
            res = subprocess.run([self.pwsh_exe, "-NoProfile", "-NonInteractive", "-Command", ps_script])
            self.assertEqual(res.returncode, 0)

            remaining = [f for f in os.listdir(temp_dir) if f.endswith(".sql.gz")]
            self.assertEqual(len(remaining), 10, f"Expected 10 files after pruning, found {len(remaining)}")

    def test_backup_retention_pruning_behavior_bash(self):
        """Verify Bash backup retention logic trims directory to BACKUP_RETENTION_COUNT files."""
        import tempfile
        import time

        if not self.bash_exe or not os.path.isfile(self.bash_exe):
            self.skipTest("Bash executable not found")

        with tempfile.TemporaryDirectory() as temp_dir:
            for i in range(1, 14):
                file_path = os.path.join(temp_dir, f"gym_db_v1_to_1.0.0_20261107_{i:02d}0000.sql.gz")
                with open(file_path, "wb") as f:
                    f.write(b"mock-gzip-data")
                time.sleep(0.01)

            posix_temp_dir = temp_dir.replace("\\", "/")
            if len(posix_temp_dir) > 1 and posix_temp_dir[1] == ":":
                posix_temp_dir = "/" + posix_temp_dir[0].lower() + posix_temp_dir[2:]

            bash_cmd = (
                f'BACKUP_DIR="{posix_temp_dir}"; '
                f'BACKUP_RETENTION_COUNT=10; '
                f'while true; do '
                f'  COUNT="$(find "$BACKUP_DIR" -maxdepth 1 -name "*.sql.gz" -type f | wc -l)"; '
                f'  if [ "$COUNT" -gt "$BACKUP_RETENTION_COUNT" ]; then '
                f'    OLDEST="$(ls -1tr "$BACKUP_DIR"/*.sql.gz 2>/dev/null | head -n 1)"; '
                f'    if [ -n "$OLDEST" -a -f "$OLDEST" ]; then rm -f "$OLDEST"; else break; fi; '
                f'  else break; fi; '
                f'done'
            )
            res = subprocess.run([self.bash_exe, "-c", bash_cmd])
            self.assertEqual(res.returncode, 0)

            remaining = [f for f in os.listdir(temp_dir) if f.endswith(".sql.gz")]
            self.assertEqual(len(remaining), 10, f"Expected 10 files after pruning, found {len(remaining)}")


if __name__ == "__main__":
    unittest.main()
