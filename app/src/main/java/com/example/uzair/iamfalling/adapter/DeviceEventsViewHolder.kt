package com.example.uzair.iamfalling.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.uzair.fallen.database.model.DeviceEvent
import com.example.uzair.fallen.util.DeviceEventType
import com.example.uzair.iamfalling.R

/**
 * A simple ViewHolder that can bind an event item. It also accepts null items since the data may
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

    private val eventImage: ImageView = itemView.findViewById(R.id.eventImage)
    private val eventName: TextView = itemView.findViewById(R.id.item_event_name)
    private val eventOccurredTime: TextView = itemView.findViewById(R.id.item_event_occurred_time)
    private val eventDuration: TextView = itemView.findViewById(R.id.item_event_duration)
    private val eventComment: TextView = itemView.findViewById(R.id.item_event_comment)

    /**
     * Items might be null if they are not paged in yet. PagedListAdapter will re-bind the
     * ViewHolder when Item is loaded.
     */
    fun bindTo(deviceEvent: DeviceEvent?) {
        deviceEvent?.let {
            eventName.text = deviceEvent.eventType
            eventOccurredTime.text = deviceEvent.eventOccurredTime
            eventDuration.text = String.format(deviceEvent.eventDuration.toString() + "%s", " Sec")

            if (eventName.text == DeviceEventType.FALL.name) {
                val drawable: Int?

                when {
                    deviceEvent.eventDuration < 0.2 -> {
                        drawable = fallDrawables[0]
                        eventComment.text =
                            parent.context.getString(R.string.fall_events_comments_low_duration)
                    }
                    deviceEvent.eventDuration < 0.5 -> {
                        drawable = fallDrawables[1]
                        eventComment.text =
                            parent.context.getString(R.string.fall_events_comments_medium_duration)
                    }
                    deviceEvent.eventDuration > 0.5 -> {
                        drawable = fallDrawables[2]
                        eventComment.text =
                            parent.context.getString(R.string.fall_events_comments_high_duration)
                    }
                    else -> {
                        drawable = fallDrawables[0]
                        eventComment.text =
                            parent.context.getString(R.string.notification_fall_detected_message)
                    }
                }

                drawable.let {
                    eventImage.setImageDrawable(
                        ResourcesCompat.getDrawable
                            (parent.context.resources, it, null)
                    )
                }

            } else {
                eventComment.text = parent.context.getString(R.string.shake_events_comment)
                eventImage.setImageDrawable(
                    ResourcesCompat.getDrawable
                        (parent.context.resources, R.drawable.ic_shake1, null)
                )
            }
        }
    }
}