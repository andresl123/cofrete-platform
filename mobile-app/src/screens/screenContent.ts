import { CORE_API_ENDPOINTS } from '../api/contracts';
import { type AppRouteId } from '../navigation/routes';

type CurrencyMetric = {
  kind: 'currency';
  label: string;
  tone: string;
  value: number;
};

type TextMetric = {
  kind: 'text';
  label: string;
  tone: string;
  value: string;
};

type Metric = CurrencyMetric | TextMetric;

type ScreenAction = {
  detail: string;
  title: string;
};

type ScreenContent = {
  actions: ScreenAction[];
  endpoints: string[];
  metrics: Metric[];
  summary: string;
  title: string;
};

export const screenContentByRoute: Record<AppRouteId, ScreenContent> = {
  compliance: {
    actions: [
      {
        detail: 'RNTRC, seguro, documentos e calendario ficam separados de login gov.br.',
        title: 'Registrar metadados',
      },
      {
        detail: 'Avisos usam linguagem de risco e apontam para canais oficiais quando houver decisao sensivel.',
        title: 'Revisar pendencias',
      },
    ],
    endpoints: [
      CORE_API_ENDPOINTS.complianceProfile,
      CORE_API_ENDPOINTS.complianceScore,
      CORE_API_ENDPOINTS.complianceCalendar,
    ],
    metrics: [
      {
        kind: 'text',
        label: 'RNTRC',
        tone: 'Dado informado pelo motorista',
        value: 'Pendente',
      },
      {
        kind: 'text',
        label: 'Seguro',
        tone: 'Sem certificacao oficial',
        value: 'Revisar',
      },
    ],
    summary:
      'Acompanhamento consultivo de RNTRC, seguros e documentos sem substituir sistemas oficiais.',
    title: 'Riscos antes de aceitar e rodar',
  },
  dashboard: {
    actions: [
      {
        detail: 'Combina lucro esperado, reservas, recebiveis e alertas de dados desatualizados.',
        title: 'Ler saude financeira',
      },
      {
        detail: 'Abre o fluxo certo quando uma area precisa de dado novo ou revisao.',
        title: 'Atacar o maior risco',
      },
    ],
    endpoints: [
      CORE_API_ENDPOINTS.financialHealthScore,
      CORE_API_ENDPOINTS.reserveWallets,
      CORE_API_ENDPOINTS.complianceProfile,
    ],
    metrics: [
      {
        kind: 'currency',
        label: 'Saque seguro',
        tone: 'Depois de custos e reservas',
        value: 1850,
      },
      {
        kind: 'text',
        label: 'Saude',
        tone: 'Baseada em dados do Core API',
        value: 'Atencao',
      },
    ],
    summary:
      'Visao inicial para planejar saque pessoal depois de custos, reservas e riscos.',
    title: 'Saque seguro sem confundir caixa com lucro',
  },
  freight: {
    actions: [
      {
        detail: 'Captura frete bruto, rota, diesel, pedagios, custos diretos e prazo de pagamento.',
        title: 'Montar decisao de viagem',
      },
      {
        detail: 'Mostra custo real, reservas obrigatorias, lucro esperado e saque seguro.',
        title: 'Calcular antes do aceite',
      },
    ],
    endpoints: [
      CORE_API_ENDPOINTS.trips,
      CORE_API_ENDPOINTS.tripProfitabilityEstimate(':tripId'),
      CORE_API_ENDPOINTS.tripProfitabilitySnapshot(':tripId'),
    ],
    metrics: [
      {
        kind: 'currency',
        label: 'Frete bruto',
        tone: 'Receita antes dos custos',
        value: 8000,
      },
      {
        kind: 'currency',
        label: 'Lucro esperado',
        tone: 'Pedagio reembolsado nao entra',
        value: 2650,
      },
    ],
    summary:
      'Formulario-base para avaliar frete com diesel, pedagio, ARLA, prazo de pagamento e reservas.',
    title: 'Frete novo com margem auditavel',
  },
  onboarding: {
    actions: [
      {
        detail: 'Conta do app, perfil do motorista, RNTRC informado e regime tributario caminham separados.',
        title: 'Criar identidade Cofrete',
      },
      {
        detail: 'Consumo, placa, RENAVAM e perfil de uso ficam prontos para calculos de viagem.',
        title: 'Cadastrar caminhao',
      },
    ],
    endpoints: [
      CORE_API_ENDPOINTS.register,
      CORE_API_ENDPOINTS.createDriver,
      CORE_API_ENDPOINTS.driverProfile,
      CORE_API_ENDPOINTS.trucks,
    ],
    metrics: [
      {
        kind: 'text',
        label: 'Perfil',
        tone: 'MVP motorista unico',
        value: 'Driver',
      },
      {
        kind: 'text',
        label: 'Caminhao',
        tone: 'Dados para custo por km',
        value: '1 ativo',
      },
    ],
    summary:
      'Cadastro inicial para o motorista autonomo e seu caminhao sem ativar modo empresa no MVP.',
    title: 'Base do motorista e do veiculo',
  },
  reserves: {
    actions: [
      {
        detail: 'Combustivel, manutencao, pneus, impostos, seguro, troca do caminhao e emergencia ficam separados.',
        title: 'Ver saldos por balde',
      },
      {
        detail: 'Movimentos virtuais de reserva nao representam transferencia bancaria real.',
        title: 'Auditar lancamentos',
      },
    ],
    endpoints: [
      CORE_API_ENDPOINTS.reserveRules,
      CORE_API_ENDPOINTS.reserveWallets,
      CORE_API_ENDPOINTS.reserveAllocations,
    ],
    metrics: [
      {
        kind: 'currency',
        label: 'Reservas',
        tone: 'Alvo minimo do mes',
        value: 4200,
      },
      {
        kind: 'currency',
        label: 'Saque seguro',
        tone: 'Planejamento, nao garantia',
        value: 1850,
      },
    ],
    summary:
      'Carteira virtual para separar obrigacoes futuras do dinheiro que parece disponivel no caixa.',
    title: 'Reserva nao e lucro',
  },
};
