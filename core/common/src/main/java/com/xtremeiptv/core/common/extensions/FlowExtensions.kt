package com.xtremeiptv.core.common.extensions

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import timber.log.Timber

fun <T> Flow<T>.logErrors(tag: String = "Flow"): Flow<T> = catch { e ->
    Timber.e(e, "Error in $tag flow")
    throw e
}

fun <T> Flow<T>.onStartLoading(emitLoading: suspend () -> Unit): Flow<T> = onStart {
    emitLoading()
}
