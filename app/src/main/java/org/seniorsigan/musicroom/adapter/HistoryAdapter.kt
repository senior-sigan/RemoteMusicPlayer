package org.seniorsigan.musicroom.adapter

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import org.jetbrains.anko.find
import org.jetbrains.anko.layoutInflater
import org.jetbrains.anko.onClick
import org.seniorsigan.musicroom.R
import org.seniorsigan.musicroom.TAG
import org.seniorsigan.musicroom.data.HistoryModel

class HistoryAdapter: RecyclerView.Adapter<HistoryViewHolder>() {
    var onItemClickListener: ((HistoryModel) -> Unit)? = null

    var collection: List<HistoryModel> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
            Log.d(TAG, "HistoryAdapter updated")
        }

    fun addItem(model: HistoryModel) {
        collection += model
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

class HistoryViewHolder(view: View, val onItemClickListener: ((HistoryModel) -> Unit)?) : RecyclerView.ViewHolder(view) {
    val title = view.find<TextView>(R.id.history_title)
    val artist = view.find<TextView>(R.id.history_artist)

    fun setItem(model: HistoryModel) {
        title.text = model.title
        artist.text = model.artist
        itemView?.onClick {
            Log.d(TAG, "Handle click on $model")
            onItemClickListener?.invoke(model)
        }
    }
}