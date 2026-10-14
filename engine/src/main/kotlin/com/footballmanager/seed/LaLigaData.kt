package com.footballmanager.seed

import com.footballmanager.model.Attribute
import com.footballmanager.model.Club
import com.footballmanager.model.Contract
import com.footballmanager.model.Facilities
import com.footballmanager.model.Finance
import com.footballmanager.model.Game
import com.footballmanager.model.League
import com.footballmanager.model.MAX_ATTRIBUTE
import com.footballmanager.model.MIN_ATTRIBUTE
import com.footballmanager.model.Player
import com.footballmanager.model.PlayerAttributes
import com.footballmanager.model.Position
import com.footballmanager.model.PositionWeights
import com.footballmanager.model.Squad
import com.footballmanager.simulation.Formation
import com.footballmanager.simulation.Mentality
import com.footballmanager.simulation.Tactics
import com.footballmanager.simulation.Team
import java.time.LocalDate

/**
 * Authentic LaLiga EA Sports 2026/2027 Datapack.
 *
 * Contains the premier 10 Spanish top-flight clubs with real squads, latest summer signings
 * (Mbappé, Álvarez, Dani Olmo, Gallagher, Sørloth, Le Normand, etc.), realistic financial
 * budgets, and authentic tactical formations.
 */
object LaLigaData {

    const val LEAGUE_ID = 2L
    const val LEAGUE_NAME = "LaLiga EA Sports"
    val START_DATE: LocalDate = LocalDate.of(2026, 8, 15)

    data class PlayerData(
        val name: String,
        val position: Position,
        val age: Int,
        val nationality: String,
        val rating: Int,
        val potential: Int = rating + 3,
        val wage: Long = (rating * 1_200L),
    )

    data class ClubData(
        val name: String,
        val shortName: String,
        val formation: Formation,
        val mentality: Mentality,
        val balance: Long,
        val transferBudget: Long,
        val wageBudget: Long,
        val trainingLevel: Int,
        val stadiumCapacity: Int,
        val players: List<PlayerData>,
    )

