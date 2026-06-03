import { Component, OnInit, inject } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule, DatePipe } from '@angular/common';
import { AuthService } from '../../services/auth.service';
import { AdminService } from '../../services/admin.service';

import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { TagModule } from 'primeng/tag';
import { ToastModule } from 'primeng/toast';
import { TabViewModule } from 'primeng/tabview'; 
import { DialogModule } from 'primeng/dialog'; // <-- ADDED
import { MessageService } from 'primeng/api';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, DatePipe, TableModule, ButtonModule, TagModule, ToastModule, TabViewModule, DialogModule],
  providers: [MessageService],
  templateUrl: './admin-dashboard.component.html'
})
export class AdminDashboardComponent implements OnInit {
  private authService = inject(AuthService);
  private adminService = inject(AdminService);
  private router = inject(Router);
  private messageService = inject(MessageService);

  pendingUsers: any[] = [];
  allUsers: any[] = [];
  isLoading: boolean = true;

  // NEW: Targeted Logs State
  userLogs: any[] = [];
  showLogsDialog: boolean = false;
  isLoadingLogs: boolean = false;
  selectedUserForLogs: any = null;

  ngOnInit() {
    this.loadUsers();
  }

  loadUsers() {
    this.isLoading = true;
    
    this.adminService.getPendingUsers().subscribe({
      next: (data) => this.pendingUsers = data,
      error: (err) => console.error(err)
    });

    this.adminService.getAllUsers().subscribe({
      next: (data) => {
        this.allUsers = data;
        this.isLoading = false;
      },
      error: (err) => {
        console.error(err);
        this.isLoading = false;
      }
    });
  }

  verifyUser(userId: number) {
    this.adminService.verifyUser(userId).subscribe({
      next: (res) => {
        this.messageService.add({ severity: 'success', summary: 'Verified', detail: res });
        this.loadUsers(); 
      },
      error: (err) => {
        this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Failed to verify user.' });
      }
    });
  }

  // --- NEW: TARGETED LOG FETCH ---
  viewLogs(user: any) {
    this.selectedUserForLogs = user;
    this.showLogsDialog = true;
    this.isLoadingLogs = true;
    this.userLogs = [];

    this.adminService.getUserLogs(user.userId).subscribe({
      next: (data) => {
        this.userLogs = data;
        this.isLoadingLogs = false;
      },
      error: (err) => {
        this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Could not fetch logs.' });
        this.isLoadingLogs = false;
      }
    });
  }

  impersonate(userId: number) {
    this.adminService.impersonateUser(userId).subscribe({
      next: (res) => {
        const currentToken = localStorage.getItem('jwt_token');
        if (currentToken) {
          localStorage.setItem('admin_token', currentToken);
        }

        localStorage.setItem('jwt_token', res.token);
        localStorage.setItem('user_role', res.role);
        localStorage.setItem('user_status', res.status);
        localStorage.setItem('user_email', res.email);

        if (res.role === 'WORKER') {
          this.router.navigate(['/worker-dashboard']);
        } else {
          this.router.navigate(['/business-dashboard']);
        }
      },
      error: (err) => {
        this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Impersonation failed.' });
      }
    });
  }

  getLogSeverity(action: string): "success" | "secondary" | "info" | "warning" | "danger" | "contrast" {
    if (action.includes("APPROVED") || action.includes("ACTIVATION") || action.includes("SUCCESS")) return "success";
    if (action.includes("CANCEL") || action.includes("REJECT")) return "danger";
    if (action.includes("IMPERSONATION")) return "warning";
    return "info";
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}