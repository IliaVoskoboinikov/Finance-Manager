package soft.divan.finansemanager.account.presenter.model

sealed interface AccountUiState {
    data object Loading : AccountUiState
    data class Success(val account: AccountUiModel) : AccountUiState
    data class Error(val message: String) : AccountUiState
}