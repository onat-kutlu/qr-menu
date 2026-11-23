import { describe, expect, it, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { Pager, ResourceTable } from './ResourceTable';
import { LocaleProvider } from '../lib/i18n';

describe('ResourceTable primitives', () => {
  it('renders empty state when there are no records', () => {
    render(<LocaleProvider><ResourceTable headers={['Name', 'Status']} empty>Record</ResourceTable></LocaleProvider>);
    expect(screen.getByText('No records found.')).toBeInTheDocument();
  });

  it('moves to the next page when enabled', async () => {
    const user = userEvent.setup();
    const onPage = vi.fn();
    render(<LocaleProvider><Pager page={0} totalPages={3} onPage={onPage} /></LocaleProvider>);
    await user.click(screen.getByRole('button', { name: 'Next page' }));
    expect(onPage).toHaveBeenCalledWith(1);
  });
});
