import { ApplicationConfig, importProvidersFrom, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http'; // <-- Import withInterceptors
import { provideAnimations } from '@angular/platform-browser/animations';
import { TranslateLoader, provideTranslateService } from '@ngx-translate/core';
import { provideTranslateHttpLoader, TranslateHttpLoader } from '@ngx-translate/http-loader';
import { routes } from './app.routes';
import { authInterceptor } from './interceptors/auth.interceptor'; // <-- Import the interceptor
import { MessageService } from 'primeng/api';


export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }), 
    MessageService,
    provideRouter(routes),
    provideHttpClient(withInterceptors([authInterceptor])), // <-- Add it here!
    provideAnimations(),
    provideTranslateService({
      lang: 'en',             // <-- FIXED: Changed from 'defaultLanguage'
      fallbackLang: 'en',     // <-- FIXED: Added fallback language parameter
      loader: provideTranslateHttpLoader({
        prefix: './i18n/',    // <-- FIXED: Points straight to your translation asset folder
        suffix: '.json'
      })
    })
  ]
};