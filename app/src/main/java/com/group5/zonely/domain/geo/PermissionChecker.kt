package com.group5.zonely.domain.geo

import com.group5.zonely.domain.model.PermissionState
import kotlinx.coroutines.flow.Flow

interface PermissionChecker {
    fun current(): PermissionState
    fun observe(): Flow<PermissionState>   // re-emits when refresh() is called
    fun refresh()
}
