# MPAZ NEB

Aplicación móvil Android de apoyo y nivelación en **Lectura** y **Matemática** para estudiantes de 1.º a 4.º básico de la Escuela Escritora Marcela Paz (San Bernardo). Es un MVP académico que usa **solo datos ficticios**.

> Asignatura: Desarrollo de Aplicaciones Móviles (DSY1105) · Duoc UC · Evaluación Parcial 2

## Integrantes

| Integrante | Usuario GitHub | Responsabilidades principales |
|---|---|---|
| Genesis Cerda | `gene1921` | Módulos de Lectura y Matemática, cámara, texto a voz y animaciones |
| Yoximar Ruiz | `yoxi1502` | Panel docente, sesiones con código, login con perfiles, observación pedagógica, catálogo de actividades (UTP) y documentación |
| Maximiliano Valenzuela | `maxijrr20` | Arquitectura y paquetes, tema Material 3, validación de la asignación, transiciones de navegación e historial de resultados |

**Equipo N.º:** 2

## Problema que resuelve

Las asistentes de aula preparan a mano guías de refuerzo repartidas en distintos documentos y no hay un registro central de qué actividad hizo cada estudiante ni cuántos intentos necesitó. MPAZ NEB centraliza actividades de Lectura y Matemática organizadas por curso y dificultad, permite asignarlas a un estudiante ficticio con un código temporal y registra respuestas, intentos y resultados básicos.

La app es una herramienta de apoyo: **no diagnostica, no califica oficialmente y no reemplaza el criterio de docentes, UTP o PIE**.

## Perfiles y flujo

| Perfil | Qué puede hacer |
|---|---|
| **Docente / UTP** | Todo lo de la asistente + catálogo de actividades: filtrar, crear, editar, activar y desactivar actividades |
| **Asistente de aula** | Asignar una actividad *Disponible* a un estudiante ficticio, generar el código, acompañar la sesión, registrar observaciones y ver resultados |
| **Estudiante** | Entrar solo con el código de su sesión, responder, recibir retroalimentación y escuchar textos en voz alta |

## Funcionalidades implementadas

- **Inicio** con acceso diferenciado para estudiante y equipo pedagógico.
- **Login con cuentas protegidas de prueba** por perfil, validación por campo (íconos y mensajes) y registro de último acceso.
- **Rutas protegidas:** las pantallas del equipo pedagógico exigen sesión iniciada; el catálogo solo admite el perfil Docente/UTP.
- **Catálogo de actividades (UTP):** filtros por módulo y curso, formulario validado (nombre, contenido, objetivo, dificultad, instrucciones, n.º de preguntas, intentos y estado).
- **Asignación:** módulo + curso → solo actividades *Disponibles* → estudiante ficticio → código temporal `MPAZ-XXXXXX`.
- **Sesión en curso:** estado (esperando, en curso, completada, finalizada), asistente responsable, intentos y **formulario de observación pedagógica** validado (largo y bloqueo de RUT/datos personales).
- **Ingreso del estudiante** con código validado (formato y sesión abierta); solo puede abrir la actividad asignada.
- **Lectura:** texto breve con preguntas explícita, implícita y de vocabulario, retroalimentación específica por pregunta.
- **Matemática:** ejercicios con explicación del procedimiento en la retroalimentación.
- **Confirmación antes de enviar** las respuestas finales.
- **Resultados:** último resultado por módulo + historial de sesiones filtrable por estudiante (intentos, aciertos, errores y observación).

### Animaciones
- Transiciones entre pantallas (fade + desplazamiento) en el `NavHost`.
- Cambio de pregunta con `AnimatedContent`.
- Barra de avance animada y cambio de color animado en las alternativas.
- Aparición animada de la retroalimentación y de los mensajes de error.
- Estrella con rebote y contador de aciertos animado al terminar.
- Ícono "latiendo" mientras se lee en voz alta; tarjetas del historial que se expanden.

