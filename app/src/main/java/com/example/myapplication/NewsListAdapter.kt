package com.example.myapplication

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

class NewsListAdapter(private val listener : NewsItemClicked): RecyclerView.Adapter<NewsViewHolder>() {

    private val items : ArrayList<News> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_news, parent, false)
        val viewHolder = NewsViewHolder(view)
        view.setOnClickListener {
            listener.onItemClicked(items[viewHolder.adapterPosition])
        }
        return viewHolder
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val currItem = items[position]
        holder.titlView.text = currItem.title
        holder.description.text = currItem.des
        Glide.with(holder.itemView.context).load(currItem.img).listener( object :
            RequestListener<Drawable> {

            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Drawable>,
                isFirstResource: Boolean
            ): Boolean {
                binding.progressBar.visibility = View.GONE
                binding.progressText.visibility = View.GONE
                return false
            }

            override fun onResourceReady(
                resource: Drawable,
                model: Any,
                target: Target<Drawable>?,
                dataSource: DataSource,
                isFirstResource: Boolean
            ): Boolean {
                binding.progressBar.visibility = View.GONE
                binding.progressText.visibility = View.GONE
                return false
            }


            }
        ).into(holder.image)
        holder.date.text = currItem.date
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateNews(updatedNews: List<News>){
        items.clear()
        items.addAll(updatedNews)

        notifyDataSetChanged()
    }
}

class NewsViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
    val titlView: TextView = itemView.findViewById(R.id.articleTitle)
    val description : TextView = itemView.findViewById(R.id.articleDescription)
    val image : ImageView = itemView.findViewById(R.id.articleImage)
    val date : TextView = itemView.findViewById(R.id.articleDateTime)
}

interface NewsItemClicked {
    fun onItemClicked(item: News)
}