import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { ApiPaths } from './api-paths';
import {
  HealthResponse,
  ProductResponse,
  ReplenishmentRecommendationResponse,
  StockMovementResponse,
} from './api.types';
import { DashboardApiService } from './dashboard-api.service';

describe('DashboardApiService', () => {
  let service: DashboardApiService;
  let httpMock: HttpTestingController;

  const createHealth = (overrides: Partial<HealthResponse> = {}): HealthResponse => ({
    status: 'UP',
    service: 'warehouse-flow-manager',
    timestamp: '2026-04-26T14:30:00Z',
    ...overrides,
  });

  const createProduct = (overrides: Partial<ProductResponse> = {}): ProductResponse => ({
    id: 1,
    sku: 'SKU-1001',
    name: 'Industrial Storage Bin',
    description: 'Large plastic bin for warehouse spare parts',
    unit: 'piece',
    quantity: 2,
    storageLocationId: 1,
    storageLocationCode: 'A-01-01',
    status: 'ACTIVE',
    minimumQuantity: 5,
    lowStock: true,
    ...overrides,
  });

  const createCandidate = (
    overrides: Partial<ReplenishmentRecommendationResponse> = {},
  ): ReplenishmentRecommendationResponse => ({
    productId: 1,
    sku: 'SKU-1001',
    name: 'Industrial Storage Bin',
    unit: 'piece',
    currentQuantity: 0,
    minimumQuantity: 5,
    shortageQuantity: 5,
    recentOutboundQuantity: 3,
    recommendedReorderQuantity: 8,
    priority: 'CRITICAL',
    storageLocationId: 1,
    storageLocationCode: 'A-01-01',
    ...overrides,
  });

  const createMovement = (
    overrides: Partial<StockMovementResponse> = {},
  ): StockMovementResponse => ({
    id: 1,
    productId: 1,
    productSku: 'SKU-1001',
    movementType: 'INBOUND',
    quantity: 10,
    resultingQuantity: 10,
    note: 'Initial stock on product creation',
    movementAt: '2026-04-26T14:18:59.084063Z',
    ...overrides,
  });

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });

    service = TestBed.inject(DashboardApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should request API health from the centralized /api/v1 health path', () => {
    const expectedResponse = createHealth();

    service.getHealth().subscribe((response) => {
      expect(response).toEqual(expectedResponse);
    });

    const request = httpMock.expectOne((httpRequest) => httpRequest.url === ApiPaths.health);

    expect(request.request.url).toBe('/api/v1/health');
    expect(request.request.method).toBe('GET');
    expect(request.request.params.keys().length).toBe(0);

    request.flush(expectedResponse);
  });

  it('should request low-stock products from the centralized low-stock path', () => {
    const expectedResponse = [
      createProduct({
        id: 1,
        sku: 'SKU-LOW-1',
        quantity: 1,
        minimumQuantity: 5,
        lowStock: true,
      }),
      createProduct({
        id: 2,
        sku: 'SKU-LOW-2',
        name: 'Packing Tape Roll',
        quantity: 0,
        minimumQuantity: 3,
        lowStock: true,
      }),
    ];

    service.getLowStockProducts().subscribe((response) => {
      expect(response).toEqual(expectedResponse);
    });

    const request = httpMock.expectOne(
      (httpRequest) => httpRequest.url === ApiPaths.lowStockProducts,
    );

    expect(request.request.url).toBe('/api/v1/products/low-stock');
    expect(request.request.method).toBe('GET');
    expect(request.request.params.keys().length).toBe(0);

    request.flush(expectedResponse);
  });

  it('should request replenishment candidates with recentDays query param', () => {
    const expectedResponse = [
      createCandidate({
        productId: 1,
        sku: 'SKU-CRITICAL',
        priority: 'CRITICAL',
        recommendedReorderQuantity: 12,
      }),
      createCandidate({
        productId: 2,
        sku: 'SKU-HIGH',
        name: 'Packing Tape Roll',
        priority: 'HIGH',
        recommendedReorderQuantity: 8,
      }),
    ];

    service.getReplenishmentCandidates(30).subscribe((response) => {
      expect(response).toEqual(expectedResponse);
    });

    const request = httpMock.expectOne(
      (httpRequest) => httpRequest.url === ApiPaths.replenishmentCandidates,
    );

    expect(request.request.url).toBe('/api/v1/products/replenishment-candidates');
    expect(request.request.method).toBe('GET');
    expect(request.request.params.get('recentDays')).toBe('30');

    request.flush(expectedResponse);
  });

  it('should request stock movements and return only the requested recent limit', () => {
    const backendResponse = [
      createMovement({ id: 1, productSku: 'SKU-1001' }),
      createMovement({ id: 2, productSku: 'SKU-1002', movementType: 'OUTBOUND' }),
      createMovement({ id: 3, productSku: 'SKU-1003', movementType: 'ADJUSTMENT' }),
      createMovement({ id: 4, productSku: 'SKU-1004' }),
    ];

    const expectedResponse = backendResponse.slice(0, 2);

    service.getRecentStockMovements(2).subscribe((response) => {
      expect(response).toEqual(expectedResponse);
    });

    const request = httpMock.expectOne(
      (httpRequest) => httpRequest.url === ApiPaths.stockMovements,
    );

    expect(request.request.url).toBe('/api/v1/stock-movements');
    expect(request.request.method).toBe('GET');
    expect(request.request.params.keys().length).toBe(0);

    request.flush(backendResponse);
  });

  it('should return an empty recent movement list when the backend returns no movements', () => {
    service.getRecentStockMovements(8).subscribe((response) => {
      expect(response).toEqual([]);
    });

    const request = httpMock.expectOne(
      (httpRequest) => httpRequest.url === ApiPaths.stockMovements,
    );

    expect(request.request.method).toBe('GET');

    request.flush([]);
  });

  it('should pass health backend errors to the caller', () => {
    let receivedErrorStatus: number | null = null;

    service.getHealth().subscribe({
      next: () => {
        throw new Error('Expected request to fail');
      },
      error: (error) => {
        receivedErrorStatus = error.status;
      },
    });

    const request = httpMock.expectOne((httpRequest) => httpRequest.url === ApiPaths.health);

    request.flush(
      {
        timestamp: '2026-04-27T18:00:00Z',
        status: 503,
        error: 'Service Unavailable',
        message: 'Health check failed',
        path: '/api/v1/health',
        validationErrors: null,
      },
      {
        status: 503,
        statusText: 'Service Unavailable',
      },
    );

    expect(receivedErrorStatus).toBe(503);
  });

  it('should pass low-stock backend errors to the caller', () => {
    let receivedErrorStatus: number | null = null;

    service.getLowStockProducts().subscribe({
      next: () => {
        throw new Error('Expected request to fail');
      },
      error: (error) => {
        receivedErrorStatus = error.status;
      },
    });

    const request = httpMock.expectOne(
      (httpRequest) => httpRequest.url === ApiPaths.lowStockProducts,
    );

    request.flush(
      {
        timestamp: '2026-04-27T18:00:00Z',
        status: 500,
        error: 'Internal Server Error',
        message: 'Unexpected backend error',
        path: '/api/v1/products/low-stock',
        validationErrors: null,
      },
      {
        status: 500,
        statusText: 'Internal Server Error',
      },
    );

    expect(receivedErrorStatus).toBe(500);
  });

  it('should pass replenishment backend errors to the caller', () => {
    let receivedErrorStatus: number | null = null;

    service.getReplenishmentCandidates(0).subscribe({
      next: () => {
        throw new Error('Expected request to fail');
      },
      error: (error) => {
        receivedErrorStatus = error.status;
      },
    });

    const request = httpMock.expectOne(
      (httpRequest) => httpRequest.url === ApiPaths.replenishmentCandidates,
    );

    expect(request.request.params.get('recentDays')).toBe('0');

    request.flush(
      {
        timestamp: '2026-04-27T18:00:00Z',
        status: 400,
        error: 'Bad Request',
        message: 'Validation failed',
        path: '/api/v1/products/replenishment-candidates',
        validationErrors: {
          recentDays: 'recentDays must be greater than 0',
        },
      },
      {
        status: 400,
        statusText: 'Bad Request',
      },
    );

    expect(receivedErrorStatus).toBe(400);
  });

  it('should pass recent stock movement backend errors to the caller', () => {
    let receivedErrorStatus: number | null = null;

    service.getRecentStockMovements(8).subscribe({
      next: () => {
        throw new Error('Expected request to fail');
      },
      error: (error) => {
        receivedErrorStatus = error.status;
      },
    });

    const request = httpMock.expectOne(
      (httpRequest) => httpRequest.url === ApiPaths.stockMovements,
    );

    request.flush(
      {
        timestamp: '2026-04-27T18:00:00Z',
        status: 500,
        error: 'Internal Server Error',
        message: 'Unexpected backend error',
        path: '/api/v1/stock-movements',
        validationErrors: null,
      },
      {
        status: 500,
        statusText: 'Internal Server Error',
      },
    );

    expect(receivedErrorStatus).toBe(500);
  });
});
