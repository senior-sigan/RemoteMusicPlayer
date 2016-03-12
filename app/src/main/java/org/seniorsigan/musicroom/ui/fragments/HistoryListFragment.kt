package org.seniorsigan.musicroom.ui.fragments

import android.app.Fragment
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.async
import org.jetbrains.anko.find
import org.jetbrains.anko.onUiThread
import org.jetbrains.anko.support.v4.onRefresh
import org.seniorsigan.musicroom.*
import org.seniorsigan.musicroom.adapter.HistoryAdapter
import org.seniorsigan.musicroom.data.HistoryModel

class HistoryListFragment: Fragment() {
    private lateinit var refresh: SwipeRefreshLayout
    private lateinit var historyView: RecyclerView
    private val adapter = HistoryAdapter()

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onHistoryUpdate(model: HistoryModel) {
        Log.d(TAG, "Update history list, new item's added: $model")
        adapter.insert(model)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.history_list_fragment, container, false)

        with(rootView, {
            refresh = find<SwipeRefreshLayout>(R.id.refresh_history)
            historyView = find<RecyclerView>(R.id.rv_history)
        })

        adapter.onItemClickListener = { history ->
            App.queue.add(TrackInfo(
                    url = history.url,
                    title = history.title,
                    artist = history.artist,
                    source = history.source,
                    coverURL = history.coverURL))
        }

        historyView.layoutManager = LinearLayoutManager(context)
        historyView.adapter = adapter

        refresh.onRefresh { renderList() }
        renderList()

        return rootView
    }

    fun renderList() {
        refresh.isRefreshing = true
        async() {
            val history = App.historyRepository.findAll()
            onUiThread {
                adapter.update(history)
                refresh.isRefreshing = false
                Log.d(TAG, "Refreshed")
            }
        }
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }
}