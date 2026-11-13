# Pruebas y guía de defensa — TeamDex

## Lista de pruebas (registra el resultado observado y captura propia)

| ID | Acción | Resultado que se debe observar | Resultado / captura |
|---|---|---|---|
| 7.1 CREATE | Crear un equipo con nombre válido; guardar un favorito desde una ficha. | Se ven en sus listas y quedan almacenados en SQLite. | Pendiente de ejecución |
| 7.2 READ | Abrir Mis equipos, detalle del equipo y Pokes favoritos. | Los datos de las listas proceden de consultas SQLite. | Pendiente de ejecución |
| 7.3 UPDATE | Cambiar el nombre del equipo; modificar un set; editar etiqueta de favorito. | Los cambios se muestran al volver a consultar y conservan los IDs. | Pendiente de ejecución |
| 7.4 DELETE | Eliminar un equipo y un favorito confirmando el diálogo. | Desaparece el seleccionado; borrar equipo elimina sus integrantes por cascada. | Pendiente de ejecución |
| 7.5 Validación | Enviar nombre vacío, espacios, nombre largo, más de seis o integrante duplicado. | La operación se detiene y se comunica el motivo. | Pendiente de ejecución |
| 7.6 Restricción | Repetir nombre de equipo, duplicar Pokémon o exceder el cupo. | SQLite/lógica rechaza la operación y la interfaz captura el error. | Pendiente de ejecución |
| 7.7 Persistencia | Crear y editar; cerrar completamente y volver a abrir la app. | Se conservan equipos, integrantes, favoritos y nivel. | Pendiente de ejecución |
| 7.8 Navegación | Recorrer inicio, regiones, detalle, favoritos, equipos y ajustes. | Las pantallas abren y vuelven a la app sin perder consistencia. | Pendiente de ejecución |

En Android Studio, **View → Tool Windows → App Inspection → Database Inspector** permite mostrar `teamdex.db` y verificar directamente `equipos`, `equipo_pokemon`, `favoritos` y `configuracion`. Adjunta capturas solo después de haber hecho cada comprobación.

## Secuencia de demostración grupal (aprox. 7–9 minutos; ajustar al tiempo indicado por el docente)

1. **Integrante A — propósito y evolución (G1):** explicar la Pokédex TeamDex heredada de la Unidad I; indicar que la mejora incorpora almacenamiento local real sin cambiar el propósito.
2. **A — recorrido:** elegir nivel, abrir una región, buscar por nombre/número, abrir ficha y guardar un favorito.
3. **A — CREATE/READ:** crear “Equipo evaluación”, abrirlo y añadir un Pokémon; mostrar la lista y detalle.
4. **Integrante B — UPDATE/DELETE:** cambiar el nombre, editar set, guardar; luego solicitar eliminación y cancelar una vez para evidenciar confirmación, finalmente borrar un registro de prueba.
5. **B — validaciones:** mostrar nombre vacío/duplicado, duplicado de Pokémon y límite de seis; explicar el mensaje y la restricción.
6. **B — SQLite:** abrir Database Inspector, identificar claves y filas; cerrar y reiniciar para demostrar persistencia.
7. **Ambos:** responder preguntas, mencionar una dificultad real del proceso y aprendizaje propio; cerrar con una mejora futura.

Preparen una base de demostración el día anterior, pero dejen también un equipo vacío para mostrar CREATE desde cero. No desinstalen la aplicación entre la creación y la prueba de persistencia. Mantengan disponibles el emulador, conexión a Internet para PokeAPI y el Database Inspector.

## Arquitectura y SQLite

`TeamDexDatabase` extiende `SQLiteOpenHelper`. `onCreate` define tablas y restricciones y crea la configuración inicial. `equipos.id` identifica cada equipo; `equipo_pokemon.equipo_id` referencia a `equipos.id` con `ON DELETE CASCADE`. La pareja `(equipo_id,pokemon_id)` es única y evita duplicados. CHECK valida longitud y valores permitidos. `favoritos.pokemon_id` es clave primaria, y `configuracion.id=1` limita a una preferencia activa.

