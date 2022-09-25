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
        ).allowMainThreadQueries().build()
    }


    @After
    fun closeDb() = database.close()

    // this test trying to get record from room database using id
    @Test
    fun saveReminder_GetById() = runBlockingTest {
        // GIVEN - add reminder
        val reminder = ReminderDTO("My Shop", "Get to the Shop", "Abuja", 6.54545, 7.54545)
        database.reminderDao().saveReminder(reminder)

        //WHEN - get the reminder
        val result = database.reminderDao().getReminderById(reminder.id)

        // THEN - check that data is correct
        assertThat(result as ReminderDTO, notNullValue())
        assertThat(result.id, `is`(reminder.id))
        assertThat(result.title, `is`(reminder.title))
        assertThat(result.description, `is`(reminder.description))
        assertThat(result.location, `is`(reminder.location))
        assertThat(result.latitude, `is`(reminder.latitude))
        assertThat(result.longitude, `is`(reminder.longitude))

    }


    // this test trying to get all records from room database
    @Test
    fun getAllRemindersFromDb() = runBlockingTest {
        //GIVEN - add reminders
        val reminder = ReminderDTO("My Shop", "Get to the Shop", "Abuja", 6.54545, 7.54545)
        val reminder2 = ReminderDTO("My Work place", "Get to the office", "Wuse", 6.57545, 7.53845)
        val reminder3 = ReminderDTO("My Gym", "Get to the Gym", "Karu", 6.87645, 7.98555)
        database.reminderDao().saveReminder(reminder)
        database.reminderDao().saveReminder(reminder2)
        database.reminderDao().saveReminder(reminder3)

        //WHEN - get data
        val remindersList = database.reminderDao().getReminders()

        //THEN - check that result no equal null
        assertThat(remindersList, `is`(notNullValue()))
    }


    // this test trying to delete all records from room database
    @Test
    fun insertReminders_deleteAllReminders() = runBlockingTest {
        //GIVEN - add reminders
        val reminder = ReminderDTO("My Shop", "Get to the Shop", "Abuja", 6.54545, 7.54545)
        val reminder2 = ReminderDTO("My Work place", "Get to the office", "Wuse", 6.57545, 7.53845)
        val reminder3 = ReminderDTO("My Gym", "Get to the Gym", "Karu", 6.87645, 7.98555)
        database.reminderDao().saveReminder(reminder)
        database.reminderDao().saveReminder(reminder2)
        database.reminderDao().saveReminder(reminder3)

        //WHEN - delete data
        database.reminderDao().deleteAllReminders()
        val remindersList = database.reminderDao().getReminders()

        //THEN - check that result is an empty list
        assertThat(remindersList, `is`(emptyList()))
    }

}