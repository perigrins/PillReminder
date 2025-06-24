package com.example.pillreminder

import android.content.Context
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.ModalDrawer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import java.util.regex.Pattern

class LoginRegisterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        setContent {
            MaterialTheme{
                AuthSwitchContent()
            }
        }
    }
}

/**
 * Displays a screen responsible for login and register
 *
 * Displays login option by default
 * If a user is logged, displays [LogOutScreen]
 * If user is not logged:
 * - based on 'showLogin" value displays either login or register screen
 * - user can change the screens by clicking the button
 *
 * @param auth instance of [FirebaseAuth] used for accessing the current user
 */
@Composable
fun AuthSwitchContent(auth: FirebaseAuth = FirebaseAuth.getInstance()) {
    var showLogin by remember { mutableStateOf(true) }
    val loginViewModel: LoginViewModel = viewModel()
    val registerViewModel: RegisterViewModel = viewModel()
    
    var currentUser by remember { mutableStateOf(auth.currentUser) }
    
    LaunchedEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener {
            currentUser = it.currentUser
        }
        auth.addAuthStateListener(listener)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (currentUser != null) {
            LogOutScreen(currentUser!!.email ?: "User", onLogout = {
                auth.signOut()
            })
        } else {
            if (showLogin) {
                LoginScreen(viewModel = loginViewModel, onSuccess = {
                    currentUser = auth.currentUser
                })
            } else {
                RegisterScreen(viewModel = registerViewModel, onSuccess = {
                    currentUser = auth.currentUser
                })
            }

            TextButton(
                onClick = { showLogin = !showLogin }
            ) {
                Text(
                    text = if (showLogin) "Don't have an account? Register" else "Already have an account? Login",
                    fontSize = 15.sp,
                    modifier = Modifier.testTag("RegisterLoginButton"),
                    color = Color.Blue
                )
            }
        }
    }
}

/**
 * ViewModel responsible for holding and managing user login credentials
 *
 * Contains mutable state variables for the user's email and password
 *
 * The use of [mutableStateOf] ensures that any composable functions observing these variables
 * will recompose when the values change
 */
class LoginViewModel : ViewModel() {
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var errorMessage by mutableStateOf<String?>(null)

