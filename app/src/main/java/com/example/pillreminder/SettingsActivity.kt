package com.example.pillreminder

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.ButtonDefaults
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Calendar
import androidx.compose.material3.Switch
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.testTag
import kotlinx.coroutines.tasks.await

class SettingsActivity : ComponentActivity() {
    lateinit var dataBase: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // database initialisation
        dataBase = Firebase.database.reference
        setContent {
            SettingsScreen()
            ReminderNotifications()
        }
    }
}

/**
 * Displays the whole main screen. The screen contains:
 *
 * @Composable fun [PillNumberText] - displays the text "Choose the number of pills in one blister pack"
 * @Composable fun [SettingPillType] - enables user to choose a number of pills in a blister
 * @Composable fun [FirstPillDateInputField] - display the text "Choose the first day of your blister" and enables user to choose a starting date for a blister
 * @Composable fun [SettingReminderTime] - displays the text "Set a notification reminder time" and triggers fun [ReminderTime]
 * @Composable fun [SettingShoppingReminder] - enables user to choose if they want to get notification about the upcoming end of a blister
 * Spacers - divide elements from each other
 *
 * If user is not logged in, they cannot access the features
 * A text "Log in or register to unlock all features!" is then displayed
 *
 * @param fakeAuth used to simulate user in testing
 * @param isInTestMode if true, disables Firebase interaction
 */
