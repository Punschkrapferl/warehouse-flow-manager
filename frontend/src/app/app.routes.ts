import { Routes } from '@angular/router';
import { DashboardComponent } from './features/dashboard/dashboard.component';
import { ProductsComponent } from './features/products/products.component';

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
    path: '**',
    redirectTo: 'dashboard',
  },
];
