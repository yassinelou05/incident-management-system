import axiosClient from './axiosClient';
import { versCommentaire } from './adaptateurs';

export const commentaireApi = {
  lister: (idProbleme) =>
    axiosClient.get(`/problemes/${idProbleme}/commentaires`).then((r) => r.data.map(versCommentaire)),

  ajouter: (idProbleme, contenu, interne = false) =>
    axiosClient
      .post(`/problemes/${idProbleme}/commentaires`, { contenu, interne })
      .then((r) => versCommentaire(r.data)),

  modifier: (id, contenu) =>
    axiosClient.put(`/commentaires/${id}`, { contenu }).then((r) => versCommentaire(r.data)),

  supprimer: (id) => axiosClient.delete(`/commentaires/${id}`).then((r) => r.data),
};
