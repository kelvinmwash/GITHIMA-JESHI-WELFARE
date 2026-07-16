package com.example.data.db

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WelfareDao {
    // --- USERS ---
    @Query("SELECT * FROM users ORDER BY name ASC")
    fun getAllUsers(): Flow<List<User>>

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Int): User?

    @Query("SELECT * FROM users WHERE phone = :phone LIMIT 1")
    suspend fun getUserByPhone(phone: String): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long

    @Update
    suspend fun updateUser(user: User)

    @Delete
    suspend fun deleteUser(user: User)

    // --- CONTRIBUTIONS ---
    @Query("SELECT * FROM contributions ORDER BY id DESC")
    fun getAllContributions(): Flow<List<Contribution>>

    @Query("SELECT * FROM contributions WHERE userId = :userId ORDER BY id DESC")
    fun getContributionsByUserId(userId: Int): Flow<List<Contribution>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContribution(contribution: Contribution): Long

    @Update
    suspend fun updateContribution(contribution: Contribution)

    // --- PAYOUTS ---
    @Query("SELECT * FROM payouts ORDER BY id DESC")
    fun getAllPayouts(): Flow<List<Payout>>

    @Query("SELECT * FROM payouts WHERE userId = :userId ORDER BY id DESC")
    fun getPayoutsByUserId(userId: Int): Flow<List<Payout>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayout(payout: Payout): Long

    @Update
    suspend fun updatePayout(payout: Payout)

    // --- ANNOUNCEMENTS ---
    @Query("SELECT * FROM announcements ORDER BY id DESC")
    fun getAllAnnouncements(): Flow<List<Announcement>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnouncement(announcement: Announcement): Long

    // --- WELFARE REQUESTS ---
    @Query("SELECT * FROM welfare_requests ORDER BY id DESC")
    fun getAllWelfareRequests(): Flow<List<WelfareRequest>>

    @Query("SELECT * FROM welfare_requests WHERE userId = :userId ORDER BY id DESC")
    fun getWelfareRequestsByUserId(userId: Int): Flow<List<WelfareRequest>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWelfareRequest(request: WelfareRequest): Long

    @Update
    suspend fun updateWelfareRequest(request: WelfareRequest)

    // --- REMINDERS ---
    @Query("SELECT * FROM reminders WHERE userId = :userId ORDER BY id DESC")
    fun getRemindersByUserId(userId: Int): Flow<List<Reminder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: Reminder): Long

    @Query("UPDATE reminders SET isRead = 1 WHERE id = :reminderId")
    suspend fun markReminderAsRead(reminderId: Int)

    // --- AUDIT LOGS ---
    @Query("SELECT * FROM audit_logs ORDER BY id DESC")
    fun getAllAuditLogs(): Flow<List<AuditLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLog): Long

    // --- GROUP DEPOSIT ACCOUNTS ---
    @Query("SELECT * FROM group_deposit_accounts ORDER BY id ASC")
    fun getAllGroupDepositAccounts(): Flow<List<GroupDepositAccount>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroupDepositAccount(account: GroupDepositAccount): Long

    @Update
    suspend fun updateGroupDepositAccount(account: GroupDepositAccount)

    @Delete
    suspend fun deleteGroupDepositAccount(account: GroupDepositAccount)
}
