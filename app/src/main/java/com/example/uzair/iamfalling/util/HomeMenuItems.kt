package com.example.uzair.iamfalling.util

import android.util.SparseArray
import com.example.uzair.iamfalling.R

/**
 * A model class that holds all the menu items, in real project keep it static
 */
class HomeMenuItems {
    private var homeMenuItems = SparseArray<GridMenuItemsModel>()

    fun getMenuList(): SparseArray<GridMenuItemsModel> {
        /*Adding home menu items to the list instance once*/
        homeMenuItems.put(
            HomeMenuType.ONLY_FALLS.value(),
            GridMenuItemsModel(
                HomeMenuType.ONLY_FALLS.value(), R.drawable.ic_fall1,
                R.string.home_menu_detect_only_falls
            )
        )
        homeMenuItems.put(
            HomeMenuType.ONLY_SHAKES.value(),
            GridMenuItemsModel(
                HomeMenuType.ONLY_SHAKES.value(), R.drawable.ic_shake1,
                R.string.home_menu_detect_only_shakes
            )
        )
        homeMenuItems.put(
            HomeMenuType.FALLS_AND_SHAKES.value(),
            GridMenuItemsModel(
                HomeMenuType.FALLS_AND_SHAKES.value(), R.drawable.ic_fall2,
                R.string.home_menu_detect_falls_and_shakes
            )
        )
        homeMenuItems.put(
            HomeMenuType.ALL.value(),
            GridMenuItemsModel(
                HomeMenuType.ALL.value(), R.drawable.ic_shake2,
                R.string.home_menu_detect_falls_frequent_falls_and_shakes
            )
        )
        homeMenuItems.put(
            HomeMenuType.ALL_EVENTS.value(),
            GridMenuItemsModel(
                HomeMenuType.ALL_EVENTS.value(), R.drawable.ic_list,
                R.string.home_menu_show_all_events
            )
        )
        homeMenuItems.put(
            HomeMenuType.STOP_SERVICE.value(),
            GridMenuItemsModel(
                HomeMenuType.STOP_SERVICE.value(), R.drawable.ic_stop_services,
                R.string.home_menu_stop_service
            )
        )

        return homeMenuItems
    }
}