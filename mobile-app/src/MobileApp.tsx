import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import { NavigationContainer } from '@react-navigation/native';
import { StatusBar } from 'expo-status-bar';
import { useCallback, useEffect, useMemo, useState } from 'react';
import {
  ActivityIndicator,
  Pressable,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  View,
  type KeyboardTypeOptions,
} from 'react-native';

import { createCoreApiClient, type CoreApiClient } from './api/client';
import {
  type ComplianceCalendarItemResponse,
  type ComplianceDocumentResponse,
  type ComplianceItemStatus,
  type ComplianceProfileResponse,
  type ComplianceScoreResponse,
  type ComplianceSeverity,
  type DriverProfileRequest,
  type FinancialHealthResponse,
  type InsurancePolicyResponse,
  type ProfitabilityEstimateRequest,
  type ProfitabilityEstimateResponse,
  type ReceivablesEnvelope,
  type ReserveAllocationRequest,
  type ReserveBucket,
  type ReserveRuleRequest,
  type ReserveWalletResponse,
  type RoutePoint,
  type TripRequest,
  type TripResponse,
  type TruckProfileRequest,
} from './api/contracts';
import { getCoreApiBaseUrl } from './config/env';
import { MVP_ROUTES, type AppRouteId } from './navigation/routes';
import { screenContentByRoute } from './screens/screenContent';

type RootTabParamList = Record<AppRouteId, undefined>;

type OnboardingFormState = {
  axleCount: string;
  driverName: string;
  email: string;
  phone: string;
  plate: string;
  renavamLast4: string;
  rntrcNumber: string;
  state: string;
};

type FreightFormState = {
  advanceAmount: string;
  arlaCost: string;
  balanceDueDays: string;
  cargoDescription: string;
  destinationCity: string;
  destinationState: string;
  dieselConsumptionKmPerLiter: string;
  dieselPricePerLiter: string;
  emptyKm: string;
  financingAllocation: string;
  grossFreight: string;
  loadedKm: string;
  mealsAndLodgingCost: string;
  nonReimbursedToll: string;
  originCity: string;
  originState: string;
  otherDirectCost: string;
  otherPassThrough: string;
  tollReimbursement: string;
  truckId: string;
  valePedagio: string;
};

type ComplianceFormState = {
  documentLast4: string;
  documentTitle: string;
  insuranceLast4: string;
  insurer: string;
  rntrcNumber: string;
};

type OnboardingResult = {
  driverId: string;
  truckId: string;
};

type FreightResult = {
  estimate: ProfitabilityEstimateResponse;
  trip: TripResponse;
};

type ComplianceResult = {
  calendarItems: ComplianceCalendarItemResponse[];
  documents: ComplianceDocumentResponse[];
  insurancePolicies: InsurancePolicyResponse[];
  profile: ComplianceProfileResponse | null;
  score: ComplianceScoreResponse | null;
};

type ReserveWalletResult = {
  financialHealth: FinancialHealthResponse | null;
  wallets: ReserveWalletResponse[];
};

type DashboardResult = {
  compliance: ComplianceProfileResponse | null;
  financialHealth: FinancialHealthResponse | null;
  overdueReceivables: ReceivablesEnvelope | null;
  wallets: ReserveWalletResponse[];
};

type FieldProps = {
  label: string;
  keyboardType?: KeyboardTypeOptions;
  onChangeText: (value: string) => void;
  value: string;
};

const Tab = createBottomTabNavigator<RootTabParamList>();

const currencyFormatter = new Intl.NumberFormat('pt-BR', {
  currency: 'BRL',
  style: 'currency',
});

const percentFormatter = new Intl.NumberFormat('pt-BR', {
  maximumFractionDigits: 2,
  minimumFractionDigits: 2,
  style: 'percent',
});

const defaultOnboardingForm: OnboardingFormState = {
  axleCount: '6',
  driverName: 'Joao Motorista',
  email: 'joao@example.test',
  phone: '+55 62 99999-0000',
  plate: 'ABC1D23',
  renavamLast4: '6789',
  rntrcNumber: '12345678',
  state: 'GO',
};

const defaultFreightForm: FreightFormState = {
  advanceAmount: '3000.00',
  arlaCost: '120.00',
  balanceDueDays: '21',
  cargoDescription: 'carga geral',
  destinationCity: 'Sao Paulo',
  destinationState: 'SP',
  dieselConsumptionKmPerLiter: '2.5000',
  dieselPricePerLiter: '5.2500',
  emptyKm: '80.00',
  financingAllocation: '650.00',
  grossFreight: '8000.00',
  loadedKm: '920.00',
  mealsAndLodgingCost: '280.00',
  nonReimbursedToll: '300.00',
  originCity: 'Goiania',
  originState: 'GO',
  otherDirectCost: '200.00',
  otherPassThrough: '0.00',
  tollReimbursement: '385.70',
  truckId: 'truck_123',
  valePedagio: '214.30',
};

const defaultComplianceForm: ComplianceFormState = {
  documentLast4: '1122',
  documentTitle: 'CRLV 2026',
  insuranceLast4: '6789',
  insurer: 'Example Seguros',
  rntrcNumber: '987654321',
};

const reserveBucketOrder: ReserveBucket[] = [
  'FUEL_ARLA_TOLL_CASH_FLOW',
  'MAINTENANCE',
  'TIRES',
  'INSURANCE',
  'TAXES_AND_DOCUMENTS',
  'TRUCK_REPLACEMENT',
  'EMERGENCY',
  'DRIVER_SALARY',
  'PROFIT',
];

const reserveBucketCopy: Record<ReserveBucket, { label: string; tone: string; required: boolean }> = {
  DRIVER_SALARY: {
    label: 'Salario do motorista',
    required: false,
    tone: 'Parte planejada para uso pessoal',
  },
  EMERGENCY: {
    label: 'Emergencia',
    required: true,
    tone: 'Parada, quebra ou dia sem frete',
  },
  FUEL_ARLA_TOLL_CASH_FLOW: {
    label: 'Diesel, ARLA e pedagio',
    required: true,
    tone: 'Caixa de viagem, nao lucro',
  },
  INSURANCE: {
    label: 'Seguro',
    required: true,
    tone: 'Apolices e responsabilidade',
  },
  MAINTENANCE: {
    label: 'Manutencao',
    required: true,
    tone: 'Oleo, filtros, freio e suspensao',
  },
  PROFIT: {
    label: 'Lucro',
    required: false,
    tone: 'Crescimento do negocio',
  },
  TAXES_AND_DOCUMENTS: {
    label: 'Impostos e documentos',
    required: true,
    tone: 'DAS, IPVA, licenciamento e contador',
  },
  TIRES: {
    label: 'Pneus',
    required: true,
    tone: 'Troca, recapagem e alinhamento',
  },
  TRUCK_REPLACEMENT: {
    label: 'Troca do caminhao',
    required: true,
    tone: 'Entrada futura ou substituicao',
  },
};

const starterReserveRules: ReserveRuleRequest[] = [
  starterRule('FUEL_ARLA_TOLL_CASH_FLOW', '0.000000', '0.00'),
  starterRule('MAINTENANCE', '0.080000', '5000.00'),
  starterRule('TIRES', '0.040000', '3000.00'),
  starterRule('INSURANCE', '0.020000', '2500.00'),
  starterRule('TAXES_AND_DOCUMENTS', '0.030000', '1800.00'),
  starterRule('TRUCK_REPLACEMENT', '0.050000', '12000.00'),
  starterRule('EMERGENCY', '0.020000', '2500.00'),
  starterRule('DRIVER_SALARY', '0.150000'),
  starterRule('PROFIT', '0.050000'),
];

export function MobileApp() {
  const apiClient = useMemo(() => createCoreApiClient(getCoreApiBaseUrl()), []);
  const [activeTruckId, setActiveTruckId] = useState(defaultFreightForm.truckId);

  return (
    <NavigationContainer>
      <StatusBar style="dark" />
      <Tab.Navigator
        initialRouteName="dashboard"
        screenOptions={{
          headerShown: false,
          tabBarActiveBackgroundColor: colors.accent,
          tabBarActiveTintColor: colors.accentText,
          tabBarInactiveTintColor: colors.muted,
          tabBarItemStyle: styles.tabButton,
          tabBarLabelStyle: styles.tabLabel,
          tabBarStyle: styles.tabBar,
        }}
      >
        {MVP_ROUTES.map((route) => (
          <Tab.Screen
            key={route.id}
            name={route.id}
            options={{
              tabBarAccessibilityLabel: route.shortLabel,
              tabBarIcon: ({ color }) => (
                <Text style={[styles.tabIcon, { color }]}>{route.icon}</Text>
              ),
              tabBarLabel: route.shortLabel,
              title: route.label,
            }}
          >
            {() => renderRoute(route.id)}
          </Tab.Screen>
        ))}
      </Tab.Navigator>
    </NavigationContainer>
  );

  function renderRoute(routeId: AppRouteId) {
    return (
      <MvpScreen
        activeTruckId={activeTruckId}
        apiClient={apiClient}
        onTruckReady={setActiveTruckId}
        routeId={routeId}
      />
    );
  }
}

