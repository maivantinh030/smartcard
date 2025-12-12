package org.example.project.screen.user

import androidx.compose.foundation.background
import androidx.compose.foundation. layout.*
import androidx.compose. foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose. foundation.shape.CircleShape
import androidx.compose. foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose. runtime.*
import androidx.compose. ui.Alignment
import androidx. compose.ui.Modifier
import androidx.compose.ui.draw. clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui. text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose. ui.unit.sp
import kotlinx.coroutines.launch
import org.example.project.SmartCardManager
import org.example.project. screen.FloatingBubbles
import org.example.project. GameEntry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserGameListScreen(
    smartCardManager: SmartCardManager,
    onBack: () -> Unit
) {
    var games by remember { mutableStateOf<List<GameEntry>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    fun loadGames() {
        scope.launch {
            isLoading = true
            try {
                val gameList = smartCardManager.readGames()
                games = gameList
                status = if (gameList.isEmpty())
                    "📭 Chưa có vé game nào"
                else
                    "✅ Đã tải ${gameList.size} game"
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
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Card(
                modifier = Modifier. fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFBA68C8)),
                elevation = CardDefaults. cardElevation(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onBack,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color. White. copy(alpha = 0.2f),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier. size(48.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("←", fontSize = 20.sp)
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "🎮 Vé Chơi Game",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color. White
                        )
                    }

                    Button(
                        onClick = { loadGames() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color. White.copy(alpha = 0.2f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(12.dp)
                    ) {
                        Text("🔄", fontSize = 16.sp)
                    }
                }
            }

            Spacer(modifier = Modifier. height(16.dp))

            // Status message
            if (status.isNotEmpty()) {
                Card(
                    modifier = Modifier. fillMaxWidth(),
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
                            text = "Chưa có vé nào",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                        Text(
                            text = "Liên hệ quản lý để mua vé nhé! ",
                            fontSize = 14.sp,
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
                        GameCard(
                            game = game,
                            onUseTicket = {
                                scope.launch {
                                    try {
                                        smartCardManager.decreaseGameTickets(game.gameCode, 1)
                                        status = "✅ Đã sử dụng 1 vé game ${game.gameCode}"
                                        loadGames()
                                    } catch (e: Exception) {
                                        status = "❌ Lỗi:  ${e.message}"
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

// ✅ THÊM COMPOSABLE NÀY
@Composable
fun GameCard(
    game: GameEntry,
    onUseTicket:  () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Row(
            modifier = Modifier
                . fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    . background(
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

            Column(modifier = Modifier. weight(1f)) {
                Text(
                    text = "Game #${game.gameCode}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )
                Spacer(modifier = Modifier. height(4.dp))
                Text(
                    text = "Số vé: ${game. tickets}",
                    fontSize = 14.sp,
                    color = Color(0xFF666666)
                )
            }

            Button(
                onClick = onUseTicket,
                enabled = game.tickets > 0,
                colors = ButtonDefaults. buttonColors(
                    containerColor = Color(0xFFBA68C8),
                    disabledContainerColor = Color.LightGray
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("🎮 Chơi", fontSize = 14.sp)
            }
        }
    }
}