    private val clubsData: List<ClubData> by lazy {
        listOf(
            ClubData(
                name = "Real Madrid CF",
                shortName = "RMA",
                formation = Formation.FOUR_THREE_THREE,
                mentality = Mentality.ATTACKING,
                balance = 250_000_000L,
                transferBudget = 120_000_000L,
                wageBudget = 5_000_000L,
                trainingLevel = 5,
                stadiumCapacity = 85_000,
                players = listOf(
                    PlayerData("Thibaut Courtois", Position.GK, 34, "BEL", 89, 89, 280_000L),
                    PlayerData("Andriy Lunin", Position.GK, 27, "UKR", 82, 85, 90_000L),
                    PlayerData("Antonio Rüdiger", Position.CB, 33, "GER", 88, 88, 220_000L),
                    PlayerData("Éder Militão", Position.CB, 28, "BRA", 86, 88, 190_000L),
                    PlayerData("David Alaba", Position.CB, 34, "AUT", 83, 83, 200_000L),
                    PlayerData("Dani Carvajal", Position.RB, 34, "ESP", 86, 86, 180_000L),
                    PlayerData("Ferland Mendy", Position.LB, 31, "FRA", 83, 83, 150_000L),
                    PlayerData("Fran García", Position.LB, 27, "ESP", 79, 82, 70_000L),
                    PlayerData("Jude Bellingham", Position.AM, 23, "ENG", 91, 95, 320_000L),
                    PlayerData("Federico Valverde", Position.CM, 28, "URU", 89, 91, 260_000L),
                    PlayerData("Eduardo Camavinga", Position.DM, 23, "FRA", 87, 92, 190_000L),
                    PlayerData("Aurélien Tchouaméni", Position.DM, 26, "FRA", 87, 90, 200_000L),
                    PlayerData("Arda Güler", Position.AM, 21, "TUR", 83, 91, 95_000L),
                    PlayerData("Kylian Mbappé", Position.ST, 27, "FRA", 92, 94, 450_000L),
                    PlayerData("Vinícius Júnior", Position.LW, 26, "BRA", 91, 94, 380_000L),
                    PlayerData("Rodrygo", Position.RW, 25, "BRA", 88, 91, 220_000L),
                    PlayerData("Brahim Díaz", Position.RW, 27, "MAR", 84, 86, 120_000L),
                    PlayerData("Endrick", Position.ST, 20, "BRA", 81, 92, 85_000L),
                ),
            ),
            ClubData(
                name = "FC Barcelona",
                shortName = "FCB",
                formation = Formation.FOUR_THREE_THREE,
                mentality = Mentality.ATTACKING,
                balance = 120_000_000L,
                transferBudget = 60_000_000L,
                wageBudget = 4_200_000L,
                trainingLevel = 5,
                stadiumCapacity = 99_000,
                players = listOf(
                    PlayerData("Marc-André ter Stegen", Position.GK, 34, "GER", 88, 88, 240_000L),
                    PlayerData("Iñaki Peña", Position.GK, 27, "ESP", 79, 82, 60_000L),
                    PlayerData("Pau Cubarsí", Position.CB, 19, "ESP", 83, 93, 75_000L),
                    PlayerData("Jules Koundé", Position.RB, 27, "FRA", 86, 88, 190_000L),
                    PlayerData("Alejandro Balde", Position.LB, 22, "ESP", 83, 89, 110_000L),
                    PlayerData("Iñigo Martínez", Position.CB, 35, "ESP", 81, 81, 140_000L),
                    PlayerData("Andreas Christensen", Position.CB, 30, "DEN", 83, 84, 150_000L),
                    PlayerData("Eric García", Position.CB, 25, "ESP", 79, 82, 80_000L),
                    PlayerData("Pedri", Position.CM, 23, "ESP", 89, 93, 220_000L),
                    PlayerData("Gavi", Position.CM, 22, "ESP", 87, 92, 180_000L),
                    PlayerData("Frenkie de Jong", Position.CM, 29, "NED", 87, 88, 290_000L),
                    PlayerData("Dani Olmo", Position.AM, 28, "ESP", 87, 88, 200_000L),
                    PlayerData("Marc Casadó", Position.DM, 22, "ESP", 81, 88, 65_000L),
                    PlayerData("Lamine Yamal", Position.RW, 19, "ESP", 89, 96, 160_000L),
                    PlayerData("Robert Lewandowski", Position.ST, 38, "POL", 87, 87, 320_000L),
                    PlayerData("Raphinha", Position.LW, 29, "BRA", 88, 88, 210_000L),
                    PlayerData("Ferran Torres", Position.ST, 26, "ESP", 82, 84, 130_000L),
                    PlayerData("Fermín López", Position.AM, 23, "ESP", 82, 88, 80_000L),
                ),
            ),
            ClubData(
                name = "Atlético de Madrid",
                shortName = "ATM",
                formation = Formation.THREE_FIVE_TWO,
                mentality = Mentality.BALANCED,
                balance = 140_000_000L,
                transferBudget = 70_000_000L,
                wageBudget = 3_800_000L,
                trainingLevel = 4,
                stadiumCapacity = 70_000,
                players = listOf(
                    PlayerData("Jan Oblak", Position.GK, 33, "SVN", 88, 88, 250_000L),
                    PlayerData("Juan Musso", Position.GK, 32, "ARG", 80, 80, 65_000L),
                    PlayerData("Robin Le Normand", Position.CB, 29, "ESP", 85, 86, 150_000L),
                    PlayerData("José María Giménez", Position.CB, 31, "URU", 84, 84, 160_000L),
                    PlayerData("Axel Witsel", Position.CB, 37, "BEL", 80, 80, 110_000L),
                    PlayerData("Clément Lenglet", Position.CB, 31, "FRA", 80, 80, 120_000L),
                    PlayerData("Marcos Llorente", Position.RB, 31, "ESP", 83, 83, 140_000L),
                    PlayerData("Reinildo", Position.LB, 32, "MOZ", 80, 80, 85_000L),
                    PlayerData("Conor Gallagher", Position.CM, 26, "ENG", 84, 87, 160_000L),
                    PlayerData("Rodrigo De Paul", Position.CM, 32, "ARG", 84, 84, 170_000L),
                    PlayerData("Koke", Position.CM, 34, "ESP", 82, 82, 160_000L),
                    PlayerData("Pablo Barrios", Position.CM, 23, "ESP", 81, 88, 70_000L),
                    PlayerData("Samuel Lino", Position.LM, 26, "BRA", 82, 85, 95_000L),
                    PlayerData("Julián Álvarez", Position.ST, 26, "ARG", 88, 92, 240_000L),
                    PlayerData("Antoine Griezmann", Position.AM, 35, "FRA", 87, 87, 260_000L),
                    PlayerData("Alexander Sørloth", Position.ST, 30, "NOR", 84, 84, 150_000L),
                    PlayerData("Ángel Correa", Position.ST, 31, "ARG", 82, 82, 130_000L),
                    PlayerData("Rodrigo Riquelme", Position.LW, 26, "ESP", 81, 84, 80_000L),
                ),
            ),
            ClubData(
                name = "Athletic Club Bilbao",
                shortName = "ATH",
                formation = Formation.FOUR_TWO_THREE_ONE,
                mentality = Mentality.ATTACKING,
                balance = 90_000_000L,
                transferBudget = 45_000_000L,
                wageBudget = 2_200_000L,
                trainingLevel = 4,
                stadiumCapacity = 53_000,
                players = listOf(
                    PlayerData("Unai Simón", Position.GK, 29, "ESP", 86, 88, 140_000L),
                    PlayerData("Julen Agirrezabala", Position.GK, 25, "ESP", 80, 84, 55_000L),
                    PlayerData("Dani Vivian", Position.CB, 27, "ESP", 83, 85, 100_000L),
                    PlayerData("Yeray Álvarez", Position.CB, 31, "ESP", 81, 81, 90_000L),
                    PlayerData("Aitor Paredes", Position.CB, 26, "ESP", 79, 82, 65_000L),
                    PlayerData("Óscar de Marcos", Position.RB, 37, "ESP", 78, 78, 70_000L),
                    PlayerData("Yuri Berchiche", Position.LB, 36, "ESP", 79, 79, 85_000L),
                    PlayerData("Andoni Gorosabel", Position.RB, 29, "ESP", 78, 78, 60_000L),
                    PlayerData("Iñigo Ruiz de Galarreta", Position.CM, 33, "ESP", 81, 81, 75_000L),
                    PlayerData("Beñat Prados", Position.CM, 25, "ESP", 80, 84, 60_000L),
                    PlayerData("Mikel Vesga", Position.DM, 33, "ESP", 78, 78, 65_000L),
                    PlayerData("Ander Herrera", Position.CM, 37, "ESP", 77, 77, 70_000L),
                    PlayerData("Oihan Sancet", Position.AM, 26, "ESP", 83, 86, 110_000L),
                    PlayerData("Nico Williams", Position.LW, 24, "ESP", 87, 91, 190_000L),
                    PlayerData("Iñaki Williams", Position.RW, 32, "GHA", 83, 83, 140_000L),
                    PlayerData("Gorka Guruzeta", Position.ST, 29, "ESP", 81, 82, 85_000L),
                    PlayerData("Alex Berenguer", Position.LW, 31, "ESP", 80, 80, 80_000L),
                    PlayerData("Álvaro Djaló", Position.RW, 27, "ESP", 79, 81, 75_000L),
                ),
            ),
            ClubData(
                name = "Real Sociedad",
                shortName = "RSO",
                formation = Formation.FOUR_THREE_THREE,
                mentality = Mentality.BALANCED,
                balance = 85_000_000L,
                transferBudget = 40_000_000L,
                wageBudget = 2_100_000L,
                trainingLevel = 4,
                stadiumCapacity = 40_000,
                players = listOf(
                    PlayerData("Álex Remiro", Position.GK, 31, "ESP", 84, 85, 95_000L),
                    PlayerData("Unai Marrero", Position.GK, 24, "ESP", 76, 80, 30_000L),
                    PlayerData("Nayef Aguerd", Position.CB, 30, "MAR", 82, 82, 90_000L),
                    PlayerData("Igor Zubeldia", Position.CB, 29, "ESP", 81, 82, 80_000L),
                    PlayerData("Jon Pacheco", Position.CB, 25, "ESP", 78, 82, 50_000L),
                    PlayerData("Jon Aramburu", Position.RB, 24, "VEN", 79, 83, 45_000L),
                    PlayerData("Javi López", Position.LB, 24, "ESP", 78, 82, 45_000L),
                    PlayerData("Sergio Gómez", Position.LB, 25, "ESP", 80, 84, 75_000L),
                    PlayerData("Martín Zubimendi", Position.DM, 27, "ESP", 86, 89, 140_000L),
                    PlayerData("Brais Méndez", Position.CM, 29, "ESP", 82, 83, 90_000L),
                    PlayerData("Luka Sučić", Position.CM, 23, "CRO", 80, 86, 65_000L),
                    PlayerData("Beñat Turrientes", Position.CM, 24, "ESP", 78, 82, 45_000L),
                    PlayerData("Arsen Zakharyan", Position.AM, 23, "RUS", 78, 83, 50_000L),
                    PlayerData("Takefusa Kubo", Position.RW, 25, "JPN", 84, 88, 120_000L),
                    PlayerData("Mikel Oyarzabal", Position.LW, 29, "ESP", 84, 85, 130_000L),
                    PlayerData("Orri Óskarsson", Position.ST, 21, "ISL", 78, 86, 50_000L),
                    PlayerData("Ander Barrenetxea", Position.LW, 24, "ESP", 79, 83, 55_000L),
                    PlayerData("Umar Sadiq", Position.ST, 29, "NGA", 77, 78, 60_000L),
                ),
            ),
            ClubData(
                name = "Real Betis",
                shortName = "BET",
                formation = Formation.FOUR_TWO_THREE_ONE,
                mentality = Mentality.BALANCED,
                balance = 75_000_000L,
                transferBudget = 35_000_000L,
                wageBudget = 2_000_000L,
                trainingLevel = 3,
                stadiumCapacity = 60_000,
                players = listOf(
                    PlayerData("Rui Silva", Position.GK, 32, "POR", 81, 81, 75_000L),
                    PlayerData("Adrián", Position.GK, 39, "ESP", 75, 75, 40_000L),
                    PlayerData("Diego Llorente", Position.CB, 33, "ESP", 80, 80, 80_000L),
                    PlayerData("Marc Bartra", Position.CB, 35, "ESP", 78, 78, 70_000L),
                    PlayerData("Natan", Position.CB, 25, "BRA", 78, 82, 50_000L),
                    PlayerData("Héctor Bellerín", Position.RB, 31, "ESP", 78, 78, 70_000L),
                    PlayerData("Romain Perraud", Position.LB, 28, "FRA", 77, 78, 55_000L),
                    PlayerData("Youssouf Sabaly", Position.RB, 33, "SEN", 77, 77, 60_000L),
                    PlayerData("Johnny Cardoso", Position.DM, 24, "USA", 80, 86, 65_000L),
                    PlayerData("Marc Roca", Position.DM, 29, "ESP", 79, 80, 70_000L),
                    PlayerData("Sergi Altimira", Position.CM, 24, "ESP", 77, 82, 45_000L),
                    PlayerData("Giovani Lo Celso", Position.AM, 30, "ARG", 82, 82, 100_000L),
                    PlayerData("Pablo Fornals", Position.AM, 30, "ESP", 80, 80, 85_000L),
                    PlayerData("Isco", Position.AM, 34, "ESP", 83, 83, 110_000L),
                    PlayerData("Ez Abde", Position.LW, 24, "MAR", 79, 85, 60_000L),
                    PlayerData("Vitor Roque", Position.ST, 21, "BRA", 79, 88, 75_000L),
                    PlayerData("Chimy Ávila", Position.ST, 32, "ARG", 78, 78, 65_000L),
                    PlayerData("Cédric Bakambu", Position.ST, 35, "COD", 76, 76, 60_000L),
                ),
            ),
            ClubData(
                name = "Villarreal CF",
                shortName = "VIL",
                formation = Formation.FOUR_FOUR_TWO,
                mentality = Mentality.ATTACKING,
                balance = 80_000_000L,
                transferBudget = 38_000_000L,
                wageBudget = 2_000_000L,
                trainingLevel = 4,
                stadiumCapacity = 23_000,
                players = listOf(
                    PlayerData("Diego Conde", Position.GK, 27, "ESP", 80, 83, 60_000L),
                    PlayerData("Luiz Júnior", Position.GK, 25, "BRA", 78, 82, 45_000L),
                    PlayerData("Logan Costa", Position.CB, 25, "CPV", 80, 84, 60_000L),
                    PlayerData("Raúl Albiol", Position.CB, 40, "ESP", 77, 77, 65_000L),
                    PlayerData("Willy Kambwala", Position.CB, 22, "FRA", 77, 84, 50_000L),
                    PlayerData("Juan Foyth", Position.RB, 28, "ARG", 80, 81, 80_000L),
                    PlayerData("Sergi Cardona", Position.LB, 27, "ESP", 78, 80, 55_000L),
                    PlayerData("Kiko Femenía", Position.RB, 35, "ESP", 76, 76, 50_000L),
                    PlayerData("Dani Parejo", Position.CM, 37, "ESP", 81, 81, 95_000L),
                    PlayerData("Álex Baena", Position.AM, 25, "ESP", 84, 89, 110_000L),
                    PlayerData("Santi Comesaña", Position.CM, 29, "ESP", 79, 80, 65_000L),
                    PlayerData("Pape Gueye", Position.DM, 27, "SEN", 79, 81, 65_000L),
                    PlayerData("Yeremy Pino", Position.RW, 23, "ESP", 80, 86, 75_000L),
                    PlayerData("Nicolas Pépé", Position.RW, 31, "CIV", 80, 80, 85_000L),
                    PlayerData("Gerard Moreno", Position.ST, 34, "ESP", 82, 82, 115_000L),
                    PlayerData("Ayoze Pérez", Position.ST, 33, "ESP", 81, 81, 90_000L),
                    PlayerData("Thierno Barry", Position.ST, 23, "FRA", 78, 85, 55_000L),
                    PlayerData("Denis Suárez", Position.AM, 32, "ESP", 77, 77, 60_000L),
                ),
            ),
            ClubData(
                name = "Sevilla FC",
                shortName = "SEV",
                formation = Formation.FOUR_THREE_THREE,
                mentality = Mentality.BALANCED,
                balance = 70_000_000L,
                transferBudget = 30_000_000L,
                wageBudget = 1_900_000L,
                trainingLevel = 3,
                stadiumCapacity = 43_000,
                players = listOf(
                    PlayerData("Ørjan Nyland", Position.GK, 35, "NOR", 80, 80, 65_000L),
                    PlayerData("Álvaro Fernández", Position.GK, 28, "ESP", 76, 78, 40_000L),
                    PlayerData("Loïc Badé", Position.CB, 26, "FRA", 82, 85, 85_000L),
                    PlayerData("Marcão", Position.CB, 30, "BRA", 77, 77, 65_000L),
                    PlayerData("Kike Salas", Position.CB, 24, "ESP", 76, 80, 40_000L),
                    PlayerData("José Ángel Carmona", Position.RB, 24, "ESP", 78, 82, 50_000L),
                    PlayerData("Adrià Pedrosa", Position.LB, 28, "ESP", 77, 78, 55_000L),
                    PlayerData("Juanlu Sánchez", Position.RB, 23, "ESP", 78, 84, 50_000L),
                    PlayerData("Nemanja Gudelj", Position.DM, 34, "SRB", 78, 78, 70_000L),
                    PlayerData("Albert Sambi Lokonga", Position.CM, 26, "BEL", 78, 81, 65_000L),
                    PlayerData("Djibril Sow", Position.CM, 29, "SUI", 78, 79, 70_000L),
                    PlayerData("Saúl Ñíguez", Position.CM, 31, "ESP", 79, 79, 90_000L),
                    PlayerData("Gerard Fernández Peque", Position.AM, 23, "ESP", 77, 83, 45_000L),
                    PlayerData("Dodi Lukébakio", Position.RW, 28, "BEL", 81, 82, 80_000L),
                    PlayerData("Isaac Romero", Position.ST, 26, "ESP", 79, 83, 60_000L),
                    PlayerData("Kelechi Iheanacho", Position.ST, 29, "NGA", 77, 77, 65_000L),
                    PlayerData("Suso", Position.RW, 32, "ESP", 78, 78, 75_000L),
                    PlayerData("Chidera Ejuke", Position.LW, 28, "NGA", 77, 78, 55_000L),
                ),
            ),
            ClubData(
                name = "Valencia CF",
                shortName = "VAL",
                formation = Formation.FOUR_FOUR_TWO,
                mentality = Mentality.DEFENSIVE,
                balance = 60_000_000L,
                transferBudget = 25_000_000L,
                wageBudget = 1_700_000L,
                trainingLevel = 3,
                stadiumCapacity = 49_000,
                players = listOf(
                    PlayerData("Giorgi Mamardashvili", Position.GK, 25, "GEO", 85, 89, 95_000L),
                    PlayerData("Jaume Doménech", Position.GK, 35, "ESP", 75, 75, 40_000L),
                    PlayerData("Cristhian Mosquera", Position.CB, 22, "ESP", 80, 87, 60_000L),
                    PlayerData("César Tárrega", Position.CB, 24, "ESP", 77, 82, 45_000L),
                    PlayerData("Yarek Gasiorowski", Position.CB, 21, "ESP", 76, 85, 40_000L),
                    PlayerData("Thierry Correia", Position.RB, 27, "POR", 78, 80, 60_000L),
                    PlayerData("José Luis Gayà", Position.LB, 31, "ESP", 82, 82, 110_000L),
                    PlayerData("Dimitri Foulquier", Position.RB, 33, "GLP", 76, 76, 50_000L),
                    PlayerData("Pepelu", Position.DM, 28, "ESP", 81, 82, 75_000L),
                    PlayerData("Javi Guerra", Position.CM, 23, "ESP", 80, 87, 65_000L),
                    PlayerData("Enzo Barrenechea", Position.DM, 25, "ARG", 78, 82, 55_000L),
                    PlayerData("André Almeida", Position.AM, 26, "POR", 78, 81, 60_000L),
                    PlayerData("Hugo Guillamón", Position.DM, 26, "ESP", 77, 80, 55_000L),
                    PlayerData("Diego López", Position.RW, 24, "ESP", 79, 83, 55_000L),
                    PlayerData("Luis Rioja", Position.LW, 32, "ESP", 78, 78, 60_000L),
                    PlayerData("Sergi Canós", Position.RW, 29, "ESP", 76, 77, 50_000L),
                    PlayerData("Hugo Duro", Position.ST, 26, "ESP", 80, 82, 75_000L),
                    PlayerData("Dani Gómez", Position.ST, 28, "ESP", 76, 77, 45_000L),
                ),
            ),
            ClubData(
                name = "Girona FC",
                shortName = "GIR",
                formation = Formation.FOUR_TWO_THREE_ONE,
                mentality = Mentality.ATTACKING,
                balance = 75_000_000L,
                transferBudget = 35_000_000L,
                wageBudget = 1_900_000L,
                trainingLevel = 3,
                stadiumCapacity = 14_000,
                players = listOf(
                    PlayerData("Paulo Gazzaniga", Position.GK, 34, "ARG", 80, 80, 65_000L),
                    PlayerData("Juan Carlos", Position.GK, 38, "ESP", 74, 74, 35_000L),
                    PlayerData("Daley Blind", Position.CB, 36, "NED", 80, 80, 80_000L),
                    PlayerData("David López", Position.CB, 36, "ESP", 79, 79, 70_000L),
                    PlayerData("Alejandro Francés", Position.CB, 24, "ESP", 78, 83, 50_000L),
                    PlayerData("Ladislav Krejčí", Position.CB, 27, "CZE", 79, 82, 60_000L),
                    PlayerData("Miguel Gutiérrez", Position.LB, 25, "ESP", 82, 87, 85_000L),
                    PlayerData("Arnau Martínez", Position.RB, 23, "ESP", 79, 85, 60_000L),
                    PlayerData("Yangel Herrera", Position.CM, 28, "VEN", 80, 82, 75_000L),
                    PlayerData("Iván Martín", Position.CM, 27, "ESP", 80, 82, 70_000L),
                    PlayerData("Oriol Romeu", Position.DM, 34, "ESP", 78, 78, 65_000L),
                    PlayerData("Donny van de Beek", Position.CM, 29, "NED", 78, 79, 75_000L),
                    PlayerData("Yáser Asprilla", Position.AM, 22, "COL", 79, 86, 55_000L),
                    PlayerData("Viktor Tsygankov", Position.RW, 28, "UKR", 83, 84, 95_000L),
                    PlayerData("Bryan Gil", Position.LW, 25, "ESP", 80, 84, 70_000L),
                    PlayerData("Arnaut Danjuma", Position.LW, 29, "NED", 79, 80, 75_000L),
                    PlayerData("Bojan Miovski", Position.ST, 27, "MKD", 79, 81, 65_000L),
                    PlayerData("Abel Ruiz", Position.ST, 26, "ESP", 78, 81, 60_000L),
                ),
            ),
        )
    }

