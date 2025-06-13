package soft.divan.financemanager.presenter.ui.icons

import androidx.compose.material.icons.Icons
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Icons.Filled.Home: ImageVector
    get() {
        if (_Home != null) {
            return _Home!!
        }
        _Home = ImageVector.Builder(
            name = "Filled.Home",
            defaultWidth = 19.dp,
            defaultHeight = 19.dp,
            viewportWidth = 19f,
            viewportHeight = 19f
        ).apply {
            path(fill = SolidColor(Color(0xFF795548))) {
                moveTo(7.668f, 17.979f)
                horizontalLineTo(3.979f)
                verticalLineTo(12.293f)
                curveTo(3.979f, 11.708f, 4.458f, 11.229f, 5.043f, 11.229f)
                horizontalLineTo(6.603f)
                curveTo(7.188f, 11.229f, 7.666f, 11.708f, 7.666f, 12.293f)
                verticalLineTo(17.979f)
                horizontalLineTo(7.668f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF424242)),
                fillAlpha = 0.2f,
                strokeAlpha = 0.2f
            ) {
                moveTo(6.605f, 11.679f)
                curveTo(6.944f, 11.679f, 7.218f, 11.955f, 7.218f, 12.293f)
                verticalLineTo(17.529f)
                horizontalLineTo(4.43f)
                verticalLineTo(12.293f)
                curveTo(4.43f, 11.954f, 4.706f, 11.679f, 5.043f, 11.679f)
                horizontalLineTo(6.605f)
                close()
                moveTo(6.605f, 11.229f)
                horizontalLineTo(5.045f)
                curveTo(4.46f, 11.229f, 3.981f, 11.708f, 3.981f, 12.293f)
                verticalLineTo(17.979f)
                horizontalLineTo(7.668f)
                verticalLineTo(12.293f)
                curveTo(7.668f, 11.708f, 7.19f, 11.229f, 6.605f, 11.229f)
                close()
            }
            path(
                fill = Brush.radialGradient(
                    colorStops = arrayOf(
                        0f to Color(0xFF8BC34A),
                        0.789f to Color(0xFF558B2F),
                        1f to Color(0xFF558B2F)
                    ),
                    center = Offset(3.881f, 4.723f),
                    radius = 7.412f
                )
            ) {
                moveTo(0.837f, 8.754f)
                curveTo(0.933f, 6.647f, 4.343f, 0.833f, 4.343f, 0.833f)
                curveTo(4.526f, 0.294f, 5.537f, 0.294f, 5.72f, 0.833f)
                curveTo(5.72f, 0.833f, 9.029f, 6.699f, 9.021f, 8.801f)
                curveTo(9.015f, 10.874f, 8.511f, 13.749f, 5.031f, 13.749f)
                curveTo(1.449f, 13.749f, 0.741f, 10.824f, 0.837f, 8.754f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF424242)),
                fillAlpha = 0.2f,
                strokeAlpha = 0.2f
            ) {
                moveTo(5.031f, 0.911f)
                curveTo(5.174f, 0.911f, 5.256f, 0.966f, 5.264f, 0.987f)
                lineTo(5.279f, 1.029f)
                lineTo(5.301f, 1.068f)
                curveTo(6.203f, 2.67f, 8.544f, 7.179f, 8.54f, 8.799f)
                curveTo(8.531f, 11.847f, 7.415f, 13.268f, 5.031f, 13.268f)
                curveTo(3.879f, 13.268f, 2.981f, 12.938f, 2.36f, 12.288f)
                curveTo(1.361f, 11.243f, 1.278f, 9.639f, 1.317f, 8.775f)
                curveTo(1.392f, 7.146f, 3.827f, 2.664f, 4.758f, 1.076f)
                lineTo(4.782f, 1.034f)
                lineTo(4.797f, 0.987f)
                curveTo(4.805f, 0.966f, 4.887f, 0.911f, 5.031f, 0.911f)
                close()
                moveTo(5.031f, 0.429f)
                curveTo(4.733f, 0.429f, 4.434f, 0.564f, 4.343f, 0.833f)
                curveTo(4.343f, 0.833f, 0.933f, 6.647f, 0.837f, 8.754f)
                curveTo(0.741f, 10.824f, 1.449f, 13.749f, 5.031f, 13.749f)
                curveTo(8.511f, 13.749f, 9.015f, 10.874f, 9.021f, 8.801f)
                curveTo(9.027f, 6.699f, 5.72f, 0.833f, 5.72f, 0.833f)
                curveTo(5.628f, 0.564f, 5.33f, 0.429f, 5.031f, 0.429f)
                close()
            }
            path(
                fill = Brush.radialGradient(
                    colorStops = arrayOf(
                        0f to Color(0xFF9CCC65),
                        0.3f to Color(0xFF96C660),
                        0.383f to Color(0xFF93C35E),
                        0.487f to Color(0xFF86B854),
                        0.818f to Color(0xFF639739),
                        0.995f to Color(0xFF558B2F)
                    ),
                    center = Offset(2.476f, 14.631f),
                    radius = 3.054f
                )
            ) {
                moveTo(0.92f, 17.344f)
                curveTo(0.915f, 17.169f, 0.924f, 16.861f, 1.016f, 16.549f)
                curveTo(1.124f, 16.182f, 1.362f, 15.717f, 1.878f, 15.591f)
                curveTo(1.962f, 15.57f, 2.048f, 15.559f, 2.132f, 15.559f)
                curveTo(2.199f, 15.559f, 2.268f, 15.565f, 2.336f, 15.579f)
                curveTo(2.345f, 15.58f, 2.355f, 15.582f, 2.364f, 15.582f)
                curveTo(2.43f, 15.582f, 2.489f, 15.538f, 2.508f, 15.474f)
                curveTo(2.643f, 15.012f, 3.072f, 14.689f, 3.554f, 14.689f)
                curveTo(3.933f, 14.689f, 4.278f, 14.881f, 4.479f, 15.204f)
                curveTo(4.508f, 15.249f, 4.556f, 15.274f, 4.607f, 15.274f)
                curveTo(4.619f, 15.274f, 4.631f, 15.273f, 4.643f, 15.27f)
                curveTo(4.728f, 15.249f, 4.815f, 15.238f, 4.902f, 15.238f)
                curveTo(4.968f, 15.238f, 5.034f, 15.244f, 5.099f, 15.256f)
                curveTo(5.574f, 15.342f, 5.937f, 15.727f, 5.984f, 16.195f)
                curveTo(5.999f, 16.345f, 5.97f, 16.492f, 5.904f, 16.611f)
                curveTo(5.871f, 16.669f, 5.877f, 16.75f, 5.925f, 16.798f)
                curveTo(6.033f, 16.906f, 6.038f, 17.086f, 5.987f, 17.208f)
                curveTo(5.97f, 17.248f, 5.921f, 17.343f, 5.834f, 17.343f)
                horizontalLineTo(0.92f)
                verticalLineTo(17.344f)
                close()
            }
            path(fill = SolidColor(Color(0xFF689F38))) {
                moveTo(3.554f, 14.841f)
                curveTo(3.881f, 14.841f, 4.179f, 15.007f, 4.352f, 15.285f)
                curveTo(4.407f, 15.375f, 4.505f, 15.426f, 4.607f, 15.426f)
                curveTo(4.631f, 15.426f, 4.655f, 15.423f, 4.679f, 15.417f)
                curveTo(4.752f, 15.399f, 4.827f, 15.39f, 4.902f, 15.39f)
                curveTo(4.959f, 15.39f, 5.016f, 15.394f, 5.073f, 15.405f)
                curveTo(5.475f, 15.477f, 5.796f, 15.816f, 5.835f, 16.211f)
                curveTo(5.847f, 16.33f, 5.825f, 16.446f, 5.774f, 16.538f)
                curveTo(5.688f, 16.69f, 5.745f, 16.83f, 5.819f, 16.905f)
                curveTo(5.855f, 16.941f, 5.873f, 17.005f, 5.867f, 17.074f)
                curveTo(5.861f, 17.139f, 5.835f, 17.181f, 5.823f, 17.194f)
                horizontalLineTo(1.07f)
                curveTo(1.074f, 17.029f, 1.095f, 16.81f, 1.16f, 16.591f)
                curveTo(1.301f, 16.111f, 1.556f, 15.823f, 1.914f, 15.736f)
                curveTo(1.986f, 15.719f, 2.06f, 15.71f, 2.133f, 15.71f)
                curveTo(2.192f, 15.71f, 2.25f, 15.715f, 2.309f, 15.726f)
                curveTo(2.328f, 15.729f, 2.346f, 15.732f, 2.364f, 15.732f)
                curveTo(2.495f, 15.732f, 2.615f, 15.646f, 2.652f, 15.516f)
                curveTo(2.768f, 15.118f, 3.138f, 14.841f, 3.554f, 14.841f)
                close()
                moveTo(3.554f, 14.541f)
                curveTo(2.99f, 14.541f, 2.514f, 14.917f, 2.364f, 15.434f)
                curveTo(2.289f, 15.42f, 2.211f, 15.411f, 2.132f, 15.411f)
                curveTo(2.037f, 15.411f, 1.941f, 15.422f, 1.842f, 15.446f)
                curveTo(1.325f, 15.571f, 1.022f, 15.996f, 0.872f, 16.507f)
                curveTo(0.725f, 17.005f, 0.779f, 17.491f, 0.779f, 17.496f)
                horizontalLineTo(5.835f)
                curveTo(6.15f, 17.496f, 6.296f, 16.959f, 6.032f, 16.695f)
                curveTo(6.03f, 16.694f, 6.165f, 16.499f, 6.134f, 16.184f)
                curveTo(6.081f, 15.646f, 5.657f, 15.208f, 5.127f, 15.113f)
                curveTo(5.051f, 15.099f, 4.977f, 15.093f, 4.904f, 15.093f)
                curveTo(4.802f, 15.093f, 4.703f, 15.105f, 4.608f, 15.129f)
                curveTo(4.388f, 14.775f, 3.998f, 14.541f, 3.554f, 14.541f)
                close()
            }
            path(
                stroke = SolidColor(Color(0xFF689F38)),
                strokeLineWidth = 0.299991f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(4.772f, 15.934f)
                curveTo(4.772f, 15.934f, 4.784f, 15.595f, 4.509f, 15.231f)
            }
            path(
                stroke = SolidColor(Color(0xFF689F38)),
                strokeLineWidth = 0.299991f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(2.384f, 15.575f)
                curveTo(2.705f, 15.58f, 3.018f, 15.831f, 3.065f, 16.14f)
            }
            path(
                stroke = SolidColor(Color(0xFF689F38)),
                strokeLineWidth = 0.299991f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(2.714f, 16.282f)
                curveTo(2.996f, 16.155f, 3.375f, 16.174f, 3.666f, 16.323f)
            }
            path(
                fill = Brush.radialGradient(
                    colorStops = arrayOf(
                        0f to Color(0xFFFFD54F),
                        0.179f to Color(0xFFFFD34B),
                        0.353f to Color(0xFFFFCE3F),
                        0.524f to Color(0xFFFFC62C),
                        0.693f to Color(0xFFFFBA11),
                        0.778f to Color(0xFFFFB300),
                        1f to Color(0xFFFFB300)
                    ),
                    center = Offset(11.273f, 3.176f),
                    radius = 16.038f
                )
            ) {
                moveTo(11.273f, 3.129f)
                lineTo(4.277f, 9.126f)
                horizontalLineTo(4.776f)
                verticalLineTo(17.971f)
                horizontalLineTo(17.769f)
                verticalLineTo(9.126f)
                horizontalLineTo(18.268f)
                lineTo(11.273f, 3.129f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF424242)),
                fillAlpha = 0.2f,
                strokeAlpha = 0.2f
            ) {
                moveTo(11.272f, 3.722f)
                lineTo(17.359f, 8.939f)
                curveTo(17.333f, 8.996f, 17.319f, 9.059f, 17.319f, 9.125f)
                verticalLineTo(17.52f)
                horizontalLineTo(5.226f)
                verticalLineTo(9.126f)
                curveTo(5.226f, 9.06f, 5.211f, 8.996f, 5.185f, 8.94f)
                lineTo(11.272f, 3.722f)
                close()
                moveTo(11.272f, 3.129f)
                lineTo(4.276f, 9.126f)
                horizontalLineTo(4.776f)
                verticalLineTo(17.971f)
                horizontalLineTo(17.768f)
                verticalLineTo(9.126f)
                horizontalLineTo(18.268f)
                lineTo(11.272f, 3.129f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF424242)),
                fillAlpha = 0.2f,
                strokeAlpha = 0.2f
            ) {
                moveTo(12.396f, 5.879f)
                curveTo(12.534f, 5.879f, 12.646f, 5.991f, 12.646f, 6.129f)
                verticalLineTo(8.378f)
                curveTo(12.646f, 8.516f, 12.534f, 8.628f, 12.396f, 8.628f)
                horizontalLineTo(10.147f)
                curveTo(10.009f, 8.628f, 9.897f, 8.516f, 9.897f, 8.378f)
                verticalLineTo(6.128f)
                curveTo(9.897f, 5.99f, 10.009f, 5.877f, 10.147f, 5.877f)
                horizontalLineTo(12.396f)
                moveTo(12.396f, 5.579f)
                horizontalLineTo(10.147f)
                curveTo(9.844f, 5.579f, 9.597f, 5.825f, 9.597f, 6.129f)
                verticalLineTo(8.378f)
                curveTo(9.597f, 8.681f, 9.843f, 8.928f, 10.147f, 8.928f)
                horizontalLineTo(12.396f)
                curveTo(12.699f, 8.928f, 12.946f, 8.682f, 12.946f, 8.378f)
                verticalLineTo(6.128f)
                curveTo(12.946f, 5.825f, 12.7f, 5.579f, 12.396f, 5.579f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF424242)),
                fillAlpha = 0.2f,
                strokeAlpha = 0.2f
            ) {
                moveTo(15.52f, 10.125f)
                curveTo(15.658f, 10.125f, 15.771f, 10.238f, 15.771f, 10.376f)
                verticalLineTo(13.874f)
                curveTo(15.771f, 14.012f, 15.658f, 14.124f, 15.52f, 14.124f)
                horizontalLineTo(13.27f)
                curveTo(13.132f, 14.124f, 13.02f, 14.012f, 13.02f, 13.874f)
                verticalLineTo(10.376f)
                curveTo(13.02f, 10.238f, 13.132f, 10.125f, 13.27f, 10.125f)
                horizontalLineTo(15.52f)
                close()
                moveTo(15.52f, 9.825f)
                horizontalLineTo(13.27f)
                curveTo(12.967f, 9.825f, 12.72f, 10.071f, 12.72f, 10.376f)
                verticalLineTo(13.874f)
                curveTo(12.72f, 14.177f, 12.966f, 14.424f, 13.27f, 14.424f)
                horizontalLineTo(15.519f)
                curveTo(15.822f, 14.424f, 16.069f, 14.178f, 16.069f, 13.874f)
                verticalLineTo(10.376f)
                curveTo(16.069f, 10.073f, 15.823f, 9.825f, 15.52f, 9.825f)
                close()
            }
            path(
                fill = Brush.radialGradient(
                    colorStops = arrayOf(
                        0f to Color(0xFFD32F2F),
                        0.437f to Color(0xFFCA2929),
                        0.996f to Color(0xFFB71C1C)
                    ),
                    center = Offset(8.945f, 9.595f),
                    radius = 6.559f
                )
            ) {
                moveTo(11.272f, 16.122f)
                horizontalLineTo(6.775f)
                verticalLineTo(9.876f)
                curveTo(6.775f, 9.738f, 6.888f, 9.626f, 7.026f, 9.626f)
                horizontalLineTo(11.023f)
                curveTo(11.161f, 9.626f, 11.274f, 9.738f, 11.274f, 9.876f)
                verticalLineTo(16.122f)
                horizontalLineTo(11.272f)
                close()
            }
            path(
                fill = Brush.radialGradient(
                    colorStops = arrayOf(
                        0f to Color(0xFFEF5350),
                        1f to Color(0xFFE53935)
                    ),
                    center = Offset(9.008f, 9.938f),
                    radius = 5.888f
                )
            ) {
                moveTo(10.773f, 15.819f)
                horizontalLineTo(7.275f)
                curveTo(7.206f, 15.819f, 7.15f, 15.764f, 7.15f, 15.694f)
                verticalLineTo(10.055f)
                curveTo(7.15f, 9.986f, 7.206f, 9.93f, 7.275f, 9.93f)
                horizontalLineTo(10.773f)
                curveTo(10.842f, 9.93f, 10.897f, 9.986f, 10.897f, 10.055f)
                verticalLineTo(15.694f)
                curveTo(10.897f, 15.764f, 10.842f, 15.819f, 10.773f, 15.819f)
                close()
            }
            path(fill = SolidColor(Color(0xFF4FC3F7))) {
                moveTo(10.086f, 6.066f)
                horizontalLineTo(12.459f)
                verticalLineTo(8.439f)
                horizontalLineTo(10.086f)
                verticalLineTo(6.066f)
                close()
            }
            path(fill = SolidColor(Color(0xFFEEEEEE))) {
                moveTo(9.899f, 6.128f)
                verticalLineTo(8.376f)
                curveTo(9.899f, 8.514f, 10.011f, 8.627f, 10.149f, 8.627f)
                horizontalLineTo(12.398f)
                curveTo(12.536f, 8.627f, 12.648f, 8.514f, 12.648f, 8.376f)
                verticalLineTo(6.128f)
                curveTo(12.648f, 5.99f, 12.536f, 5.877f, 12.398f, 5.877f)
                horizontalLineTo(10.148f)
                curveTo(10.01f, 5.879f, 9.899f, 5.99f, 9.899f, 6.128f)
                close()
                moveTo(12.396f, 7.127f)
                horizontalLineTo(11.397f)
                verticalLineTo(6.128f)
                horizontalLineTo(12.396f)
                verticalLineTo(7.127f)
                close()
                moveTo(11.148f, 6.128f)
                verticalLineTo(7.127f)
                horizontalLineTo(10.149f)
                verticalLineTo(6.128f)
                horizontalLineTo(11.148f)
                close()
                moveTo(10.148f, 7.377f)
                horizontalLineTo(11.147f)
                verticalLineTo(8.376f)
                horizontalLineTo(10.148f)
                verticalLineTo(7.377f)
                close()
                moveTo(11.397f, 8.376f)
                verticalLineTo(7.377f)
                horizontalLineTo(12.396f)
                verticalLineTo(8.376f)
                horizontalLineTo(11.397f)
                close()
            }
            path(fill = SolidColor(Color(0xFF4FC3F7))) {
                moveTo(13.15f, 10.313f)
                horizontalLineTo(15.642f)
                verticalLineTo(13.935f)
                horizontalLineTo(13.15f)
                verticalLineTo(10.313f)
                close()
            }
            path(
                fill = Brush.radialGradient(
                    colorStops = arrayOf(
                        0f to Color(0xFFE53935),
                        0.407f to Color(0xFFDC3431),
                        1f to Color(0xFFC62828)
                    ),
                    center = Offset(8.962f, 10.469f),
                    radius = 5.199f
                )
            ) {
                moveTo(10.399f, 12.373f)
                horizontalLineTo(7.651f)
                curveTo(7.582f, 12.373f, 7.526f, 12.318f, 7.526f, 12.249f)
                verticalLineTo(10.5f)
                curveTo(7.526f, 10.431f, 7.582f, 10.376f, 7.651f, 10.376f)
                horizontalLineTo(10.399f)
                curveTo(10.468f, 10.376f, 10.523f, 10.431f, 10.523f, 10.5f)
                verticalLineTo(12.249f)
                curveTo(10.523f, 12.318f, 10.468f, 12.373f, 10.399f, 12.373f)
                close()
            }
            path(
                fill = Brush.radialGradient(
                    colorStops = arrayOf(
                        0f to Color(0xFFE53935),
                        0.407f to Color(0xFFDC3431),
                        1f to Color(0xFFC62828)
                    ),
                    center = Offset(8.962f, 10.469f),
                    radius = 5.199f
                )
            ) {
                moveTo(10.399f, 15.372f)
                horizontalLineTo(7.651f)
                curveTo(7.582f, 15.372f, 7.526f, 15.316f, 7.526f, 15.247f)
                verticalLineTo(12.999f)
                curveTo(7.526f, 12.93f, 7.582f, 12.875f, 7.651f, 12.875f)
                horizontalLineTo(10.399f)
                curveTo(10.468f, 12.875f, 10.523f, 12.93f, 10.523f, 12.999f)
                verticalLineTo(15.247f)
                curveTo(10.523f, 15.316f, 10.468f, 15.372f, 10.399f, 15.372f)
                close()
            }
            path(
                fill = Brush.linearGradient(
                    colorStops = arrayOf(
                        0.004f to Color(0xFFBDBDBD),
                        0.215f to Color(0xFFB5B5B5),
                        0.55f to Color(0xFF9E9E9E),
                        0.965f to Color(0xFF787878),
                        1f to Color(0xFF757575)
                    ),
                    start = Offset(9.027f, 15.831f),
                    end = Offset(9.027f, 16.647f)
                )
            ) {
                moveTo(11.775f, 16.538f)
                horizontalLineTo(6.278f)
                verticalLineTo(16.08f)
                curveTo(6.278f, 15.909f, 6.416f, 15.771f, 6.587f, 15.771f)
                horizontalLineTo(11.465f)
                curveTo(11.636f, 15.771f, 11.774f, 15.909f, 11.774f, 16.08f)
                verticalLineTo(16.538f)
                horizontalLineTo(11.775f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF263238)),
                fillAlpha = 0.3f,
                strokeAlpha = 0.3f
            ) {
                moveTo(11.466f, 15.921f)
                curveTo(11.555f, 15.921f, 11.625f, 15.993f, 11.625f, 16.08f)
                verticalLineTo(16.388f)
                horizontalLineTo(6.428f)
                verticalLineTo(16.08f)
                curveTo(6.428f, 15.991f, 6.5f, 15.921f, 6.587f, 15.921f)
                horizontalLineTo(11.466f)
                close()
                moveTo(11.466f, 15.771f)
                horizontalLineTo(6.588f)
                curveTo(6.417f, 15.771f, 6.279f, 15.909f, 6.279f, 16.08f)
                verticalLineTo(16.538f)
                horizontalLineTo(11.777f)
                verticalLineTo(16.08f)
                curveTo(11.775f, 15.909f, 11.637f, 15.771f, 11.466f, 15.771f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF424242)),
                fillAlpha = 0.2f,
                strokeAlpha = 0.2f
            ) {
                moveTo(8.841f, 16.08f)
                curveTo(8.852f, 16.054f, 8.818f, 16.014f, 8.764f, 15.99f)
                curveTo(8.71f, 15.967f, 8.656f, 15.97f, 8.645f, 15.997f)
                curveTo(8.634f, 16.024f, 8.668f, 16.064f, 8.722f, 16.087f)
                curveTo(8.777f, 16.11f, 8.83f, 16.107f, 8.841f, 16.08f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF424242)),
                fillAlpha = 0.2f,
                strokeAlpha = 0.2f
            ) {
                moveTo(11.195f, 16.328f)
                curveTo(11.211f, 16.308f, 11.194f, 16.267f, 11.156f, 16.237f)
                curveTo(11.119f, 16.207f, 11.076f, 16.198f, 11.06f, 16.218f)
                curveTo(11.043f, 16.238f, 11.061f, 16.279f, 11.098f, 16.309f)
                curveTo(11.135f, 16.34f, 11.179f, 16.348f, 11.195f, 16.328f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF424242)),
                fillAlpha = 0.08f,
                strokeAlpha = 0.08f
            ) {
                moveTo(9.062f, 16.285f)
                curveTo(9.133f, 16.285f, 9.191f, 16.257f, 9.191f, 16.222f)
                curveTo(9.191f, 16.188f, 9.133f, 16.159f, 9.062f, 16.159f)
                curveTo(8.99f, 16.159f, 8.933f, 16.188f, 8.933f, 16.222f)
                curveTo(8.933f, 16.257f, 8.99f, 16.285f, 9.062f, 16.285f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF424242)),
                fillAlpha = 0.08f,
                strokeAlpha = 0.08f
            ) {
                moveTo(10.17f, 16.278f)
                curveTo(10.241f, 16.278f, 10.299f, 16.25f, 10.299f, 16.215f)
                curveTo(10.299f, 16.18f, 10.241f, 16.152f, 10.17f, 16.152f)
                curveTo(10.099f, 16.152f, 10.041f, 16.18f, 10.041f, 16.215f)
                curveTo(10.041f, 16.25f, 10.099f, 16.278f, 10.17f, 16.278f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF424242)),
                fillAlpha = 0.08f,
                strokeAlpha = 0.08f
            ) {
                moveTo(9.905f, 16.11f)
                curveTo(9.954f, 16.11f, 9.993f, 16.086f, 9.993f, 16.056f)
                curveTo(9.993f, 16.026f, 9.954f, 16.002f, 9.905f, 16.002f)
                curveTo(9.856f, 16.002f, 9.816f, 16.026f, 9.816f, 16.056f)
                curveTo(9.816f, 16.086f, 9.856f, 16.11f, 9.905f, 16.11f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF424242)),
                fillAlpha = 0.08f,
                strokeAlpha = 0.08f
            ) {
                moveTo(11.347f, 16.153f)
                curveTo(11.418f, 16.153f, 11.475f, 16.119f, 11.475f, 16.077f)
                curveTo(11.475f, 16.035f, 11.418f, 16f, 11.347f, 16f)
                curveTo(11.277f, 16f, 11.22f, 16.035f, 11.22f, 16.077f)
                curveTo(11.22f, 16.119f, 11.277f, 16.153f, 11.347f, 16.153f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF424242)),
                fillAlpha = 0.08f,
                strokeAlpha = 0.08f
            ) {
                moveTo(8.292f, 16.08f)
                curveTo(8.341f, 16.08f, 8.381f, 16.06f, 8.381f, 16.036f)
                curveTo(8.381f, 16.012f, 8.341f, 15.993f, 8.292f, 15.993f)
                curveTo(8.243f, 15.993f, 8.204f, 16.012f, 8.204f, 16.036f)
                curveTo(8.204f, 16.06f, 8.243f, 16.08f, 8.292f, 16.08f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF424242)),
                fillAlpha = 0.08f,
                strokeAlpha = 0.08f
            ) {
                moveTo(10.843f, 16.185f)
                curveTo(10.921f, 16.173f, 10.98f, 16.133f, 10.974f, 16.094f)
                curveTo(10.969f, 16.056f, 10.9f, 16.034f, 10.822f, 16.046f)
                curveTo(10.743f, 16.058f, 10.684f, 16.098f, 10.69f, 16.137f)
                curveTo(10.696f, 16.176f, 10.764f, 16.197f, 10.843f, 16.185f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF424242)),
                fillAlpha = 0.2f,
                strokeAlpha = 0.2f
            ) {
                moveTo(8.511f, 16.274f)
                curveTo(8.586f, 16.256f, 8.639f, 16.212f, 8.631f, 16.175f)
                curveTo(8.622f, 16.138f, 8.554f, 16.122f, 8.479f, 16.139f)
                curveTo(8.404f, 16.157f, 8.35f, 16.201f, 8.359f, 16.239f)
                curveTo(8.368f, 16.276f, 8.436f, 16.291f, 8.511f, 16.274f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF424242)),
                fillAlpha = 0.2f,
                strokeAlpha = 0.2f
            ) {
                moveTo(10.4f, 16.101f)
                curveTo(10.497f, 16.101f, 10.577f, 16.072f, 10.577f, 16.036f)
                curveTo(10.577f, 16.001f, 10.497f, 15.972f, 10.4f, 15.972f)
                curveTo(10.302f, 15.972f, 10.223f, 16.001f, 10.223f, 16.036f)
                curveTo(10.223f, 16.072f, 10.302f, 16.101f, 10.4f, 16.101f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF424242)),
                fillAlpha = 0.2f,
                strokeAlpha = 0.2f
            ) {
                moveTo(9.363f, 16.044f)
                curveTo(9.412f, 16.011f, 9.44f, 15.968f, 9.426f, 15.946f)
                curveTo(9.412f, 15.925f, 9.361f, 15.934f, 9.312f, 15.966f)
                curveTo(9.262f, 15.998f, 9.234f, 16.042f, 9.248f, 16.064f)
                curveTo(9.262f, 16.085f, 9.314f, 16.076f, 9.363f, 16.044f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF424242)),
                fillAlpha = 0.2f,
                strokeAlpha = 0.2f
            ) {
                moveTo(9.827f, 16.281f)
                curveTo(9.843f, 16.245f, 9.782f, 16.181f, 9.689f, 16.138f)
                curveTo(9.597f, 16.096f, 9.508f, 16.09f, 9.492f, 16.126f)
                curveTo(9.475f, 16.162f, 9.536f, 16.226f, 9.629f, 16.269f)
                curveTo(9.721f, 16.312f, 9.81f, 16.317f, 9.827f, 16.281f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF424242)),
                fillAlpha = 0.2f,
                strokeAlpha = 0.2f
            ) {
                moveTo(7.808f, 16.385f)
                curveTo(7.825f, 16.365f, 7.807f, 16.325f, 7.77f, 16.294f)
                curveTo(7.733f, 16.264f, 7.69f, 16.256f, 7.673f, 16.275f)
                curveTo(7.657f, 16.295f, 7.674f, 16.336f, 7.712f, 16.367f)
                curveTo(7.749f, 16.397f, 7.792f, 16.405f, 7.808f, 16.385f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF424242)),
                fillAlpha = 0.08f,
                strokeAlpha = 0.08f
            ) {
                moveTo(6.785f, 16.335f)
                curveTo(6.856f, 16.335f, 6.914f, 16.307f, 6.914f, 16.272f)
                curveTo(6.914f, 16.237f, 6.856f, 16.209f, 6.785f, 16.209f)
                curveTo(6.714f, 16.209f, 6.656f, 16.237f, 6.656f, 16.272f)
                curveTo(6.656f, 16.307f, 6.714f, 16.335f, 6.785f, 16.335f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF424242)),
                fillAlpha = 0.08f,
                strokeAlpha = 0.08f
            ) {
                moveTo(6.519f, 16.168f)
                curveTo(6.568f, 16.168f, 6.608f, 16.144f, 6.608f, 16.114f)
                curveTo(6.608f, 16.085f, 6.568f, 16.06f, 6.519f, 16.06f)
                curveTo(6.47f, 16.06f, 6.431f, 16.085f, 6.431f, 16.114f)
                curveTo(6.431f, 16.144f, 6.47f, 16.168f, 6.519f, 16.168f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF424242)),
                fillAlpha = 0.08f,
                strokeAlpha = 0.08f
            ) {
                moveTo(7.962f, 16.212f)
                curveTo(8.032f, 16.212f, 8.089f, 16.178f, 8.089f, 16.135f)
                curveTo(8.089f, 16.093f, 8.032f, 16.059f, 7.962f, 16.059f)
                curveTo(7.892f, 16.059f, 7.834f, 16.093f, 7.834f, 16.135f)
                curveTo(7.834f, 16.178f, 7.892f, 16.212f, 7.962f, 16.212f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF424242)),
                fillAlpha = 0.08f,
                strokeAlpha = 0.08f
            ) {
                moveTo(7.457f, 16.244f)
                curveTo(7.536f, 16.232f, 7.595f, 16.191f, 7.589f, 16.153f)
                curveTo(7.583f, 16.114f, 7.515f, 16.092f, 7.436f, 16.104f)
                curveTo(7.358f, 16.116f, 7.299f, 16.157f, 7.304f, 16.195f)
                curveTo(7.31f, 16.234f, 7.379f, 16.256f, 7.457f, 16.244f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF424242)),
                fillAlpha = 0.2f,
                strokeAlpha = 0.2f
            ) {
                moveTo(7.014f, 16.158f)
                curveTo(7.112f, 16.158f, 7.191f, 16.129f, 7.191f, 16.093f)
                curveTo(7.191f, 16.058f, 7.112f, 16.029f, 7.014f, 16.029f)
                curveTo(6.917f, 16.029f, 6.837f, 16.058f, 6.837f, 16.093f)
                curveTo(6.837f, 16.129f, 6.917f, 16.158f, 7.014f, 16.158f)
                close()
            }
            path(fill = SolidColor(Color(0xFFEEEEEE))) {
                moveTo(13.271f, 10.125f)
                curveTo(13.132f, 10.125f, 13.02f, 10.238f, 13.02f, 10.376f)
                verticalLineTo(13.873f)
                curveTo(13.02f, 14.012f, 13.132f, 14.124f, 13.271f, 14.124f)
                horizontalLineTo(15.519f)
                curveTo(15.657f, 14.124f, 15.769f, 14.012f, 15.769f, 13.873f)
                verticalLineTo(10.376f)
                curveTo(15.769f, 10.238f, 15.657f, 10.125f, 15.519f, 10.125f)
                horizontalLineTo(13.271f)
                close()
                moveTo(14.52f, 10.376f)
                horizontalLineTo(15.519f)
                verticalLineTo(11.375f)
                horizontalLineTo(14.52f)
                verticalLineTo(10.376f)
                close()
                moveTo(13.271f, 10.376f)
                horizontalLineTo(14.269f)
                verticalLineTo(11.375f)
                horizontalLineTo(13.271f)
                verticalLineTo(10.376f)
                close()
                moveTo(13.271f, 11.625f)
                horizontalLineTo(14.269f)
                verticalLineTo(12.624f)
                horizontalLineTo(13.271f)
                verticalLineTo(11.625f)
                close()
                moveTo(14.271f, 13.873f)
                horizontalLineTo(13.272f)
                verticalLineTo(12.875f)
                horizontalLineTo(14.271f)
                verticalLineTo(13.873f)
                close()
                moveTo(15.52f, 13.873f)
                horizontalLineTo(14.521f)
                verticalLineTo(12.875f)
                horizontalLineTo(15.52f)
                verticalLineTo(13.873f)
                close()
                moveTo(14.52f, 12.624f)
                verticalLineTo(11.625f)
                horizontalLineTo(15.519f)
                verticalLineTo(12.624f)
                horizontalLineTo(14.52f)
                close()
            }
            path(fill = SolidColor(Color(0xFFFFC107))) {
                moveTo(10.365f, 13.274f)
                curveTo(10.075f, 13.274f, 9.84f, 13.038f, 9.84f, 12.748f)
                curveTo(9.84f, 12.459f, 10.075f, 12.224f, 10.365f, 12.224f)
                curveTo(10.654f, 12.224f, 10.89f, 12.459f, 10.89f, 12.748f)
                curveTo(10.89f, 13.038f, 10.654f, 13.274f, 10.365f, 13.274f)
                close()
            }
            path(fill = SolidColor(Color(0xFFC62828))) {
                moveTo(10.365f, 12.373f)
                curveTo(10.572f, 12.373f, 10.74f, 12.542f, 10.74f, 12.748f)
                curveTo(10.74f, 12.955f, 10.572f, 13.123f, 10.365f, 13.123f)
                curveTo(10.158f, 13.123f, 9.99f, 12.955f, 9.99f, 12.748f)
                curveTo(9.99f, 12.542f, 10.158f, 12.373f, 10.365f, 12.373f)
                close()
                moveTo(10.365f, 12.073f)
                curveTo(9.993f, 12.073f, 9.69f, 12.377f, 9.69f, 12.748f)
                curveTo(9.69f, 13.12f, 9.993f, 13.423f, 10.365f, 13.423f)
                curveTo(10.737f, 13.423f, 11.04f, 13.12f, 11.04f, 12.748f)
                curveTo(11.04f, 12.377f, 10.737f, 12.073f, 10.365f, 12.073f)
                close()
            }
            path(fill = SolidColor(Color(0xFF795548))) {
                moveTo(15.894f, 14.623f)
                horizontalLineTo(12.897f)
                curveTo(12.759f, 14.623f, 12.646f, 14.511f, 12.646f, 14.373f)
                verticalLineTo(13.873f)
                curveTo(12.646f, 13.736f, 12.759f, 13.623f, 12.897f, 13.623f)
                horizontalLineTo(15.895f)
                curveTo(16.033f, 13.623f, 16.146f, 13.736f, 16.146f, 13.873f)
                verticalLineTo(14.373f)
                curveTo(16.144f, 14.511f, 16.032f, 14.623f, 15.894f, 14.623f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF424242)),
                fillAlpha = 0.2f,
                strokeAlpha = 0.2f
            ) {
                moveTo(15.894f, 13.873f)
                verticalLineTo(14.373f)
                horizontalLineTo(12.897f)
                verticalLineTo(13.873f)
                horizontalLineTo(15.894f)
                close()
                moveTo(15.894f, 13.623f)
                horizontalLineTo(12.897f)
                curveTo(12.759f, 13.623f, 12.646f, 13.736f, 12.646f, 13.873f)
                verticalLineTo(14.373f)
                curveTo(12.646f, 14.511f, 12.759f, 14.623f, 12.897f, 14.623f)
                horizontalLineTo(15.895f)
                curveTo(16.033f, 14.623f, 16.146f, 14.511f, 16.146f, 14.373f)
                verticalLineTo(13.873f)
                curveTo(16.144f, 13.736f, 16.032f, 13.623f, 15.894f, 13.623f)
                close()
            }
            path(
                fill = Brush.radialGradient(
                    colorStops = arrayOf(
                        0f to Color(0xFFF44336),
                        0.34f to Color(0xFFEE3F35),
                        0.803f to Color(0xFFDC3531),
                        0.996f to Color(0xFFD32F2F)
                    ),
                    center = Offset(11.21f, 2.599f),
                    radius = 8.45f
                )
            ) {
                moveTo(18.553f, 8.777f)
                lineTo(11.671f, 2.78f)
                curveTo(11.442f, 2.579f, 11.101f, 2.579f, 10.872f, 2.78f)
                lineTo(3.992f, 8.777f)
                curveTo(3.734f, 9.002f, 3.704f, 9.396f, 3.926f, 9.657f)
                curveTo(4.146f, 9.92f, 4.535f, 9.95f, 4.793f, 9.725f)
                lineTo(11.028f, 4.292f)
                curveTo(11.169f, 4.169f, 11.379f, 4.169f, 11.52f, 4.292f)
                lineTo(17.755f, 9.725f)
                curveTo(17.871f, 9.825f, 18.013f, 9.875f, 18.154f, 9.875f)
                curveTo(18.327f, 9.875f, 18.499f, 9.801f, 18.621f, 9.657f)
                curveTo(18.841f, 9.396f, 18.811f, 9.002f, 18.553f, 8.777f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF424242)),
                fillAlpha = 0.2f,
                strokeAlpha = 0.2f
            ) {
                moveTo(11.272f, 3.08f)
                curveTo(11.299f, 3.08f, 11.338f, 3.087f, 11.377f, 3.119f)
                lineTo(18.259f, 9.116f)
                curveTo(18.331f, 9.179f, 18.34f, 9.294f, 18.277f, 9.368f)
                curveTo(18.235f, 9.419f, 18.183f, 9.426f, 18.154f, 9.426f)
                curveTo(18.127f, 9.426f, 18.088f, 9.419f, 18.051f, 9.387f)
                lineTo(11.814f, 3.953f)
                curveTo(11.664f, 3.822f, 11.472f, 3.75f, 11.272f, 3.75f)
                curveTo(11.073f, 3.75f, 10.881f, 3.822f, 10.731f, 3.953f)
                lineTo(4.496f, 9.386f)
                curveTo(4.458f, 9.419f, 4.418f, 9.425f, 4.392f, 9.425f)
                curveTo(4.365f, 9.425f, 4.313f, 9.417f, 4.269f, 9.366f)
                curveTo(4.208f, 9.293f, 4.215f, 9.177f, 4.287f, 9.114f)
                lineTo(11.169f, 3.117f)
                curveTo(11.205f, 3.087f, 11.245f, 3.08f, 11.272f, 3.08f)
                close()
                moveTo(11.272f, 2.63f)
                curveTo(11.13f, 2.63f, 10.987f, 2.679f, 10.872f, 2.78f)
                lineTo(3.992f, 8.777f)
                curveTo(3.734f, 9.002f, 3.704f, 9.396f, 3.926f, 9.657f)
                curveTo(4.047f, 9.801f, 4.22f, 9.875f, 4.392f, 9.875f)
                curveTo(4.533f, 9.875f, 4.676f, 9.825f, 4.791f, 9.725f)
                lineTo(11.026f, 4.292f)
                curveTo(11.097f, 4.23f, 11.185f, 4.2f, 11.272f, 4.2f)
                curveTo(11.359f, 4.2f, 11.448f, 4.23f, 11.518f, 4.292f)
                lineTo(17.754f, 9.725f)
                curveTo(17.869f, 9.825f, 18.012f, 9.875f, 18.153f, 9.875f)
                curveTo(18.325f, 9.875f, 18.498f, 9.801f, 18.619f, 9.657f)
                curveTo(18.84f, 9.395f, 18.81f, 9.002f, 18.553f, 8.777f)
                lineTo(11.671f, 2.78f)
                curveTo(11.557f, 2.681f, 11.415f, 2.63f, 11.272f, 2.63f)
                close()
            }
            path(
                fill = Brush.linearGradient(
                    colorStops = arrayOf(
                        0.004f to Color(0xFFBDBDBD),
                        0.397f to Color(0xFFBABABA),
                        0.703f to Color(0xFFB0B0B0),
                        0.979f to Color(0xFFA0A0A0),
                        1f to Color(0xFF9E9E9E)
                    ),
                    start = Offset(9.027f, 16.413f),
                    end = Offset(9.027f, 17.223f)
                )
            ) {
                moveTo(12.275f, 17.236f)
                horizontalLineTo(5.778f)
                verticalLineTo(16.711f)
                curveTo(5.778f, 16.531f, 5.924f, 16.386f, 6.104f, 16.386f)
                horizontalLineTo(11.948f)
                curveTo(12.128f, 16.386f, 12.273f, 16.531f, 12.273f, 16.711f)
                verticalLineTo(17.236f)
                horizontalLineTo(12.275f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF263238)),
                fillAlpha = 0.3f,
                strokeAlpha = 0.3f
            ) {
                moveTo(11.949f, 16.536f)
                curveTo(12.047f, 16.536f, 12.125f, 16.615f, 12.125f, 16.711f)
                verticalLineTo(17.086f)
                horizontalLineTo(5.928f)
                verticalLineTo(16.711f)
                curveTo(5.928f, 16.614f, 6.008f, 16.536f, 6.104f, 16.536f)
                horizontalLineTo(11.949f)
                close()
                moveTo(11.949f, 16.386f)
                horizontalLineTo(6.105f)
                curveTo(5.925f, 16.386f, 5.78f, 16.531f, 5.78f, 16.711f)
                verticalLineTo(17.236f)
                horizontalLineTo(12.276f)
                verticalLineTo(16.711f)
                curveTo(12.275f, 16.531f, 12.129f, 16.386f, 11.949f, 16.386f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF424242)),
                fillAlpha = 0.2f,
                strokeAlpha = 0.2f
            ) {
                moveTo(11.377f, 16.768f)
                curveTo(11.437f, 16.747f, 11.477f, 16.706f, 11.467f, 16.678f)
                curveTo(11.457f, 16.65f, 11.4f, 16.645f, 11.34f, 16.667f)
                curveTo(11.28f, 16.689f, 11.24f, 16.729f, 11.25f, 16.757f)
                curveTo(11.26f, 16.785f, 11.317f, 16.79f, 11.377f, 16.768f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF424242)),
                fillAlpha = 0.2f,
                strokeAlpha = 0.2f
            ) {
                moveTo(8.755f, 16.99f)
                curveTo(8.798f, 16.961f, 8.82f, 16.92f, 8.805f, 16.898f)
                curveTo(8.79f, 16.876f, 8.744f, 16.882f, 8.701f, 16.911f)
                curveTo(8.659f, 16.94f, 8.637f, 16.981f, 8.652f, 17.003f)
                curveTo(8.667f, 17.025f, 8.713f, 17.019f, 8.755f, 16.99f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF424242)),
                fillAlpha = 0.08f,
                strokeAlpha = 0.08f
            ) {
                moveTo(11.007f, 16.963f)
                curveTo(11.085f, 16.963f, 11.149f, 16.935f, 11.149f, 16.9f)
                curveTo(11.149f, 16.866f, 11.085f, 16.837f, 11.007f, 16.837f)
                curveTo(10.928f, 16.837f, 10.864f, 16.866f, 10.864f, 16.9f)
                curveTo(10.864f, 16.935f, 10.928f, 16.963f, 11.007f, 16.963f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF424242)),
                fillAlpha = 0.08f,
                strokeAlpha = 0.08f
            ) {
                moveTo(9.783f, 16.956f)
                curveTo(9.861f, 16.956f, 9.925f, 16.928f, 9.925f, 16.893f)
                curveTo(9.925f, 16.858f, 9.861f, 16.83f, 9.783f, 16.83f)
                curveTo(9.704f, 16.83f, 9.64f, 16.858f, 9.64f, 16.893f)
                curveTo(9.64f, 16.928f, 9.704f, 16.956f, 9.783f, 16.956f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF424242)),
                fillAlpha = 0.08f,
                strokeAlpha = 0.08f
            ) {
                moveTo(10.077f, 16.788f)
                curveTo(10.131f, 16.788f, 10.175f, 16.764f, 10.175f, 16.734f)
                curveTo(10.175f, 16.704f, 10.131f, 16.68f, 10.077f, 16.68f)
                curveTo(10.023f, 16.68f, 9.979f, 16.704f, 9.979f, 16.734f)
                curveTo(9.979f, 16.764f, 10.023f, 16.788f, 10.077f, 16.788f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF424242)),
                fillAlpha = 0.08f,
                strokeAlpha = 0.08f
            ) {
                moveTo(8.485f, 16.831f)
                curveTo(8.563f, 16.831f, 8.626f, 16.797f, 8.626f, 16.755f)
                curveTo(8.626f, 16.713f, 8.563f, 16.678f, 8.485f, 16.678f)
                curveTo(8.407f, 16.678f, 8.344f, 16.713f, 8.344f, 16.755f)
                curveTo(8.344f, 16.797f, 8.407f, 16.831f, 8.485f, 16.831f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF424242)),
                fillAlpha = 0.08f,
                strokeAlpha = 0.08f
            ) {
                moveTo(11.856f, 16.756f)
                curveTo(11.91f, 16.756f, 11.953f, 16.737f, 11.953f, 16.713f)
                curveTo(11.953f, 16.689f, 11.91f, 16.669f, 11.856f, 16.669f)
                curveTo(11.802f, 16.669f, 11.758f, 16.689f, 11.758f, 16.713f)
                curveTo(11.758f, 16.737f, 11.802f, 16.756f, 11.856f, 16.756f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF424242)),
                fillAlpha = 0.08f,
                strokeAlpha = 0.08f
            ) {
                moveTo(9.21f, 16.815f)
                curveTo(9.215f, 16.776f, 9.148f, 16.736f, 9.061f, 16.725f)
                curveTo(8.974f, 16.713f, 8.899f, 16.736f, 8.894f, 16.774f)
                curveTo(8.889f, 16.813f, 8.956f, 16.853f, 9.043f, 16.864f)
                curveTo(9.13f, 16.876f, 9.205f, 16.854f, 9.21f, 16.815f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF424242)),
                fillAlpha = 0.2f,
                strokeAlpha = 0.2f
            ) {
                moveTo(11.782f, 16.917f)
                curveTo(11.79f, 16.878f, 11.729f, 16.834f, 11.646f, 16.817f)
                curveTo(11.563f, 16.801f, 11.49f, 16.818f, 11.482f, 16.856f)
                curveTo(11.474f, 16.895f, 11.535f, 16.939f, 11.618f, 16.956f)
                curveTo(11.701f, 16.972f, 11.774f, 16.955f, 11.782f, 16.917f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF424242)),
                fillAlpha = 0.2f,
                strokeAlpha = 0.2f
            ) {
                moveTo(9.531f, 16.779f)
                curveTo(9.639f, 16.779f, 9.726f, 16.75f, 9.726f, 16.714f)
                curveTo(9.726f, 16.679f, 9.639f, 16.65f, 9.531f, 16.65f)
                curveTo(9.423f, 16.65f, 9.336f, 16.679f, 9.336f, 16.714f)
                curveTo(9.336f, 16.75f, 9.423f, 16.779f, 9.531f, 16.779f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF424242)),
                fillAlpha = 0.2f,
                strokeAlpha = 0.2f
            ) {
                moveTo(10.804f, 16.742f)
                curveTo(10.818f, 16.719f, 10.783f, 16.675f, 10.728f, 16.643f)
                curveTo(10.673f, 16.611f, 10.617f, 16.604f, 10.604f, 16.627f)
                curveTo(10.591f, 16.65f, 10.625f, 16.695f, 10.68f, 16.726f)
                curveTo(10.736f, 16.758f, 10.791f, 16.765f, 10.804f, 16.742f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF424242)),
                fillAlpha = 0.2f,
                strokeAlpha = 0.2f
            ) {
                moveTo(10.377f, 16.949f)
                curveTo(10.48f, 16.907f, 10.551f, 16.843f, 10.536f, 16.805f)
                curveTo(10.521f, 16.768f, 10.425f, 16.771f, 10.322f, 16.813f)
                curveTo(10.219f, 16.854f, 10.148f, 16.919f, 10.163f, 16.956f)
                curveTo(10.179f, 16.994f, 10.274f, 16.991f, 10.377f, 16.949f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF424242)),
                fillAlpha = 0.2f,
                strokeAlpha = 0.2f
            ) {
                moveTo(7.691f, 16.825f)
                curveTo(7.751f, 16.804f, 7.791f, 16.763f, 7.781f, 16.735f)
                curveTo(7.771f, 16.707f, 7.714f, 16.702f, 7.654f, 16.724f)
                curveTo(7.594f, 16.746f, 7.554f, 16.786f, 7.564f, 16.814f)
                curveTo(7.574f, 16.842f, 7.631f, 16.847f, 7.691f, 16.825f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF424242)),
                fillAlpha = 0.08f,
                strokeAlpha = 0.08f
            ) {
                moveTo(7.322f, 17.022f)
                curveTo(7.4f, 17.022f, 7.464f, 16.994f, 7.464f, 16.959f)
                curveTo(7.464f, 16.924f, 7.4f, 16.896f, 7.322f, 16.896f)
                curveTo(7.243f, 16.896f, 7.179f, 16.924f, 7.179f, 16.959f)
                curveTo(7.179f, 16.994f, 7.243f, 17.022f, 7.322f, 17.022f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF424242)),
                fillAlpha = 0.08f,
                strokeAlpha = 0.08f
            ) {
                moveTo(6.391f, 16.846f)
                curveTo(6.445f, 16.846f, 6.489f, 16.822f, 6.489f, 16.792f)
                curveTo(6.489f, 16.763f, 6.445f, 16.738f, 6.391f, 16.738f)
                curveTo(6.338f, 16.738f, 6.294f, 16.763f, 6.294f, 16.792f)
                curveTo(6.294f, 16.822f, 6.338f, 16.846f, 6.391f, 16.846f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF424242)),
                fillAlpha = 0.2f,
                strokeAlpha = 0.2f
            ) {
                moveTo(8.097f, 16.976f)
                curveTo(8.105f, 16.938f, 8.044f, 16.893f, 7.961f, 16.877f)
                curveTo(7.878f, 16.86f, 7.805f, 16.878f, 7.797f, 16.916f)
                curveTo(7.789f, 16.954f, 7.85f, 16.998f, 7.933f, 17.015f)
                curveTo(8.016f, 17.032f, 8.089f, 17.014f, 8.097f, 16.976f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF424242)),
                fillAlpha = 0.2f,
                strokeAlpha = 0.2f
            ) {
                moveTo(7.118f, 16.799f)
                curveTo(7.131f, 16.776f, 7.097f, 16.732f, 7.041f, 16.7f)
                curveTo(6.986f, 16.668f, 6.931f, 16.661f, 6.917f, 16.684f)
                curveTo(6.904f, 16.707f, 6.938f, 16.751f, 6.994f, 16.783f)
                curveTo(7.049f, 16.815f, 7.105f, 16.822f, 7.118f, 16.799f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF424242)),
                fillAlpha = 0.2f,
                strokeAlpha = 0.2f
            ) {
                moveTo(6.691f, 17.006f)
                curveTo(6.794f, 16.965f, 6.865f, 16.9f, 6.85f, 16.863f)
                curveTo(6.835f, 16.825f, 6.739f, 16.829f, 6.636f, 16.87f)
                curveTo(6.533f, 16.912f, 6.462f, 16.976f, 6.477f, 17.014f)
                curveTo(6.493f, 17.051f, 6.588f, 17.048f, 6.691f, 17.006f)
                close()
            }
            path(
                fill = Brush.radialGradient(
                    colorStops = arrayOf(
                        0f to Color(0xFF9CCC65),
                        0.254f to Color(0xFF96C760),
                        0.599f to Color(0xFF84B851),
                        0.995f to Color(0xFF689F38)
                    ),
                    center = Offset(15.786f, 15.245f),
                    radius = 3.741f
                )
            ) {
                moveTo(13.378f, 17.997f)
                curveTo(13.275f, 17.997f, 13.219f, 17.887f, 13.2f, 17.841f)
                curveTo(13.143f, 17.703f, 13.149f, 17.5f, 13.27f, 17.379f)
                curveTo(13.318f, 17.331f, 13.323f, 17.25f, 13.29f, 17.191f)
                curveTo(13.216f, 17.061f, 13.185f, 16.897f, 13.201f, 16.731f)
                curveTo(13.251f, 16.224f, 13.662f, 15.789f, 14.176f, 15.696f)
                curveTo(14.248f, 15.682f, 14.322f, 15.676f, 14.394f, 15.676f)
                curveTo(14.49f, 15.676f, 14.586f, 15.688f, 14.68f, 15.711f)
                curveTo(14.692f, 15.714f, 14.704f, 15.715f, 14.716f, 15.715f)
                curveTo(14.767f, 15.715f, 14.815f, 15.69f, 14.844f, 15.645f)
                curveTo(15.064f, 15.289f, 15.447f, 15.076f, 15.865f, 15.076f)
                curveTo(16.396f, 15.076f, 16.87f, 15.432f, 17.019f, 15.94f)
                curveTo(17.038f, 16.005f, 17.097f, 16.048f, 17.163f, 16.048f)
                curveTo(17.172f, 16.048f, 17.181f, 16.047f, 17.191f, 16.045f)
                curveTo(17.266f, 16.032f, 17.341f, 16.024f, 17.416f, 16.024f)
                curveTo(17.509f, 16.024f, 17.604f, 16.036f, 17.697f, 16.059f)
                curveTo(18.198f, 16.18f, 18.576f, 16.623f, 18.615f, 17.133f)
                curveTo(18.639f, 17.452f, 18.541f, 17.757f, 18.337f, 17.998f)
                horizontalLineTo(13.378f)
                verticalLineTo(17.997f)
                close()
            }
            path(fill = SolidColor(Color(0xFF689F38))) {
                moveTo(15.865f, 15.226f)
                curveTo(16.33f, 15.226f, 16.744f, 15.537f, 16.875f, 15.983f)
                curveTo(16.912f, 16.113f, 17.032f, 16.198f, 17.163f, 16.198f)
                curveTo(17.181f, 16.198f, 17.2f, 16.197f, 17.218f, 16.192f)
                curveTo(17.284f, 16.181f, 17.35f, 16.173f, 17.415f, 16.173f)
                curveTo(17.496f, 16.173f, 17.578f, 16.184f, 17.659f, 16.203f)
                curveTo(18.098f, 16.309f, 18.428f, 16.697f, 18.463f, 17.142f)
                curveTo(18.483f, 17.398f, 18.412f, 17.644f, 18.262f, 17.847f)
                lineTo(13.38f, 17.848f)
                curveTo(13.368f, 17.842f, 13.33f, 17.8f, 13.317f, 17.718f)
                curveTo(13.305f, 17.646f, 13.312f, 17.547f, 13.374f, 17.485f)
                curveTo(13.449f, 17.412f, 13.504f, 17.271f, 13.419f, 17.118f)
                curveTo(13.36f, 17.013f, 13.335f, 16.881f, 13.348f, 16.746f)
                curveTo(13.392f, 16.305f, 13.75f, 15.925f, 14.202f, 15.843f)
                curveTo(14.265f, 15.831f, 14.329f, 15.826f, 14.392f, 15.826f)
                curveTo(14.476f, 15.826f, 14.56f, 15.837f, 14.643f, 15.856f)
                curveTo(14.667f, 15.863f, 14.691f, 15.866f, 14.715f, 15.866f)
                curveTo(14.817f, 15.866f, 14.914f, 15.813f, 14.97f, 15.724f)
                curveTo(15.166f, 15.413f, 15.499f, 15.226f, 15.865f, 15.226f)
                close()
                moveTo(15.865f, 14.927f)
                curveTo(15.381f, 14.927f, 14.955f, 15.182f, 14.716f, 15.566f)
                curveTo(14.613f, 15.54f, 14.505f, 15.526f, 14.394f, 15.526f)
                curveTo(14.314f, 15.526f, 14.233f, 15.534f, 14.149f, 15.549f)
                curveTo(13.57f, 15.654f, 13.108f, 16.132f, 13.051f, 16.718f)
                curveTo(13.018f, 17.061f, 13.165f, 17.272f, 13.162f, 17.275f)
                curveTo(12.874f, 17.563f, 13.033f, 18.15f, 13.377f, 18.15f)
                horizontalLineTo(18.403f)
                curveTo(18.65f, 17.884f, 18.793f, 17.52f, 18.763f, 17.122f)
                curveTo(18.719f, 16.544f, 18.295f, 16.053f, 17.731f, 15.915f)
                curveTo(17.623f, 15.889f, 17.518f, 15.877f, 17.416f, 15.877f)
                curveTo(17.329f, 15.877f, 17.245f, 15.887f, 17.163f, 15.901f)
                curveTo(16.999f, 15.337f, 16.48f, 14.927f, 15.865f, 14.927f)
                close()
            }
            path(
                stroke = SolidColor(Color(0xFF689F38)),
                strokeLineWidth = 0.299991f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(14.536f, 16.447f)
                curveTo(14.536f, 16.447f, 14.524f, 16.077f, 14.821f, 15.681f)
            }
            path(
                stroke = SolidColor(Color(0xFF689F38)),
                strokeLineWidth = 0.299991f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(17.14f, 16.053f)
                curveTo(16.791f, 16.059f, 16.449f, 16.333f, 16.396f, 16.67f)
            }
            path(
                stroke = SolidColor(Color(0xFF689F38)),
                strokeLineWidth = 0.299991f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(16.782f, 16.825f)
                curveTo(16.475f, 16.686f, 16.061f, 16.707f, 15.743f, 16.869f)
            }
            path(fill = SolidColor(Color(0xFF689F38))) {
                moveTo(0.769f, 17.229f)
                horizontalLineTo(18.768f)
                verticalLineTo(18.429f)
                horizontalLineTo(0.769f)
                verticalLineTo(17.229f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF424242)),
                fillAlpha = 0.2f,
                strokeAlpha = 0.2f
            ) {
                moveTo(0.769f, 17.979f)
                horizontalLineTo(18.768f)
                verticalLineTo(18.429f)
                horizontalLineTo(0.769f)
                verticalLineTo(17.979f)
                close()
            }
        }.build()

        return _Home!!
    }

@Suppress("ObjectPropertyName")
private var _Home: ImageVector? = null
