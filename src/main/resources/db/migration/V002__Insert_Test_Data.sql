-- Insert sample tasks for testing
INSERT INTO tasks (title, description, status, priority, created_at, updated_at, deleted)
VALUES
    ('Setup Development Environment', 'Install Java 21, Maven, PostgreSQL and IDE', 'DONE', 'HIGH', NOW() - INTERVAL '5 days', NOW() - INTERVAL '4 days', FALSE),
    ('Create Project Structure', 'Setup Spring Boot project with required dependencies', 'DONE', 'HIGH', NOW() - INTERVAL '4 days', NOW() - INTERVAL '3 days', FALSE),
    ('Implement REST API', 'Create controllers, services and repositories', 'IN_PROGRESS', 'HIGH', NOW() - INTERVAL '3 days', NOW(), FALSE),
    ('Write Unit Tests', 'Add unit tests for all service methods', 'TODO', 'MEDIUM', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days', FALSE),
    ('Write Integration Tests', 'Add integration tests using TestContainers', 'TODO', 'MEDIUM', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days', FALSE),
    ('Setup GitHub Actions', 'Configure CI/CD pipeline for automated testing', 'TODO', 'CRITICAL', NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day', FALSE),
    ('Write Documentation', 'Create comprehensive README and API documentation', 'TODO', 'LOW', NOW(), NOW(), FALSE),
    ('Code Review', 'Review code for best practices and optimizations', 'TODO', 'MEDIUM', NOW(), NOW(), FALSE),
    ('Deploy to Production', 'Deploy application to production environment', 'CANCELLED', 'LOW', NOW() - INTERVAL '6 days', NOW() - INTERVAL '5 days', FALSE);
