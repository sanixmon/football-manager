package com.footballmanager.mod

import com.footballmanager.model.Attribute
import com.footballmanager.model.Contract
import com.footballmanager.model.Player
import com.footballmanager.model.PlayerAttributes
import com.footballmanager.model.Position
import java.time.LocalDate

object PlayerMapper {

    fun toPlayer(modPlayer: ModPlayer, id: Long, contractExpiry: LocalDate = LocalDate.of(2030, 6, 30)): Player = Player(
        id = id,
        name = modPlayer.name,
        age = modPlayer.age,
        nationality = modPlayer.nationality,
        naturalPositions = listOf(parsePosition(modPlayer.position)),
        attributes = PlayerAttributes(
            modPlayer.attributes.entries.associate { (key, value) -> parseAttribute(key) to value },
        ),
        contract = Contract(expiresOn = contractExpiry),
        graphicsId = modPlayer.graphicsId,
    )

    fun parsePosition(input: String): Position =
        Position.entries.firstOrNull { it.name.equals(input, ignoreCase = true) }
            ?: error("unknown position: '$input'")

    fun parseAttribute(input: String): Attribute =
        Attribute.entries.firstOrNull { it.name.equals(input, ignoreCase = true) }
            ?: error("unknown attribute: '$input'")
}
