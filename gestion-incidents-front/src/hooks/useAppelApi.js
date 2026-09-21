import { useCallback, useEffect, useState } from 'react';

/**
 * Encapsule un appel API : etats de chargement, d'erreur et de donnees,
 * avec une fonction de rechargement manuelle.
 */
export function useAppelApi(fonction, dependances = [], actifParDefaut = true) {
  const [donnees, setDonnees] = useState(null);
  const [chargement, setChargement] = useState(actifParDefaut);
  const [erreur, setErreur] = useState(null);

  const executer = useCallback(() => {
    setChargement(true);
    setErreur(null);
    return fonction()
      .then((resultat) => {
        setDonnees(resultat);
        return resultat;
      })
      .catch((e) => {
        setErreur(e.message || 'Erreur de chargement.');
        return null;
      })
      .finally(() => setChargement(false));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, dependances);

  useEffect(() => {
    if (actifParDefaut) {
      executer();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [executer]);

  return { donnees, chargement, erreur, recharger: executer, setDonnees };
}
