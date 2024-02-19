package com.example.onclickrecyclerview

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.onclickrecyclerview.databinding.ItemsRowBinding


class ItemAdapter(private var employeeList: MutableList<Employee>, private var onDeleteClickListener: OnDeleteClickListener) :
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
        val item = employeeList[position]
        holder.bind(item)
        // Finally add an onclickListener to the item.
        holder.itemView.setOnClickListener {
            if (onClickListener != null) {
                onClickListener!!.onClick(position, item )
            }
        }
    }
    fun updateData(newList: List<Employee>) {
        employeeList.clear()
        employeeList.addAll(newList)
        notifyDataSetChanged()
        Log.d("ItemAdapter", "Data updated. New list size: ${newList.size}")
    }

    // Gets the number of items in the list
    override fun getItemCount(): Int {
        return employeeList.size
    }

    // A function to bind the onclickListener.
    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }
    fun setOnDeleteClickListener(onDeleteClickListener: OnDeleteClickListener){
        this.onDeleteClickListener = onDeleteClickListener
    }
    interface OnDeleteClickListener {
        fun onDeleteClick(employee: Employee)
    }
    interface OnClickListener {
        fun onClick(position: Int, model: Employee)
    }


    // A ViewHolder describes an item view and metadata
    // about its place within the RecyclerView.
    class ViewHolder(binding: ItemsRowBinding) : RecyclerView.ViewHolder(binding.root) {
        // Holds the TextView that
        // will add each item to
        val tvName = binding.tvName
        val tvTemperature = binding.tvTemperature
        fun bind(employee: Employee){
            tvName.text = employee.name
            tvTemperature.text = "${employee.temperature}°F"
        }
    }
}