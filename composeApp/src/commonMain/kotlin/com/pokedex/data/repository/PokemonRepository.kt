// Caminho: composeApp/src/commonMain/kotlin/com/pokedex/data/repository/PokemonRepository.kt
package com.pokedex.data.repository

import com.pokedex.data.local.AppDatabase
import com.pokedex.data.local.PokemonCacheEntity
import com.pokedex.data.local.PokemonFavoriteEntity
import com.pokedex.data.model.Pokemon
import com.pokedex.data.model.PokemonStats
import com.pokedex.data.model.PokemonType
import com.pokedex.data.remote.PokeApiClient
import com.pokedex.data.remote.PokemonDetailResponse
import kotlinx.coroutines.flow.Flow

class PokemonRepository(private val db: AppDatabase) {

    private val cacheDao = db.pokemonCacheDao()
    private val favoriteDao = db.pokemonFavoriteDao()

    // ─── Sincronização Inicial (Offline-First) ────────────────────────────────

    /**
     * Na primeira abertura do app busca a lista completa na API e persiste localmente.
     * Nas aberturas seguintes, se o banco já tiver dados, não faz nenhuma requisição.
     */
    suspend fun syncIfNeeded() {
        if (cacheDao.count() > 0) return   // banco já populado – nada a fazer

        val response = PokeApiClient.fetchPokemonList()
        val entities = response.results.map { item ->
            PokemonCacheEntity(id = item.id, name = item.name)
        }
        cacheDao.insertAll(entities)
    }

    // ─── Listagem paginada com filtros (banco local) ──────────────────────────

    /**
     * Retorna uma página de Pokémons do banco local.
     * As cláusulas LIKE e LIMIT/OFFSET são construídas pelo DAO.
     */
    suspend fun getPage(
        nameFilter: String = "",
        typeFilter: String = "",
        page: Int = 0,
        pageSize: Int = 20
    ): List<PokemonCacheEntity> = cacheDao.getPage(
        nameFilter = nameFilter,
        typeFilter = typeFilter,
        limit = pageSize,
        offset = page * pageSize
    )

    // ─── Detalhe em tempo real (HTTP direto à PokeAPI) ────────────────────────

    /**
     * Sempre busca da API – garante dados frescos (imagens HD, atributos novos).
     */
    suspend fun fetchDetail(pokemonId: Int): Pokemon {
        val dto = PokeApiClient.fetchPokemonDetail(pokemonId.toString())
        return dto.toDomain()
    }

    // ─── Favoritos (persistência local) ──────────────────────────────────────

    fun getFavorites(): Flow<List<PokemonFavoriteEntity>> = favoriteDao.getAll()

    suspend fun isFavorite(id: Int): Boolean = favoriteDao.isFavorite(id)

    suspend fun addFavorite(pokemon: Pokemon, capturedLocation: String) {
        favoriteDao.insert(
            PokemonFavoriteEntity(
                id = pokemon.id,
                name = pokemon.name,
                types = pokemon.types.joinToString(",") { it.name },
                imageUrl = pokemon.imageUrl,
                capturedLocation = capturedLocation
            )
        )
    }

    suspend fun removeFavorite(id: Int) {
        val entity = favoriteDao.getById(id) ?: return
        favoriteDao.delete(entity)
    }

    // ─── Mapeamento DTO → Domain ──────────────────────────────────────────────

    private fun PokemonDetailResponse.toDomain(): Pokemon {
        val types = this.types
            .sortedBy { it.slot }
            .mapNotNull { typeSlot -> typeSlot.type.name.toPokedexType() }

        val ability = abilities
            .firstOrNull { !it.isHidden }?.ability?.name
            ?: abilities.firstOrNull()?.ability?.name
            ?: "—"

        val statMap = stats.associate { it.stat.name to it.baseStat }

        val imageUrl = sprites.other?.officialArtwork?.frontDefault
            ?: "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/$id.png"

        return Pokemon(
            id = id,
            name = name.replaceFirstChar { it.uppercase() },
            types = types,
            description = "",            // detalhe vem da API; flavor text requer outro endpoint
            weight = weight / 10.0,
            height = height / 10.0,
            category = "—",
            ability = ability.replace('-', ' ').replaceFirstChar { it.uppercase() },
            stats = PokemonStats(
                hp        = statMap["hp"] ?: 0,
                attack    = statMap["attack"] ?: 0,
                defense   = statMap["defense"] ?: 0,
                spAttack  = statMap["special-attack"] ?: 0,
                spDefense = statMap["special-defense"] ?: 0,
                speed     = statMap["speed"] ?: 0
            ),
            imageUrl = imageUrl
        )
    }

    private fun String.toPokedexType(): PokemonType? = PokemonType.entries.firstOrNull {
        it.apiName.equals(this, ignoreCase = true)
    }
}
