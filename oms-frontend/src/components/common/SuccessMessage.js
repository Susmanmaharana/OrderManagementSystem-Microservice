function SuccessMessage({ message }) {
  if (!message) return null;
  return (
    <div className="success-box" role="status">
      {message}
    </div>
  );
}

export default SuccessMessage;
