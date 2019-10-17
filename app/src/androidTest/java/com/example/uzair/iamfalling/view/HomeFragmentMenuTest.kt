package com.example.uzair.iamfalling.view

import android.view.View
import android.view.ViewGroup
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import com.example.uzair.iamfalling.R
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.TypeSafeMatcher
import org.junit.Rule
import org.junit.Test

@LargeTest
class HomeFragmentMenuTest {

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(HomeActivity::class.java)

    @Test
    fun homeFragmentTest() {
        val viewGroup = onView(
            allOf(
                withId(R.id.item_menu),
                childAtPosition(
                    allOf(
                        withId(R.id.layout_main_recyclerview),
                        childAtPosition(
                            withId(R.id.root_home_activity),
                            0
                        )
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        viewGroup.check(matches(isDisplayed()))

        val imageView = onView(
            allOf(
                withId(R.id.grid_menu_items_for_recycler_view_menu_image),
                withContentDescription("Event Image"),
                childAtPosition(
                    allOf(
                        withId(R.id.item_menu),
                        childAtPosition(
                            withId(R.id.layout_main_recyclerview),
                            0
                        )
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        imageView.check(matches(isDisplayed()))

        val textView = onView(
            allOf(
                withId(R.id.grid_menu_items_for_recycler_view_menu_text), withText("Only Falls"),
                childAtPosition(
                    allOf(
                        withId(R.id.item_menu),
                        childAtPosition(
                            withId(R.id.layout_main_recyclerview),
                            0
                        )
                    ),
                    1
                ),
                isDisplayed()
            )
        )
        textView.check(matches(withText("Only Falls")))

        val textView2 = onView(
            allOf(
                withId(R.id.grid_menu_items_for_recycler_view_menu_text), withText("Only Falls"),
                childAtPosition(
                    allOf(
                        withId(R.id.item_menu),
                        childAtPosition(
                            withId(R.id.layout_main_recyclerview),
                            0
                        )
                    ),
                    1
                ),
                isDisplayed()
            )
        )
        textView2.check(matches(withText("Only Falls")))

        val constraintLayout = onView(
            allOf(
                withId(R.id.item_menu),
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
        constraintLayout.perform(click())

        val recyclerView = onView(
            allOf(
                withId(R.id.layout_main_recyclerview),
                childAtPosition(
                    allOf(
                        withId(R.id.root_home_activity),
                        childAtPosition(
                            withId(android.R.id.content),
                            0
                        )
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        recyclerView.check(matches(isDisplayed()))
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
}
