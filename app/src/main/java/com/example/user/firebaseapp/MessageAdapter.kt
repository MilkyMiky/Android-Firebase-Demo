package com.example.user.firebaseapp

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Mikhail Lysyansky on 13.06.18.
 */
class MessageAdapter : RecyclerView.Adapter<MessageAdapter.ViewHolder>() {
    private val formatter = SimpleDateFormat("HH:mm ", Locale.getDefault())
    var messageList: List<Message> = ArrayList()

    override fun getItemViewType(position: Int): Int {
        return if (messageList[position].user == "user") 1
        else 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = if (viewType == 1)
            R.layout.message_item
        else
            R.layout.message_item_white
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                layout,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = messageList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val message: Message = messageList[position]
        holder.messageText.text = message.text
        holder.messageDate.text = formatter.format(message.date)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageText: TextView = itemView.findViewById(R.id.message_text)
        val messageDate: TextView = itemView.findViewById(R.id.message_date)
    }
}
