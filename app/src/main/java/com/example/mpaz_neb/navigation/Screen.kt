package com.example.mpaz_neb.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object TeacherLogin : Screen("teacher_login")
    object StudentCode : Screen("student_code")
    object Lectura : Screen("lectura")
    object Matematica : Screen("matematica")
}