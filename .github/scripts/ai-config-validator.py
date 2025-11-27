#!/usr/bin/env python3
"""
AI-powered configuration validator for CI/CD pipeline
Validates Docker, YAML, and deployment configurations before deployment
"""

import json
import sys
import yaml
import re
from pathlib import Path
from typing import Dict, List, Tuple

class AIConfigValidator:
    """Validates configurations using AI-inspired heuristics and best practices"""

    def __init__(self):
        self.errors = []
        self.warnings = []
        self.suggestions = []

    def validate_all(self) -> bool:
        """Run all validation checks"""
        print("🤖 AI Configuration Validator Starting...")
        print("=" * 60)

        # Validate different configuration types
        self.validate_docker_files()
        self.validate_github_workflows()
        self.validate_docker_compose()
        self.validate_security_config()

        # Print results
        self.print_results()

        # Return success if no errors
        return len(self.errors) == 0

    def validate_docker_files(self):
        """Validate Dockerfile configurations"""
        print("\n📦 Validating Docker configurations...")

        docker_files = [
            Path("Dockerfile"),
            Path("Dockerfile.production")
        ]

        for dockerfile in docker_files:
            if not dockerfile.exists():
                continue

            print(f"  Checking {dockerfile}...")
            content = dockerfile.read_text()

            # Check for security best practices
            if "USER root" in content and "USER " not in content.split("USER root", 1)[1]:
                self.errors.append(f"{dockerfile}: Running as root user (security risk)")

            if "HEALTHCHECK" not in content and "production" in str(dockerfile):
                self.warnings.append(f"{dockerfile}: No health check defined")

            if "--no-cache" not in content and "apk add" in content:
                self.suggestions.append(f"{dockerfile}: Consider adding --no-cache to apk commands")

            # Check for multi-stage builds
            if "FROM" in content:
                from_count = content.count("FROM ")
                if from_count == 1:
                    self.suggestions.append(f"{dockerfile}: Consider multi-stage build for smaller images")
                else:
                    print(f"    ✅ Multi-stage build detected ({from_count} stages)")

            # Check for exposed ports
            if "EXPOSE" in content:
                print("    ✅ Port exposure configured")
            else:
                self.warnings.append(f"{dockerfile}: No EXPOSE directive found")

    def validate_github_workflows(self):
        """Validate GitHub Actions workflows"""
        print("\n⚙️  Validating GitHub Actions workflows...")

        workflows_dir = Path(".github/workflows")
        if not workflows_dir.exists():
            self.errors.append("No .github/workflows directory found")
            return

        workflow_files = list(workflows_dir.glob("*.yml")) + list(workflows_dir.glob("*.yaml"))

        for workflow_file in workflow_files:
            print(f"  Checking {workflow_file.name}...")

            try:
                with open(workflow_file) as f:
                    workflow = yaml.safe_load(f)

                # Check for required fields
                if "name" not in workflow:
                    self.warnings.append(f"{workflow_file.name}: No workflow name defined")

                if "on" not in workflow:
                    self.errors.append(f"{workflow_file.name}: No trigger events defined")

                # Check for permissions
                if "permissions" in workflow:
                    print("    ✅ Permissions explicitly defined")
                else:
                    self.warnings.append(f"{workflow_file.name}: No explicit permissions (uses defaults)")

                # Check for caching
                content = workflow_file.read_text()
                if "cache" in content or "actions/cache" in content:
                    print("    ✅ Caching configured")
                else:
                    self.suggestions.append(f"{workflow_file.name}: Consider adding dependency caching")

                # Check for artifact uploads
                if "upload-artifact" in content:
                    print("    ✅ Artifacts configuration found")

            except yaml.YAMLError as e:
                self.errors.append(f"{workflow_file.name}: Invalid YAML - {str(e)}")

    def validate_docker_compose(self):
        """Validate docker-compose configuration"""
        print("\n🐳 Validating docker-compose configuration...")

        compose_file = Path("docker-compose.yml")
        if not compose_file.exists():
            self.warnings.append("No docker-compose.yml found")
            return

        try:
            with open(compose_file) as f:
                compose = yaml.safe_load(f)

            if "services" not in compose:
                self.errors.append("docker-compose.yml: No services defined")
                return

            # Check each service
            for service_name, service_config in compose.get("services", {}).items():
                print(f"  Checking service: {service_name}...")

                # Check for health checks
                if "healthcheck" in service_config:
                    print(f"    ✅ Health check configured for {service_name}")
                else:
                    self.warnings.append(f"Service '{service_name}': No health check defined")

                # Check for restart policy
                if "restart" in service_config:
                    print(f"    ✅ Restart policy: {service_config['restart']}")
                else:
                    self.suggestions.append(f"Service '{service_name}': Consider adding restart policy")

                # Check for resource limits
                if "deploy" in service_config and "resources" in service_config["deploy"]:
                    print(f"    ✅ Resource limits configured for {service_name}")
                else:
                    self.suggestions.append(f"Service '{service_name}': Consider adding resource limits")

            # Check for networks
            if "networks" in compose:
                print("    ✅ Custom networks defined")

            # Check for volumes
            if "volumes" in compose:
                print("    ✅ Named volumes defined")

        except yaml.YAMLError as e:
            self.errors.append(f"docker-compose.yml: Invalid YAML - {str(e)}")

    def validate_security_config(self):
        """Validate security-related configurations"""
        print("\n🔒 Validating security configurations...")

        # Check for .gitignore
        gitignore = Path(".gitignore")
        if gitignore.exists():
            content = gitignore.read_text()
            sensitive_patterns = ['.env', 'secrets', 'credentials', '*.key', '*.pem']

            for pattern in sensitive_patterns:
                if pattern in content:
                    print(f"    ✅ Ignoring {pattern}")
                else:
                    self.warnings.append(f"Consider adding '{pattern}' to .gitignore")
        else:
            self.errors.append("No .gitignore file found")

        # Check for secrets in common places
        sensitive_files = ['.env', 'secrets.yml', 'credentials.json']
        for file in sensitive_files:
            if Path(file).exists():
                self.errors.append(f"⚠️  Sensitive file '{file}' found in repository!")

        # Check pom.xml for known vulnerable versions
        pom = Path("pom.xml")
        if pom.exists():
            content = pom.read_text()
            # This is a simplified check - in production, use actual vulnerability databases
            if "spring-boot-starter-parent" in content:
                print("    ✅ Spring Boot dependency management in use")

    def print_results(self):
        """Print validation results"""
        print("\n" + "=" * 60)
        print("📊 VALIDATION RESULTS")
        print("=" * 60)

        if self.errors:
            print(f"\n❌ ERRORS ({len(self.errors)}):")
            for error in self.errors:
                print(f"  • {error}")

        if self.warnings:
            print(f"\n⚠️  WARNINGS ({len(self.warnings)}):")
            for warning in self.warnings:
                print(f"  • {warning}")

        if self.suggestions:
            print(f"\n💡 SUGGESTIONS ({len(self.suggestions)}):")
            for suggestion in self.suggestions:
                print(f"  • {suggestion}")

        print("\n" + "=" * 60)

        if not self.errors and not self.warnings:
            print("✅ All configurations are valid!")
        elif not self.errors:
            print("⚠️  Configuration valid with warnings")
        else:
            print("❌ Configuration validation failed!")

        print("=" * 60)

def main():
    """Main entry point"""
    validator = AIConfigValidator()
    success = validator.validate_all()

    sys.exit(0 if success else 1)

if __name__ == "__main__":
    main()
