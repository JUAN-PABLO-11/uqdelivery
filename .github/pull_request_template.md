## HU que resuelve
HU-XX – <nombre>

## Qué se hizo
-

## Checklist (Definition of Done)
- [ ] Cumple los 2 criterios de validación de la HU (camino feliz y de error)
- [ ] Valida en front y back (`@Valid`, `@SinHtml` en textos libres)
- [ ] Respeta las capas Controller → Service → Repository y el formato de error común
- [ ] Los cambios de estado usan `EstadoService` (nadie hace `setEstado`)
- [ ] Tiene pruebas JUnit y `mvn test` pasa
- [ ] Lo probé en local con los usuarios del DataSeeder
