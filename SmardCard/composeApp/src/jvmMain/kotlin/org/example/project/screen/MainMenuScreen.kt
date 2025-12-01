package org.example.project.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.SmartCardManager

//
//data class MenuOption(
//    val title: String,
//    val emoji: String,
//    val description: String,
//    val color: Color,
//    val onClick: () -> Unit
//)
//
//@Composable
//fun MainMenuScreen() {
//    val menuOptions = listOf(
//        MenuOption(
//            title = "Ghi thông tin",
//            emoji = "✍️",
//            description = "Ghi thông tin khách hàng lên thẻ",
//            color = Color(0xFF4CAF50)
//        ) { /* Navigate to write screen */ },
//
//        MenuOption(
//            title = "Xem thông tin",
//            emoji = "👁️",
//            description = "Xem thông tin từ thẻ",
//            color = Color(0xFF2196F3)
//        ) { /* Navigate to read screen */ },
//
//        MenuOption(
//            title = "Đổi mật khẩu",
//            emoji = "🔄",
//            description = "Thay đổi mã PIN",
//            color = Color(0xFFFF9800)
//        ) { /* Navigate to change pin screen */ },
//
//        MenuOption(
//            title = "Cài đặt",
//            emoji = "⚙️",
//            description = "Cấu hình hệ thống",
//            color = Color(0xFF9C27B0)
//        ) { /* Navigate to settings */ }
//    )
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(24.dp)
//    ) {
//        // Header
//        Card(
//            modifier = Modifier.fillMaxWidth(),
//            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
//            colors = CardDefaults.cardColors(containerColor = Color(0xFF1976D2))
//        ) {
//            Column(
//                modifier = Modifier.padding(24.dp),
//                horizontalAlignment = Alignment.CenterHorizontally
//            ) {
//                Text(
//                    text = "✅",
//                    fontSize = 48.sp
//                )
//                Spacer(modifier = Modifier.height(8.dp))
//                Text(
//                    text = "Thẻ đã kết nối thành công",
//                    fontSize = 20.sp,
//                    fontWeight = FontWeight.Bold,
//                    color = Color.White
//                )
//                Text(
//                    text = "Chọn chức năng bạn muốn sử dụng",
//                    fontSize = 14.sp,
//                    color = Color.White.copy(alpha = 0.8f)
//                )
//            }
//        }
//
//        Spacer(modifier = Modifier.height(32.dp))
//
//        // Menu Grid
//        LazyVerticalGrid(
//            columns = GridCells.Fixed(2),
//            horizontalArrangement = Arrangement.spacedBy(16.dp),
//            verticalArrangement = Arrangement.spacedBy(16.dp)
//        ) {
//            items(menuOptions) { option ->
//                MenuCard(option = option)
//            }
//        }
//
//        Spacer(modifier = Modifier.weight(1f))
//
//        // Logout Button
//        OutlinedButton(
//            onClick = { /* Disconnect and back to connect screen */ },
//            modifier = Modifier.fillMaxWidth(),
//            colors = ButtonDefaults.outlinedButtonColors(
//                contentColor = Color.Red
//            )
//        ) {
//            Text(
//                text = "🚪 Ngắt kết nối",
//                fontSize = 16.sp,
//                fontWeight = FontWeight.Medium
//            )
//        }
//    }
//}
//
//@Composable
//fun MenuCard(
//    option: MenuOption
//) {
//    Card(
//        onClick = option.onClick,
//        modifier = Modifier
//            .fillMaxWidth()
//            .aspectRatio(1.1f),
//        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
//        colors = CardDefaults.cardColors(containerColor = Color.White),
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(20.dp),
//            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.Center
//        ) {
//            // Colored circle background
//            Card(
//                modifier = Modifier.size(80.dp),
//                colors = CardDefaults.cardColors(containerColor = option.color),
//                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
//            ) {
//                Box(
//                    modifier = Modifier.fillMaxSize(),
//                    contentAlignment = Alignment.Center
//                ) {
//                    Text(
//                        text = option.emoji,
//                        fontSize = 36.sp
//                    )
//                }
//            }
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            Text(
//                text = option.title,
//                fontSize = 16.sp,
//                fontWeight = FontWeight.Bold,
//                textAlign = TextAlign.Center,
//                color = Color(0xFF333333)
//            )
//
//            Spacer(modifier = Modifier.height(4.dp))
//
//            Text(
//                text = option.description,
//                fontSize = 12.sp,
//                textAlign = TextAlign.Center,
//                color = Color.Gray,
//                lineHeight = 16.sp
//            )
//        }
//    }
//}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainMenuScreen(
    smartCardManager: SmartCardManager,
    onDisconnect: () -> Unit = {},
    onNavigateWriteInfo: () -> Unit,
    onNavigateReadInfo: () -> Unit,
    onNavigateChangePin: () -> Unit,
    onNavigateSettings: () -> Unit
) {
    val menuOptions = remember {
        listOf(
            MenuOption(
                title = "Ghi thông tin",
                emoji = "✍️",
                description = "Thêm khách mới vào thẻ",
                gradientColors = listOf(Color(0xFF81C784), Color(0xFFA5D6A7)),
                onClick = onNavigateWriteInfo
            ),
            MenuOption(
                title = "Xem thông tin",
                emoji = "👁️",
                description = "Xem thông tin & ảnh khách",
                gradientColors = listOf(Color(0xFF64B5F6), Color(0xFF90CAF9)),
                onClick = onNavigateReadInfo
            ),
            MenuOption(
                title = "Đổi mật khẩu",
                emoji = "🔐",
                description = "Thay đổi mã PIN bảo mật",
                gradientColors = listOf(Color(0xFFFFB74D), Color(0xFFFFCC02)),
                onClick = onNavigateChangePin
            ),
            MenuOption(
                title = "Cài đặt",
                emoji = "⚙️",
                description = "Cấu hình & quản lý hệ thống",
                gradientColors = listOf(Color(0xFFBA68C8), Color(0xFFCE93D8)),
                onClick = onNavigateSettings
            )
        )
    }

    // Background giống ConnectScreen
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFF3E0),  // cam nhạt
                        Color(0xFFFFF0F5),  // hồng phấn
                        Color(0xFFE0F7FA)   // xanh mint
                    ),
                    startY = 0f,
                    endY = 2000f
                )
            )
    ) {
        // Hiệu ứng bong bóng giống ConnectScreen
        FloatingBubbles()

        Column(modifier = Modifier.fillMaxSize()) {
            // Header siêu vui nhộn giống style ConnectScreen
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFF5252)),
                elevation = CardDefaults.cardElevation(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Success icon với hiệu ứng sáng
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.4f),
                                        Color.White.copy(alpha = 0.2f),
                                        Color.Transparent
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🎉", fontSize = 40.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Thẻ đã sẵn sàng vui chơi!",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = "Chọn chức năng bạn muốn nè!",
                        fontSize = 16.sp,
                        color = Color.White.copy(0.9f),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Grid 2 cột - style vui nhộn
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                items(menuOptions) { option ->
                    FunMenuCard(option = option)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Nút ngắt kết nối style vui nhộn
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .align(Alignment.CenterHorizontally),
                shape = RoundedCornerShape(32.dp),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                OutlinedButton(
                    onClick = onDisconnect,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    shape = RoundedCornerShape(32.dp),
                    border = BorderStroke(3.dp, Color(0xFFD32F2F)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFD32F2F)
                    )
                ) {
                    Text("🚪", fontSize = 28.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Ngắt kết nối",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun FunMenuCard(option: MenuOption) {
    Card(
        onClick = option.onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .shadow(12.dp, RoundedCornerShape(28.dp)), // Thêm shadow giống ConnectScreen
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(option.gradientColors),
                    shape = RoundedCornerShape(28.dp)
                )
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Emoji to bự trong vòng tròn trắng mờ giống style ConnectScreen
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = option.emoji,
                        fontSize = 48.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = option.title,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = option.description,
                    fontSize = 14.sp,
                    color = Color.White.copy(0.9f),
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

// Hiệu ứng bong bóng giống ConnectScreen

@Composable
fun ModernMenuCard(option: MenuOption) {
    Card(
        onClick = option.onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Icon circle with gradient background
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = option.gradientColors
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = option.emoji,
                        fontSize = 28.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = option.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = option.description,
                    fontSize = 12.sp,
                    color = Color(0xFF666666),
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp,
                    maxLines = 2
                )
            }

            // Subtle gradient overlay for premium feel
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                option.gradientColors.first().copy(alpha = 0.05f)
                            )
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
            )
        }
    }
}

data class MenuOption(
    val title: String,
    val emoji: String,
    val description: String,
    val gradientColors: List<Color>,
    val onClick: () -> Unit
)