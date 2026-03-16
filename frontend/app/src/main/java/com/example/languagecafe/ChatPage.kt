package com.example.languagecafe

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun ChatPage(
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel
) {

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val messages = viewModel.conversation
    val isLoading = viewModel.isLoading

    // Auto-scroll when new messages appear
    LaunchedEffect(messages.size, isLoading) {
        coroutineScope.launch {
            listState.animateScrollToItem(messages.size)
        }
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {

        AppHeader()

        ChatMessages(
            messages = messages,
            isLoading = isLoading,
            listState = listState,
            modifier = Modifier.weight(1f)
        )

        MessageInput(
            onMessageSend = { viewModel.sendMessage(it) }
        )
    }
}

@Composable
fun AppHeader() {

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
    ) {

        Text(
            modifier = Modifier.padding(16.dp),
            text = "Language Cafe",
            color = Color.White,
            fontSize = 22.sp
        )
    }
}

@Composable
fun ChatMessages(
    messages: List<ChatMessage>,
    isLoading: Boolean,
    listState: androidx.compose.foundation.lazy.LazyListState,
    modifier: Modifier = Modifier
) {

    LazyColumn(
        modifier = modifier.padding(8.dp),
        state = listState
    ) {

        items(messages) { msg ->
            MessageBubble(msg)
        }

        if (isLoading) {
            item {
                TypingIndicator()
            }
        }
    }
}

@Composable
fun MessageBubble(message: ChatMessage) {

    val isUser = message.role == "user"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement =
            if (isUser) Arrangement.End else Arrangement.Start
    ) {

        Box(
            modifier = Modifier
                .padding(6.dp)
                .background(
                    if (isUser)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.secondary,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(12.dp)
        ) {

            Text(
                text = message.text,
                color = Color.White
            )
        }
    }
}

@Composable
fun TypingIndicator() {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
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

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

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
fun MessageInput(
    onMessageSend: (String) -> Unit
) {

    var message by remember { mutableStateOf("") }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        OutlinedTextField(
            modifier = Modifier.weight(1f),
            value = message,
            onValueChange = { message = it },
            placeholder = { Text("Type a message...") }
        )

        IconButton(
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