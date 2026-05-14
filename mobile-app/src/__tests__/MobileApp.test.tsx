import { render, screen, userEvent } from '@testing-library/react-native';

import { MobileApp } from '../MobileApp';
import { MVP_ROUTES } from '../navigation/routes';
import { screenContentByRoute } from '../screens/screenContent';

const fetchMock = jest.fn();

beforeEach(() => {
  fetchMock.mockReset();
  global.fetch = fetchMock as unknown as typeof fetch;
});

describe('MobileApp', () => {
  it('renders every MVP route without a dead tab', async () => {
    const user = userEvent.setup();

    render(<MobileApp />);

    for (const route of MVP_ROUTES) {
      await user.press(screen.getByRole('button', { name: route.shortLabel }));

      expect(screen.getByText(route.label)).toBeOnTheScreen();
      expect(screen.getByText(screenContentByRoute[route.id].title)).toBeOnTheScreen();
    }
  });

  it('uses advisory compliance wording', async () => {
    const user = userEvent.setup();

    render(<MobileApp />);

    await user.press(screen.getByRole('button', { name: 'Riscos' }));

    expect(screen.getByText('Sem certificacao oficial')).toBeOnTheScreen();
    expect(screen.getByText(/canais oficiais/)).toBeOnTheScreen();
  });

  it('keeps finance caveats visible across MVP routes', async () => {
    const user = userEvent.setup();
    fetchMock
      .mockResolvedValueOnce(okJson({ reserveWallets: reserveWalletsResponse() }))
      .mockResolvedValueOnce(okJson({ financialHealthScore: financialHealthResponse() }));

    render(<MobileApp />);

    await user.press(screen.getByRole('button', { name: 'Frete' }));
    expect(screen.getByText('Pedagio reembolsado e Vale-Pedagio nao viram lucro.')).toBeOnTheScreen();

    await user.press(screen.getByRole('button', { name: 'Reservas' }));
    await screen.findByText('Baldes de reserva');
    expect(screen.getByText('Somente o balde salario do motorista. Reservas obrigatorias continuam separadas.')).toBeOnTheScreen();
    expect(screen.getByText(/Nao representam transferencia bancaria/)).toBeOnTheScreen();
  });

  it('creates driver and truck data from onboarding', async () => {
    const user = userEvent.setup();
    fetchMock
      .mockResolvedValueOnce(okJson({ driver: { id: 'driver_123' } }))
      .mockResolvedValueOnce(okJson({ truck: { id: 'truck_created' } }));

    render(<MobileApp />);

    await user.press(screen.getByRole('button', { name: 'Perfil' }));
    await user.press(screen.getByRole('button', { name: 'Criar perfil e caminhao' }));

    await screen.findByText('Perfil pronto para calcular frete');
    expect(screen.getByText('Motorista: driver_123')).toBeOnTheScreen();
    expect(screen.getByText('Caminhao: truck_created')).toBeOnTheScreen();

    expect(fetchMock).toHaveBeenNthCalledWith(
      1,
      expect.stringContaining('/api/drivers'),
      expect.objectContaining({ method: 'POST' })
    );
    expect(fetchMock).toHaveBeenNthCalledWith(
      2,
      expect.stringContaining('/api/trucks'),
      expect.objectContaining({ method: 'POST' })
    );

    await user.press(screen.getByRole('button', { name: 'Frete' }));
    expect(screen.getByLabelText('ID do caminhao')).toHaveProp('value', 'truck_created');
  });

  it('runs the new freight calculator happy path through trip and profitability APIs', async () => {
    const user = userEvent.setup();
    fetchMock
      .mockResolvedValueOnce(okJson({ trip: tripResponse() }))
      .mockResolvedValueOnce(okJson({ trip: tripResponse() }))
      .mockResolvedValueOnce(okJson({ profitabilityEstimate: profitabilityEstimateResponse() }));

    render(<MobileApp />);

    await user.press(screen.getByRole('button', { name: 'Frete' }));
    await user.press(screen.getByRole('button', { name: 'Calcular frete' }));

    await screen.findByText('Estimativa do frete');
    expect(screen.getByText('Repasse de caixa')).toBeOnTheScreen();
    expect(screen.getByText('Saque pessoal seguro')).toBeOnTheScreen();
    expect(screen.getAllByText('R$ 1.974,00')).toHaveLength(2);
    expect(screen.getByText(/Reembolso de pedagio e Vale-Pedagio/)).toBeOnTheScreen();

    expect(fetchMock).toHaveBeenNthCalledWith(
      1,
      expect.stringContaining('/api/trips'),
      expect.objectContaining({ method: 'POST' })
    );
    expect(fetchMock).toHaveBeenNthCalledWith(
      2,
      expect.stringContaining('/api/trips/trip_123'),
      expect.objectContaining({ method: 'GET' })
    );
    expect(fetchMock).toHaveBeenNthCalledWith(
      3,
      expect.stringContaining('/api/trips/trip_123/profitability-estimate'),
      expect.objectContaining({ method: 'POST' })
    );

    const estimateBody = JSON.parse(fetchMock.mock.calls[2][1].body as string);
    expect(estimateBody.tollReimbursement).toBe('385.70');
    expect(estimateBody.valePedagio).toBe('214.30');
    expect(estimateBody.reservePolicy.maintenanceRate).toBe('0.080000');
  });

  it('validates required freight inputs before calling the API', async () => {
    const user = userEvent.setup();

    render(<MobileApp />);

    await user.press(screen.getByRole('button', { name: 'Frete' }));
    await user.clear(screen.getByLabelText('Frete bruto'));
    await user.press(screen.getByRole('button', { name: 'Calcular frete' }));

    expect(screen.getByText('Informe caminhao e frete bruto maior que zero.')).toBeOnTheScreen();
    expect(fetchMock).not.toHaveBeenCalled();
  });

  it('shows driver-friendly copy for Core API validation errors', async () => {
    const user = userEvent.setup();
    fetchMock.mockResolvedValueOnce({
      json: async () => ({ error: 'VALIDATION_ERROR' }),
      ok: false,
      status: 422,
    });

    render(<MobileApp />);

    await user.press(screen.getByRole('button', { name: 'Frete' }));
    await user.press(screen.getByRole('button', { name: 'Calcular frete' }));

    await screen.findByText('Algum valor nao passou na validacao. Revise os campos e tente novamente.');
    expect(screen.queryByText(/Core API request failed/)).toBeNull();
  });

  it('renders reserve wallet balances, transactions, and safe withdrawal separately', async () => {
    const user = userEvent.setup();
    fetchMock
      .mockResolvedValueOnce(okJson({ reserveWallets: reserveWalletsResponse() }))
      .mockResolvedValueOnce(okJson({ financialHealthScore: financialHealthResponse() }));

    render(<MobileApp />);

    await user.press(screen.getByRole('button', { name: 'Reservas' }));

    await screen.findByText('Baldes de reserva');
    expect(screen.getByText('Saque pessoal seguro')).toBeOnTheScreen();
    expect(screen.getAllByText('R$ 1.100,00')).toHaveLength(2);
    expect(screen.getAllByText('Manutencao').length).toBeGreaterThanOrEqual(1);
    expect(screen.getByText('Salario do motorista')).toBeOnTheScreen();
    expect(screen.getByText('Alvo R$ 5.000,00 (12,00%)')).toBeOnTheScreen();
    expect(screen.getByText('Movimento virtual de reserva. Nao e transferencia bancaria real.')).toBeOnTheScreen();

    expect(fetchMock).toHaveBeenNthCalledWith(
      1,
      expect.stringContaining('/api/reserve-wallets'),
      expect.objectContaining({ method: 'GET' })
    );
    expect(fetchMock).toHaveBeenNthCalledWith(
      2,
      expect.stringContaining('/api/financial-health-score'),
      expect.objectContaining({ method: 'GET' })
    );
  });

  it('shows reserve wallet empty state and all blueprint buckets', async () => {
    const user = userEvent.setup();
    fetchMock
      .mockResolvedValueOnce(okJson({ reserveWallets: [] }))
      .mockResolvedValueOnce(okJson({ financialHealthScore: financialHealthResponse('0.00', 'UNKNOWN') }));

    render(<MobileApp />);

    await user.press(screen.getByRole('button', { name: 'Reservas' }));

    await screen.findByText('Nenhum balde criado');
    expect(screen.getByText('Diesel, ARLA e pedagio')).toBeOnTheScreen();
    expect(screen.getByText('Manutencao')).toBeOnTheScreen();
    expect(screen.getByText('Pneus')).toBeOnTheScreen();
    expect(screen.getByText('Seguro')).toBeOnTheScreen();
    expect(screen.getByText('Impostos e documentos')).toBeOnTheScreen();
    expect(screen.getByText('Troca do caminhao')).toBeOnTheScreen();
    expect(screen.getByText('Emergencia')).toBeOnTheScreen();
    expect(screen.getByText('Salario do motorista')).toBeOnTheScreen();
    expect(screen.getByText('Lucro')).toBeOnTheScreen();
  });

  it('shows a reserve wallet API error without raw technical text', async () => {
    const user = userEvent.setup();
    fetchMock.mockResolvedValueOnce({
      json: async () => ({ error: 'SOURCE_STALE' }),
      ok: false,
      status: 500,
    });

    render(<MobileApp />);

    await user.press(screen.getByRole('button', { name: 'Reservas' }));

    await screen.findByText('O Core API nao conseguiu responder agora. Tente novamente em alguns minutos.');
    expect(screen.queryByText(/Core API request failed/)).toBeNull();
  });

  it('creates reserve rules and requests an allocation from the reserve screen', async () => {
    const user = userEvent.setup();
    fetchMock
      .mockResolvedValueOnce(okJson({ reserveWallets: [] }))
      .mockResolvedValueOnce(okJson({ financialHealthScore: financialHealthResponse('0.00', 'UNKNOWN') }));
    for (let index = 0; index < 9; index += 1) {
      fetchMock.mockResolvedValueOnce(okJson({ reserveRule: { id: `reserve_rule_${index}` } }));
    }
    fetchMock
      .mockResolvedValueOnce(okJson({ reserveWallets: reserveWalletsResponse() }))
      .mockResolvedValueOnce(okJson({ financialHealthScore: financialHealthResponse() }))
      .mockResolvedValueOnce(okJson({ reserveAllocation: reserveAllocationResponse() }))
      .mockResolvedValueOnce(okJson({ reserveWallets: reserveWalletsResponse() }))
      .mockResolvedValueOnce(okJson({ financialHealthScore: financialHealthResponse() }));

    render(<MobileApp />);

    await user.press(screen.getByRole('button', { name: 'Reservas' }));
    await screen.findByText('Nenhum balde criado');

    await user.press(screen.getByRole('button', { name: 'Criar regras starter' }));
    await screen.findByText('Regras starter registradas. Revise percentuais antes de usar em fretes reais.');

    await user.press(screen.getByRole('button', { name: 'Solicitar alocacao virtual' }));
    await screen.findByText('Alocacao solicitada. Os saldos mudam quando o worker financeiro confirmar o evento.');

    expect(fetchMock.mock.calls.some((call) => String(call[0]).includes('/api/reserve-rules'))).toBe(true);
    expect(fetchMock.mock.calls.some((call) => String(call[0]).includes('/api/reserve-allocations'))).toBe(true);
  });
});

