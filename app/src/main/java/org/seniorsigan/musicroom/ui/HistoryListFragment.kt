package org.seniorsigan.musicroom.ui

import android.app.Fragment
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.jetbrains.anko.async
import org.jetbrains.anko.find
import org.seniorsigan.musicroom.App
import org.seniorsigan.musicroom.R
import org.seniorsigan.musicroom.adapter.HistoryAdapter

class HistoryListFragment: Fragment() {
    private lateinit var historyView: RecyclerView
    private val adapter = HistoryAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.history_list_fragment, container, false)

        with(rootView, {
            historyView = find<RecyclerView>(R.id.rv_history)
        })

        historyView.layoutManager = LinearLayoutManager(context)
        historyView.adapter = adapter
        renderList()

        return rootView
    }

    fun renderList() {
        async() {
            adapter.collection = App.historyRepository.findAll()
        }
    }
}