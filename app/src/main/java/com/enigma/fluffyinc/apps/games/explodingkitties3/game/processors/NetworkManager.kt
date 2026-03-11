package com.enigma.fluffyinc.games.explodingkitties3.game.processors


import android.content.Context
import android.net.wifi.WifiManager
import com.enigma.fluffyinc.apps.games.explodingkitties3.game.gamelogic.GameAction
import com.enigma.fluffyinc.apps.games.explodingkitties3.game.gamelogic.GameStateUpdate
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

class NetworkManager(private val scope: CoroutineScope) {

    private var serverSocket: ServerSocket? = null
    private val clients = ConcurrentHashMap<Socket, PrintWriter>()
    private var clientSocket: Socket? = null
    private var clientWriter: PrintWriter? = null

    var onStateReceived: ((GameStateUpdate) -> Unit)? = null
    var onActionReceived: ((GameAction) -> Unit)? = null
    var onClientConnected: ((String) -> Unit)? = null
    // --- NEW: Callbacks for disconnection events ---
    var onClientDisconnected: ((String) -> Unit)? = null
    var onHostDisconnected: (() -> Unit)? = null

    fun startHost() {
        if (serverSocket != null) return
        scope.launch(Dispatchers.IO) {
            serverSocket = ServerSocket(SERVER_PORT)
            while (isActive) {
                try {
                    val clientSocket = serverSocket!!.accept()
                    val writer = PrintWriter(clientSocket.getOutputStream(), true)
                    val clientIp = clientSocket.inetAddress.hostAddress ?: "Unknown"
                    clients[clientSocket] = writer
                    withContext(Dispatchers.Main) { onClientConnected?.invoke(clientIp) }
                    listenToClient(clientSocket)
                } catch (e: Exception) {
                    break // Server socket closed
                }
            }
        }
    }

    private fun listenToClient(socket: Socket) {
        scope.launch(Dispatchers.IO) {
            val clientIp = socket.inetAddress.hostAddress ?: "Unknown"
            try {
                val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
                while (isActive) {
                    val message = reader.readLine() ?: break // Client disconnected
                    val action = Json.decodeFromString<GameAction>(message)
                    withContext(Dispatchers.Main) { onActionReceived?.invoke(action) }
                }
            } catch (e: Exception) {
                // Exception means client disconnected
            } finally {
                clients.remove(socket)
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
                withContext(Dispatchers.Main) { onClientConnected?.invoke(hostIp) }
                listenForGameStateUpdates()
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { onHostDisconnected?.invoke() }
            }
        }
    }

    private fun listenForGameStateUpdates() {
        scope.launch(Dispatchers.IO) {
            try {
                val socket = clientSocket ?: return@launch
                val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
                while (isActive) {
                    val message = reader.readLine() ?: break // Host disconnected
                    val state = Json.decodeFromString<GameStateUpdate>(message)
                    withContext(Dispatchers.Main) { onStateReceived?.invoke(state) }
                }
            } catch (e: Exception) {
                // Exception means host disconnected
            } finally {
                withContext(Dispatchers.Main) { onHostDisconnected?.invoke() }
            }
        }
    }

    fun sendActionToHost(action: GameAction) {
        clientWriter?.let { writer ->
            scope.launch(Dispatchers.IO) {
                writer.println(Json.encodeToString(action))
            }
        }
    }

    fun getLocalIPAddress(context: Context): String {
        return try {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val ipAddress = wifiManager.connectionInfo.ipAddress
            InetAddress.getByAddress(byteArrayOf((ipAddress and 0xFF).toByte(), (ipAddress shr 8 and 0xFF).toByte(), (ipAddress shr 16 and 0xFF).toByte(), (ipAddress shr 24 and 0xFF).toByte())).hostAddress ?: "Unavailable"
        } catch (e: Exception) { "Unavailable" }
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
        } catch (_: Exception) {}
    }
}
