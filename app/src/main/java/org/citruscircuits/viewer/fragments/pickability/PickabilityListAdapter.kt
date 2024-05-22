package org.citruscircuits.viewer.fragments.pickability

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import org.citruscircuits.viewer.constants.Constants
import org.citruscircuits.viewer.databinding.PickabilityCellBinding
import java.lang.Float.parseFloat

/**
 * Adapter for the pickability list.
 */
class PickabilityListAdapter(context: Context, var items: Map<String, String>, var mode: PickabilityMode) : BaseAdapter() {
    private val inflater = LayoutInflater.from(context)
    override fun getCount() = items.size
    override fun getItem(i: Int) = items.keys.elementAt(i)
    override fun getItemId(position: Int) = position.toLong()
    @SuppressLint("ViewHolder")
    override fun getView(i: Int, view: View?, parent: ViewGroup?): View {
        val e = getItem(i)
        val pickability = items[e]!!
        val rowView = PickabilityCellBinding.inflate(inflater)
        rowView.tvPlacement.text = "${i + 1}"
        rowView.tvTeamNumber.text = e
        rowView.tvType.text =
            if (mode != PickabilityMode.First) pickability.substringBefore("&")
            else "1st"
        rowView.tvPickability.text =
            if (pickability != Constants.NULL_CHARACTER)
                if (mode != PickabilityMode.First) parseFloat(("%.1f").format(pickability.substringAfter("&").toFloat())).toString()
                else parseFloat(("%.1f").format(pickability.toFloat())).toString()
            else pickability
        return rowView.root
    }
}
