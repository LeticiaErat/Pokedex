// Caminho: composeApp/src/commonMain/kotlin/com/pokedex/data/remote/PokeApiClient.kt
package com.pokedex.data.remote

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

object PokeApiClient {

    private val BASE = "https://pokeapi.co/api/v2"

    val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    /**
     * Busca a lista completa de Pokémons (apenas nome + ID).
     * limit=10000 garante que todos os ~1300 registros venham de uma só chamada.
     */
    suspend fun fetchPokemonList(limit: Int = 10000, offset: Int = 0): PokemonListResponse =
        httpClient.get("$BASE/pokemon?limit=$limit&offset=$offset").body()

    /**
     * Busca os detalhes completos de um Pokémon por ID ou nome.
     */
    suspend fun fetchPokemonDetail(idOrName: String): PokemonDetailResponse =
        httpClient.get("$BASE/pokemon/$idOrName").body()
}
