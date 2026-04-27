import { expect, test } from '@playwright/test';
import { mockWarehouseApi } from './mocks/warehouse-api.mock';

test.beforeEach(async ({ page }) => {
  await mockWarehouseApi(page);
});

test('renders stock movement summary and movement history', async ({ page }) => {
  await page.goto('/stock-movements');

  await expect(page.getByRole('heading', { name: 'Stock movement monitoring' })).toBeVisible();

  await expect(page.getByText('Movement summary', { exact: true })).toBeVisible();
  await expect(
    page.locator('.movement-kpi__label').filter({ hasText: /^Total movements$/ }),
  ).toBeVisible();
  await expect(page.locator('.movement-kpi__label').filter({ hasText: /^Inbound$/ })).toBeVisible();
  await expect(
    page.locator('.movement-kpi__label').filter({ hasText: /^Outbound$/ }),
  ).toBeVisible();
  await expect(
    page.locator('.movement-kpi__label').filter({ hasText: /^Adjustment$/ }),
  ).toBeVisible();

  const adjustmentRow = page.getByRole('row').filter({ hasText: 'Demo stock correction' });
  await expect(adjustmentRow).toBeVisible();
  await expect(adjustmentRow).toContainText('SKU-1002');
  await expect(adjustmentRow).toContainText('ADJUSTMENT');

  const outboundRow = page.getByRole('row').filter({ hasText: 'Demo outbound movement' });
  await expect(outboundRow).toBeVisible();
  await expect(outboundRow).toContainText('SKU-1001');
  await expect(outboundRow).toContainText('OUTBOUND');
});

test('filters stock movements by product id and movement type', async ({ page }) => {
  await page.goto('/stock-movements');

  await page.locator('#productId').fill('1');
  await page.locator('#movementType').selectOption({ label: 'OUTBOUND' });

  await page.getByRole('button', { name: 'Apply filters' }).click();

  const outboundRow = page.getByRole('row').filter({ hasText: 'Demo outbound movement' });

  await expect(outboundRow).toBeVisible();
  await expect(outboundRow).toContainText('SKU-1001');
  await expect(outboundRow).toContainText('OUTBOUND');

  await expect(page.getByRole('row').filter({ hasText: 'Demo stock correction' })).toHaveCount(0);
  await expect(page.getByRole('row').filter({ hasText: 'Restock from supplier' })).toHaveCount(0);
});

test('resets stock movement filters', async ({ page }) => {
  await page.goto('/stock-movements');

  await page.locator('#productId').fill('1');
  await page.locator('#movementType').selectOption({ label: 'OUTBOUND' });
  await page.getByRole('button', { name: 'Apply filters' }).click();

  await expect(page.getByRole('row').filter({ hasText: 'Demo outbound movement' })).toBeVisible();

  await page.getByRole('button', { name: 'Reset' }).click();

  await expect(page.getByRole('row').filter({ hasText: 'Demo stock correction' })).toBeVisible();
  await expect(page.getByRole('row').filter({ hasText: 'Restock from supplier' })).toBeVisible();
});
