-- Make task_code nullable to allow two-step creation process
ALTER TABLE tasks ALTER COLUMN task_code DROP NOT NULL;
