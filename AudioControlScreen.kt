package com.remoteinput

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AudioControlScreen(net: NetClient) {
    var pcMuted by remember { mutableStateOf(false) }
    var phoneMuted by remember { mutableStateOf(false) }

    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF131A2E), Color(0xFF0A0E1A))))
    ) {
        Column(
            Modifier.fillMaxSize().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("🎚 مركز الصوت", fontSize = 22.sp, color = AppColors.Text, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text("تحكم كامل بالصوت من مكانك", fontSize = 13.sp, color = AppColors.TextDim)
            Spacer(Modifier.height(28.dp))

            // ═══ بطاقة: التحكم بصوت الكمبيوتر ═══
            AudioCard(
                title = "🖥 صوت الكمبيوتر",
                subtitle = if (pcMuted) "مكتوم" else "مفعّل",
                accent = if (pcMuted) AppColors.Danger else AppColors.Success
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ToggleBtn("🔇 كتم/فتح", pcMuted, AppColors.Danger) {
                        net.audioCmd(1)
                        pcMuted = !pcMuted
                    }
                    ToggleBtn("🔊 أعلى", false, AppColors.Success) { net.audioCmd(2) }
                    ToggleBtn("🔉 أخفض", false, AppColors.Warn)  { net.audioCmd(3) }
                }
                Spacer(Modifier.height(10.dp))
                ToggleBtn("🔀 تبديل جهاز الإخراج", false, AppColors.Accent2,
                    Modifier.fillMaxWidth()) { net.audioCmd(4) }
            }

            Spacer(Modifier.height(16.dp))

            // ═══ بطاقة: التحكم بصوت التابلت ═══
            AudioCard(
                title = "📱 صوت التابلت",
                subtitle = if (phoneMuted) "مكتوم محليًا" else "مفعّل",
                accent = if (phoneMuted) AppColors.Danger else AppColors.Success
            ) {
                ToggleBtn("🔇 كتم صوت التابلت", phoneMuted, AppColors.Danger,
                    Modifier.fillMaxWidth()) {
                    phoneMuted = !phoneMuted
                    // ملاحظة: يتم عبر MediaPlayer محلي (يُطبّق عبر النظام)
                }
            }

            Spacer(Modifier.height(16.dp))

            // ═══ بطاقة: وضع الاستماع ═══
            AudioCard(
                title = "🎧 وضع التشغيل",
                subtitle = "اختر أين يُشغَّل الصوت",
                accent = AppColors.Accent
            ) {
                Text(
                    "• PC فقط: يعمل صوت الكمبيوتر والتابلت صامت\n" +
                            "• Tablet فقط: يعمل صوت التابلت والكمبيوتر مكتوم\n" +
                            "• كليهما: يعمل الاثنان معًا",
                    color = AppColors.TextDim, fontSize = 12.sp, lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
fun AudioCard(
    title: String,
    subtitle: String,
    accent: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AppColors.Surface)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(8.dp).clip(RoundedCornerShape(4.dp)).background(accent))
            Spacer(Modifier.width(8.dp))
            Text(title, color = AppColors.Text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(4.dp))
        Text(subtitle, color = accent, fontSize = 12.sp)
        Spacer(Modifier.height(14.dp))
        content()
    }
}

@Composable
fun ToggleBtn(
    label: String,
    active: Boolean,
    tint: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    Box(
        modifier
            .height(46.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (active) tint.copy(alpha = 0.35f) else tint.copy(alpha = 0.15f))
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown()
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                    waitForUpOrCancellation()
                }
            }
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = tint, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}