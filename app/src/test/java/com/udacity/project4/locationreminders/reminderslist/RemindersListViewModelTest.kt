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
        mainCoroutineRule.pauseDispatcher()

        // WHEN
        viewModel.loadReminders()
        assertThat(viewModel.showLoading.getOrAwaitValue()).isTrue()
        mainCoroutineRule.resumeDispatcher()

        // THEN
        assertThat(viewModel.showLoading.getOrAwaitValue()).isFalse()

    }

    //  test to check what will be happen if list have a data of reminder
    @Test
    fun loadReminders_remainderListNotEmpty() = mainCoroutineRule.runBlockingTest {
        // GIVEN
        val reminder = ReminderDTO("My Store", "Pick Stuff", "Abuja", 6.454202, 7.599545)

        // WHEN
        remindersRepository.saveReminder(reminder)
        viewModel.loadReminders()

        // THEN
        assertThat(viewModel.remindersList.getOrAwaitValue()).isNotEmpty()
    }


    // test to check what will be happen if list is empty
    @Test
    fun loadReminders_updateSnackBarValue() {
        mainCoroutineRule.pauseDispatcher()

        // WHEN
        remindersRepository.setReturnError(true)
        viewModel.loadReminders()
        mainCoroutineRule.resumeDispatcher()

        // THEN
        assertThat(viewModel.showSnackBar.getOrAwaitValue()).isEqualTo("Error getting reminders")
    }
}


