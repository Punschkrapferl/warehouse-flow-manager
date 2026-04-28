import { Injectable, inject, ChangeDetectorRef } from '@angular/core';
import { catchError, finalize, forkJoin, of, tap } from 'rxjs';
import {
  HealthResponse,
  ProductResponse,
  ReplenishmentRecommendationResponse,
  StockMovementResponse,
} from '../../core/api/api.types';
import { DashboardApiService } from '../../core/api/dashboard-api.service';
import { ApiErrorMessageService } from '../../core/error/api-error-message.service';

@Injectable()
export class DashboardFacade {
  private readonly dashboardApi = inject(DashboardApiService);
  private readonly apiErrorMessage = inject(ApiErrorMessageService);
  private readonly cdr = inject(ChangeDetectorRef);

  loading = true;
  errorMessage = '';

  health: HealthResponse | null = null;
  lowStockProducts: ProductResponse[] = [];
  replenishmentCandidates: ReplenishmentRecommendationResponse[] = [];
  recentMovements: StockMovementResponse[] = [];

  // Loads the dashboard as one coordinated view-model so the component only renders state
  // and does not need to know how many backend endpoints are involved.
  loadDashboard(): void {
    this.loading = true;
    this.errorMessage = '';

    forkJoin({
      health: this.dashboardApi.getHealth(),
      lowStockProducts: this.dashboardApi.getLowStockProducts(),
      replenishmentCandidates: this.dashboardApi.getReplenishmentCandidates(30),
      recentMovements: this.dashboardApi.getRecentStockMovements(8),
    })
      .pipe(
        tap((result) => {
          this.health = result.health;
          this.lowStockProducts = result.lowStockProducts;
          this.replenishmentCandidates = result.replenishmentCandidates;
          this.recentMovements = result.recentMovements;
        }),
        // Clear partial dashboard data so the UI does not show stale values after a failed refresh.
        catchError((error) => {
          this.errorMessage = this.apiErrorMessage.toMessage(
            error,
            'Could not load dashboard data.',
          );
          this.health = null;
          this.lowStockProducts = [];
          this.replenishmentCandidates = [];
          this.recentMovements = [];

          return of({
            health: this.health,
            lowStockProducts: this.lowStockProducts,
            replenishmentCandidates: this.replenishmentCandidates,
            recentMovements: this.recentMovements,
          });
        }),
        finalize(() => {
          this.loading = false;
          this.cdr.detectChanges();
        }),
      )
      .subscribe();
  }

  get criticalCount(): number {
    return this.replenishmentCandidates.filter((candidate) => candidate.priority === 'CRITICAL')
      .length;
  }
}
