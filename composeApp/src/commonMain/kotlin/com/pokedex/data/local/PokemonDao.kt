// Caminho: composeApp/src/commonMain/kotlin/com/pokedex/data/local/PokemonDao.kt
package com.pokedex.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// ─── Cache DAO ────────────────────────────────────────────────────────────────

@Dao
interface PokemonCacheDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(pokemons: List<PokemonCacheEntity>)

    @Query("SELECT COUNT(*) FROM pokemon_cache")
    suspend fun count(): Int

    /**
     * Paginação com filtro opcional por nome (LIKE) e tipo (LIKE no campo types).
     * LIMIT/OFFSET implementa o carregamento incremental conforme o scroll.
     */
    @Query("""
        SELECT * FROM pokemon_cache
        WHERE (:nameFilter = '' OR name LIKE '%' || :nameFilter || '%')
          AND (:typeFilter = '' OR types LIKE '%' || :typeFilter || '%')
        ORDER BY id ASC
        LIMIT :limit OFFSET :offset
    """)
    suspend fun getPage(
        nameFilter: String,
        typeFilter: String,
        limit: Int,
        offset: Int
    ): List<PokemonCacheEntity>

    @Query("SELECT * FROM pokemon_cache WHERE id = :id")
    suspend fun getById(id: Int): PokemonCacheEntity?
}

// ─── Favorites DAO ────────────────────────────────────────────────────────────

@Dao
interface PokemonFavoriteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(favorite: PokemonFavoriteEntity)

    @Delete
    suspend fun delete(favorite: PokemonFavoriteEntity)

    @Query("SELECT * FROM pokemon_favorites ORDER BY name ASC")
    fun getAll(): Flow<List<PokemonFavoriteEntity>>

    @Query("SELECT * FROM pokemon_favorites WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): PokemonFavoriteEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM pokemon_favorites WHERE id = :id)")
    suspend fun isFavorite(id: Int): Boolean
}
