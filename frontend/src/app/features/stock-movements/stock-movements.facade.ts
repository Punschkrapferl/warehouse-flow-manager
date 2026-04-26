import { Injectable, inject } from '@angular/core';
import { catchError, finalize, forkJoin, of, tap } from 'rxjs';
import {
  StockMovementResponse,
  StockMovementSummaryResponse,
  StockMovementType,
} from '../../core/api/api.types';
import {
  StockMovementQuery,
  StockMovementsApiService,
} from '../../core/api/stock-movements-api.service';

export type MovementTypeFilter = 'ALL' | StockMovementType;

export interface StockMovementFilters {
  productId: string;
  movementType: MovementTypeFilter;
  from: string;
  to: string;
}

const DEFAULT_FILTERS: StockMovementFilters = {
  productId: '',
  movementType: 'ALL',
  from: '',
  to: '',
};

@Injectable()
export class StockMovementsFacade {
  private readonly stockMovementsApi = inject(StockMovementsApiService);

  readonly movementTypes: MovementTypeFilter[] = ['ALL', 'INBOUND', 'OUTBOUND', 'ADJUSTMENT'];

  loadingMovements = false;
  loadingSummary = false;
  errorMessage = '';

  movements: StockMovementResponse[] = [];
  summary: StockMovementSummaryResponse | null = null;

  filters: StockMovementFilters = { ...DEFAULT_FILTERS };

  loadData(): void {
    this.loadingMovements = true;
    this.loadingSummary = true;
    this.errorMessage = '';

    const query = this.buildMovementQuery();
    const summaryQuery = this.buildSummaryQuery(query);

    forkJoin({
      movements: this.stockMovementsApi.getStockMovements(query),
      summary: this.stockMovementsApi.getStockMovementSummary(summaryQuery),
    })
      .pipe(
        tap((result) => {
          this.movements = result.movements;
          this.summary = result.summary;
        }),
        catchError((error) => {
          this.errorMessage = this.buildErrorMessage(error);
          this.movements = [];
          this.summary = null;

          return of({
            movements: this.movements,
            summary: this.summary,
          });
        }),
        finalize(() => {
          this.loadingMovements = false;
          this.loadingSummary = false;
        }),
      )
      .subscribe();
  }

  refreshData(): void {
    this.loadData();
  }

  applyFilters(): void {
    this.loadData();
  }

  resetFilters(): void {
    this.filters = { ...DEFAULT_FILTERS };
    this.loadData();
  }

  private buildMovementQuery(): StockMovementQuery {
    return {
      productId: this.parseProductId(this.filters.productId),
      movementType: this.filters.movementType === 'ALL' ? undefined : this.filters.movementType,
      from: this.toIsoInstant(this.filters.from),
      to: this.toIsoInstant(this.filters.to),
    };
  }

  private buildSummaryQuery(query: StockMovementQuery): Omit<StockMovementQuery, 'movementType'> {
    return {
      productId: query.productId,
      from: query.from,
      to: query.to,
    };
  }

  private parseProductId(value: string): number | undefined {
    const trimmed = value.trim();

    if (!trimmed) {
      return undefined;
    }

    const parsed = Number(trimmed);
    return Number.isFinite(parsed) && parsed > 0 ? parsed : undefined;
  }

  private toIsoInstant(value: string): string | undefined {
    if (!value) {
      return undefined;
    }

    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? undefined : date.toISOString();
  }

  private buildErrorMessage(error: unknown): string {
    if (error instanceof Error && error.message) {
      return error.message;
    }

    return 'Could not load stock movement data.';
  }
}
