package com.aliashraf.assignment2


import android.view.View
import android.view.ViewGroup
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.`is`
import org.hamcrest.TypeSafeMatcher
import org.hamcrest.core.IsInstanceOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class MainActivityTest3 {

    @Rule
    @JvmField
    var mActivityScenarioRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun mainActivityTest3() {
        Thread.sleep(1000)
        val appCompatButton = onView(
            allOf(
                withId(R.id.loginBtn), withText("Login"),
                childAtPosition(
                    allOf(
                        withId(R.id.constraintLayout2),
                        childAtPosition(
                            withClassName(`is`("androidx.constraintlayout.widget.ConstraintLayout")),
                            1
                        )
                    ),
                    6
                ),
                isDisplayed()
            )
        )
        appCompatButton.perform(click())
        Thread.sleep(3000)

        val cardView = onView(
            allOf(
                withId(R.id.mentor1),
                childAtPosition(
                    allOf(
                        withId(R.id.yes_Scroll_View),
                        childAtPosition(
                            withId(R.id.horizontalScrollView3),
                            0
                        )
                    ),
                    0
                )
            )
        )
        cardView.perform(scrollTo(), click())
        Thread.sleep(1000)
        val view = onView(
            allOf(
                withId(R.id.backBtn),
                childAtPosition(
                    allOf(
                        withId(R.id.constraintLayout),
                        childAtPosition(
                            withClassName(`is`("androidx.constraintlayout.widget.ConstraintLayout")),
                            0
                        )
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        view.perform(click())
        Thread.sleep(1000)
        val cardView2 = onView(
            childAtPosition(
                allOf(
                    withId(R.id.yes_Scroll_View),
                    childAtPosition(
                        withId(R.id.horizontalScrollView3),
                        0
                    )
                ),
                1
            )
        )
        cardView2.perform(scrollTo(), click())
        Thread.sleep(1000)
        val view2 = onView(
            allOf(
                withId(R.id.backBtn),
                childAtPosition(
                    allOf(
                        withId(R.id.constraintLayout),
                        childAtPosition(
                            withClassName(`is`("androidx.constraintlayout.widget.ConstraintLayout")),
                            0
                        )
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        view2.perform(click())
        Thread.sleep(1000)
        val cardView3 = onView(
            childAtPosition(
                allOf(
                    withId(R.id.yes_Scroll_View),
                    childAtPosition(
                        withId(R.id.horizontalScrollView3),
                        0
                    )
                ),
                2
            )
        )
        cardView3.perform(scrollTo(), click())
        Thread.sleep(1000)
        val button = onView(
            allOf(
                withId(R.id.dropReviewBtn), withText("Drop a review \uD83D\uDD00"),
                withParent(
                    allOf(
                        withId(R.id.constraintLayout2),
                        withParent(IsInstanceOf.instanceOf(android.view.ViewGroup::class.java))
                    )
                ),
                isDisplayed()
            )
        )
        button.check(matches(isDisplayed()))

        val textView = onView(
            allOf(
                withId(R.id.desc), withText("Hello My name is Muhamamd Hani, "),
                withParent(
                    allOf(
                        withId(R.id.constraintLayout),
                        withParent(IsInstanceOf.instanceOf(android.view.ViewGroup::class.java))
                    )
                ),
                isDisplayed()
            )
        )
        textView.check(matches(withText("Hello My name is Muhamamd Hani, ")))
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
