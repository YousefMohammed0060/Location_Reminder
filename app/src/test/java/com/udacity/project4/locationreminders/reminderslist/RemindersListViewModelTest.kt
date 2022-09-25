package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.R
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.google.common.truth.Truth.assertThat
import getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.junit.After

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()


    private lateinit var remindersRepository: FakeDataSource

    //Subject under test
    private lateinit var viewModel: RemindersListViewModel

    @Before
    fun setupViewModel() {
        remindersRepository = FakeDataSource()
        viewModel =
            RemindersListViewModel(ApplicationProvider.getApplicationContext(), remindersRepository)
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    // test to check what will be happen while the app waiting for data
    @Test
    fun loadReminders_showLoading() {
        mainCoroutineRule.pauseDispatcher() //pausing the dispatcher to make any new coroutines will not execute immediately

        // WHEN
        viewModel.loadReminders() // ask to load the reminders to show them into the recyclerView
        assertThat(viewModel.showLoading.getOrAwaitValue()).isTrue() // while they loading check that loading states is true
        mainCoroutineRule.resumeDispatcher() //make the dispatcher resume to get other process

        // THEN
        assertThat(viewModel.showLoading.getOrAwaitValue()).isFalse() // check that loading is finished and loading states is false
    }
    // end of test


    //  test to check what will be happen if list have a data of reminder
    @Test
    fun loadReminders_remainderListNotEmpty() = mainCoroutineRule.runBlockingTest {
        // GIVEN
        val reminder = ReminderDTO("My Store", "Pick Stuff", "Abuja", 6.454202, 7.599545) //add the reminder data to create an object

        // WHEN
        remindersRepository.saveReminder(reminder) // using the repository and dispatchers to ask database to save the reminder
        viewModel.loadReminders() // ask to load the reminders to show them into the recyclerView

        // THEN
        assertThat(viewModel.remindersList.getOrAwaitValue()).isNotEmpty() // check that list in the viewModel is not empty
    }
    // end of test


    // test to check what will be happen if list is empty
    @Test
    fun loadReminders_updateSnackBarValue() {
        mainCoroutineRule.pauseDispatcher() //pausing the dispatcher to make any new coroutines will not execute immediately

        // WHEN
        remindersRepository.setReturnError(true) // the return error to check what the system will show
        viewModel.loadReminders() // ask to load the reminders to show them into the recyclerView
        mainCoroutineRule.resumeDispatcher() //make the dispatcher resume to get other process

        // THEN
        assertThat(viewModel.showSnackBar.getOrAwaitValue()).isEqualTo("Error getting reminders") // check that the system return to user that "Error getting reminders" to let him know that their is problem  to get data
    }
    // end of test
}


