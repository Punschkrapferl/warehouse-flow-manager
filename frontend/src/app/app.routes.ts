import { Routes } from '@angular/router';
import { DashboardComponent } from './features/dashboard/dashboard.component';
import { ProductsComponent } from './features/products/products.component';
import { StockMovementsComponent } from './features/stock-movements/stock-movements.component';
import { StorageLocationsComponent } from './features/storage-locations/storage-locations.component';

export const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'dashboard',
  },
  {
    path: 'dashboard',
    component: DashboardComponent,
    title: 'Dashboard | Warehouse Flow Manager',
  },
  {
    path: 'products',
    component: ProductsComponent,
    title: 'Products | Warehouse Flow Manager',
  },
  {
    path: 'storage-locations',
    component: StorageLocationsComponent,
    title: 'Storage Locations | Warehouse Flow Manager',
  },
  {
    path: 'stock-movements',
    component: StockMovementsComponent,
    title: 'Stock Movements | Warehouse Flow Manager',
  },
  {
    path: '**',
    redirectTo: 'dashboard',
  },
];
