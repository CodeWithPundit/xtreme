package com.xtremeiptv.core.common.analytics

enum class AnalyticsEvents(val eventName: String) {
    // App Lifecycle
    APP_OPEN("app_open"),
    APP_CLOSE("app_close"),
    APP_CRASH("app_crash"),
    
    // Authentication
    LOGIN_SUCCESS("login_success"),
    LOGIN_FAILURE("login_failure"),
    LOGOUT("logout"),
    PROFILE_SWITCH("profile_switch"),
    PROFILE_CREATE("profile_create"),
    PROFILE_DELETE("profile_delete"),
    
    // Content
    STREAM_PLAY("stream_play"),
    STREAM_PAUSE("stream_pause"),
    STREAM_STOP("stream_stop"),
    STREAM_COMPLETE("stream_complete"),
    STREAM_ERROR("stream_error"),
    STREAM_BUFFERING("stream_buffering"),
    
    // EPG
    EPG_VIEW("epg_view"),
    EPG_PROGRAM_CLICK("epg_program_click"),
    EPG_CATCHUP_PLAY("epg_catchup_play"),
    
    // Downloads
    DOWNLOAD_START("download_start"),
    DOWNLOAD_COMPLETE("download_complete"),
    DOWNLOAD_FAIL("download_fail"),
    DOWNLOAD_CANCEL("download_cancel"),
    DOWNLOAD_PAUSE("download_pause"),
    DOWNLOAD_RESUME("download_resume"),
    
    // Recordings
    RECORDING_START("recording_start"),
    RECORDING_STOP("recording_stop"),
    RECORDING_PAUSE("recording_pause"),
    RECORDING_RESUME("recording_resume"),
    RECORDING_COMPLETE("recording_complete"),
    RECORDING_FAIL("recording_fail"),
    
    // Cast
    CAST_START("cast_start"),
    CAST_STOP("cast_stop"),
    CAST_ERROR("cast_error"),
    
    // Picture in Picture
    PIP_ENTER("pip_enter"),
    PIP_EXIT("pip_exit"),
    
    // Search
    SEARCH("search"),
    SEARCH_RESULT_CLICK("search_result_click"),
    
    // Favorites
    ADD_FAVORITE("add_favorite"),
    REMOVE_FAVORITE("remove_favorite"),
    
    // Settings
    SETTINGS_CHANGE("settings_change"),
    SETTINGS_RESET("settings_reset"),
    
    // Network
    NETWORK_CHANGE("network_change"),
    NETWORK_SPEED_TEST("network_speed_test"),
    
    // Ads
    AD_IMPRESSION("ad_impression"),
    AD_CLICK("ad_click"),
    AD_REWARD_EARNED("ad_reward_earned"),
    
    // Errors
    ERROR_OCCURRED("error_occurred"),
    NON_FATAL_ERROR("non_fatal_error")
}
