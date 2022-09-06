package ru.netology.nmedia.database.entities

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Entity(tableName = "post_remote_keys")
data class PostRemoteKeyEntity(
    @PrimaryKey
    @ColumnInfo(name = "type") val type: KeyType,
    @ColumnInfo(name = "id") val id: Long
) {
    @Parcelize
    enum class KeyType : Parcelable {
        BEFORE, AFTER
    }
}