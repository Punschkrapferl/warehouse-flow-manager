import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { catchError, finalize, of, tap } from 'rxjs';
import { FormsModule } from '@angular/forms';
import { ProductResponse, ProductStatus, PagedResponse } from '../../core/api/api.types';
import { ProductQuery, ProductsApiService } from '../../core/api/products-api.service';

type ProductStatusFilter = ProductStatus | 'ALL';

@Component({
  selector: 'app-products',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './products.component.html',
  styleUrl: './products.component.scss',
})
export class ProductsComponent implements OnInit {
  private readonly productsApi = inject(ProductsApiService);
  private readonly cdr = inject(ChangeDetectorRef);

  protected readonly statuses: ProductStatusFilter[] = ['ALL', 'ACTIVE', 'BLOCKED', 'DISCONTINUED'];

  protected loading = true;
  protected errorMessage = '';

  protected filters: {
    search: string;
    status: ProductStatusFilter;
    page: number;
    size: number;
    sortBy: string;
    direction: 'asc' | 'desc';
  } = {
    search: '',
    status: 'ALL',
    page: 0,
    size: 10,
    sortBy: 'id',
    direction: 'asc',
  };

  protected pageData: PagedResponse<ProductResponse> = {
    page: 0,
    size: 10,
    totalElements: 0,
    totalPages: 0,
    first: true,
    last: true,
    content: [],
  };

  ngOnInit(): void {
    this.loadProducts();
  }

  protected loadProducts(): void {
    this.loading = true;
    this.errorMessage = '';

    const query: ProductQuery = {
      page: this.filters.page,
      size: this.filters.size,
      sortBy: this.filters.sortBy,
      direction: this.filters.direction,
      search: this.filters.search.trim() || undefined,
      status: this.filters.status === 'ALL' ? undefined : this.filters.status,
    };

    this.productsApi.getProducts(query).pipe(
      tap((pageData) => {
        this.pageData = pageData;
        this.loading = false;
      }),
      catchError((error) => {
        this.errorMessage = this.buildErrorMessage(error);
        return of([]);
      }),
      finalize(() => {
        this.loading = false;
        this.cdr.detectChanges();
      }),
    ).subscribe();
  }

  protected applyFilters(): void {
    this.filters.page = 0;
    this.loadProducts();
  }

  protected resetFilters(): void {
    this.filters = {
      search: '',
      status: 'ALL',
      page: 0,
      size: 10,
      sortBy: 'id',
      direction: 'asc',
    };

    this.loadProducts();
  }

  protected previousPage(): void {
    if (!this.pageData.first) {
      this.filters.page -= 1;
      this.loadProducts();
    }
  }

  protected nextPage(): void {
    if (!this.pageData.last) {
      this.filters.page += 1;
      this.loadProducts();
    }
  }

  protected pageSizeChanged(value: number | string): void {
    this.filters.size = Number(value);
    this.filters.page = 0;
    this.loadProducts();
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

  private buildErrorMessage(error: unknown): string {
    if (error instanceof HttpErrorResponse) {
      return `Could not load products (${error.status}). ${error.message}`;
    }

    return 'Could not load products. Check whether the Spring Boot backend is running.';
  }
}
