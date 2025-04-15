package com.example.mapapplication.ui.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mapapplication.databinding.ItemSearchLocationBinding
import com.example.mapapplication.model.LocationSearched

class LocationAdapter(private val onItemClick: (LocationSearched) -> Unit) : ListAdapter<LocationSearched, LocationAdapter.LocationViewHolder>(LocationDiffCallback) {

    inner class LocationViewHolder(private val binding: ItemSearchLocationBinding)
        : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: LocationSearched) {
            binding.tvNameLocation.text = item.description
            binding.tvAddressLocation.text = item.address
            binding.btnDetail.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        val binding = ItemSearchLocationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LocationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

object LocationDiffCallback: DiffUtil.ItemCallback<LocationSearched>() {
    override fun areItemsTheSame(oldItem: LocationSearched, newItem: LocationSearched): Boolean {
        return oldItem.placeId == newItem.placeId
    }

    override fun areContentsTheSame(oldItem: LocationSearched, newItem: LocationSearched): Boolean {
        return oldItem == newItem
    }

}
