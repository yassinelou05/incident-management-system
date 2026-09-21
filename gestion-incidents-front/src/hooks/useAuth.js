import { useContext } from 'react';
import { AuthContext } from '../context/AuthContext';

/** Acces au contexte d'authentification. */
export function useAuth() {
  const contexte = useContext(AuthContext);
  if (!contexte) {
    throw new Error("useAuth doit etre utilise a l'interieur d'un AuthProvider.");
  }
  return contexte;
}
