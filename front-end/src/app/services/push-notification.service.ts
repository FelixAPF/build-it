import { Injectable, inject, NgZone } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Capacitor } from '@capacitor/core';
import { PushNotifications, Token, ActionPerformed, PushNotificationSchema } from '@capacitor/push-notifications';
import { environment } from '../../environment/environment';
import { MessageService } from 'primeng/api'; // <-- NEW IMPORT

@Injectable({
  providedIn: 'root'
})
export class PushNotificationService {
  private http = inject(HttpClient);
  private messageService = inject(MessageService); // <-- INJECT PRIMENG SERVICE
  private zone = inject(NgZone); // Used to ensure Angular updates the UI

  initPush() {
    if (!Capacitor.isNativePlatform()) return;
    this.registerNotifications();
  }

  private async registerNotifications() {
    try {
      let permStatus = await PushNotifications.checkPermissions();
      if (permStatus.receive === 'prompt') {
        permStatus = await PushNotifications.requestPermissions();
      }
      if (permStatus.receive !== 'granted') return;

      await PushNotifications.register();

      PushNotifications.addListener('registration', (token: Token) => {
        this.saveTokenToBackend(token.value);
      });

      // --- THE NEW FOREGROUND LISTENER ---
      PushNotifications.addListener('pushNotificationReceived', (notification: PushNotificationSchema) => {
        // Run inside NgZone so Angular knows to render the PrimeNG Toast immediately
        this.zone.run(() => {
          this.messageService.add({
            severity: 'info',
            summary: notification.title || 'New Notification',
            detail: notification.body || '',
            life: 5000 // Stays on screen for 5 seconds
          });
        });
      });

      PushNotifications.addListener('pushNotificationActionPerformed', (notification: ActionPerformed) => {
        console.log('User tapped the OS notification: ', notification);
        // You can add routing logic here later (e.g., navigate to /worker-dashboard)
      });

    } catch (error) {
      console.error('Failed to initialize push notifications', error);
    }
  }

  private saveTokenToBackend(token: string) {
    this.http.post(`${environment.apiUrl}/api/users/device-token`, { token }, { responseType: 'text' })
      .subscribe();
  }
}