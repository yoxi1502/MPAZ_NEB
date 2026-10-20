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
    object Audio : Screen("audio")
}
