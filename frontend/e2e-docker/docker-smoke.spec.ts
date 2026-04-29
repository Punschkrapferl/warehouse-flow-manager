import { expect, Page, test } from '@playwright/test';

function collectApiFailures(page: Page): string[] {
  const failures: string[] = [];

  page.on('response', (response) => {
    const url = response.url();

    if (url.includes('/api/v1/') && response.status() >= 400) {
      failures.push(`${response.status()} ${url}`);
    }
  });

  page.on('requestfailed', (request) => {
    const url = request.url();

    if (url.includes('/api/v1/')) {
      failures.push(`FAILED ${url}: ${request.failure()?.errorText ?? 'unknown error'}`);
    }
  });

  return failures;
}

async function expectPageLoadsWithoutApiFailures(
  page: Page,
  path: string,
  expectedText: RegExp,
): Promise<void> {
  const apiFailures = collectApiFailures(page);

  await page.goto(path);
  await page.waitForLoadState('networkidle');

  await expect(page.locator('body')).toContainText(expectedText);

  expect(
    apiFailures,
    `Unexpected API failures while loading ${path}:\n${apiFailures.join('\n')}`,
  ).toEqual([]);
}

test.describe('Dockerized frontend smoke test', () => {
  test('serves the Angular app and proxies the backend health endpoint', async ({
    page,
    request,
  }) => {
    const healthResponse = await request.get('/api/v1/health');

    expect(
      healthResponse.ok(),
      'Expected nginx frontend container to proxy /api/v1/health to the demo backend',
    ).toBeTruthy();

    const healthBody = await healthResponse.json();

    expect(healthBody.status).toBe('UP');

    await page.goto('/');
    await expect(page.locator('body')).toContainText(/warehouse|dashboard|operations/i);
  });

  test('exposes seeded demo products through the nginx API proxy', async ({ request }) => {
    const productsResponse = await request.get(
      '/api/v1/products?page=0&size=10&sortBy=id&direction=asc',
    );

    expect(
      productsResponse.ok(),
      'Expected /api/v1/products to be reachable through the Dockerized frontend nginx proxy',
    ).toBeTruthy();

    const productsBody = await productsResponse.json();
    const products = productsBody.content ?? [];

    expect(Array.isArray(products)).toBeTruthy();

    expect(
      products.length,
      'Expected seeded demo products. Run ./scripts/demo/seed-demo-data.sh before npm run e2e:docker.',
    ).toBeGreaterThan(0);
  });

  test('loads the dashboard page from the Dockerized frontend', async ({ page }) => {
    await expectPageLoadsWithoutApiFailures(
      page,
      '/dashboard',
      /dashboard|low stock|replenishment|movement|warehouse/i,
    );
  });

  test('loads the products page from the Dockerized frontend', async ({ page }) => {
    await expectPageLoadsWithoutApiFailures(page, '/products', /product|inventory|sku|stock/i);
  });

  test('loads the storage locations page from the Dockerized frontend', async ({ page }) => {
    await expectPageLoadsWithoutApiFailures(
      page,
      '/storage-locations',
      /storage|location|zone|stock/i,
    );
  });

  test('loads the stock movements page from the Dockerized frontend', async ({ page }) => {
    await expectPageLoadsWithoutApiFailures(
      page,
      '/stock-movements',
      /stock|movement|inbound|outbound|adjustment/i,
    );
  });
});
