package com.songify.app.lyrics

import android.content.Context
import com.music.innertube.YouTube
import com.music.innertube.models.WatchEndpoint
import com.songify.app.constants.EnableYouTubeLyricsKey
import com.songify.app.utils.dataStore
import com.songify.app.utils.get

object YouTubeLyricsProvider : LyricsProvider {
    override val name = "YouTube Music"

    override fun isEnabled(context: Context): Boolean = context.dataStore[EnableYouTubeLyricsKey] ?: true

    override suspend fun getLyrics(
        id: String,
        title: String,
        artist: String,
        duration: Int,
    ): Result<String> =
        runCatching {
            val nextResult = YouTube.next(WatchEndpoint(videoId = id)).getOrThrow()
            YouTube
                .lyrics(
                    endpoint = nextResult.lyricsEndpoint
                        ?: throw IllegalStateException("Lyrics endpoint not found"),
                ).getOrThrow() ?: throw IllegalStateException("Lyrics unavailable")
        }
}
