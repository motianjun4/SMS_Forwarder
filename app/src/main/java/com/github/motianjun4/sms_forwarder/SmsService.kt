package com.github.motianjun4.sms_forwarder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import android.widget.Toast
import com.jraska.console.Console
import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.request.SendMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.text.StringBuilder

class SmsReceiver: BroadcastReceiver(){
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("sms_receiver", "onReceive")
        if(context == null || intent == null || intent.action == null || intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION){
            return
        }
        val smsMessages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (smsMessages.isEmpty()){
            return
        }
        val sender = smsMessages[0].displayOriginatingAddress
        val textBuilder = StringBuilder()
        for (message in smsMessages) {
            textBuilder.append(message.displayMessageBody)
        }
        val text = textBuilder.toString()

        Toast.makeText(
            context,
            "Message from $sender: $text",
            Toast.LENGTH_SHORT
        )
            .show()
        Console.writeLine("You have one new message: $text")
        scope.launch {
            sendTelegram(context, "New Message:\n$sender:\n$text")
        }
    }
}

fun sendTelegram(context: Context, message: String){
    Console.writeLine("sending message to telegram")
    val token = context.getString(R.string.telegram_token)
    val chatId = context.getString(R.string.telegram_chat_id)
    val bot = TelegramBot(token)
    bot.execute(SendMessage(chatId,message))
}
