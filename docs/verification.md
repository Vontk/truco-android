# Verificación de 1.0.0

[Build y pruebas verificadas](https://github.com/Vontk/truco-android/actions/runs/41147908677), commit `4957f62e5f33905f89c5f5ccc6605a9ceb65915f`.

- Compilación debug, release optimizada con R8 y AAB: correcta.
- Cinco pruebas unitarias de ScoreTest: correctas; cubren los 31 marcadores, palitos, límites, equipos y cruce de fase.
- Lint: sin errores; siete advertencias (dependencias más recientes, candidatos a plurales, extracción de datos, comprobación de SDK y detección del ícono monocromático entre recursos versionados).
- Tres pruebas instrumentadas en API 26 y las mismas tres en API 35: correctas. Incluyen toques, límites, confirmación/cancelación, recreación de Activity y persistencia atómica de ambos equipos.
- Capturas del emulador API 35: modo oscuro y horizontal con fuente al 150% revisados. La primera captura clara se tomó antes del primer frame; el workflow posterior aumenta la espera. No se afirma validación visual en teléfono físico.
- APK release firmado localmente: esquemas v2 y v3 y alineación de 16 KB verificados. Tamaño aproximado: 1,1 MB. El APK debug tiene identificador separado y firma de depuración.
- Sin permiso de Internet. AndroidX añade su permiso interno de receptores con protección por firma; no requiere autorización del usuario.

Certificado release SHA-256:

`8737bf1bafa258f1b79c873880044c87b2577e0ade437cf25b366576bd938042`

La compilación Android se ejecutó en GitHub Actions porque el entorno de edición no puede descargar SDK/dependencias. Los APK se recuperaron mediante el conector de GitHub y se verificaron localmente. La ejecución aislada local de las cinco pruebas del núcleo también pasó con JDK 21, Kotlin 2.3.20 y JUnit 4.13.2.

La recreación de Activity y otra instancia de ScoreStore verifican la persistencia; no simulan un corte de energía durante una escritura. La UI sólo muestra el estado persistido y DataStore serializa las transacciones. Las actualizaciones necesitan conservar la clave original y aumentar versionCode.
