import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { catchError, finalize, of, tap } from 'rxjs';
import { StorageLocationsApiService } from '../../core/api/storage-locations-api.service';
import {
  StorageLocationResponse,
  StorageLocationStockOverviewResponse,
} from '../../core/api/api.types';

@Component({
  selector: 'app-storage-locations',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './storage-locations.component.html',
  styleUrls: ['./storage-locations.component.scss'],
})
export class StorageLocationsComponent implements OnInit {
  private readonly storageLocationsApi = inject(StorageLocationsApiService);
  private readonly cdr = inject(ChangeDetectorRef);

  protected loadingLocations = false;
  protected loadingOverview = false;
  protected errorMessage = '';

  protected locations: StorageLocationResponse[] = [];
  protected filteredLocations: StorageLocationResponse[] = [];
  protected selectedLocation: StorageLocationResponse | null = null;
  protected selectedOverview: StorageLocationStockOverviewResponse | null = null;

  protected searchTerm = '';

  ngOnInit(): void {
    this.loadLocations();
  }

  protected loadLocations(): void {
    this.loadingLocations = true;
    this.errorMessage = '';

    this.storageLocationsApi
      .getStorageLocations()
      .pipe(
        tap((locations) => {
          this.locations = locations;
          this.applySearch();

          if (
            this.selectedLocation &&
            !locations.some((location) => location.id === this.selectedLocation?.id)
          ) {
            this.selectedLocation = null;
            this.selectedOverview = null;
          }
        }),
        catchError((error) => {
          this.errorMessage = error?.message || 'Could not load storage locations.';
          return of([]);
        }),
        finalize(() => {
          this.loadingLocations = false;
          this.cdr.detectChanges();
        }),
      )
      .subscribe();
  }
  protected selectLocation(location: StorageLocationResponse): void {
    this.selectedLocation = location;
    this.loadOverview(location.id);
  }

  protected refreshOverview(): void {
    if (!this.selectedLocation) {
      return;
    }

    this.loadOverview(this.selectedLocation.id);
  }

  protected applySearch(): void {
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

  protected get totalCount(): number {
    return this.locations.length;
  }

  protected get activeCount(): number {
    return this.locations.filter((location) => location.active).length;
  }

  protected get inactiveCount(): number {
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
          this.loadingOverview = false;
        }),
        catchError((error) => {
          this.errorMessage = error?.message || 'Could not load storage locations.';
          return of([]);
        }),
        finalize(() => {
          this.loadingOverview = false;
          this.cdr.detectChanges();
        }),
      )
      .subscribe();
  }
}
