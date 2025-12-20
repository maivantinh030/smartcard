
        package org.example.project

        import androidx.compose.runtime.Composable
        import androidx.compose.runtime.getValue
        import androidx.compose.runtime.mutableStateOf
        import androidx.compose.runtime.remember
        import androidx.compose.runtime.setValue
        import androidx.compose.ui.window.Window
        import androidx.compose.ui.window.application
        import org.example.project.auth.AdminSession
        import org.example.project.config.ServerConfig
        import org.example.project.screen.ConnectScreen
        import org.example.project.screen.PinEntryScreen
        import org.example.project.screen.admin.AdminGameManagementScreen
        import org.example.project.screen.admin.AdminLoginScreen
        import org.example.project.screen.admin.AdminMainMenuScreen
        import org.example.project.screen.admin.AdminRechargeScreen
        import org.example.project.screen.admin.AdminRSAAuthScreen
        import org.example.project.screen.admin.AdminSettingsScreen
        import org.example.project.screen.admin.AdminViewCustomerScreen
        import org.example.project.screen.admin.AdminWriteInfoScreen

        enum class AdminScreen {
            ADMIN_LOGIN,
            CONNECT,
            PIN_ENTRY,
            MAIN,
            WRITE_INFO,
            VIEW_CUSTOMER,
            RECHARGE,
            GAME_MANAGEMENT,
            RSA_AUTH,
            SETTINGS
        }

        @Composable
        fun AdminApp() {
            var currentScreen by remember { mutableStateOf(AdminScreen.ADMIN_LOGIN) }
            val smartCardManager = remember { SmartCardManager() }
            val session = remember { AdminSession() }
            var baseUrl by remember { mutableStateOf(ServerConfig.baseUrl) }

            when (currentScreen) {
                AdminScreen.ADMIN_LOGIN -> {
                    AdminLoginScreen(
                        session = session,
                        onLoggedIn = { currentScreen = AdminScreen.CONNECT },
                        baseUrl = baseUrl,
                        onBaseUrlChange = {
                            baseUrl = it
                            ServerConfig.baseUrl = it
                        }
                    )
                }
                AdminScreen.CONNECT -> {
                    ConnectScreen(
                        onCardConnected = { currentScreen = AdminScreen.PIN_ENTRY },
                        onRequireRSASetup = { currentScreen = AdminScreen.RSA_AUTH },
                        smartCardManager = smartCardManager,
                        requireRSAAuth = false
                    )
                }
                AdminScreen.PIN_ENTRY -> {
                    PinEntryScreen(
                        smartCardManager = smartCardManager,
                        onPinVerified = { currentScreen = AdminScreen.MAIN }
                    )
                }
                AdminScreen.MAIN -> {
                    AdminMainMenuScreen(
                        smartCardManager = smartCardManager,
                        onNavigateWriteInfo = { currentScreen = AdminScreen.WRITE_INFO },
                        onNavigateRecharge = { currentScreen = AdminScreen.RECHARGE },
                        onNavigateGameManagement = { currentScreen = AdminScreen.GAME_MANAGEMENT },
                        onNavigateViewCustomer = { currentScreen = AdminScreen.VIEW_CUSTOMER },
                        onNavigateSettings = { currentScreen = AdminScreen.SETTINGS },
                        onDisconnect = {
                            smartCardManager.disconnect()
                            currentScreen = AdminScreen.ADMIN_LOGIN
                        }
                    )
                }
                AdminScreen.WRITE_INFO -> {
                    AdminWriteInfoScreen(
                        smartCardManager = smartCardManager,
                        onBack = { currentScreen = AdminScreen.MAIN }
                    )
                }
                AdminScreen.VIEW_CUSTOMER -> {
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
                AdminScreen.RSA_AUTH -> {
                    AdminRSAAuthScreen(
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