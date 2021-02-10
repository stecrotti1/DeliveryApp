package com.android.deliveryapp.util

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.android.deliveryapp.R

class CustomAdapter(private val context: Activity,
                    private val layout: Int,
                    private val array: Array<ProductItem>): ArrayAdapter<ProductItem>(context, layout, array) {

    internal class ViewHolder {
        var img: ImageView? = null
        var title: TextView? = null
        var price: TextView? = null
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView

        if (view == null) {
            val viewHolder = ViewHolder()

            view = context.layoutInflater.inflate(R.layout.list_element, null)

            viewHolder.img = view.findViewById(R.id.productImage)
            viewHolder.title = view.findViewById(R.id.productName)
            viewHolder.price = view.findViewById(R.id.productPrice)

            view.tag = viewHolder
        } else {
            view = convertView
        }

        val holder = view?.tag as ViewHolder
        holder.img?.setImageDrawable(ContextCompat.getDrawable(context, array[position].img))
        holder.title?.text = array[position].name
        holder.price?.text = array[position].price

        return view
    }

}