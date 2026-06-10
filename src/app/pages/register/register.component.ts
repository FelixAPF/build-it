import { Component, OnInit, inject, ViewChild, ElementRef, NgZone, ChangeDetectorRef, AfterViewInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, ActivatedRoute, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service';

import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { PasswordModule } from 'primeng/password';
import { DropdownModule } from 'primeng/dropdown';
import { InputNumberModule } from 'primeng/inputnumber';
import { CheckboxModule } from 'primeng/checkbox';
import { MultiSelectModule } from 'primeng/multiselect';
import { TabViewModule } from 'primeng/tabview';
import { ToastModule } from 'primeng/toast';
import { MessageService } from 'primeng/api';
import { MessageModule } from 'primeng/message';
import { DialogModule } from 'primeng/dialog';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';

declare var google: any; // <-- Required for Google Maps Places API

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, RouterModule, ButtonModule, InputTextModule,
    PasswordModule, DropdownModule, InputNumberModule, CheckboxModule,
    MultiSelectModule, TabViewModule, ToastModule, MessageModule, DialogModule, TranslatePipe 
  ],
  providers: [MessageService],
  templateUrl: './register.component.html'
})
export class RegisterComponent implements OnInit, AfterViewInit {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private messageService = inject(MessageService);
  private translate = inject(TranslateService); 
  private ngZone = inject(NgZone);
  private cdr = inject(ChangeDetectorRef);

  @ViewChild('autocompleteContainer') autocompleteContainerRef!: ElementRef;

  activeTabIndex: number = 0;
  workerForm!: FormGroup;
  businessForm!: FormGroup;
  isLoading: boolean = false;
  errorMessage: string = '';
  showTermsDialog: boolean = false;
  public currentLang = localStorage.getItem('buildit_lang') || 'en'; 

  tradeOptions: any[] = [];

  switchLanguage() {
    this.currentLang = this.currentLang === 'en' ? 'fr' : 'en';
    this.translate.use(this.currentLang);
    localStorage.setItem('buildit_lang', this.currentLang);
  }

  ngOnInit() {
    this.authService.getTrades().subscribe({
      next: (res) => this.tradeOptions = res,
      error: (err) => console.error('Failed to load trades', err)
    });

    this.route.queryParams.subscribe(params => {
      if (params['tab'] === 'business') {
        this.activeTabIndex = 1;
      } else {
        this.activeTabIndex = 0;
      }
    });

    this.workerForm = this.fb.group({
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      phoneNumber: ['', Validators.required],
      yearsExperience: [0, [Validators.required, Validators.min(0)]],
      ccqNumber: [''],
      specialties: [[], Validators.required],
      termsAccepted: [false, Validators.requiredTrue]
    });

    // FIX: Swapped billingAddress for full address suite
    this.businessForm = this.fb.group({
      businessType: ['COMPANY', Validators.required],
      companyName: ['', Validators.required],
      contactName: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      phoneNumber: ['', Validators.required],
      address: ['', Validators.required],
      city: ['', Validators.required],
      province: ['', Validators.required],
      postalCode: ['', Validators.required],
      rbqNumber: [''],
      neqNumber: [''],
      termsAccepted: [false, Validators.requiredTrue]
    });

    this.businessForm.get('businessType')?.valueChanges.subscribe(type => {
      if (type === 'COMPANY') {
        this.businessForm.get('rbqNumber')?.setValidators([Validators.required]);
        this.businessForm.get('neqNumber')?.setValidators([Validators.required]);
      } else {
        this.businessForm.get('rbqNumber')?.clearValidators();
        this.businessForm.get('neqNumber')?.clearValidators();
      }
      this.businessForm.get('rbqNumber')?.updateValueAndValidity();
      this.businessForm.get('neqNumber')?.updateValueAndValidity();
    });
  }

  ngAfterViewInit() {
    // If they land directly on the business tab via URL
    if (this.activeTabIndex === 1) {
      this.initAutocomplete();
    }
  }

  onTabChange(event: any) {
    // Fire the autocomplete injection when they click the Employer tab
    if (event.index === 1) {
      this.initAutocomplete();
    }
  }

