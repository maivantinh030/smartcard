package org.example.project.screen.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.example.project.SmartCardManager
import org.example.project.screen.FloatingBubbles

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPinEntryScreen(
    smartCardManager: SmartCardManager,
    onPinVerified: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var isVerifying by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var remainingTries by remember { mutableStateOf(5) }

    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Get Admin PIN status when screen loads
    LaunchedEffect(Unit) {
        try {
            val (tries, _, _) = smartCardManager.getAdminPINStatus()
            if (tries >= 0) {
                remainingTries = tries
            }
        } catch (e: Exception) {
            remainingTries = 5
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFE5EC),
                        Color(0xFFFFF4E6),
                        Color(0xFFE8F5E9)
                    )
                )
            )
    ) {
        FloatingBubbles()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 80.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo Card
            Card(
                modifier = Modifier
                    .size(140.dp)
                    .shadow(12.dp, CircleShape),
                shape = CircleShape,
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFFF6B9D),
                                    Color(0xFFC06FBB),
                                    Color(0xFFFEC163)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🛡️", fontSize = 64.sp)
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Title
            Text(
                text = "Xác thực Admin PIN",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFFFF6B00)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Nhập mã Admin PIN để truy cập hệ thống",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF666666),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Main Card
            Card(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxWidth()
                    .shadow(12.dp, RoundedCornerShape(28.dp)),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Title in Card
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🔐", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Nhập Admin PIN",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFFF6B00)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // PIN Status
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = when {
                                remainingTries <= 0 -> Color(0xFFFFEBEE)
                                remainingTries <= 1 -> Color(0xFFFFF3E0)
                                else -> Color(0xFFE8F5E9)
                            }
                        ),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = when {
                                    remainingTries <= 0 -> "🚫"
                                    remainingTries <= 1 -> "⚠️"
                                    else -> "✅"
                                },
                                fontSize = 24.sp
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            Text(
                                text = when {
                                    remainingTries <= 0 -> "Admin PIN đã bị khóa"
                                    remainingTries == 1 -> "Còn 1 lần thử cuối!"
                                    else -> "Còn $remainingTries lần thử"
                                },
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    remainingTries <= 0 -> Color(0xFFE53935)
                                    remainingTries <= 1 -> Color(0xFFFFA726)
                                    else -> Color(0xFF4CAF50)
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    if (remainingTries <= 0) {
                        // Blocked state
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🚫", fontSize = 64.sp)

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Admin PIN đã bị khóa",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE57373)
                            )

                            Text(
                                text = "Vui lòng liên hệ quản trị viên cấp cao",
                                fontSize = 14.sp,
                                color = Color(0xFF666666)
                            )
                        }
                    } else {
                        // PIN input
                        OutlinedTextField(
                            value = pin,
                            onValueChange = {
                                if (it.length <= 8) {
                                    pin = it
                                    errorMessage = ""
                                }
                            },
                            label = { Text("Admin PIN") },
                            placeholder = { Text("Nhập 4-8 ký tự") },
                            modifier = Modifier.fillMaxWidth(),
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            isError = errorMessage.isNotEmpty(),
                            enabled = !isVerifying,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF5C6BC0),
                                unfocusedBorderColor = Color(0xFFE0E0E0),
                                errorBorderColor = Color(0xFFE57373)
                            ),
                            leadingIcon = {
                                Text("🔑", fontSize = 20.sp)
                            }
                        )

                        if (errorMessage.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = errorMessage,
                                color = Color(0xFFE57373),
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        if (isVerifying) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(40.dp),
                                    color = Color(0xFF5C6BC0),
                                    strokeWidth = 3.dp
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Đang xác thực...",
                                    fontSize = 14.sp,
                                    color = Color(0xFF666666)
                                )
                            }
                        } else {
                            Button(
                                onClick = {
                                    if (pin.length >= 4) {
                                        scope.launch {
                                            isVerifying = true
                                            errorMessage = ""

                                            val success = smartCardManager.verifyAdminPIN(pin)
                                            if (success) {
                                                onPinVerified()
                                            } else {
                                                // Refresh tries remaining
                                                val (tries, _, _) = smartCardManager.getAdminPINStatus()
                                                if (tries >= 0) {
                                                    remainingTries = tries
                                                } else {
                                                    remainingTries--
                                                }
                                                errorMessage = when {
                                                    remainingTries <= 0 -> "Admin PIN đã bị khóa!"
                                                    remainingTries == 1 -> "Sai Admin PIN! Còn 1 lần cuối!"
                                                    else -> "Sai Admin PIN! Còn $remainingTries lần thử."
                                                }
                                                pin = ""
                                            }
                                            isVerifying = false
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                enabled = pin.length >= 4,
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF5C6BC0),
                                    disabledContainerColor = Color(0xFFE0E0E0)
                                )
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🔓", fontSize = 18.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Xác thực Admin PIN",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Help Card
            Card(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5)),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("💡", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Admin PIN mặc định là: 9999",
                        fontSize = 14.sp,
                        color = Color(0xFF7B1FA2),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

