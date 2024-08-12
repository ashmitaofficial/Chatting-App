package com.example.chattingapp.model

data class Chat(
    val sender: String? = null,
    val receiver: String? = null,
    val isSeen: Boolean? = false,
    val url: String? = null,
    val messageId: String? = null,
    val message: String? = null
)
