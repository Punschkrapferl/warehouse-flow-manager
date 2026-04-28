import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { map, Observable } from 'rxjs';
import {
  HealthResponse,
  ProductResponse,
  ReplenishmentRecommendationResponse,
  StockMovementResponse,
} from './api.types';
import { ApiPaths } from './api-paths';

@Injectable({
  providedIn: 'root',
})
export class DashboardApiService {
  private readonly http = inject(HttpClient);

  getHealth(): Observable<HealthResponse> {
    return this.http.get<HealthResponse>(ApiPaths.health);
  }

  getLowStockProducts(): Observable<ProductResponse[]> {
    return this.http.get<ProductResponse[]>(ApiPaths.lowStockProducts);
  }

  getReplenishmentCandidates(
    recentDays: number,
  ): Observable<ReplenishmentRecommendationResponse[]> {
    return this.http.get<ReplenishmentRecommendationResponse[]>(ApiPaths.replenishmentCandidates, {
      params: {
        recentDays,
      },
    });
  }

  getRecentStockMovements(limit: number): Observable<StockMovementResponse[]> {
    // The backend returns newest movements first; the dashboard only needs a compact preview.
    return this.http
      .get<StockMovementResponse[]>(ApiPaths.stockMovements)
      .pipe(map((movements) => movements.slice(0, limit)));
  }
}
