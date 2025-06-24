package com.example.pillreminder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.CircularProgressIndicator
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Divider
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Green
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pillreminder.ui.theme.GreenishNew
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.database
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

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
    val userIdCheck = FirebaseAuth.getInstance().currentUser?.uid
    if (userIdCheck == null) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth()
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            item {
                androidx.compose.material3.Text(
                    text = "Log in or register to unlock all features!",
                    fontSize = 17.sp
                )
            }
        }
    } else {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        var totalDays by remember { mutableStateOf<Int?>(null) }
        var firstPillDate by remember { mutableStateOf<LocalDate?>(null) }

        LaunchedEffect(userId) {
            val database = FirebaseDatabase.getInstance()
            val pillNumberRef = database.getReference("pillNumber").child(userId)
            val firstPillDateRef = database.getReference("firstPillDate").child(userId)

            pillNumberRef.get().addOnSuccessListener { snapshot ->
                snapshot.getValue(Int::class.java)?.let { totalDays = it }
            }
            firstPillDateRef.get().addOnSuccessListener { snapshot ->
                snapshot.getValue(String::class.java)?.let {
                    firstPillDate = LocalDate.parse(it)
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().background(Color.White).padding(8.dp)
        ) {
            item {
                Divider(modifier = Modifier.height(20.dp), color = Color.Transparent)
            }
            item {
                CurrentBlisterText()
            }
            item {
                Divider(modifier = Modifier.height(20.dp), color = Color.Transparent)
            }
            item {
                Text(
                    text = "   Starting date: ${firstPillDate?.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))}",
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            item {
                Divider(modifier = Modifier.height(40.dp), color = Color.Transparent)
            }
            item {
                if (totalDays == null || firstPillDate == null) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    PillBlister(totalDays = totalDays!!, userId = userId, firstPillDate = firstPillDate!!)
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
 * Generates and displays current blister
 *
 * Retrieves data from database
 * Creates a box with buttons - every button represents a date
 * Clicking the button indicates that the pill was taken on that day
 *
 * @param userId the identifier of the current user whose information is being retrieved
 * @param firstPillDate starting date of a blister retrieved from database
 * @param totalDays total number of pills in a blister retrieved from database
 */
@Composable
fun PillBlister(
    totalDays: Int,
    userId: String,
    firstPillDate: LocalDate
) {
    val database = Firebase.database
    val pillStates = remember { mutableStateListOf<Boolean>().apply { repeat(totalDays) { add(false) } } }

    val lastDate = firstPillDate.plusDays((totalDays - 1).toLong())
    val monthYearText = "  ${firstPillDate.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${firstPillDate.year} - " +
            "${lastDate.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${lastDate.year}"

    LaunchedEffect(userId, firstPillDate) {
        val loadedData = MutableList(totalDays) { false }

        for (i in 0 until totalDays) {
            val date = firstPillDate.plusDays(i.toLong())
            val dateKey = date.toString()

            val dayRef = database.getReference("pillStates/$userId/$dateKey/0")
            val snapshot = dayRef.get().await()
            val taken = snapshot.getValue(Boolean::class.java) == true
            loadedData[i] = taken
        }

        pillStates.clear()
        pillStates.addAll(loadedData)
    }

    Text(
        text = monthYearText,
        fontSize = 24.sp,
        modifier = Modifier.padding(bottom = 8.dp)
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier
            .padding(8.dp)
            .height(600.dp)
            .fillMaxSize(),
    ) {
        items(totalDays) { index ->
            val isTaken = pillStates.getOrNull(index) == true
            val backgroundColor = if (isTaken) GreenishNew else Color.White
            val currentDate = firstPillDate.plusDays(index.toLong())
            val dateText = dateFormatting(currentDate)

            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(backgroundColor)
                    .border(
                        width = 1.dp,
                        color = if (isTaken) Color.White else Color.Black,
                        shape = CircleShape
                    )
                    .clickable {
                        val newState = !pillStates[index]
                        pillStates[index] = newState

                        val dateForIndex = firstPillDate.plusDays(index.toLong()).toString()
                        val pillRef = database.getReference("pillStates/$userId/$dateForIndex/0")
                        pillRef.setValue(newState)
                    }
                    .testTag("pill_$index"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    //text = "${index + 1}",
                    text  = dateText,
                    fontSize = 12.sp,
                    color = if (isTaken) Color.White else Color.Black
                )
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