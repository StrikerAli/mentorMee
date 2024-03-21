package com.aliashraf.assignment2

import android.app.AlertDialog
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

class ChatAdapter(private var messages: MutableList<ChatMessage>, private val database: DatabaseReference):
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TEXT_MESSAGE_VIEW_TYPE -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_chat_message, parent, false)
                TextMessageViewHolder(view)
            }
            IMAGE_MESSAGE_VIEW_TYPE -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_chat_image, parent, false)
                ImageMessageViewHolder(view)
            }
            VIDEO_MESSAGE_VIEW_TYPE -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_chat_video, parent, false)
                VideoMessageViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        when (holder) {
            is TextMessageViewHolder -> holder.bindTextMessage(message)
            is ImageMessageViewHolder -> holder.bindImageMessage(message)
            is VideoMessageViewHolder -> holder.bindVideoMessage(message)
        }
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    override fun getItemViewType(position: Int): Int {
        val message = messages[position]
        return when {
            message.messageType == MessageType.TEXT -> TEXT_MESSAGE_VIEW_TYPE
            message.messageType == MessageType.IMAGE -> IMAGE_MESSAGE_VIEW_TYPE
            message.messageType == MessageType.VIDEO -> VIDEO_MESSAGE_VIEW_TYPE
            else -> throw IllegalArgumentException("Invalid message type")
        }
    }

    fun updateMessages(newMessages: List<ChatMessage>) {
        messages.clear()
        messages.addAll(newMessages)
        notifyDataSetChanged()
    }

    inner class TextMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.textMessage)
        private val messageTimeStamp: TextView = itemView.findViewById(R.id.textTime)
        private var clickCount = 0

        fun bindTextMessage(chatMessage: ChatMessage) {
            messageText.text = chatMessage.message
            messageTimeStamp.text = chatMessage.timestamp
            itemView.setOnLongClickListener {
                showEditMessageDialog(chatMessage)
                true // Consume the long click event
            }
            itemView.setOnClickListener {
                Log.d("YES", "DELETED")
                clickCount++
                if (clickCount < 3) {
                    // First or second click: Show toast message
                    Toast.makeText(
                        itemView.context,
                        "Press ${3 - clickCount} more times to delete",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    // Third click: Delete message
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val query = database.orderByChild("message").equalTo(chatMessage.message)
                        query.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                for (snapshot in dataSnapshot.children) {
                                    val key = snapshot.key
                                    if (key != null) {
                                        database.child(key).removeValue()
                                            .addOnCompleteListener { task ->
                                                if (task.isSuccessful) {
                                                    // Remove from RecyclerView
                                                    if (position < messages.size - 1) {
                                                        messages.removeAt(position)
                                                        notifyItemRemoved(position)
                                                    } else if (position == messages.size - 1) {
                                                        notifyItemRemoved(position - 1)
                                                    }
                                                    clickCount = 0
                                                } else {
                                                    Toast.makeText(
                                                        itemView.context,
                                                        "Failed to delete message",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                    }
                                    // Break the loop after deleting the first matching message
                                    break
                                }
                            }

                            override fun onCancelled(databaseError: DatabaseError) {
                                // Handle cancellation
                            }
                        })
                    }
                }
            }
        }

        private fun showEditMessageDialog(chatMessage: ChatMessage) {
            val dialogView =
                LayoutInflater.from(itemView.context).inflate(R.layout.dialog_edit_message, null)
            val editTextMessage = dialogView.findViewById<EditText>(R.id.editTextMessage)
            editTextMessage.setText(chatMessage.message)

            val alertDialogBuilder = AlertDialog.Builder(itemView.context)
                .setView(dialogView)
                .setTitle("Edit Message")
                .setPositiveButton("Save") { dialog, which ->
                    val newMessage = editTextMessage.text.toString()
                    editMessage(chatMessage, newMessage)
                }
                .setNegativeButton("Cancel") { dialog, which ->
                    dialog.dismiss()
                }

            val alertDialog = alertDialogBuilder.create()
            alertDialog.show()
        }

        private fun editMessage(chatMessage: ChatMessage, newMessage: String) {
            val query = database.orderByChild("message").equalTo(chatMessage.message)
            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (snapshot in dataSnapshot.children) {
                        val key = snapshot.key
                        if (key != null) {
                            // Update the message with the new content
                            database.child(key).child("message").setValue(newMessage)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Toast.makeText(
                                            itemView.context,
                                            "Message edited successfully",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        Toast.makeText(
                                            itemView.context,
                                            "Failed to edit message",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                        }
                        // Break the loop after finding the message
                        break
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle cancellation
                }
            })
        }
    }

    inner class ImageMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.imageMessage)
        private val messageTimeStamp: TextView = itemView.findViewById(R.id.textTime)

        fun bindImageMessage(chatMessage: ChatMessage) {
            Picasso.get().load(chatMessage.imageUrl).into(imageView)
            messageTimeStamp.text = chatMessage.timestamp
        }
    }

    inner class VideoMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val videoView: VideoView = itemView.findViewById(R.id.VideoMessage)
        private val messageTimeStamp: TextView = itemView.findViewById(R.id.textTime)

        fun bindVideoMessage(chatMessage: ChatMessage) {
            // Load video into videoView using an appropriate method (e.g., from URL)
            // Example with a URL:
            videoView.setVideoURI(Uri.parse(chatMessage.videoUrl))
            videoView.setOnPreparedListener { mediaPlayer ->
                mediaPlayer.setLooping(true)
                mediaPlayer.start()
                mediaPlayer.setVolume(0f, 0f)

            }
            messageTimeStamp.text = chatMessage.timestamp
        }
    }

    companion object {
        private const val TEXT_MESSAGE_VIEW_TYPE = 1
        private const val IMAGE_MESSAGE_VIEW_TYPE = 2
        private const val VIDEO_MESSAGE_VIEW_TYPE = 3
    }
}

enum class MessageType {
    TEXT,
    IMAGE,
    VIDEO
}

data class ChatMessage(
    var message: String = "",
    val sender: String = "",
    val timestamp: String = "",
    val messageType: MessageType = MessageType.TEXT,
    val imageUrl: String = "",
    val videoUrl: String = "",
    var isMarkedForDeletion: Boolean = false // New property
) {
    constructor() : this("", "", "", MessageType.TEXT)

    companion object {
        fun createTextMessage(message: String, sender: String, timestamp: String): ChatMessage {
            return ChatMessage(message, sender, timestamp, MessageType.TEXT)
        }

        fun createImageMessage(imageUrl: String, sender: String, timestamp: String): ChatMessage {
            return ChatMessage("", sender, timestamp, MessageType.IMAGE, imageUrl)
        }

        fun createVideoMessage(videoUrl: String, sender: String, timestamp: String): ChatMessage {
            return ChatMessage("", sender, timestamp, MessageType.VIDEO, "", videoUrl)
        }
    }
}