@Composable
fun SettingsScreen(fakeAuth: FirebaseAuth = FirebaseAuth.getInstance(), isInTestMode : Boolean = false) {
    val currentUser = rememberUpdatedState(fakeAuth.currentUser).value
    if (currentUser == null) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth()
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            item {
                Text(
                    text = "Log in or register to unlock all features!",
                    fontSize = 17.sp
                )
            }
        }
    } else {
        val context = LocalContext.current
        if (!isInTestMode) {
            LaunchedEffect(Unit) {
                ActivityCompat.requestPermissions(
                    context as Activity,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth()
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
            item {
                PillNumberText()
            }
            item {
                Spacer(modifier = Modifier.height(10.dp))
            }
            item {
                SettingPillType()
            }
            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
            item {
                FirstPillDateInputField(
                    onDateValid = { validDate ->
                        firstPillDateSaveToDb(validDate.toString())
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
            item {
                SettingReminderTime()
            }
            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
            item {
                //SettingShoppingReminder()
                SettingShoppingReminder(isInTestMode = isInTestMode)
            }
            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}


/**
 * Displays the headline - choosing the number of pills
 *
 * Displays a text "Choose the number of pills in one blister pack"
 */
@Composable
fun PillNumberText() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
        ) {
        Text(
            text = "Choose the number of pills in one blister pack",
            Modifier
                .testTag("numberOfPillsHeaderText")
                .width(250.dp),
            fontSize = 20.sp,
            textAlign = TextAlign.Center,
            color = Color.Black
        )
    }
}

/**
 * Handles obtaining the number of pills from user
 *
 * Displays a TextField which allows user to choose the number of pills
 * Displays a "Confirm" button that saves the number to database
 *
 * Handles getting input from user:
 * - if the starting date is formatted correctly - triggers fun [pillTypeSaveToDb] which saves the data to database
 * - else displays an error
 */
@Composable
fun SettingPillType() {
    Column (
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val database = FirebaseDatabase.getInstance()
        var pillType by remember { mutableStateOf("") }
        val context = LocalContext.current

        var showError by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf("") }

        LaunchedEffect(userId) {
            if (userId != null) {
                val pillTypeRef = database.getReference("pillNumber").child(userId)
                pillTypeRef.get().addOnSuccessListener { snapshot ->
                    val typeFromDbLong = snapshot.getValue(Long::class.java)
                    val typeFromDb = typeFromDbLong?.toString() ?: ""
                    if (true) {
                        pillType = typeFromDb
                    }
                }.addOnFailureListener {
                    Toast.makeText(context, "Failed to load pill type", Toast.LENGTH_SHORT).show()
                }
            }
        }

        OutlinedTextField(
            value = pillType,
            onValueChange = { newValue ->
                pillType = newValue
                if (userId != null) {
                    database.getReference("pillType").child(userId).setValue(newValue)
                }
            },
            label = { Text("Pill type (30, 60, etc.)") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("pillCountInput")
        )
        OutlinedButton(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .width(170.dp)
                .testTag("confirmPillTypeButton"),
            onClick = {
                val number = pillType.toIntOrNull()
                if (number != null && number > 0) {
                    pillTypeSaveToDb(number)
                    showError = false
                } else {
                    errorMessage = "Pill count must be greater than 0"
                    showError = true
                }
            },
            colors = ButtonColors(
                contentColor = Color.DarkGray,
                containerColor = Color.Transparent,
                disabledContainerColor = Color.Blue,
                disabledContentColor = Color.LightGray
            )
        ) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = "Check Icon",
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
            Text(
                text = "Confirm",
                fontSize = 17.sp
            )
        }

        if (showError) {
            Text(
                text = errorMessage,
                color = Color.Red,
                modifier = Modifier.testTag("pillCountError")
            )
        }
    }
}

/**
 * Handles obtaining the starting day of a blister from user
 *
 * Displays a prompt and input field for the user to enter the first date of their blister pack
 * Validates the date format (yyyy-MM-dd), and:
 * - if valid - triggers [firstPillDateSaveToDb] and calls [onDateValid] with the parsed date
 * - if invalid - shows an error message
 *
 * @param onDateValid Callback triggered when the user enters a valid date, receives the parsed [LocalDate]
 * @param modifier [Modifier] for styling
 */
@Composable
fun FirstPillDateInputField(
    onDateValid: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    var text by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    Column(modifier = modifier.padding(8.dp)) {
        Text(
            text = "Choose the first day of your blister",
            modifier = Modifier
                .testTag("firstDayTitle")
                .fillMaxWidth(),
            fontSize = 20.sp,
            textAlign = TextAlign.Center,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
            value = text,
            onValueChange = {
                text = it
                try {
                    val parsedDate = LocalDate.parse(it, dateFormatter)
                    isError = false
                    errorMessage = ""
                    onDateValid(parsedDate)
                } catch (_: DateTimeParseException) {
                    isError = true
                    errorMessage = "Invalid date format"
                }
            },
            label = { Text("Enter first pill date (yyyy-mm-dd)") },
            isError = isError,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("firstPillDateInput"),
            singleLine = true
        )
        if (isError) {
            Text(
                text = errorMessage,
                color = Color.Red,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .testTag("firstPillDateError")
            )
        }
        OutlinedButton(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .width(260.dp)
                .testTag("confirmFirstPillDateButton"),
            onClick = {
                firstPillDateSaveToDb(text)
            },
            colors = ButtonColors(
                contentColor = Color.DarkGray,
                containerColor = Color.Transparent,
                disabledContainerColor = Color.Blue,
                disabledContentColor = Color.LightGray
            )
        ) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = "Check Icon",
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
            Text(
                text = "Confirm starting date",
                fontSize = 17.sp
            )
        }
    }
}

/**
 * Handles obtaining the preferred notification time from user
 *
 * Displays a text "Set a notification reminder time"
 * Handles getting input from user
 */
@Composable
fun SettingReminderTime() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Set a notification reminder time",
            fontSize = 20.sp,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(10.dp))
        ReminderTime()
    }
}

