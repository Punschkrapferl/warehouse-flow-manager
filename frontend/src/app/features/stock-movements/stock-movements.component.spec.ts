import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { of, Subject, throwError } from 'rxjs';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { StockMovementResponse, StockMovementSummaryResponse } from '../../core/api/api.types';
import {
  StockMovementQuery,
  StockMovementsApiService,
} from '../../core/api/stock-movements-api.service';
import { StockMovementsComponent } from './stock-movements.component';
import { StockMovementsFacade } from './stock-movements.facade';

describe('StockMovementsComponent', () => {
  let fixture: ComponentFixture<StockMovementsComponent>;
  let component: StockMovementsComponent;
  let facade: StockMovementsFacade;
  let stockMovementsApiMock: {
    getStockMovements: ReturnType<typeof vi.fn>;
    getStockMovementSummary: ReturnType<typeof vi.fn>;
  };

  const defaultMovementQuery = (
    overrides: Partial<StockMovementQuery> = {},
  ): StockMovementQuery => ({
    productId: undefined,
    movementType: undefined,
    from: undefined,
    to: undefined,
    ...overrides,
  });

  const defaultSummaryQuery = (
    overrides: Partial<Omit<StockMovementQuery, 'movementType'>> = {},
  ): Omit<StockMovementQuery, 'movementType'> => ({
    productId: undefined,
    from: undefined,
    to: undefined,
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

  const createSummary = (
    overrides: Partial<StockMovementSummaryResponse> = {},
  ): StockMovementSummaryResponse => ({
    productId: null,
    productSku: null,
    from: null,
    to: null,
    totalMovements: 2,
    inboundMovementCount: 2,
    outboundMovementCount: 0,
    adjustmentMovementCount: 0,
    totalInboundQuantity: 12,
    totalOutboundQuantity: 0,
    totalAdjustmentQuantity: 0,
    currentQuantity: null,
    latestMovementAt: '2026-04-26T14:19:04.288332Z',
    ...overrides,
  });

  const expectMovementApiCalledWith = (query: StockMovementQuery): void => {
    expect(stockMovementsApiMock.getStockMovements).toHaveBeenCalledTimes(1);
    expect(stockMovementsApiMock.getStockMovements).toHaveBeenCalledWith(query);
  };

  const expectSummaryApiCalledWith = (query: Omit<StockMovementQuery, 'movementType'>): void => {
    expect(stockMovementsApiMock.getStockMovementSummary).toHaveBeenCalledTimes(1);
    expect(stockMovementsApiMock.getStockMovementSummary).toHaveBeenCalledWith(query);
  };

  beforeEach(async () => {
    stockMovementsApiMock = {
      getStockMovements: vi.fn(),
      getStockMovementSummary: vi.fn(),
    };

    await TestBed.configureTestingModule({
      imports: [StockMovementsComponent],
      providers: [
        {
          provide: StockMovementsApiService,
          useValue: stockMovementsApiMock,
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(StockMovementsComponent);
    component = fixture.componentInstance;
    facade = fixture.debugElement.injector.get(StockMovementsFacade);
  });

  it('should load stock movements and summary on init with default filters', () => {
    const movements = [
      createMovement({ id: 1, productId: 1, productSku: 'SKU-1001' }),
      createMovement({ id: 2, productId: 2, productSku: 'SKU-1002', quantity: 2 }),
    ];
    const summary = createSummary();

    stockMovementsApiMock.getStockMovements.mockReturnValue(of(movements));
    stockMovementsApiMock.getStockMovementSummary.mockReturnValue(of(summary));

    fixture.detectChanges();

    expectMovementApiCalledWith(defaultMovementQuery());
    expectSummaryApiCalledWith(defaultSummaryQuery());
    expect(facade.loadingMovements).toBe(false);
    expect(facade.loadingSummary).toBe(false);
    expect(facade.errorMessage).toBe('');
    expect(facade.movements).toEqual(movements);
    expect(facade.summary).toEqual(summary);
  });

  it('should pass product, movement type, and date filters to the API', () => {
    stockMovementsApiMock.getStockMovements.mockReturnValue(of([]));
    stockMovementsApiMock.getStockMovementSummary.mockReturnValue(of(createSummary()));

    fixture.detectChanges();

    stockMovementsApiMock.getStockMovements.mockClear();
    stockMovementsApiMock.getStockMovementSummary.mockClear();

    facade.filters = {
      productId: '1',
      movementType: 'OUTBOUND',
      from: '2026-04-26T10:30',
      to: '2026-04-26T12:45',
    };

    const expectedFrom = new Date('2026-04-26T10:30').toISOString();
    const expectedTo = new Date('2026-04-26T12:45').toISOString();

    facade.applyFilters();

    expectMovementApiCalledWith(
      defaultMovementQuery({
        productId: 1,
        movementType: 'OUTBOUND',
        from: expectedFrom,
        to: expectedTo,
      }),
    );

    expectSummaryApiCalledWith(
      defaultSummaryQuery({
        productId: 1,
        from: expectedFrom,
        to: expectedTo,
      }),
    );
  });

  it('should omit movement type when filter is ALL', () => {
    stockMovementsApiMock.getStockMovements.mockReturnValue(of([]));
    stockMovementsApiMock.getStockMovementSummary.mockReturnValue(of(createSummary()));

    fixture.detectChanges();

    stockMovementsApiMock.getStockMovements.mockClear();
    stockMovementsApiMock.getStockMovementSummary.mockClear();

    facade.filters = {
      productId: '2',
      movementType: 'ALL',
      from: '',
      to: '',
    };

    facade.applyFilters();

    expectMovementApiCalledWith(
      defaultMovementQuery({
        productId: 2,
        movementType: undefined,
      }),
    );

    expectSummaryApiCalledWith(
      defaultSummaryQuery({
        productId: 2,
      }),
    );
  });

  it('should omit productId when input is empty or invalid', () => {
    stockMovementsApiMock.getStockMovements.mockReturnValue(of([]));
    stockMovementsApiMock.getStockMovementSummary.mockReturnValue(of(createSummary()));

    fixture.detectChanges();

    stockMovementsApiMock.getStockMovements.mockClear();
    stockMovementsApiMock.getStockMovementSummary.mockClear();

    facade.filters = {
      productId: '-5',
      movementType: 'INBOUND',
      from: '',
      to: '',
    };

    facade.applyFilters();

    expectMovementApiCalledWith(
      defaultMovementQuery({
        productId: undefined,
        movementType: 'INBOUND',
      }),
    );

    expectSummaryApiCalledWith(defaultSummaryQuery());
  });

  it('should reset filters and reload data', () => {
    stockMovementsApiMock.getStockMovements.mockReturnValue(of([]));
    stockMovementsApiMock.getStockMovementSummary.mockReturnValue(of(createSummary()));

    fixture.detectChanges();

    stockMovementsApiMock.getStockMovements.mockClear();
    stockMovementsApiMock.getStockMovementSummary.mockClear();

    facade.filters = {
      productId: '1',
      movementType: 'ADJUSTMENT',
      from: '2026-04-26T10:30',
      to: '2026-04-26T12:45',
    };

    facade.resetFilters();

    expect(facade.filters).toEqual({
      productId: '',
      movementType: 'ALL',
      from: '',
      to: '',
    });

    expectMovementApiCalledWith(defaultMovementQuery());
    expectSummaryApiCalledWith(defaultSummaryQuery());
  });

  it('should set loading flags during request and update state when both requests succeed', () => {
    const movements$ = new Subject<StockMovementResponse[]>();
    const summary$ = new Subject<StockMovementSummaryResponse>();

    const movements = [
      createMovement({
        id: 4,
        movementType: 'OUTBOUND',
        quantity: 2,
        resultingQuantity: 13,
        note: 'Demo outbound movement',
      }),
    ];
    const summary = createSummary({
      totalMovements: 1,
      inboundMovementCount: 0,
      outboundMovementCount: 1,
      totalInboundQuantity: 0,
      totalOutboundQuantity: 2,
    });

    stockMovementsApiMock.getStockMovements.mockReturnValue(movements$.asObservable());
    stockMovementsApiMock.getStockMovementSummary.mockReturnValue(summary$.asObservable());

    fixture.detectChanges();

    expect(facade.loadingMovements).toBe(true);
    expect(facade.loadingSummary).toBe(true);

    movements$.next(movements);
    movements$.complete();

    expect(facade.loadingMovements).toBe(true);
    expect(facade.loadingSummary).toBe(true);

    summary$.next(summary);
    summary$.complete();

    expect(facade.loadingMovements).toBe(false);
    expect(facade.loadingSummary).toBe(false);
    expect(facade.movements).toEqual(movements);
    expect(facade.summary).toEqual(summary);
  });

  it('should set error message and clear state when loading fails', () => {
    stockMovementsApiMock.getStockMovements.mockReturnValue(
      throwError(() => new Error('Could not load stock movements.')),
    );
    stockMovementsApiMock.getStockMovementSummary.mockReturnValue(of(createSummary()));

    fixture.detectChanges();

    expect(facade.loadingMovements).toBe(false);
    expect(facade.loadingSummary).toBe(false);
    expect(facade.errorMessage).toBe('Could not load stock movements.');
    expect(facade.movements).toEqual([]);
    expect(facade.summary).toBeNull();
  });

  it('should render stock movement rows returned by the API', () => {
    const movements = [
      createMovement({
        id: 5,
        productId: 2,
        productSku: 'SKU-1002',
        movementType: 'ADJUSTMENT',
        quantity: 8,
        resultingQuantity: 8,
        note: 'Demo stock correction',
      }),
      createMovement({
        id: 4,
        productId: 1,
        productSku: 'SKU-1001',
        movementType: 'OUTBOUND',
        quantity: 2,
        resultingQuantity: 13,
        note: 'Demo outbound movement',
      }),
    ];

    stockMovementsApiMock.getStockMovements.mockReturnValue(of(movements));
    stockMovementsApiMock.getStockMovementSummary.mockReturnValue(
      of(
        createSummary({
          totalMovements: 2,
          inboundMovementCount: 0,
          outboundMovementCount: 1,
          adjustmentMovementCount: 1,
          totalInboundQuantity: 0,
          totalOutboundQuantity: 2,
          totalAdjustmentQuantity: 8,
        }),
      ),
    );

    fixture.detectChanges();

    const rows = fixture.debugElement.queryAll(By.css('tbody tr'));
    expect(rows.length).toBe(2);

    const text = fixture.nativeElement.textContent;
    expect(text).toContain('SKU-1002');
    expect(text).toContain('ADJUSTMENT');
    expect(text).toContain('Demo stock correction');
    expect(text).toContain('SKU-1001');
    expect(text).toContain('OUTBOUND');
    expect(text).toContain('2 record(s) returned by the backend.');
  });

  it('should render summary KPI values returned by the API', () => {
    stockMovementsApiMock.getStockMovements.mockReturnValue(of([]));
    stockMovementsApiMock.getStockMovementSummary.mockReturnValue(
      of(
        createSummary({
          totalMovements: 5,
          inboundMovementCount: 3,
          outboundMovementCount: 1,
          adjustmentMovementCount: 1,
          totalInboundQuantity: 17,
          totalOutboundQuantity: 2,
          totalAdjustmentQuantity: 8,
        }),
      ),
    );

    fixture.detectChanges();

    const text = fixture.nativeElement.textContent;
    expect(text).toContain('5');
    expect(text).toContain('17');
    expect(text).toContain('2');
    expect(text).toContain('8');
  });

  it('should render empty state when no movements are returned', () => {
    stockMovementsApiMock.getStockMovements.mockReturnValue(of([]));
    stockMovementsApiMock.getStockMovementSummary.mockReturnValue(of(createSummary()));

    fixture.detectChanges();

    const text = fixture.nativeElement.textContent;
    expect(text).toContain('No stock movements found for the selected filters.');
  });

  it('should render error state when loading fails', () => {
    stockMovementsApiMock.getStockMovements.mockReturnValue(
      throwError(() => new Error('Could not load stock movement data.')),
    );
    stockMovementsApiMock.getStockMovementSummary.mockReturnValue(of(createSummary()));

    fixture.detectChanges();

    const text = fixture.nativeElement.textContent;
    expect(text).toContain('Could not load stock movement data.');
  });

  it('should trigger filtering from the Apply filters button', () => {
    stockMovementsApiMock.getStockMovements.mockReturnValue(of([]));
    stockMovementsApiMock.getStockMovementSummary.mockReturnValue(of(createSummary()));

    fixture.detectChanges();

    facade.filters.productId = '1';
    facade.filters.movementType = 'INBOUND';

    stockMovementsApiMock.getStockMovements.mockClear();
    stockMovementsApiMock.getStockMovementSummary.mockClear();

    const applyButton: HTMLButtonElement = fixture.nativeElement.querySelector(
      '.movements-filters__buttons .button--primary',
    );

    applyButton.click();

    expectMovementApiCalledWith(
      defaultMovementQuery({
        productId: 1,
        movementType: 'INBOUND',
      }),
    );

    expectSummaryApiCalledWith(
      defaultSummaryQuery({
        productId: 1,
      }),
    );
  });

  it('should trigger refresh from the Refresh activity button', () => {
    stockMovementsApiMock.getStockMovements.mockReturnValue(of([]));
    stockMovementsApiMock.getStockMovementSummary.mockReturnValue(of(createSummary()));

    fixture.detectChanges();

    stockMovementsApiMock.getStockMovements.mockClear();
    stockMovementsApiMock.getStockMovementSummary.mockClear();

    const refreshButton: HTMLButtonElement = fixture.nativeElement.querySelector(
      '.page-hero__actions .button',
    );

    refreshButton.click();

    expectMovementApiCalledWith(defaultMovementQuery());
    expectSummaryApiCalledWith(defaultSummaryQuery());
  });

  it('should return correct badge and quantity classes for movement types', () => {
    expect(component['movementBadgeClass']('INBOUND')).toBe('badge badge--success');
    expect(component['movementBadgeClass']('OUTBOUND')).toBe('badge badge--warning');
    expect(component['movementBadgeClass']('ADJUSTMENT')).toBe('badge badge--neutral');

    expect(component['movementQuantityClass']('INBOUND')).toBe('metric-pill metric-pill--success');
    expect(component['movementQuantityClass']('OUTBOUND')).toBe(
      'metric-pill metric-pill--critical',
    );
    expect(component['movementQuantityClass']('ADJUSTMENT')).toBe(
      'metric-pill metric-pill--neutral',
    );
  });
});
