package org.example. project.screen.admin

import androidx.compose.foundation.Image
import androidx.compose.foundation. background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose. foundation.shape.RoundedCornerShape
import androidx.compose. foundation.text.KeyboardOptions
import androidx.compose.foundation. verticalScroll
import androidx. compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui. Modifier
import androidx.compose.ui.draw.clip
import androidx. compose.ui.graphics. Brush
import androidx.compose.ui.graphics.Color
import androidx. compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextRange
import androidx.compose.ui. text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose. ui.text.input.TextFieldValue
import androidx. compose.ui.unit.dp
import androidx.compose. ui.unit.sp
import kotlinx.coroutines.launch
import org.example.project. CardType
import org.example.project. SmartCardManager
import org. example.project.screen.FloatingBubbles
import org.example.project.screen.ValidationState
import org.example.project.screen.ValidationUtils
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import java.awt.image.BufferedImage
import java.io. ByteArrayOutputStream
import java. text.SimpleDateFormat
import java. util.*
import javax.imageio.ImageIO
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

@OptIn(ExperimentalMaterial3Api:: class)
@Composable
fun AdminWriteInfoScreen(
    smartCardManager: SmartCardManager,
    onBack: () -> Unit
) {
    var customerID by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var dateOfBirth by remember { mutableStateOf(TextFieldValue("")) }
    var phoneNumber by remember { mutableStateOf("") }
    var cardType by remember { mutableStateOf("THUONG") }
    var selectedImageBytes by remember { mutableStateOf<ByteArray?>(null) }
    var writeStatus by remember { mutableStateOf("") }
    var isWriting by remember { mutableStateOf(false) }

    // Validation states
    var customerIDValidation by remember { mutableStateOf(ValidationState()) }
    var nameValidation by remember { mutableStateOf(ValidationState()) }
    var dateValidation by remember { mutableStateOf(ValidationState()) }
    var phoneValidation by remember { mutableStateOf(ValidationState()) }
    var imageValidation by remember { mutableStateOf(ValidationState()) }

    val scope = rememberCoroutineScope()

    val isFormValid = customerIDValidation.isValid &&
            nameValidation.isValid &&
            dateValidation.isValid &&
            phoneValidation.isValid &&
            imageValidation.isValid &&
            customerID.isNotEmpty() &&
            name.isNotEmpty() &&
            dateOfBirth.text.isNotEmpty() &&
            phoneNumber.isNotEmpty() &&
            selectedImageBytes != null

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
                colors = CardDefaults.cardColors(containerColor = Color(0xFF81C784)),
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
                            containerColor = Color. White. copy(alpha = 0.2f),
                            contentColor = Color. White
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier. size(48.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("←", fontSize = 20.sp)
                    }

                    Spacer(modifier = Modifier. width(16.dp))

                    Text(
                        text = "✍️ Ghi Thông Tin Khách Hàng",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier. height(16.dp))

            // Form
            Column(
                modifier = Modifier
                    .weight(1f)
                    . verticalScroll(rememberScrollState())
            ) {
                Card(
                    modifier = Modifier. fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color. White),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        // Customer ID
                        OutlinedTextField(
                            value = customerID,
                            onValueChange = {
                                customerID = it
                                customerIDValidation = ValidationUtils.validateCustomerID(it)
                            },
                            label = { Text("Mã khách hàng") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            isError = ! customerIDValidation.isValid,
                            supportingText = {
                                if (!customerIDValidation.isValid) {
                                    Text(customerIDValidation.errorMessage, color = Color.Red)
                                }
                            },
                            singleLine = true
                        )

                        Spacer(modifier = Modifier. height(12.dp))

                        // Name
                        OutlinedTextField(
                            value = name,
                            onValueChange = {
                                name = it
                                nameValidation = ValidationUtils.validateName(it)
                            },
                            label = { Text("Họ tên") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            isError = ! nameValidation.isValid,
                            supportingText = {
                                if (!nameValidation. isValid) {
                                    Text(nameValidation.errorMessage, color = Color.Red)
                                }
                            },
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Date of Birth
                        // Date of Birth
                        OutlinedTextField(
                            value = dateOfBirth,
                            onValueChange = {
                                val newText = it.text
                                if (newText.length <= 10) {
                                    // Format tự động thêm dấu "/"
                                    val formatted = when (newText.length) {
                                        2 -> if (! newText.endsWith("/")) "$newText/" else newText
                                        5 -> if (newText. count { it == '/' } == 1 && !newText. endsWith("/")) "$newText/" else newText
                                        else -> newText
                                    }

                                    dateOfBirth = TextFieldValue(formatted, TextRange(formatted. length))
                                    dateValidation = ValidationUtils.validateDateOfBirth(formatted)
                                }
                            },
                            label = { Text("Ngày sinh (DD/MM/YYYY)") },
                            modifier = Modifier. fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = ! dateValidation.isValid,
                            supportingText = {
                                if (! dateValidation.isValid) {
                                    Text(dateValidation.errorMessage, color = Color.Red)
                                }
                            },
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Phone Number
                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = {
                                phoneNumber = it
                                phoneValidation = ValidationUtils.validatePhoneNumber(it)
                            },
                            label = { Text("Số điện thoại") },
                            modifier = Modifier. fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            isError = ! phoneValidation.isValid,
                            supportingText = {
                                if (!phoneValidation. isValid) {
                                    Text(phoneValidation.errorMessage, color = Color.Red)
                                }
                            },
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Card Type
                        Text(
                            text = "Loại thẻ",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier. height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CardType.values().forEach { type ->
                                FilterChip(
                                    selected = cardType == type. value,
                                    onClick = { cardType = type.value },
                                    label = { Text(type.displayName, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF81C784)
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Image Upload
                        Button(
                            onClick = {
                                val fileChooser = JFileChooser()
                                fileChooser.fileFilter = FileNameExtensionFilter(
                                    "Image files", "jpg", "jpeg", "png"
                                )
                                if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                                    try {
                                        val file = fileChooser.selectedFile
                                        val bufferedImage = ImageIO.read(file)
                                        val outputStream = ByteArrayOutputStream()
                                        ImageIO.write(bufferedImage, "jpg", outputStream)
                                        val bytes = outputStream.toByteArray()
                                        selectedImageBytes = bytes
                                        imageValidation = ValidationUtils.validateImage(bytes)
                                    } catch (e: Exception) {
                                        imageValidation = ValidationState(false, "Lỗi đọc ảnh:  ${e.message}")
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF64B5F6)
                            )
                        ) {
                            Text("📷 Chọn ảnh")
                        }

                        // Image Preview
                        selectedImageBytes?.let { bytes ->
                            Spacer(modifier = Modifier. height(16.dp))
                            val imageBitmap = remember(bytes) {
                                Image. makeFromEncoded(bytes).toComposeImageBitmap()
                            }
                            androidx.compose.foundation.Image(
                                bitmap = imageBitmap,
                                contentDescription = "Selected Image",
                                modifier = Modifier
                                    .size(150.dp)
                                    . clip(CircleShape)
                                    .align(Alignment.CenterHorizontally),
                                contentScale = ContentScale.Crop
                            )
                        }

                        if (! imageValidation.isValid) {
                            Text(
                                imageValidation.errorMessage,
                                color = Color.Red,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Write Button
                        Button(
                            onClick = {
                                if (isFormValid) {
                                    scope.launch {
                                        isWriting = true
                                        writeStatus = "Đang ghi dữ liệu..."

                                        try {
                                            val infoWritten = smartCardManager.writeCustomerInfo(
                                                customerID, name, dateOfBirth. text, phoneNumber, cardType
                                            )

                                            if (infoWritten) {
                                                val imageWritten = smartCardManager.writeCustomerImage(
                                                    selectedImageBytes!!
                                                )

                                                writeStatus = if (imageWritten) {
                                                    "✅ Ghi thành công!"
                                                } else {
                                                    "⚠️ Ghi thông tin OK nhưng ảnh lỗi"
                                                }
                                            } else {
                                                writeStatus = "❌ Lỗi ghi thông tin"
                                            }
                                        } catch (e: Exception) {
                                            writeStatus = "❌ Lỗi:  ${e.message}"
                                        } finally {
                                            isWriting = false
                                        }
                                    }
                                } else {
                                    writeStatus = "❌ Vui lòng kiểm tra lại thông tin đã nhập"
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = ! isWriting,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isFormValid) Color(0xFF81C784) else Color(0xFFBDBDBD)
                            )
                        ) {
                            if (isWriting) {
                                CircularProgressIndicator(
                                    modifier = Modifier. size(16.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Đang ghi.. .", fontSize = 14.sp, color = Color.White)
                            } else {
                                Text("💾 Ghi vào thẻ", fontSize = 14.sp, color = Color.White)
                            }
                        }

                        // Clear Button
                        OutlinedButton(
                            onClick = {
                                customerID = ""
                                name = ""
                                dateOfBirth = TextFieldValue("")
                                phoneNumber = ""
                                cardType = "THUONG"
                                selectedImageBytes = null
                                writeStatus = ""
                                customerIDValidation = ValidationState()
                                nameValidation = ValidationState()
                                dateValidation = ValidationState()
                                phoneValidation = ValidationState()
                                imageValidation = ValidationState()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("🗑️ Xóa form")
                        }
                    }
                }

                // Status Message
                if (writeStatus.isNotEmpty()) {
                    Spacer(modifier = Modifier. height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (writeStatus.startsWith("✅"))
                                Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                        )
                    ) {
                        Text(
                            text = writeStatus,
                            modifier = Modifier.padding(16.dp),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}