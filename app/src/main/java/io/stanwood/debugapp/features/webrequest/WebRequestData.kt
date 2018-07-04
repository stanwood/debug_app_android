package io.stanwood.debugapp.features.webrequest

import java.util.*

data class WebRequestData(val id: String, val url: String, val requestDate: Date,
                          val error: String?, val method: String?, val protocol: String?, val requestContentLength: Long,
                          val requestContentType: String?, val requestHeaders: List<Pair<String, String>>?,
                          val responseDate: Date?, val responseCode: Int, val responseContentLength: Long,
                          val responseContentType: String?, val responseMessage: String?,
                          val responseHeaders: List<Pair<String, String>>?, val duration: Long)