/**
 * Handles obtaining the preferred notification time from user
 *
 * Based on: https://github.com/android/snippets/blob/187d70f980fbe0f0b300c7fb24c8e092d29328fa/compose/snippets/src/main/java/com/example/compose/snippets/components/TimePickers.kt#L207-L231
 *
 * Handles getting input from user:
 * - after clicking the button "Confirm" - triggers fun [pillTimeSaveToDb] and fun [fetchPillTimeAndScheduleAlarm]
 * - after clicking the button "Dismiss" - a suitable Toast is displayed
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderTime() {
    val currentTime = Calendar.getInstance()
    val context = LocalContext.current

    val timePickerState = rememberTimePickerState(
        initialHour = currentTime.get(Calendar.HOUR_OF_DAY),
        initialMinute = currentTime.get(Calendar.MINUTE),
        is24Hour = true,
    )

    val selectedHour = timePickerState.hour
    val selectedMinute = timePickerState.minute

    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

    Column (
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        TimeInput(
            state = timePickerState,
        )
        OutlinedButton(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .width(170.dp),
            onClick = {
                Toast.makeText(context, "Picker dismissed", Toast.LENGTH_SHORT).show()
            },
            colors = ButtonColors(
                contentColor = Color.DarkGray,
                containerColor = Color.Transparent,
                disabledContainerColor = Color.Blue,
                disabledContentColor = Color.LightGray
            )
        ){
            Text(
                text = "Dismiss picker",
                fontSize = 15.sp
            )
        }
        OutlinedButton(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .width(170.dp),
            onClick = {
                pillTimeSaveToDb(selectedHour, selectedMinute)
                fetchPillTimeAndScheduleAlarm(context, userId)
                Toast.makeText(context, "Selection confirmed", Toast.LENGTH_SHORT).show()
            },
            colors = ButtonColors(
                contentColor = Color.DarkGray,
                containerColor = Color.Transparent,
                disabledContainerColor = Color.Blue,
                disabledContentColor = Color.LightGray
            )
        ){
            Text(
                text = "Confirm time",
                fontSize = 15.sp
            )
        }
    }
}

/**
 * Handles obtaining the preference to be informed about the upcoming end of a blister
 *
 *
 * Displays the text "Set a reminder to buy another pill pack"
 * Handles getting input from user:
 * - when the switch is checked fun [saveReminderSwitchState] is triggered
 *
 * @param isInTestMode if true, disables Firebase interaction and uses the [initialChecked] value instead
 * @param initialChecked the initial checked state of the switch when in test mode
 * @param onCheckedChange optional callback invoked when the checked state changes
 */
@Composable
fun SettingShoppingReminder(
    isInTestMode: Boolean = false,
    initialChecked: Boolean = false,
    onCheckedChange: ((Boolean) -> Unit)? = null
) {
    var checked by remember { mutableStateOf(initialChecked) }
    val context = LocalContext.current

    if (!isInTestMode) {
        val database = FirebaseDatabase.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val switchRef = database.getReference("reminderSwitch").child(userId)

        LaunchedEffect(Unit) {
            switchRef.get().addOnSuccessListener { snapshot ->
                val savedState = snapshot.getValue(Boolean::class.java) == true
                checked = savedState
            }.addOnFailureListener {
                Log.d("Switch state", "Error getting switch state from database")
            }
        }
    }

    Column (
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row (
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ){
            Text(
                text = "Set a reminder to buy another pill pack",
                Modifier.width(250.dp),
                textAlign = TextAlign.Center,
                fontSize = 20.sp,
                color = Color.Black
            )
            Spacer(modifier = Modifier.width(10.dp))
            Switch(
                checked = checked,
                modifier = Modifier.testTag("reminderSwitch"),
                onCheckedChange = {
                    checked = it
                    if (!isInTestMode) {
                        saveReminderSwitchState(checked)
                        if (checked) {
                            Toast.makeText(context, "A notification will be sent at 14:00, 7 days before the end of your blister", Toast.LENGTH_LONG).show()
                        }
                    }
                    onCheckedChange?.invoke(it)
                }
            )
        }
    }
}

/**
 * Saves the user's reminder switch state to database
 *
 * Stores whether the reminder notification feature is enabled or disabled
 * Stores the data under the current user's ID in the "reminderSwitch" node
 *
 * @param enabled `true` if the reminder switch is turned on, `false` otherwise
 */
fun saveReminderSwitchState(enabled: Boolean) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val ref = FirebaseDatabase.getInstance().getReference("reminderSwitch").child(userId)
    ref.setValue(enabled)
}

/**
 * Saves the user's number of pills to the database
 *
 * Stores the chosen number of pills in a blister
 * Stores the data under the current user's ID in the "pillNumber" node
 *
 * @param pillNumber total number of pills in the user's blister pack
 */
fun pillTypeSaveToDb(pillNumber: Int) {
    val database = FirebaseDatabase.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

    val pillNumberRef = database.getReference("pillNumber").child(userId)
    pillNumberRef.setValue(pillNumber)
}

