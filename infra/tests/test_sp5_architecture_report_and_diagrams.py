import os
import re
import unittest

class TestSP5ArchitectureReportAndDiagrams(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.root_dir = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
        cls.report_path = os.path.join(cls.root_dir, "docs", "reports", "sp5-aws-architecture-and-cicd-report.md")
        cls.html_path = os.path.join(cls.root_dir, "docs", "architecture", "sp5-aws-architecture-diagram.html")

    def test_report_file_exists_and_substantial(self):
        """Technical report must exist at docs/reports/sp5-aws-architecture-and-cicd-report.md and have substantial content"""
        self.assertTrue(os.path.isfile(self.report_path), f"Report not found at {self.report_path}")
        file_size = os.path.getsize(self.report_path)
        self.assertGreater(file_size, 5000, f"Report file size is suspiciously small: {file_size} bytes")

    def test_html_diagram_file_exists_and_substantial(self):
        """Standalone HTML diagram must exist at docs/architecture/sp5-aws-architecture-diagram.html and have substantial content"""
        self.assertTrue(os.path.isfile(self.html_path), f"HTML diagram not found at {self.html_path}")
        file_size = os.path.getsize(self.html_path)
        self.assertGreater(file_size, 3000, f"HTML diagram file size is suspiciously small: {file_size} bytes")

    def test_report_contains_valid_mermaid_blocks(self):
        """Report must contain at least 2 valid Mermaid diagrams (Infrastructure and CI/CD)"""
        self.assertTrue(os.path.isfile(self.report_path))
        with open(self.report_path, "r", encoding="utf-8") as f:
            content = f.read()

        mermaid_blocks = re.findall(r"```mermaid\s*\n(.*?)\n```", content, re.DOTALL)
        self.assertGreaterEqual(len(mermaid_blocks), 2, "Expected at least 2 Mermaid diagram blocks in report")

        for idx, block in enumerate(mermaid_blocks):
            lines = [line.strip() for line in block.splitlines() if line.strip() and not line.strip().startswith("%%")]
            self.assertTrue(len(lines) > 5, f"Mermaid block {idx + 1} has too few lines")
            # First non-comment line should define diagram type
            header = lines[0]
            valid_headers = ("flowchart", "graph", "sequenceDiagram", "stateDiagram", "classDiagram", "erDiagram")
            self.assertTrue(any(header.startswith(vh) for vh in valid_headers),
                            f"Block {idx + 1} does not start with valid Mermaid header: {header}")

    def test_report_infra_diagram_covers_production_components(self):
        """Infrastructure diagram must cover all production networking, compute, database, and security components"""
        self.assertTrue(os.path.isfile(self.report_path))
        with open(self.report_path, "r", encoding="utf-8") as f:
            content = f.read()

        required_tokens = [
            "10.1.0.0/16",       # VPC CIDR
            "10.1.1.0/24",       # Public Subnet 1
            "10.1.2.0/24",       # Public Subnet 2
            "10.1.10.0/24",      # Database Subnet 1
            "10.1.11.0/24",      # Database Subnet 2
            "ALB",               # Application Load Balancer
            "ECS",               # Elastic Container Service
            "Fargate",           # Serverless compute
            "RDS",               # Relational Database Service
            "MySQL",             # Database engine
            "ECR",               # Container Registry
            "CloudWatch",        # Observability
            "Internet Gateway",  # IGW
            "Security Group",    # SGs
            "OIDC",              # GitHub Actions OIDC Auth
            "SSM",               # Parameter Store
            "DuckDNS",           # Dynamic DNS
        ]

        for token in required_tokens:
            self.assertIn(token.lower(), content.lower(), f"Expected token '{token}' in technical report")

    def test_report_cicd_diagram_covers_pipeline_flow(self):
        """CI/CD diagram must cover commit, build, pre-migration backup, expand-contract, deploy, and rollback"""
        self.assertTrue(os.path.isfile(self.report_path))
        with open(self.report_path, "r", encoding="utf-8") as f:
            content = f.read()

        cicd_tokens = [
            "GitHub Actions",
            "OIDC",
            "Docker",
            "ECR",
            "Pre-Migration",
            "Flyway",
            "Expand-Contract",
            "Health",
            "Rollback",
        ]

        for token in cicd_tokens:
            self.assertIn(token.lower(), content.lower(), f"Expected CI/CD token '{token}' in technical report")

    def test_report_contains_comprehensive_sections(self):
        """Report must contain detailed technical sections addressing all requirements"""
        self.assertTrue(os.path.isfile(self.report_path))
        with open(self.report_path, "r", encoding="utf-8") as f:
            content = f.read()

        required_sections = [
            "Nguyên lý thiết kế",
            "Sơ đồ kiến trúc",
            "Quy trình CI/CD",
            "Cơ chế an toàn cơ sở dữ liệu",
            "Tối ưu hóa chi phí",
            "Cơ chế tự phục hồi",
        ]

        for section in required_sections:
            self.assertIn(section.lower(), content.lower(), f"Section '{section}' missing from technical report")

    def test_html_visualization_has_export_and_interactive_controls(self):
        """Standalone HTML diagram must provide tab switcher, Pan/Zoom, and export buttons (SVG/PNG)"""
        self.assertTrue(os.path.isfile(self.html_path))
        with open(self.html_path, "r", encoding="utf-8") as f:
            content = f.read()

        self.assertIn("<!DOCTYPE html>", content)
        self.assertIn("<title>", content)
        self.assertIn("mermaid", content.lower())
        # Check interactive and export features
        self.assertTrue(any(term in content.lower() for term in ["export", "download", "svg", "png"]),
                        "HTML diagram should provide export options")
        self.assertTrue(any(term in content.lower() for term in ["tab", "switch", "view-btn", "nav"]),
                        "HTML diagram should provide diagram switching / tabs")

if __name__ == "__main__":
    unittest.main()
