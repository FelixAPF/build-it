import { Component, inject, OnInit } from '@angular/core';
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

  ngOnInit(): void {
    const savedLang = localStorage.getItem('buildit_lang') || 'en';
    
    // Apply the language live
    this.translate.use(savedLang);
  }
}
