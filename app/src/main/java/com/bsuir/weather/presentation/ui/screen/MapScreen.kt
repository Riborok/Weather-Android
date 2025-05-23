package com.bsuir.weather.presentation.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bsuir.weather.R
import com.bsuir.weather.presentation.state.CoordinatesState
import com.bsuir.weather.presentation.ui.component.content.LoadingContent
import com.bsuir.weather.presentation.ui.component.top_bar.TopAppBarWithBackButton
import com.bsuir.weather.presentation.ui.utils.RequestLocationPermission
import com.bsuir.weather.presentation.viewmodel.MapViewModel
import com.bsuir.weather.utils.constants.mapZoom
import com.bsuir.weather.utils.ext.primaryIconButtonColors
import com.bsuir.weather.utils.ext.primaryTextFieldColors
import com.bsuir.weather.utils.mapper.CoordinatesMapper.toLatLng
import com.bsuir.weather.utils.mapper.CoordinatesMapper.toModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun MapScreen(
    onNavigateToMainClick: () -> Unit,
    modifier: Modifier = Modifier,
    mapViewModel: MapViewModel = hiltViewModel()
) {
    var permissionGranted by remember { mutableStateOf(false) }
    RequestLocationPermission { granted ->
        permissionGranted = granted
    }

    val currentCoordinatesState by mapViewModel.currentCoordinatesState.collectAsState()
    val cameraPositionState = rememberCameraPositionState()
    val userInput by mapViewModel.userInput.collectAsState()
    val selectedCoordinates by mapViewModel.selectedCoordinates.collectAsState()

    when (currentCoordinatesState) {
        is CoordinatesState.Loading -> {
            LoadingContent()
            return
        }
        is CoordinatesState.Success -> {
            val currentCoordinates = (currentCoordinatesState as CoordinatesState.Success).coordinates
            cameraPositionState.position = CameraPosition.fromLatLngZoom(
                currentCoordinates.toLatLng(),
                mapZoom
            )
        }
        is CoordinatesState.NoContent -> {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(
                LatLng(53.9021, 27.5505), mapZoom
            )
        }
    }

    Scaffold (
        topBar = {
            TopAppBarWithBackButton(
                title = stringResource(R.string.add_with_map),
                onBackClick = { onNavigateToMainClick() },
                modifier = Modifier
                    .padding(horizontal = 16.dp)
            )
        }
    ) { innerPadding ->
        Column (
            modifier = modifier.padding(innerPadding)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = userInput,
                        onValueChange = mapViewModel::onUserInputChange,
                        label = { Text(stringResource(R.string.enter_alias)) },
                        trailingIcon = {
                            IconButton (
                                onClick = {
                                    mapViewModel.saveLocation()
                                    onNavigateToMainClick()
                                },
                                enabled = selectedCoordinates != null,
                                colors = MaterialTheme.colorScheme.primaryIconButtonColors,
                                modifier = Modifier.padding(end = 4.dp)
                            ) {
                                Icon(
                                    Icons.Default.Done,
                                    contentDescription = stringResource(R.string.done)
                                )
                            }
                        },
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium,
                        colors = MaterialTheme.colorScheme.primaryTextFieldColors,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    )
                }
            }

            GoogleMap(
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = permissionGranted),
                onMapClick = { latLng -> mapViewModel.onMapClick(latLng.toModel()) },
                modifier = Modifier
                    .fillMaxSize()
            ) {
                selectedCoordinates?.let { coordinates ->
                    Marker(
                        state = MarkerState(position = coordinates.toLatLng()),
                        title = userInput.ifBlank { stringResource(R.string.selected_point) }
                    )
                }
            }
        }
    }
}