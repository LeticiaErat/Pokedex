// Caminho: composeApp/src/androidMain/kotlin/com/pokedex/data/local/AppDatabase.android.kt
package com.pokedex.data.local

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

actual fun getDatabaseBuilder(context: Any?): RoomDatabase.Builder<AppDatabase> {
    val appContext = (context as Context).applicationContext
    val dbFile = appContext.getDatabasePath("pokedex.db")
    return Room.databaseBuilder<AppDatabase>(
        context = appContext,
        name = dbFile.absolutePath
    )
}
