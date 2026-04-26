import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { of, Subject, throwError } from 'rxjs';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import {
  StorageLocationResponse,
  StorageLocationStockOverviewResponse,
} from '../../core/api/api.types';
import { StorageLocationsApiService } from '../../core/api/storage-locations-api.service';
import { StorageLocationsComponent } from './storage-locations.component';
import { StorageLocationsFacade } from './storage-locations.facade';

describe('StorageLocationsComponent', () => {
  let fixture: ComponentFixture<StorageLocationsComponent>;
  let facade: StorageLocationsFacade;
  let storageLocationsApiMock: {
    getStorageLocations: ReturnType<typeof vi.fn>;
    getStorageLocationStockOverview: ReturnType<typeof vi.fn>;
  };

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

  beforeEach(async () => {
    storageLocationsApiMock = {
      getStorageLocations: vi.fn(),
      getStorageLocationStockOverview: vi.fn(),
    };

    await TestBed.configureTestingModule({
      imports: [StorageLocationsComponent],
      providers: [
        {
          provide: StorageLocationsApiService,
          useValue: storageLocationsApiMock,
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(StorageLocationsComponent);
    facade = fixture.debugElement.injector.get(StorageLocationsFacade);
  });

  it('should load storage locations on init', () => {
    const locations = [
      createLocation({ id: 1, code: 'A-01-01', active: true }),
      createLocation({ id: 2, code: 'B-02-01', zone: 'ZONE-B', active: false }),
    ];

    storageLocationsApiMock.getStorageLocations.mockReturnValue(of(locations));

    fixture.detectChanges();

    expect(storageLocationsApiMock.getStorageLocations).toHaveBeenCalledTimes(1);
    expect(facade.loadingLocations).toBe(false);
    expect(facade.errorMessage).toBe('');
    expect(facade.locations).toEqual(locations);
    expect(facade.filteredLocations).toEqual(locations);
    expect(facade.totalCount).toBe(2);
    expect(facade.activeCount).toBe(1);
    expect(facade.inactiveCount).toBe(1);
  });

  it('should set loading flag during location request and update locations after success', () => {
    const locations$ = new Subject<StorageLocationResponse[]>();
    const locations = [createLocation()];

    storageLocationsApiMock.getStorageLocations.mockReturnValue(locations$.asObservable());

    fixture.detectChanges();

    expect(facade.loadingLocations).toBe(true);

    locations$.next(locations);
    locations$.complete();

    expect(facade.loadingLocations).toBe(false);
    expect(facade.locations).toEqual(locations);
    expect(facade.filteredLocations).toEqual(locations);
  });

  it('should filter locations by code, zone, and description', () => {
    const locations = [
      createLocation({
        id: 1,
        code: 'A-01-01',
        zone: 'ZONE-A',
        description: 'Main demo storage location',
      }),
      createLocation({
        id: 2,
        code: 'B-02-01',
        zone: 'ZONE-B',
        description: 'Overflow picking area',
      }),
      createLocation({
        id: 3,
        code: 'C-03-01',
        zone: 'COLD-STORAGE',
        description: 'Temperature controlled goods',
      }),
    ];

    storageLocationsApiMock.getStorageLocations.mockReturnValue(of(locations));

    fixture.detectChanges();

    facade.searchTerm = 'overflow';
    facade.applySearch();

    expect(facade.filteredLocations).toEqual([locations[1]]);

    facade.searchTerm = 'cold';
    facade.applySearch();

    expect(facade.filteredLocations).toEqual([locations[2]]);

    facade.searchTerm = 'A-01';
    facade.applySearch();

    expect(facade.filteredLocations).toEqual([locations[0]]);
  });

  it('should reset filtered locations when search term is blank', () => {
    const locations = [
      createLocation({ id: 1, code: 'A-01-01' }),
      createLocation({ id: 2, code: 'B-02-01' }),
    ];

    storageLocationsApiMock.getStorageLocations.mockReturnValue(of(locations));

    fixture.detectChanges();

    facade.searchTerm = 'B-02';
    facade.applySearch();
    expect(facade.filteredLocations).toEqual([locations[1]]);

    facade.searchTerm = '   ';
    facade.applySearch();
    expect(facade.filteredLocations).toEqual(locations);
  });

  it('should select a location and load its stock overview', () => {
    const location = createLocation({ id: 1 });
    const overview = createOverview({ id: 1 });

    storageLocationsApiMock.getStorageLocations.mockReturnValue(of([location]));
    storageLocationsApiMock.getStorageLocationStockOverview.mockReturnValue(of(overview));

    fixture.detectChanges();

    facade.selectLocation(location);

    expect(storageLocationsApiMock.getStorageLocationStockOverview).toHaveBeenCalledTimes(1);
    expect(storageLocationsApiMock.getStorageLocationStockOverview).toHaveBeenCalledWith(1);
    expect(facade.selectedLocation).toEqual(location);
    expect(facade.selectedOverview).toEqual(overview);
    expect(facade.loadingOverview).toBe(false);
  });

  it('should refresh overview only when a location is selected', () => {
    const location = createLocation({ id: 1 });
    const overview = createOverview({ id: 1 });

    storageLocationsApiMock.getStorageLocations.mockReturnValue(of([location]));
    storageLocationsApiMock.getStorageLocationStockOverview.mockReturnValue(of(overview));

    fixture.detectChanges();

    facade.refreshOverview();
    expect(storageLocationsApiMock.getStorageLocationStockOverview).not.toHaveBeenCalled();

    facade.selectLocation(location);
    storageLocationsApiMock.getStorageLocationStockOverview.mockClear();

    facade.refreshOverview();

    expect(storageLocationsApiMock.getStorageLocationStockOverview).toHaveBeenCalledTimes(1);
    expect(storageLocationsApiMock.getStorageLocationStockOverview).toHaveBeenCalledWith(1);
  });

  it('should clear selected location if it no longer exists after reloading locations', () => {
    const selectedLocation = createLocation({ id: 1, code: 'A-01-01' });
    const remainingLocation = createLocation({ id: 2, code: 'B-02-01' });

    storageLocationsApiMock.getStorageLocations.mockReturnValue(of([selectedLocation]));
    storageLocationsApiMock.getStorageLocationStockOverview.mockReturnValue(
      of(createOverview({ id: 1 })),
    );

    fixture.detectChanges();

    facade.selectLocation(selectedLocation);
    expect(facade.selectedLocation).toEqual(selectedLocation);
    expect(facade.selectedOverview).not.toBeNull();

    storageLocationsApiMock.getStorageLocations.mockReturnValue(of([remainingLocation]));

    facade.loadLocations();

    expect(facade.selectedLocation).toBeNull();
    expect(facade.selectedOverview).toBeNull();
  });

  it('should set error state when loading locations fails', () => {
    storageLocationsApiMock.getStorageLocations.mockReturnValue(
      throwError(() => new Error('Could not load storage locations.')),
    );

    fixture.detectChanges();

    expect(facade.loadingLocations).toBe(false);
    expect(facade.errorMessage).toBe('Could not load storage locations.');
    expect(facade.locations).toEqual([]);
    expect(facade.filteredLocations).toEqual([]);
    expect(facade.selectedLocation).toBeNull();
    expect(facade.selectedOverview).toBeNull();
  });

  it('should set error state when loading overview fails', () => {
    const location = createLocation({ id: 1 });

    storageLocationsApiMock.getStorageLocations.mockReturnValue(of([location]));
    storageLocationsApiMock.getStorageLocationStockOverview.mockReturnValue(
      throwError(() => new Error('Could not load storage location overview.')),
    );

    fixture.detectChanges();

    facade.selectLocation(location);

    expect(facade.loadingOverview).toBe(false);
    expect(facade.errorMessage).toBe('Could not load storage location overview.');
    expect(facade.selectedOverview).toBeNull();
  });

  it('should render storage locations returned by the API', () => {
    const locations = [
      createLocation({
        id: 1,
        code: 'A-01-01',
        zone: 'ZONE-A',
        description: 'Main demo storage location',
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

    storageLocationsApiMock.getStorageLocations.mockReturnValue(of(locations));

    fixture.detectChanges();

    const cards = fixture.debugElement.queryAll(By.css('.location-card'));
    expect(cards.length).toBe(2);

    const text = fixture.nativeElement.textContent;
    expect(text).toContain('A-01-01');
    expect(text).toContain('ZONE-A');
    expect(text).toContain('Main demo storage location');
    expect(text).toContain('B-02-01');
    expect(text).toContain('ZONE-B');
    expect(text).toContain('Overflow picking area');
  });

  it('should render empty search state when no locations match', async () => {
    const locations = [createLocation({ code: 'A-01-01' })];

    storageLocationsApiMock.getStorageLocations.mockReturnValue(of(locations));

    fixture.detectChanges();
    await fixture.whenStable();

    const searchInput: HTMLInputElement = fixture.nativeElement.querySelector('#location-search');

    searchInput.value = 'missing';
    searchInput.dispatchEvent(new Event('input'));

    fixture.detectChanges();
    await fixture.whenStable();

    const text = fixture.nativeElement.textContent;
    expect(text).toContain('No storage locations match the current search.');
  });

  it('should render selected overview and assigned products', () => {
    const location = createLocation({ id: 1, code: 'A-01-01' });
    const overview = createOverview();

    storageLocationsApiMock.getStorageLocations.mockReturnValue(of([location]));
    storageLocationsApiMock.getStorageLocationStockOverview.mockReturnValue(of(overview));

    fixture.detectChanges();

    const locationButton: HTMLButtonElement = fixture.nativeElement.querySelector('.location-card');

    locationButton.click();
    fixture.detectChanges();

    const rows = fixture.debugElement.queryAll(By.css('tbody tr'));
    expect(rows.length).toBe(2);

    const text = fixture.nativeElement.textContent;
    expect(text).toContain('A-01-01');
    expect(text).toContain('ZONE-A');
    expect(text).toContain('2 product(s) assigned to this');
    expect(text).toContain('Industrial Storage Bin');
    expect(text).toContain('Packing Tape Roll');
    expect(text).toContain('Yes');
    expect(text).toContain('No');
  });

  it('should render prompt when no location is selected', () => {
    storageLocationsApiMock.getStorageLocations.mockReturnValue(of([]));

    fixture.detectChanges();

    const text = fixture.nativeElement.textContent;
    expect(text).toContain(
      'Select a storage location from the directory to view its stock overview.',
    );
  });

  it('should trigger refresh locations from the Refresh locations button', () => {
    storageLocationsApiMock.getStorageLocations.mockReturnValue(of([]));

    fixture.detectChanges();

    storageLocationsApiMock.getStorageLocations.mockClear();

    const refreshLocationsButton: HTMLButtonElement = fixture.nativeElement.querySelector(
      '.page-hero__actions .button--ghost',
    );

    refreshLocationsButton.click();

    expect(storageLocationsApiMock.getStorageLocations).toHaveBeenCalledTimes(1);
  });

  it('should trigger refresh overview from the Refresh overview button when enabled', async () => {
    const location = createLocation({ id: 1 });
    const overview = createOverview({ id: 1 });

    storageLocationsApiMock.getStorageLocations.mockReturnValue(of([location]));
    storageLocationsApiMock.getStorageLocationStockOverview.mockReturnValue(of(overview));

    fixture.detectChanges();
    await fixture.whenStable();

    const locationButton: HTMLButtonElement =
      fixture.nativeElement.querySelector('.location-card');

    locationButton.click();

    fixture.detectChanges();
    await fixture.whenStable();

    storageLocationsApiMock.getStorageLocationStockOverview.mockClear();

    const refreshOverviewButton: HTMLButtonElement =
      fixture.nativeElement.querySelector('.page-hero__actions .button--primary');

    expect(refreshOverviewButton.disabled).toBe(false);

    refreshOverviewButton.click();

    expect(storageLocationsApiMock.getStorageLocationStockOverview).toHaveBeenCalledTimes(1);
    expect(storageLocationsApiMock.getStorageLocationStockOverview).toHaveBeenCalledWith(1);
  });
});
