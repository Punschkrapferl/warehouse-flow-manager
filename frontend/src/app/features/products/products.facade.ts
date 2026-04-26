import { HttpErrorResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { catchError, finalize, of, tap } from 'rxjs';
import { PagedResponse, ProductResponse, ProductStatus } from '../../core/api/api.types';
import { ProductQuery, ProductsApiService } from '../../core/api/products-api.service';

export type ProductStatusFilter = ProductStatus | 'ALL';

export interface ProductFilters {
  search: string;
  status: ProductStatusFilter;
  page: number;
  size: number;
  sortBy: string;
  direction: 'asc' | 'desc';
}

const DEFAULT_FILTERS: ProductFilters = {
  search: '',
  status: 'ALL',
  page: 0,
  size: 10,
  sortBy: 'id',
  direction: 'asc',
};

const EMPTY_PRODUCT_PAGE: PagedResponse<ProductResponse> = {
  page: 0,
  size: 10,
  totalElements: 0,
  totalPages: 0,
  first: true,
  last: true,
  content: [],
};

@Injectable()
export class ProductsFacade {
  private readonly productsApi = inject(ProductsApiService);

  readonly statuses: ProductStatusFilter[] = ['ALL', 'ACTIVE', 'BLOCKED', 'DISCONTINUED'];

  loading = true;
  errorMessage = '';

  filters: ProductFilters = { ...DEFAULT_FILTERS };
  pageData: PagedResponse<ProductResponse> = { ...EMPTY_PRODUCT_PAGE };

  loadProducts(): void {
    this.loading = true;
    this.errorMessage = '';

    this.productsApi
      .getProducts(this.buildQuery())
      .pipe(
        tap((pageData) => {
          this.pageData = pageData;
        }),
        catchError((error) => {
          this.errorMessage = this.buildErrorMessage(error);
          this.pageData = {
            ...EMPTY_PRODUCT_PAGE,
            page: this.filters.page,
            size: this.filters.size,
          };

          return of(this.pageData);
        }),
        finalize(() => {
          this.loading = false;
        }),
      )
      .subscribe();
  }

  applyFilters(): void {
    this.filters.page = 0;
    this.loadProducts();
  }

  resetFilters(): void {
    this.filters = { ...DEFAULT_FILTERS };
    this.loadProducts();
  }

  previousPage(): void {
    if (this.pageData.first) {
      return;
    }

    this.filters.page -= 1;
    this.loadProducts();
  }

  nextPage(): void {
    if (this.pageData.last) {
      return;
    }

    this.filters.page += 1;
    this.loadProducts();
  }

  pageSizeChanged(value: number | string): void {
    this.filters.size = Number(value);
    this.filters.page = 0;
    this.loadProducts();
  }

  private buildQuery(): ProductQuery {
    return {
      page: this.filters.page,
      size: this.filters.size,
      sortBy: this.filters.sortBy,
      direction: this.filters.direction,
      search: this.filters.search.trim() || undefined,
      status: this.filters.status === 'ALL' ? undefined : this.filters.status,
    };
  }

  private buildErrorMessage(error: unknown): string {
    if (error instanceof HttpErrorResponse) {
      return `Could not load products (${error.status}). ${error.message}`;
    }

    return 'Could not load products. Check whether the Spring Boot backend is running.';
  }
}
