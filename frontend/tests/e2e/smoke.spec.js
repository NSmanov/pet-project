import { test, expect } from '@playwright/test';

/**
 * Smoke Tests
 *
 * Quick sanity checks to verify the application is working
 * These tests should run fast and cover basic functionality
 */

test.describe('Smoke Tests', () => {

  test('application loads successfully', async ({ page }) => {
    await page.goto('/');

    // Check main elements are visible
    await expect(page.locator('h1')).toBeVisible();
    await expect(page.locator('.stats-container')).toBeVisible();
    await expect(page.locator('button.btn-create')).toBeVisible();

    console.log('✅ Application loaded successfully');
  });

  test('can interact with search and filters', async ({ page }) => {
    await page.goto('/');

    // Test search input
    const searchInput = page.locator('input.search-input');
    await expect(searchInput).toBeVisible();
    await searchInput.fill('test');
    await expect(searchInput).toHaveValue('test');

    // Test status filter
    const statusFilter = page.locator('select.filter-select').first();
    await expect(statusFilter).toBeVisible();

    console.log('✅ Search and filters are interactive');
  });
});
