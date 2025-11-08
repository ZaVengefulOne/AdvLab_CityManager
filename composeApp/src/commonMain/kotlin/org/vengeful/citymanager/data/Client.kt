package org.vengeful.citymanager.data

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.headers
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

const val USER_AGENT_TAG = "User-agent"
const val USER_AGENT = "Vengeful-user! Version: 0.0.1"
const val HEADER_ACCEPT = "application/json"

val client = HttpClient(CIO) {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        })
    }
    engine {
        https.trustManager = null
    }
}

fun setHttpBuilder(): Headers {
    return headers {
        append(HttpHeaders.Accept, HEADER_ACCEPT)
        append(USER_AGENT_TAG, USER_AGENT)
    }
}