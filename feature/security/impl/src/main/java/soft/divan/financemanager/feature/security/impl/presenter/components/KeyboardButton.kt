package soft.divan.financemanager.feature.security.impl.presenter.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import soft.divan.financemanager.feature.security.impl.presenter.util.Dimens

/** Прозрачность клавиш, пока ввод запрещён (пауза после ошибок, идёт проверка). */
private const val DISABLED_ALPHA = 0.38f

@Composable
fun NumberButton(
    number: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Box(
        modifier = Modifier
            .wrapContentSize()
            .padding(
                vertical = Dimens.verticalKeyboardButtonPadding,
                horizontal = Dimens.horizontalKeyboardButtonPadding
            )
    ) {
        Box(
            modifier = Modifier
                .size(Dimens.keyBoardButtonSize)
                .clip(CircleShape)
                .clickable(enabled = enabled) { onClick() }
                .alpha(if (enabled) 1f else DISABLED_ALPHA),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                style = TextStyle(fontSize = Dimens.keyBoardButtonFontSize),
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
fun IconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Box(
        modifier = Modifier
            .wrapContentSize()
            .padding(
                vertical = Dimens.verticalKeyboardButtonPadding,
                horizontal = Dimens.horizontalKeyboardButtonPadding
            )
    ) {
        Box(
            modifier = Modifier
                .size(Dimens.keyBoardButtonSize)
                .clip(CircleShape)
                .clickable(enabled = enabled) { onClick() }
                .alpha(if (enabled) 1f else DISABLED_ALPHA),
            contentAlignment = Alignment.Center
        ) {
            Image(
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
                imageVector = icon,
                contentDescription = contentDescription
            )
        }
    }
}
