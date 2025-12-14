//package org.example.project.screen
//
//import androidx.compose.foundation.BorderStroke
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Brush
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.ImageBitmap
//import androidx.compose.ui.graphics.toComposeImageBitmap
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import kotlinx.coroutines.launch
//import org.example.project.Customer
//import org.example.project.SmartCardManager
//import javax.imageio.ImageIO
//import kotlin.let
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun CustomerViewScreen(
//    smartCardManager: SmartCardManager,
//    onBack: () -> Unit
//) {
//    var customer by remember { mutableStateOf<Customer?>(null) }
//    var isLoading by remember { mutableStateOf(false) }
//    var status by remember { mutableStateOf("") }
//
//    val scope = rememberCoroutineScope()
//
//    // Auto load data khi vào màn hình
//
//
//    fun loadCustomerData() {
//        scope.launch {
//            isLoading = true
//            status = "Đang đọc dữ liệu từ thẻ..."
//
//            try {
//                val customerData = smartCardManager.readCustomerDataComplete()
//                if (customerData != null) {
//                    customer = customerData
//                    status = "✅ Đọc dữ liệu thành công!"
//                } else {
//                    status = "❌ Không có dữ liệu trên thẻ!"
//                }
//            } catch (e: Exception) {
//                status = "❌ Lỗi đọc dữ liệu: ${e.message}"
//            }
//
//            isLoading = false
//        }
//    }
//    LaunchedEffect(Unit) {
//        loadCustomerData()
//    }
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
//                    ),
//                    startY = 0f,
//                    endY = 2000f
//                )
//            )
//    ) {
//        // Hiệu ứng bong bóng nhẹ
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
//                                Text("👁️", fontSize = 20.sp)
//                            }
//
//                            Spacer(modifier = Modifier.width(12.dp))
//
//                            Column {
//                                Text(
//                                    text = "Xem thông tin khách hàng",
//                                    fontSize = 18.sp,
//                                    fontWeight = FontWeight.Bold,
//                                    color = Color.White
//                                )
//                                Text(
//                                    text = "Thông tin được đọc từ thẻ",
//                                    fontSize = 12.sp,
//                                    color = Color.White.copy(0.9f)
//                                )
//                            }
//                        }
//                    }
//
//                    Spacer(modifier = Modifier.weight(1f))
//
//                    // Refresh button
//                    Button(
//                        onClick = {
//                            scope.launch { loadCustomerData() }
//                        },
//                        colors = ButtonDefaults.buttonColors(
//                            containerColor = Color.White.copy(alpha = 0.2f)
//                        ),
//                        shape = RoundedCornerShape(16.dp),
//                        modifier = Modifier.size(48.dp),
//                        contentPadding = PaddingValues(0.dp),
//                        enabled = !isLoading
//                    ) {
//                        if (isLoading) {
//                            CircularProgressIndicator(
//                                modifier = Modifier.size(20.dp),
//                                color = Color.White,
//                                strokeWidth = 2.dp
//                            )
//                        } else {
//                            Text("🔄", fontSize = 16.sp)
//                        }
//                    }
//                }
//            }
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            if (customer != null) {
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.spacedBy(16.dp)
//                ) {
//                    // Left Column - Customer Info
//                    Card(
//                        modifier = Modifier.weight(2f),
//                        shape = RoundedCornerShape(20.dp),
//                        colors = CardDefaults.cardColors(containerColor = Color.White),
//                        elevation = CardDefaults.cardElevation(6.dp)
//                    ) {
//                        Column(modifier = Modifier.padding(24.dp)) {
//                            Text(
//                                text = "👤 Thông tin cá nhân",
//                                fontSize = 16.sp,
//                                fontWeight = FontWeight.Bold,
//                                color = Color(0xFF333333)
//                            )
//
//                            Spacer(modifier = Modifier.height(20.dp))
//
//                            // Customer info fields
//                            InfoDisplayField(
//                                label = "Mã khách hàng",
//                                value = customer!!.maKH,
//                                color = Color(0xFF81C784)
//                            )
//
//                            Spacer(modifier = Modifier.height(16.dp))
//
//                            InfoDisplayField(
//                                label = "Họ và tên",
//                                value = customer!!.hoTen,
//                                color = Color(0xFF64B5F6)
//                            )
//
//                            Spacer(modifier = Modifier.height(16.dp))
//
//                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
//                                Column(modifier = Modifier.weight(1f)) {
//                                    InfoDisplayField(
//                                        label = "Ngày sinh",
//                                        value = customer!!.ngaySinh,
//                                        color = Color(0xFFFFB74D)
//                                    )
//                                }
//
//                                Column(modifier = Modifier.weight(1f)) {
//                                    InfoDisplayField(
//                                        label = "Số điện thoại",
//                                        value = customer!!.soDienThoai,
//                                        color = Color(0xFFBA68C8)
//                                    )
//                                }
//                            }
//
//                            Spacer(modifier = Modifier.height(16.dp))
//
//                            // Card Type Display
//                            Text(
//                                text = "💳 Loại thẻ",
//                                fontSize = 14.sp,
//                                fontWeight = FontWeight.Bold,
//                                color = Color(0xFF333333)
//                            )
//
//                            Spacer(modifier = Modifier.height(8.dp))
//
//                            val (emoji, color, displayName) = when (customer!!.loaiThe) {
//                                "THUONG" -> Triple("🤍", Color(0xFF81C784), "Thẻ thường")
//                                "VANG" -> Triple("💛", Color(0xFFFFB74D), "Thẻ vàng")
//                                "BACHKIM" -> Triple("🤍", Color(0xFF64B5F6), "Thẻ bạch kim")
//                                "KIMCUONG" -> Triple("💎", Color(0xFFBA68C8), "Thẻ kim cương")
//                                else -> Triple("🤍", Color.Gray, customer!!.loaiThe)
//                            }
//
//                            Card(
//                                modifier = Modifier.fillMaxWidth(),
//                                shape = RoundedCornerShape(12.dp),
//                                colors = CardDefaults.cardColors(
//                                    containerColor = color.copy(alpha = 0.1f)
//                                ),
//                                border = BorderStroke(2.dp, color)
//                            ) {
//                                Row(
//                                    modifier = Modifier.padding(16.dp),
//                                    verticalAlignment = Alignment.CenterVertically
//                                ) {
//                                    Text(emoji, fontSize = 24.sp)
//                                    Spacer(modifier = Modifier.width(12.dp))
//                                    Text(
//                                        text = displayName,
//                                        fontSize = 16.sp,
//                                        fontWeight = FontWeight.Bold,
//                                        color = color
//                                    )
//                                }
//                            }
//                        }
//                    }
//
//                    // Right Column - Photo
//                    Card(
//                        modifier = Modifier.weight(1f),
//                        shape = RoundedCornerShape(20.dp),
//                        colors = CardDefaults.cardColors(containerColor = Color.White),
//                        elevation = CardDefaults.cardElevation(6.dp)
//                    ) {
//                        Column(modifier = Modifier.padding(24.dp)) {
//                            Text(
//                                text = "📷 Ảnh khách hàng",
//                                fontSize = 16.sp,
//                                fontWeight = FontWeight.Bold,
//                                color = Color(0xFF333333)
//                            )
//
//                            Spacer(modifier = Modifier.height(16.dp))
//                            if (customer!!.anhKH != null) {
//                                val imageBitmap: ImageBitmap = remember(customer!!.anhKH) {
//                                    val img = ImageIO.read(customer!!.anhKH!!.inputStream())
//                                    img.toComposeImageBitmap()
//                                }
//
//                                Image(
//                                    bitmap = imageBitmap,
//                                    contentDescription = "Ảnh khách hàng",
//                                    modifier = Modifier
//                                        .fillMaxWidth()
//                                        .height(200.dp)
//                                        .clip(RoundedCornerShape(12.dp)),
//                                    contentScale = ContentScale.Crop
//                                )
//                            } else {
//                                // Placeholder khi không có ảnh
//                                Box(
//                                    modifier = Modifier
//                                        .fillMaxWidth()
//                                        .height(200.dp)
//                                        .background(Color(0xFFF0F0F0), RoundedCornerShape(12.dp)),
//                                    contentAlignment = Alignment.Center
//                                ) {
//                                    Text(
//                                        text = "Không có ảnh",
//                                        fontSize = 14.sp,
//                                        color = Color(0xFF999999)
//                                    )
//                                }
//                            }
//
//                            if (customer!!.anhKH != null) {
//                                Spacer(modifier = Modifier.height(8.dp))
//                                Text(
//                                    text = "📊 Kích thước: ${customer!!.anhKH!!.size} bytes",
//                                    fontSize = 12.sp,
//                                    color = Color(0xFF666666)
//                                )
//                            }
//                        }
//                    }
//                }
//            } else {
//                // Empty state or loading
//                Card(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(300.dp),
//                    shape = RoundedCornerShape(20.dp),
//                    colors = CardDefaults.cardColors(containerColor = Color.White),
//                    elevation = CardDefaults.cardElevation(6.dp)
//                ) {
//                    Box(
//                        modifier = Modifier.fillMaxSize(),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        if (isLoading) {
//                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                                CircularProgressIndicator(
//                                    modifier = Modifier.size(40.dp),
//                                    color = Color(0xFF5C6BC0),
//                                    strokeWidth = 3.dp
//                                )
//                                Spacer(modifier = Modifier.height(16.dp))
//                                Text(
//                                    text = "Đang đọc dữ liệu...",
//                                    fontSize = 16.sp,
//                                    color = Color(0xFF666666)
//                                )
//                            }
//                        } else {
//                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                                Text("📭", fontSize = 48.sp)
//                                Spacer(modifier = Modifier.height(8.dp))
//                                Text(
//                                    text = "Không có dữ liệu",
//                                    fontSize = 16.sp,
//                                    color = Color(0xFF666666)
//                                )
//                                Text(
//                                    text = "Nhấn nút làm mới để đọc lại",
//                                    fontSize = 12.sp,
//                                    color = Color(0xFF999999)
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
//fun InfoDisplayField(
//    label: String,
//    value: String,
//    color: Color
//) {
//    Column {
//        Text(
//            text = label,
//            fontSize = 12.sp,
//            fontWeight = FontWeight.Medium,
//            color = color
//        )
//
//        Spacer(modifier = Modifier.height(4.dp))
//
//        Card(
//            modifier = Modifier.fillMaxWidth(),
//            shape = RoundedCornerShape(8.dp),
//            colors = CardDefaults.cardColors(
//                containerColor = color.copy(alpha = 0.1f)
//            ),
//            border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
//        ) {
//            Text(
//                text = if (value.isNotEmpty()) value else "Không có dữ liệu",
//                modifier = Modifier.padding(12.dp),
//                fontSize = 14.sp,
//                fontWeight = FontWeight.Medium,
//                color = if (value.isNotEmpty()) Color(0xFF333333) else Color(0xFF999999)
//            )
//        }
//    }
//}
