package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.WelfareDatabase
import com.example.data.model.*
import com.example.data.repository.WelfareRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class WelfareViewModel(application: Application) : AndroidViewModel(application) {
    private val database = WelfareDatabase.getDatabase(application)
    private val repository = WelfareRepository(database.welfareDao())

    // --- SESSION STATE ---
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    // --- SIMULATION STATES ---
    private val _isMpesaProcessing = MutableStateFlow(false)
    val isMpesaProcessing: StateFlow<Boolean> = _isMpesaProcessing.asStateFlow()

    private val _mpesaStatusText = MutableStateFlow("")
    val mpesaStatusText: StateFlow<String> = _mpesaStatusText.asStateFlow()

    private val _showPinDialog = MutableStateFlow(false)
    val showPinDialog: StateFlow<Boolean> = _showPinDialog.asStateFlow()

    // Active contribution details during simulation
    private var pendingContributionAmount = 0.0
    private var pendingContributionPhone = ""
    private var pendingContributionCategory = ""

    // --- RECTIVE FLOWS FROM DB ---
    val allUsers: StateFlow<List<User>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allContributions: StateFlow<List<Contribution>> = repository.allContributions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPayouts: StateFlow<List<Payout>> = repository.allPayouts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAnnouncements: StateFlow<List<Announcement>> = repository.allAnnouncements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allWelfareRequests: StateFlow<List<WelfareRequest>> = repository.allWelfareRequests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAuditLogs: StateFlow<List<AuditLog>> = repository.allAuditLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allGroupDepositAccounts: StateFlow<List<GroupDepositAccount>> = repository.allGroupDepositAccounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered flows based on logged in user
    val userContributions: StateFlow<List<Contribution>> = _currentUser
        .flatMapLatest { user ->
            if (user == null) flowOf(emptyList())
            else repository.getContributionsByUserId(user.id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userPayouts: StateFlow<List<Payout>> = _currentUser
        .flatMapLatest { user ->
            if (user == null) flowOf(emptyList())
            else repository.getPayoutsByUserId(user.id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userWelfareRequests: StateFlow<List<WelfareRequest>> = _currentUser
        .flatMapLatest { user ->
            if (user == null) flowOf(emptyList())
            else repository.getWelfareRequestsByUserId(user.id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userReminders: StateFlow<List<Reminder>> = _currentUser
        .flatMapLatest { user ->
            if (user == null) flowOf(emptyList())
            else repository.getRemindersByUserId(user.id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Calculated Welfare Fund Balances
    val welfareBalance: StateFlow<Double> = allContributions
        .map { list ->
            val totalIn = list.filter { it.status == "Confirmed" }.sumOf { it.amount }
            val totalOut = allPayouts.value.filter { it.status == "Disbursed" }.sumOf { it.amount }
            totalIn - totalOut
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 125000.0) // Default baseline balance

    init {
        seedInitialData()
    }

    private fun getCurrentDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun getCurrentDateTimeString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun seedInitialData() {
        viewModelScope.launch {
            // Seed Group Deposit Accounts if empty
            val groupAccounts = repository.allGroupDepositAccounts.first()
            if (groupAccounts.isEmpty()) {
                repository.insertGroupDepositAccount(
                    GroupDepositAccount(
                        accountType = "M-Pesa Paybill",
                        providerName = "Safaricom",
                        accountNumber = "522522",
                        accountName = "Githima Welfare General Fund",
                        paybillAccountRef = "GITHIMA"
                    )
                )
                repository.insertGroupDepositAccount(
                    GroupDepositAccount(
                        accountType = "Bank Account",
                        providerName = "Equity Bank",
                        accountNumber = "0070179001339",
                        accountName = "Githima Jeshi Welfare Association"
                    )
                )
                repository.insertGroupDepositAccount(
                    GroupDepositAccount(
                        accountType = "M-Pesa Till Number",
                        providerName = "Safaricom",
                        accountNumber = "897654",
                        accountName = "Githima Welfare Emergencies"
                    )
                )
            }

            // Check if users table is empty to seed
            val users = allUsers.value
            if (users.isEmpty()) {
                // Seed members
                val admin = User(id = 1, name = "Lt. Gen. Joseph Kamau", phone = "0711111111", email = "kamau@githimajeshi.or.ke", role = "Admin", status = "Approved", joinedDate = "2026-01-10", payoutMethod = "M-Pesa", payoutPhone = "0711111111", payoutAccountName = "Joseph Kamau")
                val treasurer = User(id = 2, name = "Major Sarah Cherono", phone = "0722222222", email = "cherono@githimajeshi.or.ke", role = "Treasurer", status = "Approved", joinedDate = "2026-01-12", payoutMethod = "Bank Account", payoutBankName = "KCB Bank", payoutBankAccount = "1102938475", payoutAccountName = "Sarah Cherono")
                val member = User(id = 3, name = "Corporal John Onyango", phone = "0733333333", email = "onyango@githimajeshi.or.ke", role = "Member", status = "Approved", joinedDate = "2026-02-15", payoutMethod = "M-Pesa", payoutPhone = "0733333333", payoutAccountName = "John Onyango")
                val pending = User(id = 4, name = "Private Grace Mwangi", phone = "0744444444", email = "mwangi@githimajeshi.or.ke", role = "Member", status = "Pending", joinedDate = "2026-07-15")

                repository.insertUser(admin)
                repository.insertUser(treasurer)
                repository.insertUser(member)
                repository.insertUser(pending)

                // Seed announcements
                repository.insertAnnouncement(
                    Announcement(
                        title = "Quarterly Welfare Meeting",
                        content = "Our next group meeting is scheduled for July 25th, 2026 at the Main Mess Hall. We will discuss welfare fund allocation and approve the next merry-go-round payout schedule.",
                        date = "2026-07-14",
                        senderRole = "Admin",
                        type = "Meeting"
                    )
                )
                repository.insertAnnouncement(
                    Announcement(
                        title = "Welfare Contribution Reminder",
                        content = "Members are reminded that the monthly Welfare Fund contribution of KSh 2,000 is due by the 20th of this month. Please make your payments through the M-Pesa interface.",
                        date = "2026-07-12",
                        senderRole = "Treasurer",
                        type = "Reminder"
                    )
                )

                // Seed contributions
                repository.insertContribution(
                    Contribution(
                        userId = 3,
                        userName = "kelvin mwangi",
                        amount = 2000.0,
                        phoneNumber = "0733333333",
                        mpesaReceipt = "RLF5HG78KJ",
                        status = "Confirmed",
                        date = "2026-06-18",
                        category = "Welfare Fund"
                    )
                )
                repository.insertContribution(
                    Contribution(
                        userId = 3,
                        userName = "kelvin mwangi",
                        amount = 5000.0,
                        phoneNumber = "0733333333",
                        mpesaReceipt = "RLM8YF49RE",
                        status = "Confirmed",
                        date = "2026-06-20",
                        category = "Savings"
                    )
                )
                repository.insertContribution(
                    Contribution(
                        userId = 1,
                        userName = "Lt. Gen. Joseph Kamau",
                        amount = 10000.0,
                        phoneNumber = "0711111111",
                        mpesaReceipt = "RLN1AS76GH",
                        status = "Confirmed",
                        date = "2026-06-19",
                        category = "Savings"
                    )
                )

                // Seed payouts
                repository.insertPayout(
                    Payout(
                        userId = 3,
                        userName = "kelvin mwangi",
                        amount = 35000.0,
                        date = "2026-06-30",
                        status = "Disbursed",
                        mpesaTransactionId = "MPW9IK78RF"
                    )
                )

                // Seed welfare requests
                repository.insertWelfareRequest(
                    WelfareRequest(
                        userId = 3,
                        userName = "kelvin mwangi",
                        title = "Family Medical Assistance",
                        description = "Assistance for medical bills following a sudden hospitalization of my spouse. Hospital bill attached.",
                        amountRequested = 15000.0,
                        status = "Pending",
                        date = "2026-07-15"
                    )
                )

                // Seed reminders
                repository.insertReminder(
                    Reminder(
                        userId = 3,
                        title = "Monthly Contribution Pending",
                        description = "Please pay KSh 2,000 for your July Welfare Fund contribution.",
                        dueDate = "2026-07-20",
                        amount = 2000.0
                    )
                )

                // Seed audit log
                repository.insertAuditLog(
                    AuditLog(
                        action = "System Genesis",
                        details = "Githima Jeshi Welfare application initialized with military-grade security logging.",
                        timestamp = getCurrentDateTimeString(),
                        actorName = "System Core"
                    )
                )

                // Do not set any default logged in user to ensure login screen is shown first
            }
        }
    }

    // --- AUTHENTICATION ---
    fun login(phone: String, pin: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            if (phone.isBlank()) {
                onError("Phone number is required")
                return@launch
            }
            if (pin.isBlank()) {
                onError("PIN is required")
                return@launch
            }
            val user = repository.getUserByPhone(phone)
            if (user != null) {
                if (user.status == "Pending") {
                    onError("Your account is pending administrator approval")
                } else {
                    _currentUser.value = user
                    logAction("User Login", "Logged in successfully.", user.name)
                    onSuccess()
                }
            } else {
                onError("Invalid phone number or PIN")
            }
        }
    }

    fun loginBiometric(phone: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            if (phone.isBlank()) {
                onError("Please select or enter a registered phone number")
                return@launch
            }
            val user = repository.getUserByPhone(phone)
            if (user != null) {
                if (user.status == "Pending") {
                    onError("Your account is pending administrator approval")
                } else {
                    _currentUser.value = user
                    logAction("User Login (Biometric)", "Logged in securely via Biometric Fingerprint.", user.name)
                    onSuccess()
                }
            } else {
                onError("No registered member found with this phone number")
            }
        }
    }

    fun register(name: String, phone: String, email: String, role: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            if (name.isBlank() || phone.isBlank() || email.isBlank()) {
                onError("All fields are required")
                return@launch
            }
            val existing = repository.getUserByPhone(phone)
            if (existing != null) {
                onError("A member with this phone number already exists")
                return@launch
            }

            // New members are pending approval unless they are designated Admins or Treasurers
            val initialStatus = if (role == "Member") "Pending" else "Approved"
            val newUser = User(
                name = name,
                phone = phone,
                email = email,
                role = role,
                status = initialStatus,
                joinedDate = getCurrentDateString()
            )

            val newId = repository.insertUser(newUser)
            logAction("Member Registration", "New account registered as $role. Status: $initialStatus", name)

            if (initialStatus == "Approved") {
                _currentUser.value = newUser.copy(id = newId.toInt())
            }
            onSuccess()
        }
    }

    fun logout() {
        val name = _currentUser.value?.name ?: "Unknown"
        logAction("User Logout", "Logged out of session.", name)
        _currentUser.value = null
    }

    fun refreshData(onComplete: () -> Unit) {
        viewModelScope.launch {
            delay(1200) // Simulated sync time
            logAction("Database Refresh", "Manual sync and refresh completed successfully.", _currentUser.value?.name ?: "Guest")
            onComplete()
        }
    }

    fun switchUser(user: User) {
        _currentUser.value = user
        logAction("Quick Role Switch", "Switched session directly for testing.", user.name)
    }

    // --- M-PESA CONTRIBUTION (STK PUSH SIMULATION) ---
    fun initiateContribution(amount: Double, phone: String, category: String) {
        if (_isMpesaProcessing.value) return
        viewModelScope.launch {
            pendingContributionAmount = amount
            pendingContributionPhone = phone
            pendingContributionCategory = category

            _isMpesaProcessing.value = true
            _mpesaStatusText.value = "Initiating M-Pesa Safaricom Daraja API Connection..."
            delay(1500)

            _mpesaStatusText.value = "Sending STK Push to $phone..."
            delay(1500)

            _mpesaStatusText.value = "STK Push Sent. Awaiting M-Pesa PIN Input on Device..."
            _showPinDialog.value = true
        }
    }

    fun submitSimulatedMpesaPin(pin: String) {
        _showPinDialog.value = false
        viewModelScope.launch {
            _mpesaStatusText.value = "Processing PIN verification..."
            delay(1500)

            _mpesaStatusText.value = "Securing and encrypting transaction ledger..."
            delay(1200)

            val receiptCode = "MPX" + (10000..99999).random() + "JK" + (10..99).random()
            val user = _currentUser.value

            if (user != null) {
                val newContribution = Contribution(
                    userId = user.id,
                    userName = user.name,
                    amount = pendingContributionAmount,
                    phoneNumber = pendingContributionPhone,
                    mpesaReceipt = receiptCode,
                    status = "Confirmed",
                    date = getCurrentDateString(),
                    category = pendingContributionCategory
                )
                repository.insertContribution(newContribution)

                // Log Audit
                logAction(
                    "M-Pesa Contribution",
                    "Contributed KSh ${String.format("%,.2f", pendingContributionAmount)} to $pendingContributionCategory. Receipt: $receiptCode",
                    user.name
                )

                // Add successful payment notification/reminder
                repository.insertReminder(
                    Reminder(
                        userId = user.id,
                        title = "Payment Confirmed",
                        description = "Successfully contributed KSh ${String.format("%,.2f", pendingContributionAmount)} to $pendingContributionCategory. Receipt: $receiptCode.",
                        dueDate = getCurrentDateString(),
                        amount = pendingContributionAmount,
                        isRead = false
                    )
                )
            }

            _mpesaStatusText.value = "Contribution of KSh ${String.format("%,.2f", pendingContributionAmount)} confirmed! Receipt: $receiptCode"
            delay(2000)

            _isMpesaProcessing.value = false
            _mpesaStatusText.value = ""
        }
    }

    fun cancelMpesaSimulation() {
        _showPinDialog.value = false
        _isMpesaProcessing.value = false
        _mpesaStatusText.value = ""
        val user = _currentUser.value?.name ?: "Unknown"
        logAction("M-Pesa STK Push Cancelled", "STK push was cancelled by the user.", user)
    }

    // --- WELFARE REQUESTS ---
    fun requestWelfare(title: String, description: String, amount: Double) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            val request = WelfareRequest(
                userId = user.id,
                userName = user.name,
                title = title,
                description = description,
                amountRequested = amount,
                status = "Pending",
                date = getCurrentDateString()
            )
            repository.insertWelfareRequest(request)
            logAction("Welfare Assistance Requested", "Requested KSh ${String.format("%,.2f", amount)} for '$title'.", user.name)
        }
    }

    fun approveWelfareRequest(request: WelfareRequest) {
        viewModelScope.launch {
            val updated = request.copy(status = "Approved")
            repository.updateWelfareRequest(updated)

            val admin = _currentUser.value?.name ?: "Admin"
            logAction("Welfare Request Approved", "Approved welfare fund request #${request.id} for ${request.userName} (KSh ${String.format("%,.2f", request.amountRequested)}).", admin)

            // Auto trigger simulated disbursement
            disbursePayout(request.userId, request.amountRequested, "Welfare Grant #${request.id}")
        }
    }

    fun rejectWelfareRequest(request: WelfareRequest) {
        viewModelScope.launch {
            val updated = request.copy(status = "Rejected")
            repository.updateWelfareRequest(updated)

            val admin = _currentUser.value?.name ?: "Admin"
            logAction("Welfare Request Rejected", "Rejected welfare fund request #${request.id} for ${request.userName}.", admin)

            // Notify user
            repository.insertReminder(
                Reminder(
                    userId = request.userId,
                    title = "Welfare Request Rejected",
                    description = "Your welfare request for '${request.title}' has been reviewed and rejected.",
                    dueDate = getCurrentDateString(),
                    amount = 0.0
                )
            )
        }
    }

    // --- PAYOUT DISBURSEMENT (M-PESA B2C SIMULATION) ---
    fun disbursePayout(userId: Int, amount: Double, reason: String = "Merry-Go-Round Payout") {
        viewModelScope.launch {
            val adminName = _currentUser.value?.name ?: "Admin"
            val user = repository.getUserById(userId) ?: return@launch

            // Create pending payout
            val payoutId = repository.insertPayout(
                Payout(
                    userId = userId,
                    userName = user.name,
                    amount = amount,
                    date = getCurrentDateString(),
                    status = "Pending",
                    mpesaTransactionId = "PENDING"
                )
            )

            // Simulate automatic M-Pesa payout confirmation
            logAction("Payout Initiated", "M-Pesa B2C payout of KSh ${String.format("%,.2f", amount)} initiated for ${user.name}.", adminName)

            delay(3000)

            val txId = "B2C" + (10000..99999).random() + "XW" + (10..99).random()
            val confirmedPayout = Payout(
                id = payoutId.toInt(),
                userId = userId,
                userName = user.name,
                amount = amount,
                date = getCurrentDateString(),
                status = "Disbursed",
                mpesaTransactionId = txId
            )
            repository.updatePayout(confirmedPayout)

            logAction("M-Pesa Payout Confirmed", "Disbursed KSh ${String.format("%,.2f", amount)} successfully. Transaction: $txId", "Safaricom B2C Gateway")

            // Notify Member
            repository.insertReminder(
                Reminder(
                    userId = userId,
                    title = "M-Pesa Payout Received!",
                    description = "You have received KSh ${String.format("%,.2f", amount)} via M-Pesa for $reason. Transaction: $txId.",
                    dueDate = getCurrentDateString(),
                    amount = amount
                )
            )
        }
    }

    // --- MEMBER APPROVALS ---
    fun approveMember(user: User) {
        viewModelScope.launch {
            val updated = user.copy(status = "Approved")
            repository.updateUser(updated)

            val adminName = _currentUser.value?.name ?: "Admin"
            logAction("Member Approved", "Approved registration of ${user.name} (${user.phone}).", adminName)

            // Notify user by adding a greeting reminder
            repository.insertReminder(
                Reminder(
                    userId = user.id,
                    title = "Welcome to Githima Jeshi Welfare",
                    description = "Your membership has been approved! Together We Save, Together We Grow.",
                    dueDate = getCurrentDateString(),
                    amount = 0.0
                )
            )
        }
    }

    fun removeMember(user: User) {
        viewModelScope.launch {
            repository.deleteUser(user)
            val adminName = _currentUser.value?.name ?: "Admin"
            logAction("Member Removed", "Removed member ${user.name} (${user.phone}) from registry.", adminName)
        }
    }

    // --- ANNOUNCEMENTS ---
    fun createAnnouncement(title: String, content: String, type: String) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            val announcement = Announcement(
                title = title,
                content = content,
                date = getCurrentDateString(),
                senderRole = user.role,
                type = type
            )
            repository.insertAnnouncement(announcement)
            logAction("Announcement Dispatched", "Dispatched group notice: '$title'. Type: $type", user.name)
        }
    }

    // --- CONTRIBUTION SCHEDULE (ADMIN/TREASURER) ---
    fun createContributionSchedule(title: String, amount: Double, dueDate: String) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            val approvedMembers = allUsers.value.filter { it.status == "Approved" && it.role == "Member" }

            for (member in approvedMembers) {
                repository.insertReminder(
                    Reminder(
                        userId = member.id,
                        title = title,
                        description = "A contribution of KSh ${String.format("%,.2f", amount)} is scheduled for '$title' due on $dueDate.",
                        dueDate = dueDate,
                        amount = amount
                    )
                )
            }

            logAction("Schedule Created", "Scheduled contribution '$title' (KSh ${String.format("%,.2f", amount)}) for all active members.", user.name)
        }
    }

    // --- REMINDERS ---
    fun markReminderRead(reminderId: Int) {
        viewModelScope.launch {
            repository.markReminderAsRead(reminderId)
        }
    }

    // --- MEMBER PAYOUT SETTINGS ---
    fun updateUserPayoutSettings(
        payoutMethod: String,
        payoutPhone: String,
        payoutBankName: String,
        payoutBankAccount: String,
        payoutAccountName: String,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            val updated = user.copy(
                payoutMethod = payoutMethod,
                payoutPhone = payoutPhone,
                payoutBankName = payoutBankName,
                payoutBankAccount = payoutBankAccount,
                payoutAccountName = payoutAccountName
            )
            repository.updateUser(updated)
            _currentUser.value = updated
            logAction("Payout Accounts Updated", "Updated receiving account destination: $payoutMethod ($payoutAccountName).", user.name)
            onSuccess()
        }
    }

    // --- GROUP DEPOSIT ACCOUNT CRUD ---
    fun addGroupDepositAccount(
        accountType: String,
        providerName: String,
        accountNumber: String,
        accountName: String,
        paybillAccountRef: String
    ) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            val account = GroupDepositAccount(
                accountType = accountType,
                providerName = providerName,
                accountNumber = accountNumber,
                accountName = accountName,
                paybillAccountRef = paybillAccountRef
            )
            repository.insertGroupDepositAccount(account)
            logAction("Deposit Channel Added", "Added group deposit channel: $accountType - $accountNumber ($accountName)", user.name)
        }
    }

    fun updateGroupDepositAccount(account: GroupDepositAccount) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            repository.updateGroupDepositAccount(account)
            logAction("Deposit Channel Updated", "Updated group deposit channel ID #${account.id} - ${account.accountType}", user.name)
        }
    }

    fun deleteGroupDepositAccount(account: GroupDepositAccount) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            repository.deleteGroupDepositAccount(account)
            logAction("Deposit Channel Deleted", "Deleted group deposit channel ID #${account.id} - ${account.accountNumber}", user.name)
        }
    }

    // --- LOGGER UTILS ---
    private fun logAction(action: String, details: String, actorName: String) {
        viewModelScope.launch {
            repository.insertAuditLog(
                AuditLog(
                    action = action,
                    details = details,
                    timestamp = getCurrentDateTimeString(),
                    actorName = actorName
                )
            )
        }
    }
}
