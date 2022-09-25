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


    @Test
    fun saveReminder_retrieveReminderById() = runBlocking {
        val reminder = ReminderDTO("My Shop", "Get to the Shop", "Abuja", 6.54545, 7.54545)
        remindersLocalRepository.saveReminder(reminder)

        val result = remindersLocalRepository.getReminder(reminder.id) as? Result.Success

        MatcherAssert.assertThat(result is Result.Success, `is`(true))
        result as Result.Success


        MatcherAssert.assertThat(result.data.title, `is`(reminder.title))
        MatcherAssert.assertThat(result.data.description, `is`(reminder.description))
        MatcherAssert.assertThat(result.data.latitude, `is`(reminder.latitude))
        MatcherAssert.assertThat(result.data.longitude, `is`(reminder.longitude))
        MatcherAssert.assertThat(result.data.location, `is`(reminder.location))
    }


    @Test
    fun deleteReminders_EmptyList()= runBlocking {
        val reminder = ReminderDTO("My Shop", "Get to the Shop", "Abuja", 6.54545, 7.54545)
        remindersLocalRepository.saveReminder(reminder)

        remindersLocalRepository.deleteAllReminders()

        val result = remindersLocalRepository.getReminders()

        MatcherAssert.assertThat(result is Result.Success, `is`(true))
        result as Result.Success

        MatcherAssert.assertThat(result.data, `is`(emptyList()))
    }

    @Test
    fun retrieveReminderById_ReturnError() = runBlocking {
        val reminder = ReminderDTO("My Shop", "Get to the Shop", "Abuja", 6.54545, 7.54545)
        remindersLocalRepository.saveReminder(reminder)

        remindersLocalRepository.deleteAllReminders()

        val result = remindersLocalRepository.getReminder(reminder.id)

        MatcherAssert.assertThat(result is Result.Error, `is`(true))
        result as Result.Error
        MatcherAssert.assertThat(result.message, `is`("Reminder not found!"))
    }
}