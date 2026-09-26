package com.footballmanager.model

import java.time.LocalDate

object EmergencyGoalkeeper {
    fun create(clubId: Long = 0L, id: Long = if (clubId != 0L) -clubId else -9999L): Player = Player(
        id = id,
        name = "Academy Emergency GK",
        age = 17,
        nationality = "ID",
        naturalPositions = listOf(Position.GK),
        attributes = PlayerAttributes(
            mapOf(
                Attribute.GOALKEEPER to 50,
                Attribute.PHYSICAL to 50,
                Attribute.PASSING to 40,
                Attribute.DEFENDING to 40,
                Attribute.PACE to 40,
                Attribute.DRIBBLING to 30,
                Attribute.SHOOTING to 20,
            ),
        ),
        contract = Contract(weeklyWage = 200L, expiresOn = LocalDate.of(2030, 6, 30)),
        potential = 60,
        form = 50,
    )
}
