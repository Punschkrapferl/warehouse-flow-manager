import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ProductStatus } from '../../core/api/api.types';
import { ProductsFacade } from './products.facade';

@Component({
  selector: 'app-products',
  standalone: true,
  imports: [FormsModule],
  providers: [ProductsFacade],
  templateUrl: './products.component.html',
  styleUrl: './products.component.scss',
})
export class ProductsComponent implements OnInit {
  protected readonly facade = inject(ProductsFacade);

  ngOnInit(): void {
    this.facade.loadProducts();
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
}
