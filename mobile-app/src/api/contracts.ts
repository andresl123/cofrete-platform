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

export type ReserveBucket =
  | 'FUEL_ARLA_TOLL_CASH_FLOW'
  | 'MAINTENANCE'
  | 'TIRES'
  | 'INSURANCE'
  | 'TAXES_AND_DOCUMENTS'
  | 'TRUCK_REPLACEMENT'
  | 'EMERGENCY'
  | 'DRIVER_SALARY'
  | 'PROFIT';

export type ReserveRulePolicy = 'PERCENT_OF_AMOUNT' | 'FIXED_AMOUNT' | 'PER_KM';

export type ReserveAllocationReason = 'FREIGHT_PAYMENT_RECEIVED' | 'MANUAL_CORRECTION';

export type ReserveRuleRequest = {
  active: boolean;
  bucket: ReserveBucket;
  currency: CurrencyCode;
  effectiveFrom?: string;
  policy: ReserveRulePolicy;
  rate?: string;
  sourceAssumption: string;
  targetBalance?: string;
};

export type ReserveRuleResponse = ReserveRuleRequest & {
  id: string;
};

export type ReserveTransactionResponse = {
  amount: string;
  balanceAfter: string;
  bucket: ReserveBucket;
  createdAt: string;
  currency: CurrencyCode;
  id: string;
  note: string;
  sourceReference: string;
  sourceType: string;
  type: 'CREDIT' | 'DEBIT' | 'ADJUSTMENT';
};

export type ReserveWalletResponse = {
  bucket: ReserveBucket;
  currency: CurrencyCode;
  currentBalance: string;
  lastAllocationAt: string | null;
  policy: ReserveRulePolicy | null;
  targetBalance: string | null;
  transactions: ReserveTransactionResponse[];
};

export type ReserveAllocationRequest = {
  allocationRevision: number;
  allocationSubjectId: string;
  currency: CurrencyCode;
  distanceKm?: string;
  grossAmount: string;
  idempotencyKey: string;
  passThroughAmount: string;
  reason: ReserveAllocationReason;
  requestedAt: string;
};

export type ReserveAllocationResponse = {
  advisoryText: string;
  allocatableAmount: string;
  bucketAllocations: Partial<Record<ReserveBucket, string>>;
  currency: CurrencyCode;
  duplicate: boolean;
  grossAmount: string;
  id: string;
  idempotencyKey: string;
  passThroughAmount: string;
  requestStatus: 'REQUESTED' | 'ALLOCATED' | 'DUPLICATE_IGNORED';
  requiredReserveAmount: string;
  safePersonalWithdrawal: string;
  status: 'REQUESTED' | 'ALLOCATED' | 'FAILED';
  transactions: ReserveTransactionResponse[];
};

export type FinancialHealthStatus = 'GOOD' | 'ATTENTION' | 'RISK' | 'UNKNOWN';

export type FinancialHealthComponentResponse = {
  bucket: ReserveBucket;
  coveragePercent: string;
  currentBalance: string;
  status: FinancialHealthStatus;
  targetBalance: string | null;
};

export type FinancialHealthResponse = {
  advisoryText: string;
  components: FinancialHealthComponentResponse[];
  currency: CurrencyCode;
  reserveCoveragePercent: string;
  safePersonalWithdrawalAvailable: string;
  score: number;
  status: FinancialHealthStatus;
  traceId: string;
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
