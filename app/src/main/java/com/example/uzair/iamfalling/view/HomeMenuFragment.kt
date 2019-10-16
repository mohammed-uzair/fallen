package com.example.uzair.iamfalling.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import com.example.uzair.iamfalling.R
import com.example.uzair.iamfalling.adapter.HomeMenuAdapter
import com.example.uzair.iamfalling.util.HomeMenuItems
import com.example.uzair.iamfalling.util.NUMBER_OF_COLUMNS_IN_MENU_GRID
import kotlinx.android.synthetic.main.layout_main_recyclerview.*


class HomeMenuFragment : Fragment() {
    private lateinit var rootView: View
    private val adapter = HomeMenuAdapter()

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

        //Do the processing
        process()
    }

    /*View LISTENER - Implemented methods*/
    private fun init() {
        //Add the title
        activity?.title = getString(R.string.home_screen_title)
    }

    private fun process() {
        //Setting up the recycler view and the adapter.
        layout_main_recyclerview.layoutManager =
            GridLayoutManager(activity, NUMBER_OF_COLUMNS_IN_MENU_GRID)

        layout_main_recyclerview.itemAnimator = DefaultItemAnimator()

        adapter.setValues(activity, this@HomeMenuFragment, HomeMenuItems().getMenuList())

        layout_main_recyclerview.adapter = adapter
        layout_main_recyclerview.setHasFixedSize(true)
    }
}