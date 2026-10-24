# MPAZ NEB

Aplicación móvil Android de apoyo y nivelación en **Lectura** y **Matemática** para estudiantes de 1.º a 4.º básico de la Escuela Escritora Marcela Paz (San Bernardo). Es un MVP académico que usa únicamente datos ficticios.

> Asignatura: Desarrollo de Aplicaciones Móviles (DSY1105) · Duoc UC
> Evaluación Parcial 2 

## Integrantes

| Nombre | Rol principal |
|---|---|
| _Genesis Cerda 1_ 
| _Yoximar Ruiz 2_ 
| _Maximiliano Valenzuela 3_

Equipo N.º: 

## Problema que resuelve

Las asistentes de aula preparan manualmente guías y ejercicios de refuerzo distribuidos en distintos documentos, sin un registro central de qué actividad realizó cada estudiante ni cuántos intentos necesitó. MPAZ NEB centraliza actividades de Lectura y Matemática, permite asignarlas a un estudiante ficticio mediante un código temporal y registra los resultados básicos.

La app es una herramienta de apoyo: no diagnostica, no califica oficialmente y no reemplaza el criterio de docentes, UTP o PIE.

## Funcionalidades implementadas

- **Pantalla de inicio** con acceso diferenciado para estudiante y equipo pedagógico.
- **Login del equipo pedagógico** con formulario validado (correo y contraseña), íconos y mensajes de error por campo.
- **Ingreso del estudiante con código temporal**, validado por campo, con mensajes visuales de error.
- **Asignación de actividades:** el equipo elige módulo, curso y estudiante ficticio, y la app genera un código temporal de sesión.
- **Sesión en curso:** seguimiento del estado de la sesión (esperando, en curso, completada, finalizada).
- **Módulo de Lectura:** texto con preguntas de alternativas, avance, comprobación de respuesta y retroalimentación.
- **Módulo de Matemática:** ejercicios de alternativas con avance y retroalimentación.
- **Progreso:** resultados guardados (aciertos, total, fecha e intentos por sesión).
- **Animaciones:** barra de avance, cambio animado de color en respuestas y aparición animada de la retroalimentación.
- **Recursos nativos:**
  - **Cámara** (CameraX): captura de una foto con solicitud de permiso en tiempo de ejecución.
  - **Micrófono**: grabación y reproducción de audio con solicitud de permiso en tiempo de ejecución.
- **Diseño adaptable** a teléfonos y tabletas mediante clases de tamaño de ventana.

## Arquitectura y tecnologías

- Kotlin y **Jetpack Compose** con **Material 3**.
- Patrón **MVVM**: las pantallas observan el estado (`StateFlow`) expuesto por los ViewModels; la lógica de validación vive en los ViewModels, desacoplada de la interfaz.
- Capas del proyecto:
  - `model/`: clases de estado de la interfaz y modelos de datos.
  - `viewmodel/`: lógica de presentación y validaciones.
  - `repository/`: acceso a la persistencia local.
  - `navigation/`: rutas y `NavHost` (Navigation Compose).
  - `ui/`: pantallas, componentes y tema.
- **Persistencia local** con Jetpack DataStore (sesiones, intentos y progreso).
- **CameraX** para la cámara y `MediaRecorder`/`MediaPlayer` para el audio.
- Corrutinas de Kotlin para operaciones asíncronas.

## Cómo ejecutar el proyecto

**Requisitos**

- Android Studio en una versión reciente, compatible con Android Gradle Plugin 9.3.0.
- JDK 11 o superior (el que incluye Android Studio).
- Emulador o dispositivo Android con **API 30 o superior** (se recomienda una tableta).

**Pasos**

1. Clonar el repositorio:
   ```bash
   git clone <https://github.com/yoxi1502/MPAZ_NEB>
   ```
2. Abrir la carpeta del proyecto en Android Studio y esperar la sincronización de Gradle.
3. Seleccionar un emulador o dispositivo y presionar **Run ▶**.
4. Aceptar los permisos de cámara y micrófono cuando se soliciten para probar los recursos nativos.

**Datos de prueba (ficticios)**

- Equipo pedagógico: cualquier correo con formato válido (por ejemplo `asistente@prueba.cl`) y una contraseña de al menos 6 caracteres.
- Estudiante: código de sesión generado desde la pantalla de asignación, o el código de demostración `MPAZ-402`.

## Datos y privacidad

Todo el contenido de la app es ficticio. No se registran nombres, RUT, diagnósticos, antecedentes PIE ni calificaciones reales. Los datos se almacenan únicamente en el dispositivo.

## Gestión del proyecto

- **Planificación:** tablero de Trello — _pegar enlace aquí_
- **Control de versiones:** GitHub, con commits repartidos entre ambos integrantes.

## Próximos pasos (hacia la Evaluación Final Transversal)

- Perfil de administración pedagógica con catálogo de actividades (agregar, editar, activar y desactivar).
- Más actividades por curso y dificultad.
- Mejoras de accesibilidad y pruebas automatizadas.
