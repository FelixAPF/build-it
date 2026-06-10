import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service';
import { ButtonModule } from 'primeng/button';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'app-landing',
  standalone: true,
  imports: [CommonModule, RouterLink, ButtonModule, TranslatePipe],
  templateUrl: './landing.component.html'
})
export class LandingComponent {
  public authService = inject(AuthService); // <-- Ensure this is public
  private translate = inject(TranslateService); // <-- Inject the service

  // Track the current language to display the correct button text
  public currentLang = localStorage.getItem('buildit_lang') || 'en';

  // Quick helper to route them to the correct dashboard if they are already logged in
  getDashboardLink(): string {
    const role = localStorage.getItem('user_role');
    if (role === 'ADMIN') return '/admin-dashboard';
    return role === 'WORKER' ? '/worker-dashboard' : '/business-dashboard';
  }

  switchLanguage() {
    this.currentLang = this.currentLang === 'en' ? 'fr' : 'en';
    this.translate.use(this.currentLang); // Swap the translations live
    localStorage.setItem('buildit_lang', this.currentLang); // Save to browser memory
  }
}