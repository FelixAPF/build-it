import { Routes } from '@angular/router';
import { LoginComponent } from './pages/login/login.component';
import { RegisterComponent } from './pages/register/register.component';
import { WorkerDashboardComponent } from './pages/worker-dashboard/worker-dashboard.component';
import { BusinessDashboardComponent } from './pages/business-dashboard/business-dashboard.component';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'worker-dashboard', component: WorkerDashboardComponent },
  { path: 'business-dashboard', component: BusinessDashboardComponent }
];