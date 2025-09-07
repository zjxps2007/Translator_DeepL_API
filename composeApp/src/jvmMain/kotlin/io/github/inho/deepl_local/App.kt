package io.github.inho.deepl_local

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.material3.*
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import java.util.prefs.Preferences


private object SettingsStore {
    private val prefs: Preferences = Preferences.userRoot().node("deepl_local")
    private const val KEY_API = "deepl_api_key"

    fun loadApiKey(): String = prefs.get(KEY_API, "")
    fun saveApiKey(value: String) = prefs.put(KEY_API, value)
    fun clearApiKey() = prefs.remove(KEY_API)

}

object JetBrainsColors {
    val Background = Color(0xFF2B2D30)
    val Surface = Color(0xFF313336)
    val SurfaceLight = Color(0xFF3C3F41)
    val Border = Color(0xFF545558)
    val Text = Color(0xFFBCBEC4)
    val TextSecondary = Color(0xFF868A91)
    val Accent = Color(0xFF4A9EFF)
    val Success = Color(0xFF499C54)
    val Error = Color(0xFFDB5860)
}

@Composable
@Preview
fun App() {
    var sourceText by remember { mutableStateOf("") }
    var translatedText by remember { mutableStateOf("") }
    var sourceLanguage by remember { mutableStateOf(Language.AUTO) }
    var targetLanguage by remember { mutableStateOf(Language.ENGLISH) }
    var isTranslating by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showSettings by remember { mutableStateOf(false) }
    var apiKey by remember { mutableStateOf(SettingsStore.loadApiKey()) }


    val scope = rememberCoroutineScope()
    val deepLClient = remember(apiKey) { DeepLClient(apiKey) }
//    val deepLClient = remember { DeepLClient("5a3ddc72-79a3-422c-b744-b6f41dfaca9e:fx") }


    MaterialTheme(
        colorScheme = darkColorScheme(
            background = JetBrainsColors.Background,
            surface = JetBrainsColors.Surface,
            primary = JetBrainsColors.Accent,
            onBackground = JetBrainsColors.Text,
            onSurface = JetBrainsColors.Text
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize().background(JetBrainsColors.Background).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 타이틀 바
            TitleBar(
                onOpenSettings = { showSettings = true }
            )

            // 메인 번역 인터페이스
            Row(
                modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SourceTextArea(
                    text = sourceText,
                    onTextChange = {
                        sourceText = it
                        if (it.isBlank()) {
                            translatedText = ""
                            errorMessage = null
                        }
                    },
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )

                // 중간 컨트롤 (언어 번경, 번역 버튼)
                CenterControls(
                    sourceLanguage = sourceLanguage,
                    targetLanguage = targetLanguage,
                    onSourceLanguageChange = { sourceLanguage = it },
                    onTargetLanguageChange = { targetLanguage = it },
                    onSwapLanguages = {
                        if (sourceLanguage != Language.AUTO) {
                            val temp = sourceLanguage
                            sourceLanguage = targetLanguage
                            targetLanguage = temp
                        }
                    },
                    onTranslate = {
                        if (sourceText.isNotBlank() && !isTranslating) {
                            scope.launch {
                                isTranslating = true
                                errorMessage = null
                                if (apiKey.isBlank()) {
                                    errorMessage = "환경설정에서 DeepL API 키를 설정해주세요."
                                    isTranslating = false
                                    return@launch
                                }
                                deepLClient.translate(sourceText, sourceLanguage, targetLanguage)
                                    .onSuccess { translatedText = it }.onFailure { errorMessage = it.message }
                                isTranslating = false
                            }
                        }
                    },
                    isTranslating = isTranslating,
                    modifier = Modifier.fillMaxHeight()
                )

                // 번역 결과 영역
                TranslatedTextArea(
                    text = translatedText, errorMessage = errorMessage, modifier = Modifier.weight(1f).fillMaxHeight()
                )
            }

            // 하단 상태 바
            StatusBar(
                characterCount = sourceText.length, isTranslating = isTranslating
            )
        }

        if (showSettings) {
            var tempKey by remember { mutableStateOf(apiKey) }
            var reveal by remember { mutableStateOf(false) }
            val canSave = tempKey.isNotBlank()

            AlertDialog(
                onDismissRequest = { showSettings = false },
                title = { Text("환경설정") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("DeepL API Key")
                        OutlinedTextField(
                            value = tempKey,
                            onValueChange = { tempKey = it },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("예: xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx:fx") },
                            visualTransformation = if (reveal) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { reveal = !reveal }) {
                                    Icon(
                                        imageVector = if (reveal) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                        contentDescription = if (reveal) "숨기기" else "표시"
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = JetBrainsColors.Accent,
                                unfocusedBorderColor = JetBrainsColors.Border
                            )
                        )
                        if (apiKey.isBlank()) {
                            Text("현재 저장된 키가 없습니다.", color = JetBrainsColors.TextSecondary)
                        } else {
                            Text("키가 저장되어 있습니다.", color = JetBrainsColors.TextSecondary)
                        }
                    }
                },
                confirmButton = {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { showSettings = false }) {
                            Text("취소")
                        }
                        TextButton(
                            onClick = {
                                SettingsStore.clearApiKey()
                                apiKey = ""
                                tempKey = ""
                            }
                        ) {
                            Text("키 삭제", color = JetBrainsColors.Error)
                        }
                        Button(
                            onClick = {
                                val trimmed = tempKey.trim()
                                SettingsStore.saveApiKey(trimmed)
                                apiKey = trimmed
                                showSettings = false
                            },
                            enabled = canSave
                        ) {
                            Text("저장")
                        }
                    }
                },
                dismissButton = {},
                containerColor = JetBrainsColors.Surface,
                titleContentColor = JetBrainsColors.Text,
                textContentColor = JetBrainsColors.TextSecondary
            )
        }
    }
}


