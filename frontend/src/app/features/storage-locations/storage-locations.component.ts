import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { StorageLocationsFacade } from './storage-locations.facade';

@Component({
  selector: 'app-storage-locations',
  standalone: true,
  imports: [FormsModule],
  providers: [StorageLocationsFacade],
  templateUrl: './storage-locations.component.html',
  styleUrls: ['./storage-locations.component.scss'],
})
export class StorageLocationsComponent implements OnInit {
  protected readonly facade = inject(StorageLocationsFacade);

  ngOnInit(): void {
    this.facade.loadLocations();
  }
}
