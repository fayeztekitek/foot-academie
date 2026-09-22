import { createContext, useContext, useState, useEffect } from 'react';
import { authApi } from '../api/auth';
import { isDemoMode } from '../demo/demoConfig';
import { handleMockRequest } from '../demo/mockApi';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (isDemoMode()) {
      const demoRole = localStorage.getItem('demo_user_role') || 'ADMIN';
      const demoEmail = localStorage.getItem('demo_user_email') || 'admin@nadi.tn';
      setUser({ token: 'demo-token', role: demoRole, email: demoEmail, mustChangePassword: false });
      setLoading(false);
      return;
    }
    const token = localStorage.getItem('accessToken');
    const role = localStorage.getItem('userRole');
    const email = localStorage.getItem('userEmail');
    const mustChange = localStorage.getItem('mustChangePassword') === 'true';
    const tenantId = localStorage.getItem('tenantId');
    if (token && role && email) {
      setUser({ token, role, email, mustChangePassword: mustChange, tenantId: tenantId ? Number(tenantId) : null });
    }
    setLoading(false);
  }, []);

  const login = async (email, password, tenantId) => {
    if (isDemoMode()) {
      const result = await handleMockRequest('POST', '/auth/login', { email, motDePasse: password, tenantId });
      const { data } = result;
      localStorage.setItem('demo_user_role', data.role);
      localStorage.setItem('demo_user_email', data.email);
      setUser({ token: data.accessToken, role: data.role, email: data.email, mustChangePassword: false, tenantId: data.tenantId });
      return data;
    }
    localStorage.removeItem('nadi_demo_mode');
    const { data } = await authApi.login(email, password, tenantId);
    localStorage.setItem('accessToken', data.accessToken);
    localStorage.setItem('refreshToken', data.refreshToken);
    localStorage.setItem('userRole', data.role);
    localStorage.setItem('userEmail', data.email);
    localStorage.setItem('mustChangePassword', String(data.mustChangePassword));
    if (data.tenantId) localStorage.setItem('tenantId', String(data.tenantId));
    setUser({ token: data.accessToken, role: data.role, email: data.email, mustChangePassword: data.mustChangePassword, tenantId: data.tenantId });
    return data;
  };

  const changePassword = async (ancienMotDePasse, nouveauMotDePasse) => {
    await authApi.changePassword(ancienMotDePasse, nouveauMotDePasse);
    localStorage.setItem('mustChangePassword', 'false');
    setUser(prev => ({ ...prev, mustChangePassword: false }));
  };

  const logout = () => {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('userRole');
    localStorage.removeItem('userEmail');
    localStorage.removeItem('mustChangePassword');
    localStorage.removeItem('tenantId');
    localStorage.removeItem('demo_user_role');
    localStorage.removeItem('demo_user_email');
    setUser(null);
  };

  const isAdmin = user?.role === 'ADMIN';
  const isCoach = user?.role === 'COACH';
  const isParent = user?.role === 'PARENT';

  return (
    <AuthContext.Provider value={{ user, login, logout, loading, isAdmin, isCoach, isParent, changePassword }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
