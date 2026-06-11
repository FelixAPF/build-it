import { Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { BusinessService } from '../../services/business.service';
import { TranslatePipe } from '@ngx-translate/core';

@Component({
  selector: 'app-payment-success',
  standalone: true,
  imports: [CommonModule, RouterLink, TranslatePipe],
  templateUrl: './payment-success.component.html'
})
export class PaymentSuccessComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private businessService = inject(BusinessService);

  isLoading = true;
  isSuccess = false;

  ngOnInit() {
    const jobId = this.route.snapshot.queryParamMap.get('jobId');
    if (!jobId) {
      this.isLoading = false;
      return;
    }

    this.businessService.confirmPayment(Number(jobId)).subscribe({
      next: () => {
        this.isLoading = false;
        this.isSuccess = true;
      },
      error: () => {
        this.isLoading = false;
        this.isSuccess = false;
      }
    });
  }
}