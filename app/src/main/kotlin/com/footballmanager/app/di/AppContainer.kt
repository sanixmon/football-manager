package com.footballmanager.app.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.footballmanager.app.ui.viewmodel.GameViewModel
import com.footballmanager.model.Game
import com.footballmanager.repository.FilePlayerRepository
import com.footballmanager.repository.GameRepository
import com.footballmanager.repository.InMemoryGameRepository
import com.footballmanager.repository.JsonFileGameRepository
import com.footballmanager.repository.PlayerRepository
import com.footballmanager.seed.SeedData
import com.footballmanager.simulation.season.SeasonRunner
import com.footballmanager.usecase.CalculateTeamStrengthUseCase
import com.footballmanager.usecase.SelectLineupUseCase
import com.footballmanager.usecase.SimulateMatchUseCase
import java.io.File

interface AppContainer {
    val gameRepository: GameRepository
    val playerRepository: PlayerRepository
    val selectLineupUseCase: SelectLineupUseCase
    val calculateTeamStrengthUseCase: CalculateTeamStrengthUseCase
    val simulateMatchUseCase: SimulateMatchUseCase
    val submitTransferBidUseCase: com.footballmanager.usecase.SubmitTransferBidUseCase
    val evaluateTransferOfferUseCase: com.footballmanager.usecase.EvaluateTransferOfferUseCase
    val negotiateContractUseCase: com.footballmanager.usecase.NegotiateContractUseCase
    val completeTransferUseCase: com.footballmanager.usecase.CompleteTransferUseCase
    val seasonRunner: SeasonRunner
    val gameViewModelFactory: ViewModelProvider.Factory
}

class DefaultAppContainer(
    saveFile: File? = null,
    defaultGameProvider: () -> Game = { SeedData.game() },
) : AppContainer {

    override val gameRepository: GameRepository by lazy {
        if (saveFile != null) {
            JsonFileGameRepository(saveFile, defaultGameProvider)
        } else {
            InMemoryGameRepository(defaultGameProvider())
        }
    }

    override val playerRepository: PlayerRepository by lazy {
        FilePlayerRepository(gameRepository)
    }

    override val selectLineupUseCase: SelectLineupUseCase by lazy {
        SelectLineupUseCase()
    }

    override val calculateTeamStrengthUseCase: CalculateTeamStrengthUseCase by lazy {
        CalculateTeamStrengthUseCase(selectLineupUseCase)
    }

    override val simulateMatchUseCase: SimulateMatchUseCase by lazy {
        SimulateMatchUseCase()
    }

    override val submitTransferBidUseCase: com.footballmanager.usecase.SubmitTransferBidUseCase by lazy {
        com.footballmanager.usecase.SubmitTransferBidUseCase()
    }

    override val evaluateTransferOfferUseCase: com.footballmanager.usecase.EvaluateTransferOfferUseCase by lazy {
        com.footballmanager.usecase.EvaluateTransferOfferUseCase()
    }

    override val negotiateContractUseCase: com.footballmanager.usecase.NegotiateContractUseCase by lazy {
        com.footballmanager.usecase.NegotiateContractUseCase()
    }

    override val completeTransferUseCase: com.footballmanager.usecase.CompleteTransferUseCase by lazy {
        com.footballmanager.usecase.CompleteTransferUseCase()
    }

    override val seasonRunner: SeasonRunner by lazy {
        SeasonRunner()
    }

    override val gameViewModelFactory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
                return GameViewModel(
                    gameRepository = gameRepository,
                    runner = seasonRunner,
                    selectLineupUseCase = selectLineupUseCase,
                    submitTransferBidUseCase = submitTransferBidUseCase,
                    evaluateTransferOfferUseCase = evaluateTransferOfferUseCase,
                    negotiateContractUseCase = negotiateContractUseCase,
                    completeTransferUseCase = completeTransferUseCase,
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
