import { Injectable, inject } from '@angular/core';
import { catchError, finalize, of, tap } from 'rxjs';
import {
  StorageLocationResponse,
  StorageLocationStockOverviewResponse,
} from '../../core/api/api.types';
import { StorageLocationsApiService } from '../../core/api/storage-locations-api.service';
import { ApiErrorMessageService } from '../../core/error/api-error-message.service';

@Injectable()
export class StorageLocationsFacade {
  private readonly storageLocationsApi = inject(StorageLocationsApiService);
  private readonly apiErrorMessage = inject(ApiErrorMessageService);

  loadingLocations = false;
  loadingOverview = false;
  errorMessage = '';

  locations: StorageLocationResponse[] = [];
  filteredLocations: StorageLocationResponse[] = [];
  selectedLocation: StorageLocationResponse | null = null;
  selectedOverview: StorageLocationStockOverviewResponse | null = null;

  searchTerm = '';

  loadLocations(): void {
    this.loadingLocations = true;
    this.errorMessage = '';

    this.storageLocationsApi
      .getStorageLocations()
      .pipe(
        tap((locations) => {
          this.locations = locations;
          this.applySearch();
          this.clearSelectionIfLocationNoLongerExists(locations);
        }),
        catchError((error) => {
          this.errorMessage = this.apiErrorMessage.toMessage(
            error,
            'Could not load storage locations.',
          );
          this.locations = [];
          this.filteredLocations = [];
          this.selectedLocation = null;
          this.selectedOverview = null;

          return of([]);
        }),
        finalize(() => {
          this.loadingLocations = false;
        }),
      )
      .subscribe();
  }

  selectLocation(location: StorageLocationResponse): void {
    this.selectedLocation = location;
    this.loadOverview(location.id);
  }

  refreshOverview(): void {
    if (!this.selectedLocation) {
      return;
    }

    this.loadOverview(this.selectedLocation.id);
  }

  applySearch(): void {
    const term = this.searchTerm.trim().toLowerCase();

    if (!term) {
      this.filteredLocations = [...this.locations];
      return;
    }

    this.filteredLocations = this.locations.filter((location) => {
      return (
        location.code.toLowerCase().includes(term) ||
        location.zone.toLowerCase().includes(term) ||
        (location.description ?? '').toLowerCase().includes(term)
      );
    });
  }

  get totalCount(): number {
    return this.locations.length;
  }

  get activeCount(): number {
    return this.locations.filter((location) => location.active).length;
  }

  get inactiveCount(): number {
    return this.locations.filter((location) => !location.active).length;
  }

  private loadOverview(locationId: number): void {
    this.loadingOverview = true;
    this.errorMessage = '';

    this.storageLocationsApi
      .getStorageLocationStockOverview(locationId)
      .pipe(
        tap((overview) => {
          this.selectedOverview = overview;
        }),
        catchError((error) => {
          this.errorMessage = this.apiErrorMessage.toMessage(
            error,
            'Could not load storage location overview.',
          );
          this.selectedOverview = null;

          return of(null);
        }),
        finalize(() => {
          this.loadingOverview = false;
        }),
      )
      .subscribe();
  }

  private clearSelectionIfLocationNoLongerExists(locations: StorageLocationResponse[]): void {
    if (!this.selectedLocation) {
      return;
    }

    const selectedLocationStillExists = locations.some(
      (location) => location.id === this.selectedLocation?.id,
    );

    if (!selectedLocationStillExists) {
      this.selectedLocation = null;
      this.selectedOverview = null;
    }
  }
}
