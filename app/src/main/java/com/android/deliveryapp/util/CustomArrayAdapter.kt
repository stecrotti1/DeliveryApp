package com.android.deliveryapp.util

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import coil.ImageLoader
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import com.android.deliveryapp.R

class CustomArrayAdapter(
    private val activity: Activity,
    layout: Int,
    private val array: Array<ProductItem>
)
    : ArrayAdapter<ProductItem>(activity, layout, array) {

        internal class ViewHolder {
            var image: ImageView? = null
            var title: TextView? = null
            var price: TextView? = null
        }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View?

        if (convertView == null) {
            val inflater = activity.layoutInflater
            view = inflater.inflate(R.layout.list_element, parent)

            val viewHolder = ViewHolder()
            viewHolder.image = view.findViewById(R.id.productImage)
            viewHolder.title = view?.findViewById(R.id.productName)
            viewHolder.price = view?.findViewById(R.id.productPrice)
            view.tag = viewHolder
        } else {
            view = convertView
        }

        val imageLoader = ImageLoader.Builder(context)
            .availableMemoryPercentage(0.25)
            .crossfade(true)
            .build()

        val holder = view?.tag as ViewHolder

        val request = ImageRequest.Builder(context)
            .data(array[position].imgUrl) // load the image with the given url
            .crossfade(true)
            .target(holder.image!!)
            .transformations(CircleCropTransformation())
            .build()

        imageLoader.enqueue(request)

        holder.image?.id = position
        holder.title?.text = array[position].title
        holder.price?.text = array[position].price

        return view
    }
}