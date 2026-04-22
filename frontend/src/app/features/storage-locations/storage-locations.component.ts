import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import {
  ProductStatus,
  StorageLocationResponse,
  StorageLocationStockItemResponse,
  StorageLocationStockOverviewResponse,
} from '../../core/api/api.types';
import { StorageLocationsApiService } from '../../core/api/storage-locations-api.service';

@Component({
  selector: 'app-storage-locations',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './storage-locations.component.html',
  styleUrl: './storage-locations.component.scss',
})
export class StorageLocationsComponent implements OnInit {
  private readonly storageLocationsApi = inject(StorageLocationsApiService);

  protected loadingLocations = true;
  protected loadingOverview = false;
  protected errorMessage = '';

  protected searchTerm = '';
  protected storageLocations: StorageLocationResponse[] = [];
  protected selectedLocationId: number | null = null;
  protected selectedOverview: StorageLocationStockOverviewResponse | null = null;

  ngOnInit(): void {
    this.loadStorageLocations();
  }

  protected loadStorageLocations(): void {
    this.loadingLocations = true;
    this.errorMessage = '';

    const previousSelectedId = this.selectedLocationId;

    this.storageLocationsApi.getStorageLocations().subscribe({
      next: (locations) => {
        this.storageLocations = [...locations].sort((left, right) => {
          if (left.active !== right.active) {
            return left.active ? -1 : 1;
          }

          return left.code.localeCompare(right.code);
        });

        this.loadingLocations = false;

        if (this.storageLocations.length === 0) {
          this.selectedLocationId = null;
          this.selectedOverview = null;
          return;
        }

        const selectedId =
          previousSelectedId &&
          this.storageLocations.some((location) => location.id === previousSelectedId)
            ? previousSelectedId
            : (this.storageLocations.find((location) => location.active)?.id ??
              this.storageLocations[0].id);

        this.loadStorageLocationOverview(selectedId);
      },
      error: (error: unknown) => {
        this.errorMessage = this.buildErrorMessage(error, 'Could not load storage locations.');
        this.loadingLocations = false;
      },
    });
  }

  protected refreshSelectedOverview(): void {
    if (this.selectedLocationId !== null) {
      this.loadStorageLocationOverview(this.selectedLocationId);
    }
  }

  protected selectLocation(locationId: number): void {
    if (
      this.selectedLocationId === locationId &&
      this.selectedOverview !== null &&
      !this.loadingOverview
    ) {
      return;
    }

    this.loadStorageLocationOverview(locationId);
  }

  protected get filteredLocations(): StorageLocationResponse[] {
    const search = this.searchTerm.trim().toLowerCase();

    if (!search) {
      return this.storageLocations;
    }

    return this.storageLocations.filter((location) => {
      const code = location.code.toLowerCase();
      const zone = location.zone.toLowerCase();
      const description = (location.description ?? '').toLowerCase();

      return code.includes(search) || zone.includes(search) || description.includes(search);
    });
  }

  protected get activeLocationCount(): number {
    return this.storageLocations.filter((location) => location.active).length;
  }

  protected get inactiveLocationCount(): number {
    return this.storageLocations.filter((location) => !location.active).length;
  }

  protected get selectedLocationLabel(): string {
    if (!this.selectedOverview) {
      return 'No location selected';
    }

    return `${this.selectedOverview.code} · ${this.selectedOverview.zone}`;
  }

  protected locationStatusClass(active: boolean): string {
    return active ? 'badge badge--success' : 'badge badge--neutral';
  }

  protected statusClass(status: ProductStatus): string {
    switch (status) {
      case 'ACTIVE':
        return 'badge badge--success';
      case 'BLOCKED':
        return 'badge badge--warning';
      case 'DISCONTINUED':
        return 'badge badge--neutral';
      default:
        return 'badge badge--neutral';
    }
  }

  protected quantityClass(item: StorageLocationStockItemResponse): string {
    if (item.quantity === 0) {
      return 'metric-pill metric-pill--critical';
    }

    if (item.lowStock) {
      return 'metric-pill metric-pill--warning';
    }

    return 'metric-pill metric-pill--success';
  }

  protected stockGap(item: StorageLocationStockItemResponse): number {
    return item.quantity < item.minimumQuantity ? item.minimumQuantity - item.quantity : 0;
  }

  private loadStorageLocationOverview(locationId: number): void {
    this.selectedLocationId = locationId;
    this.loadingOverview = true;
    this.errorMessage = '';

    this.storageLocationsApi.getStorageLocationStockOverview(locationId).subscribe({
      next: (overview) => {
        this.selectedOverview = {
          ...overview,
          products: [...overview.products].sort((left, right) => {
            if (left.lowStock !== right.lowStock) {
              return left.lowStock ? -1 : 1;
            }

            return left.sku.localeCompare(right.sku);
          }),
        };
        this.loadingOverview = false;
      },
      error: (error: unknown) => {
        this.errorMessage = this.buildErrorMessage(
          error,
          'Could not load storage location overview.',
        );
        this.loadingOverview = false;
      },
    });
  }

  private buildErrorMessage(error: unknown, fallback: string): string {
    if (error instanceof HttpErrorResponse) {
      return `${fallback} (${error.status}) ${error.message}`;
    }

    return `${fallback} Check whether the Spring Boot backend is running.`;
  }
}
