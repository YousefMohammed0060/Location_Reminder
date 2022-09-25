package com.udacity.project4.locationreminders.savereminder

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import com.google.common.truth.Truth.assertThat
import com.udacity.project4.R
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import getOrAwaitValue
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.hamcrest.CoreMatchers.nullValue

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.junit.After

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config


//@Config(sdk = [Build.VERSION_CODES.P]) // set the target sdk to P for test
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {



    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()



    private lateinit var remindersRepository: FakeDataSource

    //Subject under test
    private lateinit var viewModel: SaveReminderViewModel

    @Before
    fun setupViewModel() {
        remindersRepository = FakeDataSource()
        viewModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext(), remindersRepository)
    }

    @After
    fun tearDown() {
        stopKoin()
    }


    //  test to check what will be happen if data missing the title
    @Test
    fun validateEnteredData_EmptyTitleAndUpdateSnackBar() {
        // GIVEN
        val reminder = ReminderDataItem("", "Description", "My School", 7.32323, 6.54343,"1")

        // THEN
        assertThat(viewModel.validateEnteredData(reminder)).isFalse()
        assertThat(viewModel.showSnackBarInt.getOrAwaitValue()).isEqualTo(R.string.err_enter_title)
    }


    //  test to check what will be happen if data missing the location name
    @Test
    fun validateEnteredData_EmptyLocationAndUpdateSnackBar() {
        // GIVEN
        val reminder = ReminderDataItem("Title", "Description", "", 7.32323, 6.54343,"2")

        // THEN
        assertThat(viewModel.validateEnteredData(reminder)).isFalse()
        assertThat(viewModel.showSnackBarInt.getOrAwaitValue()).isEqualTo(R.string.err_select_location)
    }


    //  test to check what will be happen if all data submitted
    @Test
    fun saveReminder_showLoading(){
        // GIVEN
        val reminder = ReminderDataItem("Title", "Description", "Airport", 7.32323, 6.54343,"3")

        // WHEN
        mainCoroutineRule.pauseDispatcher()
        viewModel.saveReminder(reminder)

        // THEN
        assertThat(viewModel.showLoading.getOrAwaitValue()).isTrue()
        mainCoroutineRule.resumeDispatcher()
        assertThat(viewModel.showLoading.getOrAwaitValue()).isFalse()
    }
}