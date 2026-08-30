package com.footballmanager.model

import com.footballmanager.serialization.LocalDateSerializer
import java.time.LocalDate
import kotlinx.serialization.Serializable

@Serializable
enum class TransferListingStatus {
    NONE,
    TRANSFER_LISTED,
    LOAN_LISTED,
    NOT_FOR_SALE,
}

@Serializable
enum class BidStatus {
    PENDING,
    ACCEPTED_BY_CLUB,
    REJECTED_BY_CLUB,
    TERMS_OFFERED,
    ACCEPTED_BY_PLAYER,
    REJECTED_BY_PLAYER,
    COMPLETED,
    CANCELLED,
}

@Serializable
data class ContractOffer(
    val weeklyWage: Long,
    val contractYears: Int = 3,
    val squadStatus: SquadStatus = SquadStatus.ROTATION,
    val signingBonus: Long = 0L,
)

@Serializable
data class TransferBid(
    val id: Long,
    val playerId: Long,
    val buyingClubId: Long,
    val sellingClubId: Long? = null,
    val feeOffered: Long,
    val status: BidStatus = BidStatus.PENDING,
    @Serializable(with = LocalDateSerializer::class)
    val dateSubmitted: LocalDate,
    val contractOffer: ContractOffer? = null,
)

@Serializable
data class TransferRecord(
    val id: Long,
    val playerId: Long,
    val playerName: String,
    val fromClubId: Long?,
    val toClubId: Long,
    val fee: Long,
    @Serializable(with = LocalDateSerializer::class)
    val date: LocalDate,
)
