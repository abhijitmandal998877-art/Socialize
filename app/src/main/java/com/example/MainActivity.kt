package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.data.FirebaseService
import com.example.ui.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.SlatePrimary
import com.example.viewmodel.SocializeViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: SocializeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Programmatic Firebase Initialization
        FirebaseService.initialize(applicationContext)

        setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                val currentBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = currentBackStackEntry?.destination?.route

                // Determine if we should display the M3 Bottom Navigation Bar on current route
                val showBottomBarUnwrapped = currentRoute in listOf(
                    "feed",
                    "search",
                    "messages",
                    "profile/{userId}"
                )
                // Filter specifically for showing bottom bar only when viewing MY PERSONAL profile, not peer profile
                val currentUserState by viewModel.currentUserState.collectAsState()
                val showBottomBar = showBottomBarUnwrapped && (
                    currentRoute != "profile/{userId}" || 
                    currentBackStackEntry?.arguments?.getString("userId") == currentUserState?.id
                )

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Slate900,
                    bottomBar = {
                        if (showBottomBar) {
                            NavigationBar(
                                containerColor = Slate800,
                                tonalElevation = 8.dp,
                                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                            ) {
                                // 1. Feed Tab Item
                                NavigationBarItem(
                                    selected = currentRoute == "feed",
                                    onClick = {
                                        navController.navigate("feed") {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = if (currentRoute == "feed") Icons.Default.Home else Icons.Outlined.Home,
                                            contentDescription = "Feed"
                                        )
                                    },
                                    label = { Text("Feed", fontSize = 11.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = SlatePrimary,
                                        selectedTextColor = SlatePrimary,
                                        indicatorColor = Slate700,
                                        unselectedIconColor = Color.White.copy(alpha = 0.5f),
                                        unselectedTextColor = Color.White.copy(alpha = 0.5f)
                                    )
                                )

                                // 2. Search Tab Item
                                NavigationBarItem(
                                    selected = currentRoute == "search",
                                    onClick = {
                                        navController.navigate("search") {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = if (currentRoute == "search") Icons.Default.Search else Icons.Outlined.Search,
                                            contentDescription = "Search"
                                        )
                                    },
                                    label = { Text("Search", fontSize = 11.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = SlatePrimary,
                                        selectedTextColor = SlatePrimary,
                                        indicatorColor = Slate700,
                                        unselectedIconColor = Color.White.copy(alpha = 0.5f),
                                        unselectedTextColor = Color.White.copy(alpha = 0.5f)
                                    )
                                )

                                // 3. Messages List Tab Item
                                NavigationBarItem(
                                    selected = currentRoute == "messages",
                                    onClick = {
                                        navController.navigate("messages") {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = if (currentRoute == "messages") Icons.Default.ChatBubble else Icons.AutoMirrored.Outlined.Chat,
                                            contentDescription = "Messages"
                                        )
                                    },
                                    label = { Text("Chat", fontSize = 11.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = SlatePrimary,
                                        selectedTextColor = SlatePrimary,
                                        indicatorColor = Slate700,
                                        unselectedIconColor = Color.White.copy(alpha = 0.5f),
                                        unselectedTextColor = Color.White.copy(alpha = 0.5f)
                                    )
                                )

                                // 4. Profile Tab Item
                                val activeUid = currentUserState?.id ?: ""
                                val isProfileActive = currentRoute == "profile/{userId}" && 
                                    currentBackStackEntry?.arguments?.getString("userId") == activeUid

                                NavigationBarItem(
                                    selected = isProfileActive,
                                    onClick = {
                                        if (activeUid.isNotEmpty()) {
                                            navController.navigate("profile/$activeUid") {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = if (isProfileActive) Icons.Default.Person else Icons.Outlined.Person,
                                            contentDescription = "Profile"
                                        )
                                    },
                                    label = { Text("Profile", fontSize = 11.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = SlatePrimary,
                                        selectedTextColor = SlatePrimary,
                                        indicatorColor = Slate700,
                                        unselectedIconColor = Color.White.copy(alpha = 0.5f),
                                        unselectedTextColor = Color.White.copy(alpha = 0.5f)
                                    )
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "login",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = if (showBottomBar) innerPadding.calculateBottomPadding() else 0.dp)
                    ) {
                        // 1. Auth: Sign In
                        composable("login") {
                            LoginScreen(
                                viewModel = viewModel,
                                onNavigateToRegister = { navController.navigate("register") },
                                onNavigateToFeed = {
                                    navController.navigate("feed") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                },
                                onNavigateToVerify = {
                                    navController.navigate("verify_email") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // 2. Auth: Sign Up
                        composable("register") {
                            RegisterScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() },
                                onNavigateToVerify = {
                                    navController.navigate("verify_email") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // 3. Auth: Verify Email
                        composable("verify_email") {
                            VerifyEmailScreen(
                                viewModel = viewModel,
                                onNavigateBack = {
                                    navController.navigate("login") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                },
                                onNavigateToFeed = {
                                    navController.navigate("feed") {
                                        popUpTo("verify_email") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // 4. Dash: Feed Timeline
                        composable("feed") {
                            FeedScreen(
                                viewModel = viewModel,
                                onNavigateToProfile = { userId -> navController.navigate("profile/$userId") },
                                onNavigateToChat = { peerId -> navController.navigate("chat_room/$peerId") },
                                onNavigateToSettings = { navController.navigate("settings") },
                                onNavigateToAdmin = { navController.navigate("admin_panel") },
                                onNavigateToNotifications = { navController.navigate("notifications") },
                                onNavigateToComments = { postId -> navController.navigate("comments/$postId") }
                            )
                        }

                        // 5. Dash: Search People
                        composable("search") {
                            SearchScreen(
                                viewModel = viewModel,
                                onNavigateToProfile = { userId -> navController.navigate("profile/$userId") },
                                onNavigateToChat = { peerId -> navController.navigate("chat_room/$peerId") }
                            )
                        }

                        // 6. Dash: Active Chats Threads
                        composable("messages") {
                            MessagesListScreen(
                                viewModel = viewModel,
                                onNavigateToChat = { peerId -> navController.navigate("chat_room/$peerId") }
                            )
                        }

                        // 7. Dash: User Profile
                        composable(
                            "profile/{userId}",
                            arguments = listOf(navArgument("userId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val userId = backStackEntry.arguments?.getString("userId") ?: ""
                            ProfileScreen(
                                userId = userId,
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() },
                                onNavigateToChat = { peerId -> navController.navigate("chat_room/$peerId") },
                                onNavigateToComments = { postId -> navController.navigate("comments/$postId") },
                                onLogout = {
                                    navController.navigate("login") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            )
                        }

                        // 8. View: Comments Section
                        composable(
                            "comments/{postId}",
                            arguments = listOf(navArgument("postId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val postId = backStackEntry.arguments?.getString("postId") ?: ""
                            CommentsScreen(
                                postId = postId,
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        // 9. View: 1-on-1 Chat Room
                        composable(
                            "chat_room/{peerId}",
                            arguments = listOf(navArgument("peerId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val peerId = backStackEntry.arguments?.getString("peerId") ?: ""
                            ChatRoomScreen(
                                peerUserId = peerId,
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        // 10. View: Settings/Profile Customizer
                        composable("settings") {
                            SettingsScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        // 11. View: Activity Updates
                        composable("notifications") {
                            NotificationScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() },
                                onNavigateToProfile = { userId -> navController.navigate("profile/$userId") },
                                onNavigateToComments = { postId -> navController.navigate("comments/$postId") },
                                onNavigateToChat = { peerId -> navController.navigate("chat_room/$peerId") }
                            )
                        }

                        // 12. View: Admin Area Console Screen
                        composable("admin_panel") {
                            AdminPanelScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
