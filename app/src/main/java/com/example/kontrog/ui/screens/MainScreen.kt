//MainScreen
package com.example.kontrog.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.example.kontrog.data.models.Building
import com.example.kontrog.data.models.FireExtinguisher
import com.example.kontrog.data.models.User
import com.example.kontrog.ui.navigation.AppRoute
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit

// --------------------------------------------------------
// --- Вспомогательные функции даты/времени ---
// --------------------------------------------------------

/**
 * Форматирует Long (Timestamp) в нужный строковый формат "ДД.ММ.ГГ ЧЧ:ММ"
 */
fun Long.toFormattedDateTime(): String {
    if (this == 0L) return ""
    // Используем системный часовой пояс для конвертации
    val localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneId.systemDefault())
    return String.format(
        "%02d.%02d.%d %02d:%02d",
        localDateTime.dayOfMonth,
        localDateTime.monthValue,
        localDateTime.year % 100, // Последние 2 цифры года
        localDateTime.hour,
        localDateTime.minute
    )
}

// --------------------------------------------------------
// --- ViewModel и Модели состояния ---
// --------------------------------------------------------

data class ExtinguisherListItem(
    val extinguisher: FireExtinguisher,
    val building: Building,
    val responsibleUser: User? = null,
    val isExpired: Boolean
)

class ExtinguisherViewModel : ViewModel() {

    private val _extinguishers = MutableStateFlow<List<ExtinguisherListItem>>(emptyList())
    val extinguishers: StateFlow<List<ExtinguisherListItem>> = _extinguishers.asStateFlow()

    private val _notifications = MutableStateFlow<List<String>>(emptyList())
    val notifications: StateFlow<List<String>> = _notifications.asStateFlow()

    init {
        loadExtinguisherData()
    }

    private fun loadExtinguisherData() {
        viewModelScope.launch {
            // Имитация задержки загрузки
            delay(500)

            // --- Фейковые данные ---
            val fakeResponsibleUser = User(
                id = "u1",
                fullName = "Иванов И.И.",
                role = "user"
            )

            val fakeBuilding = Building(
                id = "b1",
                organizationId = "org1",
                name = "ШКОЛА №5",
                address = "УЛ. ЛЕНИНА, 45"
            )

            val now = System.currentTimeMillis()

            // 1. Просроченный (для скриншота)
            val expiredExtinguisher = FireExtinguisher(
                id = "e1",
                buildingId = fakeBuilding.id,
                inventoryNumber = "ЭКЦ-1234567890",
                locationRoom = "КАБ. ИСТОРИИ. РЯДОМ С УХОД.№2",
                type = "ОП-4",
                manufacturer = "Венгрия",
                dateCommissioned = now - TimeUnit.DAYS.toMillis(365 * 2) , // 2 года назад
                nextRechargeDate = now - TimeUnit.DAYS.toMillis(1), // Просрочен вчера
                nextInspectionDate = now - TimeUnit.DAYS.toMillis(10), // Просрочен 10 дней назад
                status = "Expired"
            )

            // 2. Скоро истекает (для уведомлений)
            val soonExpiredExtinguisher = expiredExtinguisher.copy(
                id = "e2",
                inventoryNumber = "ЭКЦ-0000000001",
                type = "ОУ-5",
                nextRechargeDate = now + TimeUnit.DAYS.toMillis(30), // Истекает через 30 дней
                nextInspectionDate = now + TimeUnit.DAYS.toMillis(7), // Истекает через 7 дней (сработает уведомление)
                status = "SoonExpired"
            )

            // 3. В норме
            val okExtinguisher = expiredExtinguisher.copy(
                id = "e3",
                inventoryNumber = "ЭКЦ-0000000002",
                type = "ОВ-2",
                nextRechargeDate = now + TimeUnit.DAYS.toMillis(365), // Истекает через год
                nextInspectionDate = now + TimeUnit.DAYS.toMillis(365 * 2), // Истекает через 2 года
                status = "OK"
            )

            val extinguishersList = listOf(expiredExtinguisher, soonExpiredExtinguisher, okExtinguisher)

            _extinguishers.value = extinguishersList.map { fireExtinguisher ->
                ExtinguisherListItem(
                    extinguisher = fireExtinguisher,
                    building = fakeBuilding,
                    responsibleUser = fakeResponsibleUser,
                    isExpired = isExpired(fireExtinguisher.nextRechargeDate, fireExtinguisher.nextInspectionDate)
                )
            }

            // --- Логика уведомлений ---
            processNotifications(extinguishersList)
        }
    }

