export default function Button({ children, cargando, ...props }) {
  return (
    <button className="btn" disabled={cargando || props.disabled} {...props}>
      {cargando ? 'Cargando…' : children}
    </button>
  );
}
