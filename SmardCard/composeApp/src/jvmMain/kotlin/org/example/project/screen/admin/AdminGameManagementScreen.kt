package org.example.project.screen.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.example.project.SmartCardManager
import org.example.project.screen.FloatingBubbles
import org.example.project.GameEntry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminGameManagementScreen(
    smartCardManager: SmartCardManager,
    onBack: () -> Unit
) {
    var games by remember { mutableStateOf<List<GameEntry>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    fun loadGames() {
        scope.launch {
            isLoading = true
            try {
                games = smartCardManager.readGames()
                status = if (games.isEmpty())
                    "📭 Chưa có game nào"
                else
                    "✅ Đã tải ${games.size} game"
            } catch (e: Exception) {
                status = "❌ Lỗi:  ${e.message}"
                games = emptyList()
            }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        loadGames()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFF3E0),
                        Color(0xFFFFF0F5),
                        Color(0xFFE0F7FA)
                    )
                )
            )
    ) {
        FloatingBubbles()

        Column(
            modifier = Modifier
                . fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Card(
                modifier = Modifier. fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFBA68C8)),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        . fillMaxWidth()
                        . padding(20.dp),
                    verticalAlignment = Alignment. CenterVertically
                ) {
                    Button(
                        onClick = onBack,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(alpha = 0.2f),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.size(48.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("←", fontSize = 20.sp)
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "🎮 Quản Lý Vé Game",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Button(
                        onClick = { loadGames() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color. White. copy(alpha = 0.2f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(12.dp)
                    ) {
                        Text("🔄", fontSize = 16.sp)
                    }
                }
            }

            Spacer(modifier = Modifier. height(16.dp))

            // Add Game Button
            Button(
                onClick = { showAddDialog = true },
                modifier = Modifier. fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                )
            ) {
                Text("➕ Thêm game mới", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Status message
            if (status.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            status.startsWith("✅") -> Color(0xFFE8F5E9)
                            status.startsWith("📭") -> Color(0xFFFFF9C4)
                            else -> Color(0xFFFFEBEE)
                        }
                    )
                ) {
                    Text(
                        text = status,
                        modifier = Modifier.padding(16.dp),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Game List
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFFBA68C8))
                }
            } else if (games.isEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color. White),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("🎪", fontSize = 80.sp)
                        Spacer(modifier = Modifier. height(16.dp))
                        Text(
                            text = "Chưa có game nào",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier. weight(1f),
                    verticalArrangement = Arrangement. spacedBy(12.dp)
                ) {
                    items(games) { game ->
                        AdminGameCard(
                            game = game,
                            onAddTickets = { addAmount ->
                                scope.launch {
                                    try {
                                        smartCardManager.addOrIncreaseTickets(game.gameCode, addAmount)
                                        status = "✅ Đã thêm $addAmount vé cho game ${game.gameCode}"
                                        loadGames()
                                    } catch (e: Exception) {
                                        status = "❌ Lỗi: ${e.message}"
                                    }
                                }
                            },
                            onRemoveGame = {
                                scope.launch {
                                    try {
                                        smartCardManager.removeGame(game.gameCode)
                                        status = "✅ Đã xóa game ${game.gameCode}"
                                        loadGames()
                                    } catch (e: Exception) {
                                        status = "❌ Lỗi: ${e.message}"
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // Add Game Dialog
    if (showAddDialog) {
        AddGameDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { gameCode, tickets ->
                scope.launch {
                    try {
                        smartCardManager.addOrIncreaseTickets(gameCode, tickets)
                        status = "✅ Đã thêm game $gameCode với $tickets vé"
                        loadGames()
                        showAddDialog = false
                    } catch (e: Exception) {
                        status = "❌ Lỗi: ${e.message}"
                    }
                }
            }
        )
    }
}

@Composable
fun AdminGameCard(
    game: GameEntry,
    onAddTickets:  (Int) -> Unit,
    onRemoveGame: () -> Unit
) {
    var showAddTicketsDialog by remember { mutableStateOf(false) }
    var showRemoveDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier. fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color. White),
        elevation = CardDefaults. cardElevation(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier. fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFBA68C8),
                                    Color(0xFFCE93D8)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🎯", fontSize = 28.sp)
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Game #${game.gameCode}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    )
                    Spacer(modifier = Modifier. height(4.dp))
                    Text(
                        text = "Số vé: ${game.tickets}",
                        fontSize = 14.sp,
                        color = Color(0xFF666666)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement. spacedBy(8.dp)
            ) {
                Button(
                    onClick = { showAddTicketsDialog = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("➕ Thêm vé", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = { showRemoveDialog = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFE53935)
                    )
                ) {
                    Text("🗑️ Xóa", fontSize = 12.sp)
                }
            }
        }
    }

    // Add Tickets Dialog
    if (showAddTicketsDialog) {
        AddTicketsDialog(
            gameCode = game.gameCode,
            onDismiss = { showAddTicketsDialog = false },
            onConfirm = { amount ->
                onAddTickets(amount)
                showAddTicketsDialog = false
            }
        )
    }

    // Remove Confirmation Dialog
    if (showRemoveDialog) {
        AlertDialog(
            onDismissRequest = { showRemoveDialog = false },
            title = { Text("Xác nhận xóa") },
            text = { Text("Bạn có chắc muốn xóa game #${game.gameCode}? ") },
            confirmButton = {
                Button(
                    onClick = {
                        onRemoveGame()
                        showRemoveDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE53935)
                    )
                ) {
                    Text("Xóa")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRemoveDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }
}

@Composable
fun AddGameDialog(
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit
) {
    var gameCode by remember { mutableStateOf("") }
    var tickets by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Thêm game mới") },
        text = {
            Column {
                OutlinedTextField(
                    value = gameCode,
                    onValueChange = { gameCode = it },
                    label = { Text("Mã game") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                Spacer(modifier = Modifier. height(8.dp))
                OutlinedTextField(
                    value = tickets,
                    onValueChange = { tickets = it },
                    label = { Text("Số vé") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val code = gameCode.toIntOrNull()
                    val amount = tickets. toIntOrNull()
                    if (code != null && amount != null && amount > 0) {
                        onConfirm(code, amount)
                    }
                },
                enabled = gameCode.isNotEmpty() && tickets.isNotEmpty()
            ) {
                Text("Thêm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}

@Composable
fun AddTicketsDialog(
    gameCode: Int,
    onDismiss:  () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var amount by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Thêm vé cho game #$gameCode") },
        text = {
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Số vé thêm") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    val tickets = amount.toIntOrNull()
                    if (tickets != null && tickets > 0) {
                        onConfirm(tickets)
                    }
                },
                enabled = amount.isNotEmpty()
            ) {
                Text("Thêm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}