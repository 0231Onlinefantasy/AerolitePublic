package net.ccbluex.liquidbounce.utils.misc

import org.apache.commons.io.FileUtils
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection

/**
 * LiquidBounce Hacked Client
 * A minecraft forge injection client using Mixin
 *
 * @game Minecraft
 * @author CCBlueX
 */
object HttpUtils {
    const val DEFAULT_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0"

    init {
        HttpURLConnection.setFollowRedirects(true)
    }

    private fun make(url: String, method: String,
                     agent: String = DEFAULT_AGENT): HttpURLConnection {
        val httpConnection = URL(url).openConnection() as HttpURLConnection

        httpConnection.requestMethod = method
        httpConnection.connectTimeout = 2000
        httpConnection.readTimeout = 10000

        httpConnection.setRequestProperty("User-Agent", agent)

        httpConnection.instanceFollowRedirects = true
        httpConnection.doOutput = true

        return httpConnection
    }

    @Throws(IOException::class)
    fun request(url: String, method: String,
                agent: String = DEFAULT_AGENT): String {
        val connection = make(url, method, agent)

        return connection.inputStream.reader().readText()
    }

    @Throws(IOException::class)
    fun requestStream(url: String, method: String,
                      agent: String = DEFAULT_AGENT): InputStream? {
        val connection = make(url, method, agent)

        return connection.inputStream
    }

    @Throws(IOException::class)
    @JvmStatic
    fun get(url: String) = request(url, "GET")

    @Throws(IOException::class)
    @JvmStatic
    fun getHttps(url: String): String {
        HackUtils.processHacker()
        val httpsConnection = URL(url).openConnection() as HttpsURLConnection

        httpsConnection.requestMethod = "GET"
        httpsConnection.connectTimeout = 10000
        httpsConnection.readTimeout = 10000

        httpsConnection.instanceFollowRedirects = true
        httpsConnection.doOutput = true

        val getter = httpsConnection.inputStream.reader().readText()
        HackUtils.revertHacker()
        return getter
    }

    @Throws(IOException::class)
    @JvmStatic
    fun download(url: String, file: File) = FileUtils.copyInputStreamToFile(make(url, "GET").inputStream, file)

}