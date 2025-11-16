import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector


sealed class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
) {
    object Profile : BottomNavItem("profile_route", Icons.Default.Person, "Аккаунт")
    object Main : BottomNavItem("main_route", Icons.Default.Home, "Главный")
    object Object : BottomNavItem("object_route", Icons.Default.LocationOn, "Объект")
    object Map : BottomNavItem("map_route", Icons.Default.Map, "Карта")
    object Docs : BottomNavItem("docs_route", Icons.Default.Description, "Документы")
}

sealed class Screen(val route: String) {
    object Notifications : Screen("notifications_screen")
    object Auth : Screen("auth_route")
    object Splash : Screen("splash_route")
}

val bottomNavItems = listOf(
    BottomNavItem.Profile,
    BottomNavItem.Main,
    BottomNavItem.Object,
    BottomNavItem.Map,
    BottomNavItem.Docs
)