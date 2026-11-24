import { useEffect, useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { getCompanyRestaurants, getMyCompanies } from './api-client';
import { useAuthStore } from './auth-store';

export function useCompanyWorkspace() {
  const session = useAuthStore((state) => state.session);
  const companiesQuery = useQuery({ queryKey: ['my-companies'], queryFn: getMyCompanies });
  const companies = companiesQuery.data || [];
  const membershipCompanies = session?.companies || [];
  const [selectedCompanyId, setSelectedCompanyId] = useState<number | undefined>(membershipCompanies[0]?.id);

  useEffect(() => {
    if (companies.length > 0 && !companies.some((company) => company.id === selectedCompanyId)) {
      setSelectedCompanyId(companies[0].id);
    }
  }, [companies, selectedCompanyId]);

  const restaurantsQuery = useQuery({
    queryKey: ['my-company-restaurants', selectedCompanyId],
    queryFn: () => getCompanyRestaurants(selectedCompanyId as number),
    enabled: Boolean(selectedCompanyId),
  });
  const selectedMembership = membershipCompanies.find((company) => company.id === selectedCompanyId);
  const canManageCompany = session?.highestRole === 'ADMIN' || session?.highestRole === 'SUPER_ADMIN' || selectedMembership?.role === 'COMPANY_ADMIN';

  return {
    companies,
    companiesQuery,
    selectedCompanyId,
    setSelectedCompanyId,
    selectedMembership,
    restaurants: restaurantsQuery.data || [],
    restaurantsQuery,
    canManageCompany,
  };
}