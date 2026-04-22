export type ProductStatus = 'ACTIVE' | 'BLOCKED' | 'DISCONTINUED';
export type StockMovementType = 'INBOUND' | 'OUTBOUND' | 'ADJUSTMENT';
export type ReplenishmentPriority = 'CRITICAL' | 'HIGH' | 'MEDIUM';

export interface HealthResponse {
  status: string;
  service: string;
  timestamp: string;
}

export interface PagedResponse<T> {
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
  content: T[];
}

export interface ProductResponse {
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

export interface StockMovementResponse {
  id: number;
  productId: number;
  productSku: string;
  movementType: StockMovementType;
  quantity: number;
  resultingQuantity: number;
  note: string | null;
  movementAt: string;
}

export interface ReplenishmentRecommendationResponse {
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

export interface StorageLocationResponse {
  id: number;
  code: string;
  zone: string;
  description: string | null;
  active: boolean;
}

export interface StorageLocationStockItemResponse {
  id: number;
  sku: string;
  name: string;
  unit: string;
  quantity: number;
  minimumQuantity: number;
  status: ProductStatus;
  lowStock: boolean;
}

export interface StorageLocationStockOverviewResponse {
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
