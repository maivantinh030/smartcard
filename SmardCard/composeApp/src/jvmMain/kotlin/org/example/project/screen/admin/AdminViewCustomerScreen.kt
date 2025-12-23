package org.example.project.screen.admin

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.example.project.SmartCardManager
import org.example.project.screen.FloatingBubbles
import org.jetbrains.skia.Image as SkiaImage
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun AdminViewCustomerScreen(
//    smartCardManager:  SmartCardManager,
//    onBack: () -> Unit
//) {
//    var customerID by remember { mutableStateOf("") }
//    var name by remember { mutableStateOf("") }
//    var dateOfBirth by remember { mutableStateOf("") }
//    var phoneNumber by remember { mutableStateOf("") }
//    var customerPhoto by remember { mutableStateOf<ImageBitmap?>(null) }
//    var isLoading by remember { mutableStateOf(false) }
//    var status by remember { mutableStateOf("") }
//    var photoLoadProgress by remember { mutableStateOf(0f) }
//
//    val scope = rememberCoroutineScope()
//    val scrollState = rememberScrollState()
//
//    // ✅ Load dữ liệu khi vào màn hình
//    LaunchedEffect(Unit) {
//        isLoading = true
//        status = "⏳ Đang đọc thông tin..."
//
//        try {
//            // 1. Đọc thông tin cơ bản
//            smartCardManager.debugPhotoInfo()
//            val info = smartCardManager.readCustomerInfo()
//            customerID = info["customerID"] ?: ""
//            name = info["name"] ?: ""
//            dateOfBirth = info["dateOfBirth"] ?: ""
//            phoneNumber = info["phoneNumber"] ?: ""
//
//            status = "✅ Đã đọc thông tin cơ bản"
//            delay(500)
//
//            // 2. Đọc ảnh
//            status = "📥 Đang tải ảnh..."
//            photoLoadProgress = 0.1f
//
//            val photoBytes = smartCardManager.readCustomerImage()
//
//            if (photoBytes != null && photoBytes.isNotEmpty()) {
//                photoLoadProgress = 0.5f
//
//                // ✅ Convert bytes → ImageBitmap
//                try {
//                    val skiaImage = SkiaImage. makeFromEncoded(photoBytes)
//                    customerPhoto = skiaImage.toComposeImageBitmap()
//                    photoLoadProgress = 1.0f
//                    status = "✅ Đã tải ảnh thành công!"
//                } catch (e: Exception) {
//                    status = "⚠️ Không thể hiển thị ảnh:  ${e.message}"
//                    customerPhoto = null
//                }
//            } else {
//                status = "⚠️ Không có ảnh trên thẻ"
//                customerPhoto = null
//            }
//
//        } catch (e: Exception) {
//            status = "❌ Lỗi đọc dữ liệu: ${e.message}"
//            e.printStackTrace()
//        } finally {
//            delay(1000)
//            isLoading = false
//            photoLoadProgress = 0f
//        }
//    }
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(
//                brush = Brush.verticalGradient(
//                    colors = listOf(
//                        Color(0xFFE3F2FD),
//                        Color(0xFFF8BBD0),
//                        Color(0xFFFFF9C4)
//                    )
//                )
//            )
//    ) {
//        FloatingBubbles()
//
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(24.dp)
//        ) {
//            // ✅ HEADER
//            Card(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .shadow(16.dp, RoundedCornerShape(32.dp)),
//                shape = RoundedCornerShape(32.dp),
//                colors = CardDefaults.cardColors(containerColor = Color. Transparent)
//            ) {
//                Box(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .background(
//                            brush = Brush.horizontalGradient(
//                                colors = listOf(
//                                    Color(0xFF4A90E2),
//                                    Color(0xFF50C9C3),
//                                    Color(0xFF56F0A0)
//                                )
//                            )
//                        )
//                        .padding(24.dp)
//                ) {
//                    Row(
//                        modifier = Modifier. fillMaxWidth(),
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        IconButton(
//                            onClick = onBack,
//                            modifier = Modifier
//                                .size(52.dp)
//                                .clip(CircleShape)
//                                .background(Color.White. copy(alpha = 0.25f))
//                        ) {
//                            Icon(
//                                imageVector = Icons.Default.ArrowBack,
//                                contentDescription = "Back",
//                                tint = Color. White,
//                                modifier = Modifier. size(28.dp)
//                            )
//                        }
//
//                        Spacer(modifier = Modifier.width(16.dp))
//
//                        Column(modifier = Modifier.weight(1f)) {
//                            Text(
//                                text = "👤 Thông Tin Khách Hàng",
//                                fontSize = 24.sp,
//                                fontWeight = FontWeight.ExtraBold,
//                                color = Color.White
//                            )
//                            Spacer(modifier = Modifier. height(4.dp))
//                            Text(
//                                text = "Xem thông tin & ảnh đã lưu",
//                                fontSize = 15.sp,
//                                fontWeight = FontWeight.Medium,
//                                color = Color.White. copy(alpha = 0.95f)
//                            )
//                        }
//
//                        Box(
//                            modifier = Modifier
//                                .size(64.dp)
//                                .clip(CircleShape)
//                                .background(Color.White.copy(alpha = 0.25f)),
//                            contentAlignment = Alignment.Center
//                        ) {
//                            Text("📋", fontSize = 32.sp)
//                        }
//                    }
//                }
//            }
//
//            Spacer(modifier = Modifier. height(24.dp))
//
//            // ✅ CONTENT CARD
//            Card(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .weight(1f)
//                    . shadow(12.dp, RoundedCornerShape(32.dp)),
//                shape = RoundedCornerShape(32.dp),
//                colors = CardDefaults.cardColors(containerColor = Color.White)
//            ) {
//                if (isLoading) {
//                    // Loading State
//                    Box(
//                        modifier = Modifier. fillMaxSize(),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Column(
//                            horizontalAlignment = Alignment.CenterHorizontally,
//                            verticalArrangement = Arrangement. Center
//                        ) {
//                            CircularProgressIndicator(
//                                modifier = Modifier.size(64.dp),
//                                color = Color(0xFF4A90E2),
//                                strokeWidth = 6.dp
//                            )
//                            Spacer(modifier = Modifier.height(20.dp))
//                            Text(
//                                text = "Đang đọc dữ liệu...",
//                                fontSize = 16.sp,
//                                fontWeight = FontWeight.Medium,
//                                color = Color. Gray
//                            )
//
//                            if (photoLoadProgress > 0f) {
//                                Spacer(modifier = Modifier. height(16.dp))
//                                LinearProgressIndicator(
//                                    progress = { photoLoadProgress },
//                                    modifier = Modifier
//                                        .width(200.dp)
//                                        . height(8.dp)
//                                        . clip(RoundedCornerShape(4.dp)),
//                                    color = Color(0xFF4A90E2)
//                                )
//                            }
//                        }
//                    }
//                } else {
//                    // Data Display
//                    Column(
//                        modifier = Modifier
//                            .fillMaxSize()
//                            .verticalScroll(scrollState)
//                            .padding(32.dp)
//                    ) {
//                        // ✅ ẢNH KHÁCH HÀNG
//                        Card(
//                            modifier = Modifier. fillMaxWidth(),
//                            shape = RoundedCornerShape(24.dp),
//                            colors = CardDefaults.cardColors(
//                                containerColor = Color(0xFFFAFAFA)
//                            ),
//                            elevation = CardDefaults.cardElevation(4.dp)
//                        ) {
//                            Column(
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                                    .padding(28.dp),
//                                horizontalAlignment = Alignment.CenterHorizontally
//                            ) {
//                                Row(
//                                    verticalAlignment = Alignment.CenterVertically,
//                                    horizontalArrangement = Arrangement. Center
//                                ) {
//                                    Text("📸", fontSize = 24.sp)
//                                    Spacer(modifier = Modifier.width(8.dp))
//                                    Text(
//                                        text = "Ảnh khách hàng",
//                                        fontSize = 20.sp,
//                                        fontWeight = FontWeight.Bold,
//                                        color = Color(0xFF333333)
//                                    )
//                                }
//
//                                Spacer(modifier = Modifier.height(20.dp))
//
//                                Box(
//                                    modifier = Modifier
//                                        .size(180.dp)
//                                        . shadow(8.dp, CircleShape)
//                                        .clip(CircleShape)
//                                        .background(
//                                            brush = Brush. radialGradient(
//                                                colors = listOf(
//                                                    Color(0xFFBBDEFB),
//                                                    Color(0xFF90CAF9),
//                                                    Color(0xFF64B5F6)
//                                                )
//                                            )
//                                        ),
//                                    contentAlignment = Alignment.Center
//                                ) {
//                                    if (customerPhoto != null) {
//                                        Image(
//                                            bitmap = customerPhoto!!,
//                                            contentDescription = "Customer Photo",
//                                            modifier = Modifier. fillMaxSize()
//                                        )
//                                    } else {
//                                        Column(
//                                            horizontalAlignment = Alignment.CenterHorizontally,
//                                            verticalArrangement = Arrangement.Center
//                                        ) {
//                                            Icon(
//                                                imageVector = Icons.Default.Person,
//                                                contentDescription = null,
//                                                modifier = Modifier.size(72.dp),
//                                                tint = Color. White
//                                            )
//                                            Spacer(modifier = Modifier.height(8.dp))
//                                            Text(
//                                                text = "Không có ảnh",
//                                                fontSize = 14.sp,
//                                                fontWeight = FontWeight.Medium,
//                                                color = Color.White
//                                            )
//                                        }
//                                    }
//                                }
//                            }
//                        }
//
//                        Spacer(modifier = Modifier. height(28.dp))
//
//                        Divider(
//                            color = Color(0xFFE0E0E0),
//                            thickness = 2.dp,
//                            modifier = Modifier. padding(vertical = 8.dp)
//                        )
//
//                        Spacer(modifier = Modifier. height(20.dp))
//
//                        // ✅ THÔNG TIN CƠ BẢN
//                        Row(verticalAlignment = Alignment.CenterVertically) {
//                            Text("📝", fontSize = 22.sp)
//                            Spacer(modifier = Modifier.width(8.dp))
//                            Text(
//                                text = "Thông tin cơ bản",
//                                fontSize = 20.sp,
//                                fontWeight = FontWeight.Bold,
//                                color = Color(0xFF333333)
//                            )
//                        }
//
//                        Spacer(modifier = Modifier.height(20.dp))
//
//                        // Mã khách hàng
//                        InfoCard(
//                            icon = Icons.Default.Badge,
//                            label = "Mã khách hàng",
//                            value = customerID. ifEmpty { "Chưa có dữ liệu" },
//                            iconColor = Color(0xFF4A90E2)
//                        )
//
//                        Spacer(modifier = Modifier.height(16.dp))
//
//                        // Họ và tên
//                        InfoCard(
//                            icon = Icons.Default.Person,
//                            label = "Họ và tên",
//                            value = name.ifEmpty { "Chưa có dữ liệu" },
//                            iconColor = Color(0xFF4A90E2)
//                        )
//
//                        Spacer(modifier = Modifier. height(16.dp))
//
//                        // Ngày sinh
//                        InfoCard(
//                            icon = Icons.Default.CalendarToday,
//                            label = "Ngày sinh",
//                            value = dateOfBirth.ifEmpty { "Chưa có dữ liệu" },
//                            iconColor = Color(0xFF4A90E2)
//                        )
//
//                        Spacer(modifier = Modifier. height(16.dp))
//
//                        // Số điện thoại
//                        InfoCard(
//                            icon = Icons.Default. Phone,
//                            label = "Số điện thoại",
//                            value = phoneNumber. ifEmpty { "Chưa có dữ liệu" },
//                            iconColor = Color(0xFF4A90E2)
//                        )
//                    }
//                }
//            }
//
//            // ✅ STATUS
//            if (status.isNotEmpty() && ! isLoading) {
//                Spacer(modifier = Modifier. height(16.dp))
//                Card(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .shadow(8.dp, RoundedCornerShape(20.dp)),
//                    shape = RoundedCornerShape(20.dp),
//                    colors = CardDefaults.cardColors(
//                        containerColor = when {
//                            status.startsWith("✅") -> Color(0xFFE8F5E9)
//                            status.startsWith("⚠️") -> Color(0xFFFFF8E1)
//                            else -> Color(0xFFFFEBEE)
//                        }
//                    )
//                ) {
//                    Row(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(20.dp),
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        Text(
//                            text = when {
//                                status.startsWith("✅") -> "✅"
//                                status.startsWith("⚠️") -> "⚠️"
//                                status.startsWith("⏳") -> "⏳"
//                                status.startsWith("📥") -> "📥"
//                                else -> "❌"
//                            },
//                            fontSize = 28.sp
//                        )
//                        Spacer(modifier = Modifier.width(14.dp))
//                        Text(
//                            text = status. substring(2),
//                            fontSize = 15.sp,
//                            fontWeight = FontWeight.SemiBold,
//                            color = when {
//                                status.startsWith("✅") -> Color(0xFF4CAF50)
//                                status. startsWith("⚠️") -> Color(0xFFFFA726)
//                                else -> Color(0xFFE53935)
//                            },
//                            modifier = Modifier. weight(1f)
//                        )
//                    }
//                }
//            }
//        }
//    }
//}
//
//@Composable
//private fun InfoCard(
//    icon:  androidx.compose.ui.graphics.vector.ImageVector,
//    label: String,
//    value: String,
//    iconColor: Color
//) {
//    Card(
//        modifier = Modifier. fillMaxWidth(),
//        shape = RoundedCornerShape(16.dp),
//        colors = CardDefaults.cardColors(
//            containerColor = Color(0xFFFAFAFA)
//        ),
//        elevation = CardDefaults.cardElevation(2.dp)
//    ) {
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(20.dp),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Box(
//                modifier = Modifier
//                    .size(48.dp)
//                    .clip(CircleShape)
//                    .background(iconColor.copy(alpha = 0.15f)),
//                contentAlignment = Alignment. Center
//            ) {
//                Icon(
//                    imageVector = icon,
//                    contentDescription = null,
//                    tint = iconColor,
//                    modifier = Modifier.size(24.dp)
//                )
//            }
//
//            Spacer(modifier = Modifier.width(16.dp))
//
//            Column(modifier = Modifier.weight(1f)) {
//                Text(
//                    text = label,
//                    fontSize = 13.sp,
//                    color = Color.Gray,
//                    fontWeight = FontWeight.Medium
//                )
//                Spacer(modifier = Modifier. height(4.dp))
//                Text(
//                    text = value,
//                    fontSize = 17.sp,
//                    fontWeight = FontWeight.Bold,
//                    color = Color(0xFF333333)
//                )
//            }
//        }
//    }
//}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminViewCustomerScreen(
    smartCardManager: SmartCardManager,
    onBack: () -> Unit
) {
    var customerID by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var dateOfBirth by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var customerPhoto by remember { mutableStateOf<ImageBitmap?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("") }
    var photoLoadProgress by remember { mutableStateOf(0f) }

    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        isLoading = true
        status = "⏳ Đang đọc thông tin..."

        try {
            smartCardManager.debugPhotoInfo()
            val info = smartCardManager.readCustomerInfo()
            customerID = info["customerID"] ?: ""
            name = info["name"] ?:  ""
            dateOfBirth = info["dateOfBirth"] ?: ""
            phoneNumber = info["phoneNumber"] ?: ""

            status = "✅ Đã đọc thông tin cơ bản"
            delay(500)

            status = "📥 Đang tải ảnh..."
            photoLoadProgress = 0.1f

            val photoBytes = smartCardManager.readCustomerImage()

            if (photoBytes != null && photoBytes.isNotEmpty()) {
                photoLoadProgress = 0.5f

                try {
                    val skiaImage = SkiaImage. makeFromEncoded(photoBytes)
                    customerPhoto = skiaImage.toComposeImageBitmap()
                    photoLoadProgress = 1.0f
                    status = "✅ Đã tải ảnh thành công!"
                } catch (e: Exception) {
                    status = "⚠️ Không thể hiển thị ảnh:  ${e.message}"
                    customerPhoto = null
                }
            } else {
                status = "⚠️ Không có ảnh trên thẻ"
                customerPhoto = null
            }

        } catch (e: Exception) {
            status = "❌ Lỗi đọc dữ liệu: ${e.message}"
            e.printStackTrace()
        } finally {
            delay(1000)
            isLoading = false
            photoLoadProgress = 0f
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFE5EC),  // ✅ GIỐNG AdminWriteInfoScreen
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
                .padding(horizontal = 80.dp, vertical = 20.dp)  // ✅ GIỐNG AdminWriteInfoScreen
        ) {
            // ✅ HEADER
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(12.dp, RoundedCornerShape(28.dp)),  // ✅ GIỐNG
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color. Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFFFF6B9D),  // ✅ GIỐNG
                                    Color(0xFFC06FBB),
                                    Color(0xFFFEC163)
                                )
                            )
                        )
                        .padding(20.dp)  // ✅ GIỐNG
                ) {
                    Row(
                        modifier = Modifier. fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .size(48.dp)  // ✅ GIỐNG
                                .clip(CircleShape)
                                .background(Color.White. copy(alpha = 0.3f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White,
                                modifier = Modifier. size(26.dp)  // ✅ GIỐNG
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "👤 Thông Tin Khách Hàng",
                                fontSize = 22.sp,  // ✅ GIỐNG
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier. height(6.dp))
                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White. copy(alpha = 0.25f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("📋", fontSize = 18.sp)  // ✅ GIỐNG
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Xem thông tin & ảnh",
                                        fontSize = 14.sp,  // ✅ GIỐNG
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(60.dp)  // ✅ GIỐNG
                                . clip(CircleShape)
                                .background(Color.White. copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("📋", fontSize = 32.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier. height(20.dp))  // ✅ GIỐNG

            // ✅ CONTENT CARD
            Card(
                modifier = Modifier
                    . fillMaxWidth()
                    . wrapContentHeight()  // ✅ GIỐNG
                    .shadow(12.dp, RoundedCornerShape(28.dp)),  // ✅ GIỐNG
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp),
                        contentAlignment = Alignment. Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(52.dp),
                                color = Color(0xFFFF6B9D),
                                strokeWidth = 5.dp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Đang đọc dữ liệu...",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color. Gray
                            )

                            if (photoLoadProgress > 0f) {
                                Spacer(modifier = Modifier.height(14.dp))
                                LinearProgressIndicator(
                                    progress = { photoLoadProgress },
                                    modifier = Modifier
                                        .width(180.dp)
                                        . height(7.dp)
                                        . clip(RoundedCornerShape(4.dp)),
                                    color = Color(0xFFFF6B9D)
                                )
                            }
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp)  // ✅ GIỐNG
                    ) {
                        // ✅ ẢNH KHÁCH HÀNG
                        Card(
                            modifier = Modifier. fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),  // ✅ GIỐNG
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFFF3E0)  // ✅ GIỐNG
                            ),
                            elevation = CardDefaults.cardElevation(6.dp)  // ✅ GIỐNG
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),  // ✅ GIỐNG
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    verticalAlignment = Alignment. CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text("📸", fontSize = 22.sp)  // ✅ GIỐNG
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Ảnh khách hàng",
                                        fontSize = 20.sp,  // ✅ GIỐNG
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFFFF6B00)  // ✅ GIỐNG
                                    )
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                Box(
                                    modifier = Modifier
                                        .size(150.dp)  // ✅ GIỐNG
                                        .shadow(12.dp, CircleShape)  // ✅ GIỐNG
                                        .clip(CircleShape)
                                        . background(
                                            brush = Brush.radialGradient(
                                                colors = listOf(
                                                    Color(0xFFFF6B9D),  // ✅ GIỐNG
                                                    Color(0xFFFFA07A),
                                                    Color(0xFFFFD700)
                                                )
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (customerPhoto != null) {
                                        Image(
                                            bitmap = customerPhoto!!,
                                            contentDescription = "Customer Photo",
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Person,
                                                contentDescription = null,
                                                modifier = Modifier.size(64.dp),  // ✅ GIỐNG
                                                tint = Color. White
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "Không có ảnh",
                                                fontSize = 13.sp,  // ✅ GIỐNG
                                                fontWeight = FontWeight.Bold,
                                                color = Color. White
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(28.dp))  // ✅ GIỐNG

                        HorizontalDivider(
                            color = Color(0xFFFFAB91),  // ✅ GIỐNG
                            thickness = 2.dp,
                            modifier = Modifier.padding(vertical = 10.dp)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // ✅ THÔNG TIN CƠ BẢN
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("📝", fontSize = 24.sp)  // ✅ GIỐNG
                            Spacer(modifier = Modifier. width(10.dp))
                            Text(
                                text = "Thông tin cơ bản",
                                fontSize = 20.sp,  // ✅ GIỐNG
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFFF6B00)  // ✅ GIỐNG
                            )
                        }

                        Spacer(modifier = Modifier. height(20.dp))

                        InfoCard(
                            icon = Icons. Default.Badge,
                            label = "Mã khách hàng",
                            value = customerID. ifEmpty { "Chưa có dữ liệu" },
                            iconColor = Color(0xFF4CAF50)
                        )

                        Spacer(modifier = Modifier. height(18.dp))  // ✅ GIỐNG

                        InfoCard(
                            icon = Icons.Default.Person,
                            label = "Họ và tên",
                            value = name.ifEmpty { "Chưa có dữ liệu" },
                            iconColor = Color(0xFFFF6B9D)  // ✅ GIỐNG màu TextField
                        )

                        Spacer(modifier = Modifier. height(18.dp))

                        InfoCard(
                            icon = Icons.Default.CalendarToday,
                            label = "Ngày sinh",
                            value = dateOfBirth.ifEmpty { "Chưa có dữ liệu" },
                            iconColor = Color(0xFFFFA726)  // ✅ GIỐNG màu TextField
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        InfoCard(
                            icon = Icons.Default.Phone,
                            label = "Số điện thoại",
                            value = phoneNumber.ifEmpty { "Chưa có dữ liệu" },
                            iconColor = Color(0xFF66BB6A)  // ✅ GIỐNG màu TextField
                        )
                    }
                }
            }

            // ✅ STATUS
            if (status.isNotEmpty() && ! isLoading) {
                Spacer(modifier = Modifier. height(16.dp))  // ✅ GIỐNG
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(10.dp, RoundedCornerShape(20.dp)),  // ✅ GIỐNG
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            status.startsWith("✅") -> Color(0xFFE8F5E9)
                            status.startsWith("⚠️") -> Color(0xFFFFF3E0)
                            status.startsWith("🔐") -> Color(0xFFE3F2FD)
                            status.startsWith("📤") -> Color(0xFFFFF4E6)
                            status.startsWith("💾") -> Color(0xFFE8EAF6)
                            status.startsWith("📥") -> Color(0xFFE3F2FD)
                            else -> Color(0xFFFFEBEE)
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),  // ✅ GIỐNG
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when {
                                status.startsWith("✅") -> "✅"
                                status.startsWith("⚠️") -> "⚠️"
                                status.startsWith("🔐") -> "🔐"
                                status.startsWith("📤") -> "📤"
                                status.startsWith("💾") -> "💾"
                                status.startsWith("⏳") -> "⏳"
                                status.startsWith("📥") -> "📥"
                                else -> "❌"
                            },
                            fontSize = 28.sp  // ✅ GIỐNG
                        )
                        Spacer(modifier = Modifier. width(14.dp))
                        Text(
                            text = status. substring(2),
                            fontSize = 16.sp,  // ✅ GIỐNG
                            fontWeight = FontWeight.Bold,
                            color = when {
                                status.startsWith("✅") -> Color(0xFF4CAF50)
                                status.startsWith("⚠️") -> Color(0xFFFFA726)
                                status. startsWith("🔐") -> Color(0xFF2196F3)
                                status.startsWith("📤") -> Color(0xFFFF9800)
                                status.startsWith("💾") -> Color(0xFF673AB7)
                                status. startsWith("📥") -> Color(0xFF2196F3)
                                else -> Color(0xFFE53935)
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoCard(
    icon:  androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    iconColor: Color
) {
    Card(
        modifier = Modifier. fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),  // ✅ GIỐNG
        colors = CardDefaults. cardColors(
            containerColor = Color(0xFFFAFAFA)
        ),
        elevation = CardDefaults.cardElevation(4.dp)  // ✅ GIỐNG
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),  // ✅ GIỐNG
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)  // ✅ GIỐNG icon size trong TextField (24dp icon + padding)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment. Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)  // ✅ GIỐNG TextField icon
                )
            }

            Spacer(modifier = Modifier. width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    fontSize = 12.sp,  // ✅ GIỐNG
                    color = Color(0xFF666666),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = value,
                    fontSize = 18.sp,  // ✅ GIỐNG
                    fontWeight = FontWeight.ExtraBold,
                    color = iconColor  // ✅ ĐỔI:  Dùng màu icon
                )
            }
        }
    }
}