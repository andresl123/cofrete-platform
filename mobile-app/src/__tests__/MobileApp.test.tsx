import { render, screen, userEvent } from '@testing-library/react-native';

import { MobileApp } from '../MobileApp';
import { MVP_ROUTES } from '../navigation/routes';
import { screenContentByRoute } from '../screens/screenContent';

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
    expect(screen.getByText('Pedagio reembolsado nao entra')).toBeOnTheScreen();

    await user.press(screen.getByRole('button', { name: 'Reservas' }));
    expect(screen.getByText('Planejamento, nao garantia')).toBeOnTheScreen();
    expect(screen.getByText(/nao representam transferencia bancaria real/)).toBeOnTheScreen();
  });
});
