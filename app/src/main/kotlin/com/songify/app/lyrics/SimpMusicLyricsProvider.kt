package com.songify.app.lyrics

import android.content.Context
import com.songify.app.lyrics.simpmusic.SimpMusicLyrics
import com.songify.app.constants.EnableSimpMusicKey
import com.songify.app.utils.dataStore
import com.songify.app.utils.get

object SimpMusicLyricsProvider : LyricsProvider {
    override val name = "SimpMusic"

    override fun isEnabled(context: Context): Boolean = context.dataStore[EnableSimpMusicKey] ?: true

    override suspend fun getLyrics(
        id: String,
        title: String,
        artist: String,
        duration: Int,
    ): Result<String> = SimpMusicLyrics.getLyrics(id, duration)

    override suspend fun getAllLyrics(
        id: String,
        title: String,
        artist: String,
        duration: Int,
        callback: (String) -> Unit,
    ) {
        SimpMusicLyrics.getAllLyrics(id, duration, callback)
    }
}
