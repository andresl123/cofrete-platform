import {
  type ComplianceCalendarItemResponse,
  type ComplianceDocumentRequest,
  type ComplianceDocumentResponse,
  type ComplianceProfileResponse,
  type ComplianceScoreResponse,
  type DriverProfileRequest,
  type DriverProfileResponse,
  type InsurancePolicyRequest,
  type InsurancePolicyResponse,
  type ProfitabilityEstimateRequest,
  type ProfitabilityEstimateResponse,
  type ReserveWalletSummary,
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
  createTrip: (draft: TripRequest) => Promise<TripResponse>;
  createTruckProfile: (draft: TruckProfileRequest) => Promise<TruckProfileResponse>;
  getComplianceCalendar: () => Promise<ComplianceCalendarItemResponse[]>;
  getComplianceProfile: () => Promise<ComplianceProfileResponse>;
  getComplianceScore: () => Promise<ComplianceScoreResponse>;
  getDocuments: () => Promise<ComplianceDocumentResponse[]>;
  getInsurancePolicies: () => Promise<InsurancePolicyResponse[]>;
  getReserveWallets: () => Promise<ReserveWalletSummary>;
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
    getReserveWallets: () =>
      request<ReserveWalletSummary>({
        method: 'GET',
        path: '/api/reserve-wallets',
      }),
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
