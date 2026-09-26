package com.footballmanager.serialization

open class SaveException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
class UnsupportedSaveVersionException(val version: Int, message: String) : SaveException(message)
class CorruptSaveException(message: String, cause: Throwable? = null) : SaveException(message, cause)

object SaveValidator {

    fun validate(dto: SaveGameDto) {
        // 1. Version validation
        if (dto.saveVersion != CURRENT_SAVE_VERSION) {
            throw UnsupportedSaveVersionException(
                dto.saveVersion,
                "Unsupported save version: ${dto.saveVersion} (expected: $CURRENT_SAVE_VERSION)",
            )
        }

        // 2. Clubs validation
        val clubIds = mutableSetOf<Long>()
        for (club in dto.clubs) {
            if (!clubIds.add(club.id)) {
                throw CorruptSaveException("Duplicate club ID: ${club.id}")
            }
            if (club.balance < 0) {
                throw CorruptSaveException("Club ${club.name} has negative balance: ${club.balance}")
            }
        }

        // 3. Players validation
        val playerIds = mutableSetOf<Long>()
        for (player in dto.players) {
            if (!playerIds.add(player.id)) {
                throw CorruptSaveException("Duplicate player ID: ${player.id}")
            }
            if (player.form !in 1..100) {
                throw CorruptSaveException("Player ${player.name} form out of bounds: ${player.form}")
            }
            if (player.fitness !in 0..100) {
                throw CorruptSaveException("Player ${player.name} fitness out of bounds: ${player.fitness}")
            }
            if (player.potential !in 1..100) {
                throw CorruptSaveException("Player ${player.name} potential out of bounds: ${player.potential}")
            }
            for ((attr, value) in player.attributes) {
                if (value !in 1..100) {
                    throw CorruptSaveException("Player ${player.name} attribute $attr out of bounds: $value")
                }
            }
            if (player.injuryDaysRemaining != null && player.injuryDaysRemaining < 0) {
                throw CorruptSaveException("Player ${player.name} has negative injury days: ${player.injuryDaysRemaining}")
            }
            if (player.suspensionMatchesRemaining != null && player.suspensionMatchesRemaining < 0) {
                throw CorruptSaveException("Player ${player.name} has negative suspension matches: ${player.suspensionMatchesRemaining}")
            }
        }

        // 4. Squad registration validation
        val registeredPlayers = mutableSetOf<Long>()
        for (club in dto.clubs) {
            for (pId in club.playerIds) {
                if (pId !in playerIds) {
                    throw CorruptSaveException("Club ${club.name} references non-existent player ID $pId")
                }
                if (!registeredPlayers.add(pId)) {
                    throw CorruptSaveException("Player $pId is registered to multiple clubs")
                }
            }
        }

        // 5. League club validation
        for (cId in dto.league.clubIds) {
            if (cId !in clubIds) {
                throw CorruptSaveException("League references non-existent club ID $cId")
            }
        }

        // 6. Current season validation (if present)
        dto.currentSeason?.let { season ->
            for (fixture in season.fixtures) {
                if (fixture.homeClubId !in clubIds || fixture.awayClubId !in clubIds) {
                    throw CorruptSaveException("Fixture references non-existent club: ${fixture.homeClubId} vs ${fixture.awayClubId}")
                }
            }
            for (result in season.results) {
                if (result.homeClubId !in clubIds || result.awayClubId !in clubIds) {
                    throw CorruptSaveException("MatchResult references non-existent club: ${result.homeClubId} vs ${result.awayClubId}")
                }
            }
        }
    }
}
