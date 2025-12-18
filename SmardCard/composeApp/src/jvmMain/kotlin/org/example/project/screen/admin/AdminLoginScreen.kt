//package org.example.project.screen.admin
//
//import androidx.compose.foundation.layout.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.text.input.PasswordVisualTransformation
//import androidx.compose.ui.unit.dp
//import kotlinx.coroutines.launch
//import org.example.project.auth.AdminSession
//
//@Composable
//fun AdminLoginScreen(
//    session: AdminSession,
//    onLoggedIn: () -> Unit,
//    baseUrl: String,
//    onBaseUrlChange: (String) -> Unit,
//) {
//    var username by remember { mutableStateOf("") }
//    var password by remember { mutableStateOf("") }
//    var error by remember { mutableStateOf<String?>(null) }
//    var loading by remember { mutableStateOf(false) }
//
//    val scope = rememberCoroutineScope()
//
//    Box(Modifier.fillMaxSize()) {
//        Column(
//            modifier = Modifier.align(Alignment.Center).padding(24.dp).widthIn(max = 420.dp),
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            Text("Admin Login", style = MaterialTheme.typography.headlineMedium)
//            Spacer(Modifier.height(16.dp))
//
//            OutlinedTextField(
//                value = username,
//                onValueChange = { username = it },
//                label = { Text("Username") },
//                singleLine = true,
//                modifier = Modifier.fillMaxWidth()
//            )
//            Spacer(Modifier.height(8.dp))
//
//            OutlinedTextField(
//                value = password,
//                onValueChange = { password = it },
//                label = { Text("Password") },
//                singleLine = true,
//                visualTransformation = PasswordVisualTransformation(),
//                modifier = Modifier.fillMaxWidth()
//            )
//
//            Spacer(Modifier.height(12.dp))
//
//            // Server base URL
//            OutlinedTextField(
//                value = baseUrl,
//                onValueChange = onBaseUrlChange,
//                label = { Text("Server URL (http://host:port/api/v1)") },
//                singleLine = true,
//                modifier = Modifier.fillMaxWidth()
//            )
//
//            if (error != null) {
//                Spacer(Modifier.height(8.dp))
//                Text(error!!, color = MaterialTheme.colorScheme.error)
//            }
//
//            Spacer(Modifier.height(16.dp))
//
//            Button(
//                onClick = {
//                    error = null
//                    loading = true
//                    scope.launch {
//                        val res = session.login(username.trim(), password)
//                        loading = false
//                        res.onSuccess {
//                            onLoggedIn()
//                        }.onFailure { e ->
//                            error = e.message ?: "Đăng nhập thất bại"
//                        }
//                    }
//                },
//                enabled = !loading && username.isNotBlank() && password.isNotBlank(),
//                modifier = Modifier.fillMaxWidth()
//            ) {
//                Text(if (loading) "Đang đăng nhập..." else "Đăng nhập")
//            }
//        }
//    }
//}

package org.example.project.screen.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.example.project.auth.AdminSession
import org.example.project.screen.FloatingBubbles

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminLoginScreen(
    session: AdminSession,
    onLoggedIn: () -> Unit,
    baseUrl: String,
    onBaseUrlChange:  (String) -> Unit,
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            . fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFF3E0),
                        Color(0xFFFFE0F0),
                        Color(0xFFE0F7FA)
                    )
                )
            )
    ) {
        FloatingBubbles()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // LOGO CARD
            Card(
                modifier = Modifier
                    .size(120.dp)
                    .shadow(24.dp, CircleShape),
                shape = CircleShape,
                colors = CardDefaults.cardColors(containerColor = Color. Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFFBA68C8),
                                    Color(0xFFCE93D8),
                                    Color(0xFFE1BEE7)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("👨‍💼", fontSize = 56.sp)
                }
            }

            Spacer(modifier = Modifier. height(32.dp))

            // TITLE
            Text(
                text = "🔐 Admin Portal",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF333333)
            )

            Spacer(modifier = Modifier. height(8.dp))

            Text(
                text = "Đăng nhập để quản lý hệ thống",
                fontSize = 16.sp,
                color = Color. Gray
            )

            Spacer(modifier = Modifier.height(40.dp))

            // LOGIN FORM CARD
            Card(
                modifier = Modifier
                    .width(500.dp)
                    .shadow(20.dp, RoundedCornerShape(32.dp)),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp)
                ) {
                    // USERNAME
                    Text(
                        text = "👤 Tên đăng nhập",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF666666)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = username,
                        onValueChange = {
                            username = it
                            error = null
                        },
                        placeholder = { Text("Nhập tên đăng nhập") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Username",
                                tint = Color(0xFFBA68C8)
                            )
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFBA68C8),
                            focusedLabelColor = Color(0xFFBA68C8),
                            cursorColor = Color(0xFFBA68C8)
                        )
                    )

                    Spacer(modifier = Modifier. height(20.dp))

                    // PASSWORD
                    Text(
                        text = "🔒 Mật khẩu",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF666666)
                    )
                    Spacer(modifier = Modifier. height(8.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            error = null
                        },
                        placeholder = { Text("Nhập mật khẩu") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default. Lock,
                                contentDescription = "Password",
                                tint = Color(0xFFBA68C8)
                            )
                        },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFBA68C8),
                            focusedLabelColor = Color(0xFFBA68C8),
                            cursorColor = Color(0xFFBA68C8)
                        )
                    )

                    // ERROR MESSAGE
                    if (error != null) {
                        Spacer(modifier = Modifier. height(16.dp))
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFFEBEE)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("❌", fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = error!! ,
                                    color = Color(0xFFE53935),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // LOGIN BUTTON
                    Button(
                        onClick = {
                            error = null
                            loading = true
                            scope.launch {
                                val res = session. login(username.trim(), password)
                                loading = false
                                res.onSuccess {
                                    onLoggedIn()
                                }.onFailure { e ->
                                    error = e.message ?: "Đăng nhập thất bại"
                                }
                            }
                        },
                        enabled = !loading && username.isNotBlank() && password.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFBA68C8),
                            disabledContainerColor = Color(0xFFE0E0E0)
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 8.dp,
                            pressedElevation = 4.dp
                        )
                    ) {
                        if (loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 3.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Đang đăng nhập...",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Text(
                                "🚀 Đăng nhập",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier. height(24.dp))

            // FOOTER
            Text(
                text = "🎡 Smart Card Management System",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}