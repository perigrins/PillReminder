package com.example.pillreminder

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.CircularProgressIndicator
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Divider
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Green
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pillreminder.ui.theme.GreenishNew
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.ceil

class BlisterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)
        setContent {
            PillScreen()
        }
    }
}

/**
 * Displays pill blister screen of the app. The screen contains:
 *
 * Side drawer with Login / Register and Settings buttons
 * Upper left icon button to open the side drawer
 * @Composable fun [CurrentBlisterText] - displays a headline "Your current blister"
 * "Starting date" text that retrieved data from database
 * @Composable fun [PillBlister] - displays the blister (clickable buttons)
 * Dividers - divide elements from each other
 *
 * If user is not logged in, they cannot access the features
 * A text "Log in or register to unlock all features!" is then displayed
 */
@Composable
fun PillScreen(auth: FirebaseAuth = FirebaseAuth.getInstance()) {
    val context = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var currentUser by remember { mutableStateOf(auth.currentUser) }

    DisposableEffect(auth) {
        val listener = FirebaseAuth.AuthStateListener {
            currentUser = it.currentUser
        }
        auth.addAuthStateListener(listener)
        onDispose {
            auth.removeAuthStateListener(listener)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color.White,
                drawerTonalElevation = 0.dp
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                androidx.compose.material3.Text(
                    text = if (currentUser != null) "Logged in as:\n${currentUser?.email ?: "User"}" else "Menu",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                NavigationDrawerItem(
                    label = { androidx.compose.material3.Text("Login / Register", fontSize = 16.sp) },
                    icon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Login,
                            contentDescription = "Login / Register"
                        )
                    },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        context.startActivity(Intent(context, LoginRegisterActivity::class.java))
                    },
                    modifier = Modifier
                        .padding(NavigationDrawerItemDefaults.ItemPadding)
                        .testTag("drawerLoginButton")
                )

                NavigationDrawerItem(
                    label = { androidx.compose.material3.Text("Settings", fontSize = 16.sp) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        context.startActivity(Intent(context, SettingsActivity::class.java))
                    },
                    modifier = Modifier
                        .padding(NavigationDrawerItemDefaults.ItemPadding)
                        .testTag("drawerSettingsButton")
                )
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            // Upper left icon to open collapsible side panel
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, top = 8.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        scope.launch { drawerState.open() }
                    },
                    modifier = Modifier.testTag("sidePanelMenuButton")
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Open side panel",
                        modifier = Modifier.size(28.dp),
                        tint = Color.Black
                    )
                }
            }

            if (currentUser == null) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        androidx.compose.material3.Text(
                            text = "Log in or register to unlock all features!",
                            fontSize = 17.sp
                        )
                    }
                }
            } else {
                val userId = currentUser?.uid ?: return@Column
                var activeDays by remember { mutableStateOf<Int?>(null) }
                var placeboDays by remember { mutableStateOf(0) }
                var firstPillDate by remember { mutableStateOf<LocalDate?>(null) }
                var pillMotif by remember { mutableStateOf("GREEN") }

                LaunchedEffect(userId) {
                    val database = FirebaseDatabase.getInstance()
                    val pillNumberRef = database.getReference("pillNumber").child(userId)
                    val placeboNumberRef = database.getReference("placeboNumber").child(userId)
                    val firstPillDateRef = database.getReference("firstPillDate").child(userId)

                    pillNumberRef.get().addOnSuccessListener { snapshot ->
                        snapshot.getValue(Int::class.java)?.let { activeDays = it }
                    }
                    placeboNumberRef.get().addOnSuccessListener { snapshot ->
                        snapshot.getValue(Int::class.java)?.let { placeboDays = it }
                    }
                    firstPillDateRef.get().addOnSuccessListener { snapshot ->
                        snapshot.getValue(String::class.java)?.let {
                            firstPillDate = LocalDate.parse(it)
                        }
                    }
                }

                DisposableEffect(userId) {
                    val database = FirebaseDatabase.getInstance()
                    val pillMotifRef = database.getReference("pillMotif").child(userId)
                    val listener = object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            snapshot.getValue(String::class.java)?.let { pillMotif = it }
                        }
                        override fun onCancelled(error: DatabaseError) {}
                    }
                    pillMotifRef.addValueEventListener(listener)
                    onDispose {
                        pillMotifRef.removeEventListener(listener)
                    }
                }

                if (activeDays == null || firstPillDate == null) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    val totalDays = activeDays!! + placeboDays
                    val daysBetween = ChronoUnit.DAYS.between(firstPillDate!!, LocalDate.now())
                    val initialPage = if (daysBetween < 0) 0 else (daysBetween / totalDays).toInt()
                    val pageCount = 200

                    val pagerState = rememberPagerState(initialPage = initialPage.coerceIn(0, pageCount - 1)) {
                        pageCount
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White)
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Blister #${pagerState.currentPage + 1}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Text(
                            text = "Swipe horizontally to view other blisters",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) { page ->
                            val blisterStartDate = firstPillDate!!.plusDays((page * totalDays).toLong())
                            PillBlister(
                                activeDays = activeDays!!,
                                placeboDays = placeboDays,
                                userId = userId,
                                blisterStartDate = blisterStartDate,
                                pillMotif = pillMotif
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Displays current blister headline
 *
 * Displays a text "Your current blister"
 */
@Composable
fun CurrentBlisterText() {
    val gradientColors = listOf(GreenishNew, Green)
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        androidx.compose.material3.Text(
            text = "  Your current blister: ",
            fontSize = 24.sp,
            style = TextStyle(
                brush = Brush.linearGradient(
                    colors = gradientColors
                )
            )
        )
    }
}

/**
 * Generates and displays current blister (Overload for backward compatibility)
 */
@Composable
fun PillBlister(
    totalDays: Int,
    userId: String,
    blisterStartDate: LocalDate
) {
    PillBlister(
        activeDays = totalDays,
        placeboDays = 0,
        userId = userId,
        blisterStartDate = blisterStartDate
    )
}

/**
 * Generates and displays current blister with active and placebo pills support
 *
 * Retrieves data from database
 * Creates a box with buttons - every button represents a date
 * Clicking the button indicates that the pill was taken on that day
 *
 * @param activeDays total number of active pills in a blister
 * @param placeboDays total number of placebo pills in a blister
 * @param userId the identifier of the current user whose information is being retrieved
 * @param blisterStartDate starting date of this blister page
 */
@Composable
fun PillBlister(
    activeDays: Int,
    placeboDays: Int,
    userId: String,
    blisterStartDate: LocalDate,
    pillMotif: String = "GREEN"
) {
    // Total pills in the blister is active + placebo
    val totalDays = activeDays + placeboDays
    val database = Firebase.database
    val pillStates = remember(blisterStartDate) { mutableStateListOf<Boolean>().apply { repeat(totalDays) { add(false) } } }

    val motifs = listOf(
        PillMotif("GREEN", "Green", Color(0xff699166), Color(0xFF2C4A29)),
        PillMotif("BLUE", "Blue", Color(0xFF4B779D), Color(0xFF1B3D59)),
        PillMotif("PURPLE", "Purple", Color(0xFF624475), Color(0xFF381F47)),
        PillMotif("PINK", "Pink", Color(0xFF9F345B), Color(0xFF5E1731)),
        PillMotif("TEAL", "Teal", Color(0xFF00897B), Color(0xFF004D40)),
        PillMotif("ORANGE", "Orange", Color(0xFFF67D56), Color(0xFFB34320))
    )

    val currentMotif = motifs.find { it.key == pillMotif } ?: motifs[0]
    val activeColor = currentMotif.color
    val activeBorderColor = currentMotif.borderColor

    var showConfirmDialog by remember { mutableStateOf(false) }
    var pendingPillIndex by remember { mutableStateOf<Int?>(null) }
    var pendingNewState by remember { mutableStateOf(true) }

    val lastDate = blisterStartDate.plusDays((totalDays - 1).coerceAtLeast(0).toLong())
    val monthYearText = "${blisterStartDate.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${blisterStartDate.year} - " +
            "${lastDate.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${lastDate.year}"

    // Index where placebo pills start (the last X pills)
    val placeboStartIndex = totalDays - placeboDays

    LaunchedEffect(userId, blisterStartDate, totalDays) {
        val loadedData = MutableList(totalDays) { false }

        for (i in 0 until totalDays) {
            val date = blisterStartDate.plusDays(i.toLong())
            val dateKey = date.toString()

            val dayRef = database.getReference("pillStates/$userId/$dateKey/0")
            val snapshot = dayRef.get().await()
            val taken = snapshot.getValue(Boolean::class.java) == true
            loadedData[i] = taken
        }

        pillStates.clear()
        pillStates.addAll(loadedData)
    }

    val daysPerColumn = 7
    val numColumns = ceil(totalDays.toDouble() / daysPerColumn).toInt()

    if (showConfirmDialog && pendingPillIndex != null) {
        val isMarkingTaken = pendingNewState
        AlertDialog(
            onDismissRequest = {
                showConfirmDialog = false
                pendingPillIndex = null
            },
            title = { 
                Text(
                    text = if (isMarkingTaken) "Confirm Pill Taken" else "Confirm Remove Pill",
                    fontWeight = FontWeight.Bold
                ) 
            },
            text = { 
                Text(
                    text = if (isMarkingTaken) "Are you sure you want to mark this pill as taken?" 
                           else "Are you sure you want to unmark this pill as taken?"
                ) 
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val index = pendingPillIndex!!
                        val newState = pendingNewState
                        pillStates[index] = newState

                        val dateForIndex = blisterStartDate.plusDays(index.toLong()).toString()
                        val pillRef = database.getReference("pillStates/$userId/$dateForIndex/0")
                        pillRef.setValue(newState)

                        showConfirmDialog = false
                        pendingPillIndex = null
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showConfirmDialog = false
                        pendingPillIndex = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF2F4F8)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        border = BorderStroke(1.dp, Color(0xFFD0D7DE))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = monthYearText,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            if (placeboDays > 0) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(activeColor)
                            .border(1.dp, activeBorderColor, CircleShape)
                    )
                    Text(" Active pill", fontSize = 11.sp, color = Color.DarkGray)
                    Spacer(modifier = Modifier.width(16.dp))
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE8DEF8))
                            .border(1.dp, Color(0xFF6750A4), CircleShape)
                    )
                    Text(" Placebo pill", fontSize = 11.sp, color = Color.DarkGray)
                }
            } else {
                Spacer(modifier = Modifier.height(8.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Top
            ) {
                for (c in 0 until numColumns) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Week ${c + 1}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        for (r in 0 until daysPerColumn) {
                            val index = c * daysPerColumn + r
                            if (index < totalDays) {
                                // Placebo pills are always the last placeboDays pills
                                val isPlacebo = index >= placeboStartIndex
                                val isTaken = pillStates.getOrNull(index) == true

                                val backgroundColor: Color
                                val textColor: Color
                                val borderColor: Color

                                if (isPlacebo) {
                                    if (isTaken) {
                                        backgroundColor = Color(0xFF6750A4)
                                        textColor = Color.White
                                        borderColor = Color.White
                                    } else {
                                        backgroundColor = Color(0xFFE8DEF8)
                                        textColor = Color(0xFF381E72)
                                        borderColor = Color(0xFF6750A4)
                                    }
                                } else {
                                    if (isTaken) {
                                        backgroundColor = activeColor
                                        textColor = Color.White
                                        borderColor = activeBorderColor
                                    } else {
                                        backgroundColor = Color.White
                                        textColor = Color.Black
                                        borderColor = Color.Black
                                    }
                                }

                                val currentDate = blisterStartDate.plusDays(index.toLong())
                                val dateText = dateFormatting(currentDate)

                                Box(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .clip(CircleShape)
                                        .background(backgroundColor)
                                        .border(
                                            width = 1.5.dp,
                                            color = borderColor,
                                            shape = CircleShape
                                        )
                                        .clickable {
                                            val isCurrentlyTaken = pillStates[index]
                                            pendingPillIndex = index
                                            pendingNewState = !isCurrentlyTaken
                                            showConfirmDialog = true
                                        }
                                        .testTag("pill_$index"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = dateText,
                                        fontSize = 12.sp,
                                        color = textColor
                                    )
                                }

                                if (r < daysPerColumn - 1 && index + 1 < totalDays) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDownward,
                                        contentDescription = "Next day",
                                        modifier = Modifier
                                            .padding(vertical = 2.dp)
                                            .size(14.dp),
                                        tint = Color(0xFF8A9AAD)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Handles data formatting
 *
 * Changes the date so it can be displayed clearly on each blister pill place
 *
 * @param date date that is being formatted
 */
fun dateFormatting(date: LocalDate): String {
    val formatter = DateTimeFormatter.ofPattern("d MMM", Locale.ENGLISH)
    return date.format(formatter)
}

@Preview(showSystemUi = true)
@Composable
fun PillScreenPreview() {
    PillScreen()
}