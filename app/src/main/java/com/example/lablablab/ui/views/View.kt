package com.example.lablablab.ui.views

import android.app.Activity
import android.speech.RecognizerIntent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Switch
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import com.example.lablablab.House
import com.example.lablablab.R


@Composable
fun View(
    viewModel: ViewModel,
    houseUiState: HouseUiState,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    when (houseUiState) {
        is HouseUiState.Loading -> LoadingScreen(modifier = modifier.fillMaxSize())
        is HouseUiState.Success -> ResultScreen(
            house = houseUiState.house,
            modifier = modifier.fillMaxSize().padding(contentPadding),
            viewModel
        )
        is HouseUiState.Error -> ErrorScreen(modifier = modifier.fillMaxSize())
    }
}

@Composable
fun LoadingScreen(modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        Image(
            modifier = Modifier.size(200.dp),
            painter = painterResource(R.drawable.progress_helper),
            contentDescription = "loading"
        )
    }
}

@Composable
fun ResultScreen(house: House, modifier: Modifier = Modifier, viewModel: ViewModel) {
    var lightState by remember(house.light) { mutableStateOf(house.light == "on") }
    var doorState by remember(house.door) { mutableStateOf(house.door == "open") }
    var windowState by remember(house.window) { mutableStateOf(house.window == "open") }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = matches?.firstOrNull() ?: ""
            viewModel.geminiText(spokenText, house)
            println("User said: $spokenText")
        }
    }

    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.padding(top = 50.dp))

            Image(
                painter = painterResource(id = R.drawable.hehe),
                contentDescription = "Smart Home Icon",
                modifier = Modifier
                    .size(200.dp)
                    .padding(bottom = 30.dp)
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                HouseDevice(
                    deviceName = "Light",
                    deviceState = lightState,
                    onStateChange = {
                        lightState = it
                        viewModel.setHouse(house.copy(light = if (it) "on" else "off"))
                    },
                    activeText = "On",
                    inactiveText = "Off",
                    activeIcon = R.drawable.lightbulb_on,
                    inactiveIcon = R.drawable.lightbulb_off,
                    contentDescription = "light"
                )

                HouseDevice(
                    deviceName = "Door",
                    deviceState = doorState,
                    onStateChange = {
                        doorState = it
                        viewModel.setHouse(house.copy(door = if (it) "open" else "closed"))
                    },
                    activeText = "Open",
                    inactiveText = "Closed",
                    activeIcon = R.drawable.door_open,
                    inactiveIcon = R.drawable.door,
                    contentDescription = "door",
                )

                HouseDevice(
                    deviceName = "Window",
                    deviceState = windowState,
                    onStateChange = {
                        windowState = it
                        viewModel.setHouse(house.copy(window = if (it) "open" else "closed"))
                    },
                    activeText = "Open",
                    inactiveText = "Closed",
                    activeIcon = R.drawable.window_open_variant,
                    inactiveIcon = R.drawable.window_closed_variant,
                    contentDescription = "window",
                )
            }

            Button(
                onClick = {
                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(
                            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                            RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH
                        )
                    }
                    launcher.launch(intent)
                },
                modifier = Modifier
                    .padding(top = 8.dp, bottom = 16.dp)
                    .size(120.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF212f58),
                    contentColor = Color.White
                )
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Voice",
                        fontSize = 16.sp,
                        color = Color.White
                    )
                    Text(
                        text = "Assistant",
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            }

            Box(modifier = Modifier.padding(bottom = 16.dp)) {
                if (viewModel.loadingState == GeminiLoadingState.Loading) {
                    Text(
                        text = "Thinking...",
                        fontSize = 16.sp,
                    )
                } else {
                    Text(
                        text = "Press to activate voice assistant",
                        fontSize = 16.sp,
                    )
                }
            }

            Box(modifier = Modifier.padding(bottom = 16.dp)) {
                Text(
                    text = "Powered by Gemini!",
                    fontSize = 11.sp,
                )
            }
            Spacer(modifier = Modifier.padding(bottom = 30.dp))
        }
    }
}


@Composable
fun ErrorScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.network_strength_off_outline),
            contentDescription = null
        )
        Text(
            text = "Failed fetching Resources",
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
fun HouseDevice(
    deviceName: String,
    deviceState: Boolean,
    onStateChange: (Boolean) -> Unit,
    activeText: String,
    inactiveText: String,
    activeIcon: Int,
    inactiveIcon: Int,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 8.dp)
            .border(
                width = 1.dp,
                color = Color.Gray,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {

            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Image(
                    painter = painterResource(
                        if (deviceState) activeIcon else inactiveIcon
                    ),
                    contentDescription = contentDescription,
                    modifier = Modifier.size(40.dp)
                        .padding(end = 8.dp),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
                )
                Text(
                    text = deviceName,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Start
                )
            }

            Switch(
                checked = deviceState,
                onCheckedChange = onStateChange,
                modifier = Modifier.padding(start = 16.dp),
                colors = SwitchDefaults.colors(
                    checkedTrackColor = Color(0xFF1a2444),
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color.Gray,
                )
            )
        }
    }
}
