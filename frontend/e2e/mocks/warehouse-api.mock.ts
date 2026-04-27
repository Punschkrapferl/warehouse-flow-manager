import { Page, Route } from '@playwright/test';

type ProductStatus = 'ACTIVE' | 'BLOCKED' | 'DISCONTINUED';
type StockMovementType = 'INBOUND' | 'OUTBOUND' | 'ADJUSTMENT';
type ReplenishmentPriority = 'CRITICAL' | 'HIGH' | 'MEDIUM';

interface PagedResponse<T> {
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
  content: T[];
}

interface ProductResponse {
  id: number;
  sku: string;
  name: string;
  description: string | null;
  unit: string;
  quantity: number;
  storageLocationId: number | null;
  storageLocationCode: string | null;
  status: ProductStatus;
  minimumQuantity: number;
  lowStock: boolean;
}

interface StockMovementResponse {
  id: number;
  productId: number;
  productSku: string;
  movementType: StockMovementType;
  quantity: number;
  resultingQuantity: number;
  note: string | null;
  movementAt: string;
}

interface StockMovementSummaryResponse {
  productId: number | null;
  productSku: string | null;
  from: string | null;
  to: string | null;
  totalMovements: number;
  inboundMovementCount: number;
  outboundMovementCount: number;
  adjustmentMovementCount: number;
  totalInboundQuantity: number;
  totalOutboundQuantity: number;
  totalAdjustmentQuantity: number;
  currentQuantity: number | null;
  latestMovementAt: string | null;
}

interface ReplenishmentRecommendationResponse {
  productId: number;
  sku: string;
  name: string;
  unit: string;
  currentQuantity: number;
  minimumQuantity: number;
  shortageQuantity: number;
  recentOutboundQuantity: number;
  recommendedReorderQuantity: number;
  priority: ReplenishmentPriority;
  storageLocationId: number | null;
  storageLocationCode: string | null;
}

interface StorageLocationResponse {
  id: number;
  code: string;
  zone: string;
  description: string | null;
  active: boolean;
}

interface StorageLocationStockOverviewResponse {
  id: number;
  code: string;
  zone: string;
  description: string | null;
  active: boolean;
  totalProducts: number;
  totalQuantity: number;
  lowStockProductCount: number;
  products: StorageLocationStockItemResponse[];
}

interface StorageLocationStockItemResponse {
  id: number;
  sku: string;
  name: string;
  unit: string;
  quantity: number;
  minimumQuantity: number;
  status: ProductStatus;
  lowStock: boolean;
}

const products: ProductResponse[] = [
  {
    id: 1,
    sku: 'SKU-1001',
    name: 'Industrial Storage Bin',
    description: 'Large plastic bin for warehouse spare parts',
    unit: 'piece',
    quantity: 13,
    storageLocationId: 1,
    storageLocationCode: 'A-01-01',
    status: 'ACTIVE',
    minimumQuantity: 3,
    lowStock: false,
  },
  {
    id: 2,
    sku: 'SKU-1002',
    name: 'Packing Tape Roll',
    description: 'Tape rolls for outbound packing',
    unit: 'piece',
    quantity: 2,
    storageLocationId: 1,
    storageLocationCode: 'A-01-01',
    status: 'ACTIVE',
    minimumQuantity: 5,
    lowStock: true,
  },
  {
    id: 3,
    sku: 'SKU-BLOCKED-DEMO',
    name: 'Blocked Demo Item',
    description: 'Blocked product used for filter testing',
    unit: 'piece',
    quantity: 0,
    storageLocationId: null,
    storageLocationCode: null,
    status: 'BLOCKED',
    minimumQuantity: 0,
    lowStock: false,
  },
];

const stockMovements: StockMovementResponse[] = [
  {
    id: 5,
    productId: 2,
    productSku: 'SKU-1002',
    movementType: 'ADJUSTMENT',
    quantity: 8,
    resultingQuantity: 8,
    note: 'Demo stock correction',
    movementAt: '2026-04-26T14:30:00.000Z',
  },
  {
    id: 4,
    productId: 1,
    productSku: 'SKU-1001',
    movementType: 'OUTBOUND',
    quantity: 2,
    resultingQuantity: 13,
    note: 'Demo outbound movement',
    movementAt: '2026-04-26T14:25:00.000Z',
  },
  {
    id: 3,
    productId: 1,
    productSku: 'SKU-1001',
    movementType: 'INBOUND',
    quantity: 10,
    resultingQuantity: 15,
    note: 'Restock from supplier',
    movementAt: '2026-04-26T14:20:00.000Z',
  },
];

const storageLocations: StorageLocationResponse[] = [
  {
    id: 1,
    code: 'A-01-01',
    zone: 'ZONE-A',
    description: 'Main demo storage location',
    active: true,
  },
  {
    id: 2,
    code: 'B-02-01',
    zone: 'ZONE-B',
    description: 'Overflow picking area',
    active: false,
  },
];

const overviewA0101: StorageLocationStockOverviewResponse = {
  id: 1,
  code: 'A-01-01',
  zone: 'ZONE-A',
  description: 'Main demo storage location',
  active: true,
  totalProducts: 2,
  totalQuantity: 15,
  lowStockProductCount: 1,
  products: [
    {
      id: 1,
      sku: 'SKU-1001',
      name: 'Industrial Storage Bin',
      unit: 'piece',
      quantity: 13,
      minimumQuantity: 3,
      status: 'ACTIVE',
      lowStock: false,
    },
    {
      id: 2,
      sku: 'SKU-1002',
      name: 'Packing Tape Roll',
      unit: 'piece',
      quantity: 2,
      minimumQuantity: 5,
      status: 'ACTIVE',
      lowStock: true,
    },
  ],
};

