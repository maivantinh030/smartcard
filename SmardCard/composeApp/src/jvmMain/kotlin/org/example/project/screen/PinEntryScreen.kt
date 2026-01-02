package org.example.project.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.example.project.SmartCardManager
import kotlin.math.roundToInt
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

import kotlin.compareTo
import kotlin.dec


//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun PinEntryScreen(
//    smartCardManager: SmartCardManager,
//    onPinVerified: () -> Unit
//) {
//    var pin by remember { mutableStateOf("") }
//    var isVerifying by remember { mutableStateOf(false) }
//    var errorMessage by remember { mutableStateOf("") }
//    var remainingTries by remember { mutableStateOf(3) }
//
//    val scope = rememberCoroutineScope()
//
//    // Get PIN status when screen loads
//    LaunchedEffect(Unit) {
//        try {
//            val (tries, _, _) = smartCardManager.getPINStatus()
//            if (tries >= 0) {
//                remainingTries = tries
//            }
//        } catch (e: Exception) {
//            remainingTries = 3
//        }
//    }
//
//    // Background giống ConnectScreen
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
//                .padding(32.dp),
//            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.Center
//        ) {
//            // Modern PIN icon
//            Card(
//                modifier = Modifier.size(120.dp),
//                shape = CircleShape,
//                colors = CardDefaults.cardColors(containerColor = Color.White),
//                elevation = CardDefaults.cardElevation(12.dp)
//            ) {
//                Box(
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .background(
//                            brush = Brush.radialGradient(
//                                colors = listOf(
//                                    Color(0xFF5C6BC0),
//                                    Color(0xFF3F51B5)
//                                )
//                            )
//                        ),
//                    contentAlignment = Alignment.Center
//                ) {
//                    Text("🔐", fontSize = 48.sp)
//                }
//            }
//
//            Spacer(modifier = Modifier.height(32.dp))
//
//            Text(
//                text = "Xác thực PIN",
//                fontSize = 32.sp,
//                fontWeight = FontWeight.Bold,
//                color = Color(0xFF5C6BC0)
//            )
//
//            Spacer(modifier = Modifier.height(8.dp))
//
//            Text(
//                text = "Nhập mã PIN để truy cập hệ thống",
//                fontSize = 16.sp,
//                color = Color(0xFF666666),
//                textAlign = TextAlign.Center
//            )
//
//            Spacer(modifier = Modifier.height(48.dp))
//
//            Card(
//                modifier = Modifier.fillMaxWidth(0.8f),
//                shape = RoundedCornerShape(24.dp),
//                colors = CardDefaults.cardColors(containerColor = Color.White),
//                elevation = CardDefaults.cardElevation(12.dp)
//            ) {
//                Column(
//                    modifier = Modifier.padding(32.dp),
//                    horizontalAlignment = Alignment.CenterHorizontally
//                ) {
//                    // PIN Status indicator
//                    Card(
//                        shape = RoundedCornerShape(20.dp),
//                        colors = CardDefaults.cardColors(
//                            containerColor = when {
//                                remainingTries <= 0 -> Color(0xFFFFEBEE)
//                                remainingTries <= 1 -> Color(0xFFFFF8E1)
//                                else -> Color(0xFFE8F5E8)
//                            }
//                        ),
//                        border = BorderStroke(
//                            1.dp,
//                            when {
//                                remainingTries <= 0 -> Color(0xFFE57373)
//                                remainingTries <= 1 -> Color(0xFFFFB74D)
//                                else -> Color(0xFF81C784)
//                            }
//                        )
//                    ) {
//                        Row(
//                            modifier = Modifier.padding(16.dp),
//                            verticalAlignment = Alignment.CenterVertically
//                        ) {
//                            Text(
//                                text = when {
//                                    remainingTries <= 0 -> "🚫"
//                                    remainingTries <= 1 -> "⚠️"
//                                    else -> "✅"
//                                },
//                                fontSize = 20.sp
//                            )
//
//                            Spacer(modifier = Modifier.width(8.dp))
//
//                            Text(
//                                text = when {
//                                    remainingTries <= 0 -> "PIN đã bị khóa"
//                                    remainingTries == 1 -> "Còn 1 lần thử cuối!"
//                                    else -> "Còn $remainingTries lần thử"
//                                },
//                                fontSize = 14.sp,
//                                fontWeight = FontWeight.Medium,
//                                color = when {
//                                    remainingTries <= 0 -> Color(0xFFE57373)
//                                    remainingTries <= 1 -> Color(0xFFFFB74D)
//                                    else -> Color(0xFF81C784)
//                                }
//                            )
//                        }
//                    }
//
//                    Spacer(modifier = Modifier.height(24.dp))
//
//                    if (remainingTries <= 0) {
//                        // Blocked state
//                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                            Text("🚫", fontSize = 64.sp)
//
//                            Spacer(modifier = Modifier.height(16.dp))
//
//                            Text(
//                                text = "PIN đã bị khóa",
//                                fontSize = 18.sp,
//                                fontWeight = FontWeight.Bold,
//                                color = Color(0xFFE57373)
//                            )
//
//                            Text(
//                                text = "Vui lòng liên hệ quản trị viên",
//                                fontSize = 14.sp,
//                                color = Color(0xFF666666)
//                            )
//                        }
//                    } else {
//                        // PIN input
//                        OutlinedTextField(
//                            value = pin,
//                            onValueChange = {
//                                if (it.length <= 8) {
//                                    pin = it
//                                    errorMessage = ""
//                                }
//                            },
//                            label = { Text("Mã PIN") },
//                            placeholder = { Text("Nhập 4-8 ký tự") },
//                            modifier = Modifier.fillMaxWidth(),
//                            visualTransformation = PasswordVisualTransformation(),
//                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
//                            singleLine = true,
//                            isError = errorMessage.isNotEmpty(),
//                            enabled = !isVerifying,
//                            shape = RoundedCornerShape(16.dp),
//                            colors = OutlinedTextFieldDefaults.colors(
//                                focusedBorderColor = Color(0xFF5C6BC0),
//                                unfocusedBorderColor = Color(0xFFE0E0E0),
//                                errorBorderColor = Color(0xFFE57373)
//                            ),
//                            leadingIcon = {
//                                Text("🔑", fontSize = 20.sp)
//                            }
//                        )
//
//                        if (errorMessage.isNotEmpty()) {
//                            Spacer(modifier = Modifier.height(8.dp))
//                            Text(
//                                text = errorMessage,
//                                color = Color(0xFFE57373),
//                                fontSize = 12.sp,
//                                textAlign = TextAlign.Center
//                            )
//                        }
//
//                        Spacer(modifier = Modifier.height(24.dp))
//
//                        if (isVerifying) {
//                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                                CircularProgressIndicator(
//                                    modifier = Modifier.size(40.dp),
//                                    color = Color(0xFF5C6BC0),
//                                    strokeWidth = 3.dp
//                                )
//                                Spacer(modifier = Modifier.height(16.dp))
//                                Text(
//                                    text = "Đang xác thực...",
//                                    fontSize = 14.sp,
//                                    color = Color(0xFF666666)
//                                )
//                            }
//                        } else {
//                            Button(
//                                onClick = {
//                                    if (pin.length >= 4) {
//                                        scope.launch {
//                                            isVerifying = true
//                                            errorMessage = ""
//
//                                            val success = smartCardManager.verifyPIN(pin)
//                                            if (success) {
//                                                onPinVerified()
//                                            } else {
//                                                remainingTries--
//                                                errorMessage = when {
//                                                    remainingTries <= 0 -> "PIN đã bị khóa!"
//                                                    remainingTries == 1 -> "Sai PIN! Còn 1 lần cuối!"
//                                                    else -> "Sai PIN! Còn $remainingTries lần thử."
//                                                }
//                                                pin = ""
//                                            }
//                                            isVerifying = false
//                                        }
//                                    }
//                                },
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                                    .height(56.dp),
//                                enabled = pin.length >= 4,
//                                shape = RoundedCornerShape(16.dp),
//                                colors = ButtonDefaults.buttonColors(
//                                    containerColor = Color(0xFF5C6BC0),
//                                    disabledContainerColor = Color(0xFFE0E0E0)
//                                )
//                            ) {
//                                Row(verticalAlignment = Alignment.CenterVertically) {
//                                    Text("🔓", fontSize = 18.sp)
//                                    Spacer(modifier = Modifier.width(8.dp))
//                                    Text(
//                                        text = "Xác thực PIN",
//                                        fontSize = 16.sp,
//                                        fontWeight = FontWeight.Medium,
//                                        color = Color.White
//                                    )
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//
//            Spacer(modifier = Modifier.height(24.dp))
//
//            // Help text
//            Card(
//                modifier = Modifier.fillMaxWidth(0.8f),
//                shape = RoundedCornerShape(16.dp),
//                colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5))
//            ) {
//                Row(
//                    modifier = Modifier.padding(16.dp),
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Text("💡", fontSize = 20.sp)
//                    Spacer(modifier = Modifier.width(8.dp))
//                    Text(
//                        text = "PIN mặc định là: 1234",
//                        fontSize = 14.sp,
//                        color = Color(0xFF7B1FA2),
//                        fontWeight = FontWeight.Medium
//                    )
//                }
//            }
//        }
//    }
//}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinEntryScreen(
    smartCardManager: SmartCardManager,
    onPinVerified: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var isVerifying by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var remainingTries by remember { mutableStateOf(3) }

    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()  // ✅ THÊM

    // ✅ GIỮ NGUYÊN LOGIC
    LaunchedEffect(Unit) {
        try {
            val (tries, _, _) = smartCardManager.getPINStatus()
            if (tries >= 0) {
                remainingTries = tries
            }
        } catch (e: Exception) {
            remainingTries = 3
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFE5EC),  // ✅ GIỐNG
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
                .verticalScroll(scrollState)  // ✅ THÊM scroll
                .padding(horizontal = 80.dp, vertical = 20.dp),  // ✅ GIỐNG
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // ✅ LOGO CARD
            Card(
                modifier = Modifier
                    .size(140.dp)  // ✅ GIỐNG
                    .shadow(12.dp, CircleShape),
                shape = CircleShape,
                colors = CardDefaults.cardColors(containerColor = Color. Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFFF6B9D),  // ✅ GIỐNG
                                    Color(0xFFC06FBB),
                                    Color(0xFFFEC163)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🔐", fontSize = 64.sp)
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ✅ TITLE
            Text(
                text = "Xác thực PIN",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFFFF6B00)  // ✅ GIỐNG
            )

            Spacer(modifier = Modifier. height(8.dp))

            Text(
                text = "Nhập mã PIN để truy cập hệ thống",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF666666),
                textAlign = TextAlign. Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // ✅ MAIN CARD
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
                    // ✅ TITLE TRONG CARD
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🔑", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Nhập mã PIN",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFFF6B00)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // ✅ PIN STATUS
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
                                    remainingTries <= 0 -> "PIN đã bị khóa"
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

                    Spacer(modifier = Modifier. height(24.dp))

                    if (remainingTries <= 0) {
                        // Blocked state
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🚫", fontSize = 64.sp)

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "PIN đã bị khóa",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE57373)
                            )

                            Text(
                                text = "Vui lòng liên hệ quản trị viên",
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
                            label = { Text("Mã PIN") },
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

                                            val success = smartCardManager.verifyPIN(pin)
                                            if (success) {
                                                onPinVerified()
                                            } else {
                                                remainingTries--
                                                errorMessage = when {
                                                    remainingTries <= 0 -> "PIN đã bị khóa!"
                                                    remainingTries == 1 -> "Sai PIN! Còn 1 lần cuối!"
                                                    else -> "Sai PIN! Còn $remainingTries lần thử."
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
                                        text = "Xác thực PIN",
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

            // ✅ HELP CARD
            Card(
                modifier = Modifier
                    . widthIn(max = 600.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5)),
                elevation = CardDefaults. cardElevation(4.dp)
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
                        text = "PIN mặc định là:  1234",
                        fontSize = 14.sp,
                        color = Color(0xFF7B1FA2),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}