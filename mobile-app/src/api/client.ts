import {
  type DriverProfileDraft,
  type ReserveWalletSummary,
  type TripDraft,
} from './contracts';

type RequestOptions = {
  body?: unknown;
  method: 'GET' | 'POST' | 'PUT';
  path: string;
};

export type CoreApiClient = {
  baseUrl: string;
  createDriverProfile: (draft: DriverProfileDraft) => Promise<DriverProfileDraft>;
  createTripDraft: (draft: TripDraft) => Promise<TripDraft>;
  getReserveWallets: () => Promise<ReserveWalletSummary>;
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
      request<DriverProfileDraft>({
        body: draft,
        method: 'POST',
        path: '/api/drivers',
      }),
    createTripDraft: (draft) =>
      request<TripDraft>({
        body: draft,
        method: 'POST',
        path: '/api/trips',
      }),
    getReserveWallets: () =>
      request<ReserveWalletSummary>({
        method: 'GET',
        path: '/api/reserve-wallets',
      }),
  };
}
