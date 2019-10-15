package com.example.uzair.iamfalling.adapter


import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.uzair.iamfalling.R
import com.example.uzair.iamfalling.util.GridMenuItemsModel
import com.example.uzair.iamfalling.view.HomeMenuFragment

class HomeMenuAdapter : RecyclerView.Adapter<HomeMenuAdapter.MyViewHolder>() {
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
        private var linearLayout: LinearLayout = view.findViewById(R.id.item_menu)

        init {
            linearLayout.setOnClickListener { v ->
                //Give the focus
                //Enable all the hurdle parameters
                view.isFocusable = true
                view.isFocusableInTouchMode = true

                //Request the focus now
                if (view.requestFocus()) {
                    //Once the focus is gained
                    //Revoke the focusable in touch mode to false, to avoid any un foreseen results on UI
                    view.isFocusableInTouchMode = false
                }
            }
        }
    }
}
