package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phone: String,
    val email: String,
    val role: String, // "Member", "Admin", "Treasurer"
    val status: String, // "Pending", "Approved"
    val joinedDate: String
)

@Entity(tableName = "contributions")
data class Contribution(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val userName: String,
    val amount: Double,
    val phoneNumber: String,
    val mpesaReceipt: String,
    val status: String, // "Pending", "Confirmed"
    val date: String,
    val category: String // "Welfare Fund", "Savings", "Merry-Go-Round"
)

@Entity(tableName = "payouts")
data class Payout(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val userName: String,
    val amount: Double,
    val date: String,
    val status: String, // "Pending", "Disbursed"
    val mpesaTransactionId: String
)

@Entity(tableName = "announcements")
data class Announcement(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val date: String,
    val senderRole: String, // "Admin", "Treasurer"
    val type: String // "Welfare", "Meeting", "Reminder"
)

@Entity(tableName = "welfare_requests")
data class WelfareRequest(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val userName: String,
    val title: String,
    val description: String,
    val amountRequested: Double,
    val status: String, // "Pending", "Approved", "Rejected"
    val date: String
)

@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val title: String,
    val description: String,
    val dueDate: String,
    val amount: Double,
    val isRead: Boolean = false
)

@Entity(tableName = "audit_logs")
data class AuditLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val action: String,
    val details: String,
    val timestamp: String,
    val actorName: String
)
