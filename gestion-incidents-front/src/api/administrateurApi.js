import axiosClient from './axiosClient';
import { versUtilisateur } from './adaptateurs';

/** Authentification et gestion des comptes (AdministrateurController cote backend). */
export const administrateurApi = {
  // ---- Authentification ----
  connexion: (email, motDePasse) =>
    axiosClient.post('/auth/login', { email, motDePasse }).then((r) => r.data),

  profil: () => axiosClient.get('/auth/moi').then((r) => versUtilisateur(r.data)),

  permissions: () => axiosClient.get('/auth/permissions').then((r) => r.data),

  changerMotDePasse: (ancienMotDePasse, nouveauMotDePasse) =>
    axiosClient
      .patch('/auth/mot-de-passe', { ancienMotDePasse, nouveauMotDePasse })
      .then((r) => r.data),

  deconnexion: () => axiosClient.post('/auth/logout').then((r) => r.data),

  // ---- Comptes utilisateurs ----
  listerUtilisateurs: (filtres = {}) =>
    axiosClient.get('/utilisateurs', { params: filtres }).then((r) => r.data.map(versUtilisateur)),

  consulterUtilisateur: (id) =>
    axiosClient.get(`/utilisateurs/${id}`).then((r) => versUtilisateur(r.data)),

  creerUtilisateur: (donnees) =>
    axiosClient.post('/utilisateurs', donnees).then((r) => versUtilisateur(r.data)),

  modifierUtilisateur: (id, donnees) =>
    axiosClient.put(`/utilisateurs/${id}`, donnees).then((r) => versUtilisateur(r.data)),

  activer: (id) =>
    axiosClient.patch(`/utilisateurs/${id}/activer`).then((r) => versUtilisateur(r.data)),

  desactiver: (id) =>
    axiosClient.patch(`/utilisateurs/${id}/desactiver`).then((r) => versUtilisateur(r.data)),

  suspendre: (id) =>
    axiosClient.patch(`/utilisateurs/${id}/suspendre`).then((r) => versUtilisateur(r.data)),

  changerDisponibilite: (id, disponible) =>
    axiosClient
      .patch(`/utilisateurs/${id}/disponibilite`, null, { params: { disponible } })
      .then((r) => versUtilisateur(r.data)),

  reinitialiserMotDePasse: (id) =>
    axiosClient.patch(`/utilisateurs/${id}/reinitialiser-mot-de-passe`).then((r) => r.data),
};
