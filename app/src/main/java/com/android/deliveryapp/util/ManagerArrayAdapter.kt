package com.android.deliveryapp.util

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import coil.load
import coil.transform.CircleCropTransformation
import com.android.deliveryapp.R

/**
 * Array adapter used for ManagerHomeActivity
 */
class ManagerArrayAdapter(
        private val activity: Activity,
        layout: Int,
        private val array: Array<ProductItem>
): ArrayAdapter<ProductItem>(activity, layout, array) {

    internal class ViewHolder {
        var image: ImageView? = null
        var title: TextView? = null
        var price: TextView? = null
        var quantity: TextView? = null
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View?

        if (convertView == null) {
            view = activity.layoutInflater.inflate(R.layout.list_element, null)

            val viewHolder = ViewHolder()
            viewHolder.image = view.findViewById(R.id.productImage)
            viewHolder.title = view.findViewById(R.id.productName)
            viewHolder.price = view.findViewById(R.id.productPrice)
            viewHolder.quantity = view.findViewById(R.id.productQty)
            view.tag = viewHolder
        } else {
            view = convertView
        }

        val holder = view?.tag as ViewHolder
        holder.image?.load(array[position].imgUrl) {
            transformations(CircleCropTransformation())
            getItem(position)
            error(R.mipmap.ic_launcher_round)
            build()
        }
        holder.title?.text = array[position].title
        holder.price?.text = array[position].price
        holder.quantity?.text = array[position].quantity

        return view
    }
}