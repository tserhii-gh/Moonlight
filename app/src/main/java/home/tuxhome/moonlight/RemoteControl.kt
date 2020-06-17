package home.tuxhome.moonlight

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import okhttp3.OkHttpClient
import okhttp3.Request
import java.lang.Exception
import kotlin.system.measureTimeMillis

class RemoteControl {

    val relay_on_url = "http://192.168.4.1/on"
    val relay_off_url = "http://192.168.4.1/off"
    val relay_status_url = "http://192.168.4.1/status"
    val relay_ping_url = "http://192.168.4.1/ping"

    // FIXME test server config
//    private val relay_on_url = "http://192.168.2.101:8000/on"
//    private val relay_off_url = "http://192.168.2.101:8000/off"
//    private val relay_status_url = "http://192.168.2.101:8000/status"
//    private val relay_ping_url = "http://192.168.2.101:8000/ping"
    private val TAG = "Remote Request"

    fun relay_ping(): Boolean {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(relay_ping_url)
            .build()
        val resp = client.newCall(request).execute()
        return resp.code == 200
    }

    fun relay_status(): Boolean {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(relay_status_url)
            .build()
        val resp = client.newCall(request).execute()
//        Log.e(TAG, resp.code.toString())
        return resp.code == 200
    }


    suspend fun relayOff(delay_time: Long = 0): Boolean {
        return GlobalScope.async(Dispatchers.IO) {
            delay(delay_time)
            return@async makeRequest(relay_off_url)
        }.await()
    }

    suspend fun relayOn(delay_time: Long = 0): Boolean {
        return GlobalScope.async(Dispatchers.IO) {
            delay(delay_time)
            return@async makeRequest(relay_on_url)
        }.await()
    }

    private fun makeRequest(url: String): Boolean {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .build()
        try {
            val resp = client.newCall(request).execute()
            return resp.code == 200
        } catch (e: Exception) {
            return false
        }
    }
}