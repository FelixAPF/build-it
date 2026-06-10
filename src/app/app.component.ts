import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent implements OnInit {
  private translate = inject(TranslateService);
  title = 'build-it';

  // State flag to block rendering until JSON translations are ready
  isTranslationsLoaded = signal(false);

  ngOnInit(): void {
    const savedLang = localStorage.getItem('buildit_lang') || 'en';
    
    // translate.use returns an Observable. Subscribe to flip our flag when ready.
    this.translate.use(savedLang).subscribe({
      next: () => this.isTranslationsLoaded.set(true),
      error: () => this.isTranslationsLoaded.set(true) // Fallback safety guard
    });
  }
}