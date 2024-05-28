package com.randeep.moviedb.data.remote.networkUtil

import com.randeep.moviedb.R

/**
 * sealed class to handle remote result
 */
sealed class RemoteResult<out T> {
        data class Success<out T>(val value: T): RemoteResult<T>()
        data class Error(val remoteError: RemoteError): RemoteResult<Nothing>()
}

/**
 * class to handle exceptions of api calls
 */
sealed class RemoteError {
        abstract val throwable: Throwable
        abstract val messageResId: Int
}

data class NoInternet(
        override val throwable: Throwable,
        override val messageResId: Int = R.string.no_internet_service,
): RemoteError()

data class TimeOut(
        override val throwable: Throwable,
        override val messageResId: Int = R.string.network_timed_out,
): RemoteError()

data class IOError(
        override val throwable: Throwable,
        override val messageResId: Int = R.string.network_io_error,
): RemoteError()

data class HTTPError(
        override val throwable: Throwable,
        override val messageResId: Int = R.string.network_error,
        val code: Int,
        var httpErrorMessage: String?,
): RemoteError()

data class Other(
        override val throwable: Throwable,
        override val messageResId: Int = R.string.network_error,
        val message: String? = null
): RemoteError()