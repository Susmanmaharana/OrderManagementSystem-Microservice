function Loading({ label = 'Loading...' }) {
  return (
    <div className="loading" aria-live="polite" aria-busy="true">
      <span className="spinner" aria-hidden="true" />
      <span>{label}</span>
    </div>
  );
}

export default Loading;
