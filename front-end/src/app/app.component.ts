import { Component, inject, NgZone, OnInit, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Location } from '@angular/common';
import { TranslateService } from '@ngx-translate/core';
import { App } from '@capacitor/app';
import { Router } from '@angular/router';
import { Browser } from '@capacitor/browser';
import { VersionService } from './services/version.service';
import { DialogModule } from 'primeng/dialog';
import { ButtonModule } from 'primeng/button';
import { PushNotificationService } from './services/push-notification.service';
import { MessageService } from 'primeng/api';
import { ToastModule } from 'primeng/toast';



@Component({
  selector: 'app-root',
  imports: [RouterOutlet, DialogModule, ButtonModule, ToastModule],
  templateUrl: './app.component.html',
  providers: [MessageService],
  styleUrl: './app.component.css'
})
export class AppComponent implements OnInit {
  private translate = inject(TranslateService);
  private router = inject(Router);
  private location = inject(Location);
  private zone = inject(NgZone);
  private versionService = inject(VersionService);
  private pushNotificationService = inject(PushNotificationService);
  showUpdateDialog = false;
  updateStoreUrl = '';


  title = 'build-it';

  // State flag to block rendering until JSON translations are ready
  isTranslationsLoaded = signal(false);

  async ngOnInit() {
    const savedLang = localStorage.getItem('buildit_lang') || 'en';
    this.pushNotificationService.initPush();
    
    // translate.use returns an Observable. Subscribe to flip our flag when ready.
    this.translate.use(savedLang).subscribe({
      next: () => this.isTranslationsLoaded.set(true),
      error: () => this.isTranslationsLoaded.set(true) // Fallback safety guard
    });
    App.addListener('appUrlOpen', data => {
      // FIX: Check for the custom mobile scheme instead of localhost
      if (data.url.startsWith('crewup://app')) {
        
        // 1. Force the Capacitor in-app browser overlay to close
        Browser.close();

        // 2. Strip the scheme to get the actual Angular route (e.g., /payment-success?jobId=123)
        const routePath = data.url.replace('crewup://app', '');

        // 3. Use NgZone to ensure Angular detects the route change and updates the UI
        this.zone.run(() => {
          this.router.navigateByUrl(routePath);
        });
      }
    });

    await this.verifyAppVersion();

    App.addListener('backButton', ({ canGoBack }) => {
      // 1. Check if any PrimeNG Dialogs or Overlays are currently active in the DOM
      const activeDialog = document.querySelector('.p-dialog-mask, .p-component-overlay');
      
      if (activeDialog) {
        // Find the close button inside the active PrimeNG dialog and trigger a click
        const closeButton = activeDialog.querySelector('.p-dialog-header-close') as HTMLElement;
        if (closeButton) {
          closeButton.click();
          return; // Stop execution here so we don't navigate backwards!
        }
      }

      // 2. If no dialogs are open, proceed with your standard history stack navigation
      const currentUrl = this.router.url.split('?')[0];
      const exitRoutes = ['/landing', '/login', '/worker-dashboard', '/business-dashboard', '/admin-dashboard'];

      if (canGoBack) {
        this.location.back();
        return;
      }

      if (exitRoutes.includes(currentUrl)) {
        App.exitApp();
        return;
      }

      this.location.back();
      });
    }

  async verifyAppVersion() {
    const status = await this.versionService.checkForUpdates();
    if (status.requiresUpdate && status.updateUrl) {
      this.updateStoreUrl = status.updateUrl;
      this.showUpdateDialog = true;
    }
  }

  goToStore() {
    this.versionService.openAppStore(this.updateStoreUrl);
  }
    
}