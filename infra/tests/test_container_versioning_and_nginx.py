import os
import re
import unittest

class TestContainerVersioningAndNginx(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.root_dir = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
        cls.env_example_path = os.path.join(cls.root_dir, ".env.example")
        cls.gitignore_path = os.path.join(cls.root_dir, ".gitignore")
        cls.backend_dockerfile_path = os.path.join(cls.root_dir, "backend", "Dockerfile")
        cls.frontend_dockerfile_path = os.path.join(cls.root_dir, "frontend", "Dockerfile")
        cls.nginx_conf_path = os.path.join(cls.root_dir, "frontend", "nginx.conf")
        cls.docker_compose_path = os.path.join(cls.root_dir, "docker-compose.yml")
        cls.docker_compose_prod_path = os.path.join(cls.root_dir, "docker-compose.prod.yml")
        cls.cd_prod_path = os.path.join(cls.root_dir, ".github", "workflows", "cd-prod.yml")

    def _read_file(self, path: str) -> str:
        self.assertTrue(os.path.isfile(path), f"File not found: {path}")
        with open(path, "r", encoding="utf-8") as f:
            return f.read()

    def test_env_example_documents_versioning_and_backup_variables(self):
        """Verify .env.example defines DOCKER_REGISTRY, APP_VERSION, BACKUP_DIR, BACKUP_RETENTION_COUNT, and HEALTHCHECK_TIMEOUT_SECONDS."""
        content = self._read_file(self.env_example_path)
        required_vars = [
            "DOCKER_REGISTRY",
            "APP_VERSION",
            "BACKUP_DIR",
            "BACKUP_RETENTION_COUNT",
            "HEALTHCHECK_TIMEOUT_SECONDS",
        ]
        for var in required_vars:
            self.assertIn(var, content, f".env.example is missing required variable {var}")

    def test_gitignore_ignores_deploy_state_history_and_backups(self):
        """Verify .gitignore ignores .deploy-state, .deploy-history.log, and backups/."""
        content = self._read_file(self.gitignore_path)
        self.assertIn(".deploy-state", content, ".gitignore must ignore .deploy-state")
        self.assertIn(".deploy-history.log", content, ".gitignore must ignore .deploy-history.log")
        self.assertIn("backups/", content, ".gitignore must ignore backups/")

    def test_backend_dockerfile_version_arg_and_metadata(self):
        """Verify backend Dockerfile accepts ARG APP_VERSION, sets ENV APP_VERSION, and applies OCI version labels."""
        content = self._read_file(self.backend_dockerfile_path)
        self.assertIn("ARG APP_VERSION", content, "backend/Dockerfile must declare ARG APP_VERSION")
        self.assertRegex(
            content,
            r"ENV\s+APP_VERSION=(\$\{APP_VERSION\}|\$APP_VERSION)",
            "backend/Dockerfile must set ENV APP_VERSION=${APP_VERSION}"
        )
        self.assertIn(
            "org.opencontainers.image.version",
            content,
            "backend/Dockerfile must apply org.opencontainers.image.version label"
        )

    def test_frontend_dockerfile_version_arg_and_manifest(self):
        """Verify frontend Dockerfile accepts ARG APP_VERSION, sets ENV VITE_APP_VERSION, and generates /usr/share/nginx/html/version.json."""
        content = self._read_file(self.frontend_dockerfile_path)
        self.assertIn("ARG APP_VERSION", content, "frontend/Dockerfile must declare ARG APP_VERSION")
        self.assertRegex(
            content,
            r"ENV\s+VITE_APP_VERSION=(\$\{APP_VERSION\}|\$APP_VERSION)",
            "frontend/Dockerfile must set ENV VITE_APP_VERSION=${APP_VERSION}"
        )
        self.assertIn(
            "version.json",
            content,
            "frontend/Dockerfile must generate static version.json manifest in Nginx html dir"
        )
        self.assertIn(
            "org.opencontainers.image.version",
            content,
            "frontend/Dockerfile must apply org.opencontainers.image.version label"
        )

    def test_nginx_strict_no_cache_headers(self):
        """Verify Nginx sets strict anti-caching headers on index.html, version.json, and SPA fallback."""
        content = self._read_file(self.nginx_conf_path)

        # Check location = /index.html
        self.assertIn("location = /index.html", content, "nginx.conf must have location = /index.html")

        # Check location = /version.json
        self.assertIn("location = /version.json", content, "nginx.conf must have location = /version.json")

        # Check strict no-cache headers
        expected_cache_control = 'add_header Cache-Control "no-store, no-cache, must-revalidate, proxy-revalidate, max-age=0" always;'
        expected_pragma = 'add_header Pragma "no-cache" always;'
        expected_expires = 'add_header Expires "0" always;'

        self.assertIn(expected_cache_control, content, "nginx.conf must include strict Cache-Control with always")
        self.assertIn(expected_pragma, content, "nginx.conf must include Pragma no-cache always")
        self.assertIn(expected_expires, content, "nginx.conf must include Expires 0 always")

    def test_nginx_assets_immutable_cache(self):
        """Verify Nginx caches content-hashed assets immutably for 1 year with ^~ /assets/."""
        content = self._read_file(self.nginx_conf_path)
        self.assertRegex(
            content,
            r"location\s+\^~\s+/assets/",
            "nginx.conf must declare location ^~ /assets/ for highest non-regex precedence"
        )
        self.assertIn("expires 1y;", content, "nginx.conf /assets/ must have expires 1y")
        self.assertIn(
            'add_header Cache-Control "public, max-age=31536000, immutable";',
            content,
            "nginx.conf /assets/ must have 1-year immutable Cache-Control"
        )

    def test_nginx_actuator_reverse_proxy(self):
        """Verify Nginx dynamically proxies /actuator/health and /actuator/info via $backend_upstream."""
        content = self._read_file(self.nginx_conf_path)
        self.assertRegex(
            content,
            r"location\s+~\s+\^/actuator/\(health\|info\)",
            "nginx.conf must have regex location for /actuator/(health|info)"
        )
        self.assertIn(
            "set $backend_upstream http://backend:8080;",
            content,
            "nginx.conf actuator block must set $backend_upstream variable for dynamic DNS resolution"
        )
        self.assertIn(
            "proxy_pass $backend_upstream;",
            content,
            "nginx.conf actuator block must proxy_pass $backend_upstream"
        )

    def test_docker_compose_prod_configuration(self):
        """Verify docker-compose.prod.yml defines production services with versioned images, prod profile, and actuator healthchecks."""
        self.assertTrue(os.path.isfile(self.docker_compose_prod_path), "docker-compose.prod.yml must exist")
        content = self._read_file(self.docker_compose_prod_path)

        self.assertRegex(
            content,
            r"\$\{DOCKER_REGISTRY(?::-local)?\}/gym-backend:\$\{APP_VERSION\}",
            "docker-compose.prod.yml backend must use image ${DOCKER_REGISTRY:-local}/gym-backend:${APP_VERSION}"
        )
        self.assertRegex(
            content,
            r"\$\{DOCKER_REGISTRY(?::-local)?\}/gym-frontend:\$\{APP_VERSION\}",
            "docker-compose.prod.yml frontend must use image ${DOCKER_REGISTRY:-local}/gym-frontend:${APP_VERSION}"
        )
        self.assertRegex(
            content,
            r"SPRING_PROFILES_ACTIVE:\s*(\$\{SPRING_PROFILES_ACTIVE:-prod\}|prod)",
            "docker-compose.prod.yml must activate the prod profile"
        )
        self.assertIn(
            "/actuator/health",
            content,
            "docker-compose.prod.yml backend healthcheck must check /actuator/health"
        )

    def test_docker_compose_dev_alignment(self):
        """Verify docker-compose.yml is aligned with APP_VERSION build args and actuator health checks."""
        content = self._read_file(self.docker_compose_path)
        self.assertIn(
            "APP_VERSION",
            content,
            "docker-compose.yml must support APP_VERSION build args"
        )
        self.assertIn(
            "/actuator/health",
            content,
            "docker-compose.yml backend healthcheck should verify /actuator/health"
        )

    def test_cd_prod_workflow_passes_app_version_build_arg(self):
        """Verify cd-prod.yml passes --build-arg APP_VERSION=$IMAGE_TAG to backend and frontend docker build commands."""
        content = self._read_file(self.cd_prod_path)
        backend_build_match = re.search(r"docker build\s+.*?\./backend", content)
        self.assertIsNotNone(backend_build_match, "Backend docker build command not found in cd-prod.yml")
        self.assertIn(
            "--build-arg APP_VERSION=",
            backend_build_match.group(0),
            "Backend docker build in cd-prod.yml must pass --build-arg APP_VERSION"
        )

        frontend_build_match = re.search(r"docker build\s+.*?\./frontend", content)
        self.assertIsNotNone(frontend_build_match, "Frontend docker build command not found in cd-prod.yml")
        self.assertIn(
            "--build-arg APP_VERSION=",
            frontend_build_match.group(0),
            "Frontend docker build in cd-prod.yml must pass --build-arg APP_VERSION"
        )

if __name__ == "__main__":
    unittest.main()