    fun clearError() {
        errorMessage = null
    }
    /**
     * Function handling user's login
     *
     * Tries to log in using provided credentials
     * If the login fails, an error toast is displayed
     *
     * @param auth instance of [FirebaseAuth] used to authenticate the user
     * @param email user's email address
     * @param password user's password
     * @param context [Context] used to show toast messages
     * @param onSuccess Callback function triggered after a successful login
     */
    fun loginUser(
        auth: FirebaseAuth,
        email: String,
        password: String,
        context: Context,
        onSuccess: () -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "Login successful", Toast.LENGTH_SHORT).show()
                    onSuccess()
                } else {
                    Toast.makeText(context, "Authentication failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }
}

/**
 * ViewModel responsible for holding and managing user register credentials
 *
 * Contains mutable state variables for the user's email and password
 *
 * The use of [mutableStateOf] ensures that any Composable functions observing these variables
 * will recompose when the values change
 */
class RegisterViewModel : ViewModel() {
    var email by mutableStateOf("")
    var username by mutableStateOf("")
    var password by mutableStateOf("")

    var errorMessage by mutableStateOf<String?>(null)

    fun clearError() {
        errorMessage = null
    }

    /**
     * Function handling user's registration
     *
     * Tries to register using provided credentials
     * If the registration fails, an error toast is displayed
     *
     * @param auth instance of [FirebaseAuth] used to authenticate the user
     * @param email user's email address
     * @param password user's password
     * @param context [Context] used to show toast messages
     * @param onSuccess Callback function triggered after a successful registration
     */
    fun registerUser(
        auth: FirebaseAuth,
        email: String,
        password: String,
        context: Context,
        onSuccess: () -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "Registration successful", Toast.LENGTH_SHORT).show()
                    onSuccess()
                } else {
                    Toast.makeText(context, "Registration failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }
}

/**
 * Displays login or reset password screen. The screen contains:
 *
 * Headline ("Login"/"Reset your password")
 * TextField responsible for handling user's email
 * TextField responsible for handling user's password
 * Login/Reset button
 *
 * Uses variable 'showResetPassword' to determine which view to show
 *
 * Checks if user's email format is correct by triggering [isValidEmail]
 * Gives an error if the email field is not formatted correctly
 * Triggers [loginUser] if all the files are filled
 * Uses provided [LoginViewModel] to manage form state and validate input before attempting to sign in
 *
 * @param viewModel [LoginViewModel] that holds the user's email and password input
 * @param onSuccess Callback function triggered after a successful login
 */
@Composable
fun LoginScreen(viewModel: LoginViewModel, onSuccess: () -> Unit) {
    var showResetPassword by remember { mutableStateOf(false) }
    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current

    if (!showResetPassword) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Login",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.testTag("LoginHeader")
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = viewModel.email,
                modifier = Modifier.testTag("LoginEmail"),
                onValueChange = { viewModel.email = it; viewModel.clearError() },
                label = { Text("E-mail") }
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = viewModel.password,
                modifier = Modifier.testTag("LoginPassword"),
                onValueChange = { viewModel.password = it; viewModel.clearError() },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation()
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally).width(170.dp)
                    .testTag("LoginButton"),
                onClick = {
                    if (isValidEmail(viewModel.email)){
                        if (viewModel.email.isNotEmpty() && viewModel.password.isNotEmpty()) {
                            viewModel.loginUser(auth, viewModel.email, viewModel.password, context) {
                                onSuccess()
                            }
                        } else {
                            viewModel.errorMessage = "All the fields must be filled"
                        }
                    } else {
                        viewModel.errorMessage = "Invalid email or password"
                    }
                }
            ) {
                Text(
                    text = "Login",
                    fontSize = 15.sp,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            TextButton(
                onClick = { showResetPassword = !showResetPassword }
            ) {
                Text(
                    text = "Forgot your password? Reset",
                    fontSize = 15.sp,
                    color = Color.Blue
                )
            }
        }
    } else {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Reset your password", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = viewModel.email,
                onValueChange = { viewModel.email = it },
                label = { Text("E-mail") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                modifier = Modifier.align(Alignment.CenterHorizontally).width(170.dp),
                onClick = {
                    if (isValidEmail(viewModel.email)){
                        if (viewModel.email.isNotEmpty()) {
                            resetPassword(auth, viewModel.email, context)
                        } else {
                            Toast.makeText(context, "Enter your email", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            ) {
                Text(
                    text = "Reset password",
                    fontSize = 15.sp,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            TextButton(
                onClick = {
                    showResetPassword = !showResetPassword
                }
            ) {
                Text(
                    text = "Already have an account? Login",
                    fontSize = 15.sp,
                    color = Color.Blue
                )
            }
        }
    }

    viewModel.errorMessage?.let { error ->
        Text(
            text = error,
            color = Color.Red,
            modifier = Modifier
                .padding(top = 8.dp)
                .testTag("LoginError")
        )
    }
}

/**
 * Displays register screen. The screen contains:
 *
 * Headline ("Register")
 * TextField responsible for handling user's email
 * TextField responsible for handling user's password
 * Register button
 *
 * Checks if user's email format is correct by triggering [isValidEmail]
 * Gives an error if the email field is not formatted correctly
 * Triggers [registerUser] if all the files are filled
 * Uses provided [RegisterViewModel] to manage form state and validate input before attempting to register
 *
 * @param viewModel [RegisterViewModel] that holds the user's email and password input
 * @param onSuccess Callback function triggered after a successful register
 */
@Composable
fun RegisterScreen(viewModel: RegisterViewModel, onSuccess: () -> Unit) {
    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Register",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.testTag("RegisterHeader")
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = viewModel.username,
            onValueChange = { viewModel.username = it; viewModel.clearError() },
            label = { Text("Username") }
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = viewModel.email,
            modifier = Modifier.testTag("RegisterEmail"),
            onValueChange = { viewModel.email = it; viewModel.clearError() },
            label = { Text("Email") }
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = viewModel.password,
            modifier = Modifier.testTag("RegisterPassword"),
            onValueChange = { viewModel.password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation()
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            modifier = Modifier
                .align(Alignment.CenterHorizontally).width(170.dp)
                .testTag("RegisterButton"),
            onClick = {
                if (isValidEmail(viewModel.email))
                {
                    if (viewModel.email.isNotEmpty() && viewModel.password.isNotEmpty()) {
                        viewModel.registerUser(auth, viewModel.email, viewModel.password, context) {
                            onSuccess()
                        }
                    } else {
                        viewModel.errorMessage = "All the fields must be filled"
                    }
                } else {
                    viewModel.errorMessage = "Invalid email or password"
                }
            }
        ) {
            Text(
                text = "Register",
                fontSize = 15.sp,
                color = Color.Black
            )
        }
    }
    viewModel.errorMessage?.let { error ->
        Text(
            text = error,
            color = Color.Red,
            modifier = Modifier
                .padding(top = 8.dp)
                .testTag("RegisterError")
        )
    }
}

/**
 * Displays logout screen. The screen contains:
 *
 * Text displaying current user's email
 * Log out button
 *
 * Checks if user's email format is correct by triggering [isValidEmail]
 * Gives an error if the email field is not formatted correctly
 * Triggers [registerUser] if all the files are filled
 * Uses provided [RegisterViewModel] to manage form state and validate input before attempting to register
 *
 * @param email user's email address displayed in the greeting
 * @param onLogout Callback function triggered after a successful logout
 */
@Composable
fun LogOutScreen(email: String, onLogout: () -> Unit) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Hello $email!", style = MaterialTheme.typography.headlineMedium, fontSize = 22.sp)
        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = {
                onLogout()
                Toast.makeText(context, "Logged out", Toast.LENGTH_SHORT).show()
            }
        ) {
            Text(
                text="Log out",
                fontSize = 15.sp,
                color = Color.Black
            )
        }
    }
}




/**
 * Function handling resetting the password
 *
 * Tries to reset using provided credentials
 * If the password reset fails, an error toast is displayed
 *
 * @param auth instance of [FirebaseAuth] used to authenticate the user
 * @param email user's email address
 * @param context [Context] used to show toast messages
 */
fun resetPassword (
    auth: FirebaseAuth,
    email: String,
    context: Context,
) {
    if (email.isBlank()) {
        Toast.makeText(context, "Please enter your email", Toast.LENGTH_SHORT).show()
        return
    }
    auth.sendPasswordResetEmail(email)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(context, "Email with reset link sent!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Restoring password failed", Toast.LENGTH_SHORT).show()
            }
        }
}


/**
 * Function validating an email
 *
 * Checks if the email address's format is correct
 *
 * @param email user's email address
 */
fun isValidEmail(email: String): Boolean {
    val pattern: Pattern = Patterns.EMAIL_ADDRESS
    return pattern.matcher(email).matches()
}

@Preview(showSystemUi = true)
@Composable
fun AuthContentPreview() {
    AuthSwitchContent()
}