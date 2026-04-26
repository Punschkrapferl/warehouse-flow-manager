import { DatePipe } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { StockMovementType } from '../../core/api/api.types';
import { StockMovementsFacade } from './stock-movements.facade';

@Component({
  selector: 'app-stock-movements',
  standalone: true,
  imports: [FormsModule, DatePipe],
  providers: [StockMovementsFacade],
  templateUrl: './stock-movements.component.html',
  styleUrls: ['./stock-movements.component.scss'],
})
export class StockMovementsComponent implements OnInit {
  protected readonly facade = inject(StockMovementsFacade);

  ngOnInit(): void {
    this.facade.loadData();
  }

  protected movementBadgeClass(type: StockMovementType): string {
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
