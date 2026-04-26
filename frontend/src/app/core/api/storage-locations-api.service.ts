import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { StorageLocationResponse, StorageLocationStockOverviewResponse } from './api.types';
import { ApiPaths, storageLocationStockOverviewPath } from './api-paths';

@Injectable({
  providedIn: 'root',
})
export class StorageLocationsApiService {
  private readonly http = inject(HttpClient);

  getStorageLocations(): Observable<StorageLocationResponse[]> {
    return this.http.get<StorageLocationResponse[]>(ApiPaths.storageLocations);
  }

  getStorageLocationStockOverview(id: number): Observable<StorageLocationStockOverviewResponse> {
    return this.http.get<StorageLocationStockOverviewResponse>(
      storageLocationStockOverviewPath(id),
    );
  }
}
