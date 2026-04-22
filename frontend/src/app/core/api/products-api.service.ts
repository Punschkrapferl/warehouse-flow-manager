import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { PagedResponse, ProductResponse, ProductStatus } from './api.types';

export interface ProductQuery {
  search?: string;
  status?: ProductStatus;
  page: number;
  size: number;
  sortBy: string;
  direction: 'asc' | 'desc';
}

@Injectable({
  providedIn: 'root',
})
export class ProductsApiService {
  private readonly http = inject(HttpClient);

  getProducts(query: ProductQuery): Observable<PagedResponse<ProductResponse>> {
    let params = new HttpParams()
      .set('page', String(query.page))
      .set('size', String(query.size))
      .set('sortBy', query.sortBy)
      .set('direction', query.direction);

    if (query.search && query.search.trim().length > 0) {
      params = params.set('search', query.search.trim());
    }

    if (query.status) {
      params = params.set('status', query.status);
    }

    return this.http.get<PagedResponse<ProductResponse>>('/api/products', { params });
  }
}
