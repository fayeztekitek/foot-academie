import { Outlet, NavLink, useNavigate, useLocation } from 'react-router-dom';
import { useState, useEffect } from 'react';
import { useAuth } from '../hooks/useAuth';
import { useDemo } from '../demo/DemoProvider';
import { usePushNotifications } from '../hooks/usePushNotifications';
import NotificationBell from './NotificationBell';
import { useOnlineStatus } from '../hooks/useOnlineStatus';
import { LayoutDashboard, Users, UserCog, CreditCard, Calendar, GraduationCap, FolderOpen, Upload, Menu, X, LogOut, Bell, Smartphone, ClipboardCheck, Trophy, Building2, BarChart3, Mail, Shield } from 'lucide-react';

const NAV_ITEMS = [
  { to: '/', label: 'Tableau de bord', icon: LayoutDashboard, roles: ['ADMIN', 'COACH', 'PARENT'] },
  { to: '/super-admin', label: 'Dashboard', icon: BarChart3, roles: ['SUPER_ADMIN'] },
  { to: '/academies', label: 'Académies', icon: Building2, roles: ['SUPER_ADMIN'] },
  { to: '/invitations', label: 'Invitations', icon: Mail, roles: ['ADMIN'] },
  { to: '/billing', label: 'Facturation', icon: CreditCard, roles: ['ADMIN'] },
  { to: '/rgpd', label: 'RGPD', icon: Shield, roles: ['ADMIN'] },
  { to: '/players', label: 'Joueurs', icon: Users, roles: ['ADMIN', 'COACH'] },
  { to: '/parents', label: 'Parents', icon: UserCog, roles: ['ADMIN'] },
  { to: '/payments', label: 'Paiements', icon: CreditCard, roles: ['ADMIN'] },
  { to: '/my-payments', label: 'Mes paiements', icon: CreditCard, roles: ['PARENT'] },
  { to: '/training', label: 'Entraînements', icon: Calendar, roles: ['ADMIN', 'COACH', 'PARENT'] },
  { to: '/coaches', label: 'Entraîneurs', icon: GraduationCap, roles: ['ADMIN'] },
  { to: '/presence', label: 'Présence', icon: ClipboardCheck, roles: ['ADMIN', 'COACH'] },
  { to: '/events', label: 'Événements', icon: Trophy, roles: ['ADMIN', 'COACH', 'PARENT'] },
  { to: '/categories', label: 'Créneaux', icon: FolderOpen, roles: ['ADMIN'] },
  { to: '/import', label: 'Import', icon: Upload, roles: ['ADMIN'] },
];

const BOTTOM_NAV_ITEMS = [
  { to: '/', label: 'Accueil', icon: LayoutDashboard, roles: ['ADMIN', 'COACH', 'PARENT'] },
  { to: '/players', label: 'Joueurs', icon: Users, roles: ['ADMIN', 'COACH'] },
  { to: '/training', label: 'Planning', icon: Calendar, roles: ['ADMIN', 'COACH', 'PARENT'] },
  { to: '/payments', label: 'Paiements', icon: CreditCard, roles: ['ADMIN'] },
  { to: '/my-payments', label: 'Paiements', icon: CreditCard, roles: ['PARENT'] },
  { to: '/super-admin', label: 'Dashboard', icon: BarChart3, roles: ['SUPER_ADMIN'] },
];

function getInitials(email) {
  if (!email) return '?';
  const name = email.split('@')[0];
  return name.slice(0, 2).toUpperCase();
}

