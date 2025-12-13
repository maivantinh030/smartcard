package org.example.project.screen.user

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui. unit.sp
import org.example.project.SmartCardManager
import org.example.project.screen.FloatingBubbles

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserMainMenuScreen(
    smartCardManager: SmartCardManager,
    onNavigateViewInfo: () -> Unit,
    onNavigateBuyTickets: () -> Unit,
    onNavigateGames: () -> Unit,
    onNavigateChangePin: () -> Unit,
    onDisconnect: () -> Unit
) {
    val menuOptions = remember {
        listOf(
            UserMenuOption(
                title = "Thông tin của tôi",
                emoji = "👤",
                description = "Xem thông tin cá nhân & ảnh",
                gradientColors = listOf(Color(0xFF64B5F6), Color(0xFF90CAF9)),
                onClick = onNavigateViewInfo
            ),
            UserMenuOption(
                title = "Mua vé game",  // ✅ ĐỔI TÊN
                emoji = "🎟️",          // ✅ ĐỔI ICON
                description = "Mua vé chơi trò",  // ✅ ĐỔI MÔ TẢ
                gradientColors = listOf(Color(0xFF81C784), Color(0xFFA5D6A7)),
                onClick = onNavigateBuyTickets  // ✅ ĐỔI CALLBACK
            ),
            UserMenuOption(
                title = "Vé của tôi",  // ✅ ĐỔI TÊN
                emoji = "🎫",          // ✅ ĐỔI ICON
                description = "Xem vé đã mua",  // ✅ ĐỔI MÔ TẢ
                gradientColors = listOf(Color(0xFFBA68C8), Color(0xFFCE93D8)),
                onClick = onNavigateGames
            ),
            UserMenuOption(
                title = "Đổi mã PIN",
                emoji = "🔐",
                description = "Thay đổi mã bảo mật",
                gradientColors = listOf(Color(0xFFFFB74D), Color(0xFFFFCC02)),
                onClick = onNavigateChangePin
            )
        )
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

        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF64B5F6)),
                elevation = CardDefaults. cardElevation(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color. White. copy(alpha = 0.4f),
                                        Color. White.copy(alpha = 0.2f),
                                        Color. Transparent
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("👋", fontSize = 40.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Xin chào, Khách hàng! ",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color. White
                    )

                    Text(
                        text = "Bạn muốn làm gì hôm nay?",
                        fontSize = 16.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }

            // Menu Grid
            Column(
                modifier = Modifier
                    .weight(1f)
                    . padding(horizontal = 20.dp)
                    .padding(top = 16.dp),
                verticalArrangement = Arrangement. spacedBy(16.dp)
            ) {
                menuOptions.chunked(2).forEach { rowOptions ->
                    Row(
                        modifier = Modifier. fillMaxWidth(),
                        horizontalArrangement = Arrangement. spacedBy(16.dp)
                    ) {
                        rowOptions.forEach { option ->
                            UserMenuCard(
                                option = option,
                                modifier = Modifier. weight(1f)
                            )
                        }
                        if (rowOptions.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            // Disconnect button
            OutlinedButton(
                onClick = onDisconnect,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFFE53935)
                )
            ) {
                Text(
                    text = "🚪 Ngắt kết nối",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserMenuCard(
    option: UserMenuOption,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = option.onClick,
        modifier = modifier. height(160.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color. White),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            brush = Brush.radialGradient(colors = option.gradientColors),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = option.emoji, fontSize = 28.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = option.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier. height(4.dp))

                Text(
                    text = option.description,
                    fontSize = 12.sp,
                    color = Color(0xFF666666),
                    textAlign = TextAlign. Center,
                    lineHeight = 16.sp,
                    maxLines = 2
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color. Transparent,
                                option.gradientColors.first().copy(alpha = 0.05f)
                            )
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
            )
        }
    }
}

data class UserMenuOption(
    val title: String,
    val emoji: String,
    val description:  String,
    val gradientColors: List<Color>,
    val onClick: () -> Unit
)