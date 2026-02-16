package com.songify.suraj.lyrics

import android.content.Context
import com.music.innertube.YouTube
import com.songify.suraj.constants.EnableYouTubeSubtitleKey
import com.songify.suraj.utils.dataStore
import com.songify.suraj.utils.get

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
