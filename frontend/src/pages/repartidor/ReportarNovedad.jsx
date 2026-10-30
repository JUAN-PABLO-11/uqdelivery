import { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { registrarNovedad } from '../../api/novedades';
import { leerError } from '../../api/client';

const TIPOS_NOVEDAD = [
    { value: 'DIRECCION_INCORRECTA', label: 'Dirección incorrecta' },
    { value: 'DESTINATARIO_AUSENTE', label: 'Destinatario ausente' },
    { value: 'PAQUETE_DANADO', label: 'Paquete dañado' },
    { value: 'OTRO', label: 'Otro motivo' }
];

export default function ReportarNovedad() {
    const { id } = useParams();
    const navigate = useNavigate();

    const [tipo, setTipo] = useState('');
    const [descripcion, setDescripcion] = useState('');
    const [coordenadas, setCoordenadas] = useState({ latitud: null, longitud: null });
    const [cargando, setCargando] = useState(false);
    const [errorGeneral, setErrorGeneral] = useState('');
    const [erroresCampos, setErroresCampos] = useState({});

    // Solicitud opcional de ubicación GPS al cargar el componente
    useEffect(() => {
        if ('geolocation' in navigator) {
            navigator.geolocation.getCurrentPosition(
                (position) => {
                    setCoordenadas({
                        latitud: position.coords.latitude,
                        longitud: position.coords.longitude
                    });
                },
                (error) => {
                    console.warn('No se obtuvo geolocalización:', error.message);
                },
                { enableHighAccuracy: true, timeout: 5000 }
            );
        }
    }, []);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setErrorGeneral('');
        setErroresCampos({});

        // Validaciones locales previas al envío
        const errores = {};
        if (!tipo) {
            errores.tipo = 'Debe seleccionar un tipo de novedad';
        }
        if (!descripcion || descripcion.trim().length < 10) {
            errores.descripcion = 'La descripción debe tener al menos 10 caracteres';
        }

        if (Object.keys(errores).length > 0) {
            setErroresCampos(errores);
            return;
        }

        try {
            setCargando(true);
            await registrarNovedad(id, {
                tipo,
                descripcion: descripcion.trim(),
                latitud: coordenadas.latitud,
                longitud: coordenadas.longitud
            });

            // Al completar con éxito, redirige a la lista de entregas del repartidor
            navigate('/repartidor/entregas');
        } catch (err) {
            const errorParsed = leerError(err);
            setErrorGeneral(errorParsed.mensaje);
            if (errorParsed.campos) {
                setErroresCampos(errorParsed.campos);
            }
        } finally {
            setCargando(false);
        }
    };

    return (
        <div className="max-w-lg mx-auto mt-6 p-6 bg-white rounded-lg shadow-md">
            <h2 className="text-2xl font-bold mb-4 text-gray-800">Reportar Novedad en Ruta</h2>
            <p className="text-sm text-gray-600 mb-6">Envío ID: <strong>{id}</strong></p>

            {errorGeneral && (
                <div className="mb-4 p-3 bg-red-100 border border-red-400 text-red-700 rounded text-sm">
                    {errorGeneral}
                </div>
            )}

            <form onSubmit={handleSubmit} className="space-y-4">
                {/* Tipo de novedad */}
                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                        Tipo de Novedad <span className="text-red-500">*</span>
                    </label>
                    <select
                        value={tipo}
                        onChange={(e) => setTipo(e.target.value)}
                        className={`w-full p-2 border rounded-md focus:ring-2 focus:ring-blue-500 ${
                            erroresCampos.tipo ? 'border-red-500' : 'border-gray-300'
                        }`}
                    >
                        <option value="">-- Seleccione un tipo --</option>
                        {TIPOS_NOVEDAD.map((t) => (
                            <option key={t.value} value={t.value}>
                                {t.label}
                            </option>
                        ))}
                    </select>
                    {erroresCampos.tipo && (
                        <p className="mt-1 text-xs text-red-600">{erroresCampos.tipo}</p>
                    )}
                </div>

                {/* Descripción */}
                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                        Descripción detallada <span className="text-red-500">*</span>
                    </label>
                    <textarea
                        rows="4"
                        value={descripcion}
                        onChange={(e) => setDescripcion(e.target.value)}
                        placeholder="Describa lo sucedido (mínimo 10 caracteres)..."
                        className={`w-full p-2 border rounded-md focus:ring-2 focus:ring-blue-500 ${
                            erroresCampos.descripcion ? 'border-red-500' : 'border-gray-300'
                        }`}
                    />
                    <div className="flex justify-between mt-1 text-xs text-gray-500">
                        <span>{erroresCampos.descripcion && <span className="text-red-600">{erroresCampos.descripcion}</span>}</span>
                        <span>{descripcion.trim().length} / 10 mín.</span>
                    </div>
                </div>

                {/* Indicador GPS */}
                <div className="text-xs text-gray-500 bg-gray-50 p-2 rounded border">
                    GPS: {coordenadas.latitud && coordenadas.longitud
                    ? `Lat: ${coordenadas.latitud.toFixed(4)}, Lng: ${coordenadas.longitud.toFixed(4)}`
                    : 'Sin coordenadas (ubicación no disponible)'}
                </div>

                {/* Botones de acción */}
                <div className="flex justify-end gap-3 pt-4">
                    <Link
                        to="/repartidor/entregas"
                        className="px-4 py-2 text-sm text-gray-700 bg-gray-200 rounded-md hover:bg-gray-300 transition"
                    >
                        Cancelar
                    </Link>
                    <button
                        type="submit"
                        disabled={cargando}
                        className="px-4 py-2 text-sm text-white bg-blue-600 rounded-md hover:bg-blue-700 disabled:opacity-50 transition"
                    >
                        {cargando ? 'Enviando...' : 'Reportar Novedad'}
                    </button>
                </div>
            </form>
        </div>
    );
}