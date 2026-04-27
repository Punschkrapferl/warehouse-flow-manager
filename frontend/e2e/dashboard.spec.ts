import { expect, test } from '@playwright/test';
import { mockWarehouseApi } from './mocks/warehouse-api.mock';

test.beforeEach(async ({ page }) => {
  await mockWarehouseApi(page);
});

test('renders dashboard KPIs and operational tables', async ({ page }) => {
  await page.goto('/dashboard');

  await expect(page.getByRole('heading', { name: 'Warehouse status overview' })).toBeVisible();

  const heartbeatCard = page.locator('.console-kpi').filter({ hasText: 'API heartbeat' });
  await expect(heartbeatCard).toBeVisible();
  await expect(heartbeatCard.getByText('UP', { exact: true })).toBeVisible();

  const lowStockCard = page.locator('.console-kpi').filter({ hasText: 'Low-stock alerts' });
  await expect(lowStockCard).toBeVisible();

  const criticalCard = page.locator('.console-kpi').filter({ hasText: 'Critical shortages' });
  await expect(criticalCard).toBeVisible();

  const recentMovementCard = page
    .locator('.console-kpi')
    .filter({ hasText: 'Recent movement events' });
  await expect(recentMovementCard).toBeVisible();

  const lowStockPanel = page.locator('.ops-panel').filter({
    has: page.getByRole('heading', { name: 'Low-stock watchlist' }),
  });

  await expect(lowStockPanel).toBeVisible();

  const lowStockRow = lowStockPanel.getByRole('row').filter({ hasText: 'SKU-1002' });
  await expect(lowStockRow).toBeVisible();
  await expect(lowStockRow).toContainText('Packing Tape Roll');

  const replenishmentPanel = page.locator('.ops-panel').filter({
    has: page.getByRole('heading', { name: 'Replenishment queue' }),
  });

  await expect(replenishmentPanel).toBeVisible();

  const criticalQueueItem = replenishmentPanel
    .locator('.queue-item')
    .filter({ hasText: 'Packing Tape Roll' });

  await expect(criticalQueueItem).toBeVisible();
  await expect(criticalQueueItem).toContainText('SKU-1002');
  await expect(criticalQueueItem).toContainText('CRITICAL');
  await expect(criticalQueueItem).toContainText('Reorder');
  await expect(criticalQueueItem).toContainText('8');

  const recentMovementPanel = page.locator('.ops-panel').filter({
    has: page.getByRole('heading', { name: 'Recent stock movement stream' }),
  });

  await expect(recentMovementPanel).toBeVisible();

  const recentMovementRow = recentMovementPanel
    .getByRole('row')
    .filter({ hasText: 'Demo stock correction' });

  await expect(recentMovementRow).toBeVisible();
  await expect(recentMovementRow).toContainText('SKU-1002');
  await expect(recentMovementRow).toContainText('ADJUSTMENT');
});

test('refreshes the dashboard without leaving the page', async ({ page }) => {
  await page.goto('/dashboard');

  await page.getByRole('button', { name: 'Refresh board' }).click();

  await expect(page).toHaveURL(/\/dashboard$/);
  await expect(page.getByRole('heading', { name: 'Warehouse status overview' })).toBeVisible();
  await expect(page.locator('.console-kpi').filter({ hasText: 'API heartbeat' })).toBeVisible();
});
