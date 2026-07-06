import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../core/services/auth.service';

interface NavItem { label: string; route: string; icon: string; }

@Component({
  selector: 'app-shell',
  imports: [RouterLink, RouterLinkActive, RouterOutlet],
  template: `
    <div class="shell">
      <aside class="sidebar" [class.open]="menuOpen()">
        <a class="brand" routerLink="/dashboard" (click)="menuOpen.set(false)">
          <span class="brand-mark"><i></i><i></i><i></i></span>
          <span>IntroTech</span>
        </a>
        <nav aria-label="Primary navigation">
          <span class="nav-label">Workspace</span>
          @for (item of primaryNav; track item.route) {
            <a [routerLink]="item.route" routerLinkActive="active" (click)="menuOpen.set(false)">
              <span class="nav-icon">{{ item.icon }}</span><span>{{ item.label }}</span>
            </a>
          }
          <span class="nav-label nav-label-spaced">Insights</span>
          @for (item of secondaryNav; track item.route) {
            <a [routerLink]="item.route" routerLinkActive="active" (click)="menuOpen.set(false)">
              <span class="nav-icon">{{ item.icon }}</span><span>{{ item.label }}</span>
            </a>
          }
        </nav>
        <div class="support-card">
          <span class="support-icon">?</span>
          <div><strong>Need a hand?</strong><small>Visit the help centre</small></div>
        </div>
        <div class="sidebar-user">
          <span class="avatar">{{ initials() }}</span>
          <div><strong>{{ displayName() }}</strong><small>{{ auth.profile()?.email || 'Your workspace' }}</small></div>
          <button aria-label="Log out" title="Log out" (click)="auth.logout()">↗</button>
        </div>
      </aside>
      @if (menuOpen()) { <button class="mobile-backdrop" aria-label="Close menu" (click)="menuOpen.set(false)"></button> }
      <section class="workspace">
        <header class="topbar">
          <button class="menu-button" aria-label="Open menu" (click)="menuOpen.set(true)"><span></span><span></span><span></span></button>
          <div class="topbar-copy"><span class="live-dot"></span> Payroll workspace</div>
          <div class="topbar-actions">
            <button class="topbar-icon" aria-label="Notifications">◌</button>
            <a class="profile-chip" routerLink="/settings"><span class="avatar">{{ initials() }}</span><span>{{ displayName() }}</span></a>
          </div>
        </header>
        <main><router-outlet /></main>
      </section>
    </div>
  `,
  styleUrl: './app-shell.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AppShellComponent {
  readonly auth = inject(AuthService);
  readonly menuOpen = signal(false);
  readonly primaryNav: NavItem[] = [
    { label: 'Dashboard', route: '/dashboard', icon: '⌂' },
    { label: 'Workers', route: '/workers', icon: '♙' },
    { label: 'Schedules', route: '/schedules', icon: '□' },
    { label: 'Payments', route: '/payments', icon: '↗' }
  ];
  readonly secondaryNav: NavItem[] = [
    { label: 'Reports', route: '/reports', icon: '⌁' },
    { label: 'Settings', route: '/settings', icon: '⚙' }
  ];

  displayName(): string {
    const profile = this.auth.profile();
    return profile ? `${profile.firstName} ${profile.lastName}` : 'Employer';
  }

  initials(): string {
    const profile = this.auth.profile();
    return profile ? `${profile.firstName[0] ?? ''}${profile.lastName[0] ?? ''}`.toUpperCase() : 'IT';
  }
}
