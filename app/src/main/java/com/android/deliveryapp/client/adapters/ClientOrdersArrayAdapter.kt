package com.android.deliveryapp.client.adapters

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.android.deliveryapp.R
import com.android.deliveryapp.util.OrderItem

class ClientOrdersArrayAdapter(
    private val activity: Activity,
    layout: Int,
    private val array: Array<OrderItem>
): ArrayAdapter<OrderItem>(activity, layout, array) {

    internal class ViewHolder {
        var date: TextView? = null
        var totalPrice: TextView? = null
        var paymentType: TextView? = null
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View?

        if (convertView == null) {
            view = activity.layoutInflater.inflate(R.layout.list_element_order, null)

            val viewHolder = ViewHolder()
            viewHolder.date = view.findViewById(R.id.orderDate)
            viewHolder.totalPrice = view.findViewById(R.id.orderTotalPrice)
            viewHolder.paymentType = view.findViewById(R.id.paymentType)
            view.tag = viewHolder
        } else {
            view = convertView
        }

        val holder = view?.tag as ViewHolder
        holder.date?.text = array[position].date
        holder.totalPrice?.text = String.format( "%.2f€", array[position].totalPrice)
        holder.paymentType?.text = array[position].paymentType

        return view
    }
}