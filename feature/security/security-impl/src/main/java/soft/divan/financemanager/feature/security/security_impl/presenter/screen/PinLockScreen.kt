package soft.divan.financemanager.feature.security.security_impl.presenter.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.outlined.Backspace
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

const val pinSize = 4
const val password = "1234" //sample password


@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun PreviewPinLockScreen() {
    PinLockScreen({})
}

@Composable
fun PinLockScreen(onPinCorrect: () -> Unit) {
    val inputPin = remember { mutableStateListOf<Int>() }
    val error = remember { mutableStateOf<String>("") }
    val showSuccess = remember { mutableStateOf(false) }
    val context = LocalContext.current

    if (inputPin.size == 4) {
        LaunchedEffect(true) {
            delay(300)

            if (inputPin.joinToString("") == password) {
                showSuccess.value = true
                onPinCorrect()
                error.value = ""
            } else {
                inputPin.clear()
                error.value = "Wrong pin, Please retry!"
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(50.dp))

                /*Image(
                    painter = painterResource(id = R.drawable.makeiteasy),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                )*/

                Text(
                    text = "Enter pin to unlock",
                    modifier = Modifier.padding(16.dp),
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(30.dp))

                if (showSuccess.value) {
                    /* LottieLoadingView(
                         context = context,
                         file = "success.json",
                         iterations = 1,
                         modifier = Modifier.size(100.dp)
                     )*/
                } else {
                    Row {
                        (0 until pinSize).forEach {
                            Icon(
                                imageVector = if (inputPin.size > it) Icons.Default.Circle else Icons.Outlined.Circle,
                                contentDescription = it.toString(),
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(30.dp),
                                tint = Color.Black
                            )
                        }
                    }
                }
                Text(
                    text = error.value,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )

                Spacer(modifier = Modifier.height(50.dp))
            }

            Column(
                modifier = Modifier
                    .wrapContentSize()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    (1..3).forEach {
                        PinKeyItem(
                            onClick = { inputPin.add(it) }
                        ) {
                            Text(
                                text = it.toString(),
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    (4..6).forEach {
                        PinKeyItem(
                            onClick = { inputPin.add(it) }
                        ) {
                            Text(
                                text = it.toString(),
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    (7..9).forEach {
                        PinKeyItem(
                            onClick = { inputPin.add(it) }
                        ) {
                            Text(
                                text = it.toString(),

                                )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 10.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Success",
                        modifier = Modifier
                            .size(25.dp)
                            .clickable { }
                    )
                    PinKeyItem(
                        onClick = { inputPin.add(0) },
                        modifier = Modifier.padding(
                            horizontal = 16.dp,
                            vertical = 8.dp
                        )
                    ) {
                        Text(
                            text = "0",

                            )
                    }
                    Icon(
                        imageVector = Icons.Outlined.Backspace,
                        contentDescription = "Clear",
                        modifier = Modifier
                            .size(25.dp)
                            .clickable {
                                if (inputPin.isNotEmpty()) {
                                    inputPin.removeAt(inputPin.lastIndex)
                                }
                            }
                    )
                }
            }
        }
    }
}
/*
@Composable
fun LottieLoadingView(
    context: Context,
    file: String,
    modifier: Modifier = Modifier,
    iterations: Int = 10
) {
    val composition by rememberLottieComposition(spec = LottieCompositionSpec.Asset(file))
    LottieAnimation(
        composition = composition,
        modifier = modifier.defaultMinSize(300.dp),
        iterations = iterations
    )
}*/

@Composable
fun PinKeyItem(
    onClick: () -> Unit,
    modifier: Modifier = Modifier.padding(8.dp),
    shape: Shape = MaterialTheme.shapes.small.copy(CornerSize(percent = 50)),
    backgroundColor: Color = MaterialTheme.colorScheme.onPrimary,
    contentColor: Color = contentColorFor(backgroundColor = backgroundColor),
    elevation: Dp = 4.dp,
    content: @Composable () -> Unit
) {

    Surface(
        modifier = modifier
            .clickable(
                onClick = onClick,
                role = Role.Button,
                indication = rememberRipple(),
                interactionSource = remember { MutableInteractionSource() }
            ),
        shape = shape,
        color = backgroundColor,
        contentColor = contentColor,
        tonalElevation = elevation,
    ) {

        Box(
            modifier = Modifier
                .defaultMinSize(minWidth = 64.dp, minHeight = 64.dp)
                .alpha(contentColor.alpha),
            contentAlignment = Alignment.Center
        ) {

            ProvideTextStyle(MaterialTheme.typography.displayMedium) {
                content()
            }

        }
    }

}





