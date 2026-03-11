package com.xtremeiptv.core.common.manager

import com.xtremeiptv.core.domain.model.Profile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
    private val preferenceManager: PreferenceManager
) {
    
    private val _activeProfile = MutableStateFlow<Profile?>(null)
    val activeProfile: StateFlow<Profile?> = _activeProfile.asStateFlow()
    
    private val _stalkerToken = MutableStateFlow<String?>(null)
    private val _macSession = MutableStateFlow<MacSession?>(null)
    private val _xtreamSession = MutableStateFlow<XtreamSession?>(null)
    
    fun setActiveProfile(profile: Profile?) {
        _activeProfile.value = profile
        if (profile == null) {
            clearSessions()
        }
    }
    
    fun getActiveProfile(): Profile? = _activeProfile.value
    
    fun setStalkerToken(token: String) {
        _stalkerToken.value = token
        preferenceManager.saveSecureString("stalker_token_${_activeProfile.value?.id}", token)
    }
    
    fun getStalkerToken(): String? {
        return _stalkerToken.value ?: run {
            _activeProfile.value?.id?.let { profileId ->
                preferenceManager.getSecureString("stalker_token_$profileId").takeIf { it.isNotEmpty() }
            }
        }
    }
    
    fun setMacSession(session: MacSession) {
        _macSession.value = session
        preferenceManager.saveSecureString("mac_session_${_activeProfile.value?.id}", session.sessionId)
        preferenceManager.saveSecureLong("mac_session_expiry_${_activeProfile.value?.id}", session.expiryTime)
    }
    
    fun getMacSession(): MacSession? {
        return _macSession.value ?: run {
            _activeProfile.value?.id?.let { profileId ->
                val sessionId = preferenceManager.getSecureString("mac_session_$profileId")
                val expiry = preferenceManager.getSecureLong("mac_session_expiry_$profileId", 0)
                if (sessionId.isNotEmpty() && expiry > System.currentTimeMillis()) {
                    MacSession(sessionId, expiry)
                } else {
                    null
                }
            }
        }
    }
    
    fun setXtreamSession(serverInfo: XtreamServerInfo, userInfo: XtreamUserInfo) {
        _xtreamSession.value = XtreamSession(serverInfo, userInfo)
        // Cache session data
        _activeProfile.value?.id?.let { profileId ->
            preferenceManager.saveSecureString("xtream_server_$profileId", serverInfo.url)
            preferenceManager.saveSecureString("xtream_user_$profileId", userInfo.username)
            preferenceManager.saveSecureLong("xtream_expiry_$profileId", userInfo.expDate.toLongOrNull() ?: 0)
        }
    }
    
    fun isSessionValid(): Boolean {
        return when (_activeProfile.value?.protocolType) {
            ProtocolType.STALKER_PORTAL -> !getStalkerToken().isNullOrEmpty()
            ProtocolType.MAC_PORTAL -> getMacSession()?.isValid() ?: false
            ProtocolType.XTREAM_CODES -> _xtreamSession.value?.isValid() ?: false
            else -> true // M3U doesn't need session
        }
    }
    
    fun clearSessions() {
        _stalkerToken.value = null
        _macSession.value = null
        _xtreamSession.value = null
        
        _activeProfile.value?.id?.let { profileId ->
            preferenceManager.removeSecureKey("stalker_token_$profileId")
            preferenceManager.removeSecureKey("mac_session_$profileId")
            preferenceManager.removeSecureKey("mac_session_expiry_$profileId")
            preferenceManager.removeSecureKey("xtream_server_$profileId")
            preferenceManager.removeSecureKey("xtream_user_$profileId")
            preferenceManager.removeSecureKey("xtream_expiry_$profileId")
        }
    }
    
    fun logout() {
        clearSessions()
        _activeProfile.value = null
    }
}

data class MacSession(
    val sessionId: String,
    val expiryTime: Long
) {
    fun isValid(): Boolean = System.currentTimeMillis() < expiryTime
}

data class XtreamSession(
    val serverInfo: XtreamServerInfo,
    val userInfo: XtreamUserInfo
) {
    fun isValid(): Boolean {
        val expiry = userInfo.expDate.toLongOrNull() ?: 0
        return expiry == 0L || expiry > System.currentTimeMillis() / 1000
    }
}
