package com.example.uzair.iamfalling.adapter

import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import com.example.uzair.fallen.database.model.DeviceEvent

/**
 * A simple PagedListAdapter that binds posts items into CardViews.
 * <p>
 * PagedListAdapter is a RecyclerView.Adapter base class which can present the content of PagedLists
 * in a RecyclerView. It requests new pages as the user scrolls, and handles new PagedLists by
 * computing list differences on a background thread, and dispatching minimal, efficient updates to
 * the RecyclerView to ensure minimal UI thread work.
 * <p>
 * If you want to use your own Adapter base class, try using a PagedListAdapterHelper inside your
 * adapter instead.
 *
 * @see android.arch.paging.PagedListAdapter
 * @see android.arch.paging.AsyncPagedListDiffer
 */
class DeviceEventsAdapter : PagedListAdapter<DeviceEvent, DeviceEventsViewHolder>(
    diffCallback
) {
    override fun onBindViewHolder(holder: DeviceEventsViewHolder, position: Int) {
        holder.bindTo(getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceEventsViewHolder =
        DeviceEventsViewHolder(parent)

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    companion object {
        /**
         * This diff callback informs the PagedListAdapter how to compute list differences when new
         * PagedLists arrive.
         */
        private val diffCallback = object : DiffUtil.ItemCallback<DeviceEvent>() {
            override fun areItemsTheSame(oldItem: DeviceEvent, newItem: DeviceEvent): Boolean =
                oldItem.id == newItem.id

            /**
             * Note that in kotlin, == checking on data classes compares all contents
             */
            override fun areContentsTheSame(oldItem: DeviceEvent, newItem: DeviceEvent): Boolean =
                oldItem == newItem
        }
    }
}
