# Truco

Contador de Truco argentino para Android, nativo en Kotlin y Jetpack Compose. Minimalista, offline, sin anuncios ni permisos de red. Android 8.0 o posterior.

[Descargar última versión](https://github.com/Vontk/truco-android/releases/latest)

## Uso

Nosotros a la izquierda, Ellos a la derecha. Tocá arriba para restar y abajo para sumar, entre 0 y 30 puntos. Los cuadrados con diagonal agrupan cinco palitos; arriba están las malas (0–15), abajo las buenas (16–30). El botón de reinicio pide confirmación. El marcador se guarda automáticamente en el dispositivo con DataStore.

Colores del fondo de pantalla en Android 12+, tema claro/oscuro del sistema y gris neutro en versiones anteriores. Barras del sistema reales y diseño adaptable, también horizontal. TalkBack anuncia equipo, puntos y acción; las cuatro zonas son accesibles por teclado. Ícono adaptativo con variante monocromática en Android 13+.

## Obtener actualizaciones con Obtainium

Agregá `https://github.com/Vontk/truco-android` como fuente GitHub. Usá el filtro de APK `^truco-[0-9.]+\.apk$` para elegir exclusivamente la versión release. Conservá los datos de la app al actualizar. No hace falta un actualizador dentro de Truco.

La versión debug tiene el sufijo `.debug` y puede instalarse junto a la release; sus datos y firma son independientes.

## Compilar

Abrí esta carpeta en Android Studio estable. Usá JDK 21, Android SDK 37.2 y Build Tools 36.1.0; el wrapper descarga Gradle 9.8.1.

```sh
./gradlew testDebugUnitTest lint assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
./gradlew connectedDebugAndroidTest  # dispositivo o emulador conectado
./gradlew assembleRelease bundleRelease
# Sin credenciales: APK release sin firmar y AAB en app/build/outputs/.
```

En Windows usá `gradlew.bat`. Configurá `ANDROID_HOME` o `sdk.dir` en `local.properties`.

## Firmar una release

Creá una clave privada una sola vez y guardá una copia segura fuera del repo:

```sh
keytool -genkeypair -keystore /ruta/privada/truco-release.jks -alias truco -keyalg RSA -keysize 3072 -validity 10000
export TRUCO_KEYSTORE=/ruta/privada/truco-release.jks
read -rs TRUCO_STORE_PASSWORD; export TRUCO_STORE_PASSWORD
export TRUCO_KEY_PASSWORD="$TRUCO_STORE_PASSWORD"
./gradlew assembleRelease
```

Para actualizar la app publicada se necesita **la misma clave original**. La clave local y su contraseña están excluidas de Git en `.signing/`; conservá una copia privada de esa carpeta. Podés firmar el APK release sin firmar de Actions con:

```sh
bash scripts/sign-release.sh /ruta/app-release-unsigned.apk
```

Incrementá `versionCode` y `versionName` para la próxima actualización y repetí pruebas/build. Publicá `artifacts/truco-X.Y.Z.apk`, `SHA256SUMS` y `signature.txt` en una nueva release de GitHub con tag `vX.Y.Z` sobre el commit verificado. El APK debug es opcional y se excluye con el filtro de Obtainium. La firma se realiza localmente; las claves privadas no se suben al repositorio ni a los artefactos de Actions.

## Diseño y verificación

Referencia suministrada: [HTML original](docs/reference.html). No se usa HTML ni WebView en la aplicación. Sólo Activity, Compose Material 3, Lifecycle y DataStore; sin servidor, servicios, base de datos ni capas adicionales.

Material 3 estable incluye los componentes oficiales utilizados (diálogo, botones, ripple). Las APIs Expressive que requieren una dependencia preliminar no se incorporan para mantener versiones estables y la interfaz mínima.

Las pruebas unitarias recorren los 31 valores y sus palitos, límites y aislamiento. Las pruebas instrumentadas cubren toque, límites, reinicio, recreación de Activity y persistencia. [Actions](https://github.com/Vontk/truco-android/actions) registra los resultados reales de cada commit; [verificación](docs/verification.md) detalla el alcance comprobado.

Referencias oficiales: [BOM de Compose](https://developer.android.com/develop/ui/compose/bom), [Material 3](https://developer.android.com/jetpack/androidx/releases/compose-material3), [edge-to-edge](https://developer.android.com/develop/ui/compose/system/insets), [Adaptive Icons](https://developer.android.com/develop/ui/views/launch/icon_design_adaptive).
