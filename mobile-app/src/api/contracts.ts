export const CORE_API_ENDPOINTS = {
  complianceCalendar: 'GET /api/compliance/calendar',
  complianceProfile: 'GET /api/compliance/profile',
  complianceScore: 'GET /api/compliance/score',
  createDriver: 'POST /api/drivers',
  driverProfile: 'GET /api/drivers/me',
  financialHealthScore: 'GET /api/financial-health-score',
  register: 'POST /api/auth/register',
  reserveAllocations: 'POST /api/reserve-allocations',
  reserveRules: 'POST /api/reserve-rules',
  reserveWallets: 'GET /api/reserve-wallets',
  tripProfitabilityEstimate: (tripId: string) =>
    `POST /api/trips/${tripId}/profitability-estimate`,
  tripProfitabilitySnapshot: (tripId: string) =>
    `GET /api/trips/${tripId}/profitability-snapshot`,
  trips: 'POST /api/trips',
  trucks: 'GET /api/trucks',
} as const;

export type DriverProfileDraft = {
  displayName: string;
  taxProfile: 'MEI_CAMINHONEIRO' | 'PESSOA_FISICA' | 'UNKNOWN';
};

export type TripDraft = {
  distanceKm: number;
  grossFreight: string;
  tollTreatment: 'DRIVER_PAID' | 'REIMBURSED' | 'VALE_PEDAGIO' | 'UNKNOWN';
};

export type ReserveWalletSummary = {
  availableForWithdrawal: string;
  bucketCount: number;
  currency: 'BRL';
};
