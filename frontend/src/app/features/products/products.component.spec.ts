import { HttpErrorResponse } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { of, Subject, throwError } from 'rxjs';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { PagedResponse, ProductResponse } from '../../core/api/api.types';
import { ProductQuery, ProductsApiService } from '../../core/api/products-api.service';
import { ProductsComponent } from './products.component';
import { ProductsFacade } from './products.facade';

describe('ProductsComponent', () => {
  let fixture: ComponentFixture<ProductsComponent>;
  let component: ProductsComponent;
  let facade: ProductsFacade;
  let productsApiMock: {
    getProducts: ReturnType<typeof vi.fn>;
  };

  const defaultQuery = (overrides: Partial<ProductQuery> = {}): ProductQuery => ({
    page: 0,
    size: 10,
    sortBy: 'id',
    direction: 'asc',
    search: undefined,
    status: undefined,
    ...overrides,
  });

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

  const expectGetProductsCalledWith = (query: ProductQuery): void => {
    expect(productsApiMock.getProducts).toHaveBeenCalledTimes(1);
    expect(productsApiMock.getProducts).toHaveBeenCalledWith(query);
  };

  beforeEach(async () => {
    productsApiMock = {
      getProducts: vi.fn(),
    };

    await TestBed.configureTestingModule({
      imports: [ProductsComponent],
      providers: [
        {
          provide: ProductsApiService,
          useValue: productsApiMock,
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ProductsComponent);
    component = fixture.componentInstance;
    facade = fixture.debugElement.injector.get(ProductsFacade);
  });

  it('should load products on init with default query', () => {
    const page = createPage([createProduct()]);
    productsApiMock.getProducts.mockReturnValue(of(page));

    fixture.detectChanges();

    expectGetProductsCalledWith(defaultQuery());
    expect(facade.loading).toBe(false);
    expect(facade.errorMessage).toBe('');
    expect(facade.pageData).toEqual(page);
  });

  it('should call API with current filters when applyFilters is triggered', () => {
    productsApiMock.getProducts.mockReturnValue(of(createPage([])));
    fixture.detectChanges();
    productsApiMock.getProducts.mockClear();

    facade.filters = {
      search: '1001',
      status: 'BLOCKED',
      page: 3,
      size: 20,
      sortBy: 'sku',
      direction: 'desc',
    };

    facade.applyFilters();

    expectGetProductsCalledWith(
      defaultQuery({
        size: 20,
        sortBy: 'sku',
        direction: 'desc',
        search: '1001',
        status: 'BLOCKED',
      }),
    );
    expect(facade.filters.page).toBe(0);
  });

  it('should omit status when status filter is ALL', () => {
    productsApiMock.getProducts.mockReturnValue(of(createPage([])));
    fixture.detectChanges();
    productsApiMock.getProducts.mockClear();

    facade.filters = {
      search: 'Packing',
      status: 'ALL',
      page: 0,
      size: 10,
      sortBy: 'name',
      direction: 'asc',
    };

    facade.applyFilters();

    expectGetProductsCalledWith(
      defaultQuery({
        sortBy: 'name',
        search: 'Packing',
        status: undefined,
      }),
    );
  });

  it('should trim search values before calling the API', () => {
    productsApiMock.getProducts.mockReturnValue(of(createPage([])));
    fixture.detectChanges();
    productsApiMock.getProducts.mockClear();

    facade.filters = {
      search: '  Packing  ',
      status: 'ACTIVE',
      page: 0,
      size: 10,
      sortBy: 'name',
      direction: 'asc',
    };

    facade.applyFilters();

    expectGetProductsCalledWith(
      defaultQuery({
        sortBy: 'name',
        search: 'Packing',
        status: 'ACTIVE',
      }),
    );
  });

  it('should omit search when search input is blank', () => {
    productsApiMock.getProducts.mockReturnValue(of(createPage([])));
    fixture.detectChanges();
    productsApiMock.getProducts.mockClear();

    facade.filters = {
      search: '   ',
      status: 'ACTIVE',
      page: 0,
      size: 10,
      sortBy: 'name',
      direction: 'asc',
    };

    facade.applyFilters();

    expectGetProductsCalledWith(
      defaultQuery({
        sortBy: 'name',
        search: undefined,
        status: 'ACTIVE',
      }),
    );
  });

  it('should reset filters to defaults and reload products', () => {
    productsApiMock.getProducts.mockReturnValue(of(createPage([])));
    fixture.detectChanges();
    productsApiMock.getProducts.mockClear();

    facade.filters = {
      search: 'Barcode',
      status: 'DISCONTINUED',
      page: 4,
      size: 50,
      sortBy: 'name',
      direction: 'desc',
    };

    facade.resetFilters();

    expect(facade.filters).toEqual({
      search: '',
      status: 'ALL',
      page: 0,
      size: 10,
      sortBy: 'id',
      direction: 'asc',
    });
    expectGetProductsCalledWith(defaultQuery());
  });

  it('should update page size, reset page to 0, and reload products', () => {
    productsApiMock.getProducts.mockReturnValue(of(createPage([])));
    fixture.detectChanges();
    productsApiMock.getProducts.mockClear();

    facade.filters.page = 5;
    expect(facade.filters.size).toBe(10);

    facade.pageSizeChanged(20);

    expect(facade.filters.size).toBe(20);
    expect(facade.filters.page).toBe(0);
    expectGetProductsCalledWith(defaultQuery({ size: 20 }));
  });

  it('should load previous page when not on first page', () => {
    productsApiMock.getProducts.mockReturnValue(of(createPage([])));
    fixture.detectChanges();
    productsApiMock.getProducts.mockClear();

    facade.pageData = createPage([], {
      page: 2,
      totalPages: 5,
      first: false,
      last: false,
    });
    facade.filters.page = 2;

    facade.previousPage();

    expect(facade.filters.page).toBe(1);
    expectGetProductsCalledWith(defaultQuery({ page: 1 }));
  });

  it('should not load previous page when already on first page', () => {
    productsApiMock.getProducts.mockReturnValue(of(createPage([])));
    fixture.detectChanges();
    productsApiMock.getProducts.mockClear();

    facade.pageData = createPage([], {
      page: 0,
      totalPages: 1,
      first: true,
      last: true,
    });
    facade.filters.page = 0;

    facade.previousPage();

    expect(facade.filters.page).toBe(0);
    expect(productsApiMock.getProducts).not.toHaveBeenCalled();
  });

  it('should load next page when not on last page', () => {
    productsApiMock.getProducts.mockReturnValue(of(createPage([])));
    fixture.detectChanges();
    productsApiMock.getProducts.mockClear();

    facade.pageData = createPage([], {
      page: 1,
      totalPages: 5,
      first: false,
      last: false,
    });
    facade.filters.page = 1;

    facade.nextPage();

    expect(facade.filters.page).toBe(2);
    expectGetProductsCalledWith(defaultQuery({ page: 2 }));
  });

  it('should not load next page when already on last page', () => {
    productsApiMock.getProducts.mockReturnValue(of(createPage([])));
    fixture.detectChanges();
    productsApiMock.getProducts.mockClear();

    facade.pageData = createPage([], {
      page: 0,
      totalPages: 1,
      first: true,
      last: true,
    });
    facade.filters.page = 0;

    facade.nextPage();

    expect(facade.filters.page).toBe(0);
    expect(productsApiMock.getProducts).not.toHaveBeenCalled();
  });

  it('should set loading during request and update pageData when request succeeds', () => {
    const response$ = new Subject<PagedResponse<ProductResponse>>();
    const expectedPage = createPage([
      createProduct({ id: 4, sku: 'SKU-BLOCKED-DEMO', status: 'BLOCKED', quantity: 0 }),
    ]);

    productsApiMock.getProducts.mockReturnValue(response$.asObservable());
    fixture.detectChanges();

    expect(facade.loading).toBe(true);

    response$.next(expectedPage);
    response$.complete();

    expect(facade.loading).toBe(false);
    expect(facade.errorMessage).toBe('');
    expect(facade.pageData).toEqual(expectedPage);
  });

  it('should set errorMessage and reset pageData when request fails', () => {
    productsApiMock.getProducts.mockReturnValue(
      throwError(
        () =>
          new HttpErrorResponse({
            status: 500,
            statusText: 'Server Error',
            url: '/api/v1/products',
          }),
      ),
    );

    fixture.detectChanges();

    expect(facade.loading).toBe(false);
    expect(facade.errorMessage).toContain('Could not load products (500)');
    expect(facade.pageData).toEqual({
      page: 0,
      size: 10,
      totalElements: 0,
      totalPages: 0,
      first: true,
      last: true,
      content: [],
    });
  });

  it('should render rows returned by the API', () => {
    const page = createPage([
      createProduct({ id: 1, sku: 'SKU-1001', name: 'Industrial Storage Bin' }),
      createProduct({ id: 2, sku: 'SKU-1002', name: 'Packing Tape Roll' }),
    ]);

    productsApiMock.getProducts.mockReturnValue(of(page));
    fixture.detectChanges();

    const rows = fixture.debugElement.queryAll(By.css('tbody tr'));
    expect(rows.length).toBe(2);

    const text = fixture.nativeElement.textContent;
    expect(text).toContain('Industrial Storage Bin');
    expect(text).toContain('Packing Tape Roll');
    expect(text).toContain('2 total product(s) returned by the backend.');
  });

  it('should render empty state when filtered result is empty', () => {
    productsApiMock.getProducts.mockReturnValue(of(createPage([])));
    fixture.detectChanges();

    const text = fixture.nativeElement.textContent;
    expect(text).toContain('No products found for the selected filters.');
  });

  it('should render error state when loading products fails', () => {
    productsApiMock.getProducts.mockReturnValue(
      throwError(
        () =>
          new HttpErrorResponse({
            status: 503,
            statusText: 'Service Unavailable',
            url: '/api/v1/products',
          }),
      ),
    );

    fixture.detectChanges();

    const text = fixture.nativeElement.textContent;
    expect(text).toContain('Could not load products (503)');
  });

  it('should trigger filtering from the Apply filters button and update the UI', () => {
    productsApiMock.getProducts.mockReturnValue(of(createPage([createProduct()])));
    fixture.detectChanges();

    facade.filters.search = '1001';
    facade.filters.status = 'BLOCKED';

    const filteredPage = createPage([
      createProduct({
        id: 4,
        sku: 'SKU-BLOCKED-DEMO',
        name: 'Blocked Demo Item',
        status: 'BLOCKED',
        quantity: 0,
        minimumQuantity: 0,
        lowStock: false,
      }),
    ]);

    productsApiMock.getProducts.mockClear();
    productsApiMock.getProducts.mockReturnValue(of(filteredPage));

    const applyButton: HTMLButtonElement = fixture.nativeElement.querySelector(
      '.products-filters__buttons .button--primary',
    );

    applyButton.click();
    fixture.detectChanges();

    expectGetProductsCalledWith(
      defaultQuery({
        search: '1001',
        status: 'BLOCKED',
      }),
    );

    const text = fixture.nativeElement.textContent;
    expect(text).toContain('Blocked Demo Item');
    expect(text).toContain('1 total product(s) returned by the backend.');
  });

  it('should trigger refresh from the Refresh data button', () => {
    productsApiMock.getProducts.mockReturnValue(of(createPage([createProduct()])));
    fixture.detectChanges();
    productsApiMock.getProducts.mockClear();

    const refreshButton: HTMLButtonElement =
      fixture.nativeElement.querySelector('.page__actions .button');

    refreshButton.click();

    expectGetProductsCalledWith(defaultQuery());
  });

  it('should return correct badge class for product status', () => {
    expect(component['statusClass']('ACTIVE')).toBe('badge badge--success');
    expect(component['statusClass']('BLOCKED')).toBe('badge badge--warning');
    expect(component['statusClass']('DISCONTINUED')).toBe('badge badge--neutral');
  });
});
