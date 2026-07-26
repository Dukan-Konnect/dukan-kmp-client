package org.example.project.core.utils

class BaseURL {
    val url: String
        get() = PROTOCOL_HTTPS + API_ENDPOINT + API_PATH

    val defaultBaseUrl: String
        get() = PROTOCOL_HTTPS + API_ENDPOINT

    fun getUrl(endpoint: String): String {
        return endpoint + API_PATH
    }

    companion object {
        const val API_ENDPOINT = ""
        const val API_PATH = "192.168.2.4:8080/api/"
//        const val API_PATH = "10.0.2.2:8081/api/"
        const val PROTOCOL_HTTPS = "http://"
    }
}
