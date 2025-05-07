package com.example.mapapplication.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mapapplication.databinding.ItemGosafeBinding

class GoSafeAdapter(val onClick: (BluetoothDevice) -> Unit): ListAdapter<BluetoothDevice, GoSafeAdapter.GoSafeViewHolder>(GoSafeDiffCallback) {
    inner class GoSafeViewHolder(private val binding: ItemGosafeBinding): RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("MissingPermission")
        fun bind(item: BluetoothDevice) {
            binding.tvNameDevice.text = item.name
            binding.tvStatus.text = item.address
            binding.root.setOnClickListener {
                onClick(item)
            }
        }
    }

    fun addDevice(device: BluetoothDevice) {
        if (currentList.contains(device)) return
        submitList(currentList + device)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): GoSafeViewHolder {
        val binding = ItemGosafeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GoSafeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GoSafeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

object GoSafeDiffCallback: DiffUtil.ItemCallback<BluetoothDevice>() {
    override fun areItemsTheSame(oldItem: BluetoothDevice, newItem: BluetoothDevice): Boolean {
        return oldItem.address == newItem.address
    }

    override fun areContentsTheSame(oldItem: BluetoothDevice, newItem: BluetoothDevice): Boolean {
        return oldItem == newItem
    }

}
