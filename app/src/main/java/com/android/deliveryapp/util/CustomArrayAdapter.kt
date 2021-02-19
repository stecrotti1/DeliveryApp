package com.android.deliveryapp.util

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.android.deliveryapp.R

class CustomArrayAdapter(private val activity: Activity, private val list: MutableList<Model>):
    ArrayAdapter<Model>(activity, R.layout.list_element) {

        internal class ViewHolder {
            var image: ImageView? = null
            var pb: ProgressBar? = null
            var title: TextView? = null
            var price: TextView? = null
        }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view: View?

        if (convertView == null) {
            val inflator = activity.layoutInflater
            view = inflator.inflate(R.layout.list_element, null)

            val viewHolder = ViewHolder()
            viewHolder.pb = view.findViewById(R.id.progressBar) as ProgressBar
            viewHolder.image = view.findViewById(R.id.productImage)
            viewHolder.image?.visibility = View.GONE
            viewHolder.title = view.findViewById(R.id.productName)
            viewHolder.price = view.findViewById(R.id.productPrice)
        } else {
            view = convertView
        }

        val holder = view?.tag as ViewHolder
        holder.image?.tag = list[position].url
        holder.image?.id = position
        holder.title?.text = list[position].name
        holder.price?.text = list[position].price

        val progressImage = ProgressBarImage()
        progressImage.img = holder.image
        progressImage.pb = holder.pb

        DownloadImageTask().execute(progressImage)

        return view
    }
}