import { DatePipe } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { ReplenishmentPriority, StockMovementType } from '../../core/api/api.types';
import { DashboardFacade } from './dashboard.facade';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [DatePipe],
  providers: [DashboardFacade],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss',
})
export class DashboardComponent implements OnInit {
  protected readonly facade = inject(DashboardFacade);

  ngOnInit(): void {
    this.facade.loadDashboard();
  }

  protected priorityClass(priority: ReplenishmentPriority): string {
    switch (priority) {
      case 'CRITICAL':
        return 'badge badge--danger';
      case 'HIGH':
        return 'badge badge--warning';
      case 'MEDIUM':
        return 'badge badge--neutral';
      default:
        return 'badge badge--neutral';
    }
  }

  protected movementClass(type: StockMovementType): string {
    switch (type) {
      case 'INBOUND':
        return 'badge badge--success';
      case 'OUTBOUND':
        return 'badge badge--warning';
      case 'ADJUSTMENT':
        return 'badge badge--neutral';
      default:
        return 'badge badge--neutral';
    }
  }

  protected movementQuantityClass(type: StockMovementType): string {
    switch (type) {
      case 'INBOUND':
        return 'metric-pill metric-pill--success';
      case 'OUTBOUND':
        return 'metric-pill metric-pill--critical';
      case 'ADJUSTMENT':
        return 'metric-pill metric-pill--neutral';
      default:
        return 'metric-pill metric-pill--neutral';
    }
  }
}
