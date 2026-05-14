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
    queueComplianceLoad();

    render(<MobileApp />);

    await user.press(screen.getByRole('button', { name: 'Riscos' }));

    await screen.findByText('Status consultivo');
    expect(screen.getByText(/Organizador interno/)).toBeOnTheScreen();
    expect(screen.getByText(/RNTRC Digital e gov.br/)).toBeOnTheScreen();
    expect(screen.getByText(/Nao atualiza ANTT/)).toBeOnTheScreen();
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

  it('renders compliance profile, alerts, calendar, insurance, and documents', async () => {
    const user = userEvent.setup();
    queueComplianceLoad();

    render(<MobileApp />);

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

  it('saves RNTRC, insurance, and document metadata through compliance APIs', async () => {
    const user = userEvent.setup();
    queueComplianceLoad();
    fetchMock.mockResolvedValueOnce(okJson({ rntrcProfile: complianceProfileResponse().rntrc }));
    queueComplianceLoad();
    fetchMock.mockResolvedValueOnce(okJson({ insurancePolicy: insurancePoliciesResponse()[0] }));
    queueComplianceLoad();
    fetchMock.mockResolvedValueOnce(okJson({ document: documentsResponse()[0] }));
    queueComplianceLoad();

    render(<MobileApp />);

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
    fetchMock.mockResolvedValueOnce({
      json: async () => ({ error: 'VALIDATION_ERROR' }),
      ok: false,
      status: 400,
    });

    render(<MobileApp />);

    await user.press(screen.getByRole('button', { name: 'Riscos' }));

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
