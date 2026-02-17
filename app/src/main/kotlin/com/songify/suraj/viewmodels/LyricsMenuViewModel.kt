package com.songify.suraj.viewmodels

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.songify.suraj.db.MusicDatabase
import com.songify.suraj.db.entities.LyricsEntity
import com.songify.suraj.db.entities.Song
import com.songify.suraj.lyrics.LyricsHelper
import com.songify.suraj.lyrics.LyricsResult
import com.songify.suraj.models.MediaMetadata
import com.songify.suraj.utils.NetworkConnectivityObserver
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import androidx.datastore.preferences.core.edit
import com.songify.suraj.constants.SwipeGestureEnabledKey
import com.songify.suraj.utils.dataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context
import com.songify.suraj.db.entities.SongEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class LyricsMenuViewModel
@Inject
constructor(
    private val lyricsHelper: LyricsHelper,
    val database: MusicDatabase,
    private val networkConnectivity: NetworkConnectivityObserver,
    @ApplicationContext private val context: Context,
) : ViewModel() {
    private var job: Job? = null
    val results = MutableStateFlow(emptyList<LyricsResult>())
    val isLoading = MutableStateFlow(false)

    private val _isNetworkAvailable = MutableStateFlow(false)
    val isNetworkAvailable: StateFlow<Boolean> = _isNetworkAvailable.asStateFlow()

    private val _currentSong = mutableStateOf<Song?>(null)
    val currentSong: State<Song?> = _currentSong

    val swipeGestureEnabled: StateFlow<Boolean> = context.dataStore.data
        .map { it[SwipeGestureEnabledKey] ?: true }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    fun toggleSwipeGesture() {
        viewModelScope.launch {
            context.dataStore.edit { settings ->
                val current = settings[SwipeGestureEnabledKey] ?: true
                settings[SwipeGestureEnabledKey] = !current
            }
        }
    }

    fun setSwipeGesture(enabled: Boolean) {
        viewModelScope.launch {
            context.dataStore.edit { settings ->
                settings[SwipeGestureEnabledKey] = enabled
            }
        }
    }

    init {
        viewModelScope.launch {
            networkConnectivity.networkStatus.collect { isConnected ->
                _isNetworkAvailable.value = isConnected
            }
        }

        _isNetworkAvailable.value = try {
            networkConnectivity.isCurrentlyConnected()
        } catch (e: Exception) {
            true // Assume connected as fallback
        }
    }

    fun setCurrentSong(song: Song) {
        _currentSong.value = song
    }

    fun search(
        mediaId: String,
        title: String,
        artist: String,
        duration: Int,
    ) {
        isLoading.value = true
        results.value = emptyList()
        job?.cancel()
        job =
            viewModelScope.launch(Dispatchers.IO) {
                lyricsHelper.getAllLyrics(mediaId, title, artist, duration) { result ->
                    results.update {
                        it + result
                    }
                }
                isLoading.value = false
            }
    }

    fun cancelSearch() {
        job?.cancel()
        job = null
    }

    fun toggleRomanization(song: SongEntity) {
        viewModelScope.launch {
            database.query {
                upsert(song.copy(romanizeLyrics = !song.romanizeLyrics))
            }
        }
    }

    fun setRomanization(song: SongEntity, enabled: Boolean) {
        viewModelScope.launch {
            database.query {
                upsert(song.copy(romanizeLyrics = enabled))
            }
        }
    }

    fun updateLyrics(mediaId: String, lyrics: String) {
        viewModelScope.launch {
            database.query {
                upsert(LyricsEntity(id = mediaId, lyrics = lyrics))
            }
        }
    }

    fun refetchLyrics(
        mediaMetadata: MediaMetadata,
        lyricsEntity: LyricsEntity?,
    ) {
        viewModelScope.launch {
            val lyrics = withContext(Dispatchers.IO) {
                lyricsHelper.getLyrics(mediaMetadata)
            }
            database.query {
                lyricsEntity?.let(::delete)
                upsert(LyricsEntity(mediaMetadata.id, lyrics))
            }
        }
    }
}