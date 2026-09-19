package com.netforge.app.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {

    @Query("SELECT * FROM profiles ORDER BY isFavorite DESC, updatedAt DESC")
    fun getAllProfilesFlow(): Flow<List<ProfileEntity>>

    @Query("SELECT * FROM profiles WHERE id = :id LIMIT 1")
    suspend fun getProfileById(id: Long): ProfileEntity?

    @Query("SELECT * FROM profiles WHERE id = :id LIMIT 1")
    fun getProfileFlowById(id: Long): Flow<ProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entity: ProfileEntity): Long

    @Query("UPDATE profiles SET isFavorite = :isFavorite, updatedAt = :updatedAt WHERE id = :id")
    suspend fun setFavorite(id: Long, isFavorite: Boolean, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE profiles SET name = :newName, updatedAt = :updatedAt WHERE id = :id")
    suspend fun renameProfile(id: Long, newName: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE profiles SET importCount = importCount + 1 WHERE id = :id")
    suspend fun incrementImportCount(id: Long)

    @Delete
    suspend fun deleteProfile(entity: ProfileEntity)

    @Query("DELETE FROM profiles WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM profiles")
    suspend fun getCount(): Int
}
