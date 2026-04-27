import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { ApiPaths } from './api-paths';
import { StockMovementResponse, StockMovementSummaryResponse } from './api.types';
import { StockMovementsApiService } from './stock-movements-api.service';

describe('StockMovementsApiService', () => {
  let service: StockMovementsApiService;
  let httpMock: HttpTestingController;

  const createMovement = (
    overrides: Partial<StockMovementResponse> = {},
  ): StockMovementResponse => ({
    id: 1,
    productId: 1,
    productSku: 'SKU-1001',
    movementType: 'INBOUND',
    quantity: 10,
    resultingQuantity: 20,
    note: 'Restock from supplier',
    movementAt: '2026-04-26T14:18:59.084063Z',
    ...overrides,
  });

  const createSummary = (
    overrides: Partial<StockMovementSummaryResponse> = {},
  ): StockMovementSummaryResponse => ({
    productId: null,
    productSku: null,
    from: null,
    to: null,
    totalMovements: 2,
    inboundMovementCount: 1,
    outboundMovementCount: 1,
    adjustmentMovementCount: 0,
    totalInboundQuantity: 10,
    totalOutboundQuantity: 3,
    totalAdjustmentQuantity: 0,
    currentQuantity: null,
    latestMovementAt: '2026-04-26T14:18:59.084063Z',
    ...overrides,
  });

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });

    service = TestBed.inject(StockMovementsApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should request stock movements without optional query params by default', () => {
    const expectedResponse = [
      createMovement({
        id: 1,
        movementType: 'INBOUND',
        quantity: 10,
        resultingQuantity: 20,
      }),
      createMovement({
        id: 2,
        movementType: 'OUTBOUND',
        quantity: 3,
        resultingQuantity: 17,
      }),
    ];

    service.getStockMovements({}).subscribe((response) => {
      expect(response).toEqual(expectedResponse);
    });

    const request = httpMock.expectOne(
      (httpRequest) => httpRequest.url === ApiPaths.stockMovements,
    );

    expect(request.request.method).toBe('GET');
    expect(request.request.params.has('productId')).toBe(false);
    expect(request.request.params.has('movementType')).toBe(false);
    expect(request.request.params.has('from')).toBe(false);
    expect(request.request.params.has('to')).toBe(false);

    request.flush(expectedResponse);
  });

  it('should include productId, movementType, from, and to when provided for stock movements', () => {
    const expectedResponse = [
      createMovement({
        id: 4,
        productId: 7,
        productSku: 'SKU-7007',
        movementType: 'OUTBOUND',
        quantity: 5,
        resultingQuantity: 15,
      }),
    ];

    service
      .getStockMovements({
        productId: 7,
        movementType: 'OUTBOUND',
        from: '2026-04-26T08:00:00.000Z',
        to: '2026-04-26T18:00:00.000Z',
      })
      .subscribe((response) => {
        expect(response).toEqual(expectedResponse);
      });

    const request = httpMock.expectOne(
      (httpRequest) => httpRequest.url === ApiPaths.stockMovements,
    );

    expect(request.request.method).toBe('GET');
    expect(request.request.params.get('productId')).toBe('7');
    expect(request.request.params.get('movementType')).toBe('OUTBOUND');
    expect(request.request.params.get('from')).toBe('2026-04-26T08:00:00.000Z');
    expect(request.request.params.get('to')).toBe('2026-04-26T18:00:00.000Z');

    request.flush(expectedResponse);
  });

  it('should request stock movement summary without optional query params by default', () => {
    const expectedResponse = createSummary();

    service.getStockMovementSummary({}).subscribe((response) => {
      expect(response).toEqual(expectedResponse);
    });

    const request = httpMock.expectOne(
      (httpRequest) => httpRequest.url === ApiPaths.stockMovementSummary,
    );

    expect(request.request.method).toBe('GET');
    expect(request.request.params.has('productId')).toBe(false);
    expect(request.request.params.has('from')).toBe(false);
    expect(request.request.params.has('to')).toBe(false);
    expect(request.request.params.has('movementType')).toBe(false);

    request.flush(expectedResponse);
  });

  it('should include productId, from, and to when provided for stock movement summary', () => {
    const expectedResponse = createSummary({
      productId: 7,
      productSku: 'SKU-7007',
      from: '2026-04-26T08:00:00.000Z',
      to: '2026-04-26T18:00:00.000Z',
      totalMovements: 3,
      totalInboundQuantity: 12,
      totalOutboundQuantity: 4,
      currentQuantity: 8,
    });

    service
      .getStockMovementSummary({
        productId: 7,
        from: '2026-04-26T08:00:00.000Z',
        to: '2026-04-26T18:00:00.000Z',
      })
      .subscribe((response) => {
        expect(response).toEqual(expectedResponse);
      });

    const request = httpMock.expectOne(
      (httpRequest) => httpRequest.url === ApiPaths.stockMovementSummary,
    );

    expect(request.request.method).toBe('GET');
    expect(request.request.params.get('productId')).toBe('7');
    expect(request.request.params.get('from')).toBe('2026-04-26T08:00:00.000Z');
    expect(request.request.params.get('to')).toBe('2026-04-26T18:00:00.000Z');
    expect(request.request.params.has('movementType')).toBe(false);

    request.flush(expectedResponse);
  });

  it('should pass stock movement backend errors to the caller', () => {
    let receivedErrorStatus: number | null = null;

    service.getStockMovements({ productId: 999 }).subscribe({
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
        status: 404,
        error: 'Not Found',
        message: 'Product not found with id: 999',
        path: '/api/v1/stock-movements',
        validationErrors: null,
      },
      {
        status: 404,
        statusText: 'Not Found',
      },
    );

    expect(receivedErrorStatus).toBe(404);
  });

  it('should pass summary backend errors to the caller', () => {
    let receivedErrorStatus: number | null = null;

    service
      .getStockMovementSummary({
        from: '2026-04-27T18:00:00.000Z',
        to: '2026-04-27T08:00:00.000Z',
      })
      .subscribe({
        next: () => {
          throw new Error('Expected request to fail');
        },
        error: (error) => {
          receivedErrorStatus = error.status;
        },
      });

    const request = httpMock.expectOne(
      (httpRequest) => httpRequest.url === ApiPaths.stockMovementSummary,
    );

    request.flush(
      {
        timestamp: '2026-04-27T18:00:00Z',
        status: 400,
        error: 'Bad Request',
        message: "'from' must be before or equal to 'to'",
        path: '/api/v1/stock-movements/summary',
        validationErrors: null,
      },
      {
        status: 400,
        statusText: 'Bad Request',
      },
    );

    expect(receivedErrorStatus).toBe(400);
  });
});
