import axios from 'axios';
import type { AdminUser, ApiResponse, Company, DashboardSummary, LoginResponse, MenuApproval, PageResponse, PublicRestaurant, QrCode, Restaurant, SlugChange } from './types';

export const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  headers: { 'Content-Type': 'application/json' },
});

apiClient.interceptors.request.use((config) => {
  const raw = sessionStorage.getItem('qr-menu-admin-auth');
  if (raw) {
    try {
      const session = JSON.parse(raw) as LoginResponse;
      if (session.token) {
        config.headers.Authorization = `Bearer ${session.token}`;
      }
    } catch {
      sessionStorage.removeItem('qr-menu-admin-auth');
    }
  }
  return config;
});

apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error?.response?.status === 401) {
      sessionStorage.removeItem('qr-menu-admin-auth');
      if (window.location.pathname !== '/login') {
        window.location.assign('/login');
      }
    }
    return Promise.reject(error);
  },
);

export function getApiErrorMessage(error: unknown, fallback = 'Operation could not be completed.') {
  const response = (error as { response?: { data?: { message?: string } } })?.response;
  return response?.data?.message || fallback;
}

export async function login(email: string, password: string) {
  const response = await apiClient.post<ApiResponse<LoginResponse>>('/auth/login', { email, password });
  return response.data.data;
}

export async function getDashboard() {
  const response = await apiClient.get<ApiResponse<DashboardSummary>>('/admin/dashboard');
  return response.data.data;
}

async function getPage<T>(path: string, params: Record<string, string | number | boolean | undefined>) {
  const response = await apiClient.get<ApiResponse<PageResponse<T>>>(path, { params });
  return response.data.data;
}

export const getCompanies = (page: number, search: string) =>
  getPage<Company>('/admin/companies', { page, size: 20, search: search || undefined });

export const getUsers = (page: number, search: string) =>
  getPage<AdminUser>('/admin/users', { page, size: 20, search: search || undefined });

export const getRestaurants = (page: number, search: string, status: string) =>
  getPage<Restaurant>('/admin/restaurants', { page, size: 20, search: search || undefined, status: status || undefined });

export const getMenuApprovals = (page: number, status: string) =>
  getPage<MenuApproval>('/admin/pdf-approvals', { page, size: 20, status: status || undefined });

export const getRestaurantApprovals = (page: number) => getPage<Restaurant>('/admin/restaurants/approvals', { page, size: 20 });
export const getSlugChanges = (page: number) => getPage<SlugChange>('/admin/slug-change-requests', { page, size: 20 });

export const createCompany = (payload: { name: string; slug: string; description?: string }) => apiClient.post<ApiResponse<Company>>('/admin/companies', payload).then((response) => response.data.data);
export const updateCompanyStatus = (id: number, status: string) => apiClient.patch<ApiResponse<Company>>(`/admin/companies/${id}/status`, { status }).then((response) => response.data.data);
export const createUser = (payload: { email: string; password: string; firstName?: string; lastName?: string; phone?: string; companyId: number; role: string }) => apiClient.post<ApiResponse<AdminUser>>('/admin/users', payload).then((response) => response.data.data);
export const deactivateUser = (id: number) => apiClient.patch<ApiResponse<AdminUser>>(`/admin/users/${id}/deactivate`).then((response) => response.data.data);
export const updateUserRole = (id: number, companyId: number, role: string) => apiClient.patch<ApiResponse<AdminUser>>(`/admin/users/${id}/role`, { companyId, role }).then((response) => response.data.data);
export const updateRestaurant = (id: number, payload: { status?: string; monthlyUploadQuota?: number }) => apiClient.patch<ApiResponse<Restaurant>>(`/admin/restaurants/${id}`, payload).then((response) => response.data.data);
export const approveRestaurant = (id: number) => apiClient.post<ApiResponse<Restaurant>>(`/admin/restaurants/approvals/${id}/approve`).then((response) => response.data.data);
export const rejectRestaurant = (id: number, reason: string) => apiClient.post<ApiResponse<Restaurant>>(`/admin/restaurants/approvals/${id}/reject`, { reason }).then((response) => response.data.data);
export const approveMenu = (id: number) => apiClient.post<ApiResponse<MenuApproval>>(`/admin/pdf-approvals/${id}/approve`).then((response) => response.data.data);
export const publishMenu = (id: number) => apiClient.post<ApiResponse<MenuApproval>>(`/menus/${id}/publish`).then((response) => response.data.data);
export const previewMenu = (id: number) => apiClient.get(`/admin/pdf-approvals/${id}/preview`, { responseType: 'blob' }).then((response) => URL.createObjectURL(response.data));
export const rejectMenu = (id: number, category: string, reason: string) => apiClient.post<ApiResponse<MenuApproval>>(`/admin/pdf-approvals/${id}/reject`, { category, reason }).then((response) => response.data.data);
export const approveSlugChange = (id: number) => apiClient.post<ApiResponse<SlugChange>>(`/admin/slug-change-requests/${id}/approve`).then((response) => response.data.data);

export const getMyCompanies = () => apiClient.get<ApiResponse<Company[]>>('/companies').then((response) => response.data.data);
export const getCompanyUsers = (companyId: number) => apiClient.get<ApiResponse<AdminUser[]>>(`/companies/${companyId}/users`).then((response) => response.data.data);
export const createCompanyUser = (companyId: number, payload: { email: string; password: string; firstName?: string; lastName?: string; phone?: string }) => apiClient.post<ApiResponse<AdminUser>>(`/companies/${companyId}/users`, payload).then((response) => response.data.data);
export const getCompanyRestaurants = (companyId: number) => apiClient.get<ApiResponse<Restaurant[]>>(`/restaurants/company/${companyId}`).then((response) => response.data.data);
export const createRestaurant = (payload: import('./types').CreateRestaurantPayload) => apiClient.post<ApiResponse<Restaurant>>('/restaurants', payload).then((response) => response.data.data);
export const getMenusForRestaurant = (restaurantId: number) => apiClient.get<ApiResponse<MenuApproval[]>>(`/menus/restaurant/${restaurantId}`).then((response) => response.data.data);
export const uploadMenuFile = (restaurantId: number, language: string, file: File) => {
  const formData = new FormData();
  formData.append('restaurantId', String(restaurantId));
  formData.append('language', language);
  formData.append('file', file);
  return apiClient.post<ApiResponse<MenuApproval>>('/menus/upload', formData, { headers: { 'Content-Type': 'multipart/form-data' } }).then((response) => response.data.data);
};
export const getRestaurantQr = (restaurantId: number, format = 'png') => apiClient.get<ApiResponse<QrCode>>(`/restaurants/${restaurantId}/qr`, { params: { format } }).then((response) => response.data.data);
export const getPublicRestaurant = (slug: string, visitId: string) => apiClient.get<ApiResponse<PublicRestaurant>>(`/public/menus/${encodeURIComponent(slug)}`, { params: { visitId } }).then((response) => response.data.data);
