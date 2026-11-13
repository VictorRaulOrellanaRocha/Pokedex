# TeamDex Java

Proyecto Android nativo en Java que adapta la temática y los flujos de TeamDex original. Mantiene exploración de nueve regiones, búsqueda de Pokédex, ficha, favoritos y equipos de hasta seis Pokémon; suma gestión CRUD real con SQLite para equipos y favoritos, configuración de combate y preferencia Novato/Experto.

## Abrir en Android Studio
1. Descomprime `TeamDex_Proyecto_Java.zip`.
2. En Android Studio elige **Open** y selecciona la carpeta `TeamDexJava`, donde está `settings.gradle`.
3. Configura Android SDK Platform 36 y JDK 17 o superior. Android Studio sincronizará Gradle y descargará las dependencias necesarias (AGP 8.13.1 y Gradle 8.13.1).
4. Si el proyecto no trae el lanzador Gradle, abre **Tools → Gradle → Reload All Gradle Projects** para que Android Studio prepare/sincronice el wrapper. Luego inicia un emulador (Android 6/API 23 o superior) o conecta un teléfono con depuración USB y ejecuta `app`.
5. Las consultas de catálogo y ficha requieren Internet. Los equipos, favoritos y preferencias se almacenan localmente.

## Pruebas de pauta
La hoja `PRUEBAS_Y_DEFENSA.md` enumera la comprobación mínima 7.1–7.8, una secuencia de demostración, la explicación de las tablas y preguntas técnicas para practicar. Ejecuta las pruebas en Android Studio y registra evidencia real con capturas. El paquete fuente se compiló mediante `javac` contra Android SDK 36; la tarea Gradle `assembleDebug` no pudo completarse en este entorno porque falta una dependencia y la solicitud de descarga fue rechazada automáticamente. En tu Android Studio se debe sincronizar Gradle con Internet para resolverla. No se incluye APK ni se afirma prueba en emulador.

## Estructura
- `MainActivity.java`: interfaz nativa y navegación.
- `data/TeamDexDatabase.java`: `SQLiteOpenHelper`, esquema, restricciones y CRUD.
- `data/PokeApiClient.java`: cliente PokeAPI en segundo plano.
- `AndroidManifest.xml`, recursos y Gradle: configuración necesaria para abrir en Android Studio.

El repositorio de partida es una aplicación Flutter. Esta entrega es su adaptación Java solicitada para la evaluación.


