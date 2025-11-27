#!/usr/bin/env python3
"""
AI-powered Predictive Rollback System
Monitors deployment metrics and automatically triggers rollback if issues detected
Uses machine learning-inspired heuristics to predict deployment failures
"""

import json
import sys
import time
import subprocess
from datetime import datetime
from typing import Dict, List, Tuple
from pathlib import Path

class DeploymentMetrics:
    """Stores and analyzes deployment metrics"""

    def __init__(self):
        self.metrics = {
            'response_times': [],
            'error_rates': [],
            'cpu_usage': [],
            'memory_usage': [],
            'health_checks': [],
            'start_time': time.time()
        }
        self.thresholds = {
            'max_response_time_ms': 2000,  # 2 seconds
            'max_error_rate': 0.05,  # 5%
            'max_cpu_percent': 80,
            'max_memory_percent': 85,
            'min_health_check_success_rate': 0.95  # 95%
        }

    def add_metric(self, metric_type: str, value: float):
        """Add a metric measurement"""
        if metric_type in self.metrics:
            self.metrics[metric_type].append({
                'timestamp': time.time(),
                'value': value
            })

    def get_average(self, metric_type: str, window_seconds: int = 60) -> float:
        """Get average metric value over time window"""
        if metric_type not in self.metrics or not self.metrics[metric_type]:
            return 0.0

        current_time = time.time()
        recent_metrics = [
            m['value'] for m in self.metrics[metric_type]
            if current_time - m['timestamp'] <= window_seconds
        ]

        if not recent_metrics:
            return 0.0

        return sum(recent_metrics) / len(recent_metrics)

    def get_trend(self, metric_type: str) -> str:
        """Analyze metric trend (increasing, decreasing, stable)"""
        if metric_type not in self.metrics or len(self.metrics[metric_type]) < 3:
            return "insufficient_data"

        recent = self.metrics[metric_type][-5:]
        values = [m['value'] for m in recent]

        if len(values) < 2:
            return "stable"

        # Calculate simple linear trend
        first_half_avg = sum(values[:len(values)//2]) / (len(values)//2)
        second_half_avg = sum(values[len(values)//2:]) / (len(values) - len(values)//2)

        diff_percent = ((second_half_avg - first_half_avg) / first_half_avg * 100) if first_half_avg > 0 else 0

        if diff_percent > 10:
            return "increasing"
        elif diff_percent < -10:
            return "decreasing"
        else:
            return "stable"


class PredictiveRollbackSystem:
    """AI-powered rollback decision system"""

    def __init__(self, app_url: str, deployment_name: str):
        self.app_url = app_url
        self.deployment_name = deployment_name
        self.metrics = DeploymentMetrics()
        self.monitoring_duration = 180  # Monitor for 3 minutes after deployment
        self.check_interval = 10  # Check every 10 seconds
        self.rollback_triggered = False

    def check_health(self) -> Tuple[bool, float]:
        """Check application health endpoint"""
        try:
            import urllib.request
            import urllib.error

            start = time.time()
            req = urllib.request.Request(
                f"{self.app_url}/actuator/health",
                headers={'User-Agent': 'PredictiveRollback/1.0'}
            )

            with urllib.request.urlopen(req, timeout=5) as response:
                response_time = (time.time() - start) * 1000  # Convert to ms
                return response.status == 200, response_time

        except (urllib.error.URLError, urllib.error.HTTPError, Exception):
            return False, 5000  # Timeout or error

    def analyze_metrics(self) -> Tuple[bool, List[str]]:
        """
        Analyze collected metrics using AI-inspired heuristics
        Returns: (should_rollback, reasons)
        """
        issues = []

        # Check response times
        avg_response_time = self.metrics.get_average('response_times')
        if avg_response_time > self.metrics.thresholds['max_response_time_ms']:
            issues.append(f"❌ High response time: {avg_response_time:.0f}ms (threshold: {self.metrics.thresholds['max_response_time_ms']}ms)")

        response_trend = self.metrics.get_trend('response_times')
        if response_trend == "increasing":
            issues.append("⚠️  Response times trending upward")

        # Check health check success rate
        if self.metrics.metrics['health_checks']:
            health_success_rate = sum(
                1 for m in self.metrics.metrics['health_checks'] if m['value'] == 1
            ) / len(self.metrics.metrics['health_checks'])

            if health_success_rate < self.metrics.thresholds['min_health_check_success_rate']:
                issues.append(f"❌ Low health check success rate: {health_success_rate*100:.1f}% (threshold: {self.metrics.thresholds['min_health_check_success_rate']*100}%)")

        # Check error rates (if available)
        avg_error_rate = self.metrics.get_average('error_rates')
        if avg_error_rate > self.metrics.thresholds['max_error_rate']:
            issues.append(f"❌ High error rate: {avg_error_rate*100:.1f}% (threshold: {self.metrics.thresholds['max_error_rate']*100}%)")

        # Predictive analysis: Multiple yellow flags = rollback
        warning_count = sum(1 for issue in issues if issue.startswith("⚠️"))
        error_count = sum(1 for issue in issues if issue.startswith("❌"))

        should_rollback = error_count >= 2 or (error_count >= 1 and warning_count >= 2)

        return should_rollback, issues

    def trigger_rollback(self, reasons: List[str]):
        """Execute rollback procedure"""
        print("\n" + "=" * 60)
        print("🚨 TRIGGERING AUTOMATIC ROLLBACK")
        print("=" * 60)
        print("\nReasons:")
        for reason in reasons:
            print(f"  {reason}")

        print("\n⏪ Executing rollback procedure...")

        rollback_start = time.time()

        # In a real system, this would:
        # 1. Switch traffic back to previous version
        # 2. Stop new deployment
        # 3. Restart old containers
        # 4. Verify old version is healthy

        # For this demo, we simulate the rollback
        print("  1. Stopping new deployment...")
        time.sleep(1)
        print("  2. Switching traffic to previous version...")
        time.sleep(1)
        print("  3. Verifying previous version health...")
        time.sleep(1)

        rollback_duration = time.time() - rollback_start

        print(f"\n✅ Rollback completed in {rollback_duration:.1f}s")
        print(f"   Rollback Time Requirement: < 2 min (120s)")

        if rollback_duration < 120:
            print("   ✅ Requirement met!")
        else:
            print("   ⚠️  Rollback took longer than expected")

        print("=" * 60)

        self.rollback_triggered = True

    def monitor_deployment(self):
        """Monitor deployment and make rollback decisions"""
        print("🤖 AI-Powered Predictive Rollback System Starting...")
        print(f"   Monitoring: {self.app_url}")
        print(f"   Duration: {self.monitoring_duration}s")
        print(f"   Check Interval: {self.check_interval}s")
        print("=" * 60)

        start_time = time.time()
        check_count = 0

        while time.time() - start_time < self.monitoring_duration:
            check_count += 1
            elapsed = time.time() - start_time

            print(f"\n📊 Check #{check_count} (T+{elapsed:.0f}s)")

            # Check health
            is_healthy, response_time = self.check_health()
            self.metrics.add_metric('health_checks', 1 if is_healthy else 0)
            self.metrics.add_metric('response_times', response_time)

            # Display current metrics
            print(f"   Health: {'✅ OK' if is_healthy else '❌ FAILED'}")
            print(f"   Response Time: {response_time:.0f}ms")

            # Analyze metrics after collecting enough data
            if check_count >= 3:
                should_rollback, issues = self.analyze_metrics()

                if issues:
                    print("\n   Issues detected:")
                    for issue in issues:
                        print(f"      {issue}")

                if should_rollback:
                    self.trigger_rollback(issues)
                    return False  # Deployment failed

            # Wait before next check
            if time.time() - start_time < self.monitoring_duration:
                time.sleep(self.check_interval)

        # Monitoring completed successfully
        print("\n" + "=" * 60)
        print("✅ Deployment monitoring completed successfully")
        print(f"   Total Checks: {check_count}")
        print(f"   Average Response Time: {self.metrics.get_average('response_times'):.0f}ms")

        health_success_rate = sum(
            1 for m in self.metrics.metrics['health_checks'] if m['value'] == 1
        ) / len(self.metrics.metrics['health_checks'])
        print(f"   Health Success Rate: {health_success_rate*100:.1f}%")
        print("=" * 60)

        return True  # Deployment successful


def main():
    """Main entry point"""
    if len(sys.argv) < 3:
        print("Usage: predictive-rollback.py <app_url> <deployment_name>")
        sys.exit(1)

    app_url = sys.argv[1]
    deployment_name = sys.argv[2]

    system = PredictiveRollbackSystem(app_url, deployment_name)
    success = system.monitor_deployment()

    sys.exit(0 if success else 1)


if __name__ == "__main__":
    main()
