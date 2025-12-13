package org.example.project.screen.user

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
import androidx.compose. ui.unit.sp
import kotlinx.coroutines.launch
import org.example.project.SmartCardManager
import org.example.project.screen.FloatingBubbles
import org.jetbrains.skia.Image as SkiaImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserViewInfoScreen(
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

    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        isLoading = true
        status = "⏳ Đang đọc thông tin..."

        try {
            val info = smartCardManager.readCustomerInfo()
            customerID = info["customerID"] ?: ""
            name = info["name"] ?: ""
            dateOfBirth = info["dateOfBirth"] ?: ""
            phoneNumber = info["phoneNumber"] ?: ""

            status = "✅ Đã đọc thông tin"

            val photoBytes = smartCardManager.readCustomerImage()
            if (photoBytes != null && photoBytes.isNotEmpty()) {
                try {
                    val skiaImage = SkiaImage. makeFromEncoded(photoBytes)
                    customerPhoto = skiaImage.toComposeImageBitmap()
                    status = "✅ Đã tải đầy đủ thông tin!"
                } catch (e: Exception) {
                    status = "⚠️ Không thể hiển thị ảnh"
                }
            }

        } catch (e: Exception) {
            status = "❌ Lỗi:  ${e.message}"
        } finally {
            isLoading = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE3F2FD),
                        Color(0xFFF8BBD0),
                        Color(0xFFFFF9C4)
                    )
                )
            )
    ) {
        FloatingBubbles()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // HEADER
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(16.dp, RoundedCornerShape(32.dp)),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = Color. Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF667EEA),
                                    Color(0xFF764BA2),
                                    Color(0xFFF093FB)
                                )
                            )
                        )
                        .padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier. fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(Color.White. copy(alpha = 0.25f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White,
                                modifier = Modifier. size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "👤 Thông Tin Của Tôi",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Xem thông tin cá nhân",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White. copy(alpha = 0.95f)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("📋", fontSize = 32.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier. height(24.dp))

            // CONTENT
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .shadow(12.dp, RoundedCornerShape(32.dp)),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                if (isLoading) {
                    Box(
                        modifier = Modifier. fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier. size(64.dp),
                            color = Color(0xFF667EEA),
                            strokeWidth = 6.dp
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(32.dp)
                    ) {
                        // ẢNH
                        Card(
                            modifier = Modifier. fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFAFAFA)
                            ),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(28.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(180.dp)
                                        . shadow(8.dp, CircleShape)
                                        .clip(CircleShape)
                                        .background(
                                            brush = Brush. radialGradient(
                                                colors = listOf(
                                                    Color(0xFFBBDEFB),
                                                    Color(0xFF90CAF9),
                                                    Color(0xFF64B5F6)
                                                )
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (customerPhoto != null) {
                                        Image(
                                            bitmap = customerPhoto!!,
                                            contentDescription = "Photo",
                                            modifier = Modifier. fillMaxSize()
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default. Person,
                                            contentDescription = null,
                                            modifier = Modifier.size(72.dp),
                                            tint = Color. White
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier. height(28.dp))
                        Divider(color = Color(0xFFE0E0E0), thickness = 2.dp)
                        Spacer(modifier = Modifier. height(20.dp))

                        // THÔNG TIN (XÓA LOẠI THẺ)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("📝", fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Thông tin cơ bản",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF333333)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        InfoCard(
                            icon = Icons. Default.Badge,
                            label = "Mã khách hàng",
                            value = customerID. ifEmpty { "Chưa có" },
                            iconColor = Color(0xFF667EEA)
                        )

                        Spacer(modifier = Modifier. height(16.dp))

                        InfoCard(
                            icon = Icons. Default.Person,
                            label = "Họ và tên",
                            value = name.ifEmpty { "Chưa có" },
                            iconColor = Color(0xFF667EEA)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        InfoCard(
                            icon = Icons.Default.CalendarToday,
                            label = "Ngày sinh",
                            value = dateOfBirth.ifEmpty { "Chưa có" },
                            iconColor = Color(0xFF667EEA)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        InfoCard(
                            icon = Icons.Default. Phone,
                            label = "Số điện thoại",
                            value = phoneNumber.ifEmpty { "Chưa có" },
                            iconColor = Color(0xFF667EEA)
                        )
                    }
                }
            }

            if (status.isNotEmpty() && !isLoading) {
                Spacer(modifier = Modifier. height(16.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            status.startsWith("✅") -> Color(0xFFE8F5E9)
                            status.startsWith("⚠️") -> Color(0xFFFFF8E1)
                            else -> Color(0xFFFFEBEE)
                        }
                    )
                ) {
                    Text(
                        text = status,
                        modifier = Modifier.padding(20.dp),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
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
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
        elevation = CardDefaults. cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(iconColor. copy(alpha = 0.15f)),
                contentAlignment = Alignment. Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = label,
                    fontSize = 13.sp,
                    color = Color. Gray,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier. height(4.dp))
                Text(
                    text = value,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )
            }
        }
    }
}