    fun game(): Game {
        var nextPlayerId = 1L
        val clubs = linkedMapOf<Long, Club>()
        val players = linkedMapOf<Long, Player>()

        for ((index, spec) in clubsData.withIndex()) {
            val clubId = (index + 1).toLong()
            val playerIds = spec.players.map { pData ->
                val playerId = nextPlayerId++
                val weights = PositionWeights.weights.getValue(pData.position)
                val attrs = Attribute.entries.associateWith { attr ->
                    val weight = weights[attr] ?: 0
                    (pData.rating + (weight - 2) * 4).coerceIn(MIN_ATTRIBUTE, MAX_ATTRIBUTE)
                }
                players[playerId] = Player(
                    id = playerId,
                    name = pData.name,
                    age = pData.age,
                    nationality = pData.nationality,
                    naturalPositions = listOf(pData.position),
                    attributes = PlayerAttributes(attrs),
                    contract = Contract(weeklyWage = pData.wage, expiresOn = LocalDate.of(2030, 6, 30)),
                    potential = pData.potential,
                    form = 60,
                )
                playerId
            }

            clubs[clubId] = Club(
                id = clubId,
                name = spec.name,
                shortName = spec.shortName,
                leagueId = LEAGUE_ID,
                finance = Finance(
                    balance = spec.balance,
                    transferBudget = spec.transferBudget,
                    weeklyWageBudget = spec.wageBudget,
                ),
                facilities = Facilities(
                    trainingLevel = spec.trainingLevel,
                    stadiumCapacity = spec.stadiumCapacity,
                ),
                squad = Squad(clubId, playerIds),
                defaultTactics = Tactics(spec.formation, spec.mentality),
            )
        }

        val league = League(LEAGUE_ID, LEAGUE_NAME, clubs.keys.toList())
        return Game(
            name = "LaLiga EA Sports 2026/27",
            currentDate = START_DATE,
            clubs = clubs,
            players = players,
            competitions = mapOf(LEAGUE_ID to league),
        )
    }

    fun teams(game: Game): List<Team> =
        game.clubs.values.sortedBy { it.id }.map { club ->
            Team.fromSquad(club.id, game.squad(club.id), club.defaultTactics ?: Tactics())
        }
}
