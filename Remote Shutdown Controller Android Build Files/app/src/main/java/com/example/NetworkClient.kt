package com.example

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.isActive
import java.net.ServerSocket
import java.io.InputStream
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketTimeoutException
import java.net.ConnectException
import java.net.NoRouteToHostException

object NetworkClient {

    private const val TIMEOUT_MS = 5000
    private var serverSocket: ServerSocket? = null
    private var listenerJob: Job? = null
    private val clientScope = CoroutineScope(Dispatchers.IO + kotlinx.coroutines.SupervisorJob())

    sealed class NetworkResult {
        data class Success(val response: String) : NetworkResult()
        data class Failure(val message: String) : NetworkResult()
    }

    /**
     * Sends encrypted command bytes to the target server via standard TCP socket.
     * Binds the local socket to the configured port to send and receive from that port.
     */
    suspend fun sendCommand(
        commandBytes: ByteArray,
        ipAddress: String,
        port: Int
    ): NetworkResult = withContext(Dispatchers.IO) {
        var socket: Socket? = null
        try {
            socket = Socket()
            socket.reuseAddress = true
            try {
                // Bind locally to the target port so source port is port (e.g. 40123)
                socket.bind(InetSocketAddress(port))
            } catch (bindException: Exception) {
                // Fail-safe: if local port is already bound or unavailable, let it slide to dynamic port
            }
            
            // Establish socket connection with a explicit timeout
            socket.connect(InetSocketAddress(ipAddress, port), TIMEOUT_MS)
            socket.soTimeout = TIMEOUT_MS

            // Send encrypted byte array
            val outputStream: OutputStream = socket.getOutputStream()
            outputStream.write(commandBytes)
            outputStream.flush()

            // Read the server's reply (buffered read of up to 1024 bytes)
            val inputStream: InputStream = socket.getInputStream()
            val buffer = ByteArray(1024)
            val bytesRead = inputStream.read(buffer)

            val response = if (bytesRead != -1) {
                String(buffer, 0, bytesRead, Charsets.UTF_8).trim()
            } else {
                ""
            }

            NetworkResult.Success(response)
        } catch (e: SocketTimeoutException) {
            NetworkResult.Failure("Connection timed out. Server at $ipAddress:$port did not respond within ${TIMEOUT_MS / 1000}s.")
        } catch (e: ConnectException) {
            NetworkResult.Failure("Connection refused. Server at $ipAddress:$port is likely offline or blocking the port.")
        } catch (e: NoRouteToHostException) {
            NetworkResult.Failure("Host $ipAddress is unreachable. Please verify you are connected to the same local network.")
        } catch (e: Exception) {
            NetworkResult.Failure("Socket exception: ${e.localizedMessage ?: e.message ?: "Unknown socket error"}")
        } finally {
            try {
                socket?.close()
            } catch (ignored: Exception) {
                // Ignore socket close exceptions to avoid overriding primary result
            }
        }
    }

    /**
     * Starts listening for incoming Server connections on the specified port.
     * This ensures the connection from the server is not refused by the app.
     */
    fun startServerListener(port: Int, onMessageReceived: (String) -> Unit) {
        stopServerListener()

        listenerJob = clientScope.launch {
            try {
                val sso = ServerSocket()
                sso.reuseAddress = true
                sso.bind(InetSocketAddress(port))
                serverSocket = sso

                while (isActive && !sso.isClosed) {
                    val client = try {
                        sso.accept()
                    } catch (e: Exception) {
                        null
                    }
                    if (client != null) {
                        launch {
                            try {
                                client.soTimeout = TIMEOUT_MS
                                val input = client.getInputStream()
                                val buffer = ByteArray(1024)
                                val bytesRead = input.read(buffer)
                                val received = if (bytesRead != -1) {
                                    String(buffer, 0, bytesRead, Charsets.UTF_8).trim()
                                } else {
                                    ""
                                }
                                
                                val messageToLog = if (received.isNotEmpty()) received else "Ping/Connection handshake"
                                onMessageReceived(messageToLog)

                                val output = client.getOutputStream()
                                output.write("ACK-RECEIVED".toByteArray(Charsets.UTF_8))
                                output.flush()
                            } catch (e: Exception) {
                                // Ignore exceptions on individual connections
                            } finally {
                                try {
                                    client.close()
                                } catch (ignored: Exception) {}
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                // Server socket error or closed
            }
        }
    }

    /**
     * Stops the background server socket.
     */
    fun stopServerListener() {
        try {
            serverSocket?.close()
        } catch (ignored: Exception) {}
        serverSocket = null
        listenerJob?.cancel()
        listenerJob = null
    }

    /**
     * Checks if the target server is reachable via ICMP protocol (ping).
     */
    suspend fun checkServerOnline(ipAddress: String, port: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            // Run system ping command: -c 1 (1 packet), -W 2 (Wait timeout in seconds)
            val process = Runtime.getRuntime().exec("ping -c 1 -W 2 $ipAddress")
            val exitValue = process.waitFor()
            exitValue == 0
        } catch (e: Exception) {
            // Fallback: Use standard InetAddress isReachable
            try {
                java.net.InetAddress.getByName(ipAddress).isReachable(2500)
            } catch (ex: Exception) {
                false
            }
        }
    }
}
