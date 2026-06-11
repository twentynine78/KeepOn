package fr.twentynine.keepon.domain.usecase.app

import fr.twentynine.keepon.domain.repository.TimeoutPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/**
 * Emits whether KeepOn is actively holding the screen on, i.e. the user opted to
 * reset the timeout when the screen turns off AND the current timeout differs from
 * the default one.
 */
class GetKeepOnStatusUseCase @Inject constructor(
    private val timeoutPreferencesRepository: TimeoutPreferencesRepository,
) {
    operator fun invoke(): Flow<Boolean> =
        combine(
            timeoutPreferencesRepository.getCurrentScreenTimeoutFlow(),
            timeoutPreferencesRepository.getDefaultScreenTimeoutFlow(),
            timeoutPreferencesRepository.getResetTimeoutWhenScreenOffFlow(),
        ) { currentTimeout, defaultTimeout, resetTimeoutWhenScreenOff ->
            resetTimeoutWhenScreenOff && currentTimeout != defaultTimeout
        }
}
