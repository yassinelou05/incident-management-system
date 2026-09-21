import axiosClient from './axiosClient';
import {
  versAffectation,
  versDetailProbleme,
  versPage,
  versProbleme,
} from './adaptateurs';

/** Cycle de vie des problemes techniques. */
export const problemeApi = {
  rechercher: (params = {}) =>
    axiosClient.get('/problemes', { params }).then((r) => versPage(r.data, versProbleme)),

  consulter: (id) => axiosClient.get(`/problemes/${id}`).then((r) => versDetailProbleme(r.data)),

  mesDeclarations: () =>
    axiosClient.get('/problemes/mes-declarations').then((r) => r.data.map(versProbleme)),

  mesAffectations: () =>
    axiosClient.get('/problemes/mes-affectations').then((r) => r.data.map(versProbleme)),

  declarer: (donnees) => axiosClient.post('/problemes', donnees).then((r) => versProbleme(r.data)),

  modifier: (id, donnees) =>
    axiosClient.put(`/problemes/${id}`, donnees).then((r) => versProbleme(r.data)),

  // ---- Supervision ----
  affecter: (id, idTechnicien, noteAffectation) =>
    axiosClient
      .patch(`/problemes/${id}/affecter`, { idTechnicien, noteAffectation })
      .then((r) => versAffectation(r.data)),

  reaffecter: (id, idTechnicien, noteAffectation) =>
    axiosClient
      .patch(`/problemes/${id}/reaffecter`, { idTechnicien, noteAffectation })
      .then((r) => versAffectation(r.data)),

  affectations: (id) =>
    axiosClient.get(`/problemes/${id}/affectations`).then((r) => r.data.map(versAffectation)),

  suggestionTechnicien: (id) =>
    axiosClient.get(`/problemes/${id}/suggestion-technicien`).then((r) => r.data),

  definirPriorite: (id, priorite, motif) =>
    axiosClient.patch(`/problemes/${id}/priorite`, { priorite, motif }).then((r) => versProbleme(r.data)),

  // ---- Traitement ----
  prendreEnCharge: (id) =>
    axiosClient.patch(`/problemes/${id}/prendre-en-charge`).then((r) => versProbleme(r.data)),

  enregistrerDiagnostic: (id, diagnostic) =>
    axiosClient.patch(`/problemes/${id}/diagnostic`, { diagnostic }).then((r) => versProbleme(r.data)),

  changerStatut: (id, statut, motif) =>
    axiosClient.patch(`/problemes/${id}/statut`, { statut, motif }).then((r) => versProbleme(r.data)),

  // ---- Resolution et cloture ----
  resoudre: (id, solution) =>
    axiosClient.patch(`/problemes/${id}/resoudre`, { solution }).then((r) => versProbleme(r.data)),

  confirmerResolution: (id, confirme, commentaire) =>
    axiosClient.patch(`/problemes/${id}/confirmer`, { confirme, commentaire }).then((r) => versProbleme(r.data)),

  cloturer: (id, motif) =>
    axiosClient.patch(`/problemes/${id}/cloturer`, null, { params: { motif } }).then((r) => versProbleme(r.data)),
};
