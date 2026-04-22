import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { StorageLocationResponse, StorageLocationStockOverviewResponse } from './api.types';

@Injectable({
  providedIn: 'root',
})
export class StorageLocationsApiService {
  private readonly http = inject(HttpClient);

  getStorageLocations(): Observable<StorageLocationResponse[]> {
    return this.http.get<StorageLocationResponse[]>('/api/storage-locations');
  }

  getStorageLocationStockOverview(id: number): Observable<StorageLocationStockOverviewResponse> {
    return this.http.get<StorageLocationStockOverviewResponse>(
      `/api/storage-locations/${id}/stock-overview`,
    );
  }
}
