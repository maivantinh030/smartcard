package org.example.project.screen.admin

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.example.project.model.GameRevenue
import org.example.project.model.RevenuePoint
import org.example.project.network.TransactionApiClient

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminRevenueScreen(
    onBack: () -> Unit
) {
    val client = remember { TransactionApiClient() }
    var dayData by remember { mutableStateOf<List<RevenuePoint>>(emptyList()) }
    var monthData by remember { mutableStateOf<List<RevenuePoint>>(emptyList()) }
    var gameData by remember { mutableStateOf<List<GameRevenue>>(emptyList()) }
    var status by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun loadAll() {
        scope.launch {
            isLoading = true
            status = "Đang tải thống kê..."
            val d = client.revenueByDay()
            val m = client.revenueByMonth()
            val g = client.revenueByGame()
            d.onSuccess { dayData = it }
                .onFailure { status = "❌ ${it.message}" }
            m.onSuccess { monthData = it }
                .onFailure { status = "❌ ${it.message}" }
            g.onSuccess { gameData = it }
                .onFailure { status = "❌ ${it.message}" }
            if (status.isBlank()) status = "✅ Đã tải dữ liệu"
            isLoading = false
        }
    }

    LaunchedEffect(Unit) { loadAll() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFFFF3E0), Color(0xFFE3F2FD))
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                Text("Thống kê doanh thu", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.weight(1f))
                Button(onClick = { loadAll() }, enabled = !isLoading, shape = RoundedCornerShape(12.dp)) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("Làm mới")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            if (status.isNotBlank()) Text(status, color = Color.Gray)

            Spacer(modifier = Modifier.height(12.dp))

            Text("Theo ngày", fontWeight = FontWeight.SemiBold)
            RevenueList(dayData)

            Spacer(modifier = Modifier.height(16.dp))
            Text("Theo tháng", fontWeight = FontWeight.SemiBold)
            RevenueList(monthData)

            Spacer(modifier = Modifier.height(16.dp))
            Text("Theo game", fontWeight = FontWeight.SemiBold)
            GameRevenueList(gameData)
        }
    }
}

@Composable
private fun RevenueList(items: List<RevenuePoint>) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items) { item ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(item.label, fontWeight = FontWeight.Bold)
                    Text("${formatVnd(item.totalAmount)} VNĐ", color = Color(0xFF2E7D32), fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun GameRevenueList(items: List<GameRevenue>) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth().heightIn(max = 220.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items) { item ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                    Text("Game ${item.gameCode}", fontWeight = FontWeight.Bold)
                    Text("Doanh thu: ${formatVnd(item.totalAmount)} VNĐ", color = Color(0xFF1565C0))
                    Text("Lượt bán: ${item.totalTickets}", color = Color.Gray)
                }
            }
        }
    }
}

private fun formatVnd(amount: String): String {
    val base = amount.toDoubleOrNull() ?: return amount + "000"
    val vnd = (base * 1000).toLong()
    return "%,d".format(vnd)
}