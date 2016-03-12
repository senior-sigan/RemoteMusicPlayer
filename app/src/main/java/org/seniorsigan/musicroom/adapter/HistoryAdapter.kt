package org.seniorsigan.musicroom.adapter

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.squareup.picasso.Picasso
import org.jetbrains.anko.find
import org.jetbrains.anko.image
import org.jetbrains.anko.layoutInflater
import org.jetbrains.anko.onClick
import org.seniorsigan.musicroom.App
import org.seniorsigan.musicroom.R
import org.seniorsigan.musicroom.TAG
import org.seniorsigan.musicroom.data.HistoryModel

class HistoryAdapter: RecyclerView.Adapter<HistoryViewHolder>() {
    var onItemClickListener: ((HistoryModel) -> Unit)? = null

    private val collection: MutableList<HistoryModel> = arrayListOf()

    fun update(models: List<HistoryModel>) {
        collection.clear()
        collection.addAll(models)
        notifyDataSetChanged()
    }

    fun insert(models: List<HistoryModel>) {
        collection.addAll(models)
        notifyDataSetChanged()
    }

    fun insert(model: HistoryModel) {
        collection.add(model)
        notifyItemInserted(collection.indexOfFirst { it._id == model._id })
    }

    override fun getItemCount(): Int = collection.size

    override fun onBindViewHolder(holder: HistoryViewHolder?, position: Int) {
        val item = collection.getOrNull(position) ?: return
        holder?.setItem(item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder? {
        val view = parent.context.layoutInflater.inflate(R.layout.history_item, parent, false)
        return HistoryViewHolder(view, onItemClickListener)
    }
}

class HistoryViewHolder(view: View, private val onItemClickListener: ((HistoryModel) -> Unit)?) : RecyclerView.ViewHolder(view) {
    val title = view.find<TextView>(R.id.history_title)
    val artist = view.find<TextView>(R.id.history_artist)
    val cover = view.find<ImageView>(R.id.history_album_art)
    val context = view.context

    fun setItem(model: HistoryModel) {
        title.text = model.title
        artist.text = model.artist
        itemView?.onClick {
            Log.d(TAG, "Handle click on $model")
            onItemClickListener?.invoke(model)
        }
        if (model.coverURL != null) {
            Picasso.with(context).load(model.coverURL).into(cover)
        } else {
            cover.image = App.defaults.cover
        }
    }
}