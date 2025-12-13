package org.example.project.screen.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminRechargeScreen(
    smartCardManager: SmartCardManager,
    onBack: () -> Unit
) {
    var balance by remember { mutableStateOf(0) }
    var rechargeAmount by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    fun loadBalance() {
        scope.launch {
            isLoading = true
            try {
                balance = smartCardManager.checkBalance()
                status = "✅ Đã tải số dư"
            } catch (e: Exception) {
                status = "❌ Lỗi:  ${e.message}"
            }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        loadBalance()
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
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFB74D)),
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
                            contentColor = Color. White
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.size(48.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("←", fontSize = 20.sp)
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = "💰 Nạp Tiền Vào Thẻ",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier. height(24.dp))

            // Balance Display
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color. White),
                elevation = CardDefaults. cardElevation(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFFFFB74D),
                                        Color(0xFFFFCC02)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("💵", fontSize = 48.sp)
                    }

                    Spacer(modifier = Modifier. height(20.dp))

                    Text(
                        text = "Số dư hiện tại",
                        fontSize = 16.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier. height(8.dp))

                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(40.dp),
                            color = Color(0xFFFFB74D)
                        )
                    } else {
                        Text(
                            text = "${balance} VNĐ",
                            fontSize = 40.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFFFB74D)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { loadBalance() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFB74D)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("🔄 Làm mới", fontSize = 14.sp)
                    }
                }
            }

            Spacer(modifier = Modifier. height(24.dp))

            // Recharge Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults. cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(
                        text = "➕ Nạp tiền",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = rechargeAmount,
                        onValueChange = { rechargeAmount = it },
                        label = { Text("Số tiền nạp") },
                        placeholder = { Text("Nhập số tiền... ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFFB74D),
                            focusedLabelColor = Color(0xFFFFB74D)
                        ),
                        singleLine = true,
                        supportingText = {
                            Text("Tối đa: 30,000 VNĐ", fontSize = 12.sp, color = Color.Gray)
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Quick Amount Buttons
                    Text(
                        text = "Chọn nhanh:",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(50000, 100000, 200000, 500000).forEach { amount ->
                            Button(
                                onClick = { rechargeAmount = amount.toString() },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFFF3E0)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "${amount/1000}k",
                                    fontSize = 12.sp,
                                    color = Color(0xFFFFB74D),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            scope.launch {
                                try {
                                    val amount = rechargeAmount.toIntOrNull()
                                    if (amount != null && amount > 0) {
                                        isLoading = true
                                        val success = smartCardManager.rechargeBalance(amount)
                                        if (success) {
                                            loadBalance()
                                            rechargeAmount = ""
                                            status = "✅ Nạp thành công $amount VNĐ"
                                        } else {
                                            status = "❌ Nạp tiền thất bại"
                                        }
                                    } else {
                                        status = "❌ Số tiền không hợp lệ"
                                    }
                                } catch (e: Exception) {
                                    status = "❌ Lỗi: ${e.message}"
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading && rechargeAmount.isNotEmpty(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier. size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = if (isLoading) "Đang xử lý..." else "💳 Nạp tiền",
                            fontSize = 16.sp
                        )
                    }
                }
            }

            // Status message
            if (status.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier. fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (status.startsWith("✅"))
                            Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                    )
                ) {
                    Text(
                        text = status,
                        modifier = Modifier.padding(16.dp),
                        color = if (status.startsWith("✅"))
                            Color(0xFF4CAF50) else Color(0xFFE53935),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}