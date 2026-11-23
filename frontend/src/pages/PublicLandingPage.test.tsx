import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { PublicLandingPage } from './PublicLandingPage';
import { LocaleProvider } from '../lib/i18n';

vi.mock('../lib/api-client', () => ({
  getApiErrorMessage: () => 'error',
  getPublicRestaurant: vi.fn().mockResolvedValue({
    id: 1,
    name: 'Sunset Cafe',
    slug: 'sunset-cafe',
    address: 'Uzun bir İstanbul adresi, Kadikoy',
    phone: '+90 555 123 45 67',
    languages: ['tr', 'en'],
    status: 'ACTIVE',
  }),
}));

describe('PublicLandingPage', () => {
  it('renders initials, live languages, phone link and same-tab PDF links', async () => {
    const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
    render(<LocaleProvider><QueryClientProvider client={client}><MemoryRouter initialEntries={['/qr/menu/sunset-cafe']} future={{ v7_startTransition: true, v7_relativeSplatPath: true }}><Routes><Route path="/qr/menu/:slug" element={<PublicLandingPage />} /></Routes></MemoryRouter></QueryClientProvider></LocaleProvider>);

    expect(await screen.findByText('SC')).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /TR/ })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /EN/ })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /\+90 555/ })).toHaveAttribute('href', 'tel:+90 555 123 45 67');
    expect(screen.getByRole('link', { name: /TR/ })).toHaveAttribute('href', expect.stringContaining('/api/public/menus/sunset-cafe/pdf?lang=tr'));
    expect(screen.getByRole('link', { name: /EN/ })).not.toHaveAttribute('target');
  });
});
