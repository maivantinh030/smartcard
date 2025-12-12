//package org.example.project.screen
//
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.PaddingValues
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.layout.width
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.foundation.text.KeyboardOptions
//import androidx.compose.material3.Button
//import androidx.compose.material3.ButtonDefaults
//import androidx.compose.material3.Card
//import androidx.compose.material3.CardDefaults
//import androidx.compose.material3.CircularProgressIndicator
//import androidx.compose.material3.ExperimentalMaterial3Api
//import androidx.compose.material3.OutlinedButton
//import androidx.compose.material3.OutlinedTextField
//import androidx.compose.material3.OutlinedTextFieldDefaults
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.rememberCoroutineScope
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Brush
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.input.KeyboardType
//import androidx.compose.ui.text.input.PasswordVisualTransformation
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import kotlinx.coroutines.launch
//import org.example.project.SmartCardManager
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun ChangePinScreen(
//    smartCardManager: SmartCardManager,
//    onBack: () -> Unit
//) {
//    var currentPin by remember { mutableStateOf("") }
//    var newPin by remember { mutableStateOf("") }
//    var confirmPin by remember { mutableStateOf("") }
//    var isChanging by remember { mutableStateOf(false) }
//    var status by remember { mutableStateOf("") }
//    var pinStatus by remember { mutableStateOf("") }
//
//    val scope = rememberCoroutineScope()
//
//    // Get PIN status when screen loads
//    LaunchedEffect(Unit) {
//        try {
//            val (tries, created, validated) = smartCardManager.getPINStatus()
//            pinStatus = "Trạng thái: ${tries} lần thử còn lại"
//        } catch (e: Exception) {
//            pinStatus = "Không thể kiểm tra trạng thái PIN"
//        }
//    }
//
//    // Background giống các màn hình khác
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(
//                brush = Brush.verticalGradient(
//                    colors = listOf(
//                        Color(0xFFFAFAFA),
//                        Color(0xFFF5F5F5),
//                        Color(0xFFE8EAF6)
//                    )
//                )
//            )
//    ) {
//        FloatingBubbles()
//
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(16.dp)
//        ) {
//            // Header
//            Card(
//                modifier = Modifier.fillMaxWidth(),
//                shape = RoundedCornerShape(24.dp),
//                colors = CardDefaults.cardColors(containerColor = Color(0xFF5C6BC0)),
//                elevation = CardDefaults.cardElevation(8.dp)
//            ) {
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(20.dp),
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Button(
//                        onClick = onBack,
//                        colors = ButtonDefaults.buttonColors(
//                            containerColor = Color.White.copy(alpha = 0.2f),
//                            contentColor = Color.White
//                        ),
//                        shape = RoundedCornerShape(16.dp),
//                        modifier = Modifier.size(48.dp),
//                        contentPadding = PaddingValues(0.dp)
//                    ) {
//                        Text("←", fontSize = 20.sp)
//                    }
//
//                    Spacer(modifier = Modifier.width(16.dp))
//
//                    Column {
//                        Row(verticalAlignment = Alignment.CenterVertically) {
//                            Box(
//                                modifier = Modifier
//                                    .size(40.dp)
//                                    .background(
//                                        Color.White.copy(alpha = 0.2f),
//                                        CircleShape
//                                    ),
//                                contentAlignment = Alignment.Center
//                            ) {
//                                Text("🔄", fontSize = 20.sp)
//                            }
//
//                            Spacer(modifier = Modifier.width(12.dp))
//
//                            Column {
//                                Text(
//                                    text = "Đổi mật khẩu PIN",
//                                    fontSize = 18.sp,
//                                    fontWeight = FontWeight.Bold,
//                                    color = Color.White
//                                )
//                                Text(
//                                    text = "Thay đổi mã PIN bảo mật",
//                                    fontSize = 12.sp,
//                                    color = Color.White.copy(0.9f)
//                                )
//                            }
//                        }
//                    }
//                }
//            }
//
//            Spacer(modifier = Modifier.height(24.dp))
//
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.spacedBy(16.dp)
//            ) {
//                // Left Column - Change PIN Form
//                Card(
//                    modifier = Modifier.weight(2f),
//                    shape = RoundedCornerShape(20.dp),
//                    colors = CardDefaults.cardColors(containerColor = Color.White),
//                    elevation = CardDefaults.cardElevation(6.dp)
//                ) {
//                    Column(modifier = Modifier.padding(24.dp)) {
//                        Text(
//                            text = "🔐 Thay đổi mã PIN",
//                            fontSize = 16.sp,
//                            fontWeight = FontWeight.Bold,
//                            color = Color(0xFF333333)
//                        )
//
//                        Spacer(modifier = Modifier.height(20.dp))
//
//                        // Current PIN
//                        OutlinedTextField(
//                            value = currentPin,
//                            onValueChange = {
//                                if (it.length <= 8) {
//                                    currentPin = it
//                                    status = ""
//                                }
//                            },
//                            label = { Text("PIN hiện tại") },
//                            placeholder = { Text("Nhập PIN hiện tại") },
//                            modifier = Modifier.fillMaxWidth(),
//                            visualTransformation = PasswordVisualTransformation(),
//                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
//                            singleLine = true,
//                            shape = RoundedCornerShape(12.dp),
//                            colors = OutlinedTextFieldDefaults.colors(
//                                focusedBorderColor = Color(0xFFE57373), // Đỏ nhẹ
//                                unfocusedBorderColor = Color(0xFFE0E0E0)
//                            ),
//                            leadingIcon = {
//                                Text("🔒", fontSize = 18.sp)
//                            }
//                        )
//
//                        Spacer(modifier = Modifier.height(16.dp))
//
//                        // New PIN
//                        OutlinedTextField(
//                            value = newPin,
//                            onValueChange = {
//                                if (it.length <= 8) {
//                                    newPin = it
//                                    status = ""
//                                }
//                            },
//                            label = { Text("PIN mới") },
//                            placeholder = { Text("Nhập PIN mới (4-8 ký tự)") },
//                            modifier = Modifier.fillMaxWidth(),
//                            visualTransformation = PasswordVisualTransformation(),
//                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
//                            singleLine = true,
//                            shape = RoundedCornerShape(12.dp),
//                            colors = OutlinedTextFieldDefaults.colors(
//                                focusedBorderColor = Color(0xFF81C784), // Xanh lá
//                                unfocusedBorderColor = Color(0xFFE0E0E0)
//                            ),
//                            leadingIcon = {
//                                Text("🔓", fontSize = 18.sp)
//                            }
//                        )
//
//                        Spacer(modifier = Modifier.height(16.dp))
//
//                        // Confirm PIN
//                        OutlinedTextField(
//                            value = confirmPin,
//                            onValueChange = {
//                                if (it.length <= 8) {
//                                    confirmPin = it
//                                    status = ""
//                                }
//                            },
//                            label = { Text("Xác nhận PIN mới") },
//                            placeholder = { Text("Nhập lại PIN mới") },
//                            modifier = Modifier.fillMaxWidth(),
//                            visualTransformation = PasswordVisualTransformation(),
//                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
//                            singleLine = true,
//                            isError = confirmPin.isNotEmpty() && newPin != confirmPin,
//                            shape = RoundedCornerShape(12.dp),
//                            colors = OutlinedTextFieldDefaults.colors(
//                                focusedBorderColor = Color(0xFF64B5F6), // Xanh dương
//                                unfocusedBorderColor = Color(0xFFE0E0E0),
//                                errorBorderColor = Color(0xFFE57373)
//                            ),
//                            leadingIcon = {
//                                Text("🔐", fontSize = 18.sp)
//                            }
//                        )
//
//                        if (confirmPin.isNotEmpty() && newPin != confirmPin) {
//                            Spacer(modifier = Modifier.height(8.dp))
//                            Text(
//                                text = "PIN xác nhận không khớp",
//                                fontSize = 12.sp,
//                                color = Color(0xFFE57373)
//                            )
//                        }
//
//                        Spacer(modifier = Modifier.height(24.dp))
//
//                        // Change PIN Button
//                        Button(
//                            onClick = {
//                                if (newPin.length < 4) {
//                                    status = "❌ PIN mới phải có ít nhất 4 ký tự"
//                                    return@Button
//                                }
//
//                                if (newPin != confirmPin) {
//                                    status = "❌ PIN xác nhận không khớp"
//                                    return@Button
//                                }
//
//                                scope.launch {
//                                    isChanging = true
//                                    status = "Đang thay đổi PIN..."
//
//                                    try {
//                                        val success = smartCardManager.changePIN(currentPin, newPin)
//                                        if (success) {
//                                            status = "✅ Đổi PIN thành công!"
//                                            currentPin = ""
//                                            newPin = ""
//                                            confirmPin = ""
//                                        } else {
//                                            status = "❌ Đổi PIN thất bại! Kiểm tra PIN hiện tại."
//                                        }
//                                    } catch (e: Exception) {
//                                        status = "❌ Lỗi: ${e.message}"
//                                    }
//
//                                    isChanging = false
//                                }
//                            },
//                            modifier = Modifier.fillMaxWidth(),
//                            enabled = !isChanging &&
//                                    currentPin.length >= 4 &&
//                                    newPin.length >= 4 &&
//                                    confirmPin == newPin,
//                            shape = RoundedCornerShape(12.dp),
//                            colors = ButtonDefaults.buttonColors(
//                                containerColor = Color(0xFF81C784) // Xanh lá
//                            )
//                        ) {
//                            if (isChanging) {
//                                CircularProgressIndicator(
//                                    modifier = Modifier.size(16.dp),
//                                    color = Color.White,
//                                    strokeWidth = 2.dp
//                                )
//                                Spacer(modifier = Modifier.width(8.dp))
//                                Text("Đang đổi...", fontSize = 14.sp, color = Color.White)
//                            } else {
//                                Text("🔄 Đổi mật khẩu", fontSize = 14.sp, color = Color.White)
//                            }
//                        }
//
//                        Spacer(modifier = Modifier.height(16.dp))
//
//                        // Clear button
//                        OutlinedButton(
//                            onClick = {
//                                currentPin = ""
//                                newPin = ""
//                                confirmPin = ""
//                                status = ""
//                            },
//                            modifier = Modifier.fillMaxWidth(),
//                            shape = RoundedCornerShape(12.dp),
//                            colors = ButtonDefaults.outlinedButtonColors(
//                                contentColor = Color(0xFF666666)
//                            )
//                        ) {
//                            Text("🗑️ Xóa tất cả", fontSize = 14.sp)
//                        }
//                    }
//                }
//
//                // Right Column - Info & Guidelines
//                Column(modifier = Modifier.weight(1f)) {
//                    // PIN Status
//                    Card(
//                        modifier = Modifier.fillMaxWidth(),
//                        shape = RoundedCornerShape(20.dp),
//                        colors = CardDefaults.cardColors(containerColor = Color.White),
//                        elevation = CardDefaults.cardElevation(6.dp)
//                    ) {
//                        Column(modifier = Modifier.padding(20.dp)) {
//                            Text(
//                                text = "📊 Trạng thái PIN",
//                                fontSize = 14.sp,
//                                fontWeight = FontWeight.Bold,
//                                color = Color(0xFF333333)
//                            )
//
//                            Spacer(modifier = Modifier.height(12.dp))
//
//                            Card(
//                                shape = RoundedCornerShape(8.dp),
//                                colors = CardDefaults.cardColors(
//                                    containerColor = Color(0xFFE3F2FD)
//                                )
//                            ) {
//                                Text(
//                                    text = pinStatus,
//                                    modifier = Modifier.padding(12.dp),
//                                    fontSize = 12.sp,
//                                    color = Color(0xFF1976D2)
//                                )
//                            }
//                        }
//                    }
//
//                    Spacer(modifier = Modifier.height(12.dp))
//
//                    // Guidelines
//                    Card(
//                        modifier = Modifier.fillMaxWidth(),
//                        shape = RoundedCornerShape(20.dp),
//                        colors = CardDefaults.cardColors(containerColor = Color.White),
//                        elevation = CardDefaults.cardElevation(6.dp)
//                    ) {
//                        Column(modifier = Modifier.padding(20.dp)) {
//                            Text(
//                                text = "💡 Hướng dẫn",
//                                fontSize = 14.sp,
//                                fontWeight = FontWeight.Bold,
//                                color = Color(0xFF333333)
//                            )
//
//                            Spacer(modifier = Modifier.height(12.dp))
//
//                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
//                                GuidelineItem(
//                                    icon = "🔒",
//                                    text = "PIN hiện tại để xác thực",
//                                    color = Color(0xFFE57373)
//                                )
//
//                                GuidelineItem(
//                                    icon = "🔑",
//                                    text = "PIN mới từ 4-8 ký tự",
//                                    color = Color(0xFF81C784)
//                                )
//
//                                GuidelineItem(
//                                    icon = "⚡",
//                                    text = "Chọn PIN dễ nhớ nhưng bảo mật",
//                                    color = Color(0xFF64B5F6)
//                                )
//
//                                GuidelineItem(
//                                    icon = "⚠️",
//                                    text = "Không chia sẻ PIN cho ai",
//                                    color = Color(0xFFFFB74D)
//                                )
//                            }
//                        }
//                    }
//                }
//            }
//
//            // Status display
//            if (status.isNotEmpty()) {
//                Spacer(modifier = Modifier.height(16.dp))
//                Card(
//                    modifier = Modifier.fillMaxWidth(),
//                    shape = RoundedCornerShape(12.dp),
//                    colors = CardDefaults.cardColors(
//                        containerColor = when {
//                            status.contains("✅") -> Color(0xFF81C784)
//                            status.contains("❌") -> Color(0xFFE57373)
//                            else -> Color(0xFF64B5F6)
//                        }
//                    )
//                ) {
//                    Text(
//                        text = status,
//                        modifier = Modifier.padding(16.dp),
//                        color = Color.White,
//                        fontSize = 14.sp,
//                        fontWeight = FontWeight.Medium
//                    )
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun GuidelineItem(
//    icon: String,
//    text: String,
//    color: Color
//) {
//    Row(
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        Box(
//            modifier = Modifier
//                .size(24.dp)
//                .background(color.copy(alpha = 0.1f), CircleShape),
//            contentAlignment = Alignment.Center
//        ) {
//            Text(icon, fontSize = 12.sp)
//        }
//
//        Spacer(modifier = Modifier.width(8.dp))
//
//        Text(
//            text = text,
//            fontSize = 12.sp,
//            color = Color(0xFF666666)
//        )
//    }
//}