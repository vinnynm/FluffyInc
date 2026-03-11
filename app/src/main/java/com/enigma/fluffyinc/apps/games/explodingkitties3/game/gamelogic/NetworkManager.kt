package com.enigma.fluffyinc.apps.games.explodingkitties3.game.gamelogic

import android.content.Context
import android.net.wifi.WifiManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.ConcurrentHashMap

const val SERVER_PORT = 8989
// --- Network Manager ---
class NetworkManager(
    private val scope: CoroutineScope
) {

    // --- Host Properties ---
    private var serverSocket: ServerSocket? = null
    private val clients = ConcurrentHashMap<Socket, PrintWriter>()

    // --- Client Properties ---
    private var clientSocket: Socket? = null
    private var clientWriter: PrintWriter? = null

    // --- Public Callbacks ---
    var onStateReceived: ((GameStateUpdate) -> Unit)? = null
    var onActionReceived: ((GameAction) -> Unit)? = null
    var onClientConnected: ((String) -> Unit)? = null
    var onClientDisconnected: ((String) -> Unit)? = null
    // --- Host Functions ---

    fun startHost() {
        if (serverSocket != null) return
        scope.launch(Dispatchers.IO) {
            val SERVER_PORT = 0
            serverSocket = ServerSocket(SERVER_PORT)
            while (isActive) {
                try {
                    val clientSocket = serverSocket!!.accept()
                    val writer = PrintWriter(clientSocket.getOutputStream(), true)
                    clients[clientSocket] = writer
                    val clientIp = clientSocket.inetAddress.hostAddress ?: "Unknown"
                    withContext(Dispatchers.Main) { onClientConnected?.invoke(clientIp) }
                    listenToClient(clientSocket)
                } catch (e: Exception) {
                    // Server socket was closed, exit loop
                    break
                }
            }
        }
    }

    private fun listenToClient(socket: Socket) {
        scope.launch(Dispatchers.IO) {
            val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
            try {
                while (isActive) {
                    val message = reader.readLine() ?: break // Client disconnected
                    val action = Json.decodeFromString<GameAction>(message)
                    withContext(Dispatchers.Main) { onActionReceived?.invoke(action) }
                }
            } catch (e: Exception) {
                // Handle client disconnection
            } finally {
                clients.remove(socket)
                val clientIp = socket.inetAddress.hostAddress ?: "Unknown"
                withContext(Dispatchers.Main) { onClientDisconnected?.invoke(clientIp) }
                socket.close()
            }
        }
    }

    fun broadcastStateToClients(stateUpdate: GameStateUpdate) {
        val jsonState = Json.encodeToString(stateUpdate)
        clients.values.forEach { writer ->
            scope.launch(Dispatchers.IO) {
                writer.println(jsonState)
            }
        }
    }
    fun connectToHost(hostIp: String) {
        if (clientSocket != null) return
        scope.launch(Dispatchers.IO) {
            try {
                clientSocket = Socket(hostIp, SERVER_PORT)
                clientWriter = PrintWriter(clientSocket!!.getOutputStream(), true)
                listenForGameStateUpdates()
            } catch (e: Exception) {
                // Handle connection failure
            }
        }
    }

    private fun listenForGameStateUpdates() {
        scope.launch(Dispatchers.IO) {
            val socket = clientSocket ?: return@launch
            val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
            try {
                while (isActive) {
                    val message = reader.readLine() ?: break // Host disconnected
                    val state = Json.decodeFromString<GameStateUpdate>(message)
                    withContext(Dispatchers.Main) { onStateReceived?.invoke(state) }
                }
            } catch (e: Exception) {
                // Handle disconnection
            }
        }
    }

    fun sendActionToHost(action: GameAction) {
        val writer = clientWriter ?: return
        scope.launch(Dispatchers.IO) {
            val jsonAction = Json.encodeToString(action)
            writer.println(jsonAction)
        }
    }

    fun getLocalIPAddress(context: Context): String {
        return try {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val ipAddress = wifiManager.connectionInfo.ipAddress
            InetAddress.getByAddress(
                byteArrayOf(
                    (ipAddress and 0xFF).toByte(),
                    (ipAddress shr 8 and 0xFF).toByte(),
                    (ipAddress shr 16 and 0xFF).toByte(),
                    (ipAddress shr 24 and 0xFF).toByte()
                )
            ).hostAddress ?: "Unavailable"
        } catch (e: Exception) {
            "Unavailable"
        }
    }

    fun disconnect() {
        try {
            serverSocket?.close()
            serverSocket = null
            clients.keys.forEach { it.close() }
            clients.clear()

            clientSocket?.close()
            clientSocket = null
            clientWriter = null
        } catch (e: Exception) {
            // Ignore exceptions on close
        }
    }

}