@Composable
fun TitleBar(
    onOpenSettings: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().background(JetBrainsColors.Surface, RoundedCornerShape(8.dp)).padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "DeepL Translator",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = JetBrainsColors.Text,
            fontFamily = FontFamily.Monospace
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically

        ) {
            Badge { Text("v1.0.0", fontSize = 10.sp) }
            Badge(containerColor = JetBrainsColors.Success) {
                Text("Developed By JIH", fontSize = 10.sp)
            }
            IconButton(onClick = onOpenSettings) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = "설정",
                    tint = JetBrainsColors.TextSecondary
                )
            }
        }
    }
}


@Composable
fun SourceTextArea(
    text: String, onTextChange: (String) -> Unit, modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = JetBrainsColors.Surface),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp)
        ) {
            Text(
                text = "소스 텍스트",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = JetBrainsColors.TextSecondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = text, onValueChange = onTextChange, modifier = Modifier.fillMaxSize(), placeholder = {
                    Text(
                        "번역할 내용을 입력하세요.", color = JetBrainsColors.TextSecondary
                    )
                }, colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = JetBrainsColors.Accent,
                    unfocusedBorderColor = JetBrainsColors.Border,
                    focusedTextColor = JetBrainsColors.Text,
                    unfocusedTextColor = JetBrainsColors.Text,
                    focusedContainerColor = JetBrainsColors.Background,
                    unfocusedContainerColor = JetBrainsColors.Background
                ), textStyle = androidx.compose.ui.text.TextStyle(
                    fontFamily = FontFamily.Monospace, fontSize = 14.sp
                )
            )
        }
    }
}

