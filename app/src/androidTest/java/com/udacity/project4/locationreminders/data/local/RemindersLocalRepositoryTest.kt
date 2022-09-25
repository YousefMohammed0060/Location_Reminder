package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert
import org.junit.Assert.assertThat


@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var remindersLocalRepository: RemindersLocalRepository

    private lateinit var database: RemindersDatabase

    @Before
    fun setup() {
        // Using an in-memory database so that the information stored here disappears when the
        // process is killed.
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()

        remindersLocalRepository = RemindersLocalRepository(
            database.reminderDao(),
            Dispatchers.Main
        )
    }

    @After
    fun cleanUp() {
        database.close()
    }


    // this test trying to save the reminder and call it from room database
    @Test
    fun saveReminder_retrieveReminderById() = runBlocking {
        //GIVEN
        val reminder = ReminderDTO("My Shop", "Get to the Shop", "Abuja", 6.54545, 7.54545)
        remindersLocalRepository.saveReminder(reminder)

        //WHEN
        val result = remindersLocalRepository.getReminder(reminder.id) as? Result.Success

        //THEN
        MatcherAssert.assertThat(result is Result.Success, `is`(true))
        result as Result.Success
        MatcherAssert.assertThat(result.data.title, `is`(reminder.title))
        MatcherAssert.assertThat(result.data.description, `is`(reminder.description))
        MatcherAssert.assertThat(result.data.latitude, `is`(reminder.latitude))
        MatcherAssert.assertThat(result.data.longitude, `is`(reminder.longitude))
        MatcherAssert.assertThat(result.data.location, `is`(reminder.location))
    }


    // this test trying to delete all reminders and check that room database is empty
    @Test
    fun deleteReminders_EmptyList()= runBlocking {
        //GIVEN
        val reminder = ReminderDTO("My Shop", "Get to the Shop", "Abuja", 6.54545, 7.54545)
        remindersLocalRepository.saveReminder(reminder)
        remindersLocalRepository.deleteAllReminders()

        //WHEN
        val result = remindersLocalRepository.getReminders()

        //THEN
        MatcherAssert.assertThat(result is Result.Success, `is`(true))
        result as Result.Success
        MatcherAssert.assertThat(result.data, `is`(emptyList()))
    }


    // this test trying to add reminder and delete it and check what will happen if we call that reminder
    @Test
    fun retrieveReminderById_ReturnError() = runBlocking {
        //GIVEN
        val reminder = ReminderDTO("My Shop", "Get to the Shop", "Abuja", 6.54545, 7.54545)
        remindersLocalRepository.saveReminder(reminder)
        remindersLocalRepository.deleteAllReminders()

        //WHEN
        val result = remindersLocalRepository.getReminder(reminder.id)

        //THEN
        MatcherAssert.assertThat(result is Result.Error, `is`(true))
        result as Result.Error
        MatcherAssert.assertThat(result.message, `is`("Reminder not found!"))
    }
}