    private fun isExpired(rechargeDate: Long, inspectionDate: Long): Boolean {
        val now = System.currentTimeMillis()
        return rechargeDate < now || inspectionDate < now
    }

    private fun processNotifications(extinguishers: List<FireExtinguisher>) {
        val notifyDays = listOf(30, 14, 7, 1) // Дни для уведомления
        val currentNotifications = mutableListOf<String>()
        val now = System.currentTimeMillis()

        for (item in extinguishers) {
            // Проверка просрочки
            if (isExpired(item.nextRechargeDate, item.nextInspectionDate)) {
                currentNotifications.add("❌ ОГНЕТУШИТЕЛЬ ${item.type} (${item.inventoryNumber}) ПРОСРОЧЕН!")
                continue
            }

            // Проверка перезарядки
            checkNotification(item.nextRechargeDate, now, notifyDays)?.let { days ->
                currentNotifications.add("⚠️ Срок перезарядки ${item.type} (${item.inventoryNumber}) истекает через $days д.")
            }

            // Проверка освидетельствования
            checkNotification(item.nextInspectionDate, now, notifyDays)?.let { days ->
                currentNotifications.add("⚠️ Срок освидетельствования ${item.type} (${item.inventoryNumber}) истекает через $days д.")
            }
        }
        _notifications.value = currentNotifications.distinct()
    }

    private fun checkNotification(dueDate: Long, now: Long, notifyDays: List<Int>): Int? {
        if (dueDate <= now) return null
        val diffDays = TimeUnit.MILLISECONDS.toDays(dueDate - now)

        // Ищем, если разница в днях точно совпадает с одним из порогов уведомления
        return notifyDays.find { it.toLong() == diffDays }
    }
}

// --------------------------------------------------------
// --- Основной экран и Навигация ---
// --------------------------------------------------------

@Composable
fun MainScreen(navController: NavHostController) {
    var selectedTab by remember { mutableStateOf(MainTab.Extinguishers) } // Начинаем со Средств ПБ

    Scaffold(
        topBar = {
            MainScreenTopBar(navController)
        },
        containerColor = Color.Black
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            MainScreenTabs(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                when (selectedTab) {
                    MainTab.Main -> UserDashboardContent()
                    MainTab.Extinguishers -> ExtinguisherListScreen()
                    MainTab.Documents -> Text("ДОКУМЕНТЫ", color = Color.White)
                    MainTab.Acts -> Text("АКТЫ", color = Color.White)
                }
            }
        }
    }
}

enum class MainTab(val title: String) {
    Main("ОСНОВНОЕ"),
    Extinguishers("СРЕДСТВА ПБ"),
    Documents("ДОКУМЕНТЫ"),
    Acts("АКТЫ")
}

@Composable
fun MainScreenTabs(selectedTab: MainTab, onTabSelected: (MainTab) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MainTab.entries.forEach { tab ->
            TabButton(
                title = tab.title,
                isSelected = tab == selectedTab,
                onClick = { onTabSelected(tab) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun TabButton(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(36.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color(0xFF555555) else Color.Transparent,
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            textAlign = TextAlign.Center
        )
    }
}

// --------------------------------------------------------
// --- Компоненты Top Bar ---
// --------------------------------------------------------

@Composable
fun MainScreenTopBar(navController: NavHostController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        SearchField(modifier = Modifier.weight(1f))

        IconButton(
            onClick = { /* TODO: открыть фильтр */ },
            modifier = Modifier.size(48.dp)
        ) {
            Icon(Icons.Default.Tune, contentDescription = "Фильтр", tint = Color.White)
        }

        IconButton(
            onClick = { navController.navigate("notifications") }, // теперь работает
            modifier = Modifier.size(48.dp)
        ) {
            Icon(Icons.Default.Notifications, contentDescription = "Уведомления", tint = Color.White)
        }

        IconButton(
            onClick = { navController.navigate(AppRoute.Profile.route) }, // открывает вкладку профиля
            modifier = Modifier.size(48.dp)
        ) {
            Icon(Icons.Default.Person, contentDescription = "Аккаунт", tint = Color.White)
        }
        IconButton(
            onClick = { navController.navigate("chat") },
            modifier = Modifier.size(48.dp)
        ) {
            Icon(Icons.Default.Chat, contentDescription = "Чат")
        }
    }
}

@Composable
fun SearchField(modifier: Modifier = Modifier) {
    var text by remember { mutableStateOf("") }
    OutlinedTextField(
        value = text,
        onValueChange = { text = it },
        placeholder = { Text("Добрый день, Иван...", color = Color.Gray) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Поиск", tint = Color.White) },
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color(0xFF2E2E2E),
            unfocusedContainerColor = Color(0xFF2E2E2E),
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            cursorColor = Color.White,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
        ),
        modifier = modifier
            .height(50.dp)
            .padding(end = 8.dp)
    )
}

@Composable
fun UserDashboardContent() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Привет, Пользователь! (Главный экран)", color = Color.White)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Здесь будет дашборд с общими сводками.", textAlign = TextAlign.Center, color = Color.White)
    }
}

