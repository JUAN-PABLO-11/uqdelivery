/** Input con etiqueta y mensaje de error (usar con los "campos" que devuelve la API). */
export default function Input({ label, error, id, ...props }) {
  return (
    <div className="campo">
      <label htmlFor={id}>{label}</label>
      <input id={id} className={error ? 'invalido' : ''} {...props} />
      {error && <small className="error">{error}</small>}
    </div>
  );
}
