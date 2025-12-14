package org.example.project

import androidx. compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime. mutableStateOf
import androidx. compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window. Window
import androidx.compose.ui.window.application
import org. example.project.screen.ConnectScreen
import org.example.project.screen.PinEntryScreen
import org.example.project. screen.admin.*

enum class AdminScreen {
    CONNECT,
    PIN_ENTRY,
    MAIN,
    WRITE_INFO,
    VIEW_CUSTOMER,
    RECHARGE,
    GAME_MANAGEMENT,
    SETTINGS
}

@Composable
fun AdminApp() {
    var currentScreen by remember { mutableStateOf(AdminScreen.CONNECT) }
    val smartCardManager = remember { SmartCardManager() }

    when (currentScreen) {
        AdminScreen.CONNECT -> {
            ConnectScreen(
                onCardConnected = { currentScreen = AdminScreen.PIN_ENTRY },
                smartCardManager = smartCardManager
            )
        }

        AdminScreen.PIN_ENTRY -> {
            PinEntryScreen(
                smartCardManager = smartCardManager,
                onPinVerified = { currentScreen = AdminScreen. MAIN }
            )
        }

        AdminScreen. MAIN -> {
            AdminMainMenuScreen(
                smartCardManager = smartCardManager,
                onNavigateWriteInfo = { currentScreen = AdminScreen.WRITE_INFO },
                onNavigateRecharge = { currentScreen = AdminScreen.RECHARGE },
                onNavigateGameManagement = { currentScreen = AdminScreen.GAME_MANAGEMENT },
                onNavigateViewCustomer = { currentScreen = AdminScreen.VIEW_CUSTOMER },
                onNavigateSettings = { currentScreen = AdminScreen.SETTINGS },
                onDisconnect = {
                    smartCardManager.disconnect()
                    currentScreen = AdminScreen.CONNECT
                }
            )
        }

        AdminScreen.WRITE_INFO -> {
            AdminWriteInfoScreen(
                smartCardManager = smartCardManager,
                onBack = { currentScreen = AdminScreen. MAIN }
            )
        }

        AdminScreen. VIEW_CUSTOMER -> {
            AdminViewCustomerScreen(
                smartCardManager = smartCardManager,
                onBack = { currentScreen = AdminScreen.MAIN }
            )
        }

        AdminScreen.RECHARGE -> {
            AdminRechargeScreen(
                smartCardManager = smartCardManager,
                onBack = { currentScreen = AdminScreen.MAIN }
            )
        }

        AdminScreen.GAME_MANAGEMENT -> {
            AdminGameManagementScreen(
                smartCardManager = smartCardManager,
                onBack = { currentScreen = AdminScreen.MAIN }
            )
        }

        AdminScreen.SETTINGS -> {
            AdminSettingsScreen(
                smartCardManager = smartCardManager,
                onBack = { currentScreen = AdminScreen.MAIN }
            )
        }
    }
}

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "🛡️ SmartCard Park - ADMIN"
    ) {
        AdminApp()
    }
}