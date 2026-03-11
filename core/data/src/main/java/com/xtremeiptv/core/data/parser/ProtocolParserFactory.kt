package com.xtremeiptv.core.data.parser

import com.xtremeiptv.core.domain.model.ProtocolType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProtocolParserFactory @Inject constructor(
    private val m3uParser: M3UParser,
    private val xtreamCodesParser: XtreamCodesParser,
    private val stalkerPortalParser: StalkerPortalParser,
    private val macPortalParser: MACPortalParser
) {

    fun createParser(protocolType: ProtocolType): ProtocolParser {
        return when (protocolType) {
            ProtocolType.M3U -> m3uParser
            ProtocolType.XTREAM_CODES -> xtreamCodesParser
            ProtocolType.STALKER_PORTAL -> stalkerPortalParser
            ProtocolType.MAC_PORTAL -> macPortalParser
        }
    }
}
