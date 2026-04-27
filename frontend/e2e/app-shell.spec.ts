import { expect, test } from '@playwright/test';
import { mockWarehouseApi } from './mocks/warehouse-api.mock';

test.beforeEach(async ({ page }) => {
  await mockWarehouseApi(page);
});

test('redirects to dashboard and renders the application shell', async ({ page }) => {
  await page.goto('/');

  await expect(page).toHaveURL(/\/dashboard$/);
  await expect(page.locator('.brand__title')).toHaveText('Warehouse Flow Manager');
  await expect(page.locator('.brand__subtitle')).toHaveText('Operations Control Console');

  await expect(page.getByRole('link', { name: /Dashboard/ })).toBeVisible();
  await expect(page.getByRole('link', { name: /Products/ })).toBeVisible();
  await expect(page.getByRole('link', { name: /Storage Locations/ })).toBeVisible();
  await expect(page.getByRole('link', { name: /Stock Movements/ })).toBeVisible();

  await expect(page.locator('.topbar__title')).toHaveText(
    'Live stock visibility and replenishment monitoring',
  );
});

test('navigates through the main pages from the sidebar', async ({ page }) => {
  await page.goto('/dashboard');

  await page.getByRole('link', { name: /Products/ }).click();
  await expect(page).toHaveURL(/\/products$/);
  await expect(page.getByRole('heading', { name: 'Inventory visibility' })).toBeVisible();

  await page.getByRole('link', { name: /Storage Locations/ }).click();
  await expect(page).toHaveURL(/\/storage-locations$/);
  await expect(page.getByRole('heading', { name: 'Storage locations' })).toBeVisible();

  await page.getByRole('link', { name: /Stock Movements/ }).click();
  await expect(page).toHaveURL(/\/stock-movements$/);
  await expect(page.getByRole('heading', { name: 'Stock movement monitoring' })).toBeVisible();

  await page.getByRole('link', { name: /Dashboard/ }).click();
  await expect(page).toHaveURL(/\/dashboard$/);
  await expect(page.getByRole('heading', { name: 'Warehouse status overview' })).toBeVisible();
});