function okJson(body: unknown) {
  return {
    json: async () => body,
    ok: true,
  };
}

function tripResponse() {
  return {
    advanceAmount: '3000.00',
    balanceDueDays: 21,
    cargoDescription: 'carga geral',
    currency: 'BRL',
    decisionState: 'DRAFT',
    destination: { city: 'Sao Paulo', state: 'SP' },
    emptyKm: '80.00',
    grossFreight: '8000.00',
    id: 'trip_123',
    inputRevision: 1,
    latestSnapshotId: null,
    loadedKm: '920.00',
    origin: { city: 'Goiania', state: 'GO' },
    profitabilityEstimatePath: '/api/trips/trip_123/profitability-estimate',
    profitabilitySnapshotPath: '/api/trips/trip_123/profitability-snapshot',
    totalDistanceKm: '1000.00',
    truckId: 'truck_123',
  };
}

function profitabilityEstimateResponse() {
  return {
    calculationTraceId: 'calc_123',
    caveats: [
      'Toll reimbursement and Vale-Pedagio are tracked as pass-through amounts, not profit.',
      'Profitability is advisory planning output, not legal, tax, accounting, insurance, or government guidance.',
    ],
    currency: 'BRL',
    directTripCost: '3000.00',
    expectedProfit: '1974.00',
    financialHealthStatus: 'GOOD',
    grossFreight: '8000.00',
    marginPercent: '24.68',
    passThroughAmount: '600.00',
    recommendation: 'ACCEPT',
    requiredReserves: '1776.00',
    safePersonalWithdrawal: '1974.00',
    sourceMetadata: [{ area: 'fuel', freshnessStatus: 'CURRENT' }],
    tripId: 'trip_123',
  };
}

