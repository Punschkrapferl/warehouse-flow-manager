import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { beforeEach, describe, expect, it } from 'vitest';
import { AppComponent } from './app.component';

describe('AppComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AppComponent],
      providers: [provideRouter([])],
    }).compileComponents();
  });

  it('should render the application title in the sidebar', () => {
    const fixture = TestBed.createComponent(AppComponent);

    fixture.detectChanges();

    const element = fixture.nativeElement as HTMLElement;
    const title = element.querySelector('.brand__title');

    expect(title?.textContent?.trim()).toBe('Warehouse Flow Manager');
  });

  it('should render the application subtitle in the sidebar', () => {
    const fixture = TestBed.createComponent(AppComponent);

    fixture.detectChanges();

    const element = fixture.nativeElement as HTMLElement;
    const subtitle = element.querySelector('.brand__subtitle');

    expect(subtitle?.textContent?.trim()).toBe('Operations Control Console');
  });

  it('should render all main navigation links', () => {
    const fixture = TestBed.createComponent(AppComponent);

    fixture.detectChanges();

    const element = fixture.nativeElement as HTMLElement;
    const navigationLabels = Array.from(element.querySelectorAll('.nav__label')).map((label) =>
      label.textContent?.trim(),
    );

    expect(navigationLabels).toEqual([
      'Dashboard',
      'Products',
      'Storage Locations',
      'Stock Movements',
    ]);
  });

  it('should point navigation links to the correct routes', () => {
    const fixture = TestBed.createComponent(AppComponent);

    fixture.detectChanges();

    const element = fixture.nativeElement as HTMLElement;
    const navigationLinks = Array.from(element.querySelectorAll<HTMLAnchorElement>('.nav__link'));

    const hrefs = navigationLinks.map((link) => link.getAttribute('href'));

    expect(hrefs).toEqual(['/dashboard', '/products', '/storage-locations', '/stock-movements']);
  });

  it('should render the topbar title', () => {
    const fixture = TestBed.createComponent(AppComponent);

    fixture.detectChanges();

    const element = fixture.nativeElement as HTMLElement;
    const title = element.querySelector('.topbar__title');

    expect(title?.textContent?.trim()).toBe('Live stock visibility and replenishment monitoring');
  });
});