/**
 * Saves the user's preferred notification time to the database
 *
 * Stores the chosen hour and minute of pills in a blister
 * Stores the data under the current user's ID in the "pillTime" node -> in the "pillHour" and "pillMinute" nodes
 *
 * @param hour the hour of the day (0–23) at which the notification should trigger
 * @param minute the minute of the hour (0–59) at which the notification should trigger
 */
fun pillTimeSaveToDb(hour: Int, minute: Int) {
    val database = FirebaseDatabase.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

    val pillTimeRef = database.getReference("pillTime").child(userId)
    val goalsMap = mapOf(
        "pillHour" to hour,
        "pillMinute" to minute,
    )
    pillTimeRef.setValue(goalsMap)
        .addOnSuccessListener {
            Log.d("Firebase", "Time saved successfully")
        }
        .addOnFailureListener {
            Log.e("Firebase", "Failed to save time", it)
        }
}

/**
 * Saves the user's first pill date to the database
 *
 * Stores the chosen starting date of a blister
 * Stores the data under the current user's ID in the "firstPillDate" node
 *
 * @param date starting date of a blister
 */
fun firstPillDateSaveToDb(date: String) {
    val database = FirebaseDatabase.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

    val firstPillRef = database.getReference("firstPillDate").child(userId)

    firstPillRef.setValue(date)
        .addOnSuccessListener {
            Log.d("Firebase", "Date saved successfully")
        }
        .addOnFailureListener {
            Log.e("Firebase", "Failed to save date", it)
        }
}

/**
 * Schedules a daily inexact alarm at the specified time using Android AlarmManager
 *
 * This alarm triggers the [NotificationReceiver] every day at the given hour and minute
 * If the specified time has already passed for the current day, the alarm is scheduled for the next day
 *
 * @param context the context used to access system services
 * @param hour the hour of the day (0–23) at which the alarm should trigger
 * @param minute the minute of the hour (0–59) at which the alarm should trigger
 */
fun scheduleDailyAlarm(context: Context, hour: Int, minute: Int) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    val intent = Intent(context, NotificationReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        0,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    val now = Calendar.getInstance()
    if (calendar.before(now)) {
        calendar.add(Calendar.DAY_OF_MONTH, 1)
    }

    if (!alarmManager.canScheduleExactAlarms()) {
        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
        context.startActivity(intent)
    }

    // inexact repeating alarm
    alarmManager.setInexactRepeating(
        AlarmManager.RTC_WAKEUP,
        calendar.timeInMillis,
        AlarmManager.INTERVAL_DAY,
        pendingIntent
    )
}

/**
 * Retrieves the user's saved pill notification time from database and schedules a daily alarm
 *
 * Fetches the hour and minute values stored in the database under the user's ID,
 * and calls [scheduleDailyAlarm] to schedule a daily notification at that time
 * If either value is missing or invalid, no alarm is scheduled
 *
 * @param context the context used to access system services and schedule the alarm
 * @param userId the identifier of the current user whose pill notification time is being retrieved
 */
fun fetchPillTimeAndScheduleAlarm(context: Context, userId: String) {
    val database = FirebaseDatabase.getInstance()

    val timeRef = database.getReference("pillTime").child(userId)
    timeRef.get().addOnSuccessListener { snapshot ->
        val hour = snapshot.child("pillHour").getValue(Int::class.java) ?: return@addOnSuccessListener
        val minute = snapshot.child("pillMinute").getValue(Int::class.java) ?: return@addOnSuccessListener
        scheduleDailyAlarm(context, hour, minute)
    }
}

/**
 * Handles scheduling a notification before the end of a pill blister pack
 *
 * Checks if user enabled the reminder switch and retrieves data from database
 * Requires user to be authenticated
 * Prevents rescheduling by using 'hasScheduled' state
 * If the conditions are met, triggers [scheduleBlisterEndReminder]
 */
