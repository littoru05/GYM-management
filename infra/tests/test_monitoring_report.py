import os
import unittest

class TestMonitoringReport(unittest.TestCase):
    def setUp(self):
        self.repo_root = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
        self.doc_path = os.path.join(self.repo_root, "docs", "Bao_cao_giam_sat_tai_nguyen_AWS.pdf")
        self.script_path = os.path.join(self.repo_root, "scripts", "generate-aws-monitoring-report.py")

    def test_script_exists(self):
        self.assertTrue(os.path.isfile(self.script_path), f"Script not found at {self.script_path}")

    def test_pdf_report_exists_and_not_empty(self):
        self.assertTrue(os.path.isfile(self.doc_path), f"PDF report not found at {self.doc_path}")
        file_size = os.path.getsize(self.doc_path)
        self.assertGreater(file_size, 50000, f"PDF file size is suspiciously small: {file_size} bytes")

    def test_pdf_header_signature(self):
        if os.path.isfile(self.doc_path):
            with open(self.doc_path, "rb") as f:
                header = f.read(5)
                self.assertEqual(header, b"%PDF-", "File is not a valid PDF document")

if __name__ == "__main__":
    unittest.main()
