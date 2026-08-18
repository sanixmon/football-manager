package com.footballmanager.mod

import kotlinx.serialization.Serializable

/**
 * Author-friendly JSON schema for a custom database "mod".
 *
 * A mod is a flat, human-readable JSON file (see `resources/mod/sample-mod.json`)
 * that [ModLoader] turns into the internal `Game` aggregate. Ids are assigned
 * automatically in file order, so mod authors only name clubs and players.
 *
 * Attribute/position/formation/mentality values are given as strings using the
 * same names the engine uses (e.g. `"FINISHING"`, `"ST"`, `"4-3-3"`, `"Attacking"`),
 * so a mod reads like the data it describes.
 */
@Serializable
data class ModFile(
    val name: String,
    val startDate: String,
    val league: ModLeague,
    val clubs: List<ModClub> = emptyList(),
)

@Serializable
data class ModLeague(
    val name: String,
)

@Serializable
data class ModClub(
    val name: String,
    val shortName: String,
    val formation: String = "4-4-2",
    val mentality: String = "Balanced",
    val players: List<ModPlayer> = emptyList(),
)

@Serializable
data class ModPlayer(
    val name: String,
    val position: String,
    val age: Int = 20,
    val nationality: String = "ID",
    val attributes: Map<String, Int> = emptyMap(),
)
