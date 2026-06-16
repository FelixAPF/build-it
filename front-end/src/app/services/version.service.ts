import { Injectable } from '@angular/core';
import { App } from '@capacitor/app';
import { Capacitor } from '@capacitor/core';
import { Browser } from '@capacitor/browser';
import { environment } from '../../environment/environment';

// Firebase Imports
import { initializeApp } from 'firebase/app';
import { getRemoteConfig, fetchAndActivate, getString } from 'firebase/remote-config';

@Injectable({
  providedIn: 'root'
})
export class VersionService {
  private firebaseApp = initializeApp(environment.firebase);
  private remoteConfig = getRemoteConfig(this.firebaseApp);

  constructor() {
    // In development, fetch instantly (0ms). In production, cache for 12 hours (43200000ms) to save bandwidth.
    this.remoteConfig.settings.minimumFetchIntervalMillis = environment.production ? 43200000 : 0; 
  }

  async checkForUpdates(): Promise<{ requiresUpdate: boolean, updateUrl?: string }> {
    // Skip if running in standard web browser via 'ng serve'
    if (!Capacitor.isNativePlatform()) {
      console.log("AUTO RUNNING ");
      return { requiresUpdate: false };
    }
    console.log("NOT AUTO RUNNING")

    try {
      // 1. Download latest values from Firebase
      await fetchAndActivate(this.remoteConfig);
      
      // 2. Read the parameters you just created
      const minVersion = getString(this.remoteConfig, 'min_required_version');
      const androidUrl = getString(this.remoteConfig, 'android_store_url');
      const iosUrl = getString(this.remoteConfig, 'ios_store_url');

      // 3. Get the device's current installed version
      const appInfo = await App.getInfo();
      const currentVersion = appInfo.version;

      // 4. Compare versions
      if (this.isVersionOutdated(currentVersion, minVersion)) {
        const platform = Capacitor.getPlatform();
        const updateUrl = platform === 'ios' ? iosUrl : androidUrl;
        return { requiresUpdate: true, updateUrl };
      }

      return { requiresUpdate: false };
    } catch (error) {
      console.error('Failed to fetch Firebase Remote Config:', error);
      return { requiresUpdate: false }; // Let the user in if offline or Firebase fails
    }
  }

  private isVersionOutdated(current: string, required: string): boolean {
    if (!required) return false;
    
    const vCurrent = current.split('.').map(Number);
    const vRequired = required.split('.').map(Number);

    for (let i = 0; i < Math.max(vCurrent.length, vRequired.length); i++) {
      const c = vCurrent[i] || 0;
      const r = vRequired[i] || 0;
      if (c < r) return true;
      if (c > r) return false;
    }
    return false;
  }

  async openAppStore(url: string) {
    await Browser.open({ url });
  }
}