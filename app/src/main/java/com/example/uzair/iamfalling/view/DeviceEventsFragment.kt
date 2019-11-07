package com.example.uzair.iamfalling.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.uzair.fallen.database.model.DeviceEvent
import com.example.uzair.iamfalling.R
import com.example.uzair.iamfalling.adapter.DeviceEventsAdapter
import com.example.uzair.iamfalling.viewmodel.DeviceEventsViewModel
import kotlinx.android.synthetic.main.device_events_fragment.*

/**
 * Shows a list device events.
 * <p>
 * The UI is updated automatically using paging components.
 */
class DeviceEventsFragment : Fragment() {
    companion object {
        val TAG = DeviceEventsFragment::class.java.simpleName
    }

    private lateinit var rootView: View
    private lateinit var viewModel: DeviceEventsViewModel
    private val deviceEventAdapter by lazy{DeviceEventsAdapter()}

    /**
     * Called when the OS starts to draw the view of this fragment,
     * return the root view of fragment layout of which the views should be rendered
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.device_events_fragment, container, false)
        return rootView
    }

    /**
     * Called once the view is created, after the view is fully drawn,
     * no view can be null here, do any functionality you want to do with the views here
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Initialization Method
        init()
    }

    /**
     * Perform all the views and other initialization calls here
     */
    private fun init() {
        //Set screen title
        activity?.title = getString(R.string.device_event_screen_title)

        val recyclerView = device_events_layout as RecyclerView
        recyclerView.animation = null
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = deviceEventAdapter

        viewModel = ViewModelProviders.of(this).get(DeviceEventsViewModel::class.java)

        // Subscribe the adapter to the ViewModel, so the items in the adapter are refreshed
        // when the list changes, and observe for the changes
        viewModel.allPosts.observe(this, Observer { list ->
            //If the list is empty
            if (list.isEmpty())
                showNoDataUi()
            else
                showDataUi(deviceEventAdapter, list)
        })
    }

    /**
     * Show all the event data layout to the user and hide the
     * noEventFound layout.
     */
    private fun showDataUi(adapter: DeviceEventsAdapter, list: PagedList<DeviceEvent>) {
        no_event_found_layout.visibility = View.GONE
        device_events_layout.visibility = View.VISIBLE

        //Update the UI
        adapter.submitList(list)
    }

    /**
     * Show the noEventFound layout to the user and hide allEventsLayout
     */
    private fun showNoDataUi() {
        no_event_found_layout.visibility = View.VISIBLE
        device_events_layout.visibility = View.GONE
    }

    /**
     * Provide the adapter used to show all the device events
     */
    fun getAdapter() = deviceEventAdapter
}