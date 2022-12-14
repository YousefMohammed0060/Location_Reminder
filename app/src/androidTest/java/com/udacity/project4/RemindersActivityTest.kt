package com.udacity.project4

import android.app.Activity
import android.app.Application
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorActivity
import com.udacity.project4.utils.EspressoIdlingResource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.Matchers.`is`
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get
import org.hamcrest.core.IsNot.not
import org.koin.test.KoinTest


//Please not that these tests should be run on API 29 or less
@RunWith(AndroidJUnit4::class)
@LargeTest
//END TO END test to black box test the app
class RemindersActivityTest :
    AutoCloseKoinTest() {// Extended Koin Test - embed autoclose @after method to close Koin after every test

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application

    // An idling resource that waits for Data Binding to have no pending bindings.
    private val dataBindingIdlingResource = DataBindingIdlingResource()

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        //Get our real repository
        repository = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }




    /**
     * Idling resources tell Espresso that the app is idle or busy. This is needed when operations
     * are not scheduled in the main Looper (for example when executed on a different thread).
     */
    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    /**
     * Unregister your Idling Resource so it can be garbage collected and does not leak any memory.
     */
    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }

    // this test make a snakeBar if title is missing
    @Test
    fun saveReminderScreen_showSnackBarTitleError() {
        //WHEN
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java) // use ActivityScenario to launch the RemindersActivity
        dataBindingIdlingResource.monitorActivity(activityScenario) // ask the DataBindingIdlingResource to monitor the ui
        onView(withId(R.id.addReminderFAB)).perform(click()) // ask the ui to click the float action button and navigate to save reminder fragment
        onView(withId(R.id.saveReminder)).perform(click()) // ask the ui to click the save button to check the data an save it into database

        //THEN
        val snackBarMessage = appContext.getString(R.string.err_enter_title) // initialize the message that will be show in snackBar
        onView(withText(snackBarMessage)).check(matches(isDisplayed())) // check that the both messages is equal
        activityScenario.close() // closing the monitoring
    }
    // end of test



    // this test make a snakeBar if location is missing
    @Test
    fun saveReminderScreen_showSnackBarLocationError() {
        //WHEN
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java) // use ActivityScenario to launch the RemindersActivity
        dataBindingIdlingResource.monitorActivity(activityScenario) // ask the DataBindingIdlingResource to monitor the ui
        onView(withId(R.id.addReminderFAB)).perform(click()) // ask the ui to click the float action button and navigate to save reminder fragment
        onView(withId(R.id.reminderTitle)).perform(typeText("Title")) // ask the ui to open the keyboard and type "Title" into reminderTitle editText
        Espresso.closeSoftKeyboard() // ask the ui to close the keyboard
        onView(withId(R.id.saveReminder)).perform(click()) // ask the ui to click the save button to check the data an save it into database

        //THEN
        val snackBarMessage = appContext.getString(R.string.err_select_location) // initialize the message that will be show in snackBar
        onView(withText(snackBarMessage)).check(matches(isDisplayed())) // check that the both messages is equal
        activityScenario.close() // closing the monitoring
    }
    // end of test


    // this test check that their no data missing and make snakeBar that data is saved
    @Test
    fun saveReminderScreen_showToastMessage() {

        //WHEN
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java) // use ActivityScenario to launch the RemindersActivity
        dataBindingIdlingResource.monitorActivity(activityScenario) // ask the DataBindingIdlingResource to monitor the ui
        onView(withId(R.id.addReminderFAB)).perform(click()) // ask the ui to click the float action button and navigate to save reminder fragment
        onView(withId(R.id.reminderTitle)).perform(typeText("Title")) // ask the ui to open the keyboard and type "Title" into reminderTitle editText
        Espresso.closeSoftKeyboard() // ask the ui to close the keyboard
        onView(withId(R.id.reminderDescription)).perform(typeText("Description")) // ask the ui to open the keyboard and type "Description" into reminderDescription editText
        Espresso.closeSoftKeyboard() // ask the ui to close the keyboard
        onView(withId(R.id.selectLocation)).perform(click()) // ask the ui to click selectLocation textView and navigate to map fragment
        onView(withId(R.id.map)).perform(longClick()) // ask the ui to long click to the location
        onView(withId(R.id.button_save)).perform(click())  // ask the ui to click button_save  and navigate to save reminder fragment
        onView(withId(R.id.saveReminder)).perform(click()) // ask the ui to click the save button to check the data an save it into database

        //THEN
        onView(withText(R.string.reminder_saved)).inRoot(withDecorView(
            CoreMatchers.not(
                CoreMatchers.`is`(
                    getActivity(activityScenario).window.decorView
                )
            )
        )) // after check that all data entered it ask database to save and navigate to reminders fragment
        activityScenario.close() // closing the monitoring
    }
    // end of test

    private fun getActivity(activityScenario: ActivityScenario<RemindersActivity>): Activity {
        lateinit var activity: Activity
        activityScenario.onActivity {
            activity = it
        }
        return activity
    }


}
