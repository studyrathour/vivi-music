package com.songify.app.models

import com.music.innertube.models.YTItem
import com.songify.app.db.entities.LocalItem

data class SimilarRecommendation(
    val title: LocalItem,
    val items: List<YTItem>,
)
