# Pokédex KMP – M2: Integração, Persistência e Arquitetura

**Disciplina:** Programação para Dispositivos Móveis II – UNIVALI  
**Professor:** Welington Gadelha

---

## Integrantes
- Nome 1
- Nome 2

---

## O que foi implementado na M2

### 2 – Arquitetura e Gerenciamento de Estado
| Requisito | Implementação |
|---|---|
| ViewModel oficial (Lifecycle KMP) | `PokedexViewModel`, `DetailViewModel`, `TeamViewModel` |
| Estado reativo (StateFlow) | `PokedexUiState` (Loading / Success / Error), `DetailUiState` (Loading / Success / Error) |

### 3 – Camada de Dados (Offline-First)
| Requisito | Implementação |
|---|---|
| Sincronização inicial | `PokemonRepository.syncIfNeeded()` – busca lista completa na PokeAPI e salva em `pokemon_cache`; skip nas aberturas seguintes |
| Paginação on-demand | `PokedexViewModel.loadNextPage()` com `LIMIT/OFFSET` via `PokemonCacheDao.getPage()` |
| Filtro por nome | Cláusula `LIKE '%name%'` no DAO, debounce de 300 ms no ViewModel |
| Filtro por tipo | Cláusula `LIKE '%type%'` no campo `types` do cache |
| Detalhe em tempo real | `PokemonRepository.fetchDetail()` → requisição HTTP direta à PokeAPI via Ktor |

### 4 – Persistência Local (Room KMP)
| Requisito | Implementação |
|---|---|
| Banco relacional | `AppDatabase` com Room KMP (`androidx.room:room-runtime:2.7.x`) |
| Tabela de cache | `pokemon_cache` – `PokemonCacheEntity` |
| Tabela de favoritos | `pokemon_favorites` – `PokemonFavoriteEntity` |
| Local de captura | Campo `capturedLocation: String` obrigatório em `PokemonFavoriteEntity`; coletado via diálogo (`CaptureLocationDialog`) na tela de detalhes |

---

## Estrutura de arquivos adicionados/modificados

```
composeApp/
├── build.gradle.kts                          ← Room, Ktor, KSP adicionados
├── src/
│   ├── commonMain/kotlin/com/pokedex/
│   │   ├── App.kt                            ← context injetado, novos VMs
│   │   ├── data/
│   │   │   ├── model/Pokemon.kt              ← apiName no enum PokemonType
│   │   │   ├── remote/
│   │   │   │   ├── PokeApiClient.kt          ← cliente Ktor
│   │   │   │   └── PokeApiModels.kt          ← DTOs da PokeAPI
│   │   │   ├── local/
│   │   │   │   ├── AppDatabase.kt            ← Room Database (expect/actual)
│   │   │   │   ├── PokemonEntities.kt        ← PokemonCacheEntity, PokemonFavoriteEntity
│   │   │   │   └── PokemonDao.kt             ← PokemonCacheDao, PokemonFavoriteDao
│   │   │   └── repository/
│   │   │       └── PokemonRepository.kt      ← substituição completa do mock
│   │   ├── viewmodel/
│   │   │   ├── PokedexViewModel.kt           ← NOVO – lista paginada
│   │   │   ├── DetailViewModel.kt            ← NOVO – detalhe HTTP
│   │   │   └── TeamViewModel.kt              ← refatorado – usa Room Flow
│   │   └── ui/
│   │       ├── components/
│   │       │   ├── PokemonCacheCard.kt       ← NOVO – card da listagem
│   │       │   └── SharedComponents.kt       ← + pokemonGradientFromNames
│   │       └── screens/
│   │           ├── PokedexListScreen.kt      ← paginação + filtros
│   │           └── PokemonDetailScreen.kt    ← HTTP + diálogo captura
│   ├── androidMain/kotlin/com/pokedex/
│   │   ├── MainActivity.kt                   ← passa context ao App()
│   │   ├── data/local/AppDatabase.android.kt ← actual getDatabaseBuilder
│   │   └── ui/screens/
│   │       └── TeamBuilderScreen.android.kt  ← usa PokemonFavoriteEntity
│   └── iosMain/kotlin/com/pokedex/
│       └── data/local/AppDatabase.ios.kt     ← actual getDatabaseBuilder
gradle/
└── libs.versions.toml                        ← Room, Ktor, KSP adicionados
```

---

## Como buildar

```bash
# Android
./gradlew :composeApp:assembleDebug

# iOS (requer macOS)
./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64
```
