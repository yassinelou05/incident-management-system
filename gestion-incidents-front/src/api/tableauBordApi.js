import axiosClient from './axiosClient';
import { versTableauBord } from './adaptateurs';

export const tableauBordApi = {
  /** Vue adaptee au role de l'utilisateur connecte. */
  personnel: () => axiosClient.get('/tableau-bord').then((r) => versTableauBord(r.data)),

  /** Vue globale (responsable support et administrateur). */
  global: () => axiosClient.get('/tableau-bord/statistiques').then((r) => versTableauBord(r.data)),
};