  // --- AUTOCOMPLETE LOGIC ---
  initAutocomplete() {
    setTimeout(() => {
      if (!this.autocompleteContainerRef?.nativeElement) return;

      const container = this.autocompleteContainerRef.nativeElement;
      if (container.querySelector('input')) return; // Prevent duplicating inputs

      container.innerHTML = '';

      const input = document.createElement('input');
      input.placeholder = this.translate.instant('REGISTER.PLACEHOLDER_SEARCH_ADDRESS') || 'Search for an address...';
      input.style.cssText = 'width:100%; padding:12px; border:none; outline:none; background:transparent; font-size:14px; box-sizing:border-box;';
      container.appendChild(input);

      const dropdown = document.createElement('ul');
      dropdown.style.cssText = `
        position:absolute; z-index:99999;
        background:white; border:1px solid #e2e8f0; border-radius:8px;
        box-shadow:0 4px 20px rgba(0,0,0,0.15); list-style:none;
        margin:0; padding:4px 0; display:none; min-width:200px;
      `;
      document.body.appendChild(dropdown);

      const positionDropdown = () => {
        const rect = input.getBoundingClientRect();
        dropdown.style.top = `${rect.bottom + 4 + window.scrollY}px`;
        dropdown.style.left = `${rect.left + window.scrollX}px`;
        dropdown.style.width = `${rect.width}px`;
      };

      let debounceTimer: any;
      let sessionToken = new google.maps.places.AutocompleteSessionToken();

      input.addEventListener('input', () => {
        clearTimeout(debounceTimer);
        const query = input.value.trim();
        if (query.length < 3) { dropdown.style.display = 'none'; return; }
        positionDropdown();
        debounceTimer = setTimeout(() => this.fetchSuggestions(query, dropdown, input, sessionToken, () => {
          sessionToken = new google.maps.places.AutocompleteSessionToken();
        }), 300);
      });

      const observer = new MutationObserver(() => {
        if (!document.body.contains(container)) {
          dropdown.remove();
          observer.disconnect();
        }
      });
      observer.observe(document.body, { childList: true, subtree: true });

      document.addEventListener('click', (e) => {
        if (!container.contains(e.target as Node) && !dropdown.contains(e.target as Node)) {
          dropdown.style.display = 'none';
        }
      });
    }, 100);
  }

  async fetchSuggestions(query: string, dropdown: HTMLElement, input: HTMLInputElement, sessionToken: any, onPicked: () => void) {
    try {
      const request = {
        input: query,
        sessionToken,
        includedRegionCodes: ['ca'],
        language: this.currentLang
      };

      const { suggestions } = await google.maps.places.AutocompleteSuggestion.fetchAutocompleteSuggestions(request);
      dropdown.innerHTML = '';

      if (!suggestions?.length) {
        dropdown.style.display = 'none';
        return;
      }

      suggestions.forEach((suggestion: any) => {
        const placePrediction = suggestion.placePrediction;
        const li = document.createElement('li');
        li.style.cssText = 'padding:10px 14px; cursor:pointer; font-size:13px; color:#334155; border-bottom:1px solid #f1f5f9;';
        li.textContent = placePrediction.text.toString();

        li.addEventListener('mouseenter', () => li.style.background = '#fef2f2'); 
        li.addEventListener('mouseleave', () => li.style.background = 'white');

        li.addEventListener('click', async () => {
          input.value = placePrediction.text.toString();
          dropdown.style.display = 'none';

          try {
            const place = placePrediction.toPlace();
            await place.fetchFields({ fields: ['addressComponents'] });

            this.ngZone.run(() => {
              this.fillInAddressFromComponents(place.addressComponents ?? []);
            });

            onPicked(); 
          } catch (err) {
            console.error('fetchFields error:', err);
          }
        });

        dropdown.appendChild(li);
      });

      dropdown.style.display = 'block';

    } catch (err) {
      console.error('fetchAutocompleteSuggestions error:', err);
    }
  }

  fillInAddressFromComponents(components: any[]) {
    let streetNumber = '', route = '', city = '', province = '', postalCode = '';

    for (const component of components) {
      const types: string[] = component.types ?? [];
      const longText: string = component.longText ?? component.long_name ?? '';
      const shortText: string = component.shortText ?? component.short_name ?? '';

      if (types.includes('street_number')) streetNumber = longText;
      if (types.includes('route')) route = longText;
      if (types.includes('locality') || types.includes('sublocality') || types.includes('postal_town')) city = longText;
      if (types.includes('administrative_area_level_1')) province = shortText;
      if (types.includes('postal_code')) postalCode = longText;
    }

    this.businessForm.patchValue({ address: `${streetNumber} ${route}`.trim(), city, province, postalCode });
    this.cdr.detectChanges();
  }
  // --- END AUTOCOMPLETE LOGIC ---

  onWorkerSubmit() {
    if (this.workerForm.valid) {
      this.isLoading = true;
      this.errorMessage = '';
      const payload = { ...this.workerForm.value };
      delete payload.termsAccepted;

      this.authService.registerWorker(payload).subscribe({
        next: (res) => {
          this.isLoading = false;
          this.router.navigate(['/verify-email']);
        },
        error: (err) => {
          this.isLoading = false;
          this.errorMessage = err.error || 'Registration failed. Please try again.';
        }
      });
    }
  }

  onBusinessSubmit() {
    if (this.businessForm.valid) {
      this.isLoading = true;
      this.errorMessage = '';
      const payload = { ...this.businessForm.value };
      delete payload.termsAccepted;

      this.authService.registerBusiness(payload).subscribe({
        next: (res) => {
          this.isLoading = false;
          this.router.navigate(['/verify-email']);
        },
        error: (err) => {
          this.isLoading = false;
          this.errorMessage = err.error || 'Registration failed. Please try again.';
        }
      });
    }
  }
}