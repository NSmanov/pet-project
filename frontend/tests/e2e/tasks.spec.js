import { test, expect } from '@playwright/test';

/**
 * E2E Tests for Task Management System
 *
 * These tests verify the main user flows:
 * - Loading and displaying tasks
 * - Creating new tasks
 * - Updating existing tasks
 * - Deleting tasks
 * - Filtering and searching tasks
 */

test.describe('Task Management System - Main Flows', () => {

  test.beforeEach(async ({ page }) => {
    // Navigate to the application before each test
    await page.goto('/');
    // Wait for the app to load
    await page.waitForLoadState('networkidle');
  });

  test('should load the application and display header', async ({ page }) => {
    // Check that the main title is visible
    await expect(page.locator('h1')).toContainText('Task Management System');

    // Check that the subtitle is visible
    await expect(page.locator('.subtitle')).toContainText('Управление задачами');
  });

  test('should display statistics dashboard', async ({ page }) => {
    // Check that all statistics cards are visible
    await expect(page.locator('.stat-total')).toBeVisible();
    await expect(page.locator('.stat-todo')).toBeVisible();
    await expect(page.locator('.stat-progress')).toBeVisible();
    await expect(page.locator('.stat-done')).toBeVisible();

    // Check that statistics have numerical values
    const totalTasks = await page.locator('.stat-total .stat-value').textContent();
    expect(Number(totalTasks)).toBeGreaterThanOrEqual(0);
  });

  test('should display existing tasks', async ({ page }) => {
    // Wait for tasks to load
    await page.waitForSelector('.tasks-grid, .empty-state', { timeout: 5000 });

    // Check if tasks are displayed or empty state is shown
    const hasTasksGrid = await page.locator('.tasks-grid').isVisible().catch(() => false);
    const hasEmptyState = await page.locator('.empty-state').isVisible().catch(() => false);

    expect(hasTasksGrid || hasEmptyState).toBeTruthy();
  });

  test('should create a new task', async ({ page }) => {
    // Click the "Create Task" button
    await page.click('button.btn-create');

    // Wait for the form modal to appear
    await expect(page.locator('.task-form-container')).toBeVisible();

    // Fill in the task form
    const uniqueTitle = `Test Task ${Date.now()}`;
    await page.fill('input[name="title"]', uniqueTitle);
    await page.fill('textarea[name="description"]', 'This is a test task created by Playwright');
    await page.selectOption('select[name="priority"]', 'HIGH');

    // Submit the form
    await page.click('button[type="submit"]');

    // Wait for success notification
    await expect(page.locator('.Toastify__toast--success')).toBeVisible({ timeout: 5000 });

    // Verify the task appears in the list
    await page.waitForTimeout(1000); // Wait for UI to update
    await expect(page.locator(`text=${uniqueTitle}`)).toBeVisible();
  });

  test('should edit an existing task', async ({ page }) => {
    // Wait for tasks to load
    await page.waitForSelector('.tasks-grid, .empty-state', { timeout: 5000 });

    // Check if there are any tasks
    const tasksExist = await page.locator('.task-card').count() > 0;

    if (!tasksExist) {
      // Create a task first if none exist
      await page.click('button.btn-create');
      await page.fill('input[name="title"]', 'Task to Edit');
      await page.fill('textarea[name="description"]', 'Description');
      await page.selectOption('select[name="priority"]', 'MEDIUM');
      await page.click('button[type="submit"]');
      await page.waitForTimeout(1000);
    }

    // Find the first task's edit button
    const editButton = page.locator('.task-card').first().locator('button:has-text("✏️")');
    await editButton.click();

    // Wait for the edit form to appear
    await expect(page.locator('.task-form-container')).toBeVisible();

    // Modify the task
    const updatedTitle = `Updated Task ${Date.now()}`;
    await page.fill('input[name="title"]', updatedTitle);
    await page.selectOption('select[name="status"]', 'IN_PROGRESS');

    // Submit the form
    await page.click('button[type="submit"]');

    // Wait for success notification
    await expect(page.locator('.Toastify__toast--success')).toBeVisible({ timeout: 5000 });

    // Verify the task was updated
    await page.waitForTimeout(1000);
    await expect(page.locator(`text=${updatedTitle}`)).toBeVisible();
  });

  test('should delete a task', async ({ page }) => {
    // Wait for tasks to load
    await page.waitForSelector('.tasks-grid, .empty-state', { timeout: 5000 });

    // Check if there are any tasks
    let tasksExist = await page.locator('.task-card').count() > 0;

    if (!tasksExist) {
      // Create a task first if none exist
      await page.click('button.btn-create');
      const taskTitle = `Task to Delete ${Date.now()}`;
      await page.fill('input[name="title"]', taskTitle);
      await page.fill('textarea[name="description"]', 'This task will be deleted');
      await page.selectOption('select[name="priority"]', 'LOW');
      await page.click('button[type="submit"]');
      await page.waitForTimeout(1000);
    }

    // Get the count of tasks before deletion
    const initialTaskCount = await page.locator('.task-card').count();

    // Get the title of the first task to verify deletion
    const taskToDeleteTitle = await page.locator('.task-card').first().locator('h3').textContent();

    // Click the delete button on the first task
    const deleteButton = page.locator('.task-card').first().locator('button:has-text("🗑️")');

    // Handle the confirmation dialog
    page.on('dialog', async dialog => {
      expect(dialog.message()).toContain('Вы уверены');
      await dialog.accept();
    });

    await deleteButton.click();

    // Wait for success notification
    await expect(page.locator('.Toastify__toast--success')).toBeVisible({ timeout: 5000 });

    // Wait for UI to update
    await page.waitForTimeout(1000);

    // Verify the task was deleted (count decreased or empty state shown)
    const currentTaskCount = await page.locator('.task-card').count();
    expect(currentTaskCount).toBeLessThan(initialTaskCount);

    // Verify the specific task is no longer visible
    const taskStillExists = await page.locator(`text=${taskToDeleteTitle}`).isVisible().catch(() => false);
    expect(taskStillExists).toBeFalsy();
  });

  test('should filter tasks by status', async ({ page }) => {
    // Wait for tasks to load
    await page.waitForSelector('.tasks-grid, .empty-state', { timeout: 5000 });

    // Select "To Do" status filter
    await page.selectOption('select.filter-select >> nth=0', 'TODO');

    // Wait for filtering to complete
    await page.waitForTimeout(500);

    // Check if filtered results are displayed
    const taskCards = page.locator('.task-card');
    const count = await taskCards.count();

    if (count > 0) {
      // Verify all visible tasks have TODO status
      const statusBadges = await taskCards.locator('.status-badge').allTextContents();
      statusBadges.forEach(badge => {
        expect(badge.toLowerCase()).toContain('todo');
      });
    }
  });

  test('should filter tasks by priority', async ({ page }) => {
    // Wait for tasks to load
    await page.waitForSelector('.tasks-grid, .empty-state', { timeout: 5000 });

    // Select "HIGH" priority filter
    await page.selectOption('select.filter-select >> nth=1', 'HIGH');

    // Wait for filtering to complete
    await page.waitForTimeout(500);

    // Check if filtered results are displayed
    const taskCards = page.locator('.task-card');
    const count = await taskCards.count();

    if (count > 0) {
      // Verify all visible tasks have HIGH priority
      const priorityBadges = await taskCards.locator('.priority-badge').allTextContents();
      priorityBadges.forEach(badge => {
        expect(badge).toContain('HIGH');
      });
    }
  });

  test('should search tasks by keyword', async ({ page }) => {
    // Wait for tasks to load
    await page.waitForSelector('.tasks-grid, .empty-state', { timeout: 5000 });

    // Type in the search box
    const searchInput = page.locator('input.search-input');
    await searchInput.fill('Test');

    // Wait for search to complete
    await page.waitForTimeout(500);

    // Check results
    const taskCards = page.locator('.task-card');
    const count = await taskCards.count();

    if (count > 0) {
      // Verify that search results contain the keyword
      const taskTitles = await taskCards.locator('h3').allTextContents();
      const hasMatchingTitle = taskTitles.some(title =>
        title.toLowerCase().includes('test')
      );
      expect(hasMatchingTitle).toBeTruthy();
    }
  });

  test('should clear all filters', async ({ page }) => {
    // Wait for tasks to load
    await page.waitForSelector('.tasks-grid, .empty-state', { timeout: 5000 });

    // Apply multiple filters
    await page.selectOption('select.filter-select >> nth=0', 'TODO');
    await page.selectOption('select.filter-select >> nth=1', 'HIGH');
    await page.fill('input.search-input', 'Test');

    // Wait for filters to apply
    await page.waitForTimeout(500);

    // Click the "Clear Filters" button
    const clearButton = page.locator('button.btn-clear-filters');
    await expect(clearButton).toBeVisible();
    await clearButton.click();

    // Verify filters are cleared
    await expect(page.locator('input.search-input')).toHaveValue('');
    await expect(page.locator('select.filter-select >> nth=0')).toHaveValue('ALL');
    await expect(page.locator('select.filter-select >> nth=1')).toHaveValue('ALL');
  });

  test('should cancel task creation', async ({ page }) => {
    // Click the "Create Task" button
    await page.click('button.btn-create');

    // Wait for the form modal to appear
    await expect(page.locator('.task-form-container')).toBeVisible();

    // Fill in some data
    await page.fill('input[name="title"]', 'Task to Cancel');

    // Click cancel button
    await page.click('button:has-text("Отмена")');

    // Verify the modal is closed
    await expect(page.locator('.task-form-container')).not.toBeVisible();
  });

  test('should validate required fields on task creation', async ({ page }) => {
    // Click the "Create Task" button
    await page.click('button.btn-create');

    // Wait for the form modal to appear
    await expect(page.locator('.task-form-container')).toBeVisible();

    // Try to submit without filling required fields
    await page.click('button[type="submit"]');

    // Check that form validation prevents submission
    const titleInput = page.locator('input[name="title"]');
    await expect(titleInput).toBeFocused();
  });
});
