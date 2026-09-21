import { NavLink } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import { ROLES } from '../../utils/constants';

/** Menu lateral : les entrees sont filtrees selon le role de l'utilisateur. */
const MENU = [
  {
    groupe: null,
    entrees: [{ vers: '/tableau-bord', icone: '▤', libelle: 'Tableau de bord', roles: 'tous' }],
  },
  {
    groupe: 'Problèmes',
    entrees: [
      { vers: '/problemes', icone: '☰', libelle: 'Tous les problèmes', roles: [ROLES.RESPONSABLE_SUPPORT, ROLES.ADMINISTRATEUR] },
      { vers: '/declarer', icone: '＋', libelle: 'Déclarer un problème', roles: 'tous' },
      { vers: '/mes-declarations', icone: '◫', libelle: 'Mes déclarations', roles: 'tous' },
      { vers: '/mes-affectations', icone: '✎', libelle: 'Mes affectations', roles: [ROLES.TECHNICIEN] },
    ],
  },
  {
    groupe: 'Pilotage',
    entrees: [
      { vers: '/statistiques', icone: '◔', libelle: 'Statistiques', roles: [ROLES.RESPONSABLE_SUPPORT, ROLES.ADMINISTRATEUR] },
    ],
  },
  {
    groupe: 'Administration',
    entrees: [
      { vers: '/utilisateurs', icone: '☺', libelle: 'Utilisateurs', roles: [ROLES.ADMINISTRATEUR] },
      { vers: '/categories', icone: '⌗', libelle: 'Catégories', roles: [ROLES.ADMINISTRATEUR] },
      { vers: '/departements', icone: '⌂', libelle: 'Départements', roles: [ROLES.ADMINISTRATEUR] },
    ],
  },
  {
    groupe: 'Compte',
    entrees: [
      { vers: '/notifications', icone: '✉', libelle: 'Notifications', roles: 'tous' },
      { vers: '/profil', icone: '⚙', libelle: 'Mon profil', roles: 'tous' },
    ],
  },
];

export function Sidebar() {
  const { role } = useAuth();

  const visible = (entree) => entree.roles === 'tous' || entree.roles.includes(role);

  return (
    <aside className="sidebar">
      <div className="sidebar__marque">
        <strong>Gestion des problèmes IT</strong>
        <span>Portfolio Demo</span>
      </div>

      <nav className="sidebar__nav">
        {MENU.map((section) => {
          const entrees = section.entrees.filter(visible);
          if (entrees.length === 0) return null;
          return (
            <div className="sidebar__groupe" key={section.groupe || 'principal'}>
              {section.groupe && <div className="sidebar__groupe-titre">{section.groupe}</div>}
              {entrees.map((entree) => (
                <NavLink
                  key={entree.vers}
                  to={entree.vers}
                  className={({ isActive }) =>
                    `sidebar__lien ${isActive ? 'sidebar__lien--actif' : ''}`
                  }
                >
                  <span className="sidebar__icone">{entree.icone}</span>
                  <span>{entree.libelle}</span>
                </NavLink>
              ))}
            </div>
          );
        })}
      </nav>

    </aside>
  );
}
