package org.example.project.screen

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.example.project.SmartCardManager
import kotlin.math.sin

//@Composable
//fun ConnectScreen(
//    onCardConnected: () -> Unit,
//    smartCardManager: SmartCardManager
//) {
//    var isConnecting by remember { mutableStateOf(false) }
//    var status by remember { mutableStateOf("") }
//
//    val smartCardManager = remember { SmartCardManager() }
//    val scope = rememberCoroutineScope()
//
//    Box(
//        modifier = Modifier.fillMaxSize(),
//        contentAlignment = Alignment.Center
//    ) {
//        Card(
//            modifier = Modifier
//                .padding(32.dp)
//                .fillMaxWidth(0.8f),
//            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
//            colors = CardDefaults.cardColors(containerColor = Color.White)
//        ) {
//            Column(
//                modifier = Modifier.padding(48.dp),
//                horizontalAlignment = Alignment.CenterHorizontally
//            ) {
//                Text(
//                    text = "💳",
//                    fontSize = 80.sp
//                )
//
//                Spacer(modifier = Modifier.height(24.dp))
//
//                Text(
//                    text = "SmartCard Manager",
//                    fontSize = 28.sp,
//                    fontWeight = FontWeight.Bold,
//                    color = Color(0xFF1976D2)
//                )
//
//                Spacer(modifier = Modifier.height(8.dp))
//
//                Text(
//                    text = "Vui lòng kết nối thẻ để tiếp tục",
//                    fontSize = 16.sp,
//                    color = Color.Gray,
//                    textAlign = TextAlign.Center
//                )
//
//                Spacer(modifier = Modifier.height(32.dp))
//
//                if (isConnecting) {
//                    CircularProgressIndicator(
//                        modifier = Modifier.size(40.dp),
//                        color = Color(0xFF1976D2)
//                    )
//                    Spacer(modifier = Modifier.height(16.dp))
//                    Text(
//                        text = "Đang kết nối...",
//                        fontSize = 14.sp,
//                        color = Color.Gray
//                    )
//                } else {
//                    Button(
//                        onClick = {
//                            scope.launch {
//                                isConnecting = true
//                                status = ""
//
//                                val connected = smartCardManager.connectToCard()
//                                if (connected) {
//                                    onCardConnected()
//                                } else {
//                                    status = "Không thể kết nối thẻ!"
//                                }
//                                isConnecting = false
//                            }
//                        },
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .height(56.dp),
//                        colors = ButtonDefaults.buttonColors(
//                            containerColor = Color(0xFF1976D2)
//                        )
//                    ) {
//                        Text(
//                            text = "🔌 Kết nối thẻ",
//                            fontSize = 18.sp,
//                            fontWeight = FontWeight.Medium,
//                            color = Color.White
//                        )
//                    }
//                }
//
//                if (status.isNotEmpty()) {
//                    Spacer(modifier = Modifier.height(16.dp))
//                    Text(
//                        text = status,
//                        color = Color.Red,
//                        fontSize = 14.sp
//                    )
//                }
//            }
//        }
//    }
//}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectScreen(
    onCardConnected: () -> Unit,
    smartCardManager: SmartCardManager = remember { SmartCardManager() }
) {
    var isConnecting by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    // Background siêu vui nhộn kiểu công viên
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
        // Những bong bóng trang trí nhẹ (tùy chọn bật/tắt)
        FloatingBubbles()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo + hiệu ứng vòng sáng vui nhộn
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFF6B6B).copy(alpha = 0.4f),
                                Color(0xFFFF8C42).copy(alpha = 0.2f),
                                Color.Transparent
                            )
                        )
                    )
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Ticket",
                    fontSize = 72.sp,
                    modifier = Modifier.offset(y = (-4).dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Chào mừng đến với",
                fontSize = 20.sp,
                color = Color(0xFF424242),
                fontWeight = FontWeight.Medium
            )

            Text(
                text = "PARK ADVENTURE",
                fontSize = 42.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFFFF5252),
                letterSpacing = 3.sp,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Đưa thẻ vui chơi của bạn vào vùng đọc nhé!",
                fontSize = 17.sp,
                color = Color(0xFF555555),
                textAlign = TextAlign.Center,
                lineHeight = 26.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Nút kết nối siêu to, gradient, có hiệu ứng lấp lánh
            Button(
                onClick = {
                    scope.launch {
                        isConnecting = true
                        status = "Đang tìm thẻ thật nhanh đây..."
                        val connected = smartCardManager.connectToCard()
                        if (connected) {
                            onCardConnected()
                        } else {
                            status = "Không thấy thẻ đâu "
                        }
                        isConnecting = false
                    }
                },
                enabled = !isConnecting,
                modifier = Modifier
                    .height(70.dp)
                    .fillMaxWidth(0.85f)
                    .shadow(12.dp, RoundedCornerShape(35.dp)),
                shape = RoundedCornerShape(35.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                contentPadding = PaddingValues()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFFFF6B6B),
                                    Color(0xFFFF8C42),
                                    Color(0xFFFF5252)
                                )
                            ),
                            shape = RoundedCornerShape(35.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isConnecting) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 5.dp,
                            modifier = Modifier.size(36.dp)
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Bắt đầu vui chơi nào!",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Sparkles", fontSize = 32.sp)
                        }
                    }
                }
            }

            // Thông báo trạng thái (nếu có lỗi)
            if (status.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF0F0)),
                    border = BorderStroke(2.dp, Color(0xFFFF5252)),
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = status,
                        modifier = Modifier.padding(16.dp),
                        color = Color(0xFFD32F2F),
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

// Bonus: hiệu ứng bong bóng bay nhẹ (rất hợp công viên)
@Composable
fun FloatingBubbles() {
    val infiniteTransition = rememberInfiniteTransition()
    val yOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val colors = listOf(
            Color(0xFFFF6B6B).copy(alpha = 0.3f),
            Color(0xFFFFD93D).copy(alpha = 0.3f),
            Color(0xFF4FC3F7).copy(alpha = 0.3f),
            Color(0xFF66BB6A).copy(alpha = 0.3f)
        )
        for (i in 0..8) {
            val x = (i * 150f) % size.width
            drawCircle(
                color = colors[i % colors.size],
                radius = 40f + (i * 10f),
                center = Offset(x + sin(yOffset / 100f + i) * 80f, size.height + yOffset + i * 200f)
            )
        }
    }
}