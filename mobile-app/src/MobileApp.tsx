import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import { NavigationContainer } from '@react-navigation/native';
import { StatusBar } from 'expo-status-bar';
import { useMemo } from 'react';
import {
  ScrollView,
  StyleSheet,
  Text,
  View,
} from 'react-native';

import { createCoreApiClient } from './api/client';
import { getCoreApiBaseUrl } from './config/env';
import { MVP_ROUTES, type AppRouteId } from './navigation/routes';
import { screenContentByRoute } from './screens/screenContent';

type RootTabParamList = Record<AppRouteId, undefined>;

const Tab = createBottomTabNavigator<RootTabParamList>();

const currencyFormatter = new Intl.NumberFormat('pt-BR', {
  currency: 'BRL',
  style: 'currency',
});

export function MobileApp() {
  return (
    <NavigationContainer>
      <StatusBar style="dark" />
      <Tab.Navigator
        initialRouteName="dashboard"
        screenOptions={{
          headerShown: false,
          tabBarActiveBackgroundColor: '#0F766E',
          tabBarActiveTintColor: '#FFFFFF',
          tabBarInactiveTintColor: '#4B5563',
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
            {() => <MvpScreen routeId={route.id} />}
          </Tab.Screen>
        ))}
      </Tab.Navigator>
    </NavigationContainer>
  );
}

type MvpScreenProps = {
  routeId: AppRouteId;
};

function MvpScreen({ routeId }: MvpScreenProps) {
  const activeRoute = MVP_ROUTES.find((route) => route.id === routeId) ?? MVP_ROUTES[0];
  const activeContent = screenContentByRoute[routeId];
  const apiClient = useMemo(() => createCoreApiClient(getCoreApiBaseUrl()), []);

  return (
    <View style={styles.safeArea}>
      <StatusBar style="dark" />
      <View style={styles.appFrame}>
        <View style={styles.header}>
          <View>
            <Text style={styles.brand}>Cofrete</Text>
            <Text style={styles.account}>TAC autonomo</Text>
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

          <View style={styles.apiPanel}>
            <Text style={styles.sectionTitle}>Contrato Core API</Text>
            <Text style={styles.apiBase}>{apiClient.baseUrl}</Text>
            {activeContent.endpoints.map((endpoint) => (
              <Text key={endpoint} style={styles.endpoint}>
                {endpoint}
              </Text>
            ))}
          </View>
        </ScrollView>

      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  actionCopy: {
    flex: 1,
    gap: 3,
  },
  actionDetail: {
    color: '#4B5563',
    fontSize: 13,
    lineHeight: 18,
  },
  actionMarker: {
    backgroundColor: '#0F766E',
    borderRadius: 5,
    height: 10,
    marginTop: 4,
    width: 10,
  },
  actionRow: {
    borderTopColor: '#E5E7EB',
    borderTopWidth: 1,
    flexDirection: 'row',
    gap: 12,
    paddingVertical: 14,
  },
  actionTitle: {
    color: '#111827',
    fontSize: 15,
    fontWeight: '700',
  },
  account: {
    color: '#6B7280',
    fontSize: 13,
    marginTop: 2,
  },
  apiBase: {
    color: '#374151',
    fontSize: 13,
    fontWeight: '600',
    marginBottom: 10,
  },
  apiPanel: {
    backgroundColor: '#F9FAFB',
    borderColor: '#D1D5DB',
    borderRadius: 8,
    borderWidth: 1,
    padding: 16,
  },
  appFrame: {
    backgroundColor: '#FFFFFF',
    flex: 1,
  },
  brand: {
    color: '#111827',
    fontSize: 26,
    fontWeight: '800',
  },
  content: {
    gap: 18,
    padding: 18,
    paddingBottom: 24,
  },
  endpoint: {
    color: '#1F2937',
    fontFamily: 'monospace',
    fontSize: 12,
    lineHeight: 19,
  },
  header: {
    alignItems: 'center',
    borderBottomColor: '#E5E7EB',
    borderBottomWidth: 1,
    flexDirection: 'row',
    justifyContent: 'space-between',
    paddingHorizontal: 18,
    paddingVertical: 14,
  },
  heroPanel: {
    backgroundColor: '#EEF6F3',
    borderColor: '#B7D7CE',
    borderRadius: 8,
    borderWidth: 1,
    gap: 8,
    padding: 18,
  },
  kicker: {
    color: '#0F766E',
    fontSize: 12,
    fontWeight: '800',
    textTransform: 'uppercase',
  },
  metricCard: {
    backgroundColor: '#FFFFFF',
    borderColor: '#E5E7EB',
    borderRadius: 8,
    borderWidth: 1,
    flexBasis: '48%',
    flexGrow: 1,
    gap: 6,
    minHeight: 104,
    padding: 14,
  },
  metricGrid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 12,
  },
  metricLabel: {
    color: '#6B7280',
    fontSize: 12,
    fontWeight: '700',
    textTransform: 'uppercase',
  },
  metricTone: {
    color: '#4B5563',
    fontSize: 12,
    lineHeight: 16,
  },
  metricValue: {
    color: '#111827',
    fontSize: 21,
    fontWeight: '800',
  },
  safeArea: {
    backgroundColor: '#FFFFFF',
    flex: 1,
  },
  section: {
    backgroundColor: '#FFFFFF',
    borderColor: '#E5E7EB',
    borderRadius: 8,
    borderWidth: 1,
    padding: 16,
  },
  sectionTitle: {
    color: '#111827',
    fontSize: 15,
    fontWeight: '800',
    marginBottom: 8,
  },
  statusPill: {
    backgroundColor: '#E0E7FF',
    borderRadius: 999,
    paddingHorizontal: 12,
    paddingVertical: 6,
  },
  statusPillText: {
    color: '#3730A3',
    fontSize: 12,
    fontWeight: '800',
  },
  summary: {
    color: '#374151',
    fontSize: 15,
    lineHeight: 21,
  },
  tabBar: {
    borderTopColor: '#E5E7EB',
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
    color: '#4B5563',
    fontSize: 16,
  },
  tabLabel: {
    fontSize: 10,
    fontWeight: '800',
  },
  title: {
    color: '#111827',
    fontSize: 24,
    fontWeight: '800',
    lineHeight: 30,
  },
});
