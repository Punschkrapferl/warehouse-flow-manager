import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { ApiPaths, storageLocationStockOverviewPath } from './api-paths';
import { StorageLocationResponse, StorageLocationStockOverviewResponse } from './api.types';
import { StorageLocationsApiService } from './storage-locations-api.service';

describe('StorageLocationsApiService', () => {
  let service: StorageLocationsApiService;
  let httpMock: HttpTestingController;

  const createLocation = (
    overrides: Partial<StorageLocationResponse> = {},
  ): StorageLocationResponse => ({
    id: 1,
    code: 'A-01-01',
    zone: 'ZONE-A',
    description: 'Main demo storage location',
    active: true,
    ...overrides,
  });

  const createOverview = (
    overrides: Partial<StorageLocationStockOverviewResponse> = {},
  ): StorageLocationStockOverviewResponse => ({
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
    ...overrides,
  });

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });

    service = TestBed.inject(StorageLocationsApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should request all storage locations', () => {
    const expectedResponse = [
      createLocation({
        id: 1,
        code: 'A-01-01',
        zone: 'ZONE-A',
        active: true,
      }),
      createLocation({
        id: 2,
        code: 'B-02-01',
        zone: 'ZONE-B',
        description: 'Overflow picking area',
        active: false,
      }),
    ];

    service.getStorageLocations().subscribe((response) => {
      expect(response).toEqual(expectedResponse);
    });

    const request = httpMock.expectOne(
      (httpRequest) => httpRequest.url === ApiPaths.storageLocations,
    );

    expect(request.request.method).toBe('GET');
    expect(request.request.params.keys().length).toBe(0);

    request.flush(expectedResponse);
  });

  it('should request stock overview for a storage location by id', () => {
    const expectedResponse = createOverview({
      id: 7,
      code: 'C-03-01',
      zone: 'COLD-STORAGE',
      totalProducts: 1,
      totalQuantity: 20,
      lowStockProductCount: 0,
      products: [
        {
          id: 5,
          sku: 'SKU-5005',
          name: 'Temperature Controlled Goods',
          unit: 'box',
          quantity: 20,
          minimumQuantity: 5,
          status: 'ACTIVE',
          lowStock: false,
        },
      ],
    });

    service.getStorageLocationStockOverview(7).subscribe((response) => {
      expect(response).toEqual(expectedResponse);
    });

    const expectedUrl = storageLocationStockOverviewPath(7);

    const request = httpMock.expectOne((httpRequest) => httpRequest.url === expectedUrl);

    expect(request.request.method).toBe('GET');
    expect(request.request.params.keys().length).toBe(0);

    request.flush(expectedResponse);
  });

  it('should use the centralized /api/v1 storage location path', () => {
    service.getStorageLocations().subscribe();

    const request = httpMock.expectOne(ApiPaths.storageLocations);

    expect(request.request.url).toBe('/api/v1/storage-locations');
    expect(request.request.method).toBe('GET');

    request.flush([]);
  });

  it('should use the centralized /api/v1 stock overview path', () => {
    service.getStorageLocationStockOverview(12).subscribe();

    const request = httpMock.expectOne(storageLocationStockOverviewPath(12));

    expect(request.request.url).toBe('/api/v1/storage-locations/12/stock-overview');
    expect(request.request.method).toBe('GET');

    request.flush(
      createOverview({
        id: 12,
        code: 'D-04-01',
      }),
    );
  });

  it('should pass storage location list backend errors to the caller', () => {
    let receivedErrorStatus: number | null = null;

    service.getStorageLocations().subscribe({
      next: () => {
        throw new Error('Expected request to fail');
      },
      error: (error) => {
        receivedErrorStatus = error.status;
      },
    });

    const request = httpMock.expectOne(ApiPaths.storageLocations);

    request.flush(
      {
        timestamp: '2026-04-27T18:00:00Z',
        status: 500,
        error: 'Internal Server Error',
        message: 'Unexpected backend error',
        path: '/api/v1/storage-locations',
        validationErrors: null,
      },
      {
        status: 500,
        statusText: 'Internal Server Error',
      },
    );

    expect(receivedErrorStatus).toBe(500);
  });

  it('should pass stock overview backend errors to the caller', () => {
    let receivedErrorStatus: number | null = null;

    service.getStorageLocationStockOverview(999).subscribe({
      next: () => {
        throw new Error('Expected request to fail');
      },
      error: (error) => {
        receivedErrorStatus = error.status;
      },
    });

    const request = httpMock.expectOne(storageLocationStockOverviewPath(999));

    request.flush(
      {
        timestamp: '2026-04-27T18:00:00Z',
        status: 404,
        error: 'Not Found',
        message: 'Storage location with id 999 not found',
        path: '/api/v1/storage-locations/999/stock-overview',
        validationErrors: null,
      },
      {
        status: 404,
        statusText: 'Not Found',
      },
    );

    expect(receivedErrorStatus).toBe(404);
  });
});
