package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.data.dto.ReminderDTO

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

//    TODO: Add testing implementation to the RemindersDao.kt

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase

    @Before
    fun initDb() {
        // Using an in-memory database so that the information stored here disappears when the
        // process is killed.
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build() //opening database
    }


    @After
    fun closeDb() = database.close() //closing database

    // this test trying to get record from room database using id
    @Test
    fun saveReminder_GetById() = runBlockingTest {
        // GIVEN - add reminder
        val reminder = ReminderDTO("My Shop", "Get to the Shop", "Abuja", 6.54545, 7.54545) //add the reminder data to create an object
        database.reminderDao().saveReminder(reminder) // call the room database to save the reminder

        //WHEN - get the reminder
        val result = database.reminderDao().getReminderById(reminder.id) // ask the database to get the reminder with the id given and save it in result variable

        // THEN - check that data is correct
        assertThat(result as ReminderDTO, notNullValue()) //check that reminder has a value
        assertThat(result.id, `is`(reminder.id)) //check that id from result = the actual id
        assertThat(result.title, `is`(reminder.title)) //check that title from result = the actual title
        assertThat(result.description, `is`(reminder.description)) //check that description from result = the actual description
        assertThat(result.location, `is`(reminder.location)) //check that location from result = the actual location
        assertThat(result.latitude, `is`(reminder.latitude)) //check that latitude from result = the actual latitude
        assertThat(result.longitude, `is`(reminder.longitude)) //check that longitude from result = the actual longitude
    }
    //end of test



    // this test trying to get all records from room database
    @Test
    fun getAllRemindersFromDb() = runBlockingTest {
        //GIVEN - add reminders
        val reminder = ReminderDTO("My Shop", "Get to the Shop", "Abuja", 6.54545, 7.54545) //add the reminder1 data to create an object1
        val reminder2 = ReminderDTO("My Work place", "Get to the office", "Wuse", 6.57545, 7.53845) //add the reminder2 data to create an object2
        val reminder3 = ReminderDTO("My Gym", "Get to the Gym", "Karu", 6.87645, 7.98555) //add the reminder3 data to create an object3
        database.reminderDao().saveReminder(reminder) // call the room database to save the reminder1
        database.reminderDao().saveReminder(reminder2) // call the room database to save the reminder2
        database.reminderDao().saveReminder(reminder3) // call the room database to save the reminder3

        //WHEN - get data
        val remindersList = database.reminderDao().getReminders() // ask the database to get all reminders that saved in the table and save it in variable

        //THEN - check that result no equal null
        assertThat(remindersList, `is`(notNullValue())) // the return from calling the database will be a list this part check that list has a value and not equal null
    }
    //end of test


    // this test trying to delete all records from room database
    @Test
    fun insertReminders_deleteAllReminders() = runBlockingTest {
        //GIVEN - add reminders
        val reminder = ReminderDTO("My Shop", "Get to the Shop", "Abuja", 6.54545, 7.54545) //add the reminder data to create an object1
        val reminder2 = ReminderDTO("My Work place", "Get to the office", "Wuse", 6.57545, 7.53845) //add the reminder2 data to create an object2
        val reminder3 = ReminderDTO("My Gym", "Get to the Gym", "Karu", 6.87645, 7.98555) //add the reminder3 data to create an object3
        database.reminderDao().saveReminder(reminder) // call the room database to save the reminder
        database.reminderDao().saveReminder(reminder2) // call the room database to save the reminder2
        database.reminderDao().saveReminder(reminder3) // call the room database to save the reminder3

        //WHEN - delete data
        database.reminderDao().deleteAllReminders() // ask the database to delete all records in the database
        val remindersList = database.reminderDao().getReminders() // ask the database to get all reminders that saved in the table and save it in variable

        //THEN - check that result is an empty list
        assertThat(remindersList, `is`(emptyList()))// the return from calling the database will be a list this part check that list must be empty
    }
    //end of test
}