package com.example.kontrog.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.IconStyle
import com.yandex.runtime.image.ImageProvider
import com.yandex.mapkit.Animation
import com.yandex.mapkit.map.MapObjectCollection
import com.example.kontrog.R
import android.util.Log

@Composable
fun MapScreen(
    rootNavController: NavController,
    viewModel: MapViewModel = viewModel()
) {
    val context = LocalContext.current
    val buildings by viewModel.buildings.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(buildings) {
        Log.d("MapScreen", "Buildings received: ${buildings.size}")
        buildings.forEachIndexed { index, building ->
            Log.d("MapScreen", "Building $index: ${building.address}, " +
                    "lat=${building.latitude}, lng=${building.longitude}")
        }
    }

    val mapView = remember {
        MapView(context).apply {
            map.move(
                com.yandex.mapkit.map.CameraPosition(
                    Point(55.751244, 37.617494),
                    10.0f,
                    0.0f,
                    0.0f
                )
            )
        }
    }

    val markersCollection = remember {
        mapView.map.mapObjects.addCollection()
    }

    LaunchedEffect(buildings) {
        if (buildings.isNotEmpty()) {
            Log.d("MapScreen", "Updating markers for ${buildings.size} buildings")

            markersCollection.clear()
            val icon = ImageProvider.fromResource(context, R.drawable.placementhome)

            buildings.forEach { building ->
                if (building.latitude != 0.0 && building.longitude != 0.0) {
                    val point = Point(building.latitude, building.longitude)

                    val placemark = markersCollection.addPlacemark(point)
                    placemark.setIcon(icon)
                    placemark.setIconStyle(
                        IconStyle().apply {
                            scale = 1.5f
                            anchor = android.graphics.PointF(0.5f, 1.0f)
                        }
                    )

                    Log.d("MapScreen", "Added marker at: ${building.latitude}, ${building.longitude}")
                } else {
                    Log.w("MapScreen", "Invalid coordinates for building: ${building.address}")
                }
            }

            val validBuildings = buildings.filter { it.latitude != 0.0 && it.longitude != 0.0 }
            if (validBuildings.isNotEmpty()) {
                val firstBuilding = validBuildings.first()
                mapView.map.move(
                    com.yandex.mapkit.map.CameraPosition(
                        Point(firstBuilding.latitude, firstBuilding.longitude),
                        15.0f,
                        0.0f,
                        0.0f
                    ),
                    Animation(Animation.Type.SMOOTH, 1f),
                    null
                )
                Log.d("MapScreen", "Camera moved to: ${firstBuilding.latitude}, ${firstBuilding.longitude}")
            }
        } else {
            Log.d("MapScreen", "No buildings to display")
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { mapView }
        )
    }

    DisposableEffect(LocalLifecycleOwner.current) {
        Log.d("MapScreen", "Starting map")
        MapKitFactory.getInstance().onStart()
        mapView.onStart()

        onDispose {
            Log.d("MapScreen", "Stopping map")
            mapView.onStop()
            MapKitFactory.getInstance().onStop()
        }
    }
}