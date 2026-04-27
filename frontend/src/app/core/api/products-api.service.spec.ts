import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { ApiPaths } from './api-paths';
import { PagedResponse, ProductResponse } from './api.types';
import { ProductsApiService } from './products-api.service';

describe('ProductsApiService', () => {
  let service: ProductsApiService;
  let httpMock: HttpTestingController;

  const createProduct = (overrides: Partial<ProductResponse> = {}): ProductResponse => ({
    id: 1,
    sku: 'SKU-1001',
    name: 'Industrial Storage Bin',
    description: 'Large plastic bin for warehouse spare parts',
    unit: 'piece',
    quantity: 10,
    storageLocationId: 1,
    storageLocationCode: 'A-01-01',
    status: 'ACTIVE',
    minimumQuantity: 3,
    lowStock: false,
    ...overrides,
  });

  const createPage = (
    content: ProductResponse[],
    overrides: Partial<PagedResponse<ProductResponse>> = {},
  ): PagedResponse<ProductResponse> => ({
    page: 0,
    size: 10,
    totalElements: content.length,
    totalPages: content.length > 0 ? 1 : 0,
    first: true,
    last: true,
    content,
    ...overrides,
  });

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });

    service = TestBed.inject(ProductsApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should request products with required pagination and sorting query params', () => {
    const expectedResponse = createPage([createProduct()]);

    service
      .getProducts({
        page: 0,
        size: 10,
        sortBy: 'id',
        direction: 'asc',
      })
      .subscribe((response) => {
        expect(response).toEqual(expectedResponse);
      });

    const request = httpMock.expectOne((httpRequest) => httpRequest.url === ApiPaths.products);

    expect(request.request.method).toBe('GET');
    expect(request.request.params.get('page')).toBe('0');
    expect(request.request.params.get('size')).toBe('10');
    expect(request.request.params.get('sortBy')).toBe('id');
    expect(request.request.params.get('direction')).toBe('asc');
    expect(request.request.params.has('search')).toBe(false);
    expect(request.request.params.has('status')).toBe(false);

    request.flush(expectedResponse);
  });

  it('should include trimmed search and status when provided', () => {
    const expectedResponse = createPage([
      createProduct({
        id: 2,
        sku: 'SKU-2002',
        name: 'Packing Tape Roll',
        status: 'BLOCKED',
        quantity: 0,
        minimumQuantity: 0,
      }),
    ]);

    service
      .getProducts({
        page: 2,
        size: 20,
        sortBy: 'sku',
        direction: 'desc',
        search: '  Packing  ',
        status: 'BLOCKED',
      })
      .subscribe((response) => {
        expect(response).toEqual(expectedResponse);
      });

    const request = httpMock.expectOne((httpRequest) => httpRequest.url === ApiPaths.products);

    expect(request.request.method).toBe('GET');
    expect(request.request.params.get('page')).toBe('2');
    expect(request.request.params.get('size')).toBe('20');
    expect(request.request.params.get('sortBy')).toBe('sku');
    expect(request.request.params.get('direction')).toBe('desc');
    expect(request.request.params.get('search')).toBe('Packing');
    expect(request.request.params.get('status')).toBe('BLOCKED');

    request.flush(expectedResponse);
  });

  it('should omit blank search from query params', () => {
    const expectedResponse = createPage([]);

    service
      .getProducts({
        page: 0,
        size: 10,
        sortBy: 'name',
        direction: 'asc',
        search: '   ',
        status: 'ACTIVE',
      })
      .subscribe((response) => {
        expect(response).toEqual(expectedResponse);
      });

    const request = httpMock.expectOne((httpRequest) => httpRequest.url === ApiPaths.products);

    expect(request.request.method).toBe('GET');
    expect(request.request.params.get('page')).toBe('0');
    expect(request.request.params.get('size')).toBe('10');
    expect(request.request.params.get('sortBy')).toBe('name');
    expect(request.request.params.get('direction')).toBe('asc');
    expect(request.request.params.has('search')).toBe(false);
    expect(request.request.params.get('status')).toBe('ACTIVE');

    request.flush(expectedResponse);
  });

  it('should pass backend errors to the caller', () => {
    let receivedErrorStatus: number | null = null;

    service
      .getProducts({
        page: 0,
        size: 10,
        sortBy: 'id',
        direction: 'asc',
      })
      .subscribe({
        next: () => {
          throw new Error('Expected request to fail');
        },
        error: (error) => {
          receivedErrorStatus = error.status;
        },
      });

    const request = httpMock.expectOne((httpRequest) => httpRequest.url === ApiPaths.products);

    request.flush(
      {
        timestamp: '2026-04-27T18:00:00Z',
        status: 500,
        error: 'Internal Server Error',
        message: 'Unexpected backend error',
        path: '/api/v1/products',
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
