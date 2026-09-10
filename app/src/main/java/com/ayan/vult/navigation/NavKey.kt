package com.ayan.vult.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed class VultNavKey : NavKey {
    @Serializable
    data object Dashboard : VultNavKey()
    
    @Serializable
    data object Settings : VultNavKey()
}
