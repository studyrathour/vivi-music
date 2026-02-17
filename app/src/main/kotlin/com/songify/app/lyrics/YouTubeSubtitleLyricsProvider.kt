package com.songify.app.lyrics

import android.content.Context
import com.music.innertube.YouTube
import com.songify.app.constants.EnableYouTubeSubtitleKey
import com.songify.app.utils.dataStore
import com.songify.app.utils.get

object YouTubeSubtitleLyricsProvider : LyricsProvider {
    override val name = "YouTube Subtitle"

    override fun isEnabled(context: Context): Boolean = context.dataStore[EnableYouTubeSubtitleKey] ?: true

    override suspend fun getLyrics(
        id: String,
        title: String,
        artist: String,
        duration: Int,
    ): Result<String> = YouTube.transcript(id)
}
