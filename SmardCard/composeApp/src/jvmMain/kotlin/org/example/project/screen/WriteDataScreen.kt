//package org.example.project.screen
//
//import androidx.compose.foundation.BorderStroke
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.PaddingValues
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.layout.width
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.foundation.text.KeyboardOptions
//import androidx.compose.material3.Button
//import androidx.compose.material3.ButtonDefaults
//import androidx.compose.material3.Card
//import androidx.compose.material3.CardDefaults
//import androidx.compose.material3.CircularProgressIndicator
//import androidx.compose.material3.ExperimentalMaterial3Api
//import androidx.compose.material3.OutlinedTextField
//import androidx.compose.material3.OutlinedTextFieldDefaults
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.rememberCoroutineScope
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Brush
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.ImageBitmap
//import androidx.compose.ui.graphics.toComposeImageBitmap
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.text.TextRange
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.input.KeyboardType
//import androidx.compose.ui.text.input.TextFieldValue
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import kotlinx.coroutines.launch
//import org.example.project.SmartCardManager
//import org.jetbrains.skia.EncodedImageFormat
//
//import java.awt.image.BufferedImage
//import java.io.ByteArrayOutputStream
//import javax.imageio.ImageIO
//import org.jetbrains.skia.Image
//import java.text.SimpleDateFormat
//import java.util.Calendar
//import java.util.Locale
//
//data class ValidationState(
//    val isValid: Boolean = true,
//    val errorMessage: String = ""
//)
//
//// Object chứa các hàm validation
//object ValidationUtils {
//
//    fun validateCustomerID(customerID: String): ValidationState {
//        return when {
//            customerID.isEmpty() -> ValidationState(false, "Mã khách hàng không được để trống")
//            customerID.length < 3 -> ValidationState(false, "Mã khách hàng phải ít nhất 3 ký tự")
//            customerID.length > 15 -> ValidationState(false, "Mã khách hàng không được quá 15 ký tự")
//            !customerID.matches(Regex("^[A-Za-z0-9]+$")) -> ValidationState(false, "Mã khách hàng chỉ được chứa chữ cái và số")
//            else -> ValidationState(true, "")
//        }
//    }
//
//    fun validateName(name: String): ValidationState {
//        return when {
//            name.isEmpty() -> ValidationState(false, "Họ tên không được để trống")
//            name.trim().length < 2 -> ValidationState(false, "Họ tên phải ít nhất 2 ký tự")
//            name.length > 50 -> ValidationState(false, "Họ tên không được quá 50 ký tự")
//            !name.matches(Regex("^[a-zA-ZÀ-ỹ\\s]+$")) -> ValidationState(false, "Họ tên chỉ được chứa chữ cái và khoảng trắng")
//            name.trim().split("\\s+".toRegex()).size < 2 -> ValidationState(false, "Vui lòng nhập đầy đủ họ và tên")
//            else -> ValidationState(true, "")
//        }
//    }
//
//    fun validateDateOfBirth(dateStr: String): ValidationState {
//        return when {
//            dateStr.isEmpty() -> ValidationState(false, "Ngày sinh không được để trống")
//            !dateStr.matches(Regex("^\\d{2}/\\d{2}/\\d{4}$")) -> ValidationState(false, "Định dạng ngày sinh: dd/mm/yyyy")
//            else -> {
//                try {
//                    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
//                    formatter.isLenient = false
//                    val date = formatter.parse(dateStr)
//                    val calendar = Calendar.getInstance()
//                    calendar.time = date!!
//
//                    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
//                    val birthYear = calendar.get(Calendar.YEAR)
//                    val age = currentYear - birthYear
//
//                    when {
//                        age < 16 -> ValidationState(false, "Khách hàng phải từ 16 tuổi trở lên")
//                        age > 100 -> ValidationState(false, "Tuổi không hợp lệ")
//                        else -> ValidationState(true, "")
//                    }
//                } catch (e: Exception) {
//                    ValidationState(false, "Ngày sinh không hợp lệ")
//                }
//            }
//        }
//    }
//
//    fun validatePhoneNumber(phoneNumber: String): ValidationState {
//        return when {
//            phoneNumber.isEmpty() -> ValidationState(false, "Số điện thoại không được để trống")
//            !phoneNumber.matches(Regex("^0\\d{9}$")) -> ValidationState(false, "Số điện thoại phải có 10 số và bắt đầu bằng 0")
//            !phoneNumber.matches(Regex("^(032|033|034|035|036|037|038|039|096|097|098|086|083|084|085|081|082|088|091|094|070|079|077|076|078|090|093|089|056|058|092|059|099)[0-9]{7}$")) ->
//                ValidationState(false, "Đầu số điện thoại không hợp lệ")
//            else -> ValidationState(true, "")
//        }
//    }
//
//    fun validateImage(imageBytes: ByteArray?): ValidationState {
//        return when {
//            imageBytes == null -> ValidationState(false, "Vui lòng chọn ảnh khách hàng")
//            imageBytes.size > 8192 -> ValidationState(false, "Kích thước ảnh quá lớn (tối đa 8KB)")
//            else -> ValidationState(true, "")
//        }
//    }
//
//    fun formatDateInput(input: String): String {
//        val cleaned = input.replace(Regex("[^\\d]"), "")
//        return when {
//            cleaned.length <= 2 -> cleaned
//            cleaned.length <= 4 -> "${cleaned.substring(0, 2)}/${cleaned.substring(2)}"
//            cleaned.length <= 8 -> "${cleaned.substring(0, 2)}/${cleaned.substring(2, 4)}/${cleaned.substring(4)}"
//            else -> "${cleaned.substring(0, 2)}/${cleaned.substring(2, 4)}/${cleaned.substring(4, 8)}"
//        }
//    }
//}
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun WriteDataScreen(
//    smartCardManager: SmartCardManager,
//    onBack: () -> Unit
//) {
//    var customerID by remember { mutableStateOf("") }
//    var name by remember { mutableStateOf("") }
//    var dateOfBirth by remember { mutableStateOf(TextFieldValue("")) }
//    var phoneNumber by remember { mutableStateOf("") }
//    var cardType by remember { mutableStateOf("THUONG") }
//    var selectedImageBytes by remember { mutableStateOf<ByteArray?>(null) }
//    var writeStatus by remember { mutableStateOf("") }
//    var isWriting by remember { mutableStateOf(false) }
//
//    // Validation states
//    var customerIDValidation by remember { mutableStateOf(ValidationState()) }
//    var nameValidation by remember { mutableStateOf(ValidationState()) }
//    var dateValidation by remember { mutableStateOf(ValidationState()) }
//    var phoneValidation by remember { mutableStateOf(ValidationState()) }
//    var imageValidation by remember { mutableStateOf(ValidationState()) }
//    var showValidationErrors by remember { mutableStateOf(false) }
//
//    val scope = rememberCoroutineScope()
//    val cardTypes = listOf("THUONG", "VANG", "BACHKIM", "KIMCUONG")
//
//    // Hàm validate realtime
//    fun validateField(field: String, value: String, imageBytes: ByteArray? = null) {
//        when (field) {
//            "customerID" -> customerIDValidation = ValidationUtils.validateCustomerID(value)
//            "name" -> nameValidation = ValidationUtils.validateName(value)
//            "dateOfBirth" -> dateValidation = ValidationUtils.validateDateOfBirth(value)
//            "phoneNumber" -> phoneValidation = ValidationUtils.validatePhoneNumber(value)
//            "image" -> imageValidation = ValidationUtils.validateImage(imageBytes)
//        }
//    }
//
//    // Check if form is valid
//    val isFormValid = customerIDValidation.isValid &&
//            nameValidation.isValid &&
//            dateValidation.isValid &&
//            phoneValidation.isValid &&
//            imageValidation.isValid &&
//            customerID.isNotEmpty() &&
//            name.isNotEmpty() &&
//            dateOfBirth.text.isNotEmpty() &&
//            phoneNumber.isNotEmpty() &&
//            selectedImageBytes != null
//
//    // Background
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
//                                Text("✍️", fontSize = 20.sp)
//                            }
//
//                            Spacer(modifier = Modifier.width(12.dp))
//
//                            Column {
//                                Text(
//                                    text = "Ghi thông tin khách hàng",
//                                    fontSize = 18.sp,
//                                    fontWeight = FontWeight.Bold,
//                                    color = Color.White
//                                )
//                                Text(
//                                    text = "Nhập thông tin để lưu vào thẻ",
//                                    fontSize = 12.sp,
//                                    color = Color.White.copy(0.9f)
//                                )
//                            }
//                        }
//                    }
//                }
//            }
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.spacedBy(16.dp)
//            ) {
//                Card(
//                    modifier = Modifier.weight(2f),
//                    shape = RoundedCornerShape(20.dp),
//                    colors = CardDefaults.cardColors(containerColor = Color.White),
//                    elevation = CardDefaults.cardElevation(6.dp)
//                ) {
//                    Column(modifier = Modifier.padding(20.dp)) {
//                        Text(
//                            text = "👤 Thông tin cá nhân",
//                            fontSize = 16.sp,
//                            fontWeight = FontWeight.Bold,
//                            color = Color(0xFF333333)
//                        )
//
//                        Spacer(modifier = Modifier.height(16.dp))
//
//                        // Customer ID Field
//                        OutlinedTextField(
//                            value = customerID,
//                            onValueChange = {
//                                customerID = it.take(15).uppercase()
//                                validateField("customerID", customerID)
//                            },
//                            label = { Text("Mã khách hàng") },
//                            modifier = Modifier.fillMaxWidth(),
//                            singleLine = true,
//                            shape = RoundedCornerShape(12.dp),
//                            isError = showValidationErrors && !customerIDValidation.isValid,
//                            colors = OutlinedTextFieldDefaults.colors(
//                                focusedBorderColor = if (customerIDValidation.isValid) Color(0xFF81C784) else Color(0xFFE57373),
//                                unfocusedBorderColor = if (showValidationErrors && !customerIDValidation.isValid) Color(0xFFE57373) else Color(0xFFE0E0E0),
//                                errorBorderColor = Color(0xFFE57373)
//                            )
//                        )
//                        if (showValidationErrors && !customerIDValidation.isValid) {
//                            Text(
//                                text = customerIDValidation.errorMessage,
//                                color = Color(0xFFE57373),
//                                fontSize = 12.sp,
//                                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
//                            )
//                        }
//
//                        Spacer(modifier = Modifier.height(12.dp))
//
//                        // Name Field
//                        OutlinedTextField(
//                            value = name,
//                            onValueChange = {
//                                name = it.take(50)
//                                validateField("name", name)
//                            },
//                            label = { Text("Họ và tên") },
//                            modifier = Modifier.fillMaxWidth(),
//                            singleLine = true,
//                            shape = RoundedCornerShape(12.dp),
//                            isError = showValidationErrors && !nameValidation.isValid,
//                            colors = OutlinedTextFieldDefaults.colors(
//                                focusedBorderColor = if (nameValidation.isValid) Color(0xFF64B5F6) else Color(0xFFE57373),
//                                unfocusedBorderColor = if (showValidationErrors && !nameValidation.isValid) Color(0xFFE57373) else Color(0xFFE0E0E0),
//                                errorBorderColor = Color(0xFFE57373)
//                            )
//                        )
//                        if (showValidationErrors && !nameValidation.isValid) {
//                            Text(
//                                text = nameValidation.errorMessage,
//                                color = Color(0xFFE57373),
//                                fontSize = 12.sp,
//                                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
//                            )
//                        }
//
//                        Spacer(modifier = Modifier.height(12.dp))
//
//                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
//                            // Date of Birth Field với auto-format và cursor positioning
//                            Column(modifier = Modifier.weight(1f)) {
//                                OutlinedTextField(
//                                    value = dateOfBirth,
//                                    onValueChange = { newValue ->
//                                        val input = newValue.text
//                                        val currentText = dateOfBirth.text
//
//                                        // Chỉ format khi user đang gõ thêm
//                                        val formatted = if (input.length >= currentText.length) {
//                                            ValidationUtils.formatDateInput(input)
//                                        } else {
//                                            input // Cho phép xóa bình thường
//                                        }
//
//                                        // Update với cursor ở cuối
//                                        dateOfBirth = TextFieldValue(
//                                            text = formatted,
//                                            selection = TextRange(formatted.length)
//                                        )
//
//                                        if (formatted.length == 10) {
//                                            validateField("dateOfBirth", formatted)
//                                        }
//                                    },
//                                    label = { Text("Ngày sinh") },
//                                    placeholder = { Text("dd/mm/yyyy") },
//                                    singleLine = true,
//                                    shape = RoundedCornerShape(12.dp),
//                                    isError = showValidationErrors && !dateValidation.isValid,
//                                    colors = OutlinedTextFieldDefaults.colors(
//                                        focusedBorderColor = if (dateValidation.isValid) Color(0xFFFFB74D) else Color(0xFFE57373),
//                                        unfocusedBorderColor = if (showValidationErrors && !dateValidation.isValid) Color(0xFFE57373) else Color(0xFFE0E0E0),
//                                        errorBorderColor = Color(0xFFE57373)
//                                    ),
//                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
//                                )
//                                if (showValidationErrors && !dateValidation.isValid) {
//                                    Text(
//                                        text = dateValidation.errorMessage,
//                                        color = Color(0xFFE57373),
//                                        fontSize = 10.sp,
//                                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
//                                    )
//                                }
//                            }
//
//                            // Phone Number Field
//                            Column(modifier = Modifier.weight(1f)) {
//                                OutlinedTextField(
//                                    value = phoneNumber,
//                                    onValueChange = {
//                                        phoneNumber = it.take(10)
//                                        validateField("phoneNumber", phoneNumber)
//                                    },
//                                    label = { Text("Số điện thoại") },
//                                    singleLine = true,
//                                    shape = RoundedCornerShape(12.dp),
//                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
//                                    isError = showValidationErrors && !phoneValidation.isValid,
//                                    colors = OutlinedTextFieldDefaults.colors(
//                                        focusedBorderColor = if (phoneValidation.isValid) Color(0xFFBA68C8) else Color(0xFFE57373),
//                                        unfocusedBorderColor = if (showValidationErrors && !phoneValidation.isValid) Color(0xFFE57373) else Color(0xFFE0E0E0),
//                                        errorBorderColor = Color(0xFFE57373)
//                                    )
//                                )
//                                if (showValidationErrors && !phoneValidation.isValid) {
//                                    Text(
//                                        text = phoneValidation.errorMessage,
//                                        color = Color(0xFFE57373),
//                                        fontSize = 10.sp,
//                                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
//                                    )
//                                }
//                            }
//                        }
//
//                        Spacer(modifier = Modifier.height(16.dp))
//
//                        // Card type selection
//                        Text(
//                            text = "💳 Loại thẻ",
//                            fontSize = 14.sp,
//                            fontWeight = FontWeight.Bold,
//                            color = Color(0xFF333333)
//                        )
//
//                        Spacer(modifier = Modifier.height(8.dp))
//
//                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
//                            cardTypes.forEach { type ->
//                                val isSelected = cardType == type
//                                val (emoji, color) = when (type) {
//                                    "THUONG" -> "🤍" to Color(0xFF81C784)
//                                    "VANG" -> "💛" to Color(0xFFFFB74D)
//                                    "BACHKIM" -> "🤍" to Color(0xFF64B5F6)
//                                    "KIMCUONG" -> "💎" to Color(0xFFBA68C8)
//                                    else -> "🤍" to Color.Gray
//                                }
//
//                                Card(
//                                    onClick = { cardType = type },
//                                    modifier = Modifier.weight(1f),
//                                    shape = RoundedCornerShape(12.dp),
//                                    colors = CardDefaults.cardColors(
//                                        containerColor = if (isSelected) color.copy(alpha = 0.15f) else Color(0xFFFAFAFA)
//                                    ),
//                                    border = if (isSelected) BorderStroke(2.dp, color) else BorderStroke(1.dp, Color(0xFFE0E0E0))
//                                ) {
//                                    Column(
//                                        modifier = Modifier.padding(12.dp),
//                                        horizontalAlignment = Alignment.CenterHorizontally
//                                    ) {
//                                        Text(emoji, fontSize = 18.sp)
//                                        Text(
//                                            text = type.take(4),
//                                            fontSize = 10.sp,
//                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
//                                            color = if (isSelected) color else Color(0xFF666666)
//                                        )
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//
//                // Right Column - Image & Actions
//                Column(modifier = Modifier.weight(1f)) {
//                    // Image Section
//                    Card(
//                        modifier = Modifier.fillMaxWidth(),
//                        shape = RoundedCornerShape(20.dp),
//                        colors = CardDefaults.cardColors(containerColor = Color.White),
//                        elevation = CardDefaults.cardElevation(6.dp),
//                        border = if (showValidationErrors && !imageValidation.isValid) BorderStroke(1.dp, Color(0xFFE57373)) else null
//                    ) {
//                        Column(modifier = Modifier.padding(20.dp)) {
//                            Text(
//                                text = "📷 Ảnh khách hàng",
//                                fontSize = 14.sp,
//                                fontWeight = FontWeight.Bold,
//                                color = Color(0xFF333333)
//                            )
//
//                            Spacer(modifier = Modifier.height(16.dp))
//
//                            if (selectedImageBytes != null) {
//                                Card(
//                                    modifier = Modifier
//                                        .fillMaxWidth()
//                                        .height(200.dp),
//                                    shape = RoundedCornerShape(12.dp),
//                                    elevation = CardDefaults.cardElevation(4.dp)
//                                ) {
//                                    selectedImageBytes?.toImageBitmap()?.let { bitmap ->
//                                        Image(
//                                            bitmap = bitmap,
//                                            contentDescription = null,
//                                            modifier = Modifier
//                                                .fillMaxWidth()
//                                                .height(200.dp)
//                                                .clip(RoundedCornerShape(12.dp)),
//                                            contentScale = ContentScale.Crop
//                                        )
//                                    }
//                                }
//                                // Hiển thị kích thước ảnh
//                                Text(
//                                    text = "Kích thước: ${selectedImageBytes!!.size} bytes",
//                                    fontSize = 10.sp,
//                                    color = Color(0xFF666666),
//                                    modifier = Modifier.padding(top = 4.dp)
//                                )
//                            } else {
//                                Box(
//                                    modifier = Modifier
//                                        .fillMaxWidth()
//                                        .height(140.dp)
//                                        .background(
//                                            if (showValidationErrors && !imageValidation.isValid)
//                                                Color(0xFFFFEBEE) else Color(0xFFF8F9FA),
//                                            RoundedCornerShape(12.dp)
//                                        ),
//                                    contentAlignment = Alignment.Center
//                                ) {
//                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                                        Text("📷", fontSize = 40.sp)
//                                        Text(
//                                            text = "Chưa chọn ảnh",
//                                            fontSize = 12.sp,
//                                            color = Color(0xFF666666)
//                                        )
//                                    }
//                                }
//                            }
//
//                            if (showValidationErrors && !imageValidation.isValid) {
//                                Text(
//                                    text = imageValidation.errorMessage,
//                                    color = Color(0xFFE57373),
//                                    fontSize = 12.sp,
//                                    modifier = Modifier.padding(top = 4.dp)
//                                )
//                            }
//
//                            Spacer(modifier = Modifier.height(12.dp))
//
//                            Button(
//                                onClick = {
//                                    val fileDialog = java.awt.FileDialog(null as java.awt.Frame?, "Chọn ảnh", java.awt.FileDialog.LOAD)
//                                    fileDialog.setFilenameFilter { _, name ->
//                                        name.lowercase().endsWith(".jpg") ||
//                                                name.lowercase().endsWith(".jpeg") ||
//                                                name.lowercase().endsWith(".png")
//                                    }
//                                    fileDialog.isVisible = true
//
//                                    if (fileDialog.file != null) {
//                                        try {
//                                            val file = java.io.File(fileDialog.directory, fileDialog.file)
//                                            selectedImageBytes = compressImage(file.toString())
//                                            validateField("image", "", selectedImageBytes)
//                                            writeStatus = "✅ Đã chọn ảnh thành công"
//                                        } catch (e: Exception) {
//                                            writeStatus = "❌ Lỗi khi xử lý ảnh: ${e.message}"
//                                            selectedImageBytes = null
//                                        }
//                                    }
//                                },
//                                modifier = Modifier.fillMaxWidth(),
//                                shape = RoundedCornerShape(12.dp),
//                                colors = ButtonDefaults.buttonColors(
//                                    containerColor = Color(0xFF64B5F6)
//                                )
//                            ) {
//                                Text("📁 Chọn ảnh", fontSize = 14.sp, color = Color.White)
//                            }
//                        }
//                    }
//
//                    Spacer(modifier = Modifier.height(12.dp))
//
//                    // Action Buttons
//                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
//                        Button(
//                            onClick = {
//                                // Validate tất cả các field
//                                validateField("customerID", customerID)
//                                validateField("name", name)
//                                validateField("dateOfBirth", dateOfBirth.text)
//                                validateField("phoneNumber", phoneNumber)
//                                validateField("image", "", selectedImageBytes)
//                                showValidationErrors = true
//
//                                if (isFormValid) {
//                                    scope.launch {
//                                        isWriting = true
//                                        writeStatus = "Đang ghi dữ liệu..."
//
//                                        try {
//                                            smartCardManager.writeCustomerInfo(customerID, name, dateOfBirth.text, phoneNumber, cardType)
//
//                                            selectedImageBytes?.let {
//                                                smartCardManager.writeCustomerImage(it)
//                                            }
//
//                                            writeStatus = "✅ Ghi thành công!"
//                                        } catch (e: Exception) {
//                                            writeStatus = "❌ Lỗi ghi dữ liệu: ${e.message}"
//                                        } finally {
//                                            isWriting = false
//                                        }
//                                    }
//                                } else {
//                                    writeStatus = "❌ Vui lòng kiểm tra lại thông tin đã nhập"
//                                }
//                            },
//                            modifier = Modifier.fillMaxWidth(),
//                            enabled = !isWriting,
//                            shape = RoundedCornerShape(12.dp),
//                            colors = ButtonDefaults.buttonColors(
//                                containerColor = if (isFormValid) Color(0xFF81C784) else Color(0xFFBDBDBD)
//                            )
//                        ) {
//                            if (isWriting) {
//                                CircularProgressIndicator(
//                                    modifier = Modifier.size(16.dp),
//                                    color = Color.White,
//                                    strokeWidth = 2.dp
//                                )
//                                Spacer(modifier = Modifier.width(8.dp))
//                                Text("Đang ghi...", fontSize = 14.sp, color = Color.White)
//                            } else {
//                                Text("💾 Ghi vào thẻ", fontSize = 14.sp, color = Color.White)
//                            }
//                        }
//
//                        Button(
//                            onClick = {
//                                customerID = ""
//                                name = ""
//                                dateOfBirth = TextFieldValue("")
//                                phoneNumber = ""
//                                cardType = "THUONG"
//                                selectedImageBytes = null
//                                writeStatus = ""
//                                showValidationErrors = false
//                                // Reset validation states
//                                customerIDValidation = ValidationState()
//                                nameValidation = ValidationState()
//                                dateValidation = ValidationState()
//                                phoneValidation = ValidationState()
//                                imageValidation = ValidationState()
//                            },
//                            modifier = Modifier.fillMaxWidth(),
//                            shape = RoundedCornerShape(12.dp),
//                            colors = ButtonDefaults.buttonColors(
//                                containerColor = Color(0xFFE57373)
//                            )
//                        ) {
//                            Text("🗑️ Xóa tất cả", fontSize = 14.sp, color = Color.White)
//                        }
//
//                        // Validation Summary Button
////                        if (showValidationErrors && !isFormValid) {
////                            Button(
////                                onClick = {
////                                    validateField("customerID", customerID)
////                                    validateField("name", name)
////                                    validateField("dateOfBirth", dateOfBirth.text)
////                                    validateField("phoneNumber", phoneNumber)
////                                    validateField("image", "", selectedImageBytes)
////                                },
////                                modifier = Modifier.fillMaxWidth(),
////                                shape = RoundedCornerShape(12.dp),
////                                colors = ButtonDefaults.buttonColors(
////                                    containerColor = Color(0xFFFF9800)
////                                )
////                            ) {
////                                Text("🔍 Kiểm tra lỗi", fontSize = 14.sp, color = Color.White)
////                            }
////                        }
//                    }
//                }
//            }
//
//            // Status
//            if (writeStatus.isNotEmpty()) {
//                Spacer(modifier = Modifier.height(12.dp))
//                Card(
//                    modifier = Modifier.fillMaxWidth(),
//                    shape = RoundedCornerShape(12.dp),
//                    colors = CardDefaults.cardColors(
//                        containerColor = when {
//                            writeStatus.contains("✅") -> Color(0xFF81C784)
//                            writeStatus.contains("❌") -> Color(0xFFE57373)
//                            else -> Color(0xFF64B5F6)
//                        }
//                    )
//                ) {
//                    Text(
//                        text = writeStatus,
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
//
//// Helper extension
//fun ByteArray.toImageBitmap(): ImageBitmap? {
//    return try {
//        val bufferedImage = ImageIO.read(this.inputStream())
//        bufferedImage?.toComposeImageBitmap()
//    } catch (e: Exception) {
//        null
//    }
//}
//
//
////fun compressImage(originalBytes: ByteArray): ByteArray {
////    return try {
////        // 1. Đọc ảnh gốc
////        val originalImage = ImageIO.read(originalBytes.inputStream())
////
////        // 2. Resize về 64x64 pixels
////        val resized = BufferedImage(64, 64, BufferedImage.TYPE_INT_RGB)
////        val g2d = resized.createGraphics()
////        g2d.drawImage(originalImage.getScaledInstance(64, 64, Image.SCALE_SMOOTH), 0, 0, null)
////        g2d.dispose()
////        val output = ByteArrayOutputStream()
////        val writers = ImageIO.getImageWritersByFormatName("jpg")
////        val writer = writers.next()
////        val ios = ImageIO.createImageOutputStream(output)
////
////        writer.output = ios
////        writer.write(resized)
////        writer.dispose()
////        ios.close()
////        output.toByteArray()
////
////    } catch (e: Exception) {
////        println("Lỗi nén ảnh: ${e.message}")
////        originalBytes // Trả về ảnh gốc nếu lỗi
////    }
////}
//fun compressImage(avatarPath: String, maxSize: Int = 8192): ByteArray {
//    val bytes = java.io.File(avatarPath).readBytes()
//    val skiaImage = Image.makeFromEncoded(bytes)
//
//    // Resize: dùng makeImageSnapshot vì Skia JVM không có withDimensions/ext!
//    val targetW = 128
//    val targetH = 128
//    val resized = skiaImage.resize(targetW, targetH)
//    // JPEG encode, quality 80
//    val jpegBytes = resized.encodeToData(EncodedImageFormat.JPEG, 64)!!.bytes
//    require(jpegBytes.size <= maxSize) { "Ảnh quá lớn, vui lòng chọn ảnh nhỏ hoặc thấp chất lượng hơn." }
//    return jpegBytes
//}
//
//// Extension resize cho org.jetbrains.skia.Image
//fun Image.resize(width: Int, height: Int): Image {
//    val scaled = this.scalePixels(width, height)
//    return scaled
//}
//
//// Thêm extension cho scale:
//fun Image.scalePixels(newWidth: Int, newHeight: Int): Image {
//    val surface = org.jetbrains.skia.Surface.makeRasterN32Premul(newWidth, newHeight)
//    val canvas = surface.canvas
//    val paint = org.jetbrains.skia.Paint()
//    canvas.drawImageRect(
//        this,
//        org.jetbrains.skia.Rect.makeXYWH(0f, 0f, this.width.toFloat(), this.height.toFloat()),
//        org.jetbrains.skia.Rect.makeXYWH(0f, 0f, newWidth.toFloat(), newHeight.toFloat()),
//        paint
//    )
//    return surface.makeImageSnapshot()
//}