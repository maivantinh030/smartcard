package org.example.project.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.example.project.SmartCardManager
import org.jetbrains.skia.EncodedImageFormat

import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import org.jetbrains.skia.Image

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WriteDataScreen(
    smartCardManager: SmartCardManager,
    onBack: () -> Unit
) {
    var customerID by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var dateOfBirth by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var cardType by remember { mutableStateOf("THUONG") }
    var selectedImageBytes by remember { mutableStateOf<ByteArray?>(null) }
    var writeStatus by remember { mutableStateOf("") }
    var isWriting by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val cardTypes = listOf("THUONG", "VANG", "BACHKIM", "KIMCUONG")

    // Background giống các màn hình khác
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFAFAFA),  // Trắng xám nhẹ
                        Color(0xFFF5F5F5),  // Xám nhẹ
                        Color(0xFFE8EAF6)   // Xanh tím nhẹ
                    ),
                    startY = 0f,
                    endY = 2000f
                )
            )
    ) {
        // Hiệu ứng bong bóng nhẹ giống MainMenuScreen
        FloatingBubbles()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header giống style MainMenuScreen
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF5C6BC0)), // Tím xanh nhẹ
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
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.size(48.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("←", fontSize = 20.sp)
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        Color.White.copy(alpha = 0.2f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("✍️", fontSize = 20.sp)
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = "Ghi thông tin khách hàng",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Nhập thông tin để lưu vào thẻ",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(0.9f)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Left Column - Form
                Card(
                    modifier = Modifier.weight(2f),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(6.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "👤 Thông tin cá nhân",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Form fields với màu nhẹ nhàng
                        OutlinedTextField(
                            value = customerID,
                            onValueChange = { customerID = it.take(15) },
                            label = { Text("Mã khách hàng") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF81C784), // Xanh lá nhẹ
                                unfocusedBorderColor = Color(0xFFE0E0E0)
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it.take(50) },
                            label = { Text("Họ và tên") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF64B5F6), // Xanh dương nhẹ
                                unfocusedBorderColor = Color(0xFFE0E0E0)
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = dateOfBirth,
                                onValueChange = { dateOfBirth = it.take(10) },
                                label = { Text("Ngày sinh") },
                                placeholder = { Text("dd/mm/yyyy") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFFFB74D), // Vàng cam nhẹ
                                    unfocusedBorderColor = Color(0xFFE0E0E0)
                                )
                            )

                            OutlinedTextField(
                                value = phoneNumber,
                                onValueChange = { phoneNumber = it.take(10) },
                                label = { Text("Số điện thoại") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFBA68C8), // Tím nhẹ
                                    unfocusedBorderColor = Color(0xFFE0E0E0)
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Card type selection với màu gradient nhẹ
                        Text(
                            text = "💳 Loại thẻ",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            cardTypes.forEach { type ->
                                val isSelected = cardType == type
                                val (emoji, color) = when (type) {
                                    "THUONG" -> "🤍" to Color(0xFF81C784) // Xanh lá nhẹ
                                    "VANG" -> "💛" to Color(0xFFFFB74D)   // Vàng cam nhẹ
                                    "BACHKIM" -> "🤍" to Color(0xFF64B5F6) // Xanh dương nhẹ
                                    "KIMCUONG" -> "💎" to Color(0xFFBA68C8) // Tím nhẹ
                                    else -> "🤍" to Color.Gray
                                }

                                Card(
                                    onClick = { cardType = type },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) color.copy(alpha = 0.15f) else Color(0xFFFAFAFA)
                                    ),
                                    border = if (isSelected) BorderStroke(2.dp, color) else BorderStroke(1.dp, Color(0xFFE0E0E0))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(emoji, fontSize = 18.sp)
                                        Text(
                                            text = type.take(4),
                                            fontSize = 10.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) color else Color(0xFF666666)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Right Column - Image & Actions
                Column(modifier = Modifier.weight(1f)) {
                    // Image Section
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(6.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = "📷 Ảnh khách hàng",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF333333)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            if (selectedImageBytes != null) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(140.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    elevation = CardDefaults.cardElevation(4.dp)
                                ) {
                                    selectedImageBytes?.toImageBitmap()?.let { bitmap ->
                                        Image(
                                            bitmap = bitmap,
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(140.dp)
                                        .background(
                                            Color(0xFFF8F9FA),
                                            RoundedCornerShape(12.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("📷", fontSize = 40.sp)
                                        Text(
                                            text = "Chưa chọn ảnh",
                                            fontSize = 12.sp,
                                            color = Color(0xFF666666)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    val fileDialog = java.awt.FileDialog(null as java.awt.Frame?, "Chọn ảnh", java.awt.FileDialog.LOAD)
                                    fileDialog.isVisible = true

                                    if (fileDialog.file != null) {
                                        val file = java.io.File(fileDialog.directory, fileDialog.file)
                                        selectedImageBytes = compressImage(file.toString())
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF64B5F6) // Xanh dương nhẹ
                                )
                            ) {
                                Text("📁 Chọn ảnh", fontSize = 14.sp, color = Color.White)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Action Buttons với màu gradient nhẹ
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                scope.launch {
                                    isWriting = true
                                    writeStatus = "Đang ghi dữ liệu..."

                                    smartCardManager.writeCustomerInfo(customerID, name, dateOfBirth, phoneNumber, cardType)

                                    selectedImageBytes?.let {
                                        smartCardManager.writeCustomerImage(it)
                                    }

                                    writeStatus = "✅ Ghi thành công!"
                                    isWriting = false
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isWriting && customerID.isNotEmpty() && name.isNotEmpty(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF81C784) // Xanh lá nhẹ
                            )
                        ) {
                            if (isWriting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Đang ghi...", fontSize = 14.sp, color = Color.White)
                            } else {
                                Text("💾 Ghi vào thẻ", fontSize = 14.sp, color = Color.White)
                            }
                        }

                        Button(
                            onClick = {
                                customerID = ""
                                name = ""
                                dateOfBirth = ""
                                phoneNumber = ""
                                cardType = "THUONG"
                                selectedImageBytes = null
                                writeStatus = ""
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFE57373) // Đỏ nhạt
                            )
                        ) {
                            Text("🗑️ Xóa tất cả", fontSize = 14.sp, color = Color.White)
                        }
                    }
                }
            }

            // Status với màu nhẹ nhàng
            if (writeStatus.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            writeStatus.contains("✅") -> Color(0xFF81C784)
                            writeStatus.contains("❌") -> Color(0xFFE57373)
                            else -> Color(0xFF64B5F6)
                        }
                    )
                ) {
                    Text(
                        text = writeStatus,
                        modifier = Modifier.padding(16.dp),
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}


// Helper extension
fun ByteArray.toImageBitmap(): ImageBitmap? {
    return try {
        val bufferedImage = ImageIO.read(this.inputStream())
        bufferedImage?.toComposeImageBitmap()
    } catch (e: Exception) {
        null
    }
}


//fun compressImage(originalBytes: ByteArray): ByteArray {
//    return try {
//        // 1. Đọc ảnh gốc
//        val originalImage = ImageIO.read(originalBytes.inputStream())
//
//        // 2. Resize về 64x64 pixels
//        val resized = BufferedImage(64, 64, BufferedImage.TYPE_INT_RGB)
//        val g2d = resized.createGraphics()
//        g2d.drawImage(originalImage.getScaledInstance(64, 64, Image.SCALE_SMOOTH), 0, 0, null)
//        g2d.dispose()
//        val output = ByteArrayOutputStream()
//        val writers = ImageIO.getImageWritersByFormatName("jpg")
//        val writer = writers.next()
//        val ios = ImageIO.createImageOutputStream(output)
//
//        writer.output = ios
//        writer.write(resized)
//        writer.dispose()
//        ios.close()
//        output.toByteArray()
//
//    } catch (e: Exception) {
//        println("Lỗi nén ảnh: ${e.message}")
//        originalBytes // Trả về ảnh gốc nếu lỗi
//    }
//}
fun compressImage(avatarPath: String, maxSize: Int = 8192): ByteArray {
    val bytes = java.io.File(avatarPath).readBytes()
    val skiaImage = Image.makeFromEncoded(bytes)

    // Resize: dùng makeImageSnapshot vì Skia JVM không có withDimensions/ext!
    val targetW = 128
    val targetH = 128
    val resized = skiaImage.resize(targetW, targetH)
    // JPEG encode, quality 80
    val jpegBytes = resized.encodeToData(EncodedImageFormat.JPEG, 64)!!.bytes
    require(jpegBytes.size <= maxSize) { "Ảnh quá lớn, vui lòng chọn ảnh nhỏ hoặc thấp chất lượng hơn." }
    return jpegBytes
}

// Extension resize cho org.jetbrains.skia.Image
fun Image.resize(width: Int, height: Int): Image {
    val scaled = this.scalePixels(width, height)
    return scaled
}

// Thêm extension cho scale:
fun Image.scalePixels(newWidth: Int, newHeight: Int): Image {
    val surface = org.jetbrains.skia.Surface.makeRasterN32Premul(newWidth, newHeight)
    val canvas = surface.canvas
    val paint = org.jetbrains.skia.Paint()
    canvas.drawImageRect(
        this,
        org.jetbrains.skia.Rect.makeXYWH(0f, 0f, this.width.toFloat(), this.height.toFloat()),
        org.jetbrains.skia.Rect.makeXYWH(0f, 0f, newWidth.toFloat(), newHeight.toFloat()),
        paint
    )
    return surface.makeImageSnapshot()
}