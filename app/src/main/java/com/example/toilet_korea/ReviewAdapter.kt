package com.example.toilet_korea

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ReviewAdapter(var context: Context?, var newsList: ArrayList<ReviewData>) : RecyclerView.Adapter<ReviewAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_review, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = newsList[position]

        holder.itemView.findViewById<TextView>(R.id.userNm).text = item.userNm
        holder.itemView.findViewById<TextView>(R.id.contentR).text = item.content
        if (item.rate != null)
            holder.itemView.findViewById<RatingBar>(R.id.ratingStar).rating = item.rate!!.toFloat()
        holder.itemView.findViewById<ImageView>(R.id.imageView).setImageResource(item.image)
    }

    override fun getItemCount() = newsList.size
}