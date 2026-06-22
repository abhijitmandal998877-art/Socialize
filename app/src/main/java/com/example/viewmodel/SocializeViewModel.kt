package com.example.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.FirebaseService
import com.example.data.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.Locale

class SocializeViewModel : ViewModel() {
    private val TAG = "SocializeViewModel"
    private val auth: FirebaseAuth get() = FirebaseService.auth
    private val db: FirebaseFirestore get() = FirebaseService.firestore

    // Livedata / Flow states
    private val _currentUserState = MutableStateFlow<User?>(null)
    val currentUserState: StateFlow<User?> = _currentUserState.asStateFlow()

    private val _isEmailVerified = MutableStateFlow(false)
    val isEmailVerified: StateFlow<Boolean> = _isEmailVerified.asStateFlow()

    private val _feedPosts = MutableStateFlow<List<Post>>(emptyList())
    val feedPosts: StateFlow<List<Post>> = _feedPosts.asStateFlow()

    private val _myChats = MutableStateFlow<List<Chat>>(emptyList())
    val myChats: StateFlow<List<Chat>> = _myChats.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<Message>>(emptyList())
    val chatMessages: StateFlow<List<Message>> = _chatMessages.asStateFlow()

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications.asStateFlow()

    private val _profileUser = MutableStateFlow<User?>(null)
    val profileUser: StateFlow<User?> = _profileUser.asStateFlow()

    private val _profilePosts = MutableStateFlow<List<Post>>(emptyList())
    val profilePosts: StateFlow<List<Post>> = _profilePosts.asStateFlow()

    private val _followingState = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val followingState: StateFlow<Map<String, Boolean>> = _followingState.asStateFlow()

    // Comments State for active Post
    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments.asStateFlow()

    // All registered users for Admin panel and Search
    private val _allUsers = MutableStateFlow<List<User>>(emptyList())
    val allUsers: StateFlow<List<User>> = _allUsers.asStateFlow()

    // Active screen navigation aids or general visual overlays
    private val _isRegisterJustCompleted = MutableStateFlow(false)
    val isRegisterJustCompleted: StateFlow<Boolean> = _isRegisterJustCompleted.asStateFlow()

    // Active listener registrations for cleanup
    private var postsListener: ListenerRegistration? = null
    private var chatsListener: ListenerRegistration? = null
    private var chatMessagesListener: ListenerRegistration? = null
    private var notificationsListener: ListenerRegistration? = null
    private var userDocListener: ListenerRegistration? = null
    private var commentsListener: ListenerRegistration? = null
    private var allUsersListener: ListenerRegistration? = null

    init {
        // Observe auth changes
        auth.addAuthStateListener { firebaseAuth ->
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser != null) {
                _isEmailVerified.value = firebaseUser.isEmailVerified
                val uid = firebaseUser.uid
                
                // If it's the admin credential
                if (firebaseUser.email == "imm.abhijit@gmail.com") {
                    _isEmailVerified.value = true // Admin bypass
                }

                // Pre-seed temporary user so the UI is NEVER stuck while waiting for Firestore query
                if (_currentUserState.value == null) {
                    val emailVal = firebaseUser.email ?: ""
                    val emailPrefix = emailVal.split("@").firstOrNull() ?: "user"
                    val cleanUsername = emailPrefix.replace(".", "").replace("_", "").lowercase() + "_" + uid.takeLast(4).lowercase()
                    _currentUserState.value = User(
                        id = uid,
                        name = firebaseUser.displayName ?: emailPrefix.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                        username = cleanUsername,
                        mobile = "",
                        email = emailVal,
                        bio = "Hello! I am on Socialize 👋",
                        profilePic = "avatar_1",
                        role = if (emailVal == "imm.abhijit@gmail.com") "admin" else "user"
                    )
                }

                observeCurrentUser(uid)
                startRealTimeListeners(uid)
            } else {
                _currentUserState.value = null
                _isEmailVerified.value = false
                stopRealTimeListeners()
            }
        }
    }

    // AUTH ACTIONS
    fun checkEmailVerificationStatus(onComplete: (Boolean) -> Unit = {}) {
        val user = auth.currentUser
        if (user != null) {
            user.reload().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _isEmailVerified.value = user.isEmailVerified
                    Log.d(TAG, "Email verification status updated: ${user.isEmailVerified}")
                    onComplete(user.isEmailVerified)
                } else {
                    onComplete(false)
                }
            }
        } else {
            onComplete(false)
        }
    }

    fun resendVerificationEmail(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val user = auth.currentUser
        if (user != null && !user.isEmailVerified) {
            user.sendEmailVerification()
                .addOnSuccessListener {
                    onSuccess()
                }
                .addOnFailureListener {
                    onError(it.message ?: "Failed to resend validation mail")
                }
        } else {
            onError("No user logged in or already verified")
        }
    }

    fun sendPasswordReset(email: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (email.isBlank()) {
            onError("Email cannot be empty")
            return
        }
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener {
                onError(it.message ?: "Failed to send reset link")
            }
    }

    fun login(email: String, password: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            onError("All fields are required")
            return
        }

        // Special Admin Login Check: user details: imm.abhijit@gmail.com with password 790883
        if (email.trim() == "imm.abhijit@gmail.com" && password.trim() == "790883") {
            // Check if we can sign in or create this user in Firebase Auth programmatically first so that it forms a real session
            auth.signInWithEmailAndPassword(email.trim(), password.trim())
                .addOnSuccessListener { authResult ->
                    // Make sure doc is marked as admin and verified
                    val uid = authResult.user?.uid ?: ""
                    _isEmailVerified.value = true
                    writeAdminUserDoc(uid, email.trim(), "790883")
                    observeCurrentUser(uid)
                    startRealTimeListeners(uid)
                    onSuccess()
                }
                .addOnFailureListener {
                    // Try to create the admin in Firebase Auth has failed (perhaps not registered yet, we create them directly!)
                    auth.createUserWithEmailAndPassword(email.trim(), password.trim())
                        .addOnSuccessListener { authResult ->
                            val uid = authResult.user?.uid ?: ""
                            _isEmailVerified.value = true
                            writeAdminUserDoc(uid, email.trim(), "790883")
                            observeCurrentUser(uid)
                            startRealTimeListeners(uid)
                            onSuccess()
                        }
                        .addOnFailureListener { e ->
                            // Bypassing directly with local configuration if everything fails as they provided exact credentials
                            onError("Firebase admin setup failed: ${e.message}")
                        }
                }
            return
        }

        auth.signInWithEmailAndPassword(email.trim(), password.trim())
            .addOnSuccessListener { authResult ->
                val fbUser = authResult.user
                if (fbUser != null) {
                    _isEmailVerified.value = fbUser.isEmailVerified
                    
                    // Pre-populate userState immediately to trigger redirection in LaunchEffects without waiting for Firestore
                    val emailVal = fbUser.email ?: ""
                    val emailPrefix = emailVal.split("@").firstOrNull() ?: "user"
                    val cleanUsername = emailPrefix.replace(".", "").replace("_", "").lowercase() + "_" + fbUser.uid.takeLast(4).lowercase()
                    _currentUserState.value = User(
                        id = fbUser.uid,
                        name = fbUser.displayName ?: emailPrefix.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                        username = cleanUsername,
                        mobile = "",
                        email = emailVal,
                        bio = "Hello! I am on Socialize 👋",
                        profilePic = "avatar_1",
                        role = if (emailVal == "imm.abhijit@gmail.com") "admin" else "user"
                    )
                    
                    observeCurrentUser(fbUser.uid)
                    startRealTimeListeners(fbUser.uid)
                }
                onSuccess()
            }
            .addOnFailureListener {
                onError(it.message ?: "Login failed. Please check credentials.")
            }
    }

    private fun writeAdminUserDoc(uid: String, email: String, mobile: String) {
        val adminUser = User(
            id = uid,
            name = "System Admin",
            username = "admin",
            mobile = "790883",
            email = email,
            bio = "Official Socialize Admin Panel Controller 🛡️",
            profilePic = "avatar_5",
            role = "admin"
        )
        _currentUserState.value = adminUser
        db.collection("users").document(uid).set(adminUser)
    }

    fun register(
        name: String,
        mobile: String,
        email: String,
        password: String,
        username: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (name.isBlank() || mobile.isBlank() || email.isBlank() || password.isBlank() || username.isBlank()) {
            onError("All fields must be filled")
            return
        }

        val cleanUsername = username.trim().lowercase()
        // Verify unique username first
        db.collection("users")
            .whereEqualTo("username", cleanUsername)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    onError("Username is already taken. Pick another!")
                    return@addOnSuccessListener
                }

                // Proceed with auth registration
                auth.createUserWithEmailAndPassword(email.trim(), password.trim())
                    .addOnSuccessListener { authResult ->
                        val uid = authResult.user?.uid ?: ""
                        val newUser = User(
                            id = uid,
                            name = name.trim(),
                            username = cleanUsername,
                            mobile = mobile.trim(),
                            email = email.trim(),
                            bio = "Hello! I am new on Socialize 👋",
                            profilePic = "avatar_1",
                            role = if (email.trim() == "imm.abhijit@gmail.com") "admin" else "user"
                        )
                        
                        _currentUserState.value = newUser
                        
                        // Save in database
                        db.collection("users").document(uid).set(newUser)
                            .addOnSuccessListener {
                                // Send mail verification
                                authResult.user?.sendEmailVerification()
                                    ?.addOnSuccessListener {
                                        _isRegisterJustCompleted.value = true
                                        onSuccess()
                                    }
                                    ?.addOnFailureListener {
                                        // Still successful registration but mail failure
                                        _isRegisterJustCompleted.value = true
                                        onSuccess()
                                    }
                            }
                            .addOnFailureListener {
                                onError("Registration saved, but database failed: ${it.message}")
                            }
                    }
                    .addOnFailureListener {
                        onError(it.message ?: "Failed to register")
                    }
            }
            .addOnFailureListener {
                onError("Failed to check username details: ${it.message}")
            }
    }

    fun logout() {
        auth.signOut()
        _currentUserState.value = null
        stopRealTimeListeners()
    }

    fun ackRegisterVerificationComplete() {
        _isRegisterJustCompleted.value = false
    }

    // REAL TIME LISTENERS
    private fun observeCurrentUser(uid: String) {
        userDocListener?.remove()
        userDocListener = db.collection("users").document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening user doc", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    if (snapshot.exists()) {
                        val user = snapshot.toObject(User::class.java)
                        _currentUserState.value = user
                        Log.d(TAG, "Current user document updated: ${user?.username}")
                    } else {
                        // Document doesn't exist. Let's provide a fallback locally, but DO NOT run .set() in Firestore to avoid overwriting during registration.
                        val firebaseUser = auth.currentUser
                        if (firebaseUser != null && firebaseUser.uid == uid) {
                            val emailVal = firebaseUser.email ?: ""
                            val emailPrefix = emailVal.split("@").firstOrNull() ?: "user"
                            val cleanUsername = emailPrefix.replace(".", "").replace("_", "").lowercase() + "_" + uid.takeLast(4).lowercase()
                            val nameVal = firebaseUser.displayName ?: emailPrefix.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                            val fallbackUser = User(
                                id = uid,
                                name = nameVal,
                                username = cleanUsername,
                                mobile = "",
                                email = emailVal,
                                bio = "Hello! I am on Socialize 👋",
                                profilePic = "avatar_1",
                                role = if (emailVal == "imm.abhijit@gmail.com") "admin" else "user"
                            )
                            if (_currentUserState.value == null) {
                                _currentUserState.value = fallbackUser
                            }
                        }
                    }
                }
            }
    }

    private fun startRealTimeListeners(uid: String) {
        stopRealTimeListeners()

        // 1. Posts listener
        postsListener = db.collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to posts", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.toObjects(Post::class.java)
                    _feedPosts.value = list
                }
            }

        // 2. Chats list listener
        chatsListener = db.collection("chats")
            .whereArrayContains("participantIds", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening chats list", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.toObjects(Chat::class.java)
                        .sortedByDescending { it.lastMessageTime }
                    _myChats.value = list
                }
            }

        // 3. Notifications listener
        notificationsListener = db.collection("notifications")
            .whereEqualTo("recipientId", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening notifications", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.toObjects(Notification::class.java)
                        .sortedByDescending { it.timestamp }
                    _notifications.value = list
                }
            }

        // 4. All Users listener (useful for search / admin viewer)
        allUsersListener = db.collection("users")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening all users", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.toObjects(User::class.java)
                    _allUsers.value = list
                }
            }
            
        // Check following map for my current followers/following state
        db.collection("users").document(uid).collection("following")
            .get()
            .addOnSuccessListener { qSnapshot ->
                val map = qSnapshot.documents.associate { it.id to true }
                _followingState.value = map
            }
    }

    private fun stopRealTimeListeners() {
        postsListener?.remove()
        chatsListener?.remove()
        chatMessagesListener?.remove()
        notificationsListener?.remove()
        userDocListener?.remove()
        commentsListener?.remove()
        allUsersListener?.remove()

        postsListener = null
        chatsListener = null
        chatMessagesListener = null
        notificationsListener = null
        userDocListener = null
        commentsListener = null
        allUsersListener = null
    }

    // POST MANAGEMENT SECTION (Users post only texts)
    fun createPost(content: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val user = _currentUserState.value
        if (user == null) {
            onError("You must be logged in to create a post")
            return
        }
        if (content.trim().isBlank()) {
            onError("Post cannot be empty")
            return
        }

        val postId = UUID.randomUUID().toString()
        val post = Post(
            id = postId,
            authorId = user.id,
            authorName = user.name,
            authorUsername = user.username,
            authorPic = user.profilePic,
            content = content.trim(),
            timestamp = System.currentTimeMillis()
        )

        db.collection("posts").document(postId).set(post)
            .addOnSuccessListener {
                onSuccess()
                // Update posts count in user document
                db.collection("users").document(user.id)
                    .update("postsCount", user.postsCount + 1)
            }
            .addOnFailureListener {
                onError(it.message ?: "Failed to post")
            }
    }

    fun deletePost(postId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val user = _currentUserState.value ?: return
        db.collection("posts").document(postId).delete()
            .addOnSuccessListener {
                onSuccess()
                if (user.postsCount > 0) {
                    db.collection("users").document(user.id)
                        .update("postsCount", user.postsCount - 1)
                }
            }
            .addOnFailureListener {
                onError(it.message ?: "Failed to delete post")
            }
    }

    fun likePost(postId: String) {
        val userId = auth.currentUser?.uid ?: return
        db.collection("posts").document(postId).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val post = doc.toObject(Post::class.java) ?: return@addOnSuccessListener
                    val likes = post.likes.toMutableList()
                    val dislikes = post.dislikes.toMutableList()

                    if (likes.contains(userId)) {
                        likes.remove(userId)
                    } else {
                        likes.add(userId)
                        dislikes.remove(userId) // remove dislike if liking
                    }

                    db.collection("posts").document(postId)
                        .update(mapOf("likes" to likes, "dislikes" to dislikes))
                }
            }
    }

    fun dislikePost(postId: String) {
        val userId = auth.currentUser?.uid ?: return
        db.collection("posts").document(postId).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val post = doc.toObject(Post::class.java) ?: return@addOnSuccessListener
                    val likes = post.likes.toMutableList()
                    val dislikes = post.dislikes.toMutableList()

                    if (dislikes.contains(userId)) {
                        dislikes.remove(userId)
                    } else {
                        dislikes.add(userId)
                        likes.remove(userId) // remove like if disliking
                    }

                    db.collection("posts").document(postId)
                        .update(mapOf("likes" to likes, "dislikes" to dislikes))
                }
            }
    }

    // COMMENTS SECTION
    fun startObservingComments(postId: String) {
        commentsListener?.remove()
        commentsListener = db.collection("posts").document(postId).collection("comments")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listing comments for post $postId", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.toObjects(Comment::class.java)
                    _comments.value = list
                }
            }
    }

    fun stopObservingComments() {
        commentsListener?.remove()
        commentsListener = null
        _comments.value = emptyList()
    }

    fun addComment(postId: String, commentText: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val me = _currentUserState.value ?: return
        if (commentText.trim().isBlank()) return

        val commentId = UUID.randomUUID().toString()
        val comment = Comment(
            id = commentId,
            postId = postId,
            authorId = me.id,
            authorName = me.name,
            authorUsername = me.username,
            authorPic = me.profilePic,
            text = commentText.trim(),
            timestamp = System.currentTimeMillis()
        )

        db.collection("posts").document(postId).collection("comments").document(commentId)
            .set(comment)
            .addOnSuccessListener {
                onSuccess()
                // Update comments count in post
                db.collection("posts").document(postId).get()
                    .addOnSuccessListener { doc ->
                        val postObj = doc.toObject(Post::class.java)
                        if (postObj != null) {
                            db.collection("posts").document(postId)
                                .update("commentsCount", postObj.commentsCount + 1)
                            
                            // Send notification to post author if comments is not from themselves
                            if (postObj.authorId != me.id) {
                                triggerNotification(
                                    recipientId = postObj.authorId,
                                    type = "comment",
                                    message = "commented on your post: \"${commentText.take(40)}\"",
                                    postId = postId
                                )
                            }
                        }
                    }

                // Parse comment for tags (@username)
                parseTagsInComment(commentText, postId)
            }
            .addOnFailureListener {
                onError(it.message ?: "Failed to reply")
            }
    }

    private fun parseTagsInComment(text: String, postId: String) {
        val tags = text.split(" ")
            .filter { it.startsWith("@") && it.length > 1 }
            .map { it.drop(1).lowercase().trim().replace(Regex("[^a-zA-Z0-9_]"), "") }
            .distinct()

        for (username in tags) {
            // Find user matches username
            db.collection("users")
                .whereEqualTo("username", username)
                .get()
                .addOnSuccessListener { snapshot ->
                    if (!snapshot.isEmpty) {
                        val recipient = snapshot.documents[0].toObject(User::class.java) ?: return@addOnSuccessListener
                        if (recipient.id != auth.currentUser?.uid) {
                            triggerNotification(
                                recipientId = recipient.id,
                                type = "tag",
                                message = "tagged you in a comment on post!",
                                postId = postId
                            )
                        }
                    }
                }
        }
    }

    // DIRECT MESSAGING
    fun startObservingChatMessages(chatId: String) {
        chatMessagesListener?.remove()
        chatMessagesListener = db.collection("chats").document(chatId).collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening messages for chat $chatId", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.toObjects(Message::class.java)
                    _chatMessages.value = list
                }
            }
        
        // Reset unread count
        val myUid = auth.currentUser?.uid ?: return
        db.collection("chats").document(chatId).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val chatObj = doc.toObject(Chat::class.java) ?: return@addOnSuccessListener
                    val unreads = chatObj.unreadCounts.toMutableMap()
                    unreads[myUid] = 0
                    db.collection("chats").document(chatId).update("unreadCounts", unreads)
                }
            }
    }

    fun stopObservingChatMessages() {
        chatMessagesListener?.remove()
        chatMessagesListener = null
        _chatMessages.value = emptyList()
    }

    fun sendMessage(receiverId: String, textMessage: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val myUid = auth.currentUser?.uid ?: return
        val me = _currentUserState.value ?: return
        if (textMessage.trim().isBlank()) return

        // Fetch receiver details to ensure we have names in chat index
        db.collection("users").document(receiverId).get()
            .addOnSuccessListener { rxDoc ->
                val targetUser = rxDoc.toObject(User::class.java) ?: return@addOnSuccessListener

                val chatId = getChatId(myUid, receiverId)
                val messageId = UUID.randomUUID().toString()
                val messageObj = Message(
                    id = messageId,
                    senderId = myUid,
                    receiverId = receiverId,
                    text = textMessage.trim(),
                    timestamp = System.currentTimeMillis()
                )

                // Save message
                db.collection("chats").document(chatId).collection("messages").document(messageId)
                    .set(messageObj)
                    .addOnSuccessListener {
                        // Update chat parent index document
                        val chatObj = Chat(
                            id = chatId,
                            participantIds = listOf(myUid, receiverId).sorted(),
                            participantNames = mapOf(myUid to me.name, receiverId to targetUser.name),
                            participantUsernames = mapOf(myUid to me.username, receiverId to targetUser.username),
                            participantPics = mapOf(myUid to me.profilePic, receiverId to targetUser.profilePic),
                            lastMessage = textMessage.trim(),
                            lastMessageSenderId = myUid,
                            lastMessageTime = System.currentTimeMillis(),
                            unreadCounts = mapOf(myUid to 0, receiverId to 1) // Receiver gains 1 unread message
                        )

                        // Set/merge chat index
                        db.collection("chats").document(chatId).set(chatObj)
                            .addOnSuccessListener {
                                onSuccess()
                                // Send DM notification
                                triggerNotification(
                                    recipientId = receiverId,
                                    type = "message",
                                    message = "sent you a message: \"${textMessage.take(30)}\"",
                                    postId = ""
                                )
                            }
                    }
                    .addOnFailureListener {
                        onError(it.message ?: "Failed to send")
                    }
            }
    }

    private fun getChatId(uid1: String, uid2: String): String {
        return if (uid1 < uid2) "${uid1}_${uid2}" else "${uid2}_${uid1}"
    }

    // NOTIFICATIONS SYSTEM
    private fun triggerNotification(recipientId: String, type: String, message: String, postId: String = "") {
        val me = _currentUserState.value ?: return
        val notId = UUID.randomUUID().toString()
        val notif = Notification(
            id = notId,
            recipientId = recipientId,
            senderId = me.id,
            senderName = me.name,
            senderUsername = me.username,
            senderPic = me.profilePic,
            type = type,
            message = message,
            postId = postId,
            read = false,
            timestamp = System.currentTimeMillis()
        )
        db.collection("notifications").document(notId).set(notif)
    }

    fun markAllNotificationsRead() {
        val list = _notifications.value
        val batch = db.batch()
        for (n in list) {
            if (!n.read) {
                batch.update(db.collection("notifications").document(n.id), "read", true)
            }
        }
        batch.commit()
    }

    // PROFILE OF THIRD PARTY / VIEWER
    fun observeUserProfile(userId: String) {
        db.collection("users").document(userId).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    _profileUser.value = doc.toObject(User::class.java)
                }
            }

        // Get posts by this user
        db.collection("posts")
            .whereEqualTo("authorId", userId)
            .get()
            .addOnSuccessListener { snapshot ->
                val list = snapshot.toObjects(Post::class.java)
                    .sortedByDescending { it.timestamp }
                _profilePosts.value = list
            }
    }

    fun clearUserProfileState() {
        _profileUser.value = null
        _profilePosts.value = emptyList()
    }

    // FOLLOW / UNFOLLOW TRANSACTION ACTIONS
    fun toggleFollowUser(targetUserId: String) {
        val myUid = auth.currentUser?.uid ?: return
        val me = _currentUserState.value ?: return
        val isFollowingActive = _followingState.value[targetUserId] ?: false

        db.collection("users").document(targetUserId).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val targetUser = doc.toObject(User::class.java) ?: return@addOnSuccessListener

                    val myFollowingRef = db.collection("users").document(myUid).collection("following").document(targetUserId)
                    val targetFollowersRef = db.collection("users").document(targetUserId).collection("followers").document(myUid)

                    if (isFollowingActive) {
                        // Unfollow
                        myFollowingRef.delete()
                        targetFollowersRef.delete()

                        // Decrement counts
                        val nextMyFollowingCount = (me.followingCount - 1).coerceAtLeast(0)
                        val nextTargetFollowerCount = (targetUser.followersCount - 1).coerceAtLeast(0)

                        db.collection("users").document(myUid).update("followingCount", nextMyFollowingCount)
                        db.collection("users").document(targetUserId).update("followersCount", nextTargetFollowerCount)

                        // Update local map state
                        val newMap = _followingState.value.toMutableMap()
                        newMap.remove(targetUserId)
                        _followingState.value = newMap
                        
                        // Update active screen copy immediately too
                        _profileUser.value = targetUser.copy(followersCount = nextTargetFollowerCount)
                    } else {
                        // Follow
                        myFollowingRef.set(mapOf("followedAt" to System.currentTimeMillis()))
                        targetFollowersRef.set(mapOf("followerId" to myUid))

                        // Increment counts
                        val nextMyFollowingCount = me.followingCount + 1
                        val nextTargetFollowerCount = targetUser.followersCount + 1

                        db.collection("users").document(myUid).update("followingCount", nextMyFollowingCount)
                        db.collection("users").document(targetUserId).update("followersCount", nextTargetFollowerCount)

                        // Update local map state
                        val newMap = _followingState.value.toMutableMap()
                        newMap[targetUserId] = true
                        _followingState.value = newMap

                        // Update active viewer screen copy immediately
                        _profileUser.value = targetUser.copy(followersCount = nextTargetFollowerCount)

                        // Trigger follow notification
                        triggerNotification(
                            recipientId = targetUserId,
                            type = "follow",
                            message = "started following you!"
                        )
                    }
                }
            }
    }

    // UPDATE PROFILE SETTINGS
    fun updateProfile(
        name: String,
        username: String,
        bio: String,
        avatarId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = auth.currentUser?.uid ?: return
        if (name.isBlank() || username.isBlank()) {
            onError("Name and Username are required fields")
            return
        }

        val cleanUsername = username.trim().lowercase().replace(Regex("[^a-zA-Z0-9_]"), "")
        if (cleanUsername.isBlank()) {
            onError("Invalid username")
            return
        }

        // Check if username of someone else matches
        db.collection("users")
            .whereEqualTo("username", cleanUsername)
            .get()
            .addOnSuccessListener { qSnapshot ->
                val other = qSnapshot.documents.firstOrNull { it.id != uid }
                if (other != null) {
                    onError("Username is already taken by another socialize member!")
                    return@addOnSuccessListener
                }

                // Proceed to update
                val trimmedBio = if (bio.split(Regex("\\s+")).size > 150) {
                    // Truncate to 150 words
                    bio.split(Regex("\\s+")).take(150).joinToString(" ")
                } else {
                    bio.trim()
                }

                val updates = mapOf(
                    "name" to name.trim(),
                    "username" to cleanUsername,
                    "bio" to trimmedBio,
                    "profilePic" to avatarId
                )

                db.collection("users").document(uid).update(updates)
                    .addOnSuccessListener {
                        onSuccess()
                        // Update references in all posts of this user (optional but keeps feed looking fresh)
                        updateAuthorRefsInPostsAndComments(uid, name.trim(), cleanUsername, avatarId)
                    }
                    .addOnFailureListener {
                        onError(it.message ?: "Update profile failed")
                    }
            }
            .addOnFailureListener {
                onError("Database error: ${it.message}")
            }
    }

    private fun updateAuthorRefsInPostsAndComments(uid: String, name: String, username: String, pic: String) {
        // Query author's posts and update indices
        db.collection("posts").whereEqualTo("authorId", uid).get()
            .addOnSuccessListener { snapshot ->
                val batch = db.batch()
                for (doc in snapshot.documents) {
                    batch.update(db.collection("posts").document(doc.id), mapOf(
                        "authorName" to name,
                        "authorUsername" to username,
                        "authorPic" to pic
                    ))
                }
                batch.commit()
            }
    }
}
