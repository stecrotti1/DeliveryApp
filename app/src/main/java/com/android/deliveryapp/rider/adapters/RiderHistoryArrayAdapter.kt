package com.android.deliveryapp.rider.adapters

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.android.deliveryapp.R
import com.android.deliveryapp.util.RiderHistoryItem

class RiderHistoryArrayAdapter(
    private val activity: Activity,
    layout: Int,
    private val array: Array<RiderHistoryItem>
) : ArrayAdapter<RiderHistoryItem>(activity, layout, array) {

    internal class ViewHolder {
        var date: TextView? = null
        var location: TextView? = null
        var outcome: TextView? = null
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View?

        if (convertView == null) {
            view = activity.layoutInflater.inflate(R.layout.rider_order_list_element, null)

            val viewHolder = ViewHolder()
            viewHolder.date = view.findViewById(R.id.dateHistory)
            viewHolder.location = view.findViewById(R.id.locationHistory)
            viewHolder.outcome = view.findViewById(R.id.outcomeHistory)
            view.tag = viewHolder
        } else {
            view = convertView
        }

        val holder = view?.tag as ViewHolder
        holder.date?.text = array[position].date
        holder.location?.text = array[position].location
        holder.outcome?.text = array[position].outcome

        return view
    }
}