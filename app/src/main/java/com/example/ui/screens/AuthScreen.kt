package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.example.BuildConfig
import com.example.ui.theme.AppIcons
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(onAuthSuccess: () -> Unit) {
    val context = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    // Email Flow screen state
    var showEmailFlow by remember { mutableStateOf(false) }

    fun handleGoogleSignIn() {
        val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
        val generatedWebClientId = if (resId != 0) context.getString(resId) else ""
        val webClientId = if (generatedWebClientId.isNotEmpty()) generatedWebClientId else BuildConfig.GOOGLE_WEB_CLIENT_ID

        if (webClientId.isEmpty() || webClientId.contains("example")) {
            Toast.makeText(
                context,
                "Please configure a valid Web Client ID in your Secrets or .env file.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        isLoading = true
        val credentialManager = CredentialManager.create(context)
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(webClientId)
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        coroutineScope.launch {
            try {
                val result = credentialManager.getCredential(
                    context = context,
                    request = request
                )
                val credential = result.credential
                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val idToken = googleIdTokenCredential.idToken

                    val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                    auth.signInWithCredential(firebaseCredential)
                        .addOnCompleteListener { task ->
                            isLoading = false
                            if (task.isSuccessful) {
                                Toast.makeText(context, "Successfully signed in with Google!", Toast.LENGTH_SHORT).show()
                                onAuthSuccess()
                            } else {
                                val errorMsg = task.exception?.localizedMessage ?: "Google Auth sign-in failed."
                                Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                            }
                        }
                } else {
                    isLoading = false
                    Toast.makeText(context, "Unsupported credential type received.", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                isLoading = false
                val errorMsg = e.localizedMessage ?: "Google Sign-In was cancelled or failed."
                Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
            }
        }
    }

    if (showEmailFlow) {
        EmailAuthScreen(
            onBack = { showEmailFlow = false },
            onAuthSuccess = onAuthSuccess
        )
    } else {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.weight(0.5f))

                Image(
                    painter = painterResource(id = AppIcons.PerennialOutline),
                    contentDescription = "Perennial icon",
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "perennial",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.weight(1f))

                if (isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Connecting securely...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Apple Sign-In Placeholder
                        OutlinedButton(
                            onClick = {
                                Toast.makeText(context, "Apple Sign-In is coming soon!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = CircleShape,
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.onBackground
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                            contentPadding = PaddingValues(horizontal = 16.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxWidth()) {
                                Image(
                                    painter = painterResource(id = AppIcons.AppleLogo),
                                    contentDescription = "Apple",
                                    modifier = Modifier
                                        .size(20.dp)
                                        .align(Alignment.CenterStart)
                                )
                                Text(
                                    "Continue with Apple",
                                    style = MaterialTheme.typography.labelLarge,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        }

                        // Google Sign-In Button
                        OutlinedButton(
                            onClick = { handleGoogleSignIn() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = CircleShape,
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.onBackground
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                            contentPadding = PaddingValues(horizontal = 16.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxWidth()) {
                                Image(
                                    painter = painterResource(id = AppIcons.GoogleLogo),
                                    contentDescription = "Google",
                                    modifier = Modifier
                                        .size(20.dp)
                                        .align(Alignment.CenterStart)
                                )
                                Text(
                                    "Continue with Google",
                                    style = MaterialTheme.typography.labelLarge,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        }

                        // Email Sign-In Button
                        Button(
                            onClick = { showEmailFlow = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            contentPadding = PaddingValues(horizontal = 16.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxWidth()) {
                                Icon(
                                    painter = painterResource(id = AppIcons.Email),
                                    contentDescription = "Email",
                                    modifier = Modifier
                                        .size(20.dp)
                                        .align(Alignment.CenterStart)
                                )
                                Text(
                                    "Continue with email",
                                    style = MaterialTheme.typography.labelLarge,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Image(
                    painter = painterResource(id = AppIcons.Squiggle),
                    contentDescription = null,
                    modifier = Modifier
                        .width(24.dp)
                        .height(8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Your data stays private and\nsynced across your devices.",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun EmailAuthScreen(onBack: () -> Unit, onAuthSuccess: () -> Unit) {
    val context = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isSignUp by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    var displayNameInput by remember { mutableStateOf("") }

    // Validation/Error States
    var displayNameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }
    var generalError by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    fun validateInputs(): Boolean {
        var isValid = true
        val emailPattern = android.util.Patterns.EMAIL_ADDRESS

        if (isSignUp) {
            if (displayNameInput.isBlank()) {
                displayNameError = "What should we call you?"
                isValid = false
            } else {
                displayNameError = null
            }
        } else {
            displayNameError = null
        }

        if (email.isBlank() || !emailPattern.matcher(email).matches()) {
            emailError = "Please enter a valid email"
            isValid = false
        } else {
            emailError = null
        }

        if (password.length < 6) {
            passwordError = "Password must be at least 6 characters"
            isValid = false
        } else {
            passwordError = null
        }

        if (isSignUp) {
            if (password != confirmPassword) {
                confirmPasswordError = "Passwords don't match"
                isValid = false
            } else {
                confirmPasswordError = null
            }
        } else {
            confirmPasswordError = null
        }

        return isValid
    }

    fun handleForgotPassword() {
        val emailPattern = android.util.Patterns.EMAIL_ADDRESS
        if (email.isBlank() || !emailPattern.matcher(email).matches()) {
            emailError = "Please enter a valid email to reset your password"
            return
        }
        emailError = null
        isLoading = true
        generalError = null
        successMessage = null

        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                isLoading = false
                if (task.isSuccessful) {
                    successMessage = "Check your email for a reset link"
                } else {
                    val exception = task.exception
                    generalError = when (exception) {
                        is com.google.firebase.FirebaseNetworkException -> "Something went wrong — check your connection and try again"
                        else -> exception?.localizedMessage ?: "Failed to send reset link. Please check your email."
                    }
                }
            }
    }

    fun handleEmailAuth() {
        if (!validateInputs()) return

        isLoading = true
        generalError = null
        successMessage = null

        if (isSignUp) {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        if (user != null) {
                            val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                                .setDisplayName(displayNameInput)
                                .build()
                            user.updateProfile(profileUpdates)
                                .addOnCompleteListener { profileTask ->
                                    val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                    val sharedPrefs = context.getSharedPreferences("perennial_settings", android.content.Context.MODE_PRIVATE)
                                    val savedReminderStyle = sharedPrefs.getString("reminder_style", "Voice") ?: "Voice"
                                    val isDark = sharedPrefs.getBoolean("dark_mode", false)
                                    val themeStr = if (isDark) "Dark (Ink Teal)" else "Light (Sage)"
                                    
                                    db.collection("users").document(user.uid)
                                        .set(
                                            mapOf(
                                                "uid" to user.uid,
                                                "display_name" to displayNameInput,
                                                "displayName" to displayNameInput,
                                                "email" to (user.email ?: ""),
                                                "reminder_style" to savedReminderStyle,
                                                "reminderStyle" to savedReminderStyle,
                                                "theme" to themeStr,
                                                "subscription_tier" to "free",
                                                "subscriptionTier" to "free",
                                                "created_at" to System.currentTimeMillis(),
                                                "createdAt" to System.currentTimeMillis()
                                            ),
                                            com.google.firebase.firestore.SetOptions.merge()
                                        )
                                        .addOnCompleteListener { firestoreTask ->
                                            isLoading = false
                                            Toast.makeText(context, "Account created successfully!", Toast.LENGTH_SHORT).show()
                                            onAuthSuccess()
                                        }
                                }
                        } else {
                            isLoading = false
                            onAuthSuccess()
                        }
                    } else {
                        isLoading = false
                        val exception = task.exception
                        generalError = when (exception) {
                            is com.google.firebase.auth.FirebaseAuthUserCollisionException -> {
                                "An account with this email already exists. Try signing in instead."
                            }
                            is com.google.firebase.auth.FirebaseAuthWeakPasswordException -> {
                                "The password is too weak."
                            }
                            is com.google.firebase.FirebaseNetworkException -> {
                                "Something went wrong — check your connection and try again"
                            }
                            else -> {
                                val msg = exception?.localizedMessage ?: ""
                                if (msg.contains("network", ignoreCase = true) || msg.contains("connection", ignoreCase = true)) {
                                    "Something went wrong — check your connection and try again"
                                } else {
                                    exception?.localizedMessage ?: "Registration failed. Please try again."
                                }
                            }
                        }
                    }
                }
        } else {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    isLoading = false
                    if (task.isSuccessful) {
                        Toast.makeText(context, "Signed in successfully!", Toast.LENGTH_SHORT).show()
                        onAuthSuccess()
                    } else {
                        val exception = task.exception
                        generalError = when (exception) {
                            is com.google.firebase.auth.FirebaseAuthInvalidUserException -> {
                                "No account found with this email. Want to sign up instead?"
                            }
                            is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException -> {
                                "Incorrect password. Try again or reset your password."
                            }
                            is com.google.firebase.FirebaseNetworkException -> {
                                "Something went wrong — check your connection and try again"
                            }
                            else -> {
                                val msg = exception?.localizedMessage ?: ""
                                if (msg.contains("network", ignoreCase = true) || msg.contains("connection", ignoreCase = true)) {
                                    "Something went wrong — check your connection and try again"
                                } else {
                                    exception?.localizedMessage ?: "Login failed. Please try again."
                                }
                            }
                        }
                    }
                }
        }
    }

    fun toggleMode() {
        isSignUp = !isSignUp
        password = ""
        confirmPassword = ""
        displayNameInput = ""
        displayNameError = null
        emailError = null
        passwordError = null
        confirmPasswordError = null
        generalError = null
        successMessage = null
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onBack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Image(
                painter = painterResource(id = AppIcons.PerennialOutline),
                contentDescription = "Perennial icon",
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isSignUp) "Create account" else "Sign in",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (generalError != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Text(
                        text = generalError!!,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            if (successMessage != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Text(
                        text = successMessage!!,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            if (isSignUp) {
                OutlinedTextField(
                    value = displayNameInput,
                    onValueChange = {
                        displayNameInput = it
                        if (displayNameError != null) displayNameError = null
                    },
                    label = { Text("What should we call you?") },
                    isError = displayNameError != null,
                    supportingText = {
                        if (displayNameError != null) {
                            Text(displayNameError!!, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    enabled = !isLoading,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))
            }

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    if (emailError != null) emailError = null
                },
                label = { Text("Email") },
                isError = emailError != null,
                supportingText = {
                    if (emailError != null) {
                        Text(emailError!!, color = MaterialTheme.colorScheme.error)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                enabled = !isLoading,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    if (passwordError != null) passwordError = null
                },
                label = { Text("Password") },
                isError = passwordError != null,
                supportingText = {
                    if (passwordError != null) {
                        Text(passwordError!!, color = MaterialTheme.colorScheme.error)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }, enabled = !isLoading) {
                        Icon(image, contentDescription = if (passwordVisible) "Hide password" else "Show password")
                    }
                },
                singleLine = true,
                enabled = !isLoading,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
            )

            if (!isSignUp) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    TextButton(
                        onClick = { handleForgotPassword() },
                        enabled = !isLoading
                    ) {
                        Text(
                            "Forgot password?",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (isSignUp) {
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        if (confirmPasswordError != null) confirmPasswordError = null
                    },
                    label = { Text("Confirm password") },
                    isError = confirmPasswordError != null,
                    supportingText = {
                        if (confirmPasswordError != null) {
                            Text(confirmPasswordError!!, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        val image = if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }, enabled = !isLoading) {
                            Icon(image, contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password")
                        }
                    },
                    singleLine = true,
                    enabled = !isLoading,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
            } else {
                Spacer(modifier = Modifier.height(16.dp))
            }

            Button(
                onClick = { handleEmailAuth() },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = if (isSignUp) "Create Account" else "Sign In",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = { toggleMode() },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isSignUp) "Already have an account? Sign In" else "New to Perennial? Sign Up",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
