export function Toasts({ messages, onRetirer }) {
  if (!messages?.length) return null;
  return (
    <div className="toasts">
      {messages.map((m) => (
        <div
          key={m.id}
          className={`toast toast--${m.type}`}
          role="status"
          onClick={() => onRetirer(m.id)}
        >
          {m.texte}
        </div>
      ))}
    </div>
  );
}