@Composable
/*fun ReminderNotifications() {
    val context = LocalContext.current
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

    var totalDays by remember { mutableStateOf<Int?>(null) }
    var firstPillDate by remember { mutableStateOf<LocalDate?>(null) }
    var hasScheduled by remember { mutableStateOf(false) }
    var isReminderChecked by remember { mutableStateOf<Boolean?>(null) }

    val today = LocalDate.now()

    LaunchedEffect(userId) {
        val database = FirebaseDatabase.getInstance()
        val pillRef = database.getReference("pillNumber").child(userId)
        val dateRef = database.getReference("firstPillDate").child(userId)
        val switchRef = database.getReference("reminderSwitch").child(userId)

        val pillSnapshot = pillRef.get().await()
        val dateSnapshot = dateRef.get().await()
        val switchSnapshot = switchRef.get().await()

        totalDays = pillSnapshot.getValue(Int::class.java)
        firstPillDate = dateSnapshot.getValue(String::class.java)?.let { LocalDate.parse(it) }
        isReminderChecked = switchSnapshot.getValue(Boolean::class.java) == true
        //isReminderChecked = switchSnapshot.getValue(Boolean::class.java) ?: false
    }

    // !! - unsafe nullable type (T?) conversion to a non-nullable type (T), !! will throw NullPointerException if the value is null
    LaunchedEffect(firstPillDate, totalDays, isReminderChecked, hasScheduled) {
        if (firstPillDate != null && totalDays != null && !hasScheduled && isReminderChecked == true) {
            val endDate = firstPillDate!!.plusDays((totalDays!! - 1).toLong())
            val reminderDate = endDate.minusDays(7)

            if (today == reminderDate) {
                scheduleBlisterEndReminder(context, firstPillDate!!, totalDays!!)
                hasScheduled = true
            }
        }
    }
}*/

fun ReminderNotifications() {
    val context = LocalContext.current
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

    var totalDays by remember { mutableStateOf<Int?>(null) }
    var firstPillDate by remember { mutableStateOf<LocalDate?>(null) }
    var isReminderChecked by remember { mutableStateOf<Boolean?>(null) }

    var hasScheduled by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(userId) {
        try {
            val database = FirebaseDatabase.getInstance()
            val pillSnapshot = database.getReference("pillNumber").child(userId).get().await()
            val dateSnapshot = database.getReference("firstPillDate").child(userId).get().await()
            val switchSnapshot = database.getReference("reminderSwitch").child(userId).get().await()

            totalDays = pillSnapshot.getValue(Int::class.java)
            firstPillDate = dateSnapshot.getValue(String::class.java)?.let { LocalDate.parse(it) }
            isReminderChecked = switchSnapshot.getValue(Boolean::class.java)
        } catch (e: Exception) {
            Log.e("ReminderNotifications", "Firebase data fetch failed", e)
        }
    }

    LaunchedEffect(firstPillDate, totalDays, isReminderChecked) {
        if (
            firstPillDate != null &&
            totalDays != null &&
            isReminderChecked == true &&
            !hasScheduled
        ) {
            scheduleBlisterEndReminder(context, firstPillDate!!, totalDays!!)
            hasScheduled = true
        }
    }
}


/**
 * Schedules a notification 7 days before the end of a pill blister pack
 *
 * If the specified time has already passed for the current day, the alarm is scheduled for the next day
 *
 * @param context the context used to schedule the alarm
 * @param firstPillDate starting date of a blister retrieved from database
 * @param totalDays total number of pills in a blister retrieved from database
 */
fun scheduleBlisterEndReminder(context: Context, firstPillDate: LocalDate, totalDays: Int) {
    val reminderDate = firstPillDate.plusDays(totalDays - 1L).minusDays(6)

    val calendar = Calendar.getInstance().apply {
        set(Calendar.YEAR, reminderDate.year)
        set(Calendar.MONTH, reminderDate.monthValue - 1)
        set(Calendar.DAY_OF_MONTH, reminderDate.dayOfMonth)
        set(Calendar.HOUR_OF_DAY, 14)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    if (calendar.timeInMillis < System.currentTimeMillis()) {
        return
    }

    val intent = Intent(context, BlisterReminderReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        101,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    if (!alarmManager.canScheduleExactAlarms()) {
        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
        context.startActivity(intent)
    }

    try {
        alarmManager.setExact(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
    } catch (e: SecurityException) {
        e.printStackTrace()
    }
}

@Preview(showSystemUi = true)
@Composable
fun SettingsScreenPreview() {
    SettingsScreen()
}
