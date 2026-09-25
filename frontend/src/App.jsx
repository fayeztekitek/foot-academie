import { useState, useEffect } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { AuthProvider, useAuth } from './hooks/useAuth';
import Layout from './components/Layout';
import Login from './pages/Login';
import ForcePasswordChange from './pages/ForcePasswordChange';
import Dashboard from './pages/Dashboard';
import Players from './pages/Players';
import PlayerDetail from './pages/PlayerDetail';
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
    queries: { retry: 1, refetchOnWindowFocus: false },
    mutations: {
      onError: (error) => {
        // Bean-validation 400s carry an `errors` map instead of `message`:
        // surface the field details so users know what to fix.
        const details = error?.response?.data?.errors;
        const detailMsg = details && typeof details === 'object'
          ? Object.entries(details).map(([field, msg]) => `${field} : ${msg}`).join(' — ')
          : null;
        const msg = error?.response?.data?.message || detailMsg || error?.message || 'Une erreur est survenue';
        console.error('[Mutation error]', msg);
        if (typeof window !== 'undefined') {
          window.dispatchEvent(new CustomEvent('nadi-toast', { detail: { type: 'error', message: msg } }));
        }
      }
    }
  }
});

function ProtectedRoute({ children }) {
  const { user, loading } = useAuth();
  if (loading) return <div className="flex items-center justify-center h-screen">Chargement...</div>;
  if (!user) return <Navigate to="/login" replace />;
  if (user.mustChangePassword) return <ForcePasswordChange />;
  return children;
}

// Defense in depth: the backend enforces roles on every endpoint, but the UI
// must not mount admin screens (and fire their API calls) for unauthorized
// roles. Previously any authenticated user could open these by URL.
function RoleRoute({ roles, children }) {
  const { user, loading } = useAuth();
  if (loading) return <div className="flex items-center justify-center h-screen">Chargement...</div>;
  if (!user) return <Navigate to="/login" replace />;
  if (user.mustChangePassword) return <ForcePasswordChange />;
  if (!roles.includes(user?.role)) return <Navigate to="/" replace />;
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
      <Route path="/onboarding" element={user?.role === 'SUPER_ADMIN' ? <Onboarding /> : <Navigate to="/login" replace />} />
      <Route path="/" element={<ProtectedRoute><Layout /></ProtectedRoute>}>
        <Route index element={<SuperAdminDashboardWrapper />} />
        <Route path="super-admin" element={<RoleRoute roles={['SUPER_ADMIN']}><SuperAdminDashboard /></RoleRoute>} />
        <Route path="players" element={<Players />} />
        <Route path="players/:id" element={<PlayerDetail />} />
        <Route path="parents" element={<Parents />} />
        <Route path="payments" element={<Payments />} />
        <Route path="my-payments" element={<ParentPayments />} />
        <Route path="training" element={<Training />} />
        <Route path="coaches" element={<Coaches />} />
        <Route path="categories" element={<Categories />} />
        <Route path="presence" element={<Presence />} />
        <Route path="events" element={<Events />} />
        <Route path="academies" element={<RoleRoute roles={['SUPER_ADMIN']}><Academies /></RoleRoute>} />
        <Route path="invitations" element={<RoleRoute roles={['ADMIN', 'SUPER_ADMIN']}><Invitations /></RoleRoute>} />
        <Route path="billing" element={<RoleRoute roles={['ADMIN', 'SUPER_ADMIN']}><Billing /></RoleRoute>} />
        <Route path="rgpd" element={<RoleRoute roles={['ADMIN', 'SUPER_ADMIN']}><Rgpd /></RoleRoute>} />
        <Route path="import" element={<RoleRoute roles={['ADMIN', 'SUPER_ADMIN']}><Import /></RoleRoute>} />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}

// Global toast renderer: mutation errors are broadcast as 'nadi-toast'
// events (see queryClient above). Without this listener every failure was
// silent — the button spun, then nothing happened.
function ToastListener() {
  const [toast, setToast] = useState(null);

  useEffect(() => {
    let timer;
    const handler = (e) => {
      setToast(e.detail);
      clearTimeout(timer);
      timer = setTimeout(() => setToast(null), 5000);
    };
    window.addEventListener('nadi-toast', handler);
    return () => {
      window.removeEventListener('nadi-toast', handler);
      clearTimeout(timer);
    };
  }, []);

  if (!toast) return null;
  const type = toast.type === 'success' ? 'toast-success' : 'toast-error';
  return <div className={`toast ${type}`}>{toast.message}</div>;
}

export default function App() {
  return (
    <BrowserRouter>
      <QueryClientProvider client={queryClient}>
        <AuthProvider>
          <AppRoutes />
          <ToastListener />
        </AuthProvider>
      </QueryClientProvider>
    </BrowserRouter>
  );
}