export default function Layout() {
  const { user, logout } = useAuth();
  const { isDemo } = useDemo();
  const online = useOnlineStatus();
  usePushNotifications();
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();

  const filteredNav = NAV_ITEMS.filter(item => item.roles.includes(user?.role));
  const filteredBottomNav = BOTTOM_NAV_ITEMS.filter(item => item.roles.includes(user?.role));

  useEffect(() => {
    setSidebarOpen(false);
  }, [location.pathname]);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const currentPage = filteredNav.find(n => {
    if (n.to === '/') return location.pathname === '/';
    return location.pathname.startsWith(n.to);
  });

  return (
    <div className="flex min-h-[100dvh] bg-bg">
      {/* Mobile overlay */}
      {sidebarOpen && (
        <div
          className="fixed inset-0 z-40 bg-black/40 backdrop-blur-sm lg:hidden animate-fade-in"
          onClick={() => setSidebarOpen(false)}
          aria-label="Fermer le menu"
        />
      )}

      {/* Sidebar — desktop always visible, mobile slide-in */}
      <aside
        className={`
          fixed top-0 left-0 h-full z-50 flex flex-col
          w-[240px] shrink-0 text-white
          transition-transform duration-250 ease-out
          lg:relative lg:translate-x-0
          ${sidebarOpen ? 'translate-x-0' : '-translate-x-full'}
        `}
        style={{ background: 'linear-gradient(180deg, #0F1D17 0%, #152B21 100%)' }}
        role="navigation"
        aria-label="Menu principal"
      >
        {/* Brand */}
        <div className="flex items-center gap-3 px-5 py-5 border-b border-white/10">
          <div className="w-9 h-9 rounded-xl flex items-center justify-center font-bebas text-sm"
            style={{ background: 'linear-gradient(135deg, #22884E, #C9A227)' }}>
            N
          </div>
          <div>
            <div className="font-bebas text-lg leading-none text-white tracking-wide">NADI</div>
            <div className="text-[10px] text-white/50 tracking-[.08em] mt-0.5">ACADÉMIE DE FOOTBALL</div>
          </div>
        </div>

        {isDemo && (
          <div className="mx-3 mt-3 mb-1 flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-medium bg-gold/15 text-gold">
            <Smartphone size={12} />
            Mode Démo
          </div>
        )}

        {/* Nav */}
        <nav className="flex flex-col px-2.5 py-2 gap-0.5 flex-1 overflow-y-auto">
          {filteredNav.map(item => (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.to === '/'}
              onClick={() => setSidebarOpen(false)}
              className={({ isActive }) =>
                `flex items-center gap-2.5 px-3 py-2.5 rounded-lg text-[13.5px] font-medium no-underline transition-all duration-150 ${
                  isActive
                    ? 'bg-white/10 text-white shadow-sm'
                    : 'text-white/60 hover:bg-white/5 hover:text-white/90'
                }`
              }
            >
              <item.icon size={17} className="shrink-0 opacity-80" />
              <span>{item.label}</span>
            </NavLink>
          ))}
        </nav>

        {/* User footer */}
        <div className="px-4 py-3.5 border-t border-white/10 flex items-center gap-2.5">
          <div className="w-8 h-8 rounded-full flex items-center justify-center text-xs font-bold"
            style={{ background: 'linear-gradient(135deg, #22884E, #1A3A2D)' }}>
            {getInitials(user?.email)}
          </div>
          <div className="text-xs min-w-0 flex-1">
            <div className="text-white font-medium truncate">{user?.email?.split('@')[0]}</div>
            <div className="text-white/40 text-[10px] uppercase tracking-wider">{user?.role}</div>
          </div>
          <button
            onClick={handleLogout}
            className="p-1.5 rounded-lg text-white/40 hover:text-white hover:bg-white/10 transition-colors"
            aria-label="Déconnexion"
          >
            <LogOut size={15} />
          </button>
        </div>
      </aside>

      {/* Main content */}
      <main className="flex-1 min-w-0 flex flex-col">
        {/* Topbar — hidden on mobile (replaced by bottom nav) */}
        <div className="hidden lg:flex items-center justify-between px-6 py-3.5 border-b bg-white sticky top-0 z-30"
          style={{ borderColor: 'var(--line)' }}>
          <div className="flex items-center gap-3">
            <div>
              <h1 className="font-bebas text-[24px] m-0 leading-none" style={{ color: 'var(--pitch-dark)' }}>
                {currentPage?.label || 'Nadi'}
              </h1>
              <div className="text-[11px] mt-0.5 text-ink-soft">
                Saison 2026/2027
              </div>
            </div>
          </div>
          <div className="flex items-center gap-2.5">
            <div className="w-9 h-9 rounded-full border flex items-center justify-center relative cursor-pointer hover:bg-gray-50 transition-colors"
              style={{ borderColor: 'var(--line)' }}>
              <Bell size={15} className="text-ink-soft" />
              <NotificationBell />
            </div>
            <span className={`text-[11px] font-medium px-2.5 py-1 rounded-full ${online ? 'bg-grass-light text-grass-dark' : 'bg-red-light text-red-dark'}`}>
              {online ? 'En ligne' : 'Hors ligne'}
            </span>
            <button onClick={handleLogout} className="btn-ghost btn-sm hidden xl:flex">
              <LogOut size={13} />
              <span>Déconnexion</span>
            </button>
          </div>
        </div>

        {/* Mobile topbar */}
        <div className="flex lg:hidden items-center justify-between px-4 py-3 bg-white sticky top-0 z-30 border-b"
          style={{ borderColor: 'var(--line)' }}>
          <button
            className="flex items-center justify-center w-10 h-10 rounded-lg hover:bg-gray-100 transition-colors"
            onClick={() => setSidebarOpen(true)}
            aria-label="Ouvrir le menu"
          >
            <Menu size={20} className="text-ink" />
          </button>
          <div className="font-bebas text-lg text-pitch-dark tracking-wide">NADI</div>
          <div className="w-10 h-10 flex items-center justify-center relative">
            <Bell size={18} className="text-ink-soft" />
            <NotificationBell />
          </div>
        </div>

        {/* Page content */}
        <div className="flex-1 px-4 py-5 pb-24 lg:px-8 lg:py-7 lg:pb-8">
          <div className="animate-fade-in">
            <Outlet />
          </div>
        </div>
      </main>

      {/* Mobile bottom navigation */}
      <nav className="bottom-nav" role="navigation" aria-label="Navigation mobile">
        {filteredBottomNav.map(item => (
          <NavLink
            key={item.to}
            to={item.to}
            end={item.to === '/'}
            className={({ isActive }) =>
              `bottom-nav-item ${isActive ? 'active' : ''}`
            }
          >
            <item.icon size={22} strokeWidth={1.8} />
            <span>{item.label}</span>
          </NavLink>
        ))}
      </nav>
    </div>
  );
}
