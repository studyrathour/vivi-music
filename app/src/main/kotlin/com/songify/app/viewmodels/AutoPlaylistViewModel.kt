package com.songify.app.viewmodels

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.songify.app.constants.HideExplicitKey
import com.songify.app.constants.SongSortDescendingKey
import com.songify.app.constants.SongSortType
import com.songify.app.constants.SongSortTypeKey
import com.songify.app.db.MusicDatabase
import com.songify.app.extensions.filterExplicit
import com.songify.app.extensions.toEnum
import com.songify.app.utils.SyncUtils
import com.songify.app.utils.dataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AutoPlaylistViewModel
@Inject
constructor(
    @ApplicationContext context: Context,
    private val database: MusicDatabase,
    savedStateHandle: SavedStateHandle,
    private val syncUtils: SyncUtils,
) : ViewModel() {
    private val _playlist = MutableStateFlow(savedStateHandle.get<String>("playlist"))
    val playlist = _playlist.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val likedSongs =
        context.dataStore.data
            .map {
                Pair(
                    it[SongSortTypeKey].toEnum(SongSortType.CREATE_DATE) to (it[SongSortDescendingKey]
                        ?: true),
                    it[HideExplicitKey] ?: false
                )
            }
            .distinctUntilChanged()
            .flatMapLatest { (sortDesc, hideExplicit) ->
                val (sortType, descending) = sortDesc
                
                _playlist.filterNotNull().flatMapLatest { playlistName ->
                     when (playlistName) {
                        "liked" -> database.likedSongs(sortType, descending)
                            .map { it.filterExplicit(hideExplicit) }

                        "downloaded" -> database.downloadedSongs(sortType, descending)
                            .map { it.filterExplicit(hideExplicit) }

                        "uploaded" -> database.uploadedSongs(sortType, descending)
                            .map { it.filterExplicit(hideExplicit) }

                        else -> flowOf(emptyList())
                    }
                }
            }
            .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.Lazily, emptyList())

    fun setPlaylist(playlistParam: String) {
        if (_playlist.value != playlistParam) {
            _playlist.value = playlistParam
        }
    }

    fun syncLikedSongs() {
        viewModelScope.launch(Dispatchers.IO) { syncUtils.syncLikedSongs() }
    }

    fun syncUploadedSongs() {
        viewModelScope.launch(Dispatchers.IO) { syncUtils.syncUploadedSongs() }
    }
}
