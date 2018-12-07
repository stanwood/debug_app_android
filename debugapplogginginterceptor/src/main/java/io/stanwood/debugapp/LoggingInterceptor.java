/*
 * Copyright (c) 2018 stanwood GmbH
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.stanwood.debugapp;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import okhttp3.Connection;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public final class LoggingInterceptor implements Interceptor {
    private final static String TAG = "OkHttp";
    private final Context context;
    private boolean enableLogcatOut;

    public LoggingInterceptor(Context context, boolean enableLogcatOut) {
        this.context = context.getApplicationContext();
        this.enableLogcatOut = enableLogcatOut;
    }

    public LoggingInterceptor(Context context) {
        this(context, false);
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        DebugAppIntent transaction = new DebugAppIntent(request.url().toString(), new Date());
        Connection connection = chain.connection();
        transaction.setMethod(request.method());
        transaction.setRequestHeaders(request.headers());
        if (connection != null) {
            transaction.setProtocol(connection.protocol().toString());
        }
        RequestBody requestBody = request.body();
        if (requestBody != null) {
            if (requestBody.contentType() != null) {
                transaction.setRequestContentType(requestBody.contentType().toString());
            }
            if (requestBody.contentLength() != -1) {
                transaction.setRequestContentLength(requestBody.contentLength());
            }
        }
        context.sendBroadcast(transaction);
        if (enableLogcatOut) {
            Log.i(TAG, "--> "
                    + '[' + request.method() + "] "
                    + request.url()
                    + (connection != null ? " " + connection.protocol() : "")
                    + (requestBody != null ? " (" + requestBody.contentLength() + "-byte body)" : ""));
        }
        long startNs = System.nanoTime();
        Response response;
        try {
            response = chain.proceed(request);
        } catch (Exception e) {
            transaction.setError(e.toString());
            context.sendBroadcast(transaction);
            if (enableLogcatOut) {
                Log.i(TAG, "<-- FAILED: " + e);
            }
            throw e;
        }
        long durationNs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);
        transaction.setRequestHeaders(response.request().headers());
        transaction.setResponseDate(new Date());
        transaction.setDuration(durationNs);
        transaction.setResponseCode(response.code());
        transaction.setResponseMessage(response.message());
        ResponseBody responseBody = response.body();
        if (responseBody != null) {
            long contentLength = responseBody.contentLength();
            if (contentLength != -1) {
                transaction.setResponseContentLength(responseBody.contentLength());
            }
            if (responseBody.contentType() != null) {
                transaction.setResponseContentType(responseBody.contentType().toString());
            }
        }
        transaction.setResponseHeaders(response.headers());
        context.sendBroadcast(transaction);
        if (enableLogcatOut) {
            Log.i(TAG, "<-- "
                    + response.code()
                    + '[' + request.method() + "] "
                    + (response.message().isEmpty() ? "" : ' ' + response.message())
                    + ' ' + response.request().url()
                    + " (" + durationNs + "ms, "
                    + (responseBody != null && responseBody.contentLength() != -1 ? responseBody.contentLength() + "-byte" : "unknown-length") + " body)");
        }
        return response;
    }

    private static class DebugAppIntent extends Intent {
        DebugAppIntent(String url, Date requestDate) {
            super("io.stanwood.debugapp.plugin");
            putExtra("source", "okhttp_logger");
            putExtra("requestDate", requestDate.getTime());
            putExtra("url", url);
            putExtra("id", requestDate.getTime() + url);
        }

        static String toHttpHeaderList(Headers headers) {
            StringBuilder sb = new StringBuilder();
            for (int size = headers.size(), i = 0; i < size; i++) {
                sb.append(headers.name(i)).append("|").append(headers.value(i)).append("|");
            }
            sb.setLength(Math.max(sb.length() - 1, 0));
            return sb.toString();
        }

        void setResponseDate(Date responseDate) {
            putExtra("responseDate", responseDate.getTime());
        }

        void setError(String error) {
            putExtra("error", error);
        }

        void setMethod(String method) {
            putExtra("method", method);
        }

        void setProtocol(String protocol) {
            putExtra("protocol", protocol);
        }

        void setRequestContentLength(Long requestContentLength) {
            putExtra("requestContentLength", requestContentLength);
        }

        void setRequestContentType(String requestContentType) {
            putExtra("requestContentType", requestContentType);
        }

        void setResponseCode(Integer responseCode) {
            putExtra("responseCode", responseCode);
        }

        void setResponseContentLength(Long responseContentLength) {
            putExtra("responseContentLength", responseContentLength);
        }

        void setResponseContentType(String responseContentType) {
            putExtra("responseContentType", responseContentType);
        }

        void setResponseMessage(String responseMessage) {
            putExtra("responseMessage", responseMessage);
        }

        void setDuration(Long duration) {
            putExtra("duration", duration);
        }

        void setRequestHeaders(Headers headers) {
            putExtra("requestHeaders", toHttpHeaderList(headers));
        }

        void setResponseHeaders(Headers headers) {
            putExtra("responseHeaders", toHttpHeaderList(headers));
        }
    }
}