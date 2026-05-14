export const CORE_API_ENDPOINTS = {
  complianceCalendar: 'GET /api/compliance/calendar',
  complianceInsurancePolicies: 'GET /api/compliance/insurance-policies',
  complianceProfile: 'GET /api/compliance/profile',
  complianceRntrc: 'PUT /api/compliance/rntrc',
  complianceScore: 'GET /api/compliance/score',
  documents: 'GET /api/documents',
  createDriver: 'POST /api/drivers',
  createTruck: 'POST /api/trucks',
  driverProfile: 'GET /api/drivers/me',
  financialHealthScore: 'GET /api/financial-health-score',
  register: 'POST /api/auth/register',
  receivablesOverdue: 'GET /api/receivables?status=overdue',
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

export type RntrcCategory = 'TAC' | 'ETC' | 'CTC' | 'UNKNOWN';
export type RntrcStatus = 'ACTIVE' | 'INACTIVE' | 'SUSPENDED' | 'EXPIRED' | 'CANCELLED' | 'UNKNOWN';
export type ComplianceItemStatus = 'CURRENT' | 'EXPIRING_SOON' | 'EXPIRED' | 'UNKNOWN';
export type ComplianceSeverity = 'INFO' | 'WARNING' | 'CRITICAL';
export type ComplianceScoreStatus = 'GOOD' | 'ATTENTION' | 'RISK';
export type InsurancePolicyType = 'RCTR_C' | 'RC_DC' | 'RC_V' | 'TRUCK_HULL' | 'LIFE_ACCIDENT' | 'OTHER';
export type VerificationStatus = 'VERIFIED_BY_DRIVER' | 'PENDING_REVIEW' | 'EXPIRED' | 'UNKNOWN';
export type ComplianceDocumentType = 'RNTRC' | 'INSURANCE_POLICY' | 'CNH' | 'CRLV' | 'IPVA' | 'LICENSING' | 'CIOT' | 'TAX' | 'OTHER';
export type ComplianceOwnerType = 'DRIVER' | 'TRUCK' | 'INSURANCE_POLICY' | 'OTHER';
export type ComplianceSource = 'DRIVER_ENTERED' | 'IMPORTED' | 'COFRETE_GENERATED';

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

export type RntrcProfileResponse = {
  advisoryText: string;
  category: RntrcCategory;
  guidance: string[];
  lastCheckedAt: string | null;
  numberMasked: string | null;
  officialActionUrl: string;
  source: string;
  status: RntrcStatus;
};

export type RntrcMetadataRequest = {
  ciotRequired: 'YES' | 'NO' | 'UNKNOWN';
  latestCiotStatus: 'RECORDED' | 'MISSING' | 'NOT_RECORDED' | 'UNKNOWN';
  rntrcCategory: RntrcCategory;
  rntrcNumber: string;
  rntrcStatus: RntrcStatus;
};

export type ComplianceScoreResponse = {
  caveats: string[];
  score: {
    components: {
      advisoryText: string;
      name: string;
      status: ComplianceItemStatus;
      weight: string;
    }[];
    snapshotAt: string;
    status: ComplianceScoreStatus;
    value: number;
  };
};

export type ComplianceAlertResponse = {
  advisoryText: string;
  alertType: string;
  dueOn: string | null;
  generatedAt: string;
  id: string;
  message: string;
  officialActionUrl: string | null;
  severity: ComplianceSeverity;
  status: ComplianceItemStatus;
  subjectId: string;
  subjectType: 'RNTRC' | 'INSURANCE_POLICY' | 'DOCUMENT';
};

export type ComplianceProfileResponse = {
  alerts: ComplianceAlertResponse[];
  caveats: string[];
  ciot: {
    advisoryText: string;
    latestTripStatus: string;
    required: string;
  };
  documents: {
    activeDocuments: number;
    advisoryText: string;
    expired: number;
    expiringSoon: number;
  };
  driverId: string;
  insurance: {
    activePolicies: number;
    advisoryText: string;
    expired: number;
    expiringSoon: number;
  };
  rntrc: RntrcProfileResponse;
  score: ComplianceScoreResponse;
};

export type ComplianceCalendarItemResponse = {
  advisoryText: string;
  dueOn: string;
  id: string;
  officialActionUrl: string | null;
  severity: ComplianceSeverity;
  status: ComplianceItemStatus;
  subjectId: string;
  subjectType: 'RNTRC' | 'INSURANCE_POLICY' | 'DOCUMENT';
  title: string;
};

export type InsurancePolicyRequest = {
  active: boolean;
  annualPremium: string;
  currency: CurrencyCode;
  expiresOn: string;
  insurer: string;
  linkedRntrc: boolean;
  monthlyReserve: string;
  pgrRequired: boolean;
  policyNumberLast4: string;
  policyType: InsurancePolicyType;
  startsOn: string;
  verificationStatus: VerificationStatus;
};

export type InsurancePolicyResponse = InsurancePolicyRequest & {
  advisoryText: string;
  driverId: string;
  id: string;
  policyNumberMasked: string | null;
  status: ComplianceItemStatus;
};

export type ComplianceDocumentRequest = {
  active: boolean;
  documentType: ComplianceDocumentType;
  expiresOn: string;
  identifierLast4: string;
  issuedOn: string;
  notes: string;
  ownerType: ComplianceOwnerType;
  source: ComplianceSource;
  title: string;
};

export type ComplianceDocumentResponse = ComplianceDocumentRequest & {
  advisoryText: string;
  driverId: string;
  id: string;
  identifierMasked: string | null;
  status: ComplianceItemStatus;
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

export type ReceivableStatus = 'EXPECTED' | 'LATE' | 'PARTIALLY_PAID' | 'PAID' | 'CANCELED';

export type ReceivableType = 'FREIGHT_BALANCE' | 'DETENTION' | 'OTHER';

export type ReceivablePaymentMethod = 'PIX' | 'BANK_TRANSFER' | 'CASH' | 'CARD' | 'OTHER';

export type ReceivablePaymentResponse = {
  amount: string;
  currency: CurrencyCode;
  id: string;
  note: string;
  paidDate: string;
  paymentMethod: ReceivablePaymentMethod;
};

export type ReceivableStatusChangeResponse = {
  changedAt: string;
  id: string;
  newStatus: ReceivableStatus;
  note: string;
  previousStatus: ReceivableStatus | null;
};

export type ReceivableResponse = {
  advisoryText: string;
  amount: string;
  currency: CurrencyCode;
  customerId: string;
  customerName: string;
  dueDate: string;
  id: string;
  invoiceReference: string | null;
  paidAmount: string;
  paymentMethod: ReceivablePaymentMethod;
  payments: ReceivablePaymentResponse[];
  remainingAmount: string;
  status: ReceivableStatus;
  statusChanges: ReceivableStatusChangeResponse[];
  tripId: string;
  type: ReceivableType;
};

export type ReceivableTotalsResponse = {
  currency: CurrencyCode;
  expectedAmount: string;
  lateAmount: string;
  paidAmount: string;
  partiallyPaidAmount: string;
};

export type ReceivablesEnvelope = {
  receivables: ReceivableResponse[];
  totals: ReceivableTotalsResponse;
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
