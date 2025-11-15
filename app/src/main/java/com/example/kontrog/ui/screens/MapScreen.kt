package com.example.kontrog.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner // 👈 ЭТОТ ИМПОРТ НУЖЕН
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.PlacemarkMapObject // 👈 Нужен для работы с Placemark
import com.yandex.mapkit.map.MapObjectTapListener // 👈 Нужен для работы с addTapListener
import com.yandex.runtime.image.ImageProvider // 👈 Нужен, если будете задавать иконки

/**
 * 🗺️ Экран с картой, использующий Yandex MapKit.
 */
@Composable
fun MapScreen(
    rootNavController: NavController,
    viewModel: MapViewModel = viewModel() // Инициализация ViewModel
) {
    val context = LocalContext.current
    val buildings by viewModel.buildings.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val mapView = remember {
        MapView(context).apply {
            // Установка начальной камеры на некую центральную точку
            map.move(
                com.yandex.mapkit.map.CameraPosition(
                    Point(55.751244, 37.617494), // Москва
                    11.0f,
                    0.0f,
                    0.0f
                )
            )
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { mapView },
            update = { view ->
                if (!isLoading && buildings.isNotEmpty()) {
                    // 1. Очищаем старые маркеры перед обновлением
                    view.map.mapObjects.clear()

                    val mapObjects = view.map.mapObjects.addCollection()

                    // 2. Добавляем новые маркеры
                    buildings.forEach { building ->
                        if (building.latitude != 0.0 && building.longitude != 0.0) {
                            val markerPoint = Point(building.latitude, building.longitude)

                            // Добавляем маркер (Placemark)
                            val placemark = mapObjects.addPlacemark().apply {
                                geometry = markerPoint
                                // Временный маркер: используйте собственный ImageProvider для иконок
                                //
                                // Если нет своей иконки, можно использовать стандартную.
                                isDraggable = false
                                // Устанавливаем иконку (можно использовать растровое изображение)
                                // icon = ImageProvider.fromResource(context, R.drawable.ic_map_marker)
                            }

                            // 3. Добавляем обработчик нажатия
                            placemark.addTapListener { _, _ ->
                                // TODO: Открыть BottomSheet или диалог с деталями здания
                                true
                            }
                        }
                    }

                    // 4. Опционально: центрируем камеру на первом объекте или на кластере
                    buildings.firstOrNull()?.let { firstBuilding ->
                        view.map.move(
                            com.yandex.mapkit.map.CameraPosition(
                                Point(firstBuilding.latitude, firstBuilding.longitude),
                                14.0f, 0.0f, 0.0f
                            ),
                            com.yandex.mapkit.Animation(com.yandex.mapkit.Animation.Type.SMOOTH, 1f),
                            null
                        )
                    }
                }
            }
        )
    }

    // Обработка жизненного цикла MapView:
    DisposableEffect(LocalLifecycleOwner.current) {
        MapKitFactory.getInstance().onStart()
        mapView.onStart()
        onDispose {
            mapView.onStop()
            MapKitFactory.getInstance().onStop()
        }
    }
}