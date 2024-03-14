package com.improveskills.habitstracker.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.improveskills.habitstracker.Interface.GetImage
import com.improveskills.habitstracker.R
import com.improveskills.habitstracker.Utils.ExFunctions

class IconsAdapter(private val context: Context, private val imageNames: List<String>,private val getImage: GetImage) :
    RecyclerView.Adapter<IconsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.icon_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val imageName = imageNames[position]
        holder.imageView.setImageDrawable(ExFunctions().loadImageFromAssets(context, imageName))
        holder.imageView.setOnClickListener {
            getImage.getImage(imageName)
        }
    }

    override fun getItemCount(): Int {
        return imageNames.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.icon_item)
    }

}