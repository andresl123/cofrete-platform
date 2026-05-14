import {
  type ComplianceCalendarItemResponse,
  type ComplianceDocumentRequest,
  type ComplianceDocumentResponse,
  type ComplianceProfileResponse,
  type ComplianceScoreResponse,
  type DriverProfileRequest,
  type DriverProfileResponse,
  type FinancialHealthResponse,
  type InsurancePolicyRequest,
  type InsurancePolicyResponse,
  type ProfitabilityEstimateRequest,
  type ProfitabilityEstimateResponse,
  type ReceivableStatus,
  type ReceivablesEnvelope,
  type ReserveAllocationRequest,
  type ReserveAllocationResponse,
  type ReserveRuleRequest,
  type ReserveRuleResponse,
  type ReserveWalletResponse,
  type RntrcMetadataRequest,
  type RntrcProfileResponse,
  type TripRequest,
  type TripResponse,
  type TruckProfileRequest,
  type TruckProfileResponse,
} from './contracts';

type RequestOptions = {
  body?: unknown;
  method: 'GET' | 'POST' | 'PUT';
  path: string;
};

export type CoreApiClient = {
  baseUrl: string;
  createDocument: (draft: ComplianceDocumentRequest) => Promise<ComplianceDocumentResponse>;
  createDriverProfile: (draft: DriverProfileRequest) => Promise<DriverProfileResponse>;
  createInsurancePolicy: (draft: InsurancePolicyRequest) => Promise<InsurancePolicyResponse>;
  createReserveAllocation: (draft: ReserveAllocationRequest) => Promise<ReserveAllocationResponse>;
  createReserveRule: (draft: ReserveRuleRequest) => Promise<ReserveRuleResponse>;
  createTrip: (draft: TripRequest) => Promise<TripResponse>;
  createTruckProfile: (draft: TruckProfileRequest) => Promise<TruckProfileResponse>;
  getComplianceCalendar: () => Promise<ComplianceCalendarItemResponse[]>;
  getComplianceProfile: () => Promise<ComplianceProfileResponse>;
  getComplianceScore: () => Promise<ComplianceScoreResponse>;
  getDocuments: () => Promise<ComplianceDocumentResponse[]>;
  getFinancialHealthScore: () => Promise<FinancialHealthResponse>;
  getInsurancePolicies: () => Promise<InsurancePolicyResponse[]>;
  getReceivables: (status?: 'overdue' | ReceivableStatus) => Promise<ReceivablesEnvelope>;
  getReserveWallets: () => Promise<ReserveWalletResponse[]>;
  getTrip: (tripId: string) => Promise<TripResponse>;
  requestProfitabilityEstimate: (
    tripId: string,
    draft: ProfitabilityEstimateRequest
  ) => Promise<ProfitabilityEstimateResponse>;
  updateRntrc: (draft: RntrcMetadataRequest) => Promise<RntrcProfileResponse>;
};

export function createCoreApiClient(baseUrl: string): CoreApiClient {
  const normalizedBaseUrl = baseUrl.replace(/\/$/, '');

  async function request<T>({ body, method, path }: RequestOptions): Promise<T> {
    const response = await fetch(`${normalizedBaseUrl}${path}`, {
      body: body ? JSON.stringify(body) : undefined,
      headers: {
        Accept: 'application/json',
        'Content-Type': 'application/json',
      },
      method,
    });

    if (!response.ok) {
      throw new Error(`Core API request failed: ${method} ${path} ${response.status}`);
    }

    return response.json() as Promise<T>;
  }

  return {
    baseUrl: normalizedBaseUrl,
    createDocument: (draft) =>
      request<{ document: ComplianceDocumentResponse }>({
        body: draft,
        method: 'POST',
        path: '/api/documents',
      }).then((envelope) => envelope.document),
    createDriverProfile: (draft) =>
      request<{ driver: DriverProfileResponse }>({
        body: draft,
        method: 'POST',
        path: '/api/drivers',
      }).then((envelope) => envelope.driver),
    createInsurancePolicy: (draft) =>
      request<{ insurancePolicy: InsurancePolicyResponse }>({
        body: draft,
        method: 'POST',
        path: '/api/compliance/insurance-policies',
      }).then((envelope) => envelope.insurancePolicy),
    createReserveAllocation: (draft) =>
      request<{ reserveAllocation: ReserveAllocationResponse }>({
        body: draft,
        method: 'POST',
        path: '/api/reserve-allocations',
      }).then((envelope) => envelope.reserveAllocation),
    createReserveRule: (draft) =>
      request<{ reserveRule: ReserveRuleResponse }>({
        body: draft,
        method: 'POST',
        path: '/api/reserve-rules',
      }).then((envelope) => envelope.reserveRule),
    createTrip: (draft) =>
      request<{ trip: TripResponse }>({
        body: draft,
        method: 'POST',
        path: '/api/trips',
      }).then((envelope) => envelope.trip),
    createTruckProfile: (draft) =>
      request<{ truck: TruckProfileResponse }>({
        body: draft,
        method: 'POST',
        path: '/api/trucks',
      }).then((envelope) => envelope.truck),
    getFinancialHealthScore: () =>
      request<{ financialHealthScore: FinancialHealthResponse }>({
        method: 'GET',
        path: '/api/financial-health-score',
      }).then((envelope) => envelope.financialHealthScore),
    getReserveWallets: () =>
      request<{ reserveWallets: ReserveWalletResponse[] }>({
        method: 'GET',
        path: '/api/reserve-wallets',
      }).then((envelope) => envelope.reserveWallets),
    getComplianceCalendar: () =>
      request<{ calendarItems: ComplianceCalendarItemResponse[] }>({
        method: 'GET',
        path: '/api/compliance/calendar',
      }).then((envelope) => envelope.calendarItems),
    getComplianceProfile: () =>
      request<{ complianceProfile: ComplianceProfileResponse }>({
        method: 'GET',
        path: '/api/compliance/profile',
      }).then((envelope) => envelope.complianceProfile),
    getComplianceScore: () =>
      request<{ complianceScore: ComplianceScoreResponse }>({
        method: 'GET',
        path: '/api/compliance/score',
      }).then((envelope) => envelope.complianceScore),
    getDocuments: () =>
      request<{ documents: ComplianceDocumentResponse[] }>({
        method: 'GET',
        path: '/api/documents',
      }).then((envelope) => envelope.documents),
    getInsurancePolicies: () =>
      request<{ insurancePolicies: InsurancePolicyResponse[] }>({
        method: 'GET',
        path: '/api/compliance/insurance-policies',
      }).then((envelope) => envelope.insurancePolicies),
    getReceivables: (status) =>
      request<ReceivablesEnvelope>({
        method: 'GET',
        path: `/api/receivables${status ? `?status=${encodeURIComponent(status)}` : ''}`,
      }),
    getTrip: (tripId) =>
      request<{ trip: TripResponse }>({
        method: 'GET',
        path: `/api/trips/${tripId}`,
      }).then((envelope) => envelope.trip),
    requestProfitabilityEstimate: (tripId, draft) =>
      request<{ profitabilityEstimate: ProfitabilityEstimateResponse }>({
        body: draft,
        method: 'POST',
        path: `/api/trips/${tripId}/profitability-estimate`,
      }).then((envelope) => envelope.profitabilityEstimate),
    updateRntrc: (draft) =>
      request<{ rntrcProfile: RntrcProfileResponse }>({
        body: draft,
        method: 'PUT',
        path: '/api/compliance/rntrc',
      }).then((envelope) => envelope.rntrcProfile),
  };
}
