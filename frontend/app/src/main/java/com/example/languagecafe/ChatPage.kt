package com.example.languagecafe

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun ChatPage(
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel
) {
    val listState = rememberLazyListState()
    val messages = viewModel.conversation
    val isLoading = viewModel.isLoading

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(messages.size, isLoading) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Auto-scroll to bottom when keyboard opens
    val isKeyboardVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    LaunchedEffect(isKeyboardVisible) {
        if (isKeyboardVisible && messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = modifier.fillMaxSize() 
        // Removed .imePadding() because Scaffold in MainActivity now handles insets via safeDrawing
    ) {
        AppHeader(
            title = viewModel.scenarioTitle,
            emoji = viewModel.scenarioEmoji
        )
        ChatMessages(
            messages = messages,
            isLoading = isLoading,
            listState = listState,
            modifier = Modifier.weight(1f)
        )
        MessageInput(
            onMessageSend = { viewModel.sendMessage(it) },
            enabled = !viewModel.allGoalsCompleted
        )
    }
}

@Composable
fun AppHeader(title: String = "Language Cafe", emoji: String = "") {
    Surface(
        color = MaterialTheme.colorScheme.primary,
        contentColor = Color.White,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (emoji.isNotEmpty()) {
                Text(text = emoji, fontSize = 24.sp)
                Spacer(modifier = Modifier.width(12.dp))
            }
            Text(
                text = if (title.isEmpty()) "Language Cafe" else title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ChatMessages(
    messages: List<ChatMessage>,
    isLoading: Boolean,
    listState: androidx.compose.foundation.lazy.LazyListState,
    modifier: Modifier = Modifier
) {
    // ARCHITECTURAL FIX: Hoist the dialog state to the list level
    var selectedMessageId by remember { mutableStateOf<String?>(null) }
    var hasDismissedHint by rememberSaveable { mutableStateOf(false) }
    
    // Find the latest version of the selected message from the list
    val selectedMessage = messages.find { it.id == selectedMessageId }

    // Global dialog that reacts to the selected message
    selectedMessage?.let { message ->
        FeedbackDialog(
            message = message,
            onDismiss = { selectedMessageId = null }
        )
    }

    Column(modifier = modifier) {
        if (messages.size == 2 && !hasDismissedHint) {
            Text(
                text = "💡 Tip: Long press any message for translations or corrections!",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(8.dp)
                    .clickable { hasDismissedHint = true }
                    .fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f).padding(8.dp),
            state = listState
        ) {
            items(
                items = messages,
                key = { it.id }
            ) { msg ->
                MessageBubble(
                    message = msg,
                    onLongClick = { selectedMessageId = msg.id }
                )
            }
            
            if (isLoading) {
                item(key = "typing_indicator") { TypingIndicator() }
            }
        }
    }
}

@Composable
fun FeedbackDialog(message: ChatMessage, onDismiss: () -> Unit) {
    val isUser = message.role == "user"
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Got it")
            }
        },
        title = { 
            Text(if (isUser) "Language Feedback" else "Translation") 
        },
        text = {
            if (isUser) {
                if (!message.corrections.isNullOrEmpty()) {
                    Column {
                        message.corrections.forEach { correction ->
                            Text("Original: ", fontWeight = FontWeight.Bold)
                            Text(correction.original)
                            Text("Corrected: ", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text(correction.corrected)
                            Text("Why: ", fontWeight = FontWeight.Bold)
                            Text(correction.explanation)
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        }
                    }
                } else {
                    Text("Perfect! No corrections needed.")
                }
            } else {
                Text(message.translation ?: "Translation not available.")
            }
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageBubble(
    message: ChatMessage,
    onLongClick: () -> Unit
) {
    val isUser = message.role == "user"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .padding(6.dp)
                .background(
                    if (isUser) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.secondary,
                    shape = RoundedCornerShape(12.dp)
                )
                .combinedClickable(
                    onClick = { /* normal click */ },
                    onLongClick = onLongClick
                )
                .padding(12.dp)
        ) {
            Text(text = message.text, color = Color.White)
        }
    }
}

@Composable
fun TypingIndicator() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .background(
                    MaterialTheme.colorScheme.secondary,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Thinking...")
            }
        }
    }
}

@Composable
fun MessageInput(onMessageSend: (String) -> Unit, enabled: Boolean) {
    var message by remember { mutableStateOf("") }
    Row(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            modifier = Modifier.weight(1f),
            value = message,
            onValueChange = { message = it },
            placeholder = {
                Text(
                    if (enabled)
                        "Type a message..."
                    else
                        "Scenario completed!"
                )
                          },
            enabled = enabled
        )
        IconButton(
            enabled = enabled,
            onClick = {
                if (message.isNotBlank()) {
                    onMessageSend(message)
                    message = ""
                }
            }
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Send"
            )
        }
    }
}
