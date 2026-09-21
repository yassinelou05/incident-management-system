import axios from 'axios';

/**
 * Client HTTP centralise.
 * Intercepteur de requete  : ajoute le jeton JWT a l'en-tete Authorization.
 * Intercepteur de reponse  : normalise les erreurs et deconnecte sur 401.
 */
const axiosClient = axios.create({
  baseURL: import.meta.env.VITE_API_URL || '/api',
  headers: { 'Content-Type': 'application/json' },
  timeout: 15000,
});

export const CLE_TOKEN = 'gpt.token';
export const CLE_PROFIL = 'gpt.profil';

axiosClient.interceptors.request.use((config) => {
  const token = localStorage.getItem(CLE_TOKEN);
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

axiosClient.interceptors.response.use(
  (reponse) => reponse,
  (erreur) => {
    const statut = erreur.response?.status;
    const donnees = erreur.response?.data;

    if (statut === 401 && !erreur.config?.url?.includes('/auth/login')) {
      localStorage.removeItem(CLE_TOKEN);
      localStorage.removeItem(CLE_PROFIL);
      if (window.location.pathname !== '/connexion') {
        window.location.href = '/connexion';
      }
    }

    return Promise.reject({
      statut,
      message:
        donnees?.message ||
        (statut === 403 ? "Vous n'avez pas l'autorisation d'effectuer cette action." : null) ||
        erreur.message ||
        'Une erreur est survenue.',
      champs: donnees?.champs || null,
    });
  },
);

export default axiosClient;