@Composable
fun CenterControls(
    sourceLanguage: Language,
    targetLanguage: Language,
    onSourceLanguageChange: (Language) -> Unit,
    onTargetLanguageChange: (Language) -> Unit,
    onSwapLanguages: () -> Unit,
    onTranslate: () -> Unit,
    isTranslating: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.width(200.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 소스 언어 선택
        LanguageDropdown(
            selectedLanguage = sourceLanguage, onLanguageSelected = onSourceLanguageChange, label = "소스 언어"
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 언어 교체 버튼
        IconButton(
            onClick = onSwapLanguages,
            modifier = Modifier.background(JetBrainsColors.SurfaceLight, RoundedCornerShape(50)).size(40.dp)
        ) {
            Text("⇄", fontSize = 16.sp, color = JetBrainsColors.Accent)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 타겟 언어 선택
        LanguageDropdown(
            selectedLanguage = targetLanguage, onLanguageSelected = onTargetLanguageChange, label = "번역 결과"
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 번역 버튼
        Button(
            onClick = onTranslate,
            enabled = !isTranslating,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = JetBrainsColors.Accent
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            if (isTranslating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("번역 중...")
            } else {
                Text("번역하기", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun TranslatedTextArea(
    text: String, errorMessage: String?, modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = JetBrainsColors.Surface),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp)
        ) {
            val clipboard = LocalClipboardManager.current
            var copied by remember { mutableStateOf(false) }
            if (copied) {
                LaunchedEffect(Unit) {
                    delay(1200)
                    copied = false
                }
            }
            Row(
                Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "번역 결과",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = JetBrainsColors.TextSecondary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            Box(
                modifier = Modifier.fillMaxSize().background(JetBrainsColors.Background, RoundedCornerShape(4.dp))
                    .padding(12.dp).verticalScroll(rememberScrollState())
            ) {
                when {
                    errorMessage != null -> {
                        Text(
                            text = "❌ 오류: $errorMessage",
                            color = JetBrainsColors.Error,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 14.sp
                        )
                    }

                    text.isNotBlank() -> {
                        SelectionContainer {
                            Text(
                                text = text,
                                color = JetBrainsColors.Text,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 14.sp
                            )
                        }
                    }

                    else -> {
                        Text(
                            text = "번역 결과가 여기에 표시됩니다...",
                            color = JetBrainsColors.TextSecondary,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LanguageDropdown(
    selectedLanguage: Language, onLanguageSelected: (Language) -> Unit, label: String
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text(
            text = label,
            fontSize = 12.sp,
            color = JetBrainsColors.TextSecondary,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Box {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = JetBrainsColors.Surface, contentColor = JetBrainsColors.Text
                ),
                border = BorderStroke(1.dp, JetBrainsColors.Border),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = selectedLanguage.displayName, fontSize = 12.sp, fontFamily = FontFamily.Monospace
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(JetBrainsColors.Surface)
            ) {
                Language.values().forEach { language ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                language.displayName, color = JetBrainsColors.Text, fontSize = 12.sp
                            )
                        }, onClick = {
                            onLanguageSelected(language)
                            expanded = false
                        }, colors = MenuDefaults.itemColors(
                            textColor = JetBrainsColors.Text
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun StatusBar(
    characterCount: Int, isTranslating: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth().background(JetBrainsColors.Surface, RoundedCornerShape(4.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "문자 수: $characterCount / 5000",
            fontSize = 12.sp,
            color = JetBrainsColors.TextSecondary,
            fontFamily = FontFamily.Monospace
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically
        ) {
            if (isTranslating) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(12.dp), color = JetBrainsColors.Accent, strokeWidth = 1.dp
                    )
                    Text(
                        text = "DeepL API 연결 중...", fontSize = 12.sp, color = JetBrainsColors.TextSecondary
                    )
                }
            } else {
                Text(
                    text = "✓ 준비됨", fontSize = 12.sp, color = JetBrainsColors.Success
                )
            }

            Text(
                text = "UTF-8 | Power by DeepL", fontSize = 12.sp, color = JetBrainsColors.TextSecondary
            )
        }
    }
}
