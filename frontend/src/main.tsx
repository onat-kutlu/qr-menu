import React from 'react';
import ReactDOM from 'react-dom/client';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import { Toaster } from 'sonner';
import { AdminLayout } from './components/AdminLayout';
import { ProtectedRoute } from './components/ProtectedRoute';
import { PlatformRoute } from './components/PlatformRoute';
import { DashboardPage } from './pages/DashboardPage';
import { CompaniesPage } from './pages/CompaniesPage';
import { UsersPage } from './pages/UsersPage';
import { RestaurantsPage } from './pages/RestaurantsPage';
import { ApprovalsPage } from './pages/ApprovalsPage';
import { LoginPage } from './pages/LoginPage';
import { UnauthorizedPage } from './pages/UnauthorizedPage';
import { CompanyDashboardPage } from './pages/CompanyDashboardPage';
import { CompanyUsersPage } from './pages/CompanyUsersPage';
import { CompanyRestaurantsPage } from './pages/CompanyRestaurantsPage';
import { CompanyMenusPage } from './pages/CompanyMenusPage';
import { CompanyQrPage } from './pages/CompanyQrPage';
import { PublicLandingPage } from './pages/PublicLandingPage';
import { AnalyticsPage } from './pages/AnalyticsPage';
import './index.css';
import { LocaleProvider } from './lib/i18n';

const queryClient = new QueryClient({ defaultOptions: { queries: { staleTime: 30_000, retry: 1 } } });

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <QueryClientProvider client={queryClient}>
      <LocaleProvider>
      <BrowserRouter future={{ v7_startTransition: true, v7_relativeSplatPath: true }}>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/qr/menu/:slug" element={<PublicLandingPage />} />
          <Route path="/unauthorized" element={<UnauthorizedPage />} />
          <Route element={<ProtectedRoute />}>
            <Route element={<AdminLayout />}>
              <Route path="/dashboard/company" element={<CompanyDashboardPage />} />
              <Route path="/dashboard/company/users" element={<CompanyUsersPage />} />
              <Route path="/dashboard/company/restaurants" element={<CompanyRestaurantsPage />} />
              <Route path="/dashboard/company/menus" element={<CompanyMenusPage />} />
              <Route path="/dashboard/company/qr" element={<CompanyQrPage />} />
              <Route element={<PlatformRoute />}>
                <Route path="/dashboard" element={<DashboardPage />} />
                <Route path="/dashboard/companies" element={<CompaniesPage />} />
                <Route path="/dashboard/users" element={<UsersPage />} />
                <Route path="/dashboard/restaurants" element={<RestaurantsPage />} />
                <Route path="/dashboard/approvals" element={<ApprovalsPage />} />
                <Route path="/dashboard/analytics" element={<AnalyticsPage />} />
              </Route>
            </Route>
          </Route>
          <Route path="*" element={<Navigate to="/dashboard" replace />} />
        </Routes>
      </BrowserRouter>
      <Toaster theme="dark" position="top-right" />
      </LocaleProvider>
    </QueryClientProvider>
  </React.StrictMode>,
);
