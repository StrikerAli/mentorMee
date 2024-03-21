package com.aliashraf.assignment2

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.espresso.idling.CountingIdlingResource
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.hamcrest.Matchers.allOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class MainActivityTest1 {

    // Create an IdlingResource for waiting until the authentication is complete
    private val countingIdlingResource = CountingIdlingResource("Authentication")

    @get:Rule
    var mActivityScenarioRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun mainActivityTest1() {
        // Wait for 1 second before proceeding to the next action
        Thread.sleep(1000)

        // Register the IdlingResource before performing any action
        countingIdlingResource.increment()

        // Find and click the "Login" button
        onView(
            allOf(
                withId(R.id.loginBtn),
                withText("Login"),
                isDisplayed()
            )
        ).perform(click())

        // Wait for the authentication process to complete
        countingIdlingResource.decrement()

        // Verify that the specific ImageView in HomeActivity is displayed
        onView(withId(com.aliashraf.assignment2.R.id.textView6)).check(matches(isDisplayed()))
    }
}