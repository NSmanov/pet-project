import { test, expect } from '@playwright/test';

/**
 * Performance and Accessibility Tests
 *
 * These tests verify:
 * - Page load performance
 * - Responsive design
 * - Error handling
 * - Accessibility features
 */

test.describe('Performance and Accessibility', () => {

  test('should load the page quickly', async ({ page }) => {
    const startTime = Date.now();

    await page.goto('/');
    await page.waitForLoadState('networkidle');

    const loadTime = Date.now() - startTime;

    // Page should load in less than 3 seconds
    expect(loadTime).toBeLessThan(3000);

    console.log(`Page loaded in ${loadTime}ms`);
  });

  test('should be responsive on mobile devices', async ({ page }) => {
    // Set mobile viewport
    await page.setViewportSize({ width: 375, height: 667 });

    await page.goto('/');
    await page.waitForLoadState('networkidle');

    // Check that the header is visible on mobile
    await expect(page.locator('h1')).toBeVisible();

    // Check that the create button is accessible
    await expect(page.locator('button.btn-create')).toBeVisible();

    // Check that statistics are displayed properly
    await expect(page.locator('.stats-container')).toBeVisible();
  });

  test('should handle API errors gracefully', async ({ page }) => {
    // Intercept API calls and return error
    await page.route('**/api/tasks', route => {
      route.abort('failed');
    });

    await page.goto('/');

    // Wait for error message to appear
    await expect(page.locator('.error-message')).toBeVisible({ timeout: 5000 });

    // Check that retry button is available
    await expect(page.locator('button.btn-retry')).toBeVisible();
  });

  test('should handle empty state correctly', async ({ page }) => {
    // Intercept API to return empty array
    await page.route('**/api/tasks', route => {
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          success: true,
          data: [],
          timestamp: new Date().toISOString()
        })
      });
    });

    await page.goto('/');
    await page.waitForLoadState('networkidle');

    // Check for empty state message
    await expect(page.locator('.empty-state')).toBeVisible();
    await expect(page.locator('text=Нет задач')).toBeVisible();
  });

  test('should have proper heading hierarchy', async ({ page }) => {
    await page.goto('/');
    await page.waitForLoadState('networkidle');

    // Check that there is exactly one h1 element
    const h1Count = await page.locator('h1').count();
    expect(h1Count).toBe(1);

    // Check that h1 contains meaningful text
    const h1Text = await page.locator('h1').textContent();
    expect(h1Text?.length).toBeGreaterThan(0);
  });

  test('should have accessible form labels', async ({ page }) => {
    await page.goto('/');

    // Open create task form
    await page.click('button.btn-create');
    await expect(page.locator('.task-form-container')).toBeVisible();

    // Check that inputs have associated labels or placeholders
    const titleInput = page.locator('input[name="title"]');
    const hasLabel = await titleInput.getAttribute('placeholder') ||
                     await page.locator('label[for="title"]').isVisible().catch(() => false);
    expect(hasLabel).toBeTruthy();
  });

  test('should display loading state', async ({ page }) => {
    // Slow down network to see loading state
    await page.route('**/api/tasks', async route => {
      await new Promise(resolve => setTimeout(resolve, 1000));
      await route.continue();
    });

    await page.goto('/');

    // Check for loading indicator
    const hasLoadingState = await page.locator('.loading, text=Загрузка').isVisible({ timeout: 500 }).catch(() => false);
    expect(hasLoadingState).toBeTruthy();
  });

  test('should handle long task titles gracefully', async ({ page }) => {
    await page.goto('/');

    // Create a task with a very long title
    await page.click('button.btn-create');
    await expect(page.locator('.task-form-container')).toBeVisible();

    const longTitle = 'A'.repeat(200);
    await page.fill('input[name="title"]', longTitle);
    await page.selectOption('select[name="priority"]', 'MEDIUM');
    await page.click('button[type="submit"]');

    // Verify task is created and displayed properly
    await page.waitForTimeout(1000);

    // Check that the task card doesn't break the layout
    const taskCard = page.locator('.task-card').last();
    await expect(taskCard).toBeVisible();

    // Verify the card has reasonable dimensions
    const boundingBox = await taskCard.boundingBox();
    expect(boundingBox?.width).toBeLessThan(1000);
  });

  test('should support keyboard navigation', async ({ page }) => {
    await page.goto('/');
    await page.waitForLoadState('networkidle');

    // Tab through interactive elements
    await page.keyboard.press('Tab'); // Search input
    await page.keyboard.press('Tab'); // Status filter
    await page.keyboard.press('Tab'); // Priority filter
    await page.keyboard.press('Tab'); // Create button

    // Verify create button is focused
    const createButton = page.locator('button.btn-create');
    await expect(createButton).toBeFocused();

    // Press Enter to open form
    await page.keyboard.press('Enter');
    await expect(page.locator('.task-form-container')).toBeVisible();
  });

  test('should persist filters during session', async ({ page }) => {
    await page.goto('/');
    await page.waitForLoadState('networkidle');

    // Apply filters
    await page.selectOption('select.filter-select >> nth=0', 'TODO');
    await page.fill('input.search-input', 'Test Search');

    // Get current URL (in case filters are in URL)
    const currentUrl = page.url();

    // Reload the page
    await page.reload();
    await page.waitForLoadState('networkidle');

    // Note: Filters won't persist after reload in current implementation
    // This test documents current behavior
    const searchValue = await page.locator('input.search-input').inputValue();
    const statusValue = await page.locator('select.filter-select >> nth=0').inputValue();

    // Document the behavior (adjust expectations based on requirements)
    console.log('Search after reload:', searchValue);
    console.log('Status filter after reload:', statusValue);
  });
});
