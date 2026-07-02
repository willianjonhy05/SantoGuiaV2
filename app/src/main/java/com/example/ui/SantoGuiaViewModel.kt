package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.ExameItem
import com.example.data.LiturgiaDiaria
import com.example.data.NetworkClerigo
import com.example.data.NetworkIgreja
import com.example.data.NetworkTipoCelebracao
import com.example.data.SantoGuiaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class SantoGuiaViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val repository = SantoGuiaRepository(database.exameDao())

    // ----------------------------------------------------------------
    // Sacerdotes (Clerigos) State
    // ----------------------------------------------------------------
    private val _clerigosState = MutableStateFlow<UiState<List<NetworkClerigo>>>(UiState.Loading)
    val clerigosState: StateFlow<UiState<List<NetworkClerigo>>> = _clerigosState.asStateFlow()

    private val _searchClerigosQuery = MutableStateFlow("")
    val searchClerigosQuery: StateFlow<String> = _searchClerigosQuery.asStateFlow()

    // ----------------------------------------------------------------
    // Igrejas State
    // ----------------------------------------------------------------
    private val _igrejasState = MutableStateFlow<UiState<List<NetworkIgreja>>>(UiState.Loading)
    val igrejasState: StateFlow<UiState<List<NetworkIgreja>>> = _igrejasState.asStateFlow()

    private val _bairroFilter = MutableStateFlow("")
    val bairroFilter: StateFlow<String> = _bairroFilter.asStateFlow()

    // ----------------------------------------------------------------
    // Celebracoes & Confissoes State
    // ----------------------------------------------------------------
    private val _celebracoesState = MutableStateFlow<UiState<List<NetworkTipoCelebracao>>>(UiState.Loading)
    val celebracoesState: StateFlow<UiState<List<NetworkTipoCelebracao>>> = _celebracoesState.asStateFlow()

    private val _selectedCategoriaCelebracao = MutableStateFlow("Todas")
    val selectedCategoriaCelebracao: StateFlow<String> = _selectedCategoriaCelebracao.asStateFlow()

    private val _selectedDiaCelebracao = MutableStateFlow("Todos")
    val selectedDiaCelebracao: StateFlow<String> = _selectedDiaCelebracao.asStateFlow()

    // ----------------------------------------------------------------
    // Liturgia Diária State
    // ----------------------------------------------------------------
    private val _selectedLiturgiaDate = MutableStateFlow(Calendar.getInstance())
    val selectedLiturgiaDate: StateFlow<Calendar> = _selectedLiturgiaDate.asStateFlow()

    private val _liturgiaState = MutableStateFlow<LiturgiaDiaria>(repository.getLiturgiaDiaria(Calendar.getInstance()))
    val liturgiaState: StateFlow<LiturgiaDiaria> = _liturgiaState.asStateFlow()

    // ----------------------------------------------------------------
    // Exame de Consciência State (Room)
    // ----------------------------------------------------------------
    val exameItensState: StateFlow<List<ExameItem>> = repository.allExameItens
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _expandedExameCategories = MutableStateFlow<Set<String>>(emptySet())
    val expandedExameCategories: StateFlow<Set<String>> = _expandedExameCategories.asStateFlow()

    // ----------------------------------------------------------------
    // Map State
    // ----------------------------------------------------------------
    private val _selectedIgrejaForMap = MutableStateFlow<NetworkIgreja?>(null)
    val selectedIgrejaForMap: StateFlow<NetworkIgreja?> = _selectedIgrejaForMap.asStateFlow()

    init {
        loadClerigos()
        loadIgrejas()
        loadCelebracoes()
    }

    // ----------------------------------------------------------------
    // API Actions
    // ----------------------------------------------------------------
    fun loadClerigos() {
        viewModelScope.launch {
            _clerigosState.value = UiState.Loading
            try {
                val search = _searchClerigosQuery.value.ifBlank { null }
                val data = repository.getClerigos(search)
                _clerigosState.value = UiState.Success(data)
            } catch (e: Exception) {
                _clerigosState.value = UiState.Error(e.message ?: "Erro desconhecido")
            }
        }
    }

    fun loadIgrejas() {
        viewModelScope.launch {
            _igrejasState.value = UiState.Loading
            try {
                val bairro = _bairroFilter.value.ifBlank { null }
                val data = repository.getIgrejas(bairro)
                _igrejasState.value = UiState.Success(data)
            } catch (e: Exception) {
                _igrejasState.value = UiState.Error(e.message ?: "Erro desconhecido")
            }
        }
    }

    fun loadCelebracoes() {
        viewModelScope.launch {
            _celebracoesState.value = UiState.Loading
            try {
                val cat = _selectedCategoriaCelebracao.value.let { if (it == "Todas") null else it }
                val dia = _selectedDiaCelebracao.value.let { if (it == "Todos") null else it }
                val data = repository.getCelebracoes(categoria = cat, dia = dia)
                _celebracoesState.value = UiState.Success(data)
            } catch (e: Exception) {
                _celebracoesState.value = UiState.Error(e.message ?: "Erro desconhecido")
            }
        }
    }

    fun setSearchClerigos(query: String) {
        _searchClerigosQuery.value = query
        loadClerigos()
    }

    fun setBairroFilter(bairro: String) {
        _bairroFilter.value = bairro
        loadIgrejas()
    }

    fun selectCelebracoesFilters(categoria: String, dia: String) {
        _selectedCategoriaCelebracao.value = categoria
        _selectedDiaCelebracao.value = dia
        loadCelebracoes()
    }

    // ----------------------------------------------------------------
    // Liturgia Actions
    // ----------------------------------------------------------------
    fun selectLiturgiaDate(calendar: Calendar) {
        _selectedLiturgiaDate.value = calendar
        _liturgiaState.value = repository.getLiturgiaDiaria(calendar)
    }

    fun selectLiturgiaOffset(days: Int) {
        val current = _selectedLiturgiaDate.value.clone() as Calendar
        current.add(Calendar.DAY_OF_YEAR, days)
        selectLiturgiaDate(current)
    }

    // ----------------------------------------------------------------
    // Room / Exame Actions
    // ----------------------------------------------------------------
    fun toggleExameItem(item: ExameItem) {
        viewModelScope.launch {
            repository.updateExameItem(item.copy(isChecked = !item.isChecked))
        }
    }

    fun toggleExameCategoryExpanded(category: String) {
        val current = _expandedExameCategories.value.toMutableSet()
        if (current.contains(category)) {
            current.remove(category)
        } else {
            current.add(category)
        }
        _expandedExameCategories.value = current
    }

    fun resetExameProgress() {
        viewModelScope.launch {
            repository.resetExameProgress()
        }
    }

    // ----------------------------------------------------------------
    // Map Actions
    // ----------------------------------------------------------------
    fun selectIgrejaForMap(igreja: NetworkIgreja?) {
        _selectedIgrejaForMap.value = igreja
    }

    fun selectIgrejaBySlug(slug: String, onResult: (NetworkIgreja) -> Unit) {
        viewModelScope.launch {
            try {
                val church = repository.getIgrejaBySlug(slug)
                onResult(church)
            } catch (e: Exception) {
                // Ignore gracefully
            }
        }
    }
}

// Unified UI State definition for safety and type safety
sealed interface UiState<out T> {
    object Loading : UiState<Nothing>
    data class Success<out T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}
