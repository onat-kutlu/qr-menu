export type CompanyInfo = {
  id: number;
  name: string;
  role: string;
};

export type LoginResponse = {
  token: string;
  userId: number;
  email: string;
  firstName?: string;
  lastName?: string;
  highestRole?: string;
  companies: CompanyInfo[];
  expiresIn: number;
};

export type DashboardSummary = {
  totalCompanies: number;
  totalRestaurants: number;
  activeRestaurants: number;
  pendingRestaurants: number;
  pendingMenus: number;
  todayLandingViews: number;
  todayMenuOpens: number;
  topRestaurants: Array<{
    restaurantId: number;
    restaurantName: string;
    views: number;
  }>;
  auditLogs: Array<{
    id: number;
    userEmail?: string;
    action: string;
    status: string;
    description?: string;
    createdAt?: string;
  }>;
};

export type ApiResponse<T> = {
  success: boolean;
  message: string;
  data: T;
  code?: string;
};

export type PageResponse<T> = {
  items: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};

export type Company = {
  id: number;
  name: string;
  slug: string;
  description?: string;
  status: string;
  createdAt?: string;
  updatedAt?: string;
};

export type CreateRestaurantPayload = {
  companyId: number;
  name: string;
  slug: string;
  description?: string;
  address?: string;
  phone?: string;
};

export type AdminUser = {
  id: number;
  email: string;
  fullName?: string;
  globalRole?: string;
  isActive: boolean;
  emailVerified: boolean;
  companies: CompanyInfo[];
  createdAt?: string;
};

export type Restaurant = {
  id: number;
  companyId: number;
  name: string;
  slug: string;
  status: string;
  monthlyUploadQuota: number;
  monthlyUploadsUsed: number;
  createdAt?: string;
};

export type MenuApproval = {
  id: number;
  restaurantId: number;
  language: string;
  filePath: string;
  fileSizeBytes?: number;
  status: string;
  versionNumber: number;
  createdAt?: string;
};

export type RestaurantApproval = Restaurant;

export type SlugChange = {
  id: number;
  restaurantId: number;
  currentSlug: string;
  requestedSlug: string;
  status: string;
  createdAt?: string;
};

export type QrCode = {
  format: string;
  content: string;
  data: string;
};

export type PublicRestaurant = {
  id: number;
  name: string;
  slug: string;
  address?: string;
  phone?: string;
  logoPath?: string;
  openUrl?: string;
  openUrls?: Record<string, string>;
  languages: string[];
  redirectUrl?: string;
  status: string;
};
