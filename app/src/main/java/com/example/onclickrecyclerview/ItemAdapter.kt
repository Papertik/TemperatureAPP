package com.example.onclickrecyclerview

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.onclickrecyclerview.databinding.ItemsRowBinding


class ItemAdapter(private var sensorList: MutableList<Sensor>, private var onDeleteClickListener: OnDeleteClickListener) :
    RecyclerView.Adapter<ItemAdapter.ViewHolder>() {
    private var onClickListener: OnClickListener? = null

    // Inflates the item views which is designed in xml layout file
    // create a new
    // ViewHolder and initializes some private fields to be used by RecyclerView.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemsRowBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    // Binds each item in the ArrayList to a view

    // Called when RecyclerView needs
    // a new {@link ViewHolder} of the
    // given type to represent
    // an item.

    // This new ViewHolder should be constructed with
    // a new View that can represent the items
    // of the given type. You can either create a
    // new View manually or inflate it from an XML
    // layout file.
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = sensorList[position]
        holder.bind(item)
        // Finally add an onclickListener to the item.
        holder.itemView.setOnClickListener {
            if (onClickListener != null) {
                onClickListener!!.onClick(position, item )
            }
        }
    }
    fun updateData(newList: List<Sensor>) {
        sensorList.clear()
        sensorList.addAll(newList)
        notifyDataSetChanged()
        Log.d("ItemAdapter", "Data updated. New list size: ${newList.size}")
    }

    // Gets the number of items in the list
    override fun getItemCount(): Int {
        return sensorList.size
    }

    // A function to bind the onclickListener.
    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }
    fun setOnDeleteClickListener(onDeleteClickListener: OnDeleteClickListener){
        this.onDeleteClickListener = onDeleteClickListener
    }
    interface OnDeleteClickListener {
        fun onDeleteClick(sensor: Sensor)
    }
    interface OnClickListener {
        fun onClick(position: Int, model: Sensor)
    }
    fun updateTemperature(sensorId: Int, newTemperature: Double) {
        val position = sensorList.indexOfFirst { it.id == sensorId }
        if (position != -1) {
            sensorList[position].temperature = newTemperature
            notifyItemChanged(position)
        }
    }

    // A ViewHolder describes an item view and metadata
    // about its place within the RecyclerView.
    class ViewHolder(binding: ItemsRowBinding) : RecyclerView.ViewHolder(binding.root) {
        // Holds the TextView that
        // will add each item to
        private val tvName = binding.tvName
        private val tvTemperature = binding.tvTemperature



        fun bind(sensor: Sensor){
            tvName.text = sensor.name
            tvTemperature.text = "${SensorInfo.getMostRecentTemperature(sensor.tempList.reversed())}°F"
            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed({
            tvTemperature.text = "${SensorInfo.getMostRecentTemperature(sensor.tempList.reversed())}°F"}, 1 * 60 * 1000)
            handler.removeCallbacksAndMessages(null) // Remove any remaining callbacks

        }

    }
}