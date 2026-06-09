package com.example

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Supported server command options for future expansion.
 */
enum class ServerCommand(val displayName: String, val rawToken: String) {
    SHUTDOWN("Shutdown", "SHUTDOWN_SERVER"),
    REBOOT("Reboot", "REBOOT_SERVER"),
    WAKE_ON_LAN("Wake-on-LAN", "WOL_SERVER"),
    RESTART_SERVICE("Restart Service", "RESTART_SERVICE")
}

/**
 * Status of the shutdown action to reflect on the UI screen.
 */
enum class ServerStatus {
    READY,
    SENDING,
    COMMAND_SENT,
    FAILED
}

/**
 * Server availability/online status options
 */
enum class ServerOnlineStatus {
    ONLINE,
    OFFLINE,
    CHECKING,
    UNKNOWN
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPrefs = application.getSharedPreferences("server_controller_prefs", android.content.Context.MODE_PRIVATE)

    private val _ipAddress = MutableStateFlow(sharedPrefs.getString("ip_address", "192.168.1.23") ?: "192.168.1.23")
    val ipAddress: StateFlow<String> = _ipAddress.asStateFlow()

    private val _port = MutableStateFlow(sharedPrefs.getString("port", "40123") ?: "40123")
    val port: StateFlow<String> = _port.asStateFlow()

    private val _secretKey = MutableStateFlow(sharedPrefs.getString("secret_key", "MySecretPassphrase123") ?: "MySecretPassphrase123")
    val secretKey: StateFlow<String> = _secretKey.asStateFlow()

    private val _status = MutableStateFlow(ServerStatus.READY)
    val status: StateFlow<ServerStatus> = _status.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _serverResponse = MutableStateFlow<String?>(null)
    val serverResponse: StateFlow<String?> = _serverResponse.asStateFlow()

    private val _onlineStatus = MutableStateFlow(ServerOnlineStatus.UNKNOWN)
    val onlineStatus: StateFlow<ServerOnlineStatus> = _onlineStatus.asStateFlow()

    private val _isCheckingProgress = MutableStateFlow(false)
    val isCheckingProgress: StateFlow<Boolean> = _isCheckingProgress.asStateFlow()

    private val _incomingServerLog = MutableStateFlow<String?>(null)
    val incomingServerLog: StateFlow<String?> = _incomingServerLog.asStateFlow()

    init {
        startPeriodicPing()
        startServerListener()
    }

    private fun startServerListener() {
        val currentPort = _port.value.trim().toIntOrNull() ?: 40123
        NetworkClient.startServerListener(currentPort) { message ->
            val timestamp = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
            _incomingServerLog.value = "[$timestamp] $message"
            
            // Check if incoming handshake on port 40123 receives "SHUTDOWN-INITIATED-SUCCESSFULLY"
            if (message.trim() == "SHUTDOWN-INITIATED-SUCCESSFULLY") {
                _status.value = ServerStatus.COMMAND_SENT
                _serverResponse.value = "SHUTDOWN-INITIATED-SUCCESSFULLY"
                _errorMessage.value = null
            }
        }
    }

    private fun restartServerListener() {
        NetworkClient.stopServerListener()
        startServerListener()
    }

    fun clearIncomingLog() {
        _incomingServerLog.value = null
    }

    private fun startPeriodicPing() {
        viewModelScope.launch {
            while (true) {
                checkOnlineStatus()
                kotlinx.coroutines.delay(120_000L) // 2 minutes
            }
        }
    }

    fun checkOnlineStatus() {
        viewModelScope.launch {
            _isCheckingProgress.value = true
            _onlineStatus.value = ServerOnlineStatus.CHECKING
            val currentIp = _ipAddress.value.trim()
            val currentPortVal = _port.value.trim().toIntOrNull()

            if (currentIp.isEmpty() || currentPortVal == null || currentPortVal !in 1..65535) {
                _onlineStatus.value = ServerOnlineStatus.UNKNOWN
                _isCheckingProgress.value = false
                return@launch
            }

            val isOnline = NetworkClient.checkServerOnline(currentIp, currentPortVal)
            _onlineStatus.value = if (isOnline) ServerOnlineStatus.ONLINE else ServerOnlineStatus.OFFLINE
            _isCheckingProgress.value = false
        }
    }

