import axiosClient from './axiosClient';
import { versUtilisateur } from './adaptateurs';

/** Categories, departements, techniciens et enumerations. */
export const referentielApi = {
  // ---- Categories ----
  listerCategories: (activesSeulement = false) =>
    axiosClient.get('/categories', { params: { activesSeulement } }).then((r) => r.data),

  creerCategorie: (donnees) => axiosClient.post('/categories', donnees).then((r) => r.data),

  modifierCategorie: (id, donnees) =>
    axiosClient.put(`/categories/${id}`, donnees).then((r) => r.data),

  changerActivationCategorie: (id, active) =>
    axiosClient.patch(`/categories/${id}/activation`, null, { params: { active } }).then((r) => r.data),

  supprimerCategorie: (id) => axiosClient.delete(`/categories/${id}`).then((r) => r.data),

  // ---- Departements ----
  listerDepartements: () => axiosClient.get('/departements').then((r) => r.data),

  creerDepartement: (donnees) => axiosClient.post('/departements', donnees).then((r) => r.data),

  modifierDepartement: (id, donnees) =>
    axiosClient.put(`/departements/${id}`, donnees).then((r) => r.data),

  supprimerDepartement: (id) => axiosClient.delete(`/departements/${id}`).then((r) => r.data),

  // ---- Divers ----
  listerTechniciens: () => axiosClient.get('/techniciens').then((r) => r.data.map(versUtilisateur)),

  enumerations: () => axiosClient.get('/referentiels/enumerations').then((r) => r.data),
};
