package com.example.uzair.iamfalling.adapter


import android.util.Log
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.uzair.iamfalling.R
import com.example.uzair.iamfalling.util.GridMenuItemsModel
import com.example.uzair.iamfalling.util.HomeMenuType
import com.example.uzair.iamfalling.view.DeviceEventsFragment
import com.example.uzair.iamfalling.view.HomeActivity
import com.example.uzair.iamfalling.view.HomeMenuFragment
import com.google.android.material.snackbar.Snackbar

class HomeMenuAdapter : RecyclerView.Adapter<HomeMenuAdapter.MyViewHolder>() {
    companion object {
        private val TAG = this::class.java.simpleName
    }

    private var namesArrList: SparseArray<GridMenuItemsModel>? = null
    private var activity: FragmentActivity? = null
    private var fragment: HomeMenuFragment? = null

    internal fun setValues(
        activity: FragmentActivity?,
        fragment: HomeMenuFragment,
        namesList: SparseArray<GridMenuItemsModel>
    ) {
        this.fragment = fragment
        this.namesArrList = namesList
        this.activity = activity
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_options_menu_items, parent, false)

        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val gridMenuItemsModel = namesArrList!!.valueAt(position)

        holder.menuItemImage.setImageResource(gridMenuItemsModel.imageDrawableResource)
        holder.menuItemText.text = activity!!.getString(gridMenuItemsModel.menuTextResource)
    }

    override fun getItemCount(): Int {
        return namesArrList!!.size()
    }

    inner class MyViewHolder internal constructor(view: View) : RecyclerView.ViewHolder(view) {
        internal var menuItemText: TextView =
            view.findViewById(R.id.grid_menu_items_for_recycler_view_menu_text)
        internal var menuItemImage: ImageView =
            view.findViewById(R.id.grid_menu_items_for_recycler_view_menu_image)
        private var rootLayout: ConstraintLayout = view.findViewById(R.id.item_menu)


        val listener = rootLayout.setOnClickListener {
            val homeActivity = activity as HomeActivity
            val menu = namesArrList?.get(adapterPosition)

            when (menu?.menuId) {
                HomeMenuType.ONLY_FALLS.value() -> {
                    homeActivity.startFallen(detectShakes = false)
                    showSnackbar(rootLayout, R.string.start_fall_service)
                }
                HomeMenuType.ONLY_SHAKES.value() -> {
                    homeActivity.startFallen(detectFalls = false)
                    showSnackbar(rootLayout, R.string.start_shake_service)
                }
                HomeMenuType.FALLS_AND_SHAKES.value() -> {
                    homeActivity.startFallen()
                    showSnackbar(rootLayout, R.string.start_fall_and_shake_service)
                }
                HomeMenuType.ALL.value() -> {
                    homeActivity.startFallen()
                    showSnackbar(rootLayout, R.string.start_fall_and_shake_service)
                }
                HomeMenuType.ALL_EVENTS.value() -> {
                    //Move to home menu fragment
                    activity?.let {
                        activity?.supportFragmentManager
                            ?.beginTransaction()
                            ?.replace(
                                R.id.root_home_activity,
                                DeviceEventsFragment(),
                                "HomeMenuFragment"
                            )
                            ?.addToBackStack("HomeMenuFragment")
                            ?.commit()
                    }
                }
                HomeMenuType.STOP_SERVICE.value() -> {
                    homeActivity.stopFallen()
                    showSnackbar(rootLayout, R.string.service_stopped)
                }
                else -> {
                    Log.d(TAG, "No menu item satisfied")
                }
            }
        }
    }

    private fun showSnackbar(rootLayout: View, @StringRes messageResource: Int) {
        Snackbar.make(rootLayout, messageResource, Snackbar.LENGTH_LONG).show()
    }
}