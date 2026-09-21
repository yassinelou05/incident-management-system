import { createContext, useCallback, useEffect, useMemo, useState } from 'react';
import { administrateurApi } from '../api/administrateurApi';
import { CLE_PROFIL, CLE_TOKEN } from '../api/axiosClient';
import { ROLES } from '../utils/constants';

export const AuthContext = createContext(null);

/**
 * Fournit le profil de l'utilisateur connecte, le jeton JWT et les helpers
 * de controle d'acces utilises par les routes protegees et le menu lateral.
 */
export function AuthProvider({ children }) {
  const [utilisateur, setUtilisateur] = useState(null);
  const [chargement, setChargement] = useState(true);

  useEffect(() => {
    const token = localStorage.getItem(CLE_TOKEN);
    const profilStocke = localStorage.getItem(CLE_PROFIL);

    if (!token) {
      setChargement(false);
      return;
    }

    if (profilStocke) {
      try {
        setUtilisateur(JSON.parse(profilStocke));
      } catch {
        localStorage.removeItem(CLE_PROFIL);
      }
    }

    // Revalidation du jeton aupres du backend
    administrateurApi
      .profil()
      .then((profil) => {
        setUtilisateur((precedent) => ({ ...precedent, ...profil }));
        localStorage.setItem(CLE_PROFIL, JSON.stringify(profil));
      })
      .catch(() => {
        localStorage.removeItem(CLE_TOKEN);
        localStorage.removeItem(CLE_PROFIL);
        setUtilisateur(null);
      })
      .finally(() => setChargement(false));
  }, []);

  const connexion = useCallback(async (email, motDePasse) => {
    const reponse = await administrateurApi.connexion(email, motDePasse);
    localStorage.setItem(CLE_TOKEN, reponse.token);

    const profil = {
      idUtilisateur: reponse.idUtilisateur,
      nomComplet: reponse.nomComplet,
      email: reponse.email,
      role: reponse.role,
      departement: reponse.departement,
      permissions: reponse.permissions,
    };
    localStorage.setItem(CLE_PROFIL, JSON.stringify(profil));
    setUtilisateur(profil);
    return profil;
  }, []);

  const deconnexion = useCallback(() => {
    administrateurApi.deconnexion().catch(() => undefined);
    localStorage.removeItem(CLE_TOKEN);
    localStorage.removeItem(CLE_PROFIL);
    setUtilisateur(null);
  }, []);

  const valeur = useMemo(() => {
    const role = utilisateur?.role;
    return {
      utilisateur,
      chargement,
      connexion,
      deconnexion,
      estConnecte: Boolean(utilisateur),
      role,
      aRole: (...roles) => roles.includes(role),
      estAdministrateur: role === ROLES.ADMINISTRATEUR,
      estResponsable: role === ROLES.RESPONSABLE_SUPPORT,
      estTechnicien: role === ROLES.TECHNICIEN,
      estEmploye: role === ROLES.EMPLOYE,
      peutSuperviser: role === ROLES.RESPONSABLE_SUPPORT || role === ROLES.ADMINISTRATEUR,
    };
  }, [utilisateur, chargement, connexion, deconnexion]);

  return <AuthContext.Provider value={valeur}>{children}</AuthContext.Provider>;
}
