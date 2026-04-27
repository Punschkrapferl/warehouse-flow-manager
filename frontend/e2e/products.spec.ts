import { expect, test } from '@playwright/test';
import { mockWarehouseApi } from './mocks/warehouse-api.mock';

test.beforeEach(async ({ page }) => {
  await mockWarehouseApi(page);
});

test('renders product inventory table from backend data', async ({ page }) => {
  await page.goto('/products');

  await expect(page.getByRole('heading', { name: 'Inventory visibility' })).toBeVisible();
  await expect(page.getByText('3 total product(s) returned by the backend.')).toBeVisible();

  await expect(page.getByText('SKU-1001')).toBeVisible();
  await expect(page.getByText('Industrial Storage Bin')).toBeVisible();

  await expect(page.getByText('SKU-1002')).toBeVisible();
  await expect(page.getByText('Packing Tape Roll')).toBeVisible();

  await expect(page.getByText('SKU-BLOCKED-DEMO')).toBeVisible();
  await expect(page.getByText('Blocked Demo Item')).toBeVisible();
});

test('filters products by search and status', async ({ page }) => {
  await page.goto('/products');

  await page.locator('#search').fill('Blocked');
  await page.locator('#status').selectOption({ label: 'BLOCKED' });

  await page.getByRole('button', { name: 'Apply filters' }).click();

  await expect(page.getByText('1 total product(s) returned by the backend.')).toBeVisible();
  await expect(page.getByText('Blocked Demo Item')).toBeVisible();
  await expect(page.getByText('Industrial Storage Bin')).not.toBeVisible();
  await expect(page.getByText('Packing Tape Roll')).not.toBeVisible();
});

test('resets product filters back to the default product list', async ({ page }) => {
  await page.goto('/products');

  await page.locator('#search').fill('Blocked');
  await page.locator('#status').selectOption({ label: 'BLOCKED' });
  await page.getByRole('button', { name: 'Apply filters' }).click();

  await expect(page.getByText('Blocked Demo Item')).toBeVisible();

  await page.getByRole('button', { name: 'Reset' }).click();

  await expect(page.getByText('3 total product(s) returned by the backend.')).toBeVisible();
  await expect(page.getByText('Industrial Storage Bin')).toBeVisible();
  await expect(page.getByText('Packing Tape Roll')).toBeVisible();
});
