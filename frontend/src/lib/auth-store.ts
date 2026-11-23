import { create } from 'zustand';
import type { LoginResponse } from './types';

const AUTH_STORAGE_KEY = 'qr-menu-admin-auth';

type AuthState = {
  session: LoginResponse | null;
  setSession: (session: LoginResponse) => void;
  clearSession: () => void;
};

function readSession(): LoginResponse | null {
  try {
    const raw = sessionStorage.getItem(AUTH_STORAGE_KEY);
    return raw ? (JSON.parse(raw) as LoginResponse) : null;
  } catch {
    return null;
  }
}

export const useAuthStore = create<AuthState>((set) => ({
  session: readSession(),
  setSession: (session) => {
    sessionStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(session));
    set({ session });
  },
  clearSession: () => {
    sessionStorage.removeItem(AUTH_STORAGE_KEY);
    set({ session: null });
  },
}));

export const isPlatformAdmin = (role?: string) => role === 'SUPER_ADMIN' || role === 'ADMIN';
