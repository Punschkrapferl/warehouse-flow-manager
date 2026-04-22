import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

interface NavigationItem {
  label: string;
  route: string;
  caption: string;
}

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss',
})
export class AppComponent {
  protected readonly navigationItems: NavigationItem[] = [
    {
      label: 'Dashboard',
      route: '/dashboard',
      caption: 'Operations board and stock risk',
    },
    {
      label: 'Products',
      route: '/products',
      caption: 'Stock ledger and item visibility',
    },
    {
      label: 'Storage Locations',
      route: '/storage-locations',
      caption: 'Zones and assigned inventory',
    },
  ];
}
