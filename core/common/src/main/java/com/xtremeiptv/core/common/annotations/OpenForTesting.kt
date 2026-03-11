package com.xtremeiptv.core.common.annotations

/**
 * Annotation to mark classes as open for testing purposes.
 * This is used in combination with the AllOpen plugin.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class OpenForTesting
