import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { StorageLocationsApiService } from '../../core/api/storage-locations-api.service';
import {
  StorageLocationResponse,
  StorageLocationStockOverviewResponse,
} from '../../core/api/api.types';

@Component({
  selector: 'app-storage-locations',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './storage-locations.component.html',
  styleUrls: ['./storage-locations.component.scss'],
})
export class StorageLocationsComponent implements OnInit {
  private readonly storageLocationsApi = inject(StorageLocationsApiService);

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

    this.storageLocationsApi.getStorageLocations().subscribe({
      next: (locations) => {
        this.locations = locations;
        this.applySearch();
        this.loadingLocations = false;

        if (
          this.selectedLocation &&
          !this.locations.some((location) => location.id === this.selectedLocation?.id)
        ) {
          this.selectedLocation = null;
          this.selectedOverview = null;
        }
      },
      error: (error) => {
        this.errorMessage = error?.message || 'Could not load storage locations.';
        this.loadingLocations = false;
      },
    });
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

  protected trackByLocationId(_: number, location: StorageLocationResponse): number {
    return location.id;
  }

  private loadOverview(locationId: number): void {
    this.loadingOverview = true;
    this.errorMessage = '';

    this.storageLocationsApi.getStorageLocationStockOverview(locationId).subscribe({
      next: (overview) => {
        this.selectedOverview = overview;
        this.loadingOverview = false;
      },
      error: (error) => {
        this.errorMessage = error?.message || 'Could not load storage location overview.';
        this.loadingOverview = false;
      },
    });
  }
}
