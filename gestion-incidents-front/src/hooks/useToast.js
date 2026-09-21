import { useCallback, useState } from 'react';

/** Petite file de messages ephemeres affiches en bas a droite. */
export function useToast() {
  const [messages, setMessages] = useState([]);

  const retirer = useCallback((id) => {
    setMessages((precedents) => precedents.filter((m) => m.id !== id));
  }, []);

  const notifier = useCallback(
    (texte, type = 'succes') => {
      const id = Date.now() + Math.random();
      setMessages((precedents) => [...precedents, { id, texte, type }]);
      setTimeout(() => retirer(id), 4000);
    },
    [retirer],
  );

  return {
    messages,
    succes: (texte) => notifier(texte, 'succes'),
    erreur: (texte) => notifier(texte, 'erreur'),
    info: (texte) => notifier(texte, 'info'),
    retirer,
  };
}
