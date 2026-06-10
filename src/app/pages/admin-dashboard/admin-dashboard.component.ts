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
import { DialogModule } from 'primeng/dialog'; 
import { MessageService } from 'primeng/api';
import { DropdownModule } from 'primeng/dropdown';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { InputTextModule } from 'primeng/inputtext';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { ConfirmDialogModule } from 'primeng/confirmdialog';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, DatePipe, TableModule, ButtonModule, TagModule, ToastModule, ConfirmDialogModule, TranslatePipe, TabViewModule, DialogModule, DropdownModule, ReactiveFormsModule, FormsModule, InputTextModule],
  providers: [MessageService],
  templateUrl: './admin-dashboard.component.html'
})
export class AdminDashboardComponent implements OnInit {
  private authService = inject(AuthService);
  private adminService = inject(AdminService);
  private router = inject(Router);
  private messageService = inject(MessageService);
  private sanitizer = inject(DomSanitizer);
  private translate = inject(TranslateService);

  pendingUsers: any[] = [];
  allUsers: any[] = [];
  isLoading: boolean = true;
  newTradeLabelEn: string = '';
  newTradeLabelFr: string = '';
  newTradeValue: string = '';
  systemMetrics: { key: string, value: number, icon: string, colorClass: string, bgClass: string }[] = [];

  userLogs: any[] = [];
  showLogsDialog: boolean = false;
  isLoadingLogs: boolean = false;
  isReviewMode: boolean = true; 
  selectedUserForLogs: any = null;

  selectedUserForReview: any = null;
  showReviewDialog: boolean = false;
  documentObjectUrl: SafeUrl | null = null;
  isPdf: boolean = false;

  tradeQuestions: any[] = [];
  newQuestionText: string = '';
  selectedJobTypeForQuestion: string = '';

  public currentLang = localStorage.getItem('buildit_lang') || 'en';


  // NEW DYNAMIC TRADES
  jobTypes: any[] = [];
  trades: any[] = [];
  newTradeLabel: string = '';
  
  newQuestionTextEn: string = '';
  newQuestionTextFr: string = '';

  ngOnInit() {
    this.loadUsers();
    this.loadTrades();
    this.loadTradeQuestions();
    this.loadMetrics();
  }

  loadMetrics() {
    this.adminService.getMetrics().subscribe(data => {
      // By mapping it into an array, the HTML can just loop through it blindly!
      this.systemMetrics = [
        { key: 'TOTAL_BUSINESSES', value: data.totalBusinesses, icon: 'pi pi-briefcase', colorClass: 'text-blue-600', bgClass: 'bg-blue-100' },
        { key: 'TOTAL_WORKERS', value: data.totalWorkers, icon: 'pi pi-users', colorClass: 'text-emerald-600', bgClass: 'bg-emerald-100' },
        { key: 'TOTAL_JOBS', value: data.totalJobs, icon: 'pi pi-folder-open', colorClass: 'text-purple-600', bgClass: 'bg-purple-100' },
        { key: 'TOTAL_MATCHES', value: data.totalMatches, icon: 'pi pi-handshake', colorClass: 'text-amber-500', bgClass: 'bg-amber-100' },
        { key: 'TOTAL_CANCELLED', value: data.totalCancelled, icon: 'pi pi-times-circle', colorClass: 'text-red-500', bgClass: 'bg-red-100' }
      ];
    });
  }

    switchLanguage() {
    this.currentLang = this.currentLang === 'en' ? 'fr' : 'en';
    this.translate.use(this.currentLang);
    localStorage.setItem('buildit_lang', this.currentLang);
  }

  loadTrades() {
    this.authService.getTrades().subscribe(res => {
      this.trades = res;
      this.jobTypes = res;
      if (res.length > 0 && !this.selectedJobTypeForQuestion) {
        this.selectedJobTypeForQuestion = res[0].value;
      }
    });
  }

addTrade() {
    if (!this.newTradeLabelEn || !this.newTradeLabelFr || !this.newTradeValue) return;
    
    this.adminService.addTrade({ 
      labelEn: this.newTradeLabelEn, 
      labelFr: this.newTradeLabelFr, 
      value: this.newTradeValue.toUpperCase().replace(/\s+/g, '_') 
    }).subscribe(() => {
      this.messageService.add({severity: 'success', summary: 'Added', detail: 'Bilingual trade added successfully'});
      this.newTradeLabelEn = '';
      this.newTradeLabelFr = '';
      this.newTradeValue = '';
      this.loadTrades();
    });
  }

  deleteTrade(id: number) {
    this.adminService.deleteTrade(id).subscribe(() => {
      this.messageService.add({severity: 'success', summary: 'Deleted', detail: 'Trade removed'});
      this.loadTrades();
    });
  }

  loadTradeQuestions() {
    this.adminService.getTradeQuestions().subscribe(res => this.tradeQuestions = res);
  }

addTradeQuestion() {
    if (!this.newQuestionTextEn.trim() || !this.newQuestionTextFr.trim() || !this.selectedJobTypeForQuestion) return;
    
    // Send both languages to your backend
    this.adminService.addTradeQuestion({ 
      jobType: this.selectedJobTypeForQuestion, 
      questionEn: this.newQuestionTextEn,
      questionFr: this.newQuestionTextFr 
    }).subscribe(() => {
      this.messageService.add({severity: 'success', summary: 'Added', detail: 'Question added in both languages'});
      this.newQuestionTextEn = '';
      this.newQuestionTextFr = '';
      this.loadTradeQuestions();
    });
  }

  deleteTradeQuestion(id: number) {
    this.adminService.deleteTradeQuestion(id).subscribe(() => {
      this.messageService.add({severity: 'success', summary: 'Deleted', detail: 'Question removed'});
      this.loadTradeQuestions();
    });
  }

  openReviewDialog(user: any, isReviewMode: boolean = true) {
    this.selectedUserForReview = user;
    this.showReviewDialog = true;
    this.isReviewMode = isReviewMode;
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
        this.showReviewDialog = false; 
        this.loadUsers(); 
      },
      error: (err) => {
        this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Failed to verify user.' });
      }
    });
  }

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