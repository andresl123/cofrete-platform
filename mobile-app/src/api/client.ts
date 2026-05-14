import {
  type DriverProfileRequest,
  type DriverProfileResponse,
  type FinancialHealthResponse,
  type ProfitabilityEstimateRequest,
  type ProfitabilityEstimateResponse,
  type ReserveAllocationRequest,
  type ReserveAllocationResponse,
  type ReserveRuleRequest,
  type ReserveRuleResponse,
  type ReserveWalletResponse,
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
  createDriverProfile: (draft: DriverProfileRequest) => Promise<DriverProfileResponse>;
  createReserveAllocation: (draft: ReserveAllocationRequest) => Promise<ReserveAllocationResponse>;
  createReserveRule: (draft: ReserveRuleRequest) => Promise<ReserveRuleResponse>;
  createTrip: (draft: TripRequest) => Promise<TripResponse>;
  createTruckProfile: (draft: TruckProfileRequest) => Promise<TruckProfileResponse>;
  getFinancialHealthScore: () => Promise<FinancialHealthResponse>;
  getReserveWallets: () => Promise<ReserveWalletResponse[]>;
  getTrip: (tripId: string) => Promise<TripResponse>;
  requestProfitabilityEstimate: (
    tripId: string,
    draft: ProfitabilityEstimateRequest
  ) => Promise<ProfitabilityEstimateResponse>;
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
    createDriverProfile: (draft) =>
      request<{ driver: DriverProfileResponse }>({
        body: draft,
        method: 'POST',
        path: '/api/drivers',
      }).then((envelope) => envelope.driver),
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
  };
}
