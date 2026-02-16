package com.songify.suraj.lyrics

import android.content.Context
import com.songify.suraj.lyrics.simpmusic.SimpMusicLyrics
import com.songify.suraj.constants.EnableSimpMusicKey
import com.songify.suraj.utils.dataStore
import com.songify.suraj.utils.get

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
