import { describe, expect, it } from 'vitest';
import { isPlatformAdmin } from './auth-store';

describe('admin role policy', () => {
  it('allows only platform admin roles into the global panel', () => {
    expect(isPlatformAdmin('SUPER_ADMIN')).toBe(true);
    expect(isPlatformAdmin('ADMIN')).toBe(true);
    expect(isPlatformAdmin('COMPANY_ADMIN')).toBe(false);
    expect(isPlatformAdmin('COMPANY_USER')).toBe(false);
  });
});
