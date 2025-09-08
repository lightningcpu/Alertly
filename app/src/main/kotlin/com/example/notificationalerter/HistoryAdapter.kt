// HistoryAdapter.kt
package com.example.notificationalerter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import com.example.notificationalerter.data.NotificationHistoryItem
import java.text.SimpleDateFormat
import java.util.Locale

class HistoryAdapter(context: Context, private val historyList: List<NotificationHistoryItem>) :
    ArrayAdapter<NotificationHistoryItem>(context, 0, historyList) {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: inflater.inflate(R.layout.list_item_history, parent, false)
        val holder = view.tag as? ViewHolder ?: ViewHolder(view).also { view.tag = it }

        val historyItem = getItem(position) ?: return view
        holder.bind(historyItem)

        return view
    }

    fun getSelectedIds(): List<Long> {
        val selectedIds = mutableListOf<Long>()
        for (i in 0 until count) {
            val item = getItem(i)
            if (item != null && item.isChecked) {
                selectedIds.add(item.id)
            }
        }
        return selectedIds
    }

    private inner class ViewHolder(view: View) {
        private val iconImageView: ImageView = view.findViewById(R.id.icon_image_view)
        private val appNameTextView: TextView = view.findViewById(R.id.app_name_text_view)
        private val titleTextView: TextView = view.findViewById(R.id.title_text_view)
        private val textTextView: TextView = view.findViewById(R.id.text_text_view)
        private val timeTextView: TextView = view.findViewById(R.id.time_text_view)
        private val dateTextView: TextView = view.findViewById(R.id.date_text_view)
        private val checkBox: CheckBox = view.findViewById(R.id.checkbox)

        fun bind(historyItem: NotificationHistoryItem) {
            iconImageView.setImageDrawable(historyItem.appIcon)
            appNameTextView.text = historyItem.appName
            titleTextView.text = historyItem.title ?: "No title"
            textTextView.text = historyItem.text ?: "No text"
            timeTextView.text = timeFormat.format(historyItem.timestamp)
            dateTextView.text = dateFormat.format(historyItem.timestamp)

            checkBox.setOnCheckedChangeListener(null)
            checkBox.isChecked = historyItem.isChecked
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                historyItem.isChecked = isChecked
            }
        }
    }
}