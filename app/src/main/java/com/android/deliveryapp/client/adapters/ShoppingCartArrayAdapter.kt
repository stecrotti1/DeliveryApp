package com.android.deliveryapp.client.adapters

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.android.deliveryapp.R
import com.android.deliveryapp.util.ProductItem

class ShoppingCartArrayAdapter(
        private val activity: Activity,
        layout: Int,
        private val array: Array<ProductItem>
): ArrayAdapter<ProductItem>(activity, layout, array) {

    internal class ViewHolder {
        var title: TextView? = null
        var price: TextView? = null
        var quantity: TextView? = null
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View?

        if (convertView == null) {
            view = activity.layoutInflater.inflate(R.layout.list_element_shopping_cart, null)

            val viewHolder = ViewHolder()
            viewHolder.title = view.findViewById(R.id.productName)
            viewHolder.price = view.findViewById(R.id.productPrice)
            viewHolder.quantity = view.findViewById(R.id.productQty)
            view.tag = viewHolder
        } else {
            view = convertView
        }

        val holder = view?.tag as ViewHolder
        holder.title?.text = array[position].title
        holder.price?.text = String.format( "%.2f€", array[position].price)
        holder.quantity?.text = array[position].quantity.toString()

        return view
    }
}