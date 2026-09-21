import { Navigate, Route, Routes } from 'react-router-dom';
import { Layout } from './components/layout/Layout';
import { AuthProvider } from './context/AuthContext';
import { ConnexionPage } from './pages/ConnexionPage';
import { DeclarationPage } from './pages/DeclarationPage';
import { MesProblemesPage } from './pages/MesProblemesPage';
import { NonTrouvePage } from './pages/NonTrouvePage';
import { NotificationsPage } from './pages/NotificationsPage';
import { ProblemeDetailPage } from './pages/ProblemeDetailPage';
import { ProblemesPage } from './pages/ProblemesPage';
import { ProfilPage } from './pages/ProfilPage';
import { ReferentielsPage } from './pages/ReferentielsPage';
import { StatistiquesPage } from './pages/StatistiquesPage';
import { TableauBordPage } from './pages/TableauBordPage';
import { UtilisateursPage } from './pages/UtilisateursPage';
import { RouteProtegee } from './routes/RouteProtegee';
import { ROLES } from './utils/constants';

export default function App() {
  return (
    <AuthProvider>
      <Routes>
        {/* -------- Acces public -------- */}
        <Route path="/connexion" element={<ConnexionPage />} />

        {/* -------- Espace authentifie (RG1) -------- */}
        <Route
          element={
            <RouteProtegee>
              <Layout />
            </RouteProtegee>
          }
        >
          <Route index element={<Navigate to="/tableau-bord" replace />} />
          <Route path="/tableau-bord" element={<TableauBordPage />} />
          <Route path="/problemes/:id" element={<ProblemeDetailPage />} />
          <Route path="/notifications" element={<NotificationsPage />} />
          <Route path="/profil" element={<ProfilPage />} />

          {/* Supervision */}
          <Route
            path="/problemes"
            element={
              <RouteProtegee roles={[ROLES.RESPONSABLE_SUPPORT, ROLES.ADMINISTRATEUR]}>
                <ProblemesPage />
              </RouteProtegee>
            }
          />
          <Route
            path="/statistiques"
            element={
              <RouteProtegee roles={[ROLES.RESPONSABLE_SUPPORT, ROLES.ADMINISTRATEUR]}>
                <StatistiquesPage />
              </RouteProtegee>
            }
          />

          {/* Declaration et suivi */}
          {/* La declaration est ouverte a tous les roles : un technicien peut
              signaler une anomalie qu'il constate sur le parc. */}
          <Route path="/declarer" element={<DeclarationPage />} />
          <Route path="/mes-declarations" element={<MesProblemesPage mode="declarations" />} />
          <Route
            path="/mes-affectations"
            element={
              <RouteProtegee roles={[ROLES.TECHNICIEN]}>
                <MesProblemesPage mode="affectations" />
              </RouteProtegee>
            }
          />

          {/* Administration */}
          <Route
            path="/utilisateurs"
            element={
              <RouteProtegee roles={[ROLES.ADMINISTRATEUR]}>
                <UtilisateursPage />
              </RouteProtegee>
            }
          />
          <Route
            path="/categories"
            element={
              <RouteProtegee roles={[ROLES.ADMINISTRATEUR]}>
                <ReferentielsPage mode="categories" />
              </RouteProtegee>
            }
          />
          <Route
            path="/departements"
            element={
              <RouteProtegee roles={[ROLES.ADMINISTRATEUR]}>
                <ReferentielsPage mode="departements" />
              </RouteProtegee>
            }
          />

          <Route path="*" element={<NonTrouvePage />} />
        </Route>
      </Routes>
    </AuthProvider>
  );
}