type MvpScreenProps = {
  activeTruckId: string;
  apiClient: CoreApiClient;
  onTruckReady: (truckId: string) => void;
  routeId: AppRouteId;
};

function MvpScreen({ activeTruckId, apiClient, onTruckReady, routeId }: MvpScreenProps) {
  const activeRoute = MVP_ROUTES.find((route) => route.id === routeId) ?? MVP_ROUTES[0];
  const activeContent = screenContentByRoute[routeId];

  return (
    <View style={styles.safeArea}>
      <StatusBar style="dark" />
      <View style={styles.appFrame}>
        <View style={styles.header}>
          <View>
            <Text style={styles.brand}>Cofrete</Text>
            <Text style={styles.account}>TAC autonomo, MVP motorista unico</Text>
          </View>
          <View style={styles.statusPill}>
            <Text style={styles.statusPillText}>MVP</Text>
          </View>
        </View>

        <ScrollView contentContainerStyle={styles.content} showsVerticalScrollIndicator={false}>
          <View style={styles.heroPanel}>
            <Text style={styles.kicker}>{activeRoute.label}</Text>
            <Text style={styles.title}>{activeContent.title}</Text>
            <Text style={styles.summary}>{activeContent.summary}</Text>
          </View>

          {routeId === 'onboarding' ? (
            <OnboardingScreen apiClient={apiClient} onTruckReady={onTruckReady} />
          ) : routeId === 'freight' ? (
            <FreightCalculatorScreen apiClient={apiClient} activeTruckId={activeTruckId} />
          ) : routeId === 'compliance' ? (
            <ComplianceCenterScreen apiClient={apiClient} />
          ) : routeId === 'reserves' ? (
            <ReserveWalletScreen apiClient={apiClient} />
          ) : routeId === 'dashboard' ? (
            <FinancialHealthDashboardScreen apiClient={apiClient} />
          ) : (
            <ReferenceScreen routeId={routeId} apiBaseUrl={apiClient.baseUrl} />
          )}
        </ScrollView>
      </View>
    </View>
  );
}

function ComplianceCenterScreen({ apiClient }: { apiClient: CoreApiClient }) {
  const [result, setResult] = useState<ComplianceResult | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isSavingRntrc, setIsSavingRntrc] = useState(false);
  const [isSavingInsurance, setIsSavingInsurance] = useState(false);
  const [isSavingDocument, setIsSavingDocument] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [message, setMessage] = useState<string | null>(null);
  const [form, setForm] = useState(defaultComplianceForm);

  const updateField = (field: keyof ComplianceFormState) => (value: string) => {
    setForm((current) => ({ ...current, [field]: value }));
  };

  const loadCompliance = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    try {
      const [profile, score, calendarItems, insurancePolicies, documents] = await Promise.all([
        apiClient.getComplianceProfile(),
        apiClient.getComplianceScore(),
        apiClient.getComplianceCalendar(),
        apiClient.getInsurancePolicies(),
        apiClient.getDocuments(),
      ]);
      setResult({ calendarItems, documents, insurancePolicies, profile, score });
    } catch (loadError) {
      setResult({ calendarItems: [], documents: [], insurancePolicies: [], profile: null, score: null });
      setError(friendlyErrorMessage(loadError, 'Nao foi possivel carregar o centro de conformidade agora.'));
    } finally {
      setIsLoading(false);
    }
  }, [apiClient]);

  useEffect(() => {
    void loadCompliance();
  }, [loadCompliance]);

  const saveRntrc = async () => {
    setIsSavingRntrc(true);
    setError(null);
    setMessage(null);
    if (!form.rntrcNumber.trim()) {
      setError('Informe o RNTRC antes de salvar.');
      return;
    }
    try {
      await apiClient.updateRntrc({
        ciotRequired: 'UNKNOWN',
        latestCiotStatus: 'NOT_RECORDED',
        rntrcCategory: 'TAC',
        rntrcNumber: form.rntrcNumber.trim(),
        rntrcStatus: 'ACTIVE',
      });
      setMessage('RNTRC salvo no Cofrete. Para alteracao oficial, use RNTRC Digital com gov.br.');
      await loadCompliance();
    } catch (saveError) {
      setError(friendlyErrorMessage(saveError, 'Nao foi possivel salvar o RNTRC agora.'));
    } finally {
      setIsSavingRntrc(false);
    }
  };

  const saveInsurance = async () => {
    setIsSavingInsurance(true);
    setError(null);
    setMessage(null);
    if (!form.insurer.trim() || !form.insuranceLast4.trim()) {
      setError('Informe seguradora e final da apolice.');
      return;
    }
    try {
      await apiClient.createInsurancePolicy({
        active: true,
        annualPremium: '3600.00',
        currency: 'BRL',
        expiresOn: nextDate(30),
        insurer: form.insurer.trim(),
        linkedRntrc: true,
        monthlyReserve: '300.00',
        pgrRequired: true,
        policyNumberLast4: form.insuranceLast4.trim().slice(-4),
        policyType: 'RCTR_C',
        startsOn: nextDate(-335),
        verificationStatus: 'VERIFIED_BY_DRIVER',
      });
      setMessage('Seguro salvo como metadado. Confirme cobertura com seguradora, SUSEP ou profissional qualificado.');
      await loadCompliance();
    } catch (saveError) {
      setError(friendlyErrorMessage(saveError, 'Nao foi possivel salvar o seguro agora.'));
    } finally {
      setIsSavingInsurance(false);
    }
  };

  const saveDocument = async () => {
    setIsSavingDocument(true);
    setError(null);
    setMessage(null);
    if (!form.documentTitle.trim() || !form.documentLast4.trim()) {
      setError('Informe titulo do documento e identificador final.');
      return;
    }
    try {
      await apiClient.createDocument({
        active: true,
        documentType: 'CRLV',
        expiresOn: nextDate(45),
        identifierLast4: form.documentLast4.trim().slice(-4),
        issuedOn: nextDate(-320),
        notes: 'Organizacao interna. Confirmar canal oficial antes de decisao sensivel.',
        ownerType: 'DRIVER',
        source: 'DRIVER_ENTERED',
        title: form.documentTitle.trim(),
      });
      setMessage('Documento salvo para lembrete. Cofrete nao certifica regularidade oficial.');
      await loadCompliance();
    } catch (saveError) {
      setError(friendlyErrorMessage(saveError, 'Nao foi possivel salvar o documento agora.'));
    } finally {
      setIsSavingDocument(false);
    }
  };

  const profile = result?.profile ?? null;
  const score = result?.score ?? null;
  const alerts = profile?.alerts ?? [];
  const calendarItems = result?.calendarItems ?? [];

  return (
    <>
      <View style={styles.resultPanel}>
        <View style={styles.resultHeader}>
          <View>
            <Text style={styles.sectionTitle}>Status consultivo</Text>
            <Text style={styles.sectionNote}>Organizador interno. Nao atualiza ANTT, gov.br, SUSEP ou seguradora.</Text>
          </View>
          <View style={styles.statusPill}>
            <Text style={styles.statusPillText}>{score ? complianceScoreLabel(score.score.status) : 'Sem dado'}</Text>
          </View>
        </View>
        <View style={styles.metricGrid}>
          <TextMetric label="RNTRC" value={profile?.rntrc.status ?? 'UNKNOWN'} tone="Confirmar em RNTRC Digital" />
          <TextMetric label="Seguro" value={`${profile?.insurance.expiringSoon ?? 0} a revisar`} tone="Validar com seguradora/SUSEP" />
          <TextMetric label="Documentos" value={`${profile?.documents.expired ?? 0} vencidos`} tone="Conferir DETRAN, SEFAZ ou canal oficial" />
          <TextMetric label="Score" value={score ? `${score.score.value}/100` : 'Sem dado'} tone="Indicador consultivo" />
        </View>
        <Text style={styles.caveat}>
          RNTRC Digital e gov.br sao os canais para atualizacao oficial. Cofrete guarda metadados e lembretes.
        </Text>
      </View>

      {isLoading ? (
        <View style={styles.healthRow}>
          <ActivityIndicator color={colors.accent} />
          <Text style={styles.sectionNote}>Carregando alertas e calendario.</Text>
        </View>
      ) : null}
      <StatusMessage error={error} />
      {message ? (
        <View style={styles.successPanel}>
          <Text style={styles.successTitle}>Registro salvo</Text>
          <Text style={styles.successCopy}>{message}</Text>
        </View>
      ) : null}

      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Alertas</Text>
        {alerts.length === 0 ? (
          <Text style={styles.sectionNote}>Sem alertas. Continue conferindo canais oficiais antes de rodar.</Text>
        ) : (
          alerts.map((alert) => (
            <View key={alert.id} style={styles.actionRow}>
              <View style={severityStyle(alert.severity)} />
              <View style={styles.actionCopy}>
                <Text style={styles.actionTitle}>{alert.message}</Text>
                <Text style={styles.actionDetail}>{alert.advisoryText}</Text>
              </View>
            </View>
          ))
        )}
      </View>

      <View style={styles.formSection}>
        <View style={styles.sectionHeader}>
          <Text style={styles.sectionTitle}>Salvar metadados</Text>
          <Text style={styles.sectionNote}>Use dados informados pelo motorista e confira o canal oficial antes de decisao sensivel.</Text>
        </View>
        <Field label="RNTRC" keyboardType="number-pad" value={form.rntrcNumber} onChangeText={updateField('rntrcNumber')} />
        <View style={styles.inlineFields}>
          <Field label="Seguradora" value={form.insurer} onChangeText={updateField('insurer')} />
          <Field label="Final apolice" keyboardType="number-pad" value={form.insuranceLast4} onChangeText={updateField('insuranceLast4')} />
        </View>
        <View style={styles.inlineFields}>
          <Field label="Documento" value={form.documentTitle} onChangeText={updateField('documentTitle')} />
          <Field label="Final doc." keyboardType="number-pad" value={form.documentLast4} onChangeText={updateField('documentLast4')} />
        </View>
        <ActionButton label={isSavingRntrc ? 'Salvando RNTRC' : 'Salvar RNTRC TAC'} loading={isSavingRntrc} onPress={saveRntrc} />
        <ActionButton label={isSavingInsurance ? 'Salvando seguro' : 'Salvar seguro RCTR-C'} loading={isSavingInsurance} onPress={saveInsurance} />
        <ActionButton label={isSavingDocument ? 'Salvando documento' : 'Salvar CRLV'} loading={isSavingDocument} onPress={saveDocument} />
      </View>

      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Calendario</Text>
        {calendarItems.length === 0 ? (
          <Text style={styles.sectionNote}>Nenhum vencimento salvo ainda.</Text>
        ) : (
          calendarItems.map((item) => (
            <ComplianceListRow key={item.id} title={item.title} status={item.status} tone={item.advisoryText} />
          ))
        )}
      </View>

      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Seguros e documentos</Text>
        {result?.insurancePolicies.map((policy) => (
          <ComplianceListRow key={policy.id} title={policy.insurer} status={policy.status} tone={policy.advisoryText} />
        ))}
        {result?.documents.map((document) => (
          <ComplianceListRow key={document.id} title={document.title} status={document.status} tone={document.advisoryText} />
        ))}
        {(result?.insurancePolicies.length ?? 0) + (result?.documents.length ?? 0) === 0 ? (
          <Text style={styles.sectionNote}>Use os botoes acima para salvar os primeiros metadados.</Text>
        ) : null}
      </View>

      <EndpointPanel
        apiBaseUrl={apiClient.baseUrl}
        endpoints={[
          'GET /api/compliance/profile',
          'GET /api/compliance/score',
          'GET /api/compliance/calendar',
          'PUT /api/compliance/rntrc',
          'POST /api/compliance/insurance-policies',
          'GET /api/compliance/insurance-policies',
          'GET /api/documents',
          'POST /api/documents',
        ]}
      />
    </>
  );
}

