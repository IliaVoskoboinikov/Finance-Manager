package soft.divan.financemanager.feature.security.security_impl.presenter.model

sealed class CreatePinScreenState {

    data object InitialState : CreatePinScreenState()
    data class EnteringPinState(val pin: String) : CreatePinScreenState()
    data class ConfirmingPinState(val pin: String) : CreatePinScreenState()
    data object PinCreatedState : CreatePinScreenState()
    data class ErrorState(val errorMessage: String) : CreatePinScreenState()
}
