package org.example.project.screen.user

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.example.project.SmartCardManager
import org.example.project.model.TransactionDto
import org.example.project.network.TransactionApiClient

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserHistoryScreen(
    smartCardManager: SmartCardManager,
    onBack: () -> Unit
) {
    val client = remember { TransactionApiClient() }
    var customerId by remember { mutableStateOf("") }
    var transactions by remember { mutableStateOf<List<TransactionDto>>(emptyList()) }
    var status by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            val info = smartCardManager.readCustomerInfo()
            val id = info["customerID"] ?: ""
            if (id.isNotBlank()) {
                customerId = id
                // Tự động tải lịch sử
                isLoading = true
                status = "Đang tải lịch sử..."
                val result = client.history(id.trim())
                result.onSuccess {
                    transactions = it
                    status = if (it.isEmpty()) "📭 Chưa có giao dịch" else "✅ Đã tải ${it.size} giao dịch"
                }.onFailure { e ->
                    status = "❌ Lỗi: ${e.message}"
                }
                isLoading = false
            } else {
                status = "⚠️ Không thể đọc thẻ"
            }
        } catch (e: Exception) {
            status = "❌ Lỗi đọc thẻ: ${e.message}"
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFE3F2FD), Color(0xFFE1F5FE))
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = "Lịch sử giao dịch",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text("Customer: $customerId", fontWeight = FontWeight.SemiBold)

            Spacer(modifier = Modifier.height(12.dp))
            if (status.isNotBlank()) {
                Text(status, color = Color.DarkGray)
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(transactions) { txn ->
                    TransactionCard(txn)
                }
            }
        }
    }
}

@Composable
private fun TransactionCard(txn: TransactionDto) {
    val chipColor = if (txn.type == "TOPUP") Color(0xFF4CAF50) else Color(0xFFFF7043)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(txn.type, color = chipColor, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Text(txn.createdAt.take(19).replace('T', ' '), color = Color.Gray, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("Số tiền: ${formatVnd(txn.amount)} VNĐ", fontWeight = FontWeight.SemiBold)
            txn.gameCode?.let { Text("Game: $it") }
            txn.tickets?.let { Text("Số lượt: $it") }
            txn.balanceAfter?.let { Text("Số dư sau giao dịch: ${formatBalance(it)} VNĐ") }
        }
    }
}

private fun formatVnd(amount: String): String {
    val base = amount.toDoubleOrNull() ?: return amount + "000"
    val vnd = (base * 1000).toLong()
    return "%,d".format(vnd)
}

private fun formatBalance(balance: Int): String {
    // balance đang ở đơn vị nghìn đồng trên thẻ → nhân 1000 để ra VNĐ
    return "%,d".format(balance.toLong() * 1000)
}