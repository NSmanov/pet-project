-- Create tasks table
CREATE TABLE IF NOT EXISTS tasks (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description VARCHAR(1000),
    status VARCHAR(50) NOT NULL,
    priority VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- Create indexes for common query patterns
CREATE INDEX IF NOT EXISTS idx_tasks_status ON tasks(status) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_tasks_priority ON tasks(priority) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_tasks_created_at ON tasks(created_at DESC) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_tasks_deleted ON tasks(deleted);
CREATE INDEX IF NOT EXISTS idx_tasks_title ON tasks(title) WHERE deleted = FALSE;

-- Add comments for documentation
COMMENT ON TABLE tasks IS 'Main table for storing task information';
COMMENT ON COLUMN tasks.id IS 'Primary key, auto-incrementing';
COMMENT ON COLUMN tasks.title IS 'Task title, maximum 200 characters';
COMMENT ON COLUMN tasks.description IS 'Detailed task description, maximum 1000 characters';
COMMENT ON COLUMN tasks.status IS 'Current task status: TODO, IN_PROGRESS, DONE, CANCELLED';
COMMENT ON COLUMN tasks.priority IS 'Task priority: LOW, MEDIUM, HIGH, CRITICAL';
COMMENT ON COLUMN tasks.created_at IS 'Timestamp when task was created';
COMMENT ON COLUMN tasks.updated_at IS 'Timestamp when task was last updated';
COMMENT ON COLUMN tasks.deleted IS 'Soft delete flag, true if task is deleted';