function reserveWalletsResponse() {
  return [
    {
      bucket: 'MAINTENANCE',
      currency: 'BRL',
      currentBalance: '600.00',
      lastAllocationAt: '2026-05-11T12:00:11Z',
      policy: 'PERCENT_OF_AMOUNT',
      targetBalance: '5000.00',
      transactions: [
        {
          amount: '600.00',
          balanceAfter: '600.00',
          bucket: 'MAINTENANCE',
          createdAt: '2026-05-11T12:00:11Z',
          currency: 'BRL',
          id: 'reserve_txn_maint',
          note: 'Virtual reserve ledger movement. Not a real money transfer.',
          sourceReference: 'reserve-trip_123-pay_123-v1',
          sourceType: 'RESERVE_ALLOCATION',
          type: 'CREDIT',
        },
      ],
    },
    {
      bucket: 'DRIVER_SALARY',
      currency: 'BRL',
      currentBalance: '1100.00',
      lastAllocationAt: '2026-05-11T12:00:11Z',
      policy: 'PERCENT_OF_AMOUNT',
      targetBalance: null,
      transactions: [],
    },
  ];
}

function financialHealthResponse(safePersonalWithdrawalAvailable = '1100.00', status = 'GOOD') {
  return {
    advisoryText: 'Financial health is advisory planning output.',
    components: [],
    currency: 'BRL',
    reserveCoveragePercent: status === 'UNKNOWN' ? '0.00' : '88.00',
    safePersonalWithdrawalAvailable,
    score: status === 'UNKNOWN' ? 0 : 88,
    status,
    traceId: 'financial_health_test',
  };
}

function reserveAllocationResponse() {
  return {
    advisoryText: 'Reserve allocations are virtual ledger movements.',
    allocatableAmount: '7400.00',
    bucketAllocations: {},
    currency: 'BRL',
    duplicate: false,
    grossAmount: '8000.00',
    id: 'reserve_alloc_123',
    idempotencyKey: 'mobile-reserve-test',
    passThroughAmount: '600.00',
    requestStatus: 'REQUESTED',
    requiredReserveAmount: '0.00',
    safePersonalWithdrawal: '0.00',
    status: 'REQUESTED',
    transactions: [],
  };
}
