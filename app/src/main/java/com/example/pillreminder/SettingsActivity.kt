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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.ButtonDefaults
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
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
 * Displays the main Settings Screen.
 */
@Composable
fun SettingsScreen(
    fakeAuth: FirebaseAuth? = if (LocalView.current.isInEditMode) null else FirebaseAuth.getInstance(),
    isInTestMode: Boolean = LocalView.current.isInEditMode
) {
    val currentUser = rememberUpdatedState(fakeAuth?.currentUser).value
    if (currentUser == null && !isInTestMode) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
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
        val focusManager = LocalFocusManager.current

        LaunchedEffect(Unit) {
            focusManager.clearFocus()
            if (!isInTestMode) {
                ActivityCompat.requestPermissions(
                    context as Activity,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            /*item {
                Spacer(modifier = Modifier.height(10.dp))
            }*/
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Settings",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
            item {
                SettingCard {
                    PillNumberText()
                    Spacer(modifier = Modifier.height(10.dp))
                    SettingPillType()
                }
            }
            item {
                SettingCard {
                    PlaceboPillNumberText()
                    Spacer(modifier = Modifier.height(10.dp))
                    SettingPlaceboPillType(isInTestMode = isInTestMode)
                }
            }
            item {
                SettingCard {
                    FirstPillDateInputField(
                        onDateValid = { validDate ->
                            firstPillDateSaveToDb(validDate.toString(), context)
                        },
                        isInTestMode = isInTestMode,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            item {
                SettingCard {
                    SettingShoppingReminder(isInTestMode = isInTestMode)
                }
            }
            item {
                SettingCard {
                    SettingReminderTime(isInTestMode = isInTestMode)
                }
            }
            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}





/**
 * Card container for a setting item.
 */
@Composable
fun SettingCard(
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = BorderStroke(1.dp, Color(0xFF8F988F))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            content()
        }
    }
}

@Composable
fun PillNumberText() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Choose the number of active pills in one blister pack",
            Modifier
                .testTag("numberOfPillsHeaderText")
                .width(250.dp),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Handles obtaining the number of active pills from user
 */
@Composable
fun SettingPillType(isInTestMode: Boolean = false) {
    Column (
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val isPreview = LocalView.current.isInEditMode
        val userId = if (isInTestMode || isPreview) null else FirebaseAuth.getInstance().currentUser?.uid
        var pillType by remember { mutableStateOf("") }
        val context = LocalContext.current

        var showError by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf("") }

        if (!isInTestMode && !isPreview && userId != null) {
            LaunchedEffect(userId) {
                val database = FirebaseDatabase.getInstance()
                val pillTypeRef = database.getReference("pillNumber").child(userId)
                pillTypeRef.get().addOnSuccessListener { snapshot ->
                    val typeFromDbLong = snapshot.getValue(Long::class.java)
                    val typeFromDb = typeFromDbLong?.toString() ?: ""
                    pillType = typeFromDb
                }.addOnFailureListener {
                    Toast.makeText(context, "Failed to load active pill count", Toast.LENGTH_SHORT).show()
                }
            }
        }

        OutlinedTextField(
            value = pillType,
            onValueChange = { newValue ->
                pillType = newValue
            },
            label = { Text("Active pills count (21, 28, etc.)") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("pillCountInput")
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .width(170.dp)
                .testTag("confirmPillTypeButton"),
            onClick = {
                val number = pillType.toIntOrNull()
                if (number != null && number > 0) {
                    pillTypeSaveToDb(number, context)
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
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
            Text(
                text = "Confirm",
                fontSize = 14.sp
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

@Composable
fun PlaceboPillNumberText() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Choose the number of placebo pills in one blister pack",
            Modifier
                .testTag("placeboHeaderText")
                .width(250.dp),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Handles obtaining the number of placebo pills from user
 */
@Composable
fun SettingPlaceboPillType(isInTestMode: Boolean = false) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val isPreview = LocalView.current.isInEditMode
        val userId = if (isInTestMode || isPreview) null else FirebaseAuth.getInstance().currentUser?.uid
        var placeboType by remember { mutableStateOf("") }
        val context = LocalContext.current

        var showError by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf("") }

        if (!isInTestMode && !isPreview && userId != null) {
            LaunchedEffect(userId) {
                val database = FirebaseDatabase.getInstance()
                val placeboTypeRef = database.getReference("placeboNumber").child(userId)
                placeboTypeRef.get().addOnSuccessListener { snapshot ->
                    val typeFromDbLong = snapshot.getValue(Long::class.java)
                    val typeFromDb = typeFromDbLong?.toString() ?: ""
                    placeboType = typeFromDb
                }.addOnFailureListener {
                    Toast.makeText(context, "Failed to load placebo pill count", Toast.LENGTH_SHORT).show()
                }
            }
        }

        OutlinedTextField(
            value = placeboType,
            onValueChange = { newValue ->
                placeboType = newValue
            },
            comlabel = { Text("Placebo pills count (0, 7, etc.)") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("placeboCountInput")
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .width(170.dp)
                .testTag("confirmPlaceboTypeButton"),
            onClick = {
                val number = placeboType.toIntOrNull()
                if (number != null && number >= 0) {
                    placeboTypeSaveToDb(number, context)
                    showError = false
                } else {
                    errorMessage = "Placebo count must be 0 or greater"
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
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
            Text(
                text = "Confirm",
                fontSize = 14.sp
            )
        }

        if (showError) {
            Text(
                text = errorMessage,
                color = Color.Red,
                modifier = Modifier.testTag("placeboCountError")
            )
        }
    }
}

/**
 * Saves the user's number of active pills to the database with completion listeners
 */
fun pillTypeSaveToDb(pillNumber: Int, context: Context? = null) {
    val database = FirebaseDatabase.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

    val pillNumberRef = database.getReference("pillNumber").child(userId)
    pillNumberRef.setValue(pillNumber)
        .addOnSuccessListener {
            Log.d("Firebase", "pillNumber saved successfully: $pillNumber")
            context?.let { Toast.makeText(it, "Active pill count saved!", Toast.LENGTH_SHORT).show() }
        }
        .addOnFailureListener { e ->
            Log.e("Firebase", "Failed to save pillNumber", e)
            context?.let { Toast.makeText(it, "Failed to save active pill count", Toast.LENGTH_SHORT).show() }
        }
}

/**
 * Saves the user's number of placebo pills to the database with completion listeners
 */
fun placeboTypeSaveToDb(placeboNumber: Int, context: Context? = null) {
    val database = FirebaseDatabase.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

    val placeboNumberRef = database.getReference("placeboNumber").child(userId)
    placeboNumberRef.setValue(placeboNumber)
        .addOnSuccessListener {
            Log.d("Firebase", "placeboNumber saved successfully: $placeboNumber")
            context?.let { Toast.makeText(it, "Placebo pill count saved!", Toast.LENGTH_SHORT).show() }
        }
        .addOnFailureListener { e ->
            Log.e("Firebase", "Failed to save placeboNumber", e)
            context?.let { Toast.makeText(it, "Failed to save placebo count", Toast.LENGTH_SHORT).show() }
        }
}

@Composable
fun FirstPillDateInputField(
    onDateValid: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    isInTestMode: Boolean = false
) {
    var text by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val context = LocalContext.current

    if (!isInTestMode) {
        LaunchedEffect(userId) {
            if (userId != null) {
                try {
                    val database = FirebaseDatabase.getInstance()
                    val dateRef = database.getReference("firstPillDate").child(userId)
                    dateRef.get().addOnSuccessListener { snapshot ->
                        val savedDate = snapshot.getValue(String::class.java) ?: ""
                        if (savedDate.isNotEmpty()) {
                            text = savedDate
                            try {
                                val parsedDate = LocalDate.parse(savedDate, dateFormatter)
                                isError = false
                                errorMessage = ""
                                onDateValid(parsedDate)
                            } catch (_: DateTimeParseException) {
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("FirstPillDateInput", "Error fetching firstPillDate", e)
                }
            }
        }
    }

    Column(modifier = modifier.padding(8.dp)) {
        Text(
            text = "Choose the first day of your blister",
            modifier = Modifier
                .testTag("firstDayTitle")
                .fillMaxWidth(),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
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
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .width(260.dp)
                .testTag("confirmFirstPillDateButton"),
            onClick = {
                firstPillDateSaveToDb(text, context)
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
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun SettingReminderTime(isInTestMode: Boolean = false) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Set a notification reminder time",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(10.dp))
        ReminderTime(isInTestMode = isInTestMode)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderTime(isInTestMode: Boolean = false) {
    val currentTime = Calendar.getInstance()
    val context = LocalContext.current
    val userId = FirebaseAuth.getInstance().currentUser?.uid

    var loadedTime by remember { mutableStateOf<Pair<Int, Int>?>(null) }

    if (!isInTestMode && userId != null) {
        LaunchedEffect(userId) {
            try {
                val database = FirebaseDatabase.getInstance()
                val timeRef = database.getReference("pillTime").child(userId)
                timeRef.get().addOnSuccessListener { snapshot ->
                    val hour = snapshot.child("pillHour").getValue(Int::class.java)
                    val minute = snapshot.child("pillMinute").getValue(Int::class.java)
                    if (hour != null && minute != null) {
                        loadedTime = Pair(hour, minute)
                    }
                }
            } catch (e: Exception) {
                Log.e("ReminderTime", "Error fetching pillTime", e)
            }
        }
    }

    val initialHour = loadedTime?.first ?: currentTime.get(Calendar.HOUR_OF_DAY)
    val initialMinute = loadedTime?.second ?: currentTime.get(Calendar.MINUTE)

    val timePickerState = key(loadedTime) {
        rememberTimePickerState(
            initialHour = initialHour,
            initialMinute = initialMinute,
            is24Hour = true,
        )
    }

    val selectedHour = timePickerState.hour
    val selectedMinute = timePickerState.minute

    Column (
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        TimePicker(
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
                fontSize = 13.sp
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedButton(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .width(170.dp),
            onClick = {
                if (userId != null) {
                    pillTimeSaveToDb(selectedHour, selectedMinute, context)
                    fetchPillTimeAndScheduleAlarm(context, userId)
                }
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
                fontSize = 13.sp
            )
        }
    }
}

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
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ){
            Text(
                text = "Set a reminder to buy another pill pack",
                Modifier.weight(1f),
                textAlign = TextAlign.Start,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(10.dp))
            Switch(
                checked = checked,
                modifier = Modifier.testTag("reminderSwitch"),
                onCheckedChange = {
                    checked = it
                    if (!isInTestMode) {
                        saveReminderSwitchState(checked, context)
                    }
                    onCheckedChange?.invoke(it)
                }
            )
        }
    }
}

fun saveReminderSwitchState(enabled: Boolean, context: Context? = null) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val ref = FirebaseDatabase.getInstance().getReference("reminderSwitch").child(userId)
    ref.setValue(enabled)
        .addOnSuccessListener {
            Log.d("Firebase", "reminderSwitch saved: $enabled")
            if (enabled && context != null) {
                Toast.makeText(context, "A notification will be sent at 14:00, 7 days before the end of your blister", Toast.LENGTH_LONG).show()
            }
        }
        .addOnFailureListener { e ->
            Log.e("Firebase", "Failed to save reminderSwitch", e)
        }
}

fun pillTimeSaveToDb(hour: Int, minute: Int, context: Context? = null) {
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
            context?.let { Toast.makeText(it, "Selection confirmed", Toast.LENGTH_SHORT).show() }
        }
        .addOnFailureListener { e ->
            Log.e("Firebase", "Failed to save time", e)
            context?.let { Toast.makeText(it, "Failed to save time", Toast.LENGTH_SHORT).show() }
        }
}

fun firstPillDateSaveToDb(date: String, context: Context? = null) {
    val database = FirebaseDatabase.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

    val firstPillRef = database.getReference("firstPillDate").child(userId)

    firstPillRef.setValue(date)
        .addOnSuccessListener {
            Log.d("Firebase", "Date saved successfully")
            //context?.let { Toast.makeText(it, "Starting date saved!", Toast.LENGTH_SHORT).show() }
        }
        .addOnFailureListener { e ->
            Log.e("Firebase", "Failed to save date", e)
            context?.let { Toast.makeText(it, "Failed to save starting date", Toast.LENGTH_SHORT).show() }
        }
}

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

    alarmManager.setInexactRepeating(
        AlarmManager.RTC_WAKEUP,
        calendar.timeInMillis,
        AlarmManager.INTERVAL_DAY,
        pendingIntent
    )
}

fun fetchPillTimeAndScheduleAlarm(context: Context, userId: String) {
    val database = FirebaseDatabase.getInstance()

    val timeRef = database.getReference("pillTime").child(userId)
    timeRef.get().addOnSuccessListener { snapshot ->
        val hour = snapshot.child("pillHour").getValue(Int::class.java) ?: return@addOnSuccessListener
        val minute = snapshot.child("pillMinute").getValue(Int::class.java) ?: return@addOnSuccessListener
        scheduleDailyAlarm(context, hour, minute)
    }
}

@Composable
fun ReminderNotifications() {
    val context = LocalContext.current
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

    var activeDays by remember { mutableStateOf<Int?>(null) }
    var placeboDays by remember { mutableIntStateOf(0) }
    var firstPillDate by remember { mutableStateOf<LocalDate?>(null) }
    var isReminderChecked by remember { mutableStateOf<Boolean?>(null) }

    var hasScheduled by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(userId) {
        try {
            val database = FirebaseDatabase.getInstance()
            val pillSnapshot = database.getReference("pillNumber").child(userId).get().await()
            val placeboSnapshot = database.getReference("placeboNumber").child(userId).get().await()
            val dateSnapshot = database.getReference("firstPillDate").child(userId).get().await()
            val switchSnapshot = database.getReference("reminderSwitch").child(userId).get().await()

            activeDays = pillSnapshot.getValue(Int::class.java)
            placeboDays = placeboSnapshot.getValue(Int::class.java) ?: 0
            firstPillDate = dateSnapshot.getValue(String::class.java)?.let { LocalDate.parse(it) }
            isReminderChecked = switchSnapshot.getValue(Boolean::class.java)
        } catch (e: Exception) {
            Log.e("ReminderNotifications", "Firebase data fetch failed", e)
        }
    }

    LaunchedEffect(firstPillDate, activeDays, placeboDays, isReminderChecked) {
        if (
            firstPillDate != null &&
            activeDays != null &&
            isReminderChecked == true &&
            !hasScheduled
        ) {
            val totalDays = activeDays!! + placeboDays
            scheduleBlisterEndReminder(context, firstPillDate!!, totalDays)
            hasScheduled = true
        }
    }
}

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
