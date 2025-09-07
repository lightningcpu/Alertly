package com.example.notificationalerter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import com.example.notificationalerter.data.AppInfo

class AppAdapter(context: Context, private val appList: List<AppInfo>) :
    ArrayAdapter<AppInfo>(context, 0, appList) {

    private var listener: OnCheckedChangeListener? = null
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    fun setOnCheckedChangeListener(listener: OnCheckedChangeListener?) {
        this.listener = listener
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: inflater.inflate(R.layout.list_item_app, parent, false)
        val holder = view.tag as? ViewHolder ?: ViewHolder(view).also { view.tag = it }

        val appInfo = getItem(position) ?: return view

        holder.bind(appInfo, listener)

        return view
    }

    private class ViewHolder(view: View) {
        private val iconImageView: ImageView = view.findViewById(R.id.icon_image_view)
        private val nameTextView: TextView = view.findViewById(R.id.name_text_view)
        private val checkBox: CheckBox = view.findViewById(R.id.checkbox)

        fun bind(appInfo: AppInfo, listener: OnCheckedChangeListener?) {
            iconImageView.setImageDrawable(appInfo.icon)
            nameTextView.text = appInfo.name

            // Remove previous listener to avoid duplicate calls
            checkBox.setOnCheckedChangeListener(null)
            checkBox.isChecked = appInfo.isChecked

            checkBox.setOnCheckedChangeListener { _, isChecked ->
                appInfo.isChecked = isChecked
                listener?.onCheckedChange(appInfo.packageName, isChecked)
            }
        }
    }

    interface OnCheckedChangeListener {
        fun onCheckedChange(packageName: String?, isChecked: Boolean)
    }
}