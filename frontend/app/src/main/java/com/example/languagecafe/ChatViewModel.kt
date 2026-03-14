package com.example.languagecafe

import android.util.Log
import androidx.lifecycle.ViewModel

class ChatViewModel : ViewModel() {

    fun sendMessage(message : String) {
        Log.i("In ChatViewModel ", message)
    }
}