// Caminho: composeApp/src/commonMain/kotlin/com/pokedex/data/local/PokemonEntities.kt
package com.pokedex.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Cache da listagem da PokeAPI (nomes + IDs).
 * Preenchida na primeira inicialização do app.
 */
@Entity(tableName = "pokemon_cache")
data class PokemonCacheEntity(
    @PrimaryKey val id: Int,
    val name: String,
    /** Tipos separados por vírgula. Ex: "FIRE,FLYING" */
    val types: String = ""
)

/**
 * Pokémons salvos no time/favoritos pelo usuário.
 * Inclui o local de captura obrigatório (nova regra de negócio M2).
 */
@Entity(tableName = "pokemon_favorites")
data class PokemonFavoriteEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val types: String,
    val imageUrl: String,
    val capturedLocation: String   // obrigatório – onde o Pokémon foi capturado
)
