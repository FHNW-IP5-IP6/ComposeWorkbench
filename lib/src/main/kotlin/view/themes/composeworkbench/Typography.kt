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
val NotoSansTypography = Typography(
	h1 = defaultTypography.h1.copy(fontFamily = appFontFamily),
	h2 = defaultTypography.h2.copy(fontFamily = appFontFamily),
	h3 = defaultTypography.h3.copy(fontFamily = appFontFamily),
	h4 = defaultTypography.h4.copy(fontFamily = appFontFamily),
	h5 = defaultTypography.h5.copy(fontFamily = appFontFamily),
	h6 = defaultTypography.h6.copy(fontFamily = appFontFamily),
	subtitle1 = defaultTypography.subtitle1.copy(fontFamily = appFontFamily),
	subtitle2 = defaultTypography.subtitle2.copy(fontFamily = appFontFamily),
	body1 = defaultTypography.body1.copy(fontFamily = appFontFamily),
	body2 = defaultTypography.body2.copy(fontFamily = appFontFamily),
	button = defaultTypography.button.copy(fontFamily = appFontFamily),
	caption = defaultTypography.caption.copy(fontFamily = appFontFamily),
	overline = defaultTypography.overline.copy(fontFamily = appFontFamily)
)