    fun updateIpAddress(newIp: String) {
        _ipAddress.value = newIp
        sharedPrefs.edit().putString("ip_address", newIp).apply()
    }

    fun updatePort(newPort: String) {
        _port.value = newPort
        sharedPrefs.edit().putString("port", newPort).apply()
        restartServerListener()
    }

    fun updateSecretKey(newKey: String) {
        _secretKey.value = newKey
        sharedPrefs.edit().putString("secret_key", newKey).apply()
    }

    /**
     * Executes the secure server shutdown command on separate background scope.
     */
    fun sendShutdownCommand(onSuccess: (String) -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch {
            _status.value = ServerStatus.SENDING
            _errorMessage.value = null
            _serverResponse.value = null

            val currentIp = _ipAddress.value.trim()
            val currentPortStr = _port.value.trim()
            val currentKey = _secretKey.value

            // 1. Validation Checks
            if (currentIp.isEmpty()) {
                val err = "IP Address cannot be empty"
                _errorMessage.value = err
                _status.value = ServerStatus.FAILED
                onFailure(err)
                return@launch
            }

            val currentPort = currentPortStr.toIntOrNull()
            if (currentPort == null || currentPort !in 1..65535) {
                val err = "Invalid Port (must be 1-65535)"
                _errorMessage.value = err
                _status.value = ServerStatus.FAILED
                onFailure(err)
                return@launch
            }

            if (currentKey.isEmpty()) {
                val err = "Secret Key cannot be empty"
                _errorMessage.value = err
                _status.value = ServerStatus.FAILED
                onFailure(err)
                return@launch
            }

            // 2. Hashing the Secret Key with SHA-256 and formatting as hexadecimal string
            val hexBytesToSend = try {
                val hashedBytes = CryptoUtils.hashSha256(currentKey)
                val hexString = CryptoUtils.bytesToHex(hashedBytes)
                hexString.toByteArray(Charsets.UTF_8)
            } catch (e: Exception) {
                val err = "Hashing Error: ${e.localizedMessage ?: e.message}"
                _errorMessage.value = err
                _status.value = ServerStatus.FAILED
                onFailure(err)
                return@launch
            }

            // 3. Network Connection
            when (val result = NetworkClient.sendCommand(hexBytesToSend, currentIp, currentPort)) {
                is NetworkClient.NetworkResult.Success -> {
                    val response = result.response.trim()
                    if (response == "SHUTDOWN-INITIATED-SUCCESSFULLY" || _serverResponse.value == "SHUTDOWN-INITIATED-SUCCESSFULLY") {
                        _status.value = ServerStatus.COMMAND_SENT
                        _serverResponse.value = "SHUTDOWN-INITIATED-SUCCESSFULLY"
                        onSuccess("Command successfully sent. Server response: SHUTDOWN-INITIATED-SUCCESSFULLY")
                    } else {
                        // Double check if background listener set success in the meantime
                        if (_serverResponse.value == "SHUTDOWN-INITIATED-SUCCESSFULLY") {
                            _status.value = ServerStatus.COMMAND_SENT
                            onSuccess("Shutdown Initiated (acknowledged via background listener)")
                        } else {
                            _status.value = ServerStatus.FAILED
                            val errorDetail = "Invalid server reply: Got \"$response\" but expected \"SHUTDOWN-INITIATED-SUCCESSFULLY\""
                            _errorMessage.value = errorDetail
                            onFailure(errorDetail)
                        }
                    }
                }
                is NetworkClient.NetworkResult.Failure -> {
                    if (_serverResponse.value == "SHUTDOWN-INITIATED-SUCCESSFULLY") {
                        // The server listener received the success message, so the overall operation succeeded!
                        _status.value = ServerStatus.COMMAND_SENT
                        onSuccess("Shutdown Initiated (acknowledged via background listener)")
                    } else {
                        _status.value = ServerStatus.FAILED
                        _errorMessage.value = result.message
                        onFailure(result.message)
                    }
                }
            }
        }
    }

    /**
     * Resets status indicators back to standard READY layout status.
     */
    fun resetStatus() {
        _status.value = ServerStatus.READY
        _errorMessage.value = null
        _serverResponse.value = null
    }

    override fun onCleared() {
        super.onCleared()
        NetworkClient.stopServerListener()
    }
}
