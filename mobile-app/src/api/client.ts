import {
  type DriverProfileRequest,
  type DriverProfileResponse,
  type ProfitabilityEstimateRequest,
  type ProfitabilityEstimateResponse,
  type ReserveWalletSummary,
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
  createTrip: (draft: TripRequest) => Promise<TripResponse>;
  createTruckProfile: (draft: TruckProfileRequest) => Promise<TruckProfileResponse>;
  getReserveWallets: () => Promise<ReserveWalletSummary>;
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
