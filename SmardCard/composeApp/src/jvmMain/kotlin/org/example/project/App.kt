package org.example.project

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.Cake
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.Tag
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.example.project.screen.ChangePinScreen
import org.example.project.screen.ConnectScreen
import org.example.project.screen.CustomerViewScreen
import org.example.project.screen.FloatingBubbles
import org.example.project.screen.MainMenuScreen
import org.example.project.screen.PinEntryScreen
import org.example.project.screen.WriteDataScreen
import smardcard.composeapp.generated.resources.Res
import smardcard.composeapp.generated.resources.compose_multiplatform
import javax.smartcardio.*
import kotlin.concurrent.thread
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO



enum class AppScreen {
    CONNECT,
    PIN_ENTRY,
    MAIN_MENU,
    WriteDataScreen,
    CustomerDataScreen,
    ChangePinScreen
}

@Composable
fun SmartCardApp() {
    var currentScreen by remember { mutableStateOf(AppScreen.CONNECT) }
    val smartCardManager = remember { SmartCardManager() }
    when (currentScreen) {
        AppScreen.CONNECT -> {
            ConnectScreen(
                onCardConnected = { currentScreen = AppScreen.PIN_ENTRY },
                smartCardManager = smartCardManager
            )
        }

        AppScreen.PIN_ENTRY -> {
            PinEntryScreen(
                onPinVerified = { currentScreen = AppScreen.MAIN_MENU },
                smartCardManager = smartCardManager
            )
        }

        AppScreen.MAIN_MENU -> {
            MainMenuScreen(
                onDisconnect = {
                    smartCardManager.disconnect()
                    currentScreen = AppScreen.CONNECT
                },
                onNavigateWriteInfo = {
                    currentScreen = AppScreen.WriteDataScreen
                },
                onNavigateSettings = {

                },
                onNavigateReadInfo = {
                    currentScreen = AppScreen.CustomerDataScreen
                },

                onNavigateChangePin={
                    currentScreen = AppScreen.ChangePinScreen
                },
                smartCardManager = smartCardManager

            )
        }
        AppScreen.WriteDataScreen -> {
            WriteDataScreen(
                smartCardManager = smartCardManager,
                onBack = { currentScreen = AppScreen.MAIN_MENU }
            )
        }
        AppScreen.CustomerDataScreen -> {
            CustomerViewScreen(
                smartCardManager = smartCardManager,
                onBack = { currentScreen = AppScreen.MAIN_MENU }
            )
        }
        AppScreen.ChangePinScreen -> {
             ChangePinScreen(
                 smartCardManager = smartCardManager,
                 onBack = { currentScreen = AppScreen.MAIN_MENU }
             )
        }
    }
}




