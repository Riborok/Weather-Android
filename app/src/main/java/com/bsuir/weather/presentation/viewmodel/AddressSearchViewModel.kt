package com.bsuir.weather.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bsuir.weather.domain.usecase.GetLocationSuggestionUseCase
import com.bsuir.weather.domain.usecase.StoredLocationListUseCase
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class AddressSearchViewModel @Inject constructor(
    private val getLocationSuggestionUseCase: GetLocationSuggestionUseCase,
    @Named("SavedLocationListUseCase") private val storedLocationListUseCase: StoredLocationListUseCase
) : ViewModel() {
    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText.asStateFlow()

    private val _cityResults = MutableStateFlow<List<AutocompletePrediction>>(emptyList())
    val cityResults: StateFlow<List<AutocompletePrediction>> = _cityResults.asStateFlow()

    private val sessionToken = AutocompleteSessionToken.newInstance()

    fun onSearchTextChanged(text: String) {
        _searchText.value = text
        searchCities(text)
    }

    private fun searchCities(query: String) {
        if (query.isNotBlank()) {
            viewModelScope.launch {
                _cityResults.value = getLocationSuggestionUseCase.getAddressSuggestions(
                    query = query,
                    sessionToken = sessionToken
                )
            }
        } else {
            _cityResults.value = emptyList()
        }
    }

    fun saveLocation(
        placeId: String
    ) {
        viewModelScope.launch {
            val location = getLocationSuggestionUseCase.getLocationByPlaceId(
                placeId = placeId
            )
            storedLocationListUseCase.saveLocation(location)
        }
    }
}
