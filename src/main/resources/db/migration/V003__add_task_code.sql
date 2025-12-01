-- Add task_code column to tasks table
ALTER TABLE tasks ADD COLUMN task_code VARCHAR(50);

-- Create unique index on task_code
CREATE UNIQUE INDEX idx_tasks_task_code ON tasks(task_code) WHERE deleted = false;

-- Add comment
COMMENT ON COLUMN tasks.task_code IS 'Unique task identifier like TASK-123';

-- Generate task codes for existing tasks
UPDATE tasks
SET task_code = 'TASK-' || id
WHERE task_code IS NULL;

-- Make task_code NOT NULL after populating existing rows
ALTER TABLE tasks ALTER COLUMN task_code SET NOT NULL;
