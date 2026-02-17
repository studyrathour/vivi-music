package com.songify.app.update.viewmodelupdate


import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.songify.app.BuildConfig
import com.songify.app.constants.CheckForUpdatesKey
import com.songify.app.ui.screens.checkForUpdate
import com.songify.app.updatesreen.UpdateInfo
import com.songify.app.updatesreen.UpdateStatus
import com.songify.app.update.experiment.getUpdateCheckInterval
import com.songify.app.update.isNewerVersion
import com.songify.app.utils.dataStore
import com.songify.app.utils.get
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
//new view model for the update

@HiltViewModel
class UpdateViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _updateStatus = MutableStateFlow<UpdateStatus>(UpdateStatus.Loading)
    val updateStatus: StateFlow<UpdateStatus> = _updateStatus.asStateFlow()

    private val _updateInfo = MutableStateFlow<UpdateInfo?>(null)
    val updateInfo: StateFlow<UpdateInfo?> = _updateInfo.asStateFlow()

    private var lastCheckTime: Long = 0L
    private val currentVersion = BuildConfig.VERSION_NAME

    init {
        // Check for updates on initialization
        checkForUpdates()
    }

    /**
     * Check for updates from GitHub releases
     * Respects user's auto-update preference and check interval
     */
    fun checkForUpdates(forceCheck: Boolean = false) {
        viewModelScope.launch {
            val checkForUpdatesEnabled = context.dataStore.get(CheckForUpdatesKey, true)

            if (!checkForUpdatesEnabled && !forceCheck) {
                _updateStatus.value = UpdateStatus.Disabled
                _updateInfo.value = null
                return@launch
            }

            val updateCheckIntervalHours = getUpdateCheckInterval(context)
            val checkInterval = updateCheckIntervalHours * 60 * 60 * 1000L // Convert to milliseconds
            val currentTime = System.currentTimeMillis()

            // Skip check if not enough time has passed (unless forced)
            if (!forceCheck && currentTime - lastCheckTime < checkInterval) {
                return@launch
            }

            // Set loading state only if forced check
            if (forceCheck) {
                _updateStatus.value = UpdateStatus.Loading
            }

            withContext(Dispatchers.IO) {
                checkForUpdate(
                    context = context,
                    onSuccess = { latestVersion, changelog, apkSize, releaseDate, description, imageUrl ->
                        if (isNewerVersion(latestVersion, currentVersion)) {
                            _updateStatus.value = UpdateStatus.UpdateAvailable(latestVersion)
                            _updateInfo.value = UpdateInfo(
                                version = latestVersion,
                                changelog = changelog,
                                apkSize = apkSize,
                                releaseDate = releaseDate,
                                description = description,
                                imageUrl = imageUrl
                            )
                        } else {
                            _updateStatus.value = UpdateStatus.UpToDate
                            _updateInfo.value = null
                        }
                        lastCheckTime = currentTime
                    },
                    onError = {
                        _updateStatus.value = UpdateStatus.Error
                        _updateInfo.value = null
                        lastCheckTime = currentTime
                    }
                )
            }
        }
    }

    /**
     * Refresh update status based on current preferences
     */
    fun refreshUpdateStatus() {
        viewModelScope.launch {
            val checkForUpdatesEnabled = context.dataStore.get(CheckForUpdatesKey, true)

            if (!checkForUpdatesEnabled) {
                _updateStatus.value = UpdateStatus.Disabled
                _updateInfo.value = null
            } else {
                // Trigger a new check
                checkForUpdates(forceCheck = true)
            }
        }
    }

    /**
     * Clear update info
     */
    fun clearUpdateInfo() {
        _updateInfo.value = null
    }

    /**
     * Get current version name
     */
    fun getCurrentVersion(): String = currentVersion
}