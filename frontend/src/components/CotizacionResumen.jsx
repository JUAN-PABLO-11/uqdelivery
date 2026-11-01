import './CotizacionResumen.css';

const pesos = new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 });
const km = new Intl.NumberFormat('es-CO', { minimumFractionDigits: 2, maximumFractionDigits: 2 });

/** HU-05: desglose de una cotización (respuesta de POST /envios/cotizar). No renderiza nada si no hay cotización. */
export default function CotizacionResumen({ cotizacion }) {
  if (!cotizacion) return null;

  return (
    <div className="cotizacion" aria-live="polite">
      <h3>Cotización</h3>
      <dl>
        <dt>Distancia</dt>
        <dd>{km.format(cotizacion.distanciaKm)} km</dd>
        <dt>Tarifa base</dt>
        <dd>{pesos.format(cotizacion.tarifaBase)}</dd>
        <dt>Por distancia</dt>
        <dd>{pesos.format(cotizacion.valorDistancia)}</dd>
        <dt>Por peso</dt>
        <dd>{pesos.format(cotizacion.valorPeso)}</dd>
        <dt className="cotizacion__total">Total</dt>
        <dd className="cotizacion__total">{pesos.format(cotizacion.total)}</dd>
      </dl>
    </div>
  );
}
