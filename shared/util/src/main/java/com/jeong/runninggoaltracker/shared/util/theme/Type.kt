package com.jeong.runninggoaltracker.shared.util.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.jeong.runninggoaltracker.shared.util.R

val Pretendard = FontFamily(
    Font(R.font.pretendard_thin, FontWeight.Thin),               // 100
    Font(R.font.pretendard_extralight, FontWeight.ExtraLight),   // 200
    Font(R.font.pretendard_light, FontWeight.Light),             // 300
    Font(R.font.pretendard_regular, FontWeight.Normal),          // 400
    Font(R.font.pretendard_medium, FontWeight.Medium),           // 500
    Font(R.font.pretendard_semibold, FontWeight.SemiBold),       // 600
    Font(R.font.pretendard_bold, FontWeight.Bold),               // 700
    Font(R.font.pretendard_extrabold, FontWeight.ExtraBold),     // 800
    Font(R.font.pretendard_black, FontWeight.Black)              // 900
)

val AppTypography = Typography(
    // 화면 큰 타이틀
    titleLarge = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    // 중간 타이틀
    titleMedium = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    // 일반 본문
    bodyLarge = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    // 보조 본문
    bodyMedium = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Light,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    // 라벨/버튼 등 작은 글자
    labelSmall = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp
    )
)