const overviewB0201: StorageLocationStockOverviewResponse = {
  id: 2,
  code: 'B-02-01',
  zone: 'ZONE-B',
  description: 'Overflow picking area',
  active: false,
  totalProducts: 0,
  totalQuantity: 0,
  lowStockProductCount: 0,
  products: [],
};

const replenishmentCandidates: ReplenishmentRecommendationResponse[] = [
  {
    productId: 2,
    sku: 'SKU-1002',
    name: 'Packing Tape Roll',
    unit: 'piece',
    currentQuantity: 0,
    minimumQuantity: 5,
    shortageQuantity: 5,
    recentOutboundQuantity: 3,
    recommendedReorderQuantity: 8,
    priority: 'CRITICAL',
    storageLocationId: 1,
    storageLocationCode: 'A-01-01',
  },
  {
    productId: 1,
    sku: 'SKU-1001',
    name: 'Industrial Storage Bin',
    unit: 'piece',
    currentQuantity: 13,
    minimumQuantity: 3,
    shortageQuantity: 0,
    recentOutboundQuantity: 2,
    recommendedReorderQuantity: 2,
    priority: 'MEDIUM',
    storageLocationId: 1,
    storageLocationCode: 'A-01-01',
  },
];

export async function mockWarehouseApi(page: Page): Promise<void> {
  await page.route('**/api/v1/**', async (route) => {
    const request = route.request();
    const url = new URL(request.url());
    const path = url.pathname;

    if (request.method() !== 'GET') {
      await fulfillJson(route, { message: 'Method not mocked' }, 405);
      return;
    }

    if (path === '/api/v1/health') {
      await fulfillJson(route, {
        status: 'UP',
        service: 'warehouse-flow-manager',
        timestamp: '2026-04-26T14:30:00Z',
      });
      return;
    }

    if (path === '/api/v1/products/low-stock') {
      await fulfillJson(
        route,
        products.filter((product) => product.lowStock),
      );
      return;
    }

    if (path === '/api/v1/products/replenishment-candidates') {
      await fulfillJson(route, replenishmentCandidates);
      return;
    }

    if (path === '/api/v1/products') {
      await fulfillJson(route, buildProductsPage(url));
      return;
    }

    if (path === '/api/v1/stock-movements') {
      await fulfillJson(route, buildStockMovements(url));
      return;
    }

    if (path === '/api/v1/stock-movements/summary') {
      await fulfillJson(route, buildStockMovementSummary(url));
      return;
    }

    if (path === '/api/v1/storage-locations') {
      await fulfillJson(route, storageLocations);
      return;
    }

    if (path === '/api/v1/storage-locations/1/stock-overview') {
      await fulfillJson(route, overviewA0101);
      return;
    }

    if (path === '/api/v1/storage-locations/2/stock-overview') {
      await fulfillJson(route, overviewB0201);
      return;
    }

    await fulfillJson(route, { message: `No mock configured for ${path}` }, 404);
  });
}

function buildProductsPage(url: URL): PagedResponse<ProductResponse> {
  const search = url.searchParams.get('search')?.toLowerCase().trim() ?? '';
  const status = url.searchParams.get('status') as ProductStatus | null;
  const page = Number(url.searchParams.get('page') ?? 0);
  const size = Number(url.searchParams.get('size') ?? 10);

  let filteredProducts = [...products];

  if (status) {
    filteredProducts = filteredProducts.filter((product) => product.status === status);
  }

  if (search) {
    filteredProducts = filteredProducts.filter((product) => {
      return (
        product.sku.toLowerCase().includes(search) || product.name.toLowerCase().includes(search)
      );
    });
  }

  return {
    page,
    size,
    totalElements: filteredProducts.length,
    totalPages: filteredProducts.length > 0 ? 1 : 0,
    first: true,
    last: true,
    content: filteredProducts,
  };
}

function buildStockMovements(url: URL): StockMovementResponse[] {
  const productId = url.searchParams.get('productId');
  const movementType = url.searchParams.get('movementType') as StockMovementType | null;

  let filteredMovements = [...stockMovements];

  if (productId) {
    filteredMovements = filteredMovements.filter(
      (movement) => movement.productId === Number(productId),
    );
  }

  if (movementType) {
    filteredMovements = filteredMovements.filter(
      (movement) => movement.movementType === movementType,
    );
  }

  return filteredMovements;
}

function buildStockMovementSummary(url: URL): StockMovementSummaryResponse {
  const productId = url.searchParams.get('productId');

  if (productId === '1') {
    return {
      productId: 1,
      productSku: 'SKU-1001',
      from: null,
      to: null,
      totalMovements: 2,
      inboundMovementCount: 1,
      outboundMovementCount: 1,
      adjustmentMovementCount: 0,
      totalInboundQuantity: 10,
      totalOutboundQuantity: 2,
      totalAdjustmentQuantity: 0,
      currentQuantity: 13,
      latestMovementAt: '2026-04-26T14:25:00.000Z',
    };
  }

  return {
    productId: null,
    productSku: null,
    from: null,
    to: null,
    totalMovements: stockMovements.length,
    inboundMovementCount: 1,
    outboundMovementCount: 1,
    adjustmentMovementCount: 1,
    totalInboundQuantity: 10,
    totalOutboundQuantity: 2,
    totalAdjustmentQuantity: 8,
    currentQuantity: null,
    latestMovementAt: '2026-04-26T14:30:00.000Z',
  };
}

async function fulfillJson(route: Route, body: unknown, status = 200): Promise<void> {
  await route.fulfill({
    status,
    contentType: 'application/json',
    body: JSON.stringify(body),
  });
}
