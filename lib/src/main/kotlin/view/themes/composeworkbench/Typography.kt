package com.example.ui.theme
import androidx.compose.material.Typography
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font

//Replace with your font locations

private val appFontFamily = FontFamily(
	fonts = listOf(
		Font(
			resource = "font/noto-sans/NotoSans-Medium.ttf",
			weight = FontWeight.W400,
			style = FontStyle.Normal
		),
		Font(
			resource = "font/noto-sans/NotoSans-MediumItalic.ttf",
			weight = FontWeight.W400,
			style = FontStyle.Italic
		),
		Font(
			resource = "font/noto-sans/NotoSans-Bold.ttf",
			weight = FontWeight.W700,
			style = FontStyle.Normal
		),
		Font(
			resource = "font/noto-sans/NotoSans-Black.ttf",
			weight = FontWeight.W900,
			style = FontStyle.Normal
		),
)

)

private val defaultTypography = Typography()
private const val FACTOR = 0.75
val NotoSansTypography = Typography(
	h1 = 		defaultTypography.h1		.copy(fontFamily = appFontFamily, fontSize = defaultTypography.h1			.fontSize * FACTOR),
	h2 = 		defaultTypography.h2		.copy(fontFamily = appFontFamily, fontSize = defaultTypography.h2			.fontSize * FACTOR),
	h3 = 		defaultTypography.h3		.copy(fontFamily = appFontFamily, fontSize = defaultTypography.h3   		.fontSize * FACTOR),
	h4 = 		defaultTypography.h4		.copy(fontFamily = appFontFamily, fontSize = defaultTypography.h4   		.fontSize * FACTOR),
	h5 = 		defaultTypography.h5		.copy(fontFamily = appFontFamily, fontSize = defaultTypography.h5   		.fontSize * FACTOR),
	h6 = 		defaultTypography.h6		.copy(fontFamily = appFontFamily, fontSize = defaultTypography.h6   		.fontSize * FACTOR),
	subtitle1 = defaultTypography.subtitle1	.copy(fontFamily = appFontFamily, fontSize = defaultTypography.subtitle1   	.fontSize * FACTOR),
	subtitle2 = defaultTypography.subtitle2	.copy(fontFamily = appFontFamily, fontSize = defaultTypography.subtitle2   	.fontSize * FACTOR),
	body1 = 	defaultTypography.body1		.copy(fontFamily = appFontFamily, fontSize = defaultTypography.body1   		.fontSize * FACTOR),
	body2 = 	defaultTypography.body2		.copy(fontFamily = appFontFamily, fontSize = defaultTypography.body2   		.fontSize * FACTOR),
	button = 	defaultTypography.button	.copy(fontFamily = appFontFamily, fontSize = defaultTypography.button   	.fontSize * FACTOR),
	caption = 	defaultTypography.caption	.copy(fontFamily = appFontFamily, fontSize = defaultTypography.caption   	.fontSize * FACTOR),
	overline = 	defaultTypography.overline	.copy(fontFamily = appFontFamily, fontSize = defaultTypography.overline   	.fontSize * FACTOR)
)