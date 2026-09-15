import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { AuthProvider, useAuth } from './hooks/useAuth';
import Layout from './components/Layout';
import Login from './pages/Login';
import ForcePasswordChange from './pages/ForcePasswordChange';
import Dashboard from './pages/Dashboard';
import Players from './pages/Players';
import Parents from './pages/Parents';
import Payments from './pages/Payments';
import ParentPayments from './pages/ParentPayments';
import Training from './pages/Training';
import Coaches from './pages/Coaches';
import Categories from './pages/Categories';
import Import from './pages/Import';
import Presence from './pages/Presence';
import Events from './pages/Events';
import Academies from './pages/Academies';
import SuperAdminDashboard from './pages/SuperAdminDashboard';
import Invitations from './pages/Invitations';
import AcceptInvitation from './pages/AcceptInvitation';
import Billing from './pages/Billing';
import Onboarding from './pages/Onboarding';
import Rgpd from './pages/Rgpd';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: { retry: 1, refetchOnWindowFocus: false }
  }
});

function ProtectedRoute({ children }) {
  const { user, loading } = useAuth();
  if (loading) return <div className="flex items-center justify-center h-screen">Chargement...</div>;
  if (!user) return <Navigate to="/login" replace />;
  if (user.mustChangePassword) return <ForcePasswordChange />;
  return children;
}

function SuperAdminDashboardWrapper() {
  const { user } = useAuth();
  if (user?.role === 'SUPER_ADMIN') return <SuperAdminDashboard />;
  return <Dashboard />;
}

function AppRoutes() {
  const { user } = useAuth();

  return (
    <Routes>
      <Route path="/login" element={user ? <Navigate to="/" replace /> : <Login />} />
      <Route path="/invite/:token" element={<AcceptInvitation />} />
      <Route path="/onboarding" element={<Onboarding />} />
      <Route path="/" element={<ProtectedRoute><Layout /></ProtectedRoute>}>
        <Route index element={<SuperAdminDashboardWrapper />} />
        <Route path="super-admin" element={<SuperAdminDashboard />} />
        <Route path="players" element={<Players />} />
        <Route path="parents" element={<Parents />} />
        <Route path="payments" element={<Payments />} />
        <Route path="my-payments" element={<ParentPayments />} />
        <Route path="training" element={<Training />} />
        <Route path="coaches" element={<Coaches />} />
        <Route path="categories" element={<Categories />} />
        <Route path="presence" element={<Presence />} />
        <Route path="events" element={<Events />} />
        <Route path="academies" element={<Academies />} />
        <Route path="invitations" element={<Invitations />} />
        <Route path="billing" element={<Billing />} />
        <Route path="rgpd" element={<Rgpd />} />
        <Route path="import" element={<Import />} />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}

export default function App() {
  return (
    <BrowserRouter>
      <QueryClientProvider client={queryClient}>
        <AuthProvider>
          <AppRoutes />
        </AuthProvider>
      </QueryClientProvider>
    </BrowserRouter>
  );
}
