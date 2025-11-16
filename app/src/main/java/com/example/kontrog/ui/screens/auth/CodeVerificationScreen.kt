// CodeVerificationScreen.kt
package com.example.kontrog.ui.screens.auth

import android.app.Activity
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

private const val CODE_LENGTH = 6

@Composable
fun CodeVerificationScreen(
    viewModel: PhoneAuthViewModel,
    onVerificationSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    val focusRequesters = remember { List(CODE_LENGTH) { FocusRequester() } }
    val codeValues = remember { mutableStateListOf(*Array(CODE_LENGTH) { "" }) }

    val activity = LocalContext.current as Activity
    val fullCode = codeValues.joinToString("")
    val isCodeComplete = fullCode.length == CODE_LENGTH

    LaunchedEffect(uiState) {
        if (uiState is PhoneAuthUiState.Success) {
            onVerificationSuccess()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ПОДТВЕРЖДЕНИЕ ТЕЛЕФОНА") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.Start
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ВВЕДИТЕ\nПИН КОД",
                    style = MaterialTheme.typography.titleLarge
                )

                TextButton(
                    onClick = {
                        val phoneNumber = viewModel.lastSentPhoneNumber
                        if (phoneNumber != null) {
                            viewModel.sendVerificationCode(phoneNumber, activity)
                        } else {
                            Log.e("2FA", "Cannot resend code: Phone number not found.")
                        }
                    },
                    enabled = uiState != PhoneAuthUiState.Loading
                ) {
                    Text("ОТПРАВИТЬ ПОВТОРНО")
                }
            }

            Spacer(Modifier.height(32.dp))

            // Поля ввода кода
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                repeat(CODE_LENGTH) { index ->
                    BasicTextField(
                        value = TextFieldValue(
                            text = codeValues[index],
                            selection = TextRange(codeValues[index].length)
                        ),
                        onValueChange = { newValue ->
                            val digit = newValue.text.filter { it.isDigit() }.take(1)

                            if (digit.isNotEmpty()) {
                                codeValues[index] = digit
                                if (index < CODE_LENGTH - 1) {
                                    focusRequesters[index + 1].requestFocus()
                                }
                            } else if (newValue.text.isEmpty()) {
                                codeValues[index] = ""
                                if (index > 0) {
                                    focusRequesters[index - 1].requestFocus()
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = LocalTextStyle.current.copy(
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .focusRequester(focusRequesters[index]),
                        decorationBox = { innerTextField ->
                            OutlinedTextField(
                                value = codeValues[index],
                                onValueChange = {},
                                readOnly = true,
                                singleLine = true,
                                modifier = Modifier.fillMaxSize(),
                                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer
                                )
                            )
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                innerTextField()
                            }
                        }
                    )
                }

                LaunchedEffect(Unit) {
                    focusRequesters.first().requestFocus()
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = "КОД ОТПРАВЛЕН НА ВАШ НОМЕР\nКОД ДЕЙСТВИТЕЛЕН 15 МИНУТ",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(48.dp))

            when (uiState) {
                PhoneAuthUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }

                is PhoneAuthUiState.Error -> {
                    Text(
                        text = (uiState as PhoneAuthUiState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }

                else -> {
                    if (isCodeComplete) {
                        Button(
                            onClick = { viewModel.verifyCode(fullCode) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Подтвердить")
                        }
                    }
                }
            }
        }
    }
}