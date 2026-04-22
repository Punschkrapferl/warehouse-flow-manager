import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { map, Observable } from 'rxjs';
import {
  HealthResponse,
  ProductResponse,
  ReplenishmentRecommendationResponse,
  StockMovementResponse,
} from './api.types';

@Injectable({
  providedIn: 'root',
})
export class DashboardApiService {
  private readonly http = inject(HttpClient);

  getHealth(): Observable<HealthResponse> {
    return this.http.get<HealthResponse>('/api/v1/health');
  }

  getLowStockProducts(): Observable<ProductResponse[]> {
    return this.http.get<ProductResponse[]>('/api/products/low-stock');
  }

  getReplenishmentCandidates(
    recentDays: number,
  ): Observable<ReplenishmentRecommendationResponse[]> {
    return this.http.get<ReplenishmentRecommendationResponse[]>(
      '/api/products/replenishment-candidates',
      {
        params: {
          recentDays,
        },
      },
    );
  }

  getRecentStockMovements(limit: number): Observable<StockMovementResponse[]> {
    return this.http
      .get<StockMovementResponse[]>('/api/stock-movements')
      .pipe(map((movements) => movements.slice(0, limit)));
  }
}
