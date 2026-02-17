package com.songify.app.lyrics

import android.content.Context
import com.music.betterlyrics.BetterLyrics
import com.songify.app.constants.EnableBetterLyricsKey
import com.songify.app.utils.dataStore
import com.songify.app.utils.get

object BetterLyricsLyricsProvider : LyricsProvider {
    override val name = "BetterLyrics"

    override fun isEnabled(context: Context): Boolean = context.dataStore[EnableBetterLyricsKey] ?: true 

    override suspend fun getLyrics(
        id: String,
        title: String,
        artist: String,
        duration: Int,
    ): Result<String> = BetterLyrics.getLyrics(title, artist, duration)

    override suspend fun getAllLyrics(
        id: String,
        title: String,
        artist: String,
        duration: Int,
        callback: (String) -> Unit,
    ) = BetterLyrics.getAllLyrics(title, artist, duration, callback)
}

