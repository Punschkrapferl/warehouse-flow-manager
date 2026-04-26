import { HttpErrorResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { catchError, finalize, forkJoin, of, tap } from 'rxjs';
import {
  HealthResponse,
  ProductResponse,
  ReplenishmentRecommendationResponse,
  StockMovementResponse,
} from '../../core/api/api.types';
import { DashboardApiService } from '../../core/api/dashboard-api.service';

@Injectable()
export class DashboardFacade {
  private readonly dashboardApi = inject(DashboardApiService);

  loading = true;
  errorMessage = '';

  health: HealthResponse | null = null;
  lowStockProducts: ProductResponse[] = [];
  replenishmentCandidates: ReplenishmentRecommendationResponse[] = [];
  recentMovements: StockMovementResponse[] = [];

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
        catchError((error) => {
          this.errorMessage = this.buildErrorMessage(error);
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
        }),
      )
      .subscribe();
  }

  get criticalCount(): number {
    return this.replenishmentCandidates.filter((candidate) => candidate.priority === 'CRITICAL')
      .length;
  }

  private buildErrorMessage(error: unknown): string {
    if (error instanceof HttpErrorResponse) {
      return `Could not load dashboard data (${error.status}). ${error.message}`;
    }

    return 'Could not load dashboard data. Check whether the Spring Boot backend is running on localhost:8080.';
  }
}
