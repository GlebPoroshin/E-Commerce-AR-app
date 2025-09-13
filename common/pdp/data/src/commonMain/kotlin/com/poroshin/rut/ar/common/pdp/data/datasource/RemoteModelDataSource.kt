package com.poroshin.rut.ar.common.pdp.data.datasource

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.HttpResponse
import io.ktor.http.contentLength
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readAvailable
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem

class RemoteModelDataSource(
    private val httpClient: HttpClient,
) {
    suspend fun downloadFile(
        url: String,
        dest: Path,
        onProgress: (received: Long, total: Long?) -> Unit,
    ) {
        SystemFileSystem.createDirectories(dest.parent!!, mustCreate = false)

        httpClient.prepareGet(url).execute { response: HttpResponse ->
            val total = response.contentLength()
            val channel: ByteReadChannel = response.body()

            SystemFileSystem.sink(dest).buffered().use { sink ->
                val buf = ByteArray(BUFFER)
                var readTotal = 0L
                while (true) {
                    val n = channel.readAvailable(buf, 0, buf.size)
                    if (n == -1) break
                    sink.write(buf, 0, n)
                    readTotal += n
                    onProgress(readTotal, total)
                }
                sink.flush()
            }
        }
    }

    private val BUFFER = 64 * 1024
}