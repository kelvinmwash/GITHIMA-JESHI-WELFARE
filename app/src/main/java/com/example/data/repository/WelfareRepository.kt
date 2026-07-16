package com.example.data.repository

import com.example.data.db.WelfareDao
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

class WelfareRepository(private val welfareDao: WelfareDao) {

    // --- USERS ---
    val allUsers: Flow<List<User>> = welfareDao.getAllUsers()

    suspend fun getUserById(id: Int): User? = welfareDao.getUserById(id)

    suspend fun getUserByPhone(phone: String): User? = welfareDao.getUserByPhone(phone)

    suspend fun insertUser(user: User): Long = welfareDao.insertUser(user)

    suspend fun updateUser(user: User) = welfareDao.updateUser(user)

    suspend fun deleteUser(user: User) = welfareDao.deleteUser(user)

    // --- CONTRIBUTIONS ---
    val allContributions: Flow<List<Contribution>> = welfareDao.getAllContributions()

    fun getContributionsByUserId(userId: Int): Flow<List<Contribution>> =
        welfareDao.getContributionsByUserId(userId)

    suspend fun insertContribution(contribution: Contribution): Long =
        welfareDao.insertContribution(contribution)

    suspend fun updateContribution(contribution: Contribution) =
        welfareDao.updateContribution(contribution)

    // --- PAYOUTS ---
    val allPayouts: Flow<List<Payout>> = welfareDao.getAllPayouts()

    fun getPayoutsByUserId(userId: Int): Flow<List<Payout>> =
        welfareDao.getPayoutsByUserId(userId)

    suspend fun insertPayout(payout: Payout): Long =
        welfareDao.insertPayout(payout)

    suspend fun updatePayout(payout: Payout) =
        welfareDao.updatePayout(payout)

    // --- ANNOUNCEMENTS ---
    val allAnnouncements: Flow<List<Announcement>> = welfareDao.getAllAnnouncements()

    suspend fun insertAnnouncement(announcement: Announcement): Long =
        welfareDao.insertAnnouncement(announcement)

    // --- WELFARE REQUESTS ---
    val allWelfareRequests: Flow<List<WelfareRequest>> = welfareDao.getAllWelfareRequests()

    fun getWelfareRequestsByUserId(userId: Int): Flow<List<WelfareRequest>> =
        welfareDao.getWelfareRequestsByUserId(userId)

    suspend fun insertWelfareRequest(request: WelfareRequest): Long =
        welfareDao.insertWelfareRequest(request)

    suspend fun updateWelfareRequest(request: WelfareRequest) =
        welfareDao.updateWelfareRequest(request)

    // --- REMINDERS ---
    fun getRemindersByUserId(userId: Int): Flow<List<Reminder>> =
        welfareDao.getRemindersByUserId(userId)

    suspend fun insertReminder(reminder: Reminder): Long =
        welfareDao.insertReminder(reminder)

    suspend fun markReminderAsRead(reminderId: Int) =
        welfareDao.markReminderAsRead(reminderId)

    // --- AUDIT LOGS ---
    val allAuditLogs: Flow<List<AuditLog>> = welfareDao.getAllAuditLogs()

    suspend fun insertAuditLog(log: AuditLog): Long =
        welfareDao.insertAuditLog(log)

    // --- GROUP DEPOSIT ACCOUNTS ---
    val allGroupDepositAccounts: Flow<List<GroupDepositAccount>> = welfareDao.getAllGroupDepositAccounts()

    suspend fun insertGroupDepositAccount(account: GroupDepositAccount): Long =
        welfareDao.insertGroupDepositAccount(account)

    suspend fun updateGroupDepositAccount(account: GroupDepositAccount) =
        welfareDao.updateGroupDepositAccount(account)

    suspend fun deleteGroupDepositAccount(account: GroupDepositAccount) =
        welfareDao.deleteGroupDepositAccount(account)
}
