package org.example.project

import androidx.compose.runtime.*
import org.example.project.screen.ConnectScreen
import org.example.project. screen.PinEntryScreen
import org. example.project.screen.RoleSelectionScreen
import org.example.project.screen.UserRole
import org.example.project.screen.user.UserMainMenuScreen
import org.example. project.screen.user.UserViewInfoScreen
import org.example.project.screen.user.UserBalanceScreen
import org.example.project.screen.user.UserGameListScreen
import org.example.project.screen.user.UserChangePinScreen
import org.example.project.screen.admin.AdminMainMenuScreen
import org.example.project.screen.admin.AdminWriteInfoScreen
import org.example.project.screen.admin.AdminViewCustomerScreen
import org.example.project. screen.admin.AdminRechargeScreen
import org.example.project.screen.admin.AdminGameManagementScreen
import org.example.project.screen.admin.AdminSettingsScreen

enum class AppScreen {
    CONNECT,
    ROLE_SELECTION,
    PIN_ENTRY,

    // User screens
    USER_MAIN,
    USER_VIEW_INFO,
    USER_BALANCE,
    USER_GAMES,
    USER_CHANGE_PIN,

    // Admin screens
    ADMIN_MAIN,
    ADMIN_WRITE_INFO,
    ADMIN_VIEW_CUSTOMER,
    ADMIN_RECHARGE,
    ADMIN_GAME_MANAGEMENT,
    ADMIN_SETTINGS
}

@Composable
fun SmartCardApp() {
    var currentScreen by remember { mutableStateOf(AppScreen.CONNECT) }
    var selectedRole by remember { mutableStateOf<UserRole?>(null) }
    val smartCardManager = remember { SmartCardManager() }

    when (currentScreen) {
        // ===== COMMON SCREENS =====
        AppScreen.CONNECT -> {
            ConnectScreen(
                onCardConnected = {
                    currentScreen = AppScreen. ROLE_SELECTION
                },
                smartCardManager = smartCardManager
            )
        }

        AppScreen.ROLE_SELECTION -> {
            RoleSelectionScreen(
                onRoleSelected = { role ->
                    selectedRole = role
                    currentScreen = AppScreen.PIN_ENTRY
                },
                onBack = {
                    smartCardManager.disconnect()
                    currentScreen = AppScreen.CONNECT
                }
            )
        }

        AppScreen.PIN_ENTRY -> {
            PinEntryScreen(
                smartCardManager = smartCardManager,
                onPinVerified = {
                    currentScreen = when(selectedRole) {
                        UserRole.USER -> AppScreen.USER_MAIN
                        UserRole.ADMIN -> AppScreen.ADMIN_MAIN
                        else -> AppScreen.CONNECT
                    }
                }
            )
        }

        // ===== USER SCREENS =====
        AppScreen.USER_MAIN -> {
            UserMainMenuScreen(
                smartCardManager = smartCardManager,
                onNavigateViewInfo = { currentScreen = AppScreen.USER_VIEW_INFO },
                onNavigateBalance = { currentScreen = AppScreen. USER_BALANCE },
                onNavigateGames = { currentScreen = AppScreen.USER_GAMES },
                onNavigateChangePin = { currentScreen = AppScreen. USER_CHANGE_PIN },
                onDisconnect = {
                    smartCardManager.disconnect()
                    selectedRole = null
                    currentScreen = AppScreen.CONNECT
                }
            )
        }

        AppScreen.USER_VIEW_INFO -> {
            UserViewInfoScreen(
                smartCardManager = smartCardManager,
                onBack = { currentScreen = AppScreen.USER_MAIN }
            )
        }

        AppScreen. USER_BALANCE -> {
            UserBalanceScreen(
                smartCardManager = smartCardManager,
                onBack = { currentScreen = AppScreen.USER_MAIN }
            )
        }

        AppScreen.USER_GAMES -> {
            UserGameListScreen(
                smartCardManager = smartCardManager,
                onBack = { currentScreen = AppScreen.USER_MAIN }
            )
        }

        AppScreen.USER_CHANGE_PIN -> {
            UserChangePinScreen(
                smartCardManager = smartCardManager,
                onBack = { currentScreen = AppScreen.USER_MAIN }
            )
        }

        // ===== ADMIN SCREENS =====
        AppScreen. ADMIN_MAIN -> {
            AdminMainMenuScreen(
                smartCardManager = smartCardManager,
                onNavigateWriteInfo = { currentScreen = AppScreen.ADMIN_WRITE_INFO },
                onNavigateRecharge = { currentScreen = AppScreen.ADMIN_RECHARGE },
                onNavigateGameManagement = { currentScreen = AppScreen. ADMIN_GAME_MANAGEMENT },
                onNavigateViewCustomer = { currentScreen = AppScreen.ADMIN_VIEW_CUSTOMER },
                onNavigateSettings = { currentScreen = AppScreen. ADMIN_SETTINGS },
                onDisconnect = {
                    smartCardManager.disconnect()
                    selectedRole = null
                    currentScreen = AppScreen.CONNECT
                }
            )
        }

        AppScreen. ADMIN_WRITE_INFO -> {
            AdminWriteInfoScreen(
                smartCardManager = smartCardManager,
                onBack = { currentScreen = AppScreen.ADMIN_MAIN }
            )
        }

        AppScreen.ADMIN_VIEW_CUSTOMER -> {
            AdminViewCustomerScreen(
                smartCardManager = smartCardManager,
                onBack = { currentScreen = AppScreen. ADMIN_MAIN }
            )
        }

        AppScreen.ADMIN_RECHARGE -> {
            AdminRechargeScreen(
                smartCardManager = smartCardManager,
                onBack = { currentScreen = AppScreen.ADMIN_MAIN }
            )
        }

        AppScreen.ADMIN_GAME_MANAGEMENT -> {
            AdminGameManagementScreen(
                smartCardManager = smartCardManager,
                onBack = { currentScreen = AppScreen.ADMIN_MAIN }
            )
        }

        AppScreen. ADMIN_SETTINGS -> {
            AdminSettingsScreen(
                smartCardManager = smartCardManager,
                onBack = { currentScreen = AppScreen.ADMIN_MAIN }
            )
        }
    }
}