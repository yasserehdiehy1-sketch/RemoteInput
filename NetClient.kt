package com.remoteinput

import android.os.Handler
import android.os.Looper
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString.Companion.toByteString
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.TimeUnit

class NetClient {

    private var ws: WebSocket? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    private val client = OkHttpClient.Builder()
        .pingInterval(20, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    private val buf5 = ByteBuffer.allocate(5).order(ByteOrder.LITTLE_ENDIAN)
    private val buf4 = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN)
    private val buf3 = ByteBuffer.allocate(3).order(ByteOrder.LITTLE_ENDIAN)

    var onStatus: ((Boolean, String) -> Unit)? = null

    private fun notifyStatus(ok: Boolean, msg: String) {
        mainHandler.post { onStatus?.invoke(ok, msg) }
    }

    fun connect(rawHost: String, defaultPort: Int = 8765) {
        disconnect()
        val clean = rawHost.trim().removePrefix("ws://").removePrefix("http://").trimEnd('/')
        if (clean.isEmpty()) {
            notifyStatus(false, "عنوان IP فارغ")
            return
        }
        val url = if (clean.contains(":")) "ws://$clean/" else "ws://$clean:$defaultPort/"
        notifyStatus(false, "جاري الاتصال بـ $url...")

        val req = try {
            Request.Builder().url(url).build()
        } catch (e: Exception) {
            notifyStatus(false, "عنوان غير صالح: ${e.message}")
            return
        }

        ws = client.newWebSocket(req, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                notifyStatus(true, "متصل بـ $clean")
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                this@NetClient.ws = null
                notifyStatus(false, "فشل: ${t.localizedMessage ?: "خطأ غير معروف"}")
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                this@NetClient.ws = null
                notifyStatus(false, "انقطع الاتصال")
            }
        })
    }

    fun disconnect() {
        try {
            ws?.close(1000, null)
        } catch (_: Exception) {}
        ws = null
    }

    private fun send(bytes: ByteArray) {
        ws?.send(bytes.toByteString())
    }

    // Mouse
    fun mouseMove(dx: Float, dy: Float) {
        buf5.clear()
        buf5.put(0x01)
        buf5.putShort(dx.toInt().coerceIn(-32768, 32767).toShort())
        buf5.putShort(dy.toInt().coerceIn(-32768, 32767).toShort())
        send(buf5.array())
    }

    fun mouseButton(button: Int, down: Boolean) =
        send(byteArrayOf(0x02, button.toByte(), if (down) 1 else 0))

    fun scroll(amount: Int) {
        buf3.clear()
        buf3.put(0x03)
        buf3.putShort(amount.coerceIn(-32768, 32767).toShort())
        send(buf3.array())
    }

    // Keyboard
    fun key(vk: Int, down: Boolean) {
        buf4.clear()
        buf4.put(0x04)
        buf4.putShort(vk.toShort())
        buf4.put(if (down) 1 else 0)
        send(buf4.array())
    }

    // Media
    fun mediaKey(code: Int) = send(byteArrayOf(0x05, code.toByte()))

    // System
    fun systemCommand(cmd: Int) = send(byteArrayOf(0x06, cmd.toByte()))

    // ⚡ Emoji / Unicode Text
    fun sendText(text: String) {
        val bytes = text.toByteArray(Charsets.UTF_8)
        val packet = ByteArray(bytes.size + 1)
        packet[0] = 0x07
        System.arraycopy(bytes, 0, packet, 1, bytes.size)
        send(packet)
    }

    // ⚡ Audio Control
    // 1 = Mute PC  |  2 = Vol+  |  3 = Vol-  |  4 = Next Output Device
    fun audioCmd(cmd: Int) = send(byteArrayOf(0x08, cmd.toByte()))

    // Set Volume percentage (0..100)
    fun setVolume(percent: Int) = send(byteArrayOf(0x09, percent.coerceIn(0, 100).toByte()))
}
