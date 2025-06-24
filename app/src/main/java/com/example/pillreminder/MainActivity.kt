package com.example.pillreminder

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.ButtonDefaults
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Divider
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Icon
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)
        setContent {
            AppContent()
        }
    }
}

 /**
 * Displays starting screen of the app. The screen contains:
  *
  * Image - displays app logo
  * @Composable fun [LoginButton] - implements a button navigating to [LoginRegisterActivity]
  * @Composable fun [PillPackButton] - implements a button navigating to [BlisterActivity]
  * @Composable fun [SettingsButton] - implements a button navigating to [SettingsActivity]
  * Dividers - divide elements from each other
 */
@Composable
fun AppContent() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item {
            Divider(
                modifier = Modifier.height(100.dp),
                color = Color.Transparent
            )
        }
        item {
            Image(
                painter = painterResource(id = R.drawable.blister_img),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(300.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }
        item {
            Divider(
                modifier = Modifier.height(40.dp),
                color = Color.Transparent
            )
        }
        item {
            LoginButton()
        }
        item {
            PillPackButton()
        }
        item {
            SettingsButton()
        }
        item {
            Divider(
                modifier = Modifier.height(10.dp),
                color = Color.Transparent
            )
        }
    }
}

/**
 * Handles opening [LoginRegisterActivity]
 *
 * When the button is clicked, user gets navigated to [LoginRegisterActivity]
 */
@Composable
fun LoginButton() {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
    }
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        TextButton(
            onClick = {
                val intent = Intent(context, LoginRegisterActivity::class.java)
                launcher.launch(intent)
            },
            colors = ButtonDefaults.textButtonColors(
                contentColor = Color.Black,
                backgroundColor = Color.Transparent
            )
        ){
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Login,
                contentDescription = "Login Icon",
                modifier = Modifier.size(30.dp)
            )
            Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
            Text(
                text = "Login",
                fontSize = 26.sp
            )
        }
    }
}

/**
 * Handles opening [BlisterActivity]
 *
 * When the button is clicked, user gets navigated to [BlisterActivity]
 */
@Composable
fun PillPackButton() {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
    }
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        TextButton(
            onClick = {
                val intent = Intent(context, BlisterActivity::class.java)
                launcher.launch(intent)
            },
            colors = ButtonDefaults.textButtonColors(
                contentColor = Color.Black,
                backgroundColor = Color.Transparent
            )
        ){
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Assignment,
                contentDescription = "Assignment Icon",
                modifier = Modifier.size(30.dp)
            )
            Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
            Text(
                text = "Pill tracker",
                fontSize = 26.sp
            )
        }
    }
}

/**
 * Handles opening [SettingsActivity]
 *
 * When the button is clicked, user gets navigated to [SettingsActivity]
 */
@Composable
fun SettingsButton() {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
    }
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        TextButton(
            onClick = {
                val intent = Intent(context, SettingsActivity::class.java)
                launcher.launch(intent)
            },
            colors = ButtonDefaults.textButtonColors(
                contentColor = Color.Black,
                backgroundColor = Color.Transparent
            )
        ) {
            Icon(
                imageVector = Icons.Filled.Menu,
                contentDescription = "Settings Icon",
                modifier = Modifier.size(30.dp)
            )
            Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
            Text(
                text = "Settings",
                fontSize = 26.sp
            )
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun AppContentPreview() {
    AppContent()
}