// --------------------------------------------------------
// --- Экран списка огнетушителей (СРЕДСТВА ПБ) ---
// --------------------------------------------------------

@Composable
fun ExtinguisherListScreen(
    viewModel: ExtinguisherViewModel = viewModel()
) {
    val items = viewModel.extinguishers.collectAsState().value

    if (items.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color.White)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items) { item ->
                ExtinguisherCard(item = item)
            }
        }
    }
}

// --------------------------------------------------------
// --- Карточка огнетушителя (ExtinguisherCard) ---
// --------------------------------------------------------

@Composable
fun ExtinguisherCard(item: ExtinguisherListItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2E2E2E)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // 1. Место для изображения
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(Color(0xFF555555), RoundedCornerShape(8.dp))
            ) {
                Icon(
                    Icons.Default.FireExtinguisher, // Используем более релевантную иконку, если доступна
                    contentDescription = "Огнетушитель",
                    tint = Color.White,
                    modifier = Modifier.align(Alignment.Center).size(32.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 2. Основная информация
            Column(modifier = Modifier.weight(1f)) {
                // Тип
                Text(
                    text = "ОГНЕТУШИТЕЛЬ",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = item.extinguisher.type.uppercase(),
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                // Здание и адрес
                Text(
                    text = "${item.building.name} ${item.building.address}",
                    color = Color.LightGray,
                    style = MaterialTheme.typography.bodyMedium
                )

                // Место установки и инвентарный номер
                Text(
                    text = "ИНВ. №: ${item.extinguisher.inventoryNumber}",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "МЕСТО ХРАНЕНИЯ: ${item.extinguisher.locationRoom}",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodySmall
                )

                // Имитация даты последней работы
                val lastInspectionDate = item.extinguisher.nextInspectionDate - TimeUnit.DAYS.toMillis(365)
                val lastRechargeDate = item.extinguisher.nextRechargeDate - TimeUnit.DAYS.toMillis(365)

                Text(
                    text = "ПРОВЕДЕНО: ПЕРЕЗАРЯДКА: ${lastRechargeDate.toFormattedDateTime().substringBefore(" ")}",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "ПРОВЕДЕНО: ОСВИДЕТЕЛЬСТВОВАНИЕ: ${lastInspectionDate.toFormattedDateTime().substringBefore(" ")}",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // 3. Дата и время статуса
            Column(horizontalAlignment = Alignment.End) {
                // Дата и время последней активности (имитация)
                val statusTime = item.extinguisher.dateCommissioned
                Text(
                    text = statusTime.toFormattedDateTime().substringBefore("."),
                    color = Color.LightGray,
                    style = MaterialTheme.typography.labelSmall
                )
                Text(
                    text = statusTime.toFormattedDateTime().substringAfter(" "),
                    color = Color.LightGray,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }

        // 4. Разделитель и статус/Ответственный
        HorizontalDivider(color = Color(0xFF555555), thickness = 1.dp)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Статус (ПРОСРОЧЕН)
            if (item.isExpired) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color.Red, RoundedCornerShape(4.dp))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ПРОСРОЧЕН",
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            } else {
                Text(
                    text = item.extinguisher.status.uppercase(),
                    color = Color.Green,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                )
            }

            // Ответственный
            Text(
                text = "ОТВЕТСТВЕННЫЙ: ${item.responsibleUser?.fullName ?: "Не указан"}",
                color = Color.White,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}