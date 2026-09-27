import { NavLink, Outlet } from 'react-router-dom';
import './AppLayout.css';

const links = [
  { to: '/', label: 'Dashboard', end: true },
  { to: '/orders', label: 'Orders' },
  { to: '/inventory', label: 'Inventory' },
  { to: '/payments', label: 'Payments' },
  { to: '/notifications', label: 'Notifications' },
];

function AppLayout() {
  return (
    <div className="app-shell">
      <header className="app-header">
        <h1 className="app-brand">Order Management System</h1>
        <p className="app-tagline">Microservices demo UI</p>
      </header>
      <div className="app-body">
        <nav className="app-nav" aria-label="Main">
          {links.map((link) => (
            <NavLink
              key={link.to}
              to={link.to}
              end={link.end}
              className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')}
            >
              {link.label}
            </NavLink>
          ))}
        </nav>
        <main className="app-main">
          <Outlet />
        </main>
      </div>
    </div>
  );
}

export default AppLayout;
