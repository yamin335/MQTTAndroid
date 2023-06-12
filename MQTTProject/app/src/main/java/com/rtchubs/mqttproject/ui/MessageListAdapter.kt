package com.rtchubs.mqttproject.ui

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.rtchubs.mqttproject.MessageModel
import com.rtchubs.mqttproject.R
import com.rtchubs.mqttproject.databinding.MessageListItemBinding

class MessageListAdapter constructor(
    private val itemCallback: (MessageModel) -> Unit,
    private val messageCallback: (MessageModel) -> Unit
): RecyclerView.Adapter<MessageListAdapter.ViewHolder>() {

    private var messages: ArrayList<MessageModel> = ArrayList()
    private var messageIndexMap: HashMap<String, Int> = HashMap()
    private var selectedIndex = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: MessageListItemBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.list_item_message, parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(messages[position], position)
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    fun setMessage(message: String) {
        messageIndexMap[message]?.let {
            messageCallback(messages[it])
            selectedIndex = it
            notifyDataSetChanged()
        }
    }

    fun submitList(messages: ArrayList<MessageModel>) {
        this.messages = messages

        var i = 0
        while (i < messages.size) {
            messageIndexMap[messages[i].rfId] = i
            i++
        }

        notifyDataSetChanged()
    }

    inner class ViewHolder (private val binding: MessageListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("ClickableViewAccessibility")
        fun bind(item: MessageModel, position: Int) {
            if (position == selectedIndex) {
                binding.card.strokeColor = ContextCompat.getColor(binding.root.context, R.color.teal_200)
            } else {
                binding.card.strokeColor = ContextCompat.getColor(binding.root.context, R.color.gray)
            }
            binding.image.setImageResource(item.image)
            binding.card.setOnClickListener {
                itemCallback(item)
            }
            binding.executePendingBindings()
        }
    }
}
