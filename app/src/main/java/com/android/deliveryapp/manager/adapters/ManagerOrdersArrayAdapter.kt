package com.android.deliveryapp.manager.adapters

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.android.deliveryapp.R
import com.android.deliveryapp.util.ManagerOrderItem

class ManagerOrdersArrayAdapter(
        private val activity: Activity,
        layout: Int,
        private val array: Array<ManagerOrderItem>
) : ArrayAdapter<ManagerOrderItem>(activity, layout, array) {

    internal class ViewHolder {
        var email: TextView? = null
        var date: TextView? = null
        var total: TextView? = null
        var payment: TextView? = null
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View?

        if (convertView == null) {
            view = activity.layoutInflater.inflate(R.layout.manager_order_list_element, null)

            val viewHolder = ViewHolder()
            viewHolder.email = view.findViewById(R.id.orderEmail)
            viewHolder.date = view.findViewById(R.id.date)
            viewHolder.total = view.findViewById(R.id.total)
            viewHolder.payment = view.findViewById(R.id.payment)
            view.tag = viewHolder
        } else {
            view = convertView
        }

        val holder = view?.tag as ViewHolder
        holder.email?.text = array[position].email
        holder.date?.text = array[position].date
        holder.total?.text = String.format("%.2f €", array[position].total)
        holder.payment?.text = array[position].payment

        return view
    }
}