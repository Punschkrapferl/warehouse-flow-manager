import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { forkJoin, catchError, finalize, of, tap } from 'rxjs';
import { DashboardApiService } from '../../core/api/dashboard-api.service';
import {
  HealthResponse,
  ProductResponse,
  ReplenishmentRecommendationResponse,
  StockMovementResponse,
} from '../../core/api/api.types';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [DatePipe],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss',
})
export class DashboardComponent implements OnInit {
  private readonly dashboardApi = inject(DashboardApiService);
  private readonly cdr = inject(ChangeDetectorRef);

  protected loading = true;
  protected errorMessage = '';

  protected health: HealthResponse | null = null;
  protected lowStockProducts: ProductResponse[] = [];
  protected replenishmentCandidates: ReplenishmentRecommendationResponse[] = [];
  protected recentMovements: StockMovementResponse[] = [];

  ngOnInit(): void {
    this.loadDashboard();
  }
  protected loadDashboard(): void {
    this.loading = true;
    this.errorMessage = '';

    forkJoin({
      health: this.dashboardApi.getHealth(),
      lowStockProducts: this.dashboardApi.getLowStockProducts(),
      replenishmentCandidates: this.dashboardApi.getReplenishmentCandidates(30),
      recentMovements: this.dashboardApi.getRecentStockMovements(8),
    }).pipe(
      tap((result) => {
        this.health = result.health;
        this.lowStockProducts = result.lowStockProducts;
        this.replenishmentCandidates = result.replenishmentCandidates;
        this.recentMovements = result.recentMovements;
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
      )
      .subscribe();
  }

  protected get criticalCount(): number {
    return this.replenishmentCandidates.filter((candidate) => candidate.priority === 'CRITICAL')
      .length;
  }

  protected priorityClass(priority: string): string {
    switch (priority) {
      case 'CRITICAL':
        return 'badge badge--danger';
      case 'HIGH':
        return 'badge badge--warning';
      default:
        return 'badge badge--neutral';
    }
  }

  protected movementClass(type: string): string {
    switch (type) {
      case 'INBOUND':
        return 'badge badge--success';
      case 'OUTBOUND':
        return 'badge badge--warning';
      default:
        return 'badge badge--neutral';
    }
  }

  private buildErrorMessage(error: unknown): string {
    if (error instanceof HttpErrorResponse) {
      return `Could not load dashboard data (${error.status}). ${error.message}`;
    }

    return 'Could not load dashboard data. Check whether the Spring Boot backend is running on localhost:8080.';
  }
}