function FinancialHealthDashboardScreen({ apiClient }: { apiClient: CoreApiClient }) {
  const [result, setResult] = useState<DashboardResult | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const loadDashboard = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    try {
      const [financialHealth, wallets, compliance, overdueReceivables] = await Promise.all([
        apiClient.getFinancialHealthScore(),
        apiClient.getReserveWallets(),
        apiClient.getComplianceProfile(),
        apiClient.getReceivables('overdue'),
      ]);
      setResult({
        compliance,
        financialHealth,
        overdueReceivables,
        wallets: sortWallets(wallets),
      });
    } catch (loadError) {
      setResult({
        compliance: null,
        financialHealth: null,
        overdueReceivables: null,
        wallets: [],
      });
      setError(friendlyErrorMessage(loadError, 'Nao foi possivel carregar a saude financeira agora.'));
    } finally {
      setIsLoading(false);
    }
  }, [apiClient]);

  useEffect(() => {
    void loadDashboard();
  }, [loadDashboard]);

  const financialHealth = result?.financialHealth ?? null;
  const overdueReceivables = result?.overdueReceivables ?? null;
  const compliance = result?.compliance ?? null;
  const alerts = compliance?.alerts ?? [];
  const caveats = dashboardCaveats(financialHealth, compliance);

  return (
    <>
      <View style={styles.safeWithdrawalPanel}>
        <View style={styles.actionCopy}>
          <Text style={styles.metricLabel}>Saude financeira</Text>
          <Text style={styles.safeWithdrawalValue}>
            {financialHealth ? `${financialHealth.score}/100` : 'Sem dado'}
          </Text>
          <Text style={styles.metricTone}>
            Combina reservas, saque planejado, recebiveis vencidos e riscos consultivos.
          </Text>
        </View>
        <View style={styles.statusPill}>
          <Text style={styles.statusPillText}>
            {financialHealth ? healthLabel(financialHealth.status) : 'Sem dado'}
          </Text>
        </View>
      </View>

      {isLoading ? (
        <View style={styles.loadingPanel}>
          <ActivityIndicator color={colors.accent} />
          <Text style={styles.sectionNote}>Carregando reservas, recebiveis e alertas.</Text>
        </View>
      ) : null}

      <StatusMessage error={error} />

      <View style={styles.metricGrid}>
        <TextMetric
          label="Saque seguro"
          value={financialHealth ? formatCurrency(financialHealth.safePersonalWithdrawalAvailable) : 'R$ 0,00'}
          tone="Depois de separar pass-through e reservas obrigatorias"
        />
        <TextMetric
          label="Cobertura"
          value={financialHealth ? formatPercent(financialHealth.reserveCoveragePercent) : '0,00%'}
          tone="Quanto dos alvos de reserva ja esta coberto"
        />
        <TextMetric
          label="Recebiveis"
          value={receivableRiskLabel(overdueReceivables)}
          tone={receivableRiskTone(overdueReceivables)}
        />
        <TextMetric
          label="Alertas"
          value={`${alerts.length}`}
          tone={alerts.length === 1 ? '1 pendencia consultiva' : 'Pendencias consultivas'}
        />
      </View>

      <View style={styles.section}>
        <View style={styles.sectionHeader}>
          <Text style={styles.sectionTitle}>Repasse nao e lucro</Text>
          <Text style={styles.sectionNote}>
            Pedagio reembolsado e Vale-Pedagio ficam fora do lucro e do saque seguro.
          </Text>
        </View>
        <Text style={styles.emptyCopy}>
          O painel resume dinheiro planejado para decisao. Ele nao confirma saldo bancario, recebimento de cliente ou regularidade oficial.
        </Text>
      </View>

      <View style={styles.section}>
        <View style={styles.sectionHeader}>
          <Text style={styles.sectionTitle}>Reservas que seguram o saque</Text>
          <Text style={styles.sectionNote}>
            {financialHealth?.traceId ? `Rastro ${financialHealth.traceId}` : 'Sem rastro de calculo carregado'}
          </Text>
        </View>
        {(financialHealth?.components.length ?? 0) > 0 ? (
          sortHealthComponents(financialHealth?.components ?? []).map((component) => (
            <View key={component.bucket} style={styles.healthRow}>
              <Text style={styles.healthLabel}>{reserveBucketCopy[component.bucket].label}</Text>
              <Text style={styles.healthValue}>{formatPercent(component.coveragePercent)}</Text>
              <Text style={styles.healthMeta}>{healthLabel(component.status)}</Text>
            </View>
          ))
        ) : result?.wallets.length ? (
          result.wallets.slice(0, 4).map((wallet) => <ReserveBucketRow key={wallet.bucket} wallet={wallet} />)
        ) : (
          <Text style={styles.emptyCopy}>Sem baldes de reserva carregados para compor o painel.</Text>
        )}
      </View>

      <View style={styles.section}>
        <View style={styles.sectionHeader}>
          <Text style={styles.sectionTitle}>Risco de recebimento</Text>
          <Text style={styles.sectionNote}>
            Cofrete rastreia previsao de caixa. Nao cobra cliente nem garante pagamento.
          </Text>
        </View>
        {(overdueReceivables?.receivables.length ?? 0) > 0 ? (
          overdueReceivables?.receivables.slice(0, 3).map((receivable) => (
            <View key={receivable.id} style={styles.actionRow}>
              <View style={styles.actionCopy}>
                <Text style={styles.actionTitle}>{receivable.customerName}</Text>
                <Text style={styles.actionDetail}>
                  Venceu em {receivable.dueDate}. Restante {formatCurrency(receivable.remainingAmount)}.
                </Text>
              </View>
              <View style={styles.statusPill}>
                <Text style={styles.statusPillText}>{receivable.status === 'LATE' ? 'Vencido' : healthLabel('ATTENTION')}</Text>
              </View>
            </View>
          ))
        ) : (
          <Text style={styles.emptyCopy}>Nenhum recebivel vencido retornado pelo Core API.</Text>
        )}
      </View>

      <View style={styles.section}>
        <View style={styles.sectionHeader}>
          <Text style={styles.sectionTitle}>Alertas e fontes</Text>
          <Text style={styles.sectionNote}>
            Diesel, pedagio, impostos e documentos dependem de calculos e fontes revisadas.
          </Text>
        </View>
        {alerts.slice(0, 3).map((alert) => (
          <View key={alert.id} style={styles.actionRow}>
            <View style={severityStyle(alert.severity)} />
            <View style={styles.actionCopy}>
              <Text style={styles.actionTitle}>{alert.message}</Text>
              <Text style={styles.actionDetail}>{alert.advisoryText}</Text>
            </View>
          </View>
        ))}
        {alerts.length === 0 ? <Text style={styles.emptyCopy}>Sem alerta consultivo carregado.</Text> : null}
        {caveats.map((caveat) => (
          <Text key={caveat} style={styles.caveat}>
            {caveat}
          </Text>
        ))}
      </View>

      <EndpointPanel
        apiBaseUrl={apiClient.baseUrl}
        endpoints={[
          'GET /api/financial-health-score',
          'GET /api/reserve-wallets',
          'GET /api/receivables?status=overdue',
          'GET /api/compliance/profile',
        ]}
      />
    </>
  );
}

function ReferenceScreen({ apiBaseUrl, routeId }: { apiBaseUrl: string; routeId: AppRouteId }) {
  const activeContent = screenContentByRoute[routeId];

  return (
    <>
      <View style={styles.metricGrid}>
        {activeContent.metrics.map((metric) => (
          <View key={metric.label} style={styles.metricCard}>
            <Text style={styles.metricLabel}>{metric.label}</Text>
            <Text style={styles.metricValue}>
              {metric.kind === 'currency'
                ? currencyFormatter.format(metric.value)
                : metric.value}
            </Text>
            <Text style={styles.metricTone}>{metric.tone}</Text>
          </View>
        ))}
      </View>

      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Fluxo</Text>
        {activeContent.actions.map((action) => (
          <View key={action.title} style={styles.actionRow}>
            <View style={styles.actionMarker} />
            <View style={styles.actionCopy}>
              <Text style={styles.actionTitle}>{action.title}</Text>
              <Text style={styles.actionDetail}>{action.detail}</Text>
            </View>
          </View>
        ))}
      </View>

      <EndpointPanel apiBaseUrl={apiBaseUrl} endpoints={activeContent.endpoints} />
    </>
  );
}

function ReserveWalletScreen({ apiClient }: { apiClient: CoreApiClient }) {
  const [result, setResult] = useState<ReserveWalletResult | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isCreatingRules, setIsCreatingRules] = useState(false);
  const [isAllocating, setIsAllocating] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [actionMessage, setActionMessage] = useState<string | null>(null);

  const loadWallets = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    try {
      const [wallets, financialHealth] = await Promise.all([
        apiClient.getReserveWallets(),
        apiClient.getFinancialHealthScore(),
      ]);
      setResult({ financialHealth, wallets: sortWallets(wallets) });
    } catch (loadError) {
      setResult({ financialHealth: null, wallets: [] });
      setError(friendlyErrorMessage(loadError, 'Nao foi possivel carregar a carteira de reservas agora.'));
    } finally {
      setIsLoading(false);
    }
  }, [apiClient]);

  useEffect(() => {
    void loadWallets();
  }, [loadWallets]);

  const createStarterRules = async () => {
    setActionMessage(null);
    setError(null);
    setIsCreatingRules(true);
    try {
      await Promise.all(starterReserveRules.map((rule) => apiClient.createReserveRule(rule)));
      setActionMessage('Regras starter registradas. Revise percentuais antes de usar em fretes reais.');
      await loadWallets();
    } catch (createError) {
      setError(friendlyErrorMessage(createError, 'Nao foi possivel criar as regras de reserva agora.'));
    } finally {
      setIsCreatingRules(false);
    }
  };

  const requestExampleAllocation = async () => {
    setActionMessage(null);
    setError(null);
    setIsAllocating(true);
    try {
      const allocation = await apiClient.createReserveAllocation(buildReserveAllocationRequest());
      setActionMessage(
        allocation.status === 'REQUESTED'
          ? 'Alocacao solicitada. Os saldos mudam quando o worker financeiro confirmar o evento.'
          : 'Alocacao registrada na carteira virtual.'
      );
      await loadWallets();
    } catch (allocationError) {
      setError(friendlyErrorMessage(allocationError, 'Nao foi possivel solicitar a alocacao agora.'));
    } finally {
      setIsAllocating(false);
    }
  };

  const wallets = result?.wallets ?? [];
  const financialHealth = result?.financialHealth ?? null;

  return (
    <>
      <View style={styles.safeWithdrawalPanel}>
        <View>
          <Text style={styles.metricLabel}>Saque pessoal seguro</Text>
          <Text style={styles.safeWithdrawalValue}>
            {financialHealth ? formatCurrency(financialHealth.safePersonalWithdrawalAvailable) : 'R$ 0,00'}
          </Text>
          <Text style={styles.metricTone}>
            Somente o balde salario do motorista. Reservas obrigatorias continuam separadas.
          </Text>
        </View>
        <View style={styles.statusPill}>
          <Text style={styles.statusPillText}>
            {financialHealth ? healthLabel(financialHealth.status) : 'Sem dado'}
          </Text>
        </View>
      </View>

      {isLoading ? (
        <View style={styles.loadingPanel}>
          <ActivityIndicator color={colors.accent} />
          <Text style={styles.sectionNote}>Carregando saldos e historico virtual.</Text>
        </View>
      ) : null}

      <StatusMessage error={error} />
      {actionMessage ? (
        <View style={styles.successPanel}>
          <Text style={styles.successTitle}>Carteira atualizada</Text>
          <Text style={styles.successCopy}>{actionMessage}</Text>
        </View>
      ) : null}

      {!isLoading && wallets.length === 0 ? <ReserveEmptyState /> : null}

      {wallets.length > 0 ? (
        <View style={styles.section}>
          <View style={styles.sectionHeader}>
            <Text style={styles.sectionTitle}>Baldes de reserva</Text>
            <Text style={styles.sectionNote}>Obrigacoes e saque pessoal ficam em linhas diferentes.</Text>
          </View>
          {wallets.map((wallet) => (
            <ReserveBucketRow key={wallet.bucket} wallet={wallet} />
          ))}
        </View>
      ) : null}

      <View style={styles.section}>
        <View style={styles.sectionHeader}>
          <Text style={styles.sectionTitle}>Historico recente</Text>
          <Text style={styles.sectionNote}>Movimentos sao virtuais. Nao representam transferencia bancaria.</Text>
        </View>
        {wallets.flatMap((wallet) => wallet.transactions.slice(0, 3)).length === 0 ? (
          <Text style={styles.emptyCopy}>Nenhum movimento confirmado pelo worker financeiro ainda.</Text>
        ) : (
          wallets.flatMap((wallet) =>
            wallet.transactions.slice(0, 3).map((transaction) => (
              <View key={transaction.id} style={styles.transactionRow}>
                <Text style={styles.actionTitle}>{reserveBucketCopy[transaction.bucket].label}</Text>
                <Text style={styles.transactionAmount}>{formatCurrency(transaction.amount)}</Text>
                <Text style={styles.metricTone}>{reserveTransactionNote(transaction.note)}</Text>
              </View>
            ))
          )
        )}
      </View>

      <View style={styles.inlineFields}>
        <ActionButton
          label={isCreatingRules ? 'Criando regras' : 'Criar regras starter'}
          loading={isCreatingRules}
          onPress={createStarterRules}
        />
        <ActionButton
          label={isAllocating ? 'Solicitando' : 'Solicitar alocacao virtual'}
          loading={isAllocating}
          onPress={requestExampleAllocation}
        />
      </View>

      <EndpointPanel
        apiBaseUrl={apiClient.baseUrl}
        endpoints={[
          'GET /api/reserve-wallets',
          'POST /api/reserve-rules',
          'POST /api/reserve-allocations',
          'GET /api/financial-health-score',
        ]}
      />
    </>
  );
}

function ReserveEmptyState() {
  return (
    <View style={styles.formSection}>
      <Text style={styles.sectionTitle}>Nenhum balde criado</Text>
      <Text style={styles.emptyCopy}>
        Crie regras starter para ver os nove baldes do blueprint. Sem saldo confirmado, o saque pessoal seguro fica zerado.
      </Text>
      <View style={styles.bucketChecklist}>
        {reserveBucketOrder.map((bucket) => (
          <Text key={bucket} style={styles.bucketChecklistItem}>
            {reserveBucketCopy[bucket].label}
          </Text>
        ))}
      </View>
    </View>
  );
}

function ReserveBucketRow({ wallet }: { wallet: ReserveWalletResponse }) {
  const copy = reserveBucketCopy[wallet.bucket];
  const coverage = reserveCoverage(wallet);

  return (
    <View style={styles.bucketRow}>
      <View style={styles.bucketHeader}>
        <View style={styles.actionCopy}>
          <Text style={styles.actionTitle}>{copy.label}</Text>
          <Text style={styles.metricTone}>{copy.tone}</Text>
        </View>
        <Text style={copy.required ? styles.requiredTag : styles.safeTag}>
          {copy.required ? 'Reserva' : 'Disponivel planejado'}
        </Text>
      </View>
      <View style={styles.bucketNumbers}>
        <Text style={styles.bucketAmount}>{formatCurrency(wallet.currentBalance)}</Text>
        <Text style={styles.metricTone}>
          {wallet.targetBalance ? `Alvo ${formatCurrency(wallet.targetBalance)} (${coverage})` : 'Sem alvo definido'}
        </Text>
      </View>
    </View>
  );
}

function OnboardingScreen({
  apiClient,
  onTruckReady,
}: {
  apiClient: CoreApiClient;
  onTruckReady: (truckId: string) => void;
}) {
  const [form, setForm] = useState(defaultOnboardingForm);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [result, setResult] = useState<OnboardingResult | null>(null);

  const updateField = (field: keyof OnboardingFormState) => (value: string) => {
    setForm((current) => ({ ...current, [field]: value }));
  };

  const submit = async () => {
    setError(null);
    setResult(null);

    if (!form.driverName.trim() || !form.plate.trim()) {
      setError('Informe nome do motorista e placa para criar o perfil.');
      return;
    }

    setIsSubmitting(true);
    try {
      const driver = await apiClient.createDriverProfile(buildDriverRequest(form));
      const truck = await apiClient.createTruckProfile(buildTruckRequest(form));
      onTruckReady(truck.id);
      setResult({ driverId: driver.id, truckId: truck.id });
    } catch (submitError) {
      setError(friendlyErrorMessage(submitError, 'Nao foi possivel criar o perfil agora.'));
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <>
      <View style={styles.formSection}>
        <View style={styles.sectionHeader}>
          <Text style={styles.sectionTitle}>Motorista</Text>
          <Text style={styles.sectionNote}>Dados do app, nao registro oficial ANTT.</Text>
        </View>
        <Field label="Nome" value={form.driverName} onChangeText={updateField('driverName')} />
        <View style={styles.inlineFields}>
          <Field label="UF" value={form.state} onChangeText={updateField('state')} />
          <Field
            label="RNTRC"
            keyboardType="number-pad"
            value={form.rntrcNumber}
            onChangeText={updateField('rntrcNumber')}
          />
        </View>
        <Field label="Email" value={form.email} onChangeText={updateField('email')} />
        <Field label="Telefone" value={form.phone} onChangeText={updateField('phone')} />
      </View>

      <View style={styles.formSection}>
        <View style={styles.sectionHeader}>
          <Text style={styles.sectionTitle}>Caminhao</Text>
          <Text style={styles.sectionNote}>Base para consumo, eixo e custo por km.</Text>
        </View>
        <View style={styles.inlineFields}>
          <Field label="Placa" value={form.plate} onChangeText={updateField('plate')} />
          <Field
            label="Eixos"
            keyboardType="number-pad"
            value={form.axleCount}
            onChangeText={updateField('axleCount')}
          />
        </View>
        <Field
          label="RENAVAM final"
          keyboardType="number-pad"
          value={form.renavamLast4}
          onChangeText={updateField('renavamLast4')}
        />
      </View>

      <ActionButton
        label={isSubmitting ? 'Salvando perfil' : 'Criar perfil e caminhao'}
        loading={isSubmitting}
        onPress={submit}
      />
      <StatusMessage error={error} />

      {result ? (
        <View style={styles.successPanel}>
          <Text style={styles.successTitle}>Perfil pronto para calcular frete</Text>
          <Text style={styles.successCopy}>Motorista: {result.driverId}</Text>
          <Text style={styles.successCopy}>Caminhao: {result.truckId}</Text>
        </View>
      ) : null}

      <EndpointPanel
        apiBaseUrl={apiClient.baseUrl}
        endpoints={['POST /api/drivers', 'POST /api/trucks', 'GET /api/drivers/me', 'GET /api/trucks']}
      />
    </>
  );
}

function FreightCalculatorScreen({
  activeTruckId,
  apiClient,
}: {
  activeTruckId: string;
  apiClient: CoreApiClient;
}) {
  const [form, setForm] = useState(defaultFreightForm);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [result, setResult] = useState<FreightResult | null>(null);

  useEffect(() => {
    setForm((current) =>
      current.truckId === defaultFreightForm.truckId || current.truckId.trim() === ''
        ? { ...current, truckId: activeTruckId }
        : current
    );
  }, [activeTruckId]);

  const updateField = (field: keyof FreightFormState) => (value: string) => {
    setForm((current) => ({ ...current, [field]: value }));
  };

  const submit = async () => {
    setError(null);
    setResult(null);

    if (!form.truckId.trim() || decimalValue(form.grossFreight) <= 0) {
      setError('Informe caminhao e frete bruto maior que zero.');
      return;
    }

    setIsSubmitting(true);
    try {
      const createdTrip = await apiClient.createTrip(buildTripRequest(form));
      const trip = await apiClient.getTrip(createdTrip.id);
      const estimate = await apiClient.requestProfitabilityEstimate(
        trip.id,
        buildProfitabilityRequest(form)
      );
      setResult({ estimate, trip });
    } catch (submitError) {
      setError(friendlyErrorMessage(submitError, 'Nao foi possivel calcular este frete agora.'));
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <>
      <View style={styles.formSection}>
        <View style={styles.sectionHeader}>
          <Text style={styles.sectionTitle}>Frete e rota</Text>
          <Text style={styles.sectionNote}>Use valores do contrato antes do aceite.</Text>
        </View>
        <Field label="ID do caminhao" value={form.truckId} onChangeText={updateField('truckId')} />
        <View style={styles.inlineFields}>
          <Field label="Origem" value={form.originCity} onChangeText={updateField('originCity')} />
          <Field label="UF" value={form.originState} onChangeText={updateField('originState')} />
        </View>
        <View style={styles.inlineFields}>
          <Field
            label="Destino"
            value={form.destinationCity}
            onChangeText={updateField('destinationCity')}
          />
          <Field label="UF destino" value={form.destinationState} onChangeText={updateField('destinationState')} />
        </View>
        <View style={styles.inlineFields}>
          <Field
            label="Km carregado"
            keyboardType="decimal-pad"
            value={form.loadedKm}
            onChangeText={updateField('loadedKm')}
          />
          <Field
            label="Km vazio"
            keyboardType="decimal-pad"
            value={form.emptyKm}
            onChangeText={updateField('emptyKm')}
          />
        </View>
        <Field
          label="Frete bruto"
          keyboardType="decimal-pad"
          value={form.grossFreight}
          onChangeText={updateField('grossFreight')}
        />
        <Field
          label="Carga"
          value={form.cargoDescription}
          onChangeText={updateField('cargoDescription')}
        />
      </View>

      <View style={styles.formSection}>
        <View style={styles.sectionHeader}>
          <Text style={styles.sectionTitle}>Custos e passagem de caixa</Text>
          <Text style={styles.sectionNote}>Pedagio reembolsado e Vale-Pedagio nao viram lucro.</Text>
        </View>
        <View style={styles.inlineFields}>
          <Field
            label="Consumo km/l"
            keyboardType="decimal-pad"
            value={form.dieselConsumptionKmPerLiter}
            onChangeText={updateField('dieselConsumptionKmPerLiter')}
          />
          <Field
            label="Diesel R$/l"
            keyboardType="decimal-pad"
            value={form.dieselPricePerLiter}
            onChangeText={updateField('dieselPricePerLiter')}
          />
        </View>
        <View style={styles.inlineFields}>
          <Field
            label="Pedagio pago"
            keyboardType="decimal-pad"
            value={form.nonReimbursedToll}
            onChangeText={updateField('nonReimbursedToll')}
          />
          <Field
            label="Reembolso pedagio"
            keyboardType="decimal-pad"
            value={form.tollReimbursement}
            onChangeText={updateField('tollReimbursement')}
          />
        </View>
        <View style={styles.inlineFields}>
          <Field
            label="Vale-Pedagio"
            keyboardType="decimal-pad"
            value={form.valePedagio}
            onChangeText={updateField('valePedagio')}
          />
          <Field
            label="ARLA"
            keyboardType="decimal-pad"
            value={form.arlaCost}
            onChangeText={updateField('arlaCost')}
          />
        </View>
        <View style={styles.inlineFields}>
          <Field
            label="Diarias/refeicoes"
            keyboardType="decimal-pad"
            value={form.mealsAndLodgingCost}
            onChangeText={updateField('mealsAndLodgingCost')}
          />
          <Field
            label="Outros custos"
            keyboardType="decimal-pad"
            value={form.otherDirectCost}
            onChangeText={updateField('otherDirectCost')}
          />
        </View>
      </View>

      <View style={styles.formSection}>
        <View style={styles.sectionHeader}>
          <Text style={styles.sectionTitle}>Pagamento</Text>
          <Text style={styles.sectionNote}>Prazo afeta risco de caixa, nao muda lucro por si so.</Text>
        </View>
        <View style={styles.inlineFields}>
          <Field
            label="Adiantamento"
            keyboardType="decimal-pad"
            value={form.advanceAmount}
            onChangeText={updateField('advanceAmount')}
          />
          <Field
            label="Prazo do saldo"
            keyboardType="number-pad"
            value={form.balanceDueDays}
            onChangeText={updateField('balanceDueDays')}
          />
        </View>
        <Field
          label="Parcela do caminhao"
          keyboardType="decimal-pad"
          value={form.financingAllocation}
          onChangeText={updateField('financingAllocation')}
        />
      </View>

      <ActionButton
        label={isSubmitting ? 'Calculando frete' : 'Calcular frete'}
        loading={isSubmitting}
        onPress={submit}
      />
      <StatusMessage error={error} />

      {result ? <ProfitabilityResult result={result} /> : null}

      <EndpointPanel
        apiBaseUrl={apiClient.baseUrl}
        endpoints={[
          'POST /api/trips',
          `GET /api/trips/${result?.trip.id ?? ':tripId'}`,
          `POST /api/trips/${result?.trip.id ?? ':tripId'}/profitability-estimate`,
        ]}
      />
    </>
  );
}

function ProfitabilityResult({ result }: { result: FreightResult }) {
  const { estimate, trip } = result;

  return (
    <View style={styles.resultPanel}>
      <View style={styles.resultHeader}>
        <View>
          <Text style={styles.sectionTitle}>Estimativa do frete</Text>
          <Text style={styles.sectionNote}>
            {trip.origin.city} para {trip.destination.city}, {trip.totalDistanceKm} km
          </Text>
        </View>
        <View style={styles.recommendationPill}>
          <Text style={styles.recommendationText}>{recommendationLabel(estimate.recommendation)}</Text>
        </View>
      </View>

      <View style={styles.metricGrid}>
        <MoneyMetric label="Frete bruto" value={estimate.grossFreight} tone="Receita antes de custos" />
        <MoneyMetric
          label="Repasse de caixa"
          value={estimate.passThroughAmount}
          tone="Reembolso e Vale-Pedagio fora do lucro"
        />
        <MoneyMetric label="Custo real" value={estimate.directTripCost} tone="Diesel, pedagio pago e viagem" />
        <MoneyMetric label="Reservas" value={estimate.requiredReserves} tone="Pneus, manutencao, imposto e futuro" />
        <MoneyMetric label="Lucro esperado" value={estimate.expectedProfit} tone="Depois de custos e reservas" />
        <MoneyMetric
          label="Saque pessoal seguro"
          value={estimate.safePersonalWithdrawal}
          tone="Estimativa de planejamento"
        />
      </View>

      <View style={styles.healthRow}>
        <Text style={styles.healthLabel}>Saude financeira</Text>
        <Text style={styles.healthValue}>{healthLabel(estimate.financialHealthStatus)}</Text>
        <Text style={styles.healthMeta}>Margem {formatPercent(estimate.marginPercent)}</Text>
      </View>

      {estimate.caveats.map((caveat) => (
        <Text key={caveat} style={styles.caveat}>
          {driverFriendlyCaveat(caveat)}
        </Text>
      ))}
    </View>
  );
}

function Field({ keyboardType = 'default', label, onChangeText, value }: FieldProps) {
  return (
    <View style={styles.field}>
      <Text style={styles.fieldLabel}>{label}</Text>
      <TextInput
        accessibilityLabel={label}
        keyboardType={keyboardType}
        onChangeText={onChangeText}
        style={styles.input}
        value={value}
      />
    </View>
  );
}

function ActionButton({
  label,
  loading,
  onPress,
}: {
  label: string;
  loading: boolean;
  onPress: () => void;
}) {
  return (
    <Pressable
      accessibilityRole="button"
      accessibilityState={{ busy: loading, disabled: loading }}
      disabled={loading}
      onPress={onPress}
      style={({ pressed }) => [
        styles.primaryButton,
        loading ? styles.primaryButtonDisabled : null,
        pressed ? styles.primaryButtonPressed : null,
      ]}
    >
      {loading ? <ActivityIndicator color={colors.accentText} /> : null}
      <Text style={styles.primaryButtonText}>{label}</Text>
    </Pressable>
  );
}

function StatusMessage({ error }: { error: string | null }) {
  if (!error) {
    return null;
  }

  return (
    <View style={styles.errorPanel}>
      <Text style={styles.errorText}>{error}</Text>
    </View>
  );
}

function EndpointPanel({ apiBaseUrl, endpoints }: { apiBaseUrl: string; endpoints: string[] }) {
  return (
    <View style={styles.apiPanel}>
      <Text style={styles.sectionTitle}>Contrato Core API</Text>
      <Text style={styles.apiBase}>{apiBaseUrl}</Text>
      {endpoints.map((endpoint) => (
        <Text key={endpoint} style={styles.endpoint}>
          {endpoint}
        </Text>
      ))}
    </View>
  );
}

function MoneyMetric({ label, tone, value }: { label: string; tone: string; value: string }) {
  return (
    <View style={styles.metricCard}>
      <Text style={styles.metricLabel}>{label}</Text>
      <Text style={styles.metricValue}>{formatCurrency(value)}</Text>
      <Text style={styles.metricTone}>{tone}</Text>
    </View>
  );
}

function TextMetric({ label, tone, value }: { label: string; tone: string; value: string }) {
  return (
    <View style={styles.metricCard}>
      <Text style={styles.metricLabel}>{label}</Text>
      <Text style={styles.metricValue}>{value}</Text>
      <Text style={styles.metricTone}>{tone}</Text>
    </View>
  );
}

function ComplianceListRow({
  status,
  title,
  tone,
}: {
  status: ComplianceItemStatus;
  title: string;
  tone: string;
}) {
  return (
    <View style={styles.actionRow}>
      <View style={styles.actionCopy}>
        <Text style={styles.actionTitle}>{title}</Text>
        <Text style={styles.actionDetail}>{tone}</Text>
      </View>
      <View style={styles.statusPill}>
        <Text style={styles.statusPillText}>{complianceItemLabel(status)}</Text>
      </View>
    </View>
  );
}

function severityStyle(severity: ComplianceSeverity) {
  if (severity === 'CRITICAL') {
    return [styles.actionMarker, styles.actionMarkerCritical];
  }
  if (severity === 'WARNING') {
    return [styles.actionMarker, styles.actionMarkerWarning];
  }
  return styles.actionMarker;
}

function complianceItemLabel(status: ComplianceItemStatus) {
  switch (status) {
    case 'CURRENT':
      return 'Em dia';
    case 'EXPIRING_SOON':
      return 'Vence logo';
    case 'EXPIRED':
      return 'Vencido';
    case 'UNKNOWN':
      return 'Sem dado';
  }
}

function complianceScoreLabel(status: ComplianceScoreResponse['score']['status']) {
  switch (status) {
    case 'ATTENTION':
      return 'Atencao';
    case 'GOOD':
      return 'Boa';
    case 'RISK':
      return 'Risco';
  }
}

function nextDate(daysFromToday: number) {
  const date = new Date();
  date.setDate(date.getDate() + daysFromToday);
  return date.toISOString().slice(0, 10);
}

function starterRule(bucket: ReserveBucket, rate: string, targetBalance?: string): ReserveRuleRequest {
  return {
    active: true,
    bucket,
    currency: 'BRL',
    policy: 'PERCENT_OF_AMOUNT',
    rate,
    sourceAssumption: 'Regra starter MVP revisavel pelo motorista',
    targetBalance,
  };
}

function sortWallets(wallets: ReserveWalletResponse[]) {
  return [...wallets].sort(
    (left, right) => reserveBucketOrder.indexOf(left.bucket) - reserveBucketOrder.indexOf(right.bucket)
  );
}

function sortHealthComponents(components: FinancialHealthResponse['components']) {
  return [...components].sort(
    (left, right) => reserveBucketOrder.indexOf(left.bucket) - reserveBucketOrder.indexOf(right.bucket)
  );
}

function receivableRiskLabel(envelope: ReceivablesEnvelope | null) {
  if (!envelope) {
    return 'Sem dado';
  }

  if (decimalValue(envelope.totals.lateAmount) > 0 || envelope.receivables.length > 0) {
    return 'Risco';
  }

  if (decimalValue(envelope.totals.partiallyPaidAmount) > 0) {
    return 'Atencao';
  }

  return 'Boa';
}

function receivableRiskTone(envelope: ReceivablesEnvelope | null) {
  if (!envelope) {
    return 'Core API nao retornou recebiveis';
  }

  if (decimalValue(envelope.totals.lateAmount) > 0 || envelope.receivables.length > 0) {
    return `${formatCurrency(envelope.totals.lateAmount)} vencido para acompanhar`;
  }

  return 'Sem recebivel vencido no filtro atual';
}

function dashboardCaveats(financialHealth: FinancialHealthResponse | null, compliance: ComplianceProfileResponse | null) {
  const caveats = [
    financialHealth?.advisoryText ?? 'Saude financeira e estimativa consultiva, nao garantia de dinheiro disponivel.',
    'Dados de diesel, pedagio, impostos e importacoes precisam de fonte atual para nao distorcer margem.',
    'Cofrete nao e canal oficial governamental, juridico, tributario, contabil ou securitario.',
  ];

  return [...caveats, ...(compliance?.caveats ?? [])].filter(
    (caveat, index, allCaveats) => allCaveats.indexOf(caveat) === index
  );
}

function reserveCoverage(wallet: ReserveWalletResponse) {
  if (!wallet.targetBalance || decimalValue(wallet.targetBalance) <= 0) {
    return 'sem alvo';
  }

  return percentFormatter.format(decimalValue(wallet.currentBalance) / decimalValue(wallet.targetBalance));
}

function buildReserveAllocationRequest(): ReserveAllocationRequest {
  const timestamp = Date.now();
  return {
    allocationRevision: 1,
    allocationSubjectId: `mobile_manual_${timestamp}`,
    currency: 'BRL',
    grossAmount: '8000.00',
    idempotencyKey: `mobile-reserve-${timestamp}`,
    passThroughAmount: '600.00',
    reason: 'FREIGHT_PAYMENT_RECEIVED',
    requestedAt: new Date(timestamp).toISOString(),
  };
}

function buildDriverRequest(form: OnboardingFormState): DriverProfileRequest {
  return {
    active: true,
    cpfCnpjLast4: '4321',
    documentType: 'CPF',
    email: form.email.trim(),
    name: form.driverName.trim(),
    phone: form.phone.trim(),
    rntrcCategory: 'TAC',
    rntrcNumber: form.rntrcNumber.trim(),
    rntrcStatus: 'UNKNOWN',
    state: normalizeState(form.state),
  };
}

function buildTruckRequest(form: OnboardingFormState): TruckProfileRequest {
  return {
    active: true,
    axleCount: Math.max(2, Number.parseInt(form.axleCount, 10) || 2),
    fuelType: 'DIESEL_S10',
    plate: form.plate.trim().toUpperCase(),
    renavamLast4: form.renavamLast4.trim(),
    state: normalizeState(form.state),
    vehicleType: 'TRUCK',
  };
}

function buildTripRequest(form: FreightFormState): TripRequest {
  return {
    advanceAmount: decimalString(form.advanceAmount),
    balanceDueDays: Math.max(0, Number.parseInt(form.balanceDueDays, 10) || 0),
    cargoDescription: form.cargoDescription.trim(),
    currency: 'BRL',
    destination: routePoint(form.destinationCity, form.destinationState),
    emptyKm: decimalString(form.emptyKm),
    grossFreight: decimalString(form.grossFreight),
    loadedKm: decimalString(form.loadedKm),
    origin: routePoint(form.originCity, form.originState),
    truckId: form.truckId.trim(),
  };
}

function buildProfitabilityRequest(form: FreightFormState): ProfitabilityEstimateRequest {
  return {
    arlaCost: decimalString(form.arlaCost),
    dieselConsumptionKmPerLiter: decimalString(form.dieselConsumptionKmPerLiter, 4),
    dieselPricePerLiter: decimalString(form.dieselPricePerLiter, 4),
    financingAllocation: decimalString(form.financingAllocation),
    mealsAndLodgingCost: decimalString(form.mealsAndLodgingCost),
    nonReimbursedToll: decimalString(form.nonReimbursedToll),
    otherDirectCost: decimalString(form.otherDirectCost),
    otherPassThrough: decimalString(form.otherPassThrough),
    reservePolicy: {
      emergencyRate: '0.020000',
      insuranceRate: '0.020000',
      maintenanceRate: '0.080000',
      replacementRate: '0.050000',
      taxRate: '0.030000',
      tireRate: '0.040000',
    },
    sourceFreshness: {
      compliance: 'UNKNOWN',
      fuel: 'CURRENT',
      tax: 'CURRENT',
      toll: 'CURRENT',
    },
    tollReimbursement: decimalString(form.tollReimbursement),
    valePedagio: decimalString(form.valePedagio),
  };
}

function routePoint(city: string, state: string): RoutePoint {
  return {
    city: city.trim(),
    state: normalizeState(state),
  };
}

function normalizeState(state: string) {
  return state.trim().toUpperCase().slice(0, 2);
}

function decimalString(value: string, places = 2) {
  return decimalValue(value).toFixed(places);
}

function decimalValue(value: string) {
  const parsed = Number.parseFloat(value.replace(',', '.'));
  return Number.isFinite(parsed) ? parsed : 0;
}

function formatCurrency(value: string) {
  return currencyFormatter.format(decimalValue(value));
}

function formatPercent(value: string) {
  return percentFormatter.format(decimalValue(value) / 100);
}

function recommendationLabel(value: ProfitabilityEstimateResponse['recommendation']) {
  switch (value) {
    case 'ACCEPT':
      return 'Aceitar';
    case 'REJECT':
      return 'Recusar';
    case 'RENEGOTIATE':
      return 'Renegociar';
  }
}

function healthLabel(value: ProfitabilityEstimateResponse['financialHealthStatus'] | FinancialHealthResponse['status']) {
  switch (value) {
    case 'ATTENTION':
      return 'Atencao';
    case 'GOOD':
      return 'Boa';
    case 'RISK':
      return 'Risco';
    case 'UNKNOWN':
      return 'Sem dado';
    case 'WARNING':
      return 'Atencao';
  }
}

function driverFriendlyCaveat(caveat: string) {
  if (caveat.includes('Toll reimbursement and Vale-Pedagio')) {
    return 'Reembolso de pedagio e Vale-Pedagio sao repasse de caixa, nao lucro.';
  }

  if (caveat.includes('Profitability is advisory planning output')) {
    return 'Estimativa para planejamento. Nao e orientacao legal, tributaria, contabil, securitaria ou oficial.';
  }

  return caveat;
}

function reserveTransactionNote(note: string) {
  if (note.includes('Virtual reserve ledger movement')) {
    return 'Movimento virtual de reserva. Nao e transferencia bancaria real.';
  }

  return note;
}

function friendlyErrorMessage(error: unknown, fallback: string) {
  if (!(error instanceof Error)) {
    return `${fallback} Revise os dados e tente novamente.`;
  }

  if (error.message.includes(' 401')) {
    return 'Sua sessao precisa ser renovada antes de salvar estes dados.';
  }

  if (error.message.includes(' 403')) {
    return 'Este usuario nao tem permissao para alterar estes dados.';
  }

  if (error.message.includes(' 404')) {
    return 'Nao encontramos o registro informado. Revise o caminhao e tente novamente.';
  }

  if (error.message.includes(' 422') || error.message.includes(' 400')) {
    return 'Algum valor nao passou na validacao. Revise os campos e tente novamente.';
  }

  if (error.message.includes(' 429')) {
    return 'Muitas tentativas em pouco tempo. Aguarde um momento e tente novamente.';
  }

  if (error.message.includes(' 500')) {
    return 'O Core API nao conseguiu responder agora. Tente novamente em alguns minutos.';
  }

  return `${fallback} Revise sua conexao e tente novamente.`;
}

const colors = {
  accent: '#0F766E',
  accentBorder: '#A8CEC4',
  accentSoft: '#DDEEE9',
  accentText: '#F7FBF8',
  border: '#D9DED6',
  dangerBg: '#F8E7DF',
  dangerText: '#8A2D14',
  field: '#FFFCF2',
  infoBg: '#E6E7F4',
  infoText: '#343473',
  muted: '#5E6A64',
  panel: '#F2F0E8',
  successBg: '#E3F1DF',
  successText: '#245B34',
  surface: '#FAF9F4',
  text: '#18231F',
};

const styles = StyleSheet.create({
  actionCopy: {
    flex: 1,
    gap: 3,
  },
  actionDetail: {
    color: colors.muted,
    fontSize: 13,
    lineHeight: 18,
  },
  actionMarker: {
    backgroundColor: colors.accent,
    borderRadius: 5,
    height: 10,
    marginTop: 4,
    width: 10,
  },
  actionMarkerCritical: {
    backgroundColor: colors.dangerText,
  },
  actionMarkerWarning: {
    backgroundColor: colors.infoText,
  },
  actionRow: {
    borderTopColor: colors.border,
    borderTopWidth: 1,
    flexDirection: 'row',
    gap: 12,
    paddingVertical: 14,
  },
  actionTitle: {
    color: colors.text,
    fontSize: 15,
    fontWeight: '700',
  },
  account: {
    color: colors.muted,
    fontSize: 13,
    marginTop: 2,
  },
  apiBase: {
    color: colors.text,
    fontSize: 13,
    fontWeight: '600',
    marginBottom: 10,
  },
  apiPanel: {
    backgroundColor: colors.panel,
    borderColor: colors.border,
    borderRadius: 8,
    borderWidth: 1,
    padding: 16,
  },
  appFrame: {
    backgroundColor: colors.surface,
    flex: 1,
  },
  brand: {
    color: colors.text,
    fontSize: 26,
    fontWeight: '800',
  },
  bucketAmount: {
    color: colors.text,
    fontSize: 18,
    fontWeight: '800',
  },
  bucketChecklist: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 8,
  },
  bucketChecklistItem: {
    backgroundColor: colors.field,
    borderColor: colors.border,
    borderRadius: 999,
    borderWidth: 1,
    color: colors.text,
    fontSize: 12,
    fontWeight: '700',
    paddingHorizontal: 10,
    paddingVertical: 6,
  },
  bucketHeader: {
    alignItems: 'flex-start',
    flexDirection: 'row',
    gap: 10,
    justifyContent: 'space-between',
  },
  bucketNumbers: {
    gap: 2,
  },
  bucketRow: {
    borderTopColor: colors.border,
    borderTopWidth: 1,
    gap: 10,
    paddingVertical: 14,
  },
  caveat: {
    color: colors.muted,
    fontSize: 12,
    lineHeight: 17,
  },
  content: {
    gap: 18,
    padding: 18,
    paddingBottom: 24,
  },
  endpoint: {
    color: colors.text,
    fontFamily: 'monospace',
    fontSize: 12,
    lineHeight: 19,
  },
  emptyCopy: {
    color: colors.muted,
    fontSize: 13,
    lineHeight: 18,
  },
  errorPanel: {
    backgroundColor: colors.dangerBg,
    borderColor: colors.dangerText,
    borderRadius: 8,
    borderWidth: 1,
    padding: 12,
  },
  errorText: {
    color: colors.dangerText,
    fontSize: 13,
    fontWeight: '700',
  },
  field: {
    flex: 1,
    gap: 6,
    minWidth: 132,
  },
  fieldLabel: {
    color: colors.muted,
    fontSize: 12,
    fontWeight: '800',
    textTransform: 'uppercase',
  },
  formSection: {
    backgroundColor: colors.surface,
    borderColor: colors.border,
    borderRadius: 8,
    borderWidth: 1,
    gap: 12,
    padding: 16,
  },
  header: {
    alignItems: 'center',
    backgroundColor: colors.surface,
    borderBottomColor: colors.border,
    borderBottomWidth: 1,
    flexDirection: 'row',
    justifyContent: 'space-between',
    paddingHorizontal: 18,
    paddingVertical: 14,
  },
  healthLabel: {
    color: colors.muted,
    flex: 1,
    fontSize: 12,
    fontWeight: '800',
    textTransform: 'uppercase',
  },
  healthMeta: {
    color: colors.muted,
    fontSize: 12,
  },
  healthRow: {
    alignItems: 'center',
    backgroundColor: colors.field,
    borderColor: colors.border,
    borderRadius: 8,
    borderWidth: 1,
    flexDirection: 'row',
    gap: 10,
    padding: 12,
  },
  healthValue: {
    color: colors.text,
    fontSize: 16,
    fontWeight: '800',
  },
  heroPanel: {
    backgroundColor: colors.accentSoft,
    borderColor: colors.accentBorder,
    borderRadius: 8,
    borderWidth: 1,
    gap: 8,
    padding: 18,
  },
  inlineFields: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 12,
  },
  input: {
    backgroundColor: colors.field,
    borderColor: colors.border,
    borderRadius: 8,
    borderWidth: 1,
    color: colors.text,
    fontSize: 15,
    minHeight: 46,
    paddingHorizontal: 12,
    paddingVertical: 10,
  },
  loadingPanel: {
    alignItems: 'center',
    backgroundColor: colors.panel,
    borderColor: colors.border,
    borderRadius: 8,
    borderWidth: 1,
    flexDirection: 'row',
    gap: 12,
    padding: 14,
  },
  kicker: {
    color: colors.accent,
    fontSize: 12,
    fontWeight: '800',
    textTransform: 'uppercase',
  },
  metricCard: {
    backgroundColor: colors.field,
    borderColor: colors.border,
    borderRadius: 8,
    borderWidth: 1,
    flexBasis: '48%',
    flexGrow: 1,
    gap: 6,
    minWidth: 132,
    minHeight: 104,
    padding: 14,
  },
  metricGrid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 12,
  },
  metricLabel: {
    color: colors.muted,
    fontSize: 12,
    fontWeight: '700',
    textTransform: 'uppercase',
  },
  metricTone: {
    color: colors.muted,
    fontSize: 12,
    lineHeight: 16,
  },
  metricValue: {
    color: colors.text,
    fontSize: 21,
    fontWeight: '800',
  },
  primaryButton: {
    alignItems: 'center',
    backgroundColor: colors.accent,
    borderRadius: 8,
    flexDirection: 'row',
    gap: 8,
    justifyContent: 'center',
    minHeight: 50,
    paddingHorizontal: 16,
  },
  primaryButtonDisabled: {
    opacity: 0.72,
  },
  primaryButtonPressed: {
    opacity: 0.88,
  },
  primaryButtonText: {
    color: colors.accentText,
    fontSize: 15,
    fontWeight: '800',
  },
  recommendationPill: {
    backgroundColor: colors.successBg,
    borderRadius: 999,
    paddingHorizontal: 12,
    paddingVertical: 7,
  },
  recommendationText: {
    color: colors.successText,
    fontSize: 12,
    fontWeight: '900',
    textTransform: 'uppercase',
  },
  resultHeader: {
    alignItems: 'flex-start',
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 12,
    justifyContent: 'space-between',
  },
  resultPanel: {
    backgroundColor: colors.surface,
    borderColor: colors.accent,
    borderRadius: 8,
    borderWidth: 1,
    gap: 14,
    padding: 16,
  },
  safeArea: {
    backgroundColor: colors.surface,
    flex: 1,
  },
  safeTag: {
    backgroundColor: colors.successBg,
    borderRadius: 999,
    color: colors.successText,
    fontSize: 11,
    fontWeight: '800',
    paddingHorizontal: 10,
    paddingVertical: 6,
    textTransform: 'uppercase',
  },
  safeWithdrawalPanel: {
    alignItems: 'flex-start',
    backgroundColor: colors.field,
    borderColor: colors.accentBorder,
    borderRadius: 8,
    borderWidth: 1,
    flexDirection: 'row',
    gap: 14,
    justifyContent: 'space-between',
    padding: 16,
  },
  safeWithdrawalValue: {
    color: colors.text,
    fontSize: 24,
    fontWeight: '800',
    lineHeight: 30,
  },
  section: {
    backgroundColor: colors.surface,
    borderColor: colors.border,
    borderRadius: 8,
    borderWidth: 1,
    padding: 16,
  },
  sectionHeader: {
    gap: 3,
  },
  sectionNote: {
    color: colors.muted,
    fontSize: 12,
    lineHeight: 17,
  },
  sectionTitle: {
    color: colors.text,
    fontSize: 15,
    fontWeight: '800',
    marginBottom: 2,
  },
  statusPill: {
    backgroundColor: colors.infoBg,
    borderRadius: 999,
    paddingHorizontal: 12,
    paddingVertical: 6,
  },
  statusPillText: {
    color: colors.infoText,
    fontSize: 12,
    fontWeight: '800',
  },
  requiredTag: {
    backgroundColor: colors.infoBg,
    borderRadius: 999,
    color: colors.infoText,
    fontSize: 11,
    fontWeight: '800',
    paddingHorizontal: 10,
    paddingVertical: 6,
    textTransform: 'uppercase',
  },
  successCopy: {
    color: colors.successText,
    fontSize: 13,
    lineHeight: 18,
  },
  successPanel: {
    backgroundColor: colors.successBg,
    borderColor: colors.successText,
    borderRadius: 8,
    borderWidth: 1,
    gap: 4,
    padding: 14,
  },
  successTitle: {
    color: colors.successText,
    fontSize: 15,
    fontWeight: '800',
  },
  summary: {
    color: colors.text,
    fontSize: 15,
    lineHeight: 21,
  },
  tabBar: {
    backgroundColor: colors.surface,
    borderTopColor: colors.border,
    borderTopWidth: 1,
    flexDirection: 'row',
    gap: 6,
    paddingHorizontal: 8,
    paddingVertical: 8,
  },
  tabButton: {
    borderRadius: 8,
    minHeight: 52,
    paddingHorizontal: 4,
    paddingVertical: 7,
  },
  tabIcon: {
    color: colors.muted,
    fontSize: 16,
  },
  tabLabel: {
    fontSize: 11,
    fontWeight: '800',
  },
  title: {
    color: colors.text,
    fontSize: 24,
    fontWeight: '800',
    lineHeight: 30,
  },
  transactionAmount: {
    color: colors.text,
    fontSize: 15,
    fontWeight: '800',
  },
  transactionRow: {
    borderTopColor: colors.border,
    borderTopWidth: 1,
    gap: 4,
    paddingVertical: 12,
  },
});