- CREATE: `createTeam`, `addPokemon`, `toggleFavorite`.
- READ: `getTeams`, `getTeamPokemon`, `getFavorites`, `getLevel`, `isFavorite`.
- UPDATE: `updateTeam`, `updatePokemon`, `updateFavoriteName`, `saveLevel`.
- DELETE: `deleteTeam`, `removePokemon`, `deleteFavorite`.

La interfaz llama esos métodos y refresca la pantalla desde la consulta después de una modificación. `PokeApiClient` solo sirve datos públicos de Pokédex; no es la base de datos de equipos ni reemplaza SQLite. Usa un ejecutor en segundo plano y devuelve resultados en el hilo principal.

## Reparto de aportes (completar con la contribución verdadera de cada integrante)

- Integrante A: “Mi aporte concreto fue ______. Trabajé en la clase/método ______. Se conecta con SQLite cuando ______.”
- Integrante B: “Mi aporte concreto fue ______. Trabajé en la clase/método ______. Se conecta con SQLite cuando ______.”

No memoricen una contribución que no realizaron. Revisen juntos las clases y hagan que cada integrante ejecute al menos una operación frente al docente.

## Guion breve de introducción

“TeamDex permite explorar Pokémon y organizar equipos de combate. En esta segunda etapa conservamos el tema y las funciones principales del proyecto anterior e incorporamos SQLite para que los datos creados por el usuario permanezcan en el dispositivo. Además de consultar equipos, se pueden crear, renombrar y eliminar; se agregan y retiran Pokémon, se editan sus sets y se administran favoritos. Hoy mostraremos estas operaciones desde la aplicación y verificaremos los registros en la base de datos.”

## Preguntas probables y respuestas para practicar

**¿Por qué SQLite?** Es local, no requiere servidor y persiste datos del usuario en el dispositivo.

**¿Qué función tiene `SQLiteOpenHelper`?** Centraliza la apertura, creación y actualización del esquema de la base.

**¿Cómo se relaciona un Pokémon con su equipo?** `equipo_pokemon.equipo_id` es clave foránea hacia `equipos.id`; al borrar el equipo se borran sus relaciones mediante cascada.

**¿Cómo evitan duplicados?** Una restricción `UNIQUE(equipo_id,pokemon_id)` en SQLite y el manejo del error en la interfaz.

**¿Cómo se garantiza el límite de seis?** Antes del INSERT se consulta el total y se rechaza si ya hay seis. SQLite mantiene además la unicidad y la referencia. Para una concurrencia multiusuario, una mejora sería reforzar el cupo con transacción/trigger; esta app es de uso local de una sola interfaz.

**¿Qué diferencia hay entre UPDATE y reemplazar el registro?** UPDATE modifica columnas del ID seleccionado, preserva su identificador y no duplica la fila.

**¿Qué muestra `Cursor`?** El resultado de una consulta; se recorre fila por fila y se convierte a modelos que la interfaz puede mostrar.

**¿La Pokédex completa se guarda en SQLite?** No: PokeAPI suministra el catálogo remoto. SQLite guarda la información administrada por el usuario: equipos, integrantes, sets, favoritos y preferencia.

**¿Cómo comprueban persistencia?** Cierran y vuelven a abrir sin desinstalar; repiten consultas y contrastan las filas en Database Inspector.

**¿Qué dificultad encontraron?** Cada integrante debe completar aquí una dificultad realmente vivida y cómo la resolvió: ______.

**¿Qué mejorarían?** Cargar imágenes con caché y paginar la selección completa para reducir consumo de red; también se puede añadir respaldo/exportación de equipos.

## Cierre

“Comprobamos las operaciones con registros reales y revisamos su permanencia después de reiniciar. SQLite administra la información del usuario y PokeAPI aporta el catálogo. Como mejora, proponemos optimizar imágenes y paginación. Gracias; podemos responder preguntas sobre el flujo entre la pantalla y la base.”