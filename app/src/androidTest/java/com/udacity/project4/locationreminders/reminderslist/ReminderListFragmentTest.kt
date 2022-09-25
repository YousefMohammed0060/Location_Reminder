package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.content.Context
import android.os.Bundle
import android.service.autofill.Validators.not
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import com.udacity.project4.R
import com.udacity.project4.locationreminders.FakeDataSource
import com.udacity.project4.locationreminders.ReminderDescriptionViewModel
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.inject
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.hamcrest.core.IsNot.not
import org.mockito.AdditionalMatchers.not
import org.mockito.ArgumentMatchers.matches


@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest: KoinTest {

    private val dataSource: ReminderDataSource by inject()

    private val reminder1 = ReminderDTO("Reminder1", "Description1", "Location1", 1.0, 1.0,"1") //add the reminder1 data to create an object1
    private val reminder2 = ReminderDTO("Reminder2", "Description2", "location2", 2.0, 2.0, "2") //add the reminder2 data to create an object2
    private val reminder3 = ReminderDTO("Reminder3", "Description3", "location3", 3.0, 3.0, "3") //add the reminder3 data to create an object3

    @Before
    fun initRepository() {
        stopKoin()
        /**
         * use Koin Library as a service locator
         */
        val myModule = module {
            //Declare a ViewModel - be later inject into Fragment with dedicated injector using by viewModel()
            viewModel {
                RemindersListViewModel(
                    get(),
                    get()
                )
            }
            single {
                FakeDataSource() as ReminderDataSource
            }
        }

        startKoin {
            androidContext(getApplicationContext())
            modules(listOf(myModule))
        }
    }

    @After
    fun cleanupDb() = runBlockingTest {
        dataSource.deleteAllReminders()
    }



    // this test trying to test that UI can load reminders in the recyclerView
    @Test
    fun reminderList_DisplayedInUi() = runBlockingTest{
        // GIVEN - add reminders
        dataSource.saveReminder(reminder1) // call the room database to save the reminder1
        dataSource.saveReminder(reminder2) // call the room database to save the reminder2
        dataSource.saveReminder(reminder3) // call the room database to save the reminder3

        //WHEN - on ReminderListFragment
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme) // here we ake to launch the fragment to apply the test
        val navController = mock(NavController::class.java) // here we setup the Navigation Controller
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController) // now we ask the app Navigation to fire the fragment and the Navigation Controller
        }
        //THEN - data loaded into the right place
        onView(withText(reminder1.title)).check(matches(isDisplayed())) // here checking that the title of reminder1 is in his place on the screen
        onView(withText(reminder2.description)).check(matches(isDisplayed())) // here checking that the description of reminder2 is in his place on the screen
        onView(withText(reminder3.title)).check(matches(isDisplayed())) // here checking that the title of reminder3 is in his place on the screen
        onView(withId(R.id.noDataTextView)).check(matches(not(isDisplayed())))  // here checking that the noDataTextView is invisible because the data founded

    }
    // end of test


    // this test trying to test that UI show us that no data if the room database is empty
    @Test
    fun reminderList_noReminders() = runBlockingTest{
        // GIVEN - delete all data
        dataSource.deleteAllReminders() // ask database to delete all records in the database

        //WHEN - on the ReminderListFragment
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme) // here we ake to launch the fragment to apply the test
        val navController = mock(NavController::class.java) // here we setup the Navigation Controller
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController) // now we ask the app Navigation to fire the fragment and the Navigation Controller
        }

        //THEN - shows that no data
        onView(withText(R.string.no_data)).check(matches(isDisplayed())) // here checking that the noDataTextView has the value of no_data
        onView(withId(R.id.noDataTextView)).check(matches(isDisplayed())) // here checking that the noDataTextView is in his place on the screen
        onView(withText(reminder1.title)).check(doesNotExist()) // here check the the value of reminder1 title is not exist, because their is no data founded

    }
    // end of test


    // this test trying to test that when we click the fab_btn it navigate to destination fragment
    @Test
    fun clickFab_navigateToReminderFragment() = runBlockingTest {
        // GIVEN - On the home screen
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme) // here we ake to launch the fragment to apply the test
        val navController = mock(NavController::class.java) // here we setup the Navigation Controller
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController) // now we ask the app Navigation to fire the fragment and the Navigation Controller
        }

        // WHEN - Click on the "+" button
        onView(withId(R.id.addReminderFAB)).perform(click()) // ask the ui to click the float action button

        // THEN - Verify that we navigate to the save reminder fragment
        verify(navController).navigate(ReminderListFragmentDirections.toSaveReminder()) // check that the click navigate to the right place
    }
    // end of test

}