import { CommonModule, DatePipe } from '@angular/common';
import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { catchError, finalize, of, tap } from 'rxjs';
import { FormsModule } from '@angular/forms';
import { StockMovementsApiService } from '../../core/api/stock-movements-api.service';
import {
  StockMovementResponse,
  StockMovementSummaryResponse,
  StockMovementType,
} from '../../core/api/api.types';

type MovementTypeFilter = 'ALL' | StockMovementType;

@Component({
  selector: 'app-stock-movements',
  standalone: true,
  imports: [CommonModule, FormsModule, DatePipe],
  templateUrl: './stock-movements.component.html',
  styleUrls: ['./stock-movements.component.scss'],
})
export class StockMovementsComponent implements OnInit {
  private readonly stockMovementsApi = inject(StockMovementsApiService);
  private readonly cdr = inject(ChangeDetectorRef);

  protected loadingMovements = false;
  protected loadingSummary = false;
  protected errorMessage = '';

  protected movements: StockMovementResponse[] = [];
  protected summary: StockMovementSummaryResponse | null = null;

  protected filters = {
    productId: '',
    movementType: 'ALL' as MovementTypeFilter,
    from: '',
    to: '',
  };

  ngOnInit(): void {
    this.loadData();
  }

  protected refreshData(): void {
    this.loadData();
  }

  protected applyFilters(): void {
    this.loadData();
  }

  protected resetFilters(): void {
    this.filters = {
      productId: '',
      movementType: 'ALL',
      from: '',
      to: '',
    };

    this.loadData();
  }

  protected trackByMovementId(_: number, movement: StockMovementResponse): number {
    return movement.id;
  }

  private loadData(): void {
    this.loadingMovements = true;
    this.loadingSummary = true;
    this.errorMessage = '';

    const productId = this.parseProductId(this.filters.productId);
    const movementType =
      this.filters.movementType === 'ALL' ? undefined : this.filters.movementType;
    const from = this.toIsoInstant(this.filters.from);
    const to = this.toIsoInstant(this.filters.to);

    this.stockMovementsApi
      .getStockMovements({
        productId,
        movementType,
        from,
        to,
      })
      .pipe(
        tap((movements) => {
          this.movements = movements;
        }),
        catchError((error) => {
          this.errorMessage = error?.message || 'Could not load stock movements.';
          return of([]);
        }),
        finalize(() => {
          this.loadingMovements = false;
          this.cdr.detectChanges();
        }),
        ).subscribe();

    this.stockMovementsApi
      .getStockMovementSummary({
        productId,
        from,
        to,
      })
      .pipe(
        tap((summary) => {
          this.summary = summary;
          this.loadingSummary = false;
        }),
        catchError((error) => {
          this.errorMessage = error?.message || 'Could not load stock movement summary.';
          return of([]);
        }),
        finalize(() => {
          this.summary = null;
          this.loadingSummary = false;
          this.cdr.detectChanges();
        }),
        ).subscribe();
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
}
