import { Component, OnInit, inject } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule, DatePipe } from '@angular/common';
import { AuthService } from '../../services/auth.service';
import { WorkerService } from '../../services/worker.service';

// PrimeNG imports
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { TagModule } from 'primeng/tag';
import { TabViewModule } from 'primeng/tabview';
import { CardModule } from 'primeng/card';
import { ToastModule } from 'primeng/toast';
import { MessageService } from 'primeng/api';

@Component({
  selector: 'app-worker-dashboard',
  standalone: true,
  imports: [
    CommonModule, DatePipe, TableModule, ButtonModule, 
    TagModule, TabViewModule, CardModule, ToastModule
  ],
  providers: [MessageService], // Required for Toast notifications
  templateUrl: './worker-dashboard.component.html'
})
export class WorkerDashboardComponent implements OnInit {
  private authService = inject(AuthService);
  private workerService = inject(WorkerService);
  private router = inject(Router);
  private messageService = inject(MessageService);

  mySchedule: any[] = [];
  availableJobs: any[] = [];
  isLoadingSchedule: boolean = true;
  isLoadingFeed: boolean = true;

  ngOnInit() {
    this.loadData();
  }

  loadData() {
    this.isLoadingSchedule = true;
    this.isLoadingFeed = true;

    this.workerService.getDashboard().subscribe({
      next: (data) => {
        this.mySchedule = data;
        this.isLoadingSchedule = false;
      },
      error: (err) => console.error(err)
    });

    this.workerService.getAvailableFeed().subscribe({
      next: (data) => {
        this.availableJobs = data;
        this.isLoadingFeed = false;
      },
      error: (err) => console.error(err)
    });
  }

  apply(requirementId: number) {
    this.workerService.applyForJob(requirementId).subscribe({
      next: (res) => {
        this.messageService.add({ severity: 'success', summary: 'Success', detail: 'Application sent!' });
        this.loadData(); // Refresh both lists
      },
      error: (err) => {
        this.messageService.add({ severity: 'error', summary: 'Error', detail: err.error || 'Failed to apply.' });
      }
    });
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  getAppSeverity(status: string): "success" | "secondary" | "info" | "warning" | "danger" | "contrast" {
    switch (status) {
      case 'PENDING': return 'warning';
      case 'SELECTED': return 'success';
      case 'REJECTED': return 'danger';
      case 'AUTO_CANCELLED': return 'secondary';
      default: return 'info';
    }
  }
}