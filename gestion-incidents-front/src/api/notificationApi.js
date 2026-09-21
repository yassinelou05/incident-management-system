import axiosClient from './axiosClient';

export const notificationApi = {
  lister: () => axiosClient.get('/notifications').then((r) => r.data),

  compterNonLues: () => axiosClient.get('/notifications/non-lues/compte').then((r) => r.data),

  marquerCommeLue: (id) => axiosClient.patch(`/notifications/${id}/lue`).then((r) => r.data),

  marquerToutesCommeLues: () =>
    axiosClient.patch('/notifications/toutes-lues').then((r) => r.data),
};
