package com.example.uzair.iamfalling.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.uzair.fallen.database.model.DeviceEvent
import com.example.uzair.iamfalling.R

/**
 * A simple ViewHolder that can bind a post item. It also accepts null items since the data may
 * not have been fetched before it is bound.
 */
class DeviceEventsViewHolder(private val parent: ViewGroup) : RecyclerView.ViewHolder(
    LayoutInflater.from(parent.context).inflate(R.layout.item_device_event, parent, false)
) {
    private val fallDrawables = arrayListOf(
        R.drawable.ic_fall1,
        R.drawable.ic_fall2,
        R.drawable.ic_fall3
    )

    private val shakeDrawables = arrayListOf(
        R.drawable.ic_shake1,
        R.drawable.ic_shake2
    )

    private val eventImage: ImageView = itemView.findViewById(R.id.eventImage)
    private val eventName: TextView = itemView.findViewById(R.id.item_event_name)
    private val eventComment: TextView = itemView.findViewById(R.id.item_event_comment)
    private val eventDuration: TextView = itemView.findViewById(R.id.item_event_duration)
    private var deviceEvent: DeviceEvent? = null

    /**
     * Items might be null if they are not paged in yet. PagedListAdapter will re-bind the
     * ViewHolder when Item is loaded.
     */
    fun bindTo(deviceEvent: DeviceEvent?) {
        this.deviceEvent = deviceEvent
        eventName.text = deviceEvent?.eventType
        eventComment.text = deviceEvent?.eventComment
        eventDuration.text = deviceEvent?.eventDuration.toString()

        if (eventName.text == "Fall") {
            val drawable: Int = when {
                (eventDuration.text).toString().toDouble() < 0.2 -> fallDrawables[0]
                (eventDuration.text).toString().toDouble() < 0.5 -> fallDrawables[1]
                (eventDuration.text).toString().toDouble() > 0.5 -> fallDrawables[2]
                else -> fallDrawables[0]
            }

            eventImage.setImageDrawable(
                ResourcesCompat.getDrawable
                    (parent.context.resources, drawable, null)
            )

        } else {
            val drawable = when {
                (eventDuration.text).toString().toDouble() < 0.2 -> shakeDrawables[0]
                (eventDuration.text).toString().toDouble() < 0.5 -> shakeDrawables[1]
                else -> shakeDrawables[0]
            }

            eventImage.setImageDrawable(
                ResourcesCompat.getDrawable
                    (parent.context.resources, R.drawable.ic_shake1, null)
            )
        }
    }
}