package soft.divan.financemanager.feature.security.impl.presenter.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import soft.divan.financemanager.core.domain.model.DataProtectionLevel
import soft.divan.financemanager.feature.security.impl.R
import soft.divan.financemanager.uikit.theme.FinanceManagerTheme

@Preview(showBackground = true)
@Composable
fun PreviewProtectionLevelSection() {
    FinanceManagerTheme {
        ProtectionLevelSection(selected = DataProtectionLevel.DEVICE, enabled = true, onSelect = {})
    }
}

@Preview(showBackground = true, fontScale = 1.5f)
@Composable
fun PreviewPinSection() {
    FinanceManagerTheme(darkTheme = true) {
        Column {
            PinSection(
                hasPin = true,
                dataProtectedByPin = true,
                actions = PinSectionActions(onCreate = {}, onChange = {}, onDelete = {})
            )
            SettingSwitch(
                title = R.string.secure_screen_title,
                description = R.string.secure_screen_description,
                checked = true,
                onCheckedChange = {}
            )
        }
    }
}

/** Заголовок раздела настроек. */
@Composable
fun SectionHeader(title: Int) {
    Text(
        text = stringResource(title),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 8.dp)
    )
}

/** Выбор уровня защиты данных — радиогруппа с пояснением каждого уровня. */
@Composable
fun ProtectionLevelSection(
    selected: DataProtectionLevel,
    enabled: Boolean,
    onSelect: (DataProtectionLevel) -> Unit
) {
    Column(modifier = Modifier.selectableGroup()) {
        DataProtectionLevel.entries.forEach { level ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = level == selected,
                        enabled = enabled,
                        role = Role.RadioButton,
                        onClick = { onSelect(level) }
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Клик обрабатывает вся строка — у самой кнопки обработчика нет
                RadioButton(selected = level == selected, onClick = null, enabled = enabled)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(level.title(), style = MaterialTheme.typography.bodyLarge)
                    Text(
                        text = level.description(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/** Действия раздела PIN. */
data class PinSectionActions(
    val onCreate: () -> Unit,
    val onChange: () -> Unit,
    val onDelete: () -> Unit
)

/**
 * Раздел PIN: установить, сменить, удалить.
 *
 * На уровне PIN удаление недоступно — им завёрнут ключ данных; рядом объясняется почему.
 */
@Composable
fun PinSection(
    hasPin: Boolean,
    dataProtectedByPin: Boolean,
    actions: PinSectionActions
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        if (!hasPin) {
            OutlinedButton(onClick = actions.onCreate, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.create_pin))
            }
        } else {
            OutlinedButton(onClick = actions.onChange, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.change_pin))
            }
            OutlinedButton(
                onClick = actions.onDelete,
                enabled = !dataProtectedByPin,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.delete_pin))
            }
            if (dataProtectedByPin) {
                Text(
                    text = stringResource(R.string.message_delete_not_allowed),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/** Строка-переключатель с пояснением; переключается нажатием на всю строку. */
@Composable
fun SettingSwitch(
    title: Int,
    description: Int,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(stringResource(title), style = MaterialTheme.typography.bodyLarge)
            Text(
                text = stringResource(description),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.width(16.dp))
        Switch(checked = checked, onCheckedChange = null)
    }
}

@Composable
private fun DataProtectionLevel.title(): String = stringResource(
    when (this) {
        DataProtectionLevel.NONE -> R.string.level_none_title
        DataProtectionLevel.DEVICE -> R.string.level_device_title
        DataProtectionLevel.PIN -> R.string.level_pin_title
    }
)

@Composable
private fun DataProtectionLevel.description(): String = stringResource(
    when (this) {
        DataProtectionLevel.NONE -> R.string.level_none_description
        DataProtectionLevel.DEVICE -> R.string.level_device_description
        DataProtectionLevel.PIN -> R.string.level_pin_description
    }
)
