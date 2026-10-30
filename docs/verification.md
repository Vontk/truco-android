# Verificación

La compilación y las pruebas se ejecutan en GitHub Actions porque el entorno de edición tiene el acceso de red restringido y sólo dispone localmente de un SDK anterior sin Compose. No se considera una compilación local exitosa.

El workflow conserva reportes de pruebas, lint, APK debug, release optimizada, AAB y mapping de R8. Las pruebas instrumentadas están configuradas en API 26 y API 35. Una release publicada por el workflow requiere que todos esos trabajos finalicen correctamente y verifica la firma y alineación de su APK.

Las pruebas de recreación verifican que la Activity relea el estado, y otra instancia de ScoreStore verifica el estado persistido. No equivalen a cortar la energía durante una escritura ni a probar cada fabricante de Android. DataStore serializa las transacciones; la interfaz sólo muestra el estado persistido.

No se afirma validación visual en un teléfono físico. El código evita imágenes del sistema falsas y recursos rasterizados; se usan Canvas y vectores.

## Comprobación local del núcleo

Las cinco pruebas de `ScoreTest` se compilaron y ejecutaron con JDK 21, Kotlin 2.3.20 y JUnit 4.13.2 ya disponibles en caché: **OK (5 tests)**. Se prueban los 31 marcadores posibles, la distribución de palitos, ambos límites y equipos, el cruce de fase y entradas inválidas. Esta ejecución aislada del núcleo no verifica la compilación Android ni Compose.
