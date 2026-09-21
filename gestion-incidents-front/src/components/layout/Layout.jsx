import { Outlet, useLocation } from 'react-router-dom';
import { Sidebar } from './Sidebar';
import { Topbar } from './Topbar';

const TITRES = {
  '/tableau-bord': 'Tableau de bord',
  '/problemes': 'Liste des problèmes',
  '/declarer': 'Déclarer un problème',
  '/mes-declarations': 'Mes déclarations',
  '/mes-affectations': 'Mes affectations',
  '/statistiques': 'Statistiques et indicateurs',
  '/utilisateurs': 'Gestion des utilisateurs',
  '/categories': 'Gestion des catégories',
  '/departements': 'Gestion des départements',
  '/notifications': 'Notifications',
  '/profil': 'Mon profil',
};

export function Layout() {
  const { pathname } = useLocation();
  const titre =
    TITRES[pathname] || (pathname.startsWith('/problemes/') ? 'Détail du problème' : 'Application');

  return (
    <div className="app">
      <Sidebar />
      <div className="principal">
        <Topbar titre={titre} />
        <main className="contenu">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
