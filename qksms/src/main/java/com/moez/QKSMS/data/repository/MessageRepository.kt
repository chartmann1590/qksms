package com.moez.QKSMS.data.repository

import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Telephony
import android.telephony.SmsManager
import com.moez.QKSMS.data.model.Conversation
import com.moez.QKSMS.data.model.Message
import com.moez.QKSMS.data.sync.MessageColumns
import com.moez.QKSMS.data.sync.SyncManager
import com.moez.QKSMS.receiver.MessageDeliveredReceiver
import com.moez.QKSMS.receiver.MessageSentReceiver
import com.moez.QKSMS.util.extensions.insertOrUpdate
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort

class MessageRepository(val context: Context) {

    fun getConversationMessagesAsync(): RealmResults<Message> {
        return Realm.getDefaultInstance()
                .where(Message::class.java)
                .findAllSortedAsync("date", Sort.DESCENDING)
                .distinctAsync("threadId")
    }

    fun getConversationAsync(threadId: Long): Conversation {
        return Realm.getDefaultInstance()
                .where(Conversation::class.java)
                .equalTo("id", threadId)
                .findFirstAsync()
    }

    fun getConversation(threadId: Long): Conversation? {
        return Realm.getDefaultInstance()
                .where(Conversation::class.java)
                .equalTo("id", threadId)
                .findFirst()
    }

    fun getMessages(threadId: Long): RealmResults<Message> {
        return Realm.getDefaultInstance()
                .where(Message::class.java)
                .equalTo("threadId", threadId)
                .findAllSorted("date")
    }

    fun getUnreadUnseenMessages(): RealmResults<Message> {
        return Realm.getDefaultInstance()
                .where(Message::class.java)
                .equalTo("seen", false)
                .equalTo("read", false)
                .findAllSorted("date")
    }

    fun insertReceivedMessage(address: String, body: String, time: Long) {
        val values = ContentValues()
        values.put("address", address)
        values.put("body", body)
        values.put("date_sent", time)

        val contentResolver = context.contentResolver
        Flowable.just(values)
                .subscribeOn(Schedulers.io())
                .map { contentResolver.insert(Uri.parse("content://sms/inbox"), values) }
                .subscribe { uri -> copyMessageToRealm(uri) }
    }

    fun markSent(uri: Uri) {
        val values = ContentValues()
        values.put("type", Telephony.TextBasedSmsColumns.MESSAGE_TYPE_SENT)
        values.put("read", true)
        updateMessage(values, uri)
    }

    fun markFailed(uri: Uri, resultCode: Int) {
        val values = ContentValues()
        values.put("type", Telephony.TextBasedSmsColumns.MESSAGE_TYPE_FAILED)
        values.put("read", true)
        values.put("error_code", resultCode)
        updateMessage(values, uri)
    }

    fun sendMessage(threadId: Long, address: String, body: String) {
        val values = ContentValues()
        values.put("address", address)
        values.put("body", body)
        values.put("date", System.currentTimeMillis())
        values.put("read", true)
        values.put("type", Telephony.TextBasedSmsColumns.MESSAGE_TYPE_OUTBOX)
        values.put("thread_id", threadId)

        val contentResolver = context.contentResolver
        Flowable.just(values)
                .subscribeOn(Schedulers.io())
                .map { contentResolver.insert(Uri.parse("content://sms/"), values) }
                .subscribe { uri ->
                    copyMessageToRealm(uri)

                    val sentIntent = Intent(context, MessageSentReceiver::class.java).putExtra("uri", uri.toString())
                    val sentPI = PendingIntent.getBroadcast(context, uri.lastPathSegment.toInt(), sentIntent, PendingIntent.FLAG_UPDATE_CURRENT)

                    val deliveredIntent = Intent(context, MessageDeliveredReceiver::class.java).putExtra("uri", uri.toString())
                    val deliveredPI = PendingIntent.getBroadcast(context, uri.lastPathSegment.toInt(), deliveredIntent, PendingIntent.FLAG_UPDATE_CURRENT)

                    val smsManager = SmsManager.getDefault()
                    smsManager.sendTextMessage(address, null, body, sentPI, deliveredPI)
                }
    }

    private fun updateMessage(values: ContentValues, uri: Uri) {
        val contentResolver = context.contentResolver
        Flowable.just(values)
                .subscribeOn(Schedulers.io())
                .map { contentResolver.update(uri, values, null, null) }
                .subscribe { copyMessageToRealm(uri) }
    }

    private fun copyMessageToRealm(uri: Uri) {
        val cursor = context.contentResolver.query(uri, null, null, null, "date DESC")
        if (cursor.moveToFirst()) {
            val columns = MessageColumns(cursor)
            val message = SyncManager.messageFromCursor(cursor, columns)
            message.insertOrUpdate()
        }
        cursor.close()
    }

}