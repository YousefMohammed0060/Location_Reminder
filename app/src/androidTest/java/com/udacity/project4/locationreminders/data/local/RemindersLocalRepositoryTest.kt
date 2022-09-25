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
        ).allowMainThreadQueries().build() //opening database

        remindersLocalRepository = RemindersLocalRepository(
            database.reminderDao(),
            Dispatchers.Main
        ) //initialize the repository
    }

    @After
    fun cleanUp() {
        database.close() //closing database
    }


    // this test trying to save the reminder and call it from room database
    @Test
    fun saveReminder_retrieveReminderById() = runBlocking {
        //GIVEN
        val reminder = ReminderDTO("My Shop", "Get to the Shop", "Abuja", 6.54545, 7.54545) //add the reminder data to create an object
        remindersLocalRepository.saveReminder(reminder) // using the repository and dispatchers to ask database to save the reminder

        //WHEN
        val result = remindersLocalRepository.getReminder(reminder.id) as? Result.Success // using the repository and dispatchers to ask database to get the reminder with the id given and save it in result variable

        //THEN
        MatcherAssert.assertThat(result is Result.Success, `is`(true)) //check that result is success
        result as Result.Success
        MatcherAssert.assertThat(result.data.id, `is`(reminder.id)) //check that id from result = the actual id
        MatcherAssert.assertThat(result.data.title, `is`(reminder.title)) //check that title from result = the actual title
        MatcherAssert.assertThat(result.data.description, `is`(reminder.description)) //check that description from result = the actual description
        MatcherAssert.assertThat(result.data.latitude, `is`(reminder.latitude)) //check that latitude from result = the actual latitude
        MatcherAssert.assertThat(result.data.longitude, `is`(reminder.longitude)) //check that longitude from result = the actual longitude
        MatcherAssert.assertThat(result.data.location, `is`(reminder.location)) //check that location from result = the actual location
    }
    //end of test


    // this test trying to delete all reminders and check that room database is empty
    @Test
    fun deleteReminders_EmptyList()= runBlocking {
        //GIVEN
        val reminder = ReminderDTO("My Shop", "Get to the Shop", "Abuja", 6.54545, 7.54545) //add the reminder data to create an object
        remindersLocalRepository.saveReminder(reminder) // using the repository and dispatchers to ask database to save the reminder
        remindersLocalRepository.deleteAllReminders() // using the repository and dispatchers to ask database to delete all reminders on database

        //WHEN
        val result = remindersLocalRepository.getReminders() // using the repository and dispatchers to ask database to get all reminders and save it in result variable

        //THEN
        MatcherAssert.assertThat(result is Result.Success, `is`(true)) //check that result is success
        result as Result.Success
        MatcherAssert.assertThat(result.data, `is`(emptyList())) // the return from calling the database will be a list this part check that list must be empty
    }
    //end of test


    // this test trying to add reminder and delete it and check what will happen if we call that reminder
    @Test
    fun retrieveReminderById_ReturnError() = runBlocking {
        //GIVEN
        val reminder = ReminderDTO("My Shop", "Get to the Shop", "Abuja", 6.54545, 7.54545) //add the reminder data to create an object
        remindersLocalRepository.saveReminder(reminder) // using the repository and dispatchers to ask database to save the reminder
        remindersLocalRepository.deleteAllReminders() // using the repository and dispatchers to ask database to delete all reminders on database

        //WHEN
        val result = remindersLocalRepository.getReminder(reminder.id)  // using the repository and dispatchers to ask database to get the reminder with the id given and save it in result variable

        //THEN
        MatcherAssert.assertThat(result is Result.Error, `is`(true)) //check that result is success
        result as Result.Error
        MatcherAssert.assertThat(result.message, `is`("Reminder not found!")) //check that the message must be that the "Reminder not found!" because we delete all reminders in database
    }
    //end of test
}