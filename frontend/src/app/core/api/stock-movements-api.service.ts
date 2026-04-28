import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import {
  StockMovementResponse,
  StockMovementSummaryResponse,
  StockMovementType,
} from './api.types';
import { ApiPaths } from './api-paths';

export interface StockMovementQuery {
  productId?: number;
  movementType?: StockMovementType;
  from?: string;
  to?: string;
}

@Injectable({
  providedIn: 'root',
})
export class StockMovementsApiService {
  private readonly http = inject(HttpClient);

  getStockMovements(query: StockMovementQuery): Observable<StockMovementResponse[]> {
    let params = new HttpParams();

    if (query.productId !== undefined) {
      params = params.set('productId', String(query.productId));
    }

    if (query.movementType) {
      params = params.set('movementType', query.movementType);
    }

    if (query.from) {
      params = params.set('from', query.from);
    }

    if (query.to) {
      params = params.set('to', query.to);
    }

    return this.http.get<StockMovementResponse[]>(ApiPaths.stockMovements, { params });
  }

  getStockMovementSummary(
    query: Omit<StockMovementQuery, 'movementType'>,
  ): Observable<StockMovementSummaryResponse> {
    let params = new HttpParams();

    if (query.productId !== undefined) {
      params = params.set('productId', String(query.productId));
    }

    if (query.from) {
      params = params.set('from', query.from);
    }

    if (query.to) {
      params = params.set('to', query.to);
    }

    // The summary endpoint aggregates all movement types, so movementType is intentionally omitted.
    return this.http.get<StockMovementSummaryResponse>(ApiPaths.stockMovementSummary, {
      params,
    });
  }
}
