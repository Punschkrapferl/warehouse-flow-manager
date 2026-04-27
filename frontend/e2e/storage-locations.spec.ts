import { expect, test } from '@playwright/test';
import { mockWarehouseApi } from './mocks/warehouse-api.mock';

test.beforeEach(async ({ page }) => {
  await mockWarehouseApi(page);
});

test('renders storage location directory and stats', async ({ page }) => {
  await page.goto('/storage-locations');

  await expect(page.getByRole('heading', { name: 'Storage locations' })).toBeVisible();

  const firstLocation = page.getByRole('button', { name: /A-01-01/ });
  await expect(firstLocation).toBeVisible();
  await expect(firstLocation).toContainText('ZONE-A');
  await expect(firstLocation).toContainText('Main demo storage location');

  const secondLocation = page.getByRole('button', { name: /B-02-01/ });
  await expect(secondLocation).toBeVisible();
  await expect(secondLocation).toContainText('ZONE-B');
  await expect(secondLocation).toContainText('Overflow picking area');

  const stats = page.locator('.directory-stats');

  await expect(stats.locator('.stat-card').filter({ hasText: /^Total\s*2$/ })).toBeVisible();
  await expect(stats.locator('.stat-card').filter({ hasText: /^Active\s*1$/ })).toBeVisible();
  await expect(stats.locator('.stat-card').filter({ hasText: /^Inactive\s*1$/ })).toBeVisible();
});

test('loads stock overview after selecting a location', async ({ page }) => {
  await page.goto('/storage-locations');

  await page.getByRole('button', { name: /A-01-01/ }).click();

  await expect(page.getByRole('heading', { name: 'Stock overview' })).toBeVisible();
  await expect(page.getByText('2 product(s) assigned to this')).toBeVisible();

  await expect(page.getByRole('row').filter({ hasText: 'Industrial Storage Bin' })).toBeVisible();
  await expect(page.getByRole('row').filter({ hasText: 'Packing Tape Roll' })).toBeVisible();
  await expect(page.getByText('Low stock items', { exact: true })).toBeVisible();
});

test('filters storage locations by search term', async ({ page }) => {
  await page.goto('/storage-locations');

  await page.locator('#location-search').fill('overflow');

  await expect(page.getByRole('button', { name: /B-02-01/ })).toBeVisible();
  await expect(page.getByRole('button', { name: /Overflow picking area/ })).toBeVisible();
  await expect(page.getByRole('button', { name: /A-01-01/ })).toHaveCount(0);
});

test('refresh overview button is disabled until a location is selected', async ({ page }) => {
  await page.goto('/storage-locations');

  const refreshOverviewButton = page.getByRole('button', { name: 'Refresh overview' });

  await expect(refreshOverviewButton).toBeDisabled();

  await page.getByRole('button', { name: /A-01-01/ }).click();

  await expect(refreshOverviewButton).toBeEnabled();

  await refreshOverviewButton.click();

  await expect(page.getByRole('row').filter({ hasText: 'Industrial Storage Bin' })).toBeVisible();
});
