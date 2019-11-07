package com.example.uzair.iamfalling.view


import android.app.Application
import android.view.View
import android.view.ViewGroup
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import com.example.uzair.fallen.database.model.DeviceEvent
import com.example.uzair.fallen.repository.DeviceEventsRepository
import com.example.uzair.fallen.util.DeviceEventType
import com.example.uzair.iamfalling.R
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.TypeSafeMatcher
import org.hamcrest.core.IsInstanceOf
import org.junit.Rule
import org.junit.Test

@LargeTest
class DeviceEventsFragmentTest {
    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(HomeActivity::class.java)

    private val application by lazy {
        InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application
    }

    private val repository by lazy {
        DeviceEventsRepository(application)
    }

    /**
     * This test will only run if the this app is a fresh install
     */
    @Test
    fun testIfNoEventFoundIsDisplayed() {
        //Get the current visible fragment
        var currentVisibleFragment = getCurrentVisibleFragment()

        if (currentVisibleFragment is HomeMenuFragment) {
            //Go to all device events fragment
            val constraintLayout = onView(
                allOf(
                    withId(R.id.item_menu_layout),
                    childAtPosition(
                        allOf(
                            withId(R.id.layout_main_recyclerview),
                            childAtPosition(
                                withId(R.id.root_home_activity),
                                0
                            )
                        ),
                        4
                    ),
                    isDisplayed()
                )
            )
            constraintLayout.perform(ViewActions.click())
        }

        //Get the current visible fragment
        currentVisibleFragment = getCurrentVisibleFragment()

        if (currentVisibleFragment is DeviceEventsFragment) {
            val adapter = currentVisibleFragment.getAdapter()

            //Tests, when there are no item
            if (adapter.itemCount == 0) {
                val textView = onView(
                    allOf(
                        withText("No Event Found"),
                        childAtPosition(
                            allOf(
                                withId(R.id.no_event_found_layout),
                                childAtPosition(
                                    IsInstanceOf.instanceOf(android.widget.FrameLayout::class.java),
                                    0
                                )
                            ),
                            1
                        ),
                        isDisplayed()
                    )
                )
                textView.check(matches(withText("No Event Found")))
            }
        }
    }

    @Test
    fun testIfDeviceEventsAreSavedToDataSourceAndAreShown() {
        //Add a fake event in the database first
        repository.saveDeviceEvent(
            DeviceEvent(
                0,
                DeviceEventType.FALL.value(),
                "02 Aug 18 10:20:12",
                0.0
            )
        )

        //Get the current visible fragment
        var currentVisibleFragment = getCurrentVisibleFragment()

        if (currentVisibleFragment is HomeMenuFragment) {
            //Go to all device events fragment
            val constraintLayout = onView(
                allOf(
                    withId(R.id.item_menu_layout),
                    childAtPosition(
                        allOf(
                            withId(R.id.layout_main_recyclerview),
                            childAtPosition(
                                withId(R.id.root_home_activity),
                                0
                            )
                        ),
                        4
                    ),
                    isDisplayed()
                )
            )
            constraintLayout.perform(ViewActions.click())
        }

        //Get the current visible fragment
        currentVisibleFragment = getCurrentVisibleFragment()

        if (currentVisibleFragment is DeviceEventsFragment) {
            val adapter = currentVisibleFragment.getAdapter()

            //Tests, when there are no item
            if (adapter.itemCount > 0) {
                onView(
                    allOf(
                        withId(R.id.item_device_event_layout),
                        childAtPosition(
                            allOf(
                                withId(R.id.layout_main_recyclerview),
                                childAtPosition(
                                    withId(R.id.root_home_activity),
                                    0
                                )
                            ),
                            1
                        ),
                        isDisplayed()
                    )
                )
            }
        }
    }

    private fun childAtPosition(
        parentMatcher: Matcher<View>, position: Int
    ): Matcher<View> {

        return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("Child at position $position in parent ")
                parentMatcher.describeTo(description)
            }

            public override fun matchesSafely(view: View): Boolean {
                val parent = view.parent
                return parent is ViewGroup && parentMatcher.matches(parent)
                        && view == parent.getChildAt(position)
            }
        }
    }

    private fun getCurrentVisibleFragment() =
        mActivityTestRule.activity.supportFragmentManager.findFragmentById(R.id.root_home_activity)
}
