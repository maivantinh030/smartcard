package org.example.project.screen.admin

import androidx.compose.foundation.Image
import androidx.compose.foundation. background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose. foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx. compose.runtime.*
import androidx. compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui. draw.clip
import androidx.compose. ui.graphics. Brush
import androidx.compose.ui.graphics.Color
import androidx. compose.ui.graphics.toComposeImageBitmap
import androidx.compose. ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose. ui.unit.sp
import kotlinx.coroutines.launch
import org.example.project.Customer
import org.example.project. SmartCardManager
import org.example.project.screen.FloatingBubbles
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminViewCustomerScreen(
    smartCardManager: SmartCardManager,
    onBack: () -> Unit
) {
    var customer by remember { mutableStateOf<Customer?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    fun loadCustomerData() {
        scope.launch {
            isLoading = true
            status = "Đang đọc dữ liệu từ thẻ..."

            try {
                val customerData = smartCardManager.readCustomerDataComplete()
                if (customerData != null) {
                    customer = customerData
                    status = "✅ Đọc dữ liệu thành công!"
                } else {
                    status = "❌ Không có dữ liệu trên thẻ!"
                }
            } catch (e:  Exception) {
                status = "❌ Lỗi đọc dữ liệu: ${e.message}"
            }

            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        loadCustomerData()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFAFAFA),
                        Color(0xFFF5F5F5),
                        Color(0xFFE8EAF6)
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
                colors = CardDefaults.cardColors(containerColor = Color(0xFF64B5F6)),
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
                            containerColor = Color.White.copy(alpha = 0.2f),
                            contentColor = Color. White
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.size(48.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("←", fontSize = 20.sp)
                    }

                    Spacer(modifier = Modifier. width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "👁️ Xem Thông Tin Khách Hàng",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color. White
                        )
                    }

                    Button(
                        onClick = { loadCustomerData() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color. White.copy(alpha = 0.2f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(12.dp)
                    ) {
                        Text("🔄", fontSize = 16.sp)
                    }
                }
            }

            Spacer(modifier = Modifier. height(16.dp))

            // Status message
            if (status.isNotEmpty()) {
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
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Content
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF64B5F6))
                }
            } else if (customer != null) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        . verticalScroll(rememberScrollState())
                ) {
                    // Photo Card
                    Card(
                        modifier = Modifier. fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color. White),
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            customer!! .anhKH?.let { photoBytes ->
                                remember(photoBytes) {
                                    try {
                                        val inputStream = ByteArrayInputStream(photoBytes)
                                        ImageIO.read(inputStream)?.toComposeImageBitmap()
                                    } catch (e: Exception) {
                                        null
                                    }
                                }
                            }?. let { bitmap ->
                                Image(
                                    bitmap = bitmap,
                                    contentDescription = "Customer Photo",
                                    modifier = Modifier
                                        .size(200.dp)
                                        . clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } ?: Box(
                                modifier = Modifier
                                    .size(200.dp)
                                    . clip(CircleShape)
                                    .background(Color(0xFFE0E0E0)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("📷", fontSize = 80.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier. height(16.dp))

                    // Info Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color. White),
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        ) {
                            Text(
                                text = "📋 Chi tiết thông tin",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF333333)
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            AdminInfoRow(
                                icon = "🆔",
                                label = "Mã khách hàng",
                                value = customer!! .maKH
                            )

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 12.dp),
                                color = Color(0xFFE0E0E0)
                            )

                            AdminInfoRow(
                                icon = "👤",
                                label = "Họ tên",
                                value = customer!!.hoTen
                            )

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 12.dp),
                                color = Color(0xFFE0E0E0)
                            )

                            AdminInfoRow(
                                icon = "🎂",
                                label = "Ngày sinh",
                                value = customer!!.ngaySinh
                            )

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 12.dp),
                                color = Color(0xFFE0E0E0)
                            )

                            AdminInfoRow(
                                icon = "📞",
                                label = "Số điện thoại",
                                value = customer!!.soDienThoai
                            )

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 12.dp),
                                color = Color(0xFFE0E0E0)
                            )

                            AdminInfoRow(
                                icon = "💳",
                                label = "Loại thẻ",
                                value = customer!!.loaiThe
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("📭", fontSize = 80.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Chưa có dữ liệu",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color. Gray
                        )
                        Text(
                            text = "Sử dụng chức năng Ghi thông tin để thêm khách hàng",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdminInfoRow(
    icon:  String,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier. fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = icon,
            fontSize = 24.sp,
            modifier = Modifier.width(40.dp)
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color. Gray
            )
            Spacer(modifier = Modifier. height(4.dp))
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF333333)
            )
        }
    }
}