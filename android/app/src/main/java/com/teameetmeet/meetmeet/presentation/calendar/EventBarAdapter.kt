package com.teameetmeet.meetmeet.presentation.calendar

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.teameetmeet.meetmeet.databinding.ItemEventBarBinding
import com.teameetmeet.meetmeet.presentation.model.EventBar

class EventBarAdapter : ListAdapter<EventBar?, EventBarAdapter.EventBarViewHolder>(diffCallback) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): EventBarViewHolder {
        val binding = ItemEventBarBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        binding.root.layoutParams.height = (parent.measuredHeight / 5)
        return EventBarViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EventBarViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class EventBarViewHolder(
        private val binding: ItemEventBarBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: EventBar?) {
            item?.let {
                binding.item = item
            }
        }
    }

    companion object {
        val diffCallback = object : DiffUtil.ItemCallback<EventBar?>() {
            override fun areItemsTheSame(oldItem: EventBar, newItem: EventBar): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: EventBar, newItem: EventBar): Boolean {
                return oldItem == newItem
            }
        }
    }
}