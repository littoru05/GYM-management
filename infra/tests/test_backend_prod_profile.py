import os
import re
import unittest

class TestBackendProdProfile(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.root_dir = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
        cls.prod_yaml_path = os.path.join(
            cls.root_dir, "backend", "src", "main", "resources", "application-prod.yaml"
        )
        if not os.path.isfile(cls.prod_yaml_path):
            raise unittest.SkipTest(f"application-prod.yaml not found at {cls.prod_yaml_path}")
        with open(cls.prod_yaml_path, "r", encoding="utf-8") as f:
            cls.content = f.read()

    def test_prod_profile_file_exists(self):
        """application-prod.yaml must exist under backend/src/main/resources"""
        self.assertTrue(os.path.isfile(self.prod_yaml_path))

    def test_activates_on_prod_profile(self):
        """The configuration must activate when active Spring profile is 'prod'"""
        self.assertRegex(
            self.content,
            r"on-profile:\s*['\"]?prod['\"]?",
            "Configuration must activate on profile 'prod'"
        )

    def test_datasource_url_and_tls_preference(self):
        """Production datasource URL must target external MySQL port 3306, prefer TLS, and set timezone/charset"""
        self.assertIn("sslMode=PREFERRED", self.content, "Datasource URL must specify sslMode=PREFERRED for opportunistic TLS")
        self.assertNotIn("useSSL=false", self.content, "Production datasource must not disable SSL")
        self.assertIn("characterEncoding=UTF-8", self.content, "Datasource URL must enforce UTF-8 character encoding")
        self.assertIn("serverTimezone=Asia/Ho_Chi_Minh", self.content, "Datasource URL must align with local timezone Asia/Ho_Chi_Minh")
        self.assertIn("3306", self.content, "Production datasource URL must target external MySQL default port 3306")

    def test_hikaricp_pool_bounds(self):
        """HikariCP connection pool must bound max size to 10 and min idle to 2 for 1GB micro RDS compliance"""
        self.assertRegex(
            self.content,
            r"maximum-pool-size:\s*(\$\{DB_POOL_SIZE:\s*10\}|10)",
            "HikariCP maximum pool size must be bounded to 10"
        )
        self.assertRegex(
            self.content,
            r"minimum-idle:\s*(\$\{DB_MIN_IDLE:\s*2\}|2)",
            "HikariCP minimum idle count must be bounded to 2"
        )

    def test_hibernate_sql_formatting_disabled(self):
        """Hibernate show-sql and formatting must be disabled in production to protect CloudWatch log quota"""
        self.assertRegex(
            self.content,
            r"show-sql:\s*false",
            "show-sql must be explicitly set to false in production"
        )
        self.assertRegex(
            self.content,
            r"format_sql:\s*false",
            "format_sql must be set to false in production"
        )
        self.assertRegex(
            self.content,
            r"highlight_sql:\s*false",
            "highlight_sql must be set to false in production"
        )

    def test_sql_and_transaction_log_suppression(self):
        """Verbose SQL and transaction loggers must be raised to WARN or INFO to conserve CloudWatch 5GB free tier"""
        self.assertRegex(
            self.content,
            r"org\.hibernate\.SQL:\s*WARN",
            "org.hibernate.SQL log level must be raised to WARN"
        )
        self.assertRegex(
            self.content,
            r"org\.hibernate\.orm\.jdbc\.bind:\s*WARN",
            "org.hibernate.orm.jdbc.bind log level must be raised to WARN"
        )
        self.assertRegex(
            self.content,
            r"org\.springframework\.transaction:\s*(WARN|INFO)",
            "org.springframework.transaction log level must be raised to WARN or INFO"
        )

    def test_flyway_core_and_demo_seed_migrations(self):
        """Flyway in prod must include both core schema migrations and initial demo seed migrations"""
        self.assertIn("classpath:db/migration", self.content, "Flyway must include core migrations classpath:db/migration")
        self.assertIn("classpath:db/migration-dev", self.content, "Flyway must include demo seed migrations classpath:db/migration-dev")

if __name__ == "__main__":
    unittest.main()
