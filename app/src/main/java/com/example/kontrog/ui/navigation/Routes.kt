import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

/** Определяет элементы нижнего навигационного меню */
sealed class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
) {
    // Временно используем иконки Icons.Default.
    object Profile : BottomNavItem("profile_route", Icons.Default.Person, "Аккаунт")
    object Main : BottomNavItem("main_route", Icons.Default.Home, "Главный")
    object Object : BottomNavItem("object_route", Icons.Default.LocationOn, "Объект")
    object Map : BottomNavItem("map_route", Icons.Default.Map, "Карта")
    object Docs : BottomNavItem("docs_route", Icons.Default.Description, "Документы")
}

/** Определяет экраны, которые не являются частью нижнего меню (отдельные экраны) */
sealed class Screen(val route: String) {
    object Notifications : Screen("notifications_screen")
    object Auth : Screen("auth_route") // Главный экран входа
    object Splash : Screen("splash_route") // Для проверки аутентификации
}

val bottomNavItems = listOf(
    BottomNavItem.Profile,
    BottomNavItem.Main,
    BottomNavItem.Object,
    BottomNavItem.Map,
    BottomNavItem.Docs
)