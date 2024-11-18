package com.example.notificationalerter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.TextView

class AppAdapter(context: Context?, private val appList: List<AppInfo>) : ArrayAdapter<AppInfo?>(context!!, 0, appList) {
    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var listener: OnCheckedChangeListener? = null

    fun setOnCheckedChangeListener(listener: OnCheckedChangeListener?) {
        this.listener = listener
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val holder: ViewHolder
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item_app, parent, false)
            holder = ViewHolder()
            holder.iconImageView = convertView.findViewById(R.id.icon_image_view)
            holder.nameTextView = convertView.findViewById(R.id.name_text_view)
            holder.checkBox = convertView.findViewById(R.id.checkbox)
            convertView.setTag(holder)
        } else {
            holder = convertView.tag as ViewHolder
        }

        val appInfo = appList[position]

        holder.iconImageView!!.setImageDrawable(appInfo.icon)
        holder.nameTextView!!.text = appInfo.name

        holder.checkBox!!.setOnCheckedChangeListener(null)
        holder.checkBox!!.isChecked = appInfo.isChecked
        holder.checkBox!!.setOnCheckedChangeListener { compoundButton: CompoundButton?, isChecked: Boolean ->
            appInfo.isChecked = isChecked
            if (listener != null) {
                listener!!.onCheckedChange(appInfo.packageName, isChecked)
            }
        }

        return convertView!!
    }

    internal class ViewHolder {
        var iconImageView: ImageView? = null
        var nameTextView: TextView? = null
        var checkBox: CheckBox? = null
    }

    interface OnCheckedChangeListener {
        fun onCheckedChange(packageName: String?, isChecked: Boolean)
    }
}
