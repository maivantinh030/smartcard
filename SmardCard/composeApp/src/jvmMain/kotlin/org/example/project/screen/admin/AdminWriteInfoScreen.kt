package org.example.project.screen.admin

import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.example.project.SmartCardManager
import org.example.project.screen.FloatingBubbles
import java.awt.FileDialog
import java.awt.Frame
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.imageio.ImageIO

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminWriteInfoScreen(
    smartCardManager: SmartCardManager,
    onBack: () -> Unit
) {
    // ✅ PREFIX TỰ ĐỘNG
    val datePrefix = remember {
        val now = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("ddMMyy")
        "KH${now.format(formatter)}"
    }

    var customerID by remember { mutableStateOf(datePrefix) }
    var customerSuffix by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }

    // ✅ SỬA:  Dùng TextFieldValue để quản lý cursor
    var dateOfBirthState by remember {
        mutableStateOf(TextFieldValue(text = "", selection = TextRange(0)))
    }
    var dateOfBirth by remember { mutableStateOf("") }  // String để gửi lên thẻ

    var phoneNumber by remember { mutableStateOf("") }
    var selectedImage by remember { mutableStateOf<ImageBitmap?>(null) }
    var imageData by remember { mutableStateOf<ByteArray?>(null) }
    var isWriting by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("") }
    var uploadProgress by remember { mutableStateOf(0f) }

    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

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
            // ✅ HEADER GRADIENT ĐẸP
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(16.dp, RoundedCornerShape(32.dp)),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color. Transparent
                )
            ) {
                Box(
                    modifier = Modifier
                        . fillMaxWidth()
                        . background(
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
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(Color. White. copy(alpha = 0.25f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color. White,
                                modifier = Modifier. size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "✨ Ghi Thông Tin Khách Hàng",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color. White
                            )
                            Spacer(modifier = Modifier. height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🏷️", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Mã hôm nay: $datePrefix",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White. copy(alpha = 0.95f)
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("✍️", fontSize = 32.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier. height(24.dp))

            // ✅ FORM CARD
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .shadow(12.dp, RoundedCornerShape(32.dp)),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(32.dp)
                ) {
                    // ✅ PHOTO SECTION
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
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement. Center
                            ) {
                                Text("📸", fontSize = 24.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Ảnh khách hàng",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF333333)
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Box(
                                modifier = Modifier
                                    .size(180.dp)
                                    . shadow(8.dp, CircleShape)
                                    . clip(CircleShape)
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
                                if (selectedImage != null) {
                                    Image(
                                        bitmap = selectedImage!!,
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
                                            modifier = Modifier.size(72.dp),
                                            tint = Color. White
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Chưa có ảnh",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color.White
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = {
                                    val dialog = FileDialog(null as Frame?, "Chọn ảnh", FileDialog.LOAD)
                                    dialog.setFilenameFilter { _, name ->
                                        name.lowercase().endsWith(".jpg") ||
                                                name.lowercase().endsWith(".jpeg") ||
                                                name.lowercase().endsWith(".png")
                                    }
                                    dialog.isVisible = true

                                    val dir = dialog.directory
                                    val file = dialog.file

                                    if (dir != null && file != null) {
                                        scope.launch {
                                            try {
                                                val imageFile = File(dir, file)
                                                val bufferedImage = ImageIO.read(imageFile)

                                                val maxWidth = 200
                                                val maxHeight = 200
                                                val scaledImage = if (bufferedImage.width > maxWidth || bufferedImage.height > maxHeight) {
                                                    val scale = minOf(
                                                        maxWidth.toFloat() / bufferedImage.width,
                                                        maxHeight.toFloat() / bufferedImage.height
                                                    )
                                                    val newWidth = (bufferedImage. width * scale).toInt()
                                                    val newHeight = (bufferedImage.height * scale).toInt()

                                                    val scaled = java.awt.image.BufferedImage(newWidth, newHeight, bufferedImage.type)
                                                    val g = scaled.createGraphics()
                                                    g.drawImage(bufferedImage, 0, 0, newWidth, newHeight, null)
                                                    g.dispose()
                                                    scaled
                                                } else {
                                                    bufferedImage
                                                }

                                                val outputStream = ByteArrayOutputStream()
                                                ImageIO.write(scaledImage, "jpg", outputStream)
                                                val bytes = outputStream.toByteArray()

                                                if (bytes.size > 8000) {
                                                    var quality = 0.7f
                                                    var compressedBytes = bytes

                                                    while (compressedBytes.size > 8000 && quality > 0.1f) {
                                                        val baos = ByteArrayOutputStream()
                                                        val writer = ImageIO.getImageWritersByFormatName("jpg").next()
                                                        val ios = ImageIO.createImageOutputStream(baos)
                                                        writer.output = ios

                                                        val param = writer.defaultWriteParam
                                                        param.compressionMode = javax.imageio.ImageWriteParam.MODE_EXPLICIT
                                                        param.compressionQuality = quality

                                                        writer.write(null, javax.imageio.IIOImage(scaledImage, null, null), param)
                                                        writer. dispose()
                                                        ios.close()

                                                        compressedBytes = baos.toByteArray()
                                                        quality -= 0.1f
                                                    }

                                                    if (compressedBytes.size > 8000) {
                                                        status = "❌ Ảnh quá lớn!  Vui lòng chọn ảnh khác."
                                                        return@launch
                                                    }

                                                    imageData = compressedBytes
                                                } else {
                                                    imageData = bytes
                                                }

                                                selectedImage = scaledImage. toComposeImageBitmap()
                                                status = "✅ Đã chọn ảnh thành công!"

                                            } catch (e:  Exception) {
                                                status = "❌ Lỗi đọc ảnh: ${e. message}"
                                                e. printStackTrace()
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    . height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF667EEA)
                                ),
                                elevation = ButtonDefaults.buttonElevation(
                                    defaultElevation = 4.dp,
                                    pressedElevation = 8.dp
                                )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement. Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Upload,
                                        contentDescription = null,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = if (selectedImage == null) "📁 Chọn ảnh từ máy" else "🔄 Đổi ảnh",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier. height(28.dp))

                    Divider(
                        color = Color(0xFFE0E0E0),
                        thickness = 2.dp,
                        modifier = Modifier. padding(vertical = 8.dp)
                    )

                    Spacer(modifier = Modifier. height(20.dp))

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

                    Spacer(modifier = Modifier. height(20.dp))

                    // MÃ KHÁCH HÀNG
                    OutlinedTextField(
                        value = customerSuffix,
                        onValueChange = {
                            if (it.all { c -> c.isDigit() }) {
                                customerSuffix = it
                                customerID = datePrefix + it
                            }
                        },
                        label = { Text("Mã khách hàng", fontWeight = FontWeight.Medium) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Badge,
                                contentDescription = null,
                                tint = Color(0xFF667EEA)
                            )
                        },
                        prefix = {
                            Text(
                                text = datePrefix,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color(0xFF667EEA)
                            )
                        },
                        placeholder = { Text("001", color = Color. Gray) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF667EEA),
                            focusedLabelColor = Color(0xFF667EEA),
                            focusedLeadingIconColor = Color(0xFF667EEA),
                            cursorColor = Color(0xFF667EEA)
                        )
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // HỌ VÀ TÊN
                    OutlinedTextField(
                        value = name,
                        onValueChange = { if (it.length <= 50) name = it },
                        label = { Text("Họ và tên", fontWeight = FontWeight.Medium) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color(0xFF667EEA)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF667EEA),
                            focusedLabelColor = Color(0xFF667EEA),
                            focusedLeadingIconColor = Color(0xFF667EEA),
                            cursorColor = Color(0xFF667EEA)
                        )
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // ✅ NGÀY SINH - ĐÃ SỬA CURSOR
                    OutlinedTextField(
                        value = dateOfBirthState,
                        onValueChange = { newValue ->
                            val digitsOnly = newValue.text.filter { it.isDigit() }

                            if (digitsOnly. length <= 8) {
                                val formatted = when {
                                    digitsOnly.isEmpty() -> ""
                                    digitsOnly.length <= 2 -> digitsOnly
                                    digitsOnly.length <= 4 -> "${digitsOnly.take(2)}/${digitsOnly.drop(2)}"
                                    else -> "${digitsOnly.take(2)}/${digitsOnly.substring(2, 4)}/${digitsOnly.drop(4)}"
                                }

                                // ✅ Đặt cursor ở cuối chuỗi
                                dateOfBirthState = TextFieldValue(
                                    text = formatted,
                                    selection = TextRange(formatted.length)
                                )
                                dateOfBirth = formatted  // Lưu String để gửi lên thẻ
                            }
                        },
                        label = { Text("Ngày sinh", fontWeight = FontWeight.Medium) },
                        placeholder = { Text("13/12/2025", color = Color.Gray) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = Color(0xFF667EEA)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF667EEA),
                            focusedLabelColor = Color(0xFF667EEA),
                            focusedLeadingIconColor = Color(0xFF667EEA),
                            cursorColor = Color(0xFF667EEA)
                        ),
                        supportingText = {
                            Text(
                                text = "💡 Nhập số, tự động thêm /",
                                fontSize = 12.sp,
                                color = Color(0xFF9575CD)
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // SỐ ĐIỆN THOẠI
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = {
                            if (it.length <= 10 && it.all { c -> c.isDigit() })
                                phoneNumber = it
                        },
                        label = { Text("Số điện thoại", fontWeight = FontWeight.Medium) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = null,
                                tint = Color(0xFF667EEA)
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF667EEA),
                            focusedLabelColor = Color(0xFF667EEA),
                            focusedLeadingIconColor = Color(0xFF667EEA),
                            cursorColor = Color(0xFF667EEA)
                        )
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    // PROGRESS BAR
                    if (isWriting && uploadProgress > 0f) {
                        Card(
                            modifier = Modifier. fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFF5F5F5)
                            ),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment. CenterVertically) {
                                        Text("⏳", fontSize = 20.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Đang tải lên.. .",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF666666)
                                        )
                                    }
                                    Text(
                                        text = "${(uploadProgress * 100).toInt()}%",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF667EEA)
                                    )
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                                LinearProgressIndicator(
                                    progress = { uploadProgress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(10.dp)
                                        . clip(RoundedCornerShape(5.dp)),
                                    color = Color(0xFF667EEA),
                                    trackColor = Color(0xFFE0E0E0)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    // BUTTON GHI
                    Button(
                        onClick = {
                            scope.launch {
                                isWriting = true
                                status = ""
                                uploadProgress = 0f

                                try {
                                    val writeSuccess = smartCardManager.writeCustomerInfo(
                                        customerID, name, dateOfBirth, phoneNumber
                                    )

                                    if (! writeSuccess) {
                                        status = "❌ Lỗi ghi thông tin cơ bản"
                                        isWriting = false
                                        return@launch
                                    }

                                    status = "✅ Đã ghi thông tin cơ bản..."
                                    delay(500)

                                    imageData?. let { data ->
                                        status = "📤 Đang upload ảnh..."
                                        uploadProgress = 0.1f

                                        if (! smartCardManager.startPhotoWrite()) {
                                            status = "❌ Lỗi khởi tạo upload ảnh"
                                            isWriting = false
                                            return@launch
                                        }

                                        delay(200)
                                        uploadProgress = 0.2f

                                        val chunkSize = 200
                                        var offset = 0
                                        val totalChunks = (data.size + chunkSize - 1) / chunkSize

                                        var chunkIndex = 0
                                        while (offset < data.size) {
                                            val end = minOf(offset + chunkSize, data.size)
                                            val chunk = data.copyOfRange(offset, end)

                                            val success = smartCardManager.writePhotoChunk(chunk)
                                            if (!success) {
                                                status = "❌ Lỗi upload chunk ${chunkIndex + 1}/$totalChunks"
                                                isWriting = false
                                                return@launch
                                            }

                                            offset = end
                                            chunkIndex++
                                            uploadProgress = 0.2f + (chunkIndex. toFloat() / totalChunks) * 0.7f
                                            delay(50)
                                        }

                                        uploadProgress = 0.9f
                                        delay(200)

                                        val finishCmd = byteArrayOf(0x80. toByte(), 0x06, 0x00, 0x00, 0x00)
                                        smartCardManager.sendCommand(finishCmd)

                                        uploadProgress = 1.0f
                                        delay(300)

                                        status = "✅ Upload ảnh thành công!"
                                    } ?: run {
                                        status = "✅ Ghi thông tin thành công!"
                                    }

                                    delay(1000)
                                    status = "✅ Hoàn tất!  Đã ghi ${if (imageData != null) "thông tin + ảnh" else "thông tin"}"

                                } catch (e: Exception) {
                                    status = "❌ Lỗi:  ${e.message}"
                                    e.printStackTrace()
                                } finally {
                                    isWriting = false
                                    uploadProgress = 0f
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        enabled = ! isWriting &&
                                customerID.isNotEmpty() &&
                                name.isNotEmpty() &&
                                dateOfBirth. isNotEmpty() &&
                                phoneNumber.isNotEmpty(),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50),
                            disabledContainerColor = Color(0xFFBDBDBD)
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 6.dp,
                            pressedElevation = 10.dp
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement. Center
                        ) {
                            if (isWriting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(26.dp),
                                    color = Color. White,
                                    strokeWidth = 3.dp
                                )
                                Spacer(modifier = Modifier.width(14.dp))
                                Text(
                                    text = "Đang xử lý...",
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Save,
                                    contentDescription = null,
                                    modifier = Modifier.size(26.dp)
                                )
                                Spacer(modifier = Modifier. width(12.dp))
                                Text(
                                    text = "💾 Ghi vào thẻ",
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // STATUS
            if (status.isNotEmpty()) {
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
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when {
                                status.startsWith("✅") -> "✅"
                                status.startsWith("⚠️") -> "⚠️"
                                else -> "❌"
                            },
                            fontSize = 28.sp
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = status. substring(2),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = when {
                                status.startsWith("✅") -> Color(0xFF4CAF50)
                                status.startsWith("⚠️") -> Color(0xFFFFA726)
                                else -> Color(0xFFE53935)
                            },
                            modifier = Modifier. weight(1f)
                        )
                    }
                }
            }
        }
    }
}

private fun BufferedImage.toComposeImageBitmap(): ImageBitmap {
    val baos = ByteArrayOutputStream()
    ImageIO.write(this, "PNG", baos)
    val bytes = baos.toByteArray()
    return org.jetbrains.skia.Image.makeFromEncoded(bytes).toComposeImageBitmap()
}