package com.jeong.runninggoaltracker.shared.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.jeong.runninggoaltracker.shared.designsystem.R

val Pretendard = FontFamily(
    Font(R.font.pretendard_thin, FontWeight.Thin),
    Font(R.font.pretendard_extralight, FontWeight.ExtraLight),
    Font(R.font.pretendard_light, FontWeight.Light),
    Font(R.font.pretendard_regular, FontWeight.Normal),
    Font(R.font.pretendard_medium, FontWeight.Medium),
    Font(R.font.pretendard_semibold, FontWeight.SemiBold),
    Font(R.font.pretendard_bold, FontWeight.Bold),
    Font(R.font.pretendard_extrabold, FontWeight.ExtraBold),
    Font(R.font.pretendard_black, FontWeight.Black)
)

val AppTypography = Typography(

    titleLarge = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),

    titleMedium = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),

    bodyLarge = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),

    bodyMedium = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Light,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),

    labelSmall = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp
    )
)
