package com.example.magnifyingglass.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.magnifyingglass.R

class SavedImagesAdapter(private var dataSet: List<Uri>) : RecyclerView.Adapter<SavedImagesAdapter.ViewHolder>() {

    // Define a click listener interface
    interface OnItemClickListener {
        fun onItemClick(uri: Uri)
        fun onItemDelete(uri: Uri)
    }

    // Declare a listener variable
    private var listener: OnItemClickListener? = null

    // Function to set the listener
    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.image_view_saved_pictures)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_saved_images, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val uri = dataSet[position]
        // Load the image with Glide and apply the color filter
        Glide.with(holder.itemView.context)
            .load(uri)
            .into(holder.imageView)

        // Set a click listener on the itemView
        holder.itemView.setOnClickListener {
            listener?.onItemClick(uri)
        }
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

}