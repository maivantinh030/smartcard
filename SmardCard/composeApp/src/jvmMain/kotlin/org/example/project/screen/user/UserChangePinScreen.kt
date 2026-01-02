package org.example.project.screen.user

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.example.project.SmartCardManager
import org.example.project.screen.FloatingBubbles

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserChangePinScreen(
    smartCardManager:  SmartCardManager,
    onBack: () -> Unit
) {
    var currentPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var isChanging by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

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
                        text = "🔐 Đổi Mã PIN",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color. White
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Form Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color. White),
                elevation = CardDefaults. cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(
                        text = "Thay đổi mã PIN",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Current PIN
                    OutlinedTextField(
                        value = currentPin,
                        onValueChange = {
                            if (it.all { char -> char.isDigit() } && it.length <= 8) {
                                currentPin = it
                                status = ""
                            }
                        },
                        label = { Text("Mã PIN hiện tại") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFFB74D),
                            focusedLabelColor = Color(0xFFFFB74D)
                        ),
                        singleLine = true,
                        supportingText = {
                            Text("Nhập mã PIN hiện tại của bạn", fontSize = 12.sp, color = Color. Gray)
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // New PIN
                    OutlinedTextField(
                        value = newPin,
                        onValueChange = {
                            if (it. all { char -> char.isDigit() } && it.length <= 8) {
                                newPin = it
                                status = ""
                            }
                        },
                        label = { Text("Mã PIN mới") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFFB74D),
                            focusedLabelColor = Color(0xFFFFB74D)
                        ),
                        singleLine = true,
                        isError = newPin.isNotEmpty() && newPin == currentPin,
                        supportingText = {
                            if (newPin.isNotEmpty() && newPin == currentPin) {
                                Text("⚠️ PIN mới phải khác PIN cũ", fontSize = 12.sp, color = Color(0xFFE53935))
                            } else {
                                Text("4-8 ký tự số", fontSize = 12.sp, color = Color. Gray)
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Confirm PIN
                    OutlinedTextField(
                        value = confirmPin,
                        onValueChange = {
                            if (it.all { char -> char.isDigit() } && it.length <= 8) {
                                confirmPin = it
                                status = ""
                            }
                        },
                        label = { Text("Xác nhận PIN mới") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults. colors(
                            focusedBorderColor = Color(0xFFFFB74D),
                            focusedLabelColor = Color(0xFFFFB74D)
                        ),
                        singleLine = true,
                        isError = confirmPin. isNotEmpty() && confirmPin != newPin,
                        supportingText = {
                            if (confirmPin.isNotEmpty() && confirmPin != newPin) {
                                Text("⚠️ PIN không khớp", fontSize = 12.sp, color = Color(0xFFE53935))
                            } else if (confirmPin.isNotEmpty() && confirmPin == newPin) {
                                Text("✅ PIN khớp", fontSize = 12.sp, color = Color(0xFF4CAF50))
                            } else {
                                Text("Nhập lại PIN mới", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Change PIN Button
                    Button(
                        onClick = {
                            scope.launch {
                                // ✅ KIỂM TRA 1: PIN mới không được trùng PIN cũ
                                if (newPin == currentPin) {
                                    status = "❌ Mã PIN mới phải khác mã PIN cũ!"
                                    return@launch
                                }

                                // ✅ KIỂM TRA 2: PIN mới và xác nhận phải khớp
                                if (newPin != confirmPin) {
                                    status = "❌ Mã PIN mới không khớp!"
                                    return@launch
                                }

                                // ✅ KIỂM TRA 3: Độ dài PIN
                                if (newPin.length < 4 || newPin.length > 8) {
                                    status = "❌ PIN phải từ 4-8 ký tự!"
                                    return@launch
                                }

                                // ✅ KIỂM TRA 4: PIN chỉ chứa số
                                if (!newPin.all { it.isDigit() }) {
                                    status = "❌ PIN chỉ được chứa số!"
                                    return@launch
                                }

                                isChanging = true
                                try {
                                    val success = smartCardManager.changePIN(currentPin, newPin)

                                    if (success) {
                                        status = "✅ Đổi PIN thành công!  Vui lòng nhớ PIN mới:  $newPin"
                                        currentPin = ""
                                        newPin = ""
                                        confirmPin = ""
                                    } else {
                                        status = "❌ Đổi PIN thất bại!  Kiểm tra lại PIN cũ."
                                    }
                                } catch (e: Exception) {
                                    status = "❌ Lỗi: ${e.message}"
                                } finally {
                                    isChanging = false
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isChanging &&
                                currentPin.isNotEmpty() &&
                                newPin.isNotEmpty() &&
                                confirmPin.isNotEmpty() &&
                                newPin == confirmPin &&
                                newPin != currentPin &&
                                newPin.length >= 4,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFB74D),
                            disabledContainerColor = Color(0xFFE0E0E0)
                        )
                    ) {
                        if (isChanging) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color. White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = if (isChanging) "Đang xử lý..." else "🔐 Đổi mã PIN",
                            fontSize = 16.sp
                        )
                    }

                    // Validation hints
                    if (currentPin.isNotEmpty() || newPin.isNotEmpty() || confirmPin.isNotEmpty()) {
                        Spacer(modifier = Modifier. height(16.dp))
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFF3E5F5)
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "📋 Yêu cầu:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF7B1FA2)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                ValidationItem(
                                    text = "PIN mới khác PIN cũ",
                                    isValid = newPin. isEmpty() || newPin != currentPin
                                )
                                ValidationItem(
                                    text = "PIN có 4-8 ký tự số",
                                    isValid = newPin.isEmpty() || (newPin.length >= 4 && newPin.length <= 8 && newPin.all { it. isDigit() })
                                )
                                ValidationItem(
                                    text = "Xác nhận PIN khớp",
                                    isValid = confirmPin.isEmpty() || confirmPin == newPin
                                )
                            }
                        }
                    }
                }
            }

            // Status message
            if (status.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
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

@Composable
fun ValidationItem(text: String, isValid: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Text(
            text = if (isValid) "✓" else "○",
            fontSize = 14.sp,
            color = if (isValid) Color(0xFF4CAF50) else Color.Gray
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            color = if (isValid) Color(0xFF4CAF50) else Color.Gray
        )
    }
}

