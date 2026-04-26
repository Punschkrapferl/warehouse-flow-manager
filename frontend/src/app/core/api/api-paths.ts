export const API_BASE_PATH = '/api/v1';

export const ApiPaths = {
  health: `${API_BASE_PATH}/health`,
  products: `${API_BASE_PATH}/products`,
  lowStockProducts: `${API_BASE_PATH}/products/low-stock`,
  replenishmentCandidates: `${API_BASE_PATH}/products/replenishment-candidates`,
  stockMovements: `${API_BASE_PATH}/stock-movements`,
  stockMovementSummary: `${API_BASE_PATH}/stock-movements/summary`,
  storageLocations: `${API_BASE_PATH}/storage-locations`,
} as const;

export const storageLocationStockOverviewPath = (storageLocationId: number): string =>
  `${ApiPaths.storageLocations}/${storageLocationId}/stock-overview`;
