import { HttpErrorResponse } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { of, Subject, throwError } from 'rxjs';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import {
  HealthResponse,
  ProductResponse,
  ReplenishmentRecommendationResponse,
  StockMovementResponse,
} from '../../core/api/api.types';
import { DashboardApiService } from '../../core/api/dashboard-api.service';
import { DashboardComponent } from './dashboard.component';
import { DashboardFacade } from './dashboard.facade';

describe('DashboardComponent', () => {
  let fixture: ComponentFixture<DashboardComponent>;
  let component: DashboardComponent;
  let facade: DashboardFacade;
  let dashboardApiMock: {
    getHealth: ReturnType<typeof vi.fn>;
    getLowStockProducts: ReturnType<typeof vi.fn>;
    getReplenishmentCandidates: ReturnType<typeof vi.fn>;
    getRecentStockMovements: ReturnType<typeof vi.fn>;
  };

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

  const mockSuccessfulDashboardLoad = (
    overrides: {
      health?: HealthResponse;
      lowStockProducts?: ProductResponse[];
      replenishmentCandidates?: ReplenishmentRecommendationResponse[];
      recentMovements?: StockMovementResponse[];
    } = {},
  ): void => {
    dashboardApiMock.getHealth.mockReturnValue(of(overrides.health ?? createHealth()));
    dashboardApiMock.getLowStockProducts.mockReturnValue(
      of(overrides.lowStockProducts ?? [createProduct()]),
    );
    dashboardApiMock.getReplenishmentCandidates.mockReturnValue(
      of(overrides.replenishmentCandidates ?? [createCandidate()]),
    );
    dashboardApiMock.getRecentStockMovements.mockReturnValue(
      of(overrides.recentMovements ?? [createMovement()]),
    );
  };

  beforeEach(async () => {
    dashboardApiMock = {
      getHealth: vi.fn(),
      getLowStockProducts: vi.fn(),
      getReplenishmentCandidates: vi.fn(),
      getRecentStockMovements: vi.fn(),
    };

    await TestBed.configureTestingModule({
      imports: [DashboardComponent],
      providers: [
        {
          provide: DashboardApiService,
          useValue: dashboardApiMock,
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(DashboardComponent);
    component = fixture.componentInstance;
    facade = fixture.debugElement.injector.get(DashboardFacade);
  });

  it('should load dashboard data on init', () => {
    const health = createHealth();
    const lowStockProducts = [createProduct({ sku: 'SKU-1002', name: 'Packing Tape Roll' })];
    const replenishmentCandidates = [
      createCandidate({ productId: 2, sku: 'SKU-1002', name: 'Packing Tape Roll' }),
    ];
    const recentMovements = [createMovement({ id: 2, productSku: 'SKU-1002', quantity: 2 })];

    mockSuccessfulDashboardLoad({
      health,
      lowStockProducts,
      replenishmentCandidates,
      recentMovements,
    });

    fixture.detectChanges();

    expect(dashboardApiMock.getHealth).toHaveBeenCalledTimes(1);
    expect(dashboardApiMock.getLowStockProducts).toHaveBeenCalledTimes(1);
    expect(dashboardApiMock.getReplenishmentCandidates).toHaveBeenCalledTimes(1);
    expect(dashboardApiMock.getReplenishmentCandidates).toHaveBeenCalledWith(30);
    expect(dashboardApiMock.getRecentStockMovements).toHaveBeenCalledTimes(1);
    expect(dashboardApiMock.getRecentStockMovements).toHaveBeenCalledWith(8);

    expect(facade.loading).toBe(false);
    expect(facade.errorMessage).toBe('');
    expect(facade.health).toEqual(health);
    expect(facade.lowStockProducts).toEqual(lowStockProducts);
    expect(facade.replenishmentCandidates).toEqual(replenishmentCandidates);
    expect(facade.recentMovements).toEqual(recentMovements);
  });

  it('should set loading while dashboard request is pending', () => {
    const health$ = new Subject<HealthResponse>();
    const lowStock$ = new Subject<ProductResponse[]>();
    const candidates$ = new Subject<ReplenishmentRecommendationResponse[]>();
    const movements$ = new Subject<StockMovementResponse[]>();

    dashboardApiMock.getHealth.mockReturnValue(health$.asObservable());
    dashboardApiMock.getLowStockProducts.mockReturnValue(lowStock$.asObservable());
    dashboardApiMock.getReplenishmentCandidates.mockReturnValue(candidates$.asObservable());
    dashboardApiMock.getRecentStockMovements.mockReturnValue(movements$.asObservable());

    fixture.detectChanges();

    expect(facade.loading).toBe(true);

    health$.next(createHealth());
    health$.complete();
    lowStock$.next([createProduct()]);
    lowStock$.complete();
    candidates$.next([createCandidate()]);
    candidates$.complete();

    expect(facade.loading).toBe(true);

    movements$.next([createMovement()]);
    movements$.complete();

    expect(facade.loading).toBe(false);
    expect(facade.health?.status).toBe('UP');
    expect(facade.lowStockProducts.length).toBe(1);
    expect(facade.replenishmentCandidates.length).toBe(1);
    expect(facade.recentMovements.length).toBe(1);
  });

  it('should compute critical replenishment candidate count', () => {
    mockSuccessfulDashboardLoad({
      replenishmentCandidates: [
        createCandidate({ productId: 1, priority: 'CRITICAL' }),
        createCandidate({ productId: 2, priority: 'HIGH' }),
        createCandidate({ productId: 3, priority: 'MEDIUM' }),
        createCandidate({ productId: 4, priority: 'CRITICAL' }),
      ],
    });

    fixture.detectChanges();

    expect(facade.criticalCount).toBe(2);
  });

  it('should clear dashboard state and show error when loading fails', () => {
    dashboardApiMock.getHealth.mockReturnValue(
      throwError(
        () =>
          new HttpErrorResponse({
            status: 503,
            statusText: 'Service Unavailable',
            url: '/api/v1/health',
          }),
      ),
    );
    dashboardApiMock.getLowStockProducts.mockReturnValue(of([createProduct()]));
    dashboardApiMock.getReplenishmentCandidates.mockReturnValue(of([createCandidate()]));
    dashboardApiMock.getRecentStockMovements.mockReturnValue(of([createMovement()]));

    fixture.detectChanges();

    expect(facade.loading).toBe(false);
    expect(facade.errorMessage).toContain('Could not load dashboard data (503)');
    expect(facade.health).toBeNull();
    expect(facade.lowStockProducts).toEqual([]);
    expect(facade.replenishmentCandidates).toEqual([]);
    expect(facade.recentMovements).toEqual([]);
  });

  it('should render KPI cards from loaded dashboard data', () => {
    mockSuccessfulDashboardLoad({
      lowStockProducts: [
        createProduct({ id: 1, sku: 'SKU-1001' }),
        createProduct({ id: 2, sku: 'SKU-1002' }),
      ],
      replenishmentCandidates: [
        createCandidate({ productId: 1, priority: 'CRITICAL' }),
        createCandidate({ productId: 2, priority: 'HIGH' }),
      ],
      recentMovements: [
        createMovement({ id: 1 }),
        createMovement({ id: 2 }),
        createMovement({ id: 3 }),
      ],
    });

    fixture.detectChanges();

    const text = fixture.nativeElement.textContent;

    expect(text).toContain('UP');
    expect(text).toContain('Low-stock alerts');
    expect(text).toContain('2');
    expect(text).toContain('Critical shortages');
    expect(text).toContain('1');
    expect(text).toContain('Recent movement events');
    expect(text).toContain('3');
  });

  it('should render low-stock products', () => {
    mockSuccessfulDashboardLoad({
      lowStockProducts: [
        createProduct({
          id: 2,
          sku: 'SKU-1002',
          name: 'Packing Tape Roll',
          quantity: 2,
          minimumQuantity: 5,
        }),
      ],
    });

    fixture.detectChanges();

    const text = fixture.nativeElement.textContent;
    expect(text).toContain('Low-stock watchlist');
    expect(text).toContain('SKU-1002');
    expect(text).toContain('Packing Tape Roll');
    expect(text).toContain('A-01-01');
  });

  it('should render empty low-stock state', () => {
    mockSuccessfulDashboardLoad({
      lowStockProducts: [],
    });

    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('No low-stock products right now.');
  });

  it('should render replenishment candidates', () => {
    mockSuccessfulDashboardLoad({
      replenishmentCandidates: [
        createCandidate({
          productId: 2,
          sku: 'SKU-1002',
          name: 'Packing Tape Roll',
          priority: 'HIGH',
          currentQuantity: 2,
          minimumQuantity: 5,
          recommendedReorderQuantity: 8,
          recentOutboundQuantity: 4,
        }),
      ],
    });

    fixture.detectChanges();

    const text = fixture.nativeElement.textContent;
    expect(text).toContain('Replenishment queue');
    expect(text).toContain('Packing Tape Roll');
    expect(text).toContain('SKU-1002');
    expect(text).toContain('HIGH');
    expect(text).toContain('Reorder');
    expect(text).toContain('8');
  });

  it('should render only top six replenishment candidates', () => {
    const candidates = Array.from({ length: 8 }, (_, index) =>
      createCandidate({
        productId: index + 1,
        sku: `SKU-${index + 1}`,
        name: `Candidate ${index + 1}`,
      }),
    );

    mockSuccessfulDashboardLoad({
      replenishmentCandidates: candidates,
    });

    fixture.detectChanges();

    const queueItems = fixture.debugElement.queryAll(By.css('.queue-item'));
    expect(queueItems.length).toBe(6);

    const text = fixture.nativeElement.textContent;
    expect(text).toContain('Candidate 1');
    expect(text).toContain('Candidate 6');
    expect(text).not.toContain('Candidate 7');
    expect(text).not.toContain('Candidate 8');
  });

  it('should render empty replenishment state', () => {
    mockSuccessfulDashboardLoad({
      replenishmentCandidates: [],
    });

    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('No replenishment candidates found.');
  });

  it('should render recent stock movements', () => {
    mockSuccessfulDashboardLoad({
      recentMovements: [
        createMovement({
          id: 5,
          productSku: 'SKU-1002',
          movementType: 'ADJUSTMENT',
          quantity: 8,
          resultingQuantity: 8,
          note: 'Demo stock correction',
        }),
      ],
    });

    fixture.detectChanges();

    const rows = fixture.debugElement.queryAll(By.css('tbody tr'));
    expect(rows.length).toBeGreaterThanOrEqual(1);

    const text = fixture.nativeElement.textContent;
    expect(text).toContain('Recent stock movement stream');
    expect(text).toContain('SKU-1002');
    expect(text).toContain('ADJUSTMENT');
    expect(text).toContain('Demo stock correction');
  });

  it('should render empty recent movement state', () => {
    mockSuccessfulDashboardLoad({
      recentMovements: [],
    });

    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('No stock movements available yet.');
  });

  it('should render dashboard error state', () => {
    dashboardApiMock.getHealth.mockReturnValue(
      throwError(
        () =>
          new HttpErrorResponse({
            status: 500,
            statusText: 'Server Error',
            url: '/api/v1/health',
          }),
      ),
    );
    dashboardApiMock.getLowStockProducts.mockReturnValue(of([]));
    dashboardApiMock.getReplenishmentCandidates.mockReturnValue(of([]));
    dashboardApiMock.getRecentStockMovements.mockReturnValue(of([]));

    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Could not load dashboard data (500)');
  });

  it('should trigger dashboard refresh from Refresh board button', () => {
    mockSuccessfulDashboardLoad();

    fixture.detectChanges();

    dashboardApiMock.getHealth.mockClear();
    dashboardApiMock.getLowStockProducts.mockClear();
    dashboardApiMock.getReplenishmentCandidates.mockClear();
    dashboardApiMock.getRecentStockMovements.mockClear();

    const refreshButton: HTMLButtonElement =
      fixture.nativeElement.querySelector('.page__actions .button');

    refreshButton.click();

    expect(dashboardApiMock.getHealth).toHaveBeenCalledTimes(1);
    expect(dashboardApiMock.getLowStockProducts).toHaveBeenCalledTimes(1);
    expect(dashboardApiMock.getReplenishmentCandidates).toHaveBeenCalledTimes(1);
    expect(dashboardApiMock.getRecentStockMovements).toHaveBeenCalledTimes(1);
  });

  it('should return correct priority classes', () => {
    expect(component['priorityClass']('CRITICAL')).toBe('badge badge--danger');
    expect(component['priorityClass']('HIGH')).toBe('badge badge--warning');
    expect(component['priorityClass']('MEDIUM')).toBe('badge badge--neutral');
  });

  it('should return correct movement badge classes', () => {
    expect(component['movementClass']('INBOUND')).toBe('badge badge--success');
    expect(component['movementClass']('OUTBOUND')).toBe('badge badge--warning');
    expect(component['movementClass']('ADJUSTMENT')).toBe('badge badge--neutral');
  });

  it('should return correct movement quantity classes', () => {
    expect(component['movementQuantityClass']('INBOUND')).toBe('metric-pill metric-pill--success');
    expect(component['movementQuantityClass']('OUTBOUND')).toBe(
      'metric-pill metric-pill--critical',
    );
    expect(component['movementQuantityClass']('ADJUSTMENT')).toBe(
      'metric-pill metric-pill--neutral',
    );
  });
});
