export const CORE_API_ENDPOINTS = {
  complianceCalendar: 'GET /api/compliance/calendar',
  complianceProfile: 'GET /api/compliance/profile',
  complianceScore: 'GET /api/compliance/score',
  createDriver: 'POST /api/drivers',
  createTruck: 'POST /api/trucks',
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

export type CurrencyCode = 'BRL';

export type DriverProfileRequest = {
  active: boolean;
  cpfCnpjLast4: string;
  documentType: 'CPF' | 'CNPJ';
  email: string;
  name: string;
  phone: string;
  rntrcCategory: 'TAC' | 'ETC' | 'CTC' | 'UNKNOWN';
  rntrcNumber: string;
  rntrcStatus: 'ACTIVE' | 'INACTIVE' | 'SUSPENDED' | 'EXPIRED' | 'UNKNOWN';
  state: string;
};

export type DriverProfileResponse = DriverProfileRequest & {
  id: string;
};

export type TruckProfileRequest = {
  active: boolean;
  axleCount: number;
  fuelType: 'DIESEL_S10' | 'DIESEL_S500';
  plate: string;
  renavamLast4: string;
  state: string;
  vehicleType: 'TRUCK';
};

export type TruckProfileResponse = TruckProfileRequest & {
  id: string;
};

export type RoutePoint = {
  city: string;
  state: string;
};

export type TripRequest = {
  advanceAmount: string;
  balanceDueDays: number;
  cargoDescription: string;
  currency: CurrencyCode;
  destination: RoutePoint;
  emptyKm: string;
  expectedPickupAt?: string;
  grossFreight: string;
  loadedKm: string;
  origin: RoutePoint;
  truckId: string;
};

export type TripResponse = TripRequest & {
  id: string;
  decisionState: 'DRAFT' | 'ACCEPTED' | 'REJECTED' | 'RENEGOTIATION';
  inputRevision: number;
  latestSnapshotId: string | null;
  profitabilityEstimatePath: string;
  profitabilitySnapshotPath: string;
  totalDistanceKm: string;
};

export type ReserveWalletSummary = {
  availableForWithdrawal: string;
  bucketCount: number;
  currency: CurrencyCode;
};

export type ReservePolicyRequest = {
  emergencyRate: string;
  insuranceRate: string;
  maintenanceRate: string;
  replacementRate: string;
  taxRate: string;
  tireRate: string;
};

export type ProfitabilityEstimateRequest = {
  arlaCost: string;
  dieselConsumptionKmPerLiter: string;
  dieselPricePerLiter: string;
  financingAllocation: string;
  mealsAndLodgingCost: string;
  nonReimbursedToll: string;
  otherDirectCost: string;
  otherPassThrough: string;
  reservePolicy: ReservePolicyRequest;
  sourceFreshness: Record<'compliance' | 'fuel' | 'tax' | 'toll', 'CURRENT' | 'STALE' | 'FAILED' | 'UNKNOWN'>;
  tollReimbursement: string;
  valePedagio: string;
};

export type SourceMetadata = {
  area: string;
  freshnessStatus: 'CURRENT' | 'STALE' | 'FAILED' | 'UNKNOWN';
};

export type ProfitabilityEstimateResponse = {
  calculationTraceId: string;
  caveats: string[];
  currency: CurrencyCode;
  directTripCost: string;
  expectedProfit: string;
  financialHealthStatus: 'GOOD' | 'WARNING' | 'RISK';
  grossFreight: string;
  marginPercent: string;
  passThroughAmount: string;
  recommendation: 'ACCEPT' | 'RENEGOTIATE' | 'REJECT';
  requiredReserves: string;
  safePersonalWithdrawal: string;
  sourceMetadata: SourceMetadata[];
  tripId: string;
};
