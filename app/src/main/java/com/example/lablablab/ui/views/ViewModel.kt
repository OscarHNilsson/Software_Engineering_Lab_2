package com.example.lablablab.ui.views

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.generationConfig
import com.example.lablablab.House
import com.example.lablablab.HouseApi
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.Locale

sealed interface HouseUiState {
    data class Success(val house: House) : HouseUiState
    object Error : HouseUiState
    object Loading : HouseUiState
}

sealed interface GeminiLoadingState {
    object NotLoading : GeminiLoadingState
    object Loading : GeminiLoadingState
}

class ViewModel : ViewModel() {
    var houseUiState: HouseUiState by mutableStateOf(HouseUiState.Loading)
        private set

    var loadingState: GeminiLoadingState by mutableStateOf(GeminiLoadingState.NotLoading)
        private set

    init {
        fetchHouse()
    }

    fun geminiText(text: String, house: House) {
        viewModelScope.launch {
            val lowerText = text.lowercase(Locale.ROOT)

            geminiApiCall(lowerText, house)
        }
    }
    private suspend fun geminiApiCall(text: String, house: House) {
        val generativeModel = Firebase.ai(backend = GenerativeBackend.googleAI())
            .generativeModel(
                modelName = "gemini-3-flash-preview",
                generationConfig = generationConfig {
                    temperature = 0f
                }
            )

        val prompt = """Convert the following user command into a JSON object. 
            Only use these keys: door, window, light. 
            Only use these values: open, closed, on, off. 
            Omit any key-value pair if the value is not specified in the input. 
            Output only valid JSON—no explanations or additional text.
        Example output: 
        {
          "door": "open",
          "window": "closed",
          "light": "off"
        }
        User input: $text""".trimIndent()

        loadingState = GeminiLoadingState.Loading

        val response = generativeModel.generateContent(prompt)
        val jsonText = response.text ?: return

        parseOutput(jsonText, house)

    }

    private fun parseOutput(jsonText: String, house: House) {
        try {
            val json = JSONObject(jsonText)
            val updatedHouse = house.copy(
                door = json.optString("door", house.door),
                window = json.optString("window", house.window),
                light = json.optString("light", house.light)
            )

            Log.d("logloglog", updatedHouse.toString())
            setHouse(updatedHouse)
            loadingState = com.example.lablablab.ui.views.GeminiLoadingState.NotLoading

        } catch (e: Exception) {
            Log.e("Gemini", "JSON not valid: $jsonText", e)
        }
    }

    private fun fetchHouse() {
        viewModelScope.launch {
            houseUiState = try {
                val result = HouseApi.retrofitService.getHouse()
                HouseUiState.Success(result)
            } catch (e: Exception) {
                Log.e("HouseViewModel", "There was a problem getting the house.", e)
                HouseUiState.Error
            }
        }
    }

    fun setHouse(house: House) {
        viewModelScope.launch {
            houseUiState = try {
                val result = HouseApi.retrofitService.setHouse(house)
                HouseUiState.Success(result)
            } catch (e: Exception) {
                Log.e("HouseViewModel", "There was a problem setting the house.", e)
                HouseUiState.Error
            }
        }
    }
}