package com.example.data.model

import com.google.firebase.firestore.DocumentId
import java.io.Serializable

data class User(
    @DocumentId val id: String = "",
    val name: String = "",
    val username: String = "",
    val mobile: String = "",
    val email: String = "",
    val bio: String = "",
    val profilePic: String = "avatar_1", // avatar_1, avatar_2, etc.
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val postsCount: Int = 0,
    val role: String = "user", // "user" or "admin"
    val createdAt: Long = System.currentTimeMillis()
) : Serializable

data class Post(
    @DocumentId val id: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorUsername: String = "",
    val authorPic: String = "avatar_1",
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val likes: List<String> = emptyList(), // list of user IDs
    val dislikes: List<String> = emptyList(), // list of user IDs
    val commentsCount: Int = 0
) : Serializable

data class Comment(
    @DocumentId val id: String = "",
    val postId: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorUsername: String = "",
    val authorPic: String = "avatar_1",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis()
) : Serializable

data class Chat(
    @DocumentId val id: String = "", // sortedUids, e.g. "uid1_uid2"
    val participantIds: List<String> = emptyList(),
    val participantNames: Map<String, String> = emptyMap(),
    val participantUsernames: Map<String, String> = emptyMap(),
    val participantPics: Map<String, String> = emptyMap(),
    val lastMessage: String = "",
    val lastMessageSenderId: String = "",
    val lastMessageTime: Long = System.currentTimeMillis(),
    val unreadCounts: Map<String, Int> = emptyMap()
) : Serializable

data class Message(
    @DocumentId val id: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis()
) : Serializable

data class Notification(
    @DocumentId val id: String = "",
    val recipientId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderUsername: String = "",
    val senderPic: String = "avatar_1",
    val type: String = "follow", // "follow", "tag", "comment", "message"
    val message: String = "",
    val postId: String = "",
    val read: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
) : Serializable
