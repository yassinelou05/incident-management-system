import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { notificationApi } from '../../api/notificationApi';
import { useAuth } from '../../hooks/useAuth';
import { initiales } from '../../utils/format';
import { LIBELLES_ROLE } from '../../utils/constants';

export function Topbar({ titre }) {
  const { utilisateur, deconnexion } = useAuth();
  const navigate = useNavigate();
  const [nonLues, setNonLues] = useState(0);

  useEffect(() => {
    let actif = true;
    const charger = () =>
      notificationApi
        .compterNonLues()
        .then((d) => actif && setNonLues(d.nonLues ?? 0))
        .catch(() => undefined);

    charger();
    const minuterie = setInterval(charger, 60000);
    return () => {
      actif = false;
      clearInterval(minuterie);
    };
  }, []);

  return (
    <header className="topbar">
      <div className="topbar__titre">{titre}</div>

      <div className="topbar__droite">
        <button
          type="button"
          className="topbar__cloche"
          title="Notifications"
          onClick={() => navigate('/notifications')}
        >
          ✉
          {nonLues > 0 && <span className="topbar__pastille">{nonLues > 99 ? '99+' : nonLues}</span>}
        </button>

        <div className="topbar__profil">
          <div className="avatar">{initiales(utilisateur?.nomComplet)}</div>
          <div className="topbar__identite">
            <strong>{utilisateur?.nomComplet}</strong>
            <span>{LIBELLES_ROLE[utilisateur?.role] || utilisateur?.role}</span>
          </div>
          <button
            type="button"
            className="btn btn--secondaire btn--petit"
            onClick={() => {
              deconnexion();
              navigate('/connexion');
            }}
          >
            Déconnexion
          </button>
        </div>
      </div>
    </header>
  );
}