### Recursos nativos
| Recurso | Uso en la app | Seguridad |
|---|---|---|
| **Texto a voz** (`TextToSpeech`) | El estudiante escucha la historia, la pregunta y las opciones (español de Chile o español general) | Sin permisos peligrosos; se libera al salir de la pantalla y avisa si la voz no está disponible. Se puede desactivar desde el panel de la asistente (DataStore) |
| **Cámara** (CameraX) | La asistente fotografía el trabajo en papel del estudiante como evidencia de la práctica (Sesión en curso → *Evidencia de la práctica*) | Permiso `CAMERA` solicitado en tiempo de ejecución; solo en el panel de la asistente, que exige sesión iniciada |

La cámara **no aparece en el flujo del estudiante**: es una herramienta de la asistente, que se usa bajo supervisión y sin fotografiar rostros ni datos personales.

## Arquitectura

Patrón **MVVM** con Jetpack Compose y Material 3.

```
app/src/main/java/com/example/mpaz_neb/
├── model/        Estados de UI (UiState) y modelos de datos
├── viewmodel/    Lógica de presentación; exponen StateFlow a las pantallas
├── repository/   Acceso a persistencia local (DataStore)
├── validacion/   Reglas de validación centralizadas (funciones puras, con pruebas unitarias)
├── nativo/       Recurso nativo: LectorVoz (texto a voz)
├── navigation/   Rutas (Screen) y NavHost con rutas protegidas
└── ui/
    ├── screens/     Pantallas
    ├── components/  Componentes reutilizables (botón de voz, animaciones)
    └── theme/       Colores, tipografía y esquema Material 3
```

- **Persistencia local:** Jetpack DataStore — usuario y último acceso, catálogo de actividades, sesiones con intentos y observaciones, resultados y preferencias del dispositivo.
- **Validaciones:** `validacion/Validadores.kt`; las pantallas solo muestran los errores que entrega el ViewModel.
- **Pruebas:** `app/src/test/.../ValidadoresTest.kt`.

## Cómo ejecutar

**Requisitos:** Android Studio reciente (compatible con AGP 9.3), JDK 11+ y un emulador o dispositivo con **API 30 o superior** (se recomienda tableta).

1. Clonar el repositorio:
   ```bash
   git clone https://github.com/yoxi1502/MPAZ_NEB.git
   ```
2. Abrir la carpeta en Android Studio y esperar la sincronización de Gradle.
3. Elegir un emulador o dispositivo y presionar **Run ▶**.
4. Para el texto a voz, el emulador debe tener un motor TTS con español (Ajustes → Sistema → Idiomas → Salida de texto a voz).

**Pruebas unitarias:** `./gradlew test`

### Datos de prueba (ficticios)

| Perfil | Correo | Contraseña |
|---|---|---|
| Asistente de aula | `asistente@mpaz.cl` | `Asistente123` |
| Docente / UTP | `utp@mpaz.cl` | `Docente123` |

**Flujo de demostración:** ingresar como asistente → elegir *Lectura* y *2.° Año Básico* (o *Matemática* y *1.° Año Básico*) → generar código → *Entregar dispositivo al estudiante* → escribir el código → responder → volver al panel y revisar *Resultados*.

## Datos y privacidad

Todo el contenido es ficticio. No se registran nombres, RUT, diagnósticos, antecedentes PIE ni calificaciones reales; el formulario de observación rechaza textos con forma de RUT. Los datos se guardan solo en el dispositivo.

## Gestión del proyecto

- **Planificación (Trello):** https://trello.com/b/DEAsehxo/mpaz-neb
- **Control de versiones:** GitHub con ramas por funcionalidad y Pull Requests.

## Próximos pasos (Evaluación Final Transversal)

- Editar preguntas y alternativas desde el catálogo (hoy cada módulo tiene una unidad fija).
- Aplicar el límite de intentos permitidos definido en cada actividad.
- Base de datos remota para compartir resultados entre dispositivos.
- Pruebas de interfaz con Compose Testing.
