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

    queueDashboardLoad();
    render(<MobileApp />);

    for (const route of MVP_ROUTES) {
      await user.press(screen.getByRole('button', { name: route.shortLabel }));

      expect(screen.getByText(route.label)).toBeOnTheScreen();
      expect(screen.getByText(screenContentByRoute[route.id].title)).toBeOnTheScreen();
    }
  });

  it('renders financial health dashboard with reserves, receivables, compliance, and caveats', async () => {
    queueDashboardLoad();

    render(<MobileApp />);

    await screen.findByText('Repasse nao e lucro');
    expect(screen.getByText('88/100')).toBeOnTheScreen();
    expect(screen.getByText('R$ 1.100,00')).toBeOnTheScreen();
    expect(screen.getAllByText('Risco').length).toBeGreaterThanOrEqual(1);
    expect(screen.getByText(/R\$ 2.400,00 vencido/)).toBeOnTheScreen();
    expect(screen.getByText('Transportadora Exemplo')).toBeOnTheScreen();
    expect(screen.getByText(/Pedagio reembolsado e Vale-Pedagio ficam fora do lucro/)).toBeOnTheScreen();
    expect(screen.getByText(/Dados de diesel, pedagio, impostos e importacoes/)).toBeOnTheScreen();

    expect(fetchMock).toHaveBeenNthCalledWith(
      1,
      expect.stringContaining('/api/financial-health-score'),
      expect.objectContaining({ method: 'GET' })
    );
    expect(fetchMock).toHaveBeenNthCalledWith(
      4,
      expect.stringContaining('/api/receivables?status=overdue'),
      expect.objectContaining({ method: 'GET' })
    );
  });

  it('shows a good dashboard status when there are no overdue receivables', async () => {
    fetchMock
      .mockResolvedValueOnce(okJson({ financialHealthScore: financialHealthResponse('2300.00', 'GOOD') }))
      .mockResolvedValueOnce(okJson({ reserveWallets: reserveWalletsResponse() }))
      .mockResolvedValueOnce(okJson({ complianceProfile: { ...complianceProfileResponse(), alerts: [] } }))
      .mockResolvedValueOnce(okJson(emptyReceivablesResponse()));

    render(<MobileApp />);

    await screen.findByText('Sem recebivel vencido no filtro atual');
    expect(screen.getAllByText('Boa').length).toBeGreaterThanOrEqual(1);
    expect(screen.getByText('Nenhum recebivel vencido retornado pelo Core API.')).toBeOnTheScreen();
  });

  it('uses advisory compliance wording', async () => {
    const user = userEvent.setup();
    await renderMobileAppAfterDashboardLoad();
    queueComplianceLoad();

    await user.press(screen.getByRole('button', { name: 'Riscos' }));

    await screen.findByText('Status consultivo');
    expect(screen.getByText(/Organizador interno/)).toBeOnTheScreen();
    expect(screen.getByText(/RNTRC Digital e gov.br/)).toBeOnTheScreen();
    expect(screen.getByText(/Nao atualiza ANTT/)).toBeOnTheScreen();
  });

  it('keeps finance caveats visible across MVP routes', async () => {
    const user = userEvent.setup();
    await renderMobileAppAfterDashboardLoad();
    fetchMock
      .mockResolvedValueOnce(okJson({ reserveWallets: reserveWalletsResponse() }))
      .mockResolvedValueOnce(okJson({ financialHealthScore: financialHealthResponse() }));

    await user.press(screen.getByRole('button', { name: 'Frete' }));
    expect(screen.getByText('Pedagio reembolsado e Vale-Pedagio nao viram lucro.')).toBeOnTheScreen();

    await user.press(screen.getByRole('button', { name: 'Reservas' }));
    await screen.findByText('Baldes de reserva');
    expect(screen.getByText('Somente o balde salario do motorista. Reservas obrigatorias continuam separadas.')).toBeOnTheScreen();
    expect(screen.getByText(/Nao representam transferencia bancaria/)).toBeOnTheScreen();
  });

  it('creates driver and truck data from onboarding', async () => {
    const user = userEvent.setup();
    await renderMobileAppAfterDashboardLoad();
    fetchMock
      .mockResolvedValueOnce(okJson({ driver: { id: 'driver_123' } }))
      .mockResolvedValueOnce(okJson({ truck: { id: 'truck_created' } }));

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
    await renderMobileAppAfterDashboardLoad();
    fetchMock
      .mockResolvedValueOnce(okJson({ trip: tripResponse() }))
      .mockResolvedValueOnce(okJson({ trip: tripResponse() }))
      .mockResolvedValueOnce(okJson({ profitabilityEstimate: profitabilityEstimateResponse() }));

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

    await renderMobileAppAfterDashboardLoad();

    await user.press(screen.getByRole('button', { name: 'Frete' }));
    await user.clear(screen.getByLabelText('Frete bruto'));
    await user.press(screen.getByRole('button', { name: 'Calcular frete' }));

    expect(screen.getByText('Informe caminhao e frete bruto maior que zero.')).toBeOnTheScreen();
    expect(fetchMock).not.toHaveBeenCalled();
  });

  it('shows driver-friendly copy for Core API validation errors', async () => {
    const user = userEvent.setup();
    await renderMobileAppAfterDashboardLoad();
    fetchMock.mockResolvedValueOnce({
      json: async () => ({ error: 'VALIDATION_ERROR' }),
      ok: false,
      status: 422,
    });

    await user.press(screen.getByRole('button', { name: 'Frete' }));
    await user.press(screen.getByRole('button', { name: 'Calcular frete' }));

    await screen.findByText('Algum valor nao passou na validacao. Revise os campos e tente novamente.');
    expect(screen.queryByText(/Core API request failed/)).toBeNull();
  });

  it('renders compliance profile, alerts, calendar, insurance, and documents', async () => {
    const user = userEvent.setup();
    await renderMobileAppAfterDashboardLoad();
    queueComplianceLoad();

    await user.press(screen.getByRole('button', { name: 'Riscos' }));

    await screen.findByText('Status consultivo');
    expect(screen.getAllByText('RNTRC').length).toBeGreaterThanOrEqual(1);
    expect(screen.getByText('ACTIVE')).toBeOnTheScreen();
    expect(screen.getByText('Seguro vence em 30 dias')).toBeOnTheScreen();
    expect(screen.getByText('RCTR-C Example Seguros')).toBeOnTheScreen();
    expect(screen.getByText('CRLV 2026')).toBeOnTheScreen();
    expect(screen.getByText(/Confirmar em RNTRC Digital/)).toBeOnTheScreen();

    expect(fetchMock).toHaveBeenNthCalledWith(
      1,
      expect.stringContaining('/api/compliance/profile'),
      expect.objectContaining({ method: 'GET' })
    );
    expect(fetchMock).toHaveBeenNthCalledWith(
      5,
      expect.stringContaining('/api/documents'),
      expect.objectContaining({ method: 'GET' })
    );
  });

  it('renders reserve wallet balances, transactions, and safe withdrawal separately', async () => {
    const user = userEvent.setup();
    await renderMobileAppAfterDashboardLoad();
    fetchMock
      .mockResolvedValueOnce(okJson({ reserveWallets: reserveWalletsResponse() }))
      .mockResolvedValueOnce(okJson({ financialHealthScore: financialHealthResponse() }));

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

  it('saves RNTRC, insurance, and document metadata through compliance APIs', async () => {
    const user = userEvent.setup();
    await renderMobileAppAfterDashboardLoad();
    queueComplianceLoad();
    fetchMock.mockResolvedValueOnce(okJson({ rntrcProfile: complianceProfileResponse().rntrc }));
    queueComplianceLoad();
    fetchMock.mockResolvedValueOnce(okJson({ insurancePolicy: insurancePoliciesResponse()[0] }));
    queueComplianceLoad();
    fetchMock.mockResolvedValueOnce(okJson({ document: documentsResponse()[0] }));
    queueComplianceLoad();

    await user.press(screen.getByRole('button', { name: 'Riscos' }));
    await screen.findByText('Salvar metadados');

    await user.press(screen.getByRole('button', { name: 'Salvar RNTRC TAC' }));
    await screen.findByText('RNTRC salvo no Cofrete. Para alteracao oficial, use RNTRC Digital com gov.br.');

    await user.press(screen.getByRole('button', { name: 'Salvar seguro RCTR-C' }));
    await screen.findByText('Seguro salvo como metadado. Confirme cobertura com seguradora, SUSEP ou profissional qualificado.');

    await user.press(screen.getByRole('button', { name: 'Salvar CRLV' }));
    await screen.findByText('Documento salvo para lembrete. Cofrete nao certifica regularidade oficial.');

    expect(fetchMock.mock.calls.some((call) => String(call[0]).includes('/api/compliance/rntrc'))).toBe(true);
    expect(fetchMock.mock.calls.some((call) => String(call[0]).includes('/api/compliance/insurance-policies'))).toBe(true);
    expect(fetchMock.mock.calls.some((call) => String(call[0]).includes('/api/documents'))).toBe(true);
  });

  it('shows compliance API errors without raw technical text', async () => {
    const user = userEvent.setup();
    await renderMobileAppAfterDashboardLoad();
    fetchMock.mockResolvedValueOnce({
      json: async () => ({ error: 'VALIDATION_ERROR' }),
      ok: false,
      status: 400,
    });

    await user.press(screen.getByRole('button', { name: 'Riscos' }));

    await screen.findByText('Algum valor nao passou na validacao. Revise os campos e tente novamente.');
    expect(screen.queryByText(/Core API request failed/)).toBeNull();
  });

  it('shows reserve wallet empty state and all blueprint buckets', async () => {
    const user = userEvent.setup();
    await renderMobileAppAfterDashboardLoad();
    fetchMock
      .mockResolvedValueOnce(okJson({ reserveWallets: [] }))
      .mockResolvedValueOnce(okJson({ financialHealthScore: financialHealthResponse('0.00', 'UNKNOWN') }));

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
    await renderMobileAppAfterDashboardLoad();
    fetchMock.mockResolvedValueOnce({
      json: async () => ({ error: 'SOURCE_STALE' }),
      ok: false,
      status: 500,
    });

    await user.press(screen.getByRole('button', { name: 'Reservas' }));

    await screen.findByText('O Core API nao conseguiu responder agora. Tente novamente em alguns minutos.');
    expect(screen.queryByText(/Core API request failed/)).toBeNull();
  });

  it('creates reserve rules and requests an allocation from the reserve screen', async () => {
    const user = userEvent.setup();
    await renderMobileAppAfterDashboardLoad();
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

async function renderMobileAppAfterDashboardLoad() {
  queueDashboardLoad();
  render(<MobileApp />);
  await screen.findByText('Rastro financial_health_test');
  fetchMock.mockClear();
}

function okJson(body: unknown) {
  return {
    json: async () => body,
    ok: true,
  };
}

function queueDashboardLoad() {
  fetchMock
    .mockResolvedValueOnce(okJson({ financialHealthScore: financialHealthResponse() }))
    .mockResolvedValueOnce(okJson({ reserveWallets: reserveWalletsResponse() }))
    .mockResolvedValueOnce(okJson({ complianceProfile: complianceProfileResponse() }))
    .mockResolvedValueOnce(okJson(receivablesResponse()));
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

function queueComplianceLoad() {
  fetchMock
    .mockResolvedValueOnce(okJson({ complianceProfile: complianceProfileResponse() }))
    .mockResolvedValueOnce(okJson({ complianceScore: complianceScoreResponse() }))
    .mockResolvedValueOnce(okJson({ calendarItems: calendarItemsResponse() }))
    .mockResolvedValueOnce(okJson({ insurancePolicies: insurancePoliciesResponse() }))
    .mockResolvedValueOnce(okJson({ documents: documentsResponse() }));
}

function complianceProfileResponse() {
  return {
    alerts: [
      {
        advisoryText: 'Alerta consultivo. Confirme com seguradora ou canal oficial.',
        alertType: 'INSURANCE_EXPIRATION',
        dueOn: '2026-06-13',
        generatedAt: '2026-05-14T12:00:00Z',
        id: 'alert_insurance',
        message: 'Seguro vence em 30 dias',
        officialActionUrl: 'https://www.gov.br/susep',
        severity: 'WARNING',
        status: 'EXPIRING_SOON',
        subjectId: 'ins_123',
        subjectType: 'INSURANCE_POLICY',
      },
    ],
    caveats: ['Cofrete is not an official government, legal, tax, accounting, or insurance channel.'],
    ciot: {
      advisoryText: 'CIOT guidance is advisory.',
      latestTripStatus: 'NOT_RECORDED',
      required: 'UNKNOWN',
    },
    documents: {
      activeDocuments: 1,
      advisoryText: 'Document reminders are advisory organization aids.',
      expired: 0,
      expiringSoon: 1,
    },
    driverId: 'driver_123',
    insurance: {
      activePolicies: 1,
      advisoryText: 'Insurance data is organization metadata.',
      expired: 0,
      expiringSoon: 1,
    },
    rntrc: {
      advisoryText: 'Confirm RNTRC status in official ANTT channels.',
      category: 'TAC',
      guidance: [
        'Your Cofrete profile is not an official ANTT record. Official updates must be done through ANTT/RNTRC Digital.',
        'To update your RNTRC, access RNTRC Digital using your gov.br account, level prata or ouro.',
      ],
      lastCheckedAt: null,
      numberMasked: '***4321',
      officialActionUrl: 'https://www.gov.br/antt',
      source: 'driver_entered',
      status: 'ACTIVE',
    },
    score: complianceScoreResponse(),
  };
}

function complianceScoreResponse() {
  return {
    caveats: ['Cofrete is not an official government, legal, tax, accounting, or insurance channel.'],
    score: {
      components: [
        {
          advisoryText: 'Insurance metadata needs review.',
          name: 'insurance',
          status: 'EXPIRING_SOON',
          weight: '25.00',
        },
      ],
      snapshotAt: '2026-05-14T12:00:00Z',
      status: 'ATTENTION',
      value: 72,
    },
  };
}

function calendarItemsResponse() {
  return [
    {
      advisoryText: 'Calendario consultivo. Confirme nos canais oficiais.',
      dueOn: '2026-06-13',
      id: 'cal_insurance',
      officialActionUrl: 'https://www.gov.br/susep',
      severity: 'WARNING',
      status: 'EXPIRING_SOON',
      subjectId: 'ins_123',
      subjectType: 'INSURANCE_POLICY',
      title: 'RCTR-C Example Seguros',
    },
  ];
}

function insurancePoliciesResponse() {
  return [
    {
      active: true,
      advisoryText: 'Insurance data is organization metadata. Confirm coverage with insurer or SUSEP.',
      annualPremium: '3600.00',
      currency: 'BRL',
      driverId: 'driver_123',
      expiresOn: '2026-06-13',
      id: 'ins_123',
      insurer: 'Example Seguros',
      linkedRntrc: true,
      monthlyReserve: '300.00',
      pgrRequired: true,
      policyNumberLast4: '6789',
      policyNumberMasked: '***6789',
      policyType: 'RCTR_C',
      startsOn: '2025-06-13',
      status: 'EXPIRING_SOON',
      verificationStatus: 'VERIFIED_BY_DRIVER',
    },
  ];
}

function documentsResponse() {
  return [
    {
      active: true,
      advisoryText: 'Document reminders are advisory organization aids.',
      documentType: 'CRLV',
      driverId: 'driver_123',
      expiresOn: '2026-06-28',
      id: 'doc_123',
      identifierLast4: '1122',
      identifierMasked: '***1122',
      issuedOn: '2025-06-28',
      notes: 'Confirmar canal oficial.',
      ownerType: 'DRIVER',
      source: 'DRIVER_ENTERED',
      status: 'EXPIRING_SOON',
      title: 'CRLV 2026',
    },
  ];
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
    components: [
      {
        bucket: 'MAINTENANCE',
        coveragePercent: '12.00',
        currentBalance: '600.00',
        status: 'RISK',
        targetBalance: '5000.00',
      },
      {
        bucket: 'DRIVER_SALARY',
        coveragePercent: '100.00',
        currentBalance: safePersonalWithdrawalAvailable,
        status,
        targetBalance: null,
      },
    ],
    currency: 'BRL',
    reserveCoveragePercent: status === 'UNKNOWN' ? '0.00' : '88.00',
    safePersonalWithdrawalAvailable,
    score: status === 'UNKNOWN' ? 0 : 88,
    status,
    traceId: 'financial_health_test',
  };
}

function receivablesResponse() {
  return {
    receivables: [
      {
        advisoryText: 'Receivables are cash-flow planning records only. Cofrete does not move money or guarantee payment.',
        amount: '2400.00',
        currency: 'BRL',
        customerId: 'customer_123',
        customerName: 'Transportadora Exemplo',
        dueDate: '2026-05-01',
        id: 'recv_123',
        invoiceReference: 'NF-123',
        paidAmount: '0.00',
        paymentMethod: 'PIX',
        payments: [],
        remainingAmount: '2400.00',
        status: 'LATE',
        statusChanges: [],
        tripId: 'trip_123',
        type: 'FREIGHT_BALANCE',
      },
    ],
    totals: {
      currency: 'BRL',
      expectedAmount: '0.00',
      lateAmount: '2400.00',
      paidAmount: '0.00',
      partiallyPaidAmount: '0.00',
    },
  };
}

function emptyReceivablesResponse() {
  return {
    receivables: [],
    totals: {
      currency: 'BRL',
      expectedAmount: '0.00',
      lateAmount: '0.00',
      paidAmount: '0.00',
      partiallyPaidAmount: '0.00',
    },
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
