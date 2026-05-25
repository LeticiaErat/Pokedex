// Caminho: composeApp/src/commonMain/kotlin/com/pokedex/data/local/AppDatabase.kt
package com.pokedex.data.local

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

@Database(
    entities = [PokemonCacheEntity::class, PokemonFavoriteEntity::class],
    version = 1
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun pokemonCacheDao(): PokemonCacheDao
    abstract fun pokemonFavoriteDao(): PokemonFavoriteDao
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}

fun buildDatabase(builder: RoomDatabase.Builder<AppDatabase>): AppDatabase =
    builder
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()

/** Cada plataforma fornece seu próprio builder. */
expect fun getDatabaseBuilder(context: Any? = null): RoomDatabase.Builder<AppDatabase>
