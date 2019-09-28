package com.example.firebasesample

import android.view.ViewGroup

class HistoryAdapter(query: QueryCreator) : FirestoreAdapter<Checkin, HistoryViewHolder>(Checkin::class.java, query) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val holder = HistoryViewHolder.inflate(parent)
        return holder
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(get(position))
    }
}