package com.songify.suraj.models

import com.music.innertube.models.YTItem
import com.songify.suraj.db.entities.LocalItem

data class SimilarRecommendation(
    val title: LocalItem,
    val items: List<YTItem>,
)
