package org.example.project.screen.user

import androidx.compose. foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose. foundation.shape.RoundedCornerShape
import androidx.compose. foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui. graphics.Brush
import androidx. compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx. compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.example.project.SmartCardManager
import org.example.project.screen.FloatingBubbles

@OptIn(ExperimentalMaterial3Api:: class)
@Composable
fun UserBalanceScreen(
    smartCardManager: SmartCardManager,
    onBack: () -> Unit
) {
    var balance by remember { mutableStateOf(0) }
    var paymentAmount by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    // Load balance khi vào màn hình
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
                colors = CardDefaults.cardColors(containerColor = Color(0xFF81C784)),
                elevation = CardDefaults.cardElevation(8.dp)
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
                        modifier = Modifier. size(48.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("←", fontSize = 20.sp)
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = "💰 Số Dư & Thanh Toán",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier. height(24.dp))

            // Balance Display Card
            Card(
                modifier = Modifier. fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color. White),
                elevation = CardDefaults.cardElevation(12.dp)
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
                            . background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFF81C784),
                                        Color(0xFFA5D6A7)
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
                            modifier = Modifier. size(40.dp),
                            color = Color(0xFF81C784)
                        )
                    } else {
                        Text(
                            text = "${balance} VNĐ",
                            fontSize = 40.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF81C784)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { loadBalance() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF81C784)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("🔄 Làm mới", fontSize = 14.sp)
                    }
                }
            }

            Spacer(modifier = Modifier. height(24.dp))

            // Payment Section
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
                        text = "💳 Thanh toán",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = paymentAmount,
                        onValueChange = { paymentAmount = it },
                        label = { Text("Số tiền thanh toán") },
                        placeholder = { Text("Nhập số tiền... ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF81C784),
                            focusedLabelColor = Color(0xFF81C784)
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            scope.launch {
                                try {
                                    val amount = paymentAmount.toIntOrNull()
                                    if (amount != null && amount > 0) {
                                        isLoading = true
                                        smartCardManager.makePayment(amount)
                                        loadBalance()
                                        paymentAmount = ""
                                        status = "✅ Thanh toán thành công $amount VNĐ"
                                    } else {
                                        status = "❌ Số tiền không hợp lệ"
                                    }
                                } catch (e: Exception) {
                                    status = "❌ Lỗi thanh toán: ${e.message}"
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = ! isLoading && paymentAmount.isNotEmpty(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF7043)
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color. White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = if (isLoading) "Đang xử lý..." else "💸 Thanh toán",
                            fontSize = 16.sp
                        )
                    }
                }
            }

            // Status message
            if (status.isNotEmpty()) {
                Spacer(modifier = Modifier. height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
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