package com.example.mpaz_neb.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mpaz_neb.model.PreferenciasDispositivo
import com.example.mpaz_neb.repository.PreferenciasRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PreferenciasViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = PreferenciasRepository(application)

    val uiState: StateFlow<PreferenciasDispositivo> =
        repository.preferencias.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = PreferenciasDispositivo()
        )

    fun cambiarLecturaEnVoz(activa: Boolean) {
        viewModelScope.launch { repository.guardarLecturaEnVoz(activa) }
    }
}
