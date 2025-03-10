package com.example.myapplication.network

import android.util.Log
import com.example.myapplication.network.ServerResponse.Success
import com.google.gson.Gson
import java.io.DataInputStream
import java.net.Socket
import java.nio.ByteBuffer

private const val TAG = "SocketManager"

class SocketManager(
    private val address: String,
    private val port: Int
) {
    private var socket: Socket? = null

    fun connect() {
        socket = Socket(address, port)

        Log.d(TAG, "connected: ${socket?.isConnected}")
    }

    fun sendRequest(user: TestRequest): ServerResponse {
        return try {
            connect()
            send(user)
            return Gson().fromJson(receive(), Success::class.java)
        } catch (e: Throwable) {
            ServerResponse.Error(e.message ?: "Any error")
        } finally {
            close()
        }
    }

    fun send(request: TestRequest) {
        val message = Gson().toJson(request)

        Log.i(TAG, "sending: $message")

        val messageBytes = message.toByteArray()
        val lengthBytes = ByteBuffer.allocate(4).putInt(messageBytes.size).array()

        val outputStream = socket?.getOutputStream()
        outputStream?.write(lengthBytes)
        outputStream?.write(messageBytes)
        outputStream?.flush()
    }

    fun receive(): String {
        val inputStream = DataInputStream(socket?.getInputStream())

        val lengthBytes = ByteArray(4)
        inputStream.readFully(lengthBytes)
        val length = ByteBuffer.wrap(lengthBytes).int

        val buffer = ByteArray(length)
        inputStream.readFully(buffer)
        val message = String(buffer, 0, length)

        Log.d(TAG, "received: $message")
        return message
    }



    fun close() {
        socket?.close()
        socket = null
    }
}
