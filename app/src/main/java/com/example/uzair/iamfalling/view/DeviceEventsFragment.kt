package com.example.uzair.iamfalling.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.uzair.iamfalling.R
import com.example.uzair.iamfalling.adapter.DeviceEventsAdapter
import com.example.uzair.iamfalling.viewmodel.DeviceEventsViewModel
import kotlinx.android.synthetic.main.layout_main_recyclerview.*

/**
 * Shows a list of posts.
 * <p>
 * Posts are stored in a database, so swipes and additions edit the database directly, and the UI
 * is updated automatically using paging components.
 */
class DeviceEventsFragment : Fragment() {
    private lateinit var rootView: View
    private lateinit var viewModel: DeviceEventsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.layout_main_recyclerview, container, false)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Initialization Method
        init()
    }

    private fun init() {
        // Create adapter for the RecyclerView
        val adapter = DeviceEventsAdapter()
        layout_main_recyclerview.animation = null
        layout_main_recyclerview.setHasFixedSize(true)
        layout_main_recyclerview.layoutManager = LinearLayoutManager(activity)
        layout_main_recyclerview.adapter = adapter

        viewModel = ViewModelProviders.of(this).get(DeviceEventsViewModel::class.java)

        // Subscribe the adapter to the ViewModel, so the items in the adapter are refreshed
        // when the list changes
        viewModel.allPosts.observe(this, Observer { list ->
            adapter.submitList(list)
        })
    }
}