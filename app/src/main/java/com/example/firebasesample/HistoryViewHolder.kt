package com.example.firebasesample

import android.media.Image
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.history_item.view.*
import org.w3c.dom.Text

class HistoryViewHolder(historyView: View) : RecyclerView.ViewHolder(historyView) {
    companion object {
        fun inflate(parent: ViewGroup) : HistoryViewHolder {
            return HistoryViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.history_item, parent, false))
        }
    }

    var eventInfo: TextView = historyView.itemTextView
    var eventDate: TextView = historyView.itemDateView
    var image : ImageView = historyView.itemImageView
    var soba: ImageView = historyView.imageSoba

    fun bind(checkin: Checkin) {
        eventInfo.text = checkin.eventMonth.toString() + checkin.eventName
        eventDate.text = checkin.registerTime.toString()
        image.setImageResource(R.drawable.pic_hero_opening_img01)
        if (checkin.sobaJoin == 0) soba.setImageResource(R.drawable.soba)
    }
}