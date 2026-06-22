package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.viewmodel.SocializeViewModel
import java.text.SimpleDateFormat
import java.util.*

// Helper to format date
fun formatTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

// 1. AUTH LOGIN SCREEN
@Composable
fun LoginScreen(
    viewModel: SocializeViewModel,
    onNavigateToRegister: () -> Unit,
    onNavigateToFeed: () -> Unit,
    onNavigateToVerify: () -> Unit
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var showForgotDialog by remember { mutableStateOf(false) }
    var forgotEmail by remember { mutableStateOf("") }
    var forgotLoading by remember { mutableStateOf(false) }

    val currentUser by viewModel.currentUserState.collectAsStateWithLifecycle()
    val isVerified by viewModel.isEmailVerified.collectAsStateWithLifecycle()

    LaunchedEffect(currentUser) {
        val user = currentUser
        if (user != null) {
            if (user.role == "admin" || isVerified) {
                onNavigateToFeed()
            } else {
                onNavigateToVerify()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate900)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App Branding
            AvatarView(avatarId = "avatar_5", size = 80.dp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Socialize",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontFamily = FontFamily.SansSerif
            )
            Text(
                text = "Connect. Share. Impact.",
                fontSize = 14.sp,
                color = Slate400,
                fontStyle = FontStyle.Italic
            )
            Spacer(modifier = Modifier.height(36.dp))

            // Text Inputs
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = SlatePrimary) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_email_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = SlatePrimary,
                    unfocusedBorderColor = Slate700
                ),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = SlatePrimary) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_password_input"),
                visualTransformation = PasswordVisualTransformation(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = SlatePrimary,
                    unfocusedBorderColor = Slate700
                ),
                singleLine = true
            )

            // Forget password hyperlink
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = "Forgot password? Send reset link",
                    color = SlateAccent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clickable { showForgotDialog = true }
                        .padding(4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (loading) {
                CircularProgressIndicator(color = SlatePrimary)
            } else {
                Button(
                    onClick = {
                        loading = true
                        viewModel.login(
                            email = email,
                            password = password,
                            onSuccess = {
                                loading = false
                                Toast.makeText(context, "Log in successful!", Toast.LENGTH_SHORT).show()
                            },
                            onError = { err ->
                                loading = false
                                Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                            }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("login_submit_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = SlatePrimary)
                ) {
                    Text("Sign In", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Don't have an account? Sign up now",
                color = SlateSecondary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier
                    .clickable { onNavigateToRegister() }
                    .padding(8.dp)
            )
            
            Spacer(modifier = Modifier.weight(1f))
            AdMobBanner()
        }

        // Forgot Password Dialog
        if (showForgotDialog) {
            AlertDialog(
                onDismissRequest = { showForgotDialog = false },
                title = { Text("Forget Password Reset") },
                text = {
                    Column {
                        Text(
                            text = "Enter your registered email address below. We'll automatically build and shoot a password reset link to your account.",
                            fontSize = 13.sp,
                            color = Slate400
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = forgotEmail,
                            onValueChange = { forgotEmail = it },
                            label = { Text("Gmail Address") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "⚠️ Reset email might land directly in your Gmail Spam folder! Be sure to double check it.",
                            fontSize = 11.sp,
                            color = SlateAccent,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                confirmButton = {
                    if (forgotLoading) {
                        CircularProgressIndicator(color = SlatePrimary, modifier = Modifier.size(24.dp))
                    } else {
                        Button(
                            onClick = {
                                forgotLoading = true
                                viewModel.sendPasswordReset(
                                    email = forgotEmail,
                                    onSuccess = {
                                        forgotLoading = false
                                        showForgotDialog = false
                                        Toast.makeText(
                                            context,
                                            "Sent! Check your Gmail spam folder.",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    },
                                    onError = { err ->
                                        forgotLoading = false
                                        Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                    }
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SlateAccent)
                        ) {
                            Text("Send Link")
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showForgotDialog = false }) {
                        Text("Cancel", color = Color.White)
                    }
                },
                containerColor = Slate800,
                titleContentColor = Color.White,
                textContentColor = Color.White
            )
        }
    }
}

// 2. REGISTER ACCOUNT SCREEN
@Composable
fun RegisterScreen(
    viewModel: SocializeViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToVerify: () -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    val registeredJustNow by viewModel.isRegisterJustCompleted.collectAsStateWithLifecycle()

    LaunchedEffect(registeredJustNow) {
        if (registeredJustNow) {
            onNavigateToVerify()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate900)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { onNavigateBack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Text(
                        text = "Create Account",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = SlatePrimary) },
                    modifier = Modifier.fillMaxWidth().testTag("register_name_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = SlatePrimary,
                        unfocusedBorderColor = Slate700
                    ),
                    singleLine = true
                )
                Text(
                    text = "💡 Emojis, stylish fonts and symbols are allowed here!",
                    fontSize = 11.sp,
                    color = Slate400,
                    modifier = Modifier.fillMaxWidth().padding(start = 4.dp, top = 2.dp)
                )
            }

            item {
                OutlinedTextField(
                    value = mobile,
                    onValueChange = { mobile = it },
                    label = { Text("Mobile Number") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = SlatePrimary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth().testTag("register_mobile_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = SlatePrimary,
                        unfocusedBorderColor = Slate700
                    ),
                    singleLine = true
                )
            }

            item {
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Unique Username") },
                    leadingIcon = { Icon(Icons.Default.AlternateEmail, contentDescription = null, tint = SlatePrimary) },
                    modifier = Modifier.fillMaxWidth().testTag("register_username_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = SlatePrimary,
                        unfocusedBorderColor = Slate700
                    ),
                    singleLine = true
                )
                Text(
                    text = "⚠️ Plain alphanumeric + underscores only. No stylish characters allowed in username.",
                    fontSize = 10.sp,
                    color = SlateAccent,
                    modifier = Modifier.fillMaxWidth().padding(start = 4.dp, top = 2.dp)
                )
            }

            item {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = SlatePrimary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth().testTag("register_email_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = SlatePrimary,
                        unfocusedBorderColor = Slate700
                    ),
                    singleLine = true
                )
            }

            item {
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = SlatePrimary) },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth().testTag("register_password_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = SlatePrimary,
                        unfocusedBorderColor = Slate700
                    ),
                    singleLine = true
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                if (loading) {
                    CircularProgressIndicator(color = SlatePrimary)
                } else {
                    Button(
                        onClick = {
                            loading = true
                            viewModel.register(
                                name = name,
                                mobile = mobile,
                                email = email,
                                password = password,
                                username = username,
                                onSuccess = {
                                    loading = false
                                    Toast.makeText(context, "Registration Saved. Verification email sent!", Toast.LENGTH_LONG).show()
                                },
                                onError = { err ->
                                    loading = false
                                    Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("register_submit_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = SlatePrimary)
                    ) {
                        Text("Register", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

// 3. EMAIL VERIFICATION SCREEN
@Composable
fun VerifyEmailScreen(
    viewModel: SocializeViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToFeed: () -> Unit
) {
    val context = LocalContext.current
    val isVerified by viewModel.isEmailVerified.collectAsStateWithLifecycle()
    val userState by viewModel.currentUserState.collectAsStateWithLifecycle()
    var checking by remember { mutableStateOf(false) }

    LaunchedEffect(isVerified, userState) {
        if (userState != null && (userState?.role == "admin" || isVerified)) {
            viewModel.ackRegisterVerificationComplete()
            onNavigateToFeed()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate900)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.MarkEmailUnread,
                contentDescription = null,
                tint = SlateAccent,
                modifier = Modifier.size(96.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Verify Your Email 📧",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "We have auto sent a verification link to your email account: ${userState?.email ?: ""}.",
                fontSize = 15.sp,
                color = Slate100,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                color = SlateAccent.copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, SlateAccent),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🚨 IMPORTANT GMAIL USER WARNING:",
                        fontWeight = FontWeight.Bold,
                        color = SlateAccent,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Verification emails usually deliver directly inside your Gmail app's SPAM FOLDER! Please open Gmail, slide open the left menu, select 'Spam', and tap 'Report Not Spam' or click the link.",
                        fontSize = 12.sp,
                        color = Color.White,
                        lineHeight = 18.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(36.dp))

            if (checking) {
                CircularProgressIndicator(color = SlatePrimary)
            } else {
                Button(
                    onClick = {
                        checking = true
                        viewModel.checkEmailVerificationStatus()
                        checking = false
                        if (isVerified) {
                            Toast.makeText(context, "Email confirmed! Welcome to Socialize.", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Verification not completed yet. Please tap the link in Gmail.", Toast.LENGTH_LONG).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SlatePrimary)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("I Clicked The Link / Refresh", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = {
                        viewModel.resendVerificationEmail(
                            onSuccess = { Toast.makeText(context, "Verification email resent successfully!", Toast.LENGTH_SHORT).show() },
                            onError = { err -> Toast.makeText(context, err, Toast.LENGTH_LONG).show() }
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Text("Resend Verification Email")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            TextButton(onClick = {
                viewModel.logout()
                onNavigateBack()
            }) {
                Text("Logout and Go Back", color = Slate400, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// 4. MAIN SOCIAL FEED SCREEN (Text only posts, with likes/dislikes/comments list dialogs)
@Composable
fun FeedScreen(
    viewModel: SocializeViewModel,
    onNavigateToProfile: (String) -> Unit,
    onNavigateToChat: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToComments: (String) -> Unit
) {
    val posts by viewModel.feedPosts.collectAsStateWithLifecycle()
    val me by viewModel.currentUserState.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    var postText by remember { mutableStateOf("") }
    var publishing by remember { mutableStateOf(false) }

    val unreadNotifsCount = notifications.count { !it.read }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(Slate900)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AvatarView(
                            avatarId = me?.profilePic ?: "avatar_1",
                            size = 36.dp,
                            modifier = Modifier.clickable { onNavigateToProfile(me?.id ?: "") }
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Socialize",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "@${me?.username ?: "username"}",
                                fontSize = 11.sp,
                                color = SlateSecondary
                            )
                        }
                    }

                    Row {
                        // Admin Mode Override Button
                        if (me?.role == "admin" || me?.email == "imm.abhijit@gmail.com") {
                            IconButton(onClick = { onNavigateToAdmin() }) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = "Admin Area",
                                    tint = SlateAccent
                                )
                            }
                        }

                        // Notification Badge Icon
                        Box(contentAlignment = Alignment.TopEnd) {
                            IconButton(onClick = { onNavigateToNotifications() }) {
                                Icon(
                                    imageVector = if (unreadNotifsCount > 0) Icons.Default.NotificationsActive else Icons.Outlined.Notifications,
                                    contentDescription = "Notifications",
                                    tint = if (unreadNotifsCount > 0) SlateAccent else Color.White
                                )
                            }
                            if (unreadNotifsCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 4.dp, end = 4.dp)
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(SlateAccent),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = unreadNotifsCount.toString(),
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        IconButton(onClick = { onNavigateToSettings() }) {
                            Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
                        }
                    }
                }
                Divider(color = Slate700, thickness = 0.5.dp)
            }
        },
        containerColor = Slate900
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Post Creator Panel (Text-only posts)
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = Slate800,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Slate700)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.Top) {
                            AvatarView(avatarId = me?.profilePic ?: "avatar_1", size = 40.dp)
                            Spacer(modifier = Modifier.width(12.dp))
                            OutlinedTextField(
                                value = postText,
                                onValueChange = { postText = it },
                                placeholder = { Text("What is happening? Share posts...", color = Slate400) },
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 72.dp)
                                    .testTag("feed_post_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent
                                ),
                                singleLine = false
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "💡 Stylish fonts & symbols supported here!",
                                fontSize = 11.sp,
                                color = Slate400,
                                modifier = Modifier.weight(1f)
                            )
                            if (publishing) {
                                CircularProgressIndicator(color = SlatePrimary, modifier = Modifier.size(24.dp))
                            } else {
                                Button(
                                    onClick = {
                                        publishing = true
                                        viewModel.createPost(
                                            content = postText,
                                            onSuccess = {
                                                publishing = false
                                                postText = ""
                                            },
                                            onError = {
                                                publishing = false
                                            }
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SlatePrimary),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.testTag("feed_post_submit_button")
                                ) {
                                    Text("Post", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Fallback for Empty Feed
            if (posts.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Campaign,
                            contentDescription = null,
                            tint = Slate400,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No active posts yet on feed", color = Color.White, fontWeight = FontWeight.Bold)
                        Text(
                            "Be the first one to share a dynamic text status with friends!",
                            color = Slate400,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                }
            }

            // Feed Posts Items
            items(posts) { post ->
                PostItem(
                    post = post,
                    currentUserId = me?.id ?: "",
                    onAvatarClick = { onNavigateToProfile(post.authorId) },
                    onCommentClick = { onNavigateToComments(post.id) },
                    onLike = { viewModel.likePost(post.id) },
                    onDislike = { viewModel.dislikePost(post.id) },
                    onDeletePost = {
                        viewModel.deletePost(post.id, {}, {})
                    }
                )
            }

            // Bottom Spacing and AdMob Inline Banner
            item {
                Spacer(modifier = Modifier.height(8.dp))
                AdMobBanner()
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

// 5. POST VIEW ITEM CARD COMPONENT
@Composable
fun PostItem(
    post: Post,
    currentUserId: String,
    onAvatarClick: () -> Unit,
    onCommentClick: () -> Unit,
    onLike: () -> Unit,
    onDislike: () -> Unit,
    onDeletePost: () -> Unit
) {
    val context = LocalContext.current
    val hasLiked = post.likes.contains(currentUserId)
    val hasDisliked = post.dislikes.contains(currentUserId)

    Surface(
        color = Slate800,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(0.5.dp, Slate700),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Author Avatar and Name/Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onAvatarClick() }
                ) {
                    AvatarView(avatarId = post.authorPic, size = 42.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = post.authorName,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "@${post.authorUsername}",
                            color = SlateSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = formatTime(post.timestamp).split(" • ").firstOrNull() ?: "",
                        fontSize = 11.sp,
                        color = Slate400
                    )
                    if (post.authorId == currentUserId) {
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { onDeletePost() },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = SlateAccent,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Body content (Text support only)
            Text(
                text = post.content,
                color = Color.White,
                fontSize = 14.sp,
                lineHeight = 21.sp
            )

            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = Slate700.copy(alpha = 0.6f), thickness = 0.5.dp)

            // Interaction button row (Like, dislike, comment, system share)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Like Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { onLike() }
                        .padding(6.dp)
                ) {
                    Icon(
                        imageVector = if (hasLiked) Icons.Default.ThumbUp else Icons.Outlined.ThumbUp,
                        contentDescription = "Like",
                        tint = if (hasLiked) SlateSecondary else Slate400,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = post.likes.size.toString(),
                        color = if (hasLiked) SlateSecondary else Slate400,
                        fontSize = 12.sp
                    )
                }

                // Dislike Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { onDislike() }
                        .padding(6.dp)
                ) {
                    Icon(
                        imageVector = if (hasDisliked) Icons.Default.ThumbDown else Icons.Outlined.ThumbDown,
                        contentDescription = "Dislike",
                        tint = if (hasDisliked) SlateAccent else Slate400,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = post.dislikes.size.toString(),
                        color = if (hasDisliked) SlateAccent else Slate400,
                        fontSize = 12.sp
                    )
                }

                // Comment Section button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { onCommentClick() }
                        .padding(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Comment,
                        contentDescription = "Comments",
                        tint = Slate400,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = post.commentsCount.toString(),
                        color = Slate400,
                        fontSize = 12.sp
                    )
                }

                // Native Android Share System
                IconButton(
                    onClick = {
                        try {
                            val shareIntent = android.content.Intent().apply {
                                action = android.content.Intent.ACTION_SEND
                                type = "text/plain"
                                putExtra(
                                    android.content.Intent.EXTRA_TEXT,
                                    "Check out this Socialize post by @${post.authorUsername}: \"${post.content}\""
                                )
                            }
                            context.startActivity(
                                android.content.Intent.createChooser(shareIntent, "Share with others via")
                            )
                        } catch (e: Exception) {
                            Toast.makeText(context, "Share error", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = Slate400,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// 6. COMMENTS DETAILS DIALOG SCREEN
@Composable
fun CommentsScreen(
    postId: String,
    viewModel: SocializeViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var inputComment by remember { mutableStateOf("") }
    val commentsList by viewModel.comments.collectAsStateWithLifecycle()
    val me by viewModel.currentUserState.collectAsStateWithLifecycle()

    LaunchedEffect(postId) {
        viewModel.startObservingComments(postId)
    }

    DisposableEffect(postId) {
        onDispose {
            viewModel.stopObservingComments()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate900)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header TopBar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onNavigateBack() }) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Text(
                    text = "Comments (${commentsList.size})",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            Divider(color = Slate700, thickness = 0.5.dp)

            // Comments List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                if (commentsList.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ChatBubble,
                                contentDescription = null,
                                tint = Slate400,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("No comments yet", color = Color.White, fontWeight = FontWeight.Bold)
                            Text(
                                "Start conversations by writing a comment below. Tap '@' to tag someone!",
                                color = Slate400,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )
                        }
                    }
                } else {
                    items(commentsList) { comment ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Slate800)
                                .padding(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            AvatarView(avatarId = comment.authorPic, size = 36.dp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = comment.authorName,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = "@${comment.authorUsername}",
                                            color = SlateSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                    Text(
                                        text = formatTime(comment.timestamp).split(" • ").firstOrNull() ?: "",
                                        fontSize = 10.sp,
                                        color = Slate400
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = comment.text,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            }

            Divider(color = Slate700, thickness = 0.5.dp)

            // Comment Box Bar at bottom
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Slate800)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputComment,
                    onValueChange = { inputComment = it },
                    placeholder = { Text("Write reply... use @username to tag", color = Slate400, fontSize = 13.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("comment_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = SlatePrimary,
                        unfocusedBorderColor = Slate750()
                    ),
                    singleLine = false
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (inputComment.trim().isNotBlank()) {
                            viewModel.addComment(
                                postId = postId,
                                commentText = inputComment,
                                onSuccess = {
                                    inputComment = ""
                                },
                                onError = {
                                    Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(SlatePrimary)
                        .testTag("comment_submit_button")
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

private fun Slate750() = Color(0xFF2E3B4E)

// 7. SEARCH PEOPLE SCREEN (Find members, follow/unfollow and DM them)
@Composable
fun SearchScreen(
    viewModel: SocializeViewModel,
    onNavigateToProfile: (String) -> Unit,
    onNavigateToChat: (String) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val allUsers by viewModel.allUsers.collectAsStateWithLifecycle()
    val followingState by viewModel.followingState.collectAsStateWithLifecycle()
    val me by viewModel.currentUserState.collectAsStateWithLifecycle()

    val filteredList = remember(query, allUsers) {
        if (query.trim().isBlank()) {
            emptyList()
        } else {
            allUsers.filter {
                it.id != me?.id && (it.username.contains(query.trim(), ignoreCase = true) || it.name.contains(query.trim(), ignoreCase = true))
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate900)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Search Socialize 🔍",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Find users by username or name...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SlatePrimary) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = SlatePrimary,
                    unfocusedBorderColor = Slate700
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (filteredList.isEmpty() && query.trim().isNotBlank()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(imageVector = Icons.Outlined.PersonSearch, contentDescription = null, tint = Slate400, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("No results match \"$query\"", color = Color.White, fontWeight = FontWeight.Bold)
                    Text("Verify correct spelling of the targeted socialize member.", color = Slate400, fontSize = 11.sp)
                }
            } else if (filteredList.isEmpty() && query.trim().isBlank()) {
                // Show a list of popular recommendations default
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(32.dp))
                    Icon(imageVector = Icons.Default.Group, contentDescription = null, tint = SlatePrimary, modifier = Modifier.size(80.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Discover New People", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Enter unique username character combinations above to search friends.", color = Slate400, fontSize = 12.sp, textAlign = TextAlign.Center)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredList) { user ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Slate800)
                                .border(1.dp, Slate700, RoundedCornerShape(12.dp))
                                .clickable { onNavigateToProfile(user.id) }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AvatarView(avatarId = user.profilePic, size = 48.dp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = user.name,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "@${user.username}",
                                    color = SlateSecondary,
                                    fontSize = 12.sp
                                )
                                if (user.bio.isNotBlank()) {
                                    Text(
                                        text = user.bio,
                                        color = Slate400,
                                        fontSize = 11.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }

                            Row {
                                val isFollowing = followingState[user.id] ?: false
                                Button(
                                    onClick = { viewModel.toggleFollowUser(user.id) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isFollowing) Slate700 else SlatePrimary
                                    ),
                                    contentPadding = PaddingValues(horizontal = 12.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text(
                                        text = if (isFollowing) "Following" else "Follow",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                IconButton(
                                    onClick = { onNavigateToChat(user.id) },
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(SlateSecondary)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Message,
                                        contentDescription = "Message",
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
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

// 8. MESSAGES LIST SCREEN (Show threads list of direct messaging)
@Composable
fun MessagesListScreen(
    viewModel: SocializeViewModel,
    onNavigateToChat: (String) -> Unit
) {
    val myChats by viewModel.myChats.collectAsStateWithLifecycle()
    val me by viewModel.currentUserState.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate900)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Conversations 💬",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (myChats.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(imageVector = Icons.Outlined.Chat, contentDescription = null, tint = Slate400, modifier = Modifier.size(72.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No messages yet", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(
                        "Search direct active members on Search tab and click Message to spawn private secure threads!",
                        color = Slate400,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(myChats) { chat ->
                        // Get remote user details
                        val myUid = me?.id ?: ""
                        val peerId = chat.participantIds.firstOrNull { it != myUid } ?: ""
                        val peerName = chat.participantNames[peerId] ?: "Socialize User"
                        val peerUsername = chat.participantUsernames[peerId] ?: "user"
                        val peerPic = chat.participantPics[peerId] ?: "avatar_1"
                        val unreads = chat.unreadCounts[myUid] ?: 0

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Slate800)
                                .border(
                                    1.dp,
                                    if (unreads > 0) SlatePrimary else Slate700,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { onNavigateToChat(peerId) }
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(contentAlignment = Alignment.BottomEnd) {
                                AvatarView(avatarId = peerPic, size = 46.dp)
                                if (unreads > 0) {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(SlateAccent)
                                            .border(1.5.dp, Slate800, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = unreads.toString(),
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = peerName,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = formatTime(chat.lastMessageTime).split(" • ").firstOrNull() ?: "",
                                        color = Slate400,
                                        fontSize = 10.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "@$peerUsername",
                                    color = SlateSecondary,
                                    fontSize = 11.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = chat.lastMessage,
                                    color = if (unreads > 0) Color.White else Slate400,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    fontSize = 12.sp,
                                    fontWeight = if (unreads > 0) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// 9. CHAT ROOM SCREEN (1 on 1 direct chatting)
@Composable
fun ChatRoomScreen(
    peerUserId: String,
    viewModel: SocializeViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var inputMessage by remember { mutableStateOf("") }
    val messages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val peerUser by viewModel.profileUser.collectAsStateWithLifecycle()
    val me by viewModel.currentUserState.collectAsStateWithLifecycle()

    val chatId = remember(me, peerUserId) {
        val myUid = me?.id ?: ""
        if (myUid < peerUserId) "${myUid}_${peerUserId}" else "${peerUserId}_${myUid}"
    }

    LaunchedEffect(peerUserId, chatId) {
        viewModel.observeUserProfile(peerUserId)
        viewModel.startObservingChatMessages(chatId)
    }

    DisposableEffect(peerUserId) {
        onDispose {
            viewModel.stopObservingChatMessages()
            viewModel.clearUserProfileState()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate900)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Chat Room Header TopBar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onNavigateBack() }) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(4.dp))
                if (peerUser != null) {
                    AvatarView(avatarId = peerUser?.profilePic ?: "avatar_1", size = 38.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = peerUser?.name ?: "",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "@${peerUser?.username ?: ""}",
                            color = SlateSecondary,
                            fontSize = 11.sp
                        )
                    }
                } else {
                    CircularProgressIndicator(color = SlatePrimary, modifier = Modifier.size(20.dp))
                }
            }
            Divider(color = Slate700, thickness = 0.5.dp)

            // Chat Messages Container
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                reverseLayout = false
            ) {
                if (messages.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = SlateSecondary, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Direct message thread started", color = Color.White, fontWeight = FontWeight.Bold)
                            Text("All conversations are written directly onto secure Firebase channels.", color = Slate400, fontSize = 11.sp)
                        }
                    }
                } else {
                    items(messages) { message ->
                        val isMe = message.senderId == me?.id
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart
                        ) {
                            Column(
                                horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
                            ) {
                                Surface(
                                    color = if (isMe) SlatePrimary else Slate800,
                                    shape = RoundedCornerShape(
                                        topStart = 16.dp,
                                        topEnd = 16.dp,
                                        bottomStart = if (isMe) 16.dp else 4.dp,
                                        bottomEnd = if (isMe) 4.dp else 16.dp
                                    ),
                                    border = BorderStroke(0.5.dp, if (isMe) SlatePrimaryVariant else Slate700)
                                ) {
                                    Text(
                                        text = message.text,
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)
                                    )
                                }
                                Text(
                                    text = formatTime(message.timestamp).split(" • ").lastOrNull() ?: "",
                                    fontSize = 8.sp,
                                    color = Slate400,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Sponsors/Ads Bar
            AdMobBanner(modifier = Modifier.padding(horizontal = 16.dp))

            // Message Composer Footer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Slate800)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputMessage,
                    onValueChange = { inputMessage = it },
                    placeholder = { Text("Write message...", color = Slate400, fontSize = 13.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = SlatePrimary,
                        unfocusedBorderColor = Slate750()
                    ),
                    singleLine = false
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (inputMessage.trim().isNotBlank()) {
                            viewModel.sendMessage(
                                receiverId = peerUserId,
                                textMessage = inputMessage,
                                onSuccess = {
                                    inputMessage = ""
                                },
                                onError = {
                                    Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(SlatePrimary)
                        .testTag("chat_submit_button")
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

// 10. NOTIFICATION CENTER SCREEN
@Composable
fun NotificationScreen(
    viewModel: SocializeViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToProfile: (String) -> Unit,
    onNavigateToComments: (String) -> Unit,
    onNavigateToChat: (String) -> Unit
) {
    val list by viewModel.notifications.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.markAllNotificationsRead()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate900)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header TopBar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onNavigateBack() }) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Text(
                    text = "Activity Updates 🔔",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            Divider(color = Slate700, thickness = 0.5.dp)

            if (list.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(imageVector = Icons.Outlined.Campaign, contentDescription = null, tint = Slate400, modifier = Modifier.size(80.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No activity alerts yet", color = Color.White, fontWeight = FontWeight.Bold)
                    Text("We'll show notifications here when members follow, tag, reply, or DM you!", color = Slate400, fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 24.dp))
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    items(list) { alert ->
                        val actionColor = when(alert.type) {
                            "follow" -> SlatePrimary
                            "message" -> SlateSecondary
                            "tag", "comment" -> SlateAccent
                            else -> Color.White
                        }
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (alert.read) Slate800 else Slate800.copy(alpha = 0.6f))
                                .border(1.dp, if (alert.read) Slate700 else actionColor, RoundedCornerShape(12.dp))
                                .clickable {
                                    when (alert.type) {
                                        "follow" -> onNavigateToProfile(alert.senderId)
                                        "message" -> onNavigateToChat(alert.senderId)
                                        "tag", "comment" -> onNavigateToComments(alert.postId)
                                    }
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(contentAlignment = Alignment.BottomEnd) {
                                AvatarView(avatarId = alert.senderPic, size = 42.dp)
                                val alertIcon = when(alert.type) {
                                    "follow" -> Icons.Default.PersonAdd
                                    "message" -> Icons.AutoMirrored.Filled.Message
                                    "tag" -> Icons.Default.AlternateEmail
                                    "comment" -> Icons.AutoMirrored.Filled.Comment
                                    else -> Icons.Default.Notifications
                                }
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clip(CircleShape)
                                        .background(actionColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = alertIcon,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(9.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = alert.senderName,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = alert.message,
                                    color = Slate100,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                                Text(
                                    text = formatTime(alert.timestamp),
                                    fontSize = 9.sp,
                                    color = Slate400,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// 11. PROFILE SCREEN FOR ME AND THIRD-PARTIES
@Composable
fun ProfileScreen(
    userId: String,
    viewModel: SocializeViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToChat: (String) -> Unit,
    onNavigateToComments: (String) -> Unit,
    onLogout: () -> Unit
) {
    val me by viewModel.currentUserState.collectAsStateWithLifecycle()
    val isMyPersonalProfile = userId == me?.id

    val profileUser by viewModel.profileUser.collectAsStateWithLifecycle()
    val profilePosts by viewModel.profilePosts.collectAsStateWithLifecycle()
    val followingState by viewModel.followingState.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val activeUser = if (isMyPersonalProfile) me else profileUser
    var isVerifyingLogout by remember { mutableStateOf(false) }

    LaunchedEffect(userId, me) {
        if (!isMyPersonalProfile) {
            viewModel.observeUserProfile(userId)
        } else {
            viewModel.observeUserProfile(me?.id ?: "")
        }
    }

    DisposableEffect(userId) {
        onDispose {
            if (!isMyPersonalProfile) {
                viewModel.clearUserProfileState()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate900)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        if (activeUser == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = SlatePrimary)
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header details
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (!isMyPersonalProfile) {
                            IconButton(onClick = { onNavigateBack() }) {
                                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                            }
                        }
                        Text(
                            text = if (isMyPersonalProfile) "My Profile 👤" else "@${activeUser.username}'s bio",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }

                    if (isMyPersonalProfile) {
                        IconButton(onClick = { isVerifyingLogout = true }) {
                            Icon(imageVector = Icons.Default.Logout, contentDescription = "Logout", tint = SlateAccent)
                        }
                    }
                }
                Divider(color = Slate700, thickness = 0.5.dp)

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Profile Header card Details
                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Slate800),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Slate700),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AvatarView(avatarId = activeUser.profilePic, size = 64.dp)
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = activeUser.name,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "@${activeUser.username}",
                                            fontSize = 13.sp,
                                            color = SlateSecondary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                if (activeUser.bio.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Text(
                                        text = activeUser.bio,
                                        fontSize = 13.sp,
                                        color = Slate100,
                                        lineHeight = 18.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                                Divider(color = Slate700.copy(alpha = 0.5f))
                                Spacer(modifier = Modifier.height(12.dp))

                                // Followers, follow counts row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(text = activeUser.postsCount.toString(), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        Text(text = "Posts", fontSize = 11.sp, color = Slate400)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(text = activeUser.followersCount.toString(), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        Text(text = "Followers", fontSize = 11.sp, color = Slate400)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(text = activeUser.followingCount.toString(), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        Text(text = "Following", fontSize = 11.sp, color = Slate400)
                                    }
                                }

                                if (!isMyPersonalProfile) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Row(modifier = Modifier.fillMaxWidth()) {
                                        val isFollowing = followingState[activeUser.id] ?: false
                                        Button(
                                            onClick = { viewModel.toggleFollowUser(activeUser.id) },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isFollowing) Slate700 else SlatePrimary
                                            ),
                                            modifier = Modifier.weight(1f).height(40.dp)
                                        ) {
                                            Text(if (isFollowing) "Unfollow" else "Follow User", fontWeight = FontWeight.Bold)
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        IconButton(
                                            onClick = { onNavigateToChat(activeUser.id) },
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(SlateSecondary)
                                        ) {
                                            Icon(imageVector = Icons.AutoMirrored.Filled.Message, contentDescription = "DM", tint = Color.White)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // User posts list title
                    item {
                        Text(
                            text = "Posts by ${activeUser.name}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(top = 10.dp)
                        )
                    }

                    if (profilePosts.isEmpty()) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 36.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(imageVector = Icons.Outlined.Campaign, contentDescription = null, tint = Slate400, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("No text posts yet", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    items(profilePosts) { p ->
                        PostItem(
                            post = p,
                            currentUserId = me?.id ?: "",
                            onAvatarClick = {},
                            onCommentClick = { onNavigateToComments(p.id) },
                            onLike = { viewModel.likePost(p.id) },
                            onDislike = { viewModel.dislikePost(p.id) },
                            onDeletePost = {
                                viewModel.deletePost(p.id, {
                                    Toast.makeText(context, "Post deleted successfully", Toast.LENGTH_SHORT).show()
                                }, {})
                            }
                        )
                    }
                }
            }
        }

        // Logout verifying dialog
        if (isVerifyingLogout) {
            AlertDialog(
                onDismissRequest = { isVerifyingLogout = false },
                title = { Text("Log Out Account?") },
                text = { Text("Are you absolutely sure you want to log out from your Active Socialize Account?") },
                confirmButton = {
                    Button(
                        onClick = {
                            isVerifyingLogout = false
                            viewModel.logout()
                            onLogout()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SlateAccent)
                    ) {
                        Text("Log Out")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { isVerifyingLogout = false }) {
                        Text("Cancel", color = Color.White)
                    }
                },
                containerColor = Slate800,
                titleContentColor = Color.White,
                textContentColor = Color.White
            )
        }
    }
}

// 12. SETTINGS SCREEN (Profile Customization)
@Composable
fun SettingsScreen(
    viewModel: SocializeViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val me by viewModel.currentUserState.collectAsStateWithLifecycle()
    
    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var selectPic by remember { mutableStateOf("") }
    var updating by remember { mutableStateOf(false) }

    LaunchedEffect(me) {
        val user = me
        if (user != null) {
            name = user.name
            username = user.username
            bio = user.bio
            selectPic = user.profilePic
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate900)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Settings header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onNavigateBack() }) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Text(
                    text = "Edit Profile ⚙️",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            Divider(color = Slate700, thickness = 0.5.dp)

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                // Preset photo picker selector
                item {
                    Text(text = "Choose an in-app Profile Icon:", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        for (preset in AvatarHelper.presets.take(4)) {
                            Box(
                                modifier = Modifier
                                    .padding(4.dp)
                                    .clip(CircleShape)
                                    .clickable { selectPic = preset.id }
                                    .border(
                                        width = if (selectPic == preset.id) 3.dp else 0.dp,
                                        color = if (selectPic == preset.id) SlateAccent else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .padding(2.dp)
                            ) {
                                AvatarView(avatarId = preset.id, size = 48.dp)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        for (preset in AvatarHelper.presets.drop(4)) {
                            Box(
                                modifier = Modifier
                                    .padding(4.dp)
                                    .clip(CircleShape)
                                    .clickable { selectPic = preset.id }
                                    .border(
                                        width = if (selectPic == preset.id) 3.dp else 0.dp,
                                        color = if (selectPic == preset.id) SlateAccent else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .padding(2.dp)
                            ) {
                                AvatarView(avatarId = preset.id, size = 48.dp)
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Display Name") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = SlatePrimary,
                            unfocusedBorderColor = Slate700
                        ),
                        singleLine = true
                    )
                    Text(
                        text = "💡 Emojis, stylish fonts and symbols are allowed here!",
                        fontSize = 11.sp,
                        color = Slate400,
                        modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                    )
                }

                item {
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Unique Username") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = SlatePrimary,
                            unfocusedBorderColor = Slate700
                        ),
                        singleLine = true
                    )
                    Text(
                        text = "⚠️ Plain letters, digits and underscores only. No stylish font in username.",
                        fontSize = 10.sp,
                        color = SlateAccent,
                        modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                    )
                }

                item {
                    OutlinedTextField(
                        value = bio,
                        onValueChange = { 
                            if (it.split(Regex("\\s+")).size <= 150) {
                                bio = it
                            }
                        },
                        label = { Text("Short Bio (Max 150 words)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 100.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = SlatePrimary,
                            unfocusedBorderColor = Slate700
                        ),
                        singleLine = false
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "💡 Emojis / symbols / stylish fonts are fully allowed!",
                            fontSize = 11.sp,
                            color = Slate400
                        )
                        val wordCount = bio.trim().split(Regex("\\s+")).filter { it.isNotBlank() }.size
                        Text(
                            text = "$wordCount/150 words",
                            fontSize = 11.sp,
                            color = if (wordCount > 150) SlateAccent else Slate400,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    if (updating) {
                        CircularProgressIndicator(color = SlatePrimary, modifier = Modifier.align(Alignment.CenterHorizontally))
                    } else {
                        Button(
                            onClick = {
                                updating = true
                                viewModel.updateProfile(
                                    name = name,
                                    username = username,
                                    bio = bio,
                                    avatarId = selectPic,
                                    onSuccess = {
                                        updating = false
                                        Toast.makeText(context, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                                        onNavigateBack()
                                    },
                                    onError = { err ->
                                        updating = false
                                        Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                    }
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SlatePrimary)
                        ) {
                            Text("Save Changes", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// 13. ADMIN PANEL SCREEN (imm.abhijit@gmail.com with password 790883)
@Composable
fun AdminPanelScreen(
    viewModel: SocializeViewModel,
    onNavigateBack: () -> Unit
) {
    val allActiveUsers by viewModel.allUsers.collectAsStateWithLifecycle()
    var searchAdminQuery by remember { mutableStateOf("") }

    val filteredList = remember(searchAdminQuery, allActiveUsers) {
        if (searchAdminQuery.isBlank()) {
            allActiveUsers
        } else {
            allActiveUsers.filter {
                it.name.contains(searchAdminQuery, ignoreCase = true) ||
                        it.email.contains(searchAdminQuery, ignoreCase = true) ||
                        it.mobile.contains(searchAdminQuery, ignoreCase = true) ||
                        it.username.contains(searchAdminQuery, ignoreCase = true)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate900)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Admin panel top header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onNavigateBack() }) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Column(modifier = Modifier.padding(start = 8.dp)) {
                    Text(
                        text = "Socialize Admin Area 🛡️",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Total Registered Accounts: ${allActiveUsers.size}",
                        fontSize = 11.sp,
                        color = SlateAccent
                    )
                }
            }
            Divider(color = Slate700, thickness = 0.5.dp)

            // Search User Admin console
            OutlinedTextField(
                value = searchAdminQuery,
                onValueChange = { searchAdminQuery = it },
                label = { Text("Filter accounts by name, email, mobile...") },
                leadingIcon = { Icon(Icons.Default.FilterList, contentDescription = null, tint = SlateAccent) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = SlateAccent,
                    unfocusedBorderColor = Slate700
                ),
                singleLine = true
            )

            // Scrollable list of registered accounts
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(filteredList) { member ->
                    Surface(
                        color = Slate800,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(0.5.dp, Slate700),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AvatarView(avatarId = member.profilePic, size = 44.dp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = member.name,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "@${member.username}",
                                    color = SlateSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = Slate400, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = member.email,
                                        color = Slate100,
                                        fontSize = 12.sp
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Phone, contentDescription = null, tint = Slate400, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = member.mobile,
                                        color = Slate100,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                            
                            // Role Badge indicator
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (member.role == "admin") SlateAccent else Slate750().copy(alpha = 0.5f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = member.role.uppercase(),
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
