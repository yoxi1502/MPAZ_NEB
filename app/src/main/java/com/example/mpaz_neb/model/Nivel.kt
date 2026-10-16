package com.example.mpaz_neb.model

data class Nivel(
    val id: Int,
    val titulo: String,
    val descripcion: String
)

val nivelesEscolares = listOf(
    Nivel(1, "1.° Básico", "Conteo y primeras palabras"),
    Nivel(2, "2.° Básico", "Lectura fluida y sumas/restas"),
    Nivel(3, "3.° Básico", "Comprensión lectora y multiplicación"),
    Nivel(4, "4.° Básico", "Vocabulario y resolución de problemas")
)