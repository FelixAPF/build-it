import { Component, OnInit, inject } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule, DatePipe } from '@angular/common';
import { AuthService } from '../../services/auth.service';
import { AdminService } from '../../services/admin.service';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { TagModule } from 'primeng/tag';
import { ToastModule } from 'primeng/toast';
import { TabViewModule } from 'primeng/tabview'; 
import { DialogModule } from 'primeng/dialog'; // <-- ADDED
import { MessageService } from 'primeng/api';
import { DropdownModule } from 'primeng/dropdown';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, DatePipe, TableModule, ButtonModule, TagModule, ToastModule, TabViewModule, DialogModule, DropdownModule, ReactiveFormsModule, FormsModule],
  providers: [MessageService],
  templateUrl: './admin-dashboard.component.html'
})
export class AdminDashboardComponent implements OnInit {
  private authService = inject(AuthService);
  private adminService = inject(AdminService);
  private router = inject(Router);
  private messageService = inject(MessageService);
  private sanitizer = inject(DomSanitizer);

  pendingUsers: any[] = [];
  allUsers: any[] = [];
  isLoading: boolean = true;

  // NEW: Targeted Logs State
  userLogs: any[] = [];
  showLogsDialog: boolean = false;
  isLoadingLogs: boolean = false;
  isReviewMode: boolean = true; // <-- NEW FLAG
  selectedUserForLogs: any = null;

  selectedUserForReview: any = null;
  showReviewDialog: boolean = false;
  documentObjectUrl: SafeUrl | null = null;
  isPdf: boolean = false;

tradeQuestions: any[] = [];
  newQuestionText: string = '';
  selectedJobTypeForQuestion: string = 'ELECTRICIEN';

  jobTypes = [
    { label: 'Electrician', value: 'ELECTRICIEN' },
    { label: 'Plumber', value: 'PLOMBIER' },
    { label: 'Floor Layer', value: 'POSEUR_DE_PLANCHER' },
    { label: 'Carpenter', value: 'MENUISIER' },
    { label: 'Laborer', value: 'MANOEUVRE' }
  ];

  // Inside ngOnInit, add: this.loadTradeQuestions();

  loadTradeQuestions() {
    this.adminService.getTradeQuestions().subscribe(res => this.tradeQuestions = res);
  }

  addTradeQuestion() {
    if (!this.newQuestionText.trim()) return;
    this.adminService.addTradeQuestion({ jobType: this.selectedJobTypeForQuestion, questionText: this.newQuestionText }).subscribe(() => {
      this.messageService.add({severity: 'success', summary: 'Added', detail: 'Question added'});
      this.newQuestionText = '';
      this.loadTradeQuestions();
    });
  }

  deleteTradeQuestion(id: number) {
    this.adminService.deleteTradeQuestion(id).subscribe(() => {
      this.messageService.add({severity: 'success', summary: 'Deleted', detail: 'Question removed'});
      this.loadTradeQuestions();
    });
  }

  ngOnInit() {
    this.loadUsers();
    this.loadTradeQuestions();
  }

  

openReviewDialog(user: any, isReviewMode: boolean = true) {
    this.selectedUserForReview = user;
    this.showReviewDialog = true;
    this.isReviewMode = isReviewMode; // <-- SET THE FLAG
    this.documentObjectUrl = null;

    if (user.documentUrl) {
      this.isPdf = user.documentUrl.toLowerCase().endsWith('.pdf');
      
      this.adminService.getDocumentAsBlob(user.documentUrl).subscribe({
        next: (blob) => {
          const mimeType = this.isPdf ? 'application/pdf' : blob.type;
          const typedBlob = new Blob([blob], { type: mimeType });
          
          const objectUrl = URL.createObjectURL(typedBlob);
          this.documentObjectUrl = this.sanitizer.bypassSecurityTrustResourceUrl(objectUrl);
        },
        error: (err) => {
          this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Failed to securely load document.' });
        }
      });
    }
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
        this.showReviewDialog = false; // <-- Close modal
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