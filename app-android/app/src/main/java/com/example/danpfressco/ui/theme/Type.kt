package com.example.danpfressco.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.danpfressco.R

val Nunito = FontFamily(
    Font(R.font.nunito_regular, FontWeight.Normal),
    Font(R.font.nunito_semibold, FontWeight.SemiBold),
    Font(R.font.nunito_bold, FontWeight.Bold),
    Font(R.font.nunito_extrabold, FontWeight.ExtraBold)
)

val Typography = Typography(
    headlineLarge = TextStyle(
        fontFamily = Nunito, fontWeight = FontWeight.ExtraBold, fontSize = 30.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 22.sp
    ),
    titleLarge = TextStyle(
        fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 20.sp
    ),
    titleMedium = TextStyle(
        fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 18.sp
    ),
    labelLarge = TextStyle(
        fontFamily = Nunito, fontWeight = FontWeight.SemiBold, fontSize = 16.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = Nunito, fontWeight = FontWeight.Normal, fontSize = 16.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = Nunito, fontWeight = FontWeight.Normal, fontSize = 14.sp
    ),
    labelMedium = TextStyle(
        fontFamily = Nunito, fontWeight = FontWeight.SemiBold, fontSize = 13.sp
    ),
    labelSmall = TextStyle(
        fontFamily = Nunito, fontWeight = FontWeight.SemiBold, fontSize = 12.sp
    ),
    bodySmall = TextStyle(
        fontFamily = Nunito, fontWeight = FontWeight.Normal, fontSize = 12.sp
    )
)

val LogoTextStyle = TextStyle(
    fontFamily = Nunito,
    fontWeight = FontWeight.ExtraBold,
    fontSize = 34.sp
)