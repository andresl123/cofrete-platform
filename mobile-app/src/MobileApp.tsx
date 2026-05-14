import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import { NavigationContainer } from '@react-navigation/native';
import { StatusBar } from 'expo-status-bar';
import { useEffect, useMemo, useState } from 'react';
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
  type DriverProfileRequest,
  type ProfitabilityEstimateRequest,
  type ProfitabilityEstimateResponse,
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

type OnboardingResult = {
  driverId: string;
  truckId: string;
};

type FreightResult = {
  estimate: ProfitabilityEstimateResponse;
  trip: TripResponse;
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
          ) : (
            <ReferenceScreen routeId={routeId} apiBaseUrl={apiClient.baseUrl} />
          )}
        </ScrollView>
      </View>
    </View>
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

function healthLabel(value: ProfitabilityEstimateResponse['financialHealthStatus']) {
  switch (value) {
    case 'GOOD':
      return 'Boa';
    case 'RISK':
      return 'Risco';
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
});
