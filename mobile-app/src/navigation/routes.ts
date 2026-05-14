export type AppRouteId =
  | 'dashboard'
  | 'onboarding'
  | 'freight'
  | 'reserves'
  | 'compliance';

export type AppRoute = {
  id: AppRouteId;
  icon: string;
  label: string;
  shortLabel: string;
};

export const MVP_ROUTES: AppRoute[] = [
  {
    id: 'dashboard',
    icon: '$',
    label: 'Dashboard financeiro',
    shortLabel: 'Inicio',
  },
  {
    id: 'onboarding',
    icon: '+',
    label: 'Motorista e caminhao',
    shortLabel: 'Perfil',
  },
  {
    id: 'freight',
    icon: '>',
    label: 'Novo frete',
    shortLabel: 'Frete',
  },
  {
    id: 'reserves',
    icon: '%',
    label: 'Carteira de reservas',
    shortLabel: 'Reservas',
  },
  {
    id: 'compliance',
    icon: '!',
    label: 'Centro de conformidade',
    shortLabel: 'Riscos',
  },
];
