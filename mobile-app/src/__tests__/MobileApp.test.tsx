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

    render(<MobileApp />);

    await user.press(screen.getByRole('button', { name: 'Frete' }));
    expect(screen.getByText('Pedagio reembolsado e Vale-Pedagio nao viram lucro.')).toBeOnTheScreen();

    await user.press(screen.getByRole('button', { name: 'Reservas' }));
    expect(screen.getByText('Planejamento, nao garantia')).toBeOnTheScreen();
    expect(screen.getByText(/nao representam transferencia bancaria real/)).toBeOnTheScreen();
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
