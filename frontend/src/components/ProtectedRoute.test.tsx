import { describe, expect, it, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { ProtectedRoute } from './ProtectedRoute';
import { useAuthStore } from '../lib/auth-store';

function renderRoute() {
  return render(
    <MemoryRouter initialEntries={['/dashboard']} future={{ v7_startTransition: true, v7_relativeSplatPath: true }}>
      <Routes>
        <Route path="/login" element={<p>login</p>} />
        <Route path="/unauthorized" element={<p>unauthorized</p>} />
        <Route element={<ProtectedRoute />}>
          <Route path="/dashboard" element={<p>dashboard</p>} />
        </Route>
      </Routes>
    </MemoryRouter>,
  );
}

describe('ProtectedRoute', () => {
  beforeEach(() => {
    sessionStorage.clear();
    useAuthStore.setState({ session: null });
  });

  it('redirects anonymous users to login', () => {
    renderRoute();
    expect(screen.getByText('login')).toBeInTheDocument();
  });

  it('allows company-only roles into the authenticated shell', () => {
    useAuthStore.setState({
      session: {
        token: 'token', userId: 1, email: 'company@example.com', highestRole: 'COMPANY_ADMIN',
        companies: [{ id: 1, name: 'Acme', role: 'COMPANY_ADMIN' }], expiresIn: 1000,
      },
    });
    renderRoute();
    expect(screen.getByText('dashboard')).toBeInTheDocument();
  });

  it('allows platform admins into the protected route', () => {
    useAuthStore.setState({
      session: {
        token: 'token', userId: 1, email: 'admin@example.com', highestRole: 'ADMIN',
        companies: [], expiresIn: 1000,
      },
    });
    renderRoute();
    expect(screen.getByText('dashboard')).toBeInTheDocument();
  });
});
