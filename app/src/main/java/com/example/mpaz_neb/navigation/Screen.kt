package com.example.mpaz_neb.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object TeacherLogin : Screen("teacher_login")
    object StudentCode : Screen("student_code")
    object AssignActivity : Screen("assign_activity")
    object SesionEnCurso : Screen("sesion_en_curso")
    object Lectura : Screen("lectura")
    object Matematica : Screen("matematica")
    object Progreso : Screen("progreso")
    object Camara : Screen("camara")
    object Catalogo : Screen("catalogo")

    /** Formulario de actividad: sin id crea una nueva, con id la edita. */
    object ActividadForm : Screen("actividad_form?id={id}") {
        fun crear(id: String?): String =
            if (id == null) "actividad_form" else "actividad_form?id=$id"
    }
}
