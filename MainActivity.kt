package com.remoteinput

import android.content.Context
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import androidx.compose.foundation.clickable

// ═════════════════════════════════════════════════════════════
//  Design Tokens
// ═════════════════════════════════════════════════════════════
object AppColors {
    val Bg          = Color(0xFF0A0E1A)
    val Surface     = Color(0xFF161B2C)
    val SurfaceHigh = Color(0xFF1F2740)
    val Accent      = Color(0xFF4F8CFF)
    val Accent2     = Color(0xFF7B5CFF)
    val Success     = Color(0xFF4ADE80)
    val Danger      = Color(0xFFEF4444)
    val Warn        = Color(0xFFFBBF24)
    val Text        = Color(0xFFE8ECF5)
    val TextDim     = Color(0xFF8A93A8)
    val Key         = Color(0xFF232B42)
    val KeySpecial  = Color(0xFF2E3A5C)
}

// ═════════════════════════════════════════════════════════════
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(
                background = AppColors.Bg,
                surface = AppColors.Surface,
                primary = AppColors.Accent,
                onBackground = AppColors.Text,
                onSurface = AppColors.Text
            )) {
                Surface(color = AppColors.Bg, modifier = Modifier.fillMaxSize()) { App() }
            }
        }
    }
}

enum class Screen { CONNECT, SCAN, CONTROL }

@Composable
fun App() {
    val ctx = LocalContext.current
    val net = remember { NetClient() }
    val scope = rememberCoroutineScope()
    var screen by remember { mutableStateOf(Screen.CONNECT) }
    var status by remember { mutableStateOf("جاهز للاتصال") }
    var lastIp by remember { mutableStateOf("192.168.1.100") }

    LaunchedEffect(Unit) {
        SettingsRepo.getLastIp(ctx)?.let { lastIp = it }
        net.onStatus = { ok, msg ->
            status = msg
            if (ok) {
                screen = Screen.CONTROL
                scope.launch { SettingsRepo.saveLastIp(ctx, lastIp) }
            }
        }
    }

    when (screen) {
        Screen.CONNECT -> ConnectScreen(lastIp, status,
            onHostChange = { lastIp = it },
            onConnect = { net.connect(it) },
            onScan = { screen = Screen.SCAN })

        Screen.SCAN -> QrScanScreen(
            onScanned = { ip ->
                lastIp = ip
                net.connect(ip)
            },
            onBack = { screen = Screen.CONNECT })

        Screen.CONTROL -> {
            BackHandler {
                net.disconnect()
                screen = Screen.CONNECT
            }
            ControlScreen(net) {
                net.disconnect()
                screen = Screen.CONNECT
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════
@Composable
fun ConnectScreen(
    host: String,
    status: String,
    onHostChange: (String) -> Unit,
    onConnect: (String) -> Unit,
    onScan: () -> Unit
) {
    Box(
        Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF0A0E1A), Color(0xFF131A2E)))),
        contentAlignment = Alignment.Center
    ) {
        Column(
            Modifier.padding(32.dp).widthIn(max = 420.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("RemoteInput", fontSize = 40.sp, fontWeight = FontWeight.Bold, color = AppColors.Text)
            Spacer(Modifier.height(4.dp))
            Text("v3.0 — Pro Max", fontSize = 13.sp, color = AppColors.Accent2, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(40.dp))

            OutlinedTextField(
                value = host, onValueChange = onHostChange,
                label = { Text("عنوان IP") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppColors.Accent,
                    unfocusedBorderColor = AppColors.SurfaceHigh,
                    focusedTextColor = AppColors.Text,
                    unfocusedTextColor = AppColors.Text,
                    focusedLabelColor = AppColors.Accent,
                    unfocusedLabelColor = AppColors.TextDim),
                shape = RoundedCornerShape(14.dp)
            )
            Spacer(Modifier.height(20.dp))

            Button(onClick = { onConnect(host.trim()) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.Accent)) {
                Text("اتصال", fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(12.dp))

            OutlinedButton(onClick = onScan,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.Accent2)) {
                Text("📷  مسح كود QR", fontSize = 15.sp, fontWeight = FontWeight.Medium)
            }

            Spacer(Modifier.height(22.dp))
            Text(status, color = AppColors.TextDim, fontSize = 13.sp, textAlign = TextAlign.Center)
        }
    }
}

// ═════════════════════════════════════════════════════════════
enum class Mode(val label: String, val icon: String) {
    TRACKPAD("ماوس",  "🖱"),
    KEYBOARD("كيبورد", "⌨"),
    SPLIT("مقسّم",    "◫"),
    MEDIA("وسائط",   "🎵"),
    AUDIO("صوت",     "🎚")
}

@Composable
fun ControlScreen(net: NetClient, onExit: () -> Unit) {
    var mode by remember { mutableStateOf(Mode.TRACKPAD) }
    var emojiOpen by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            QuickActionsBar(net)

            Row(
                Modifier.fillMaxWidth().background(AppColors.Surface)
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Mode.entries.forEach { m ->
                    ModeTab(m.icon, m == mode) { mode = m }
                }
                Spacer(Modifier.weight(1f))
                TextButton(onClick = onExit) {
                    Text("خروج", color = AppColors.Danger, fontSize = 12.sp)
                }
            }

            Box(Modifier.weight(1f)) {
                when (mode) {
                    Mode.TRACKPAD -> TrackpadView(net)
                    Mode.KEYBOARD -> KeyboardView(net, onEmoji = { emojiOpen = true })
                    Mode.SPLIT    -> SplitView(net, onEmoji = { emojiOpen = true })
                    Mode.MEDIA    -> MediaView(net)
                    Mode.AUDIO    -> AudioControlScreen(net)
                }
            }
        }

        // ⚡ Floating Toggle FAB — يبدّل بين الأوضاع الرئيسية
        FloatingModeToggle(
            current = mode,
            onCycle = {
                mode = when (mode) {
                    Mode.TRACKPAD -> Mode.KEYBOARD
                    Mode.KEYBOARD -> Mode.SPLIT
                    Mode.SPLIT    -> Mode.TRACKPAD
                    else          -> Mode.TRACKPAD
                }
            },
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 16.dp, bottom = 90.dp)
        )

        // ⚡ Emoji Picker
        if (emojiOpen) {
            Box(Modifier.fillMaxSize().background(Color(0x88000000))) {
                Box(Modifier.align(Alignment.BottomCenter)) {
                    EmojiPicker(
                        onPick = { net.sendText(it) },
                        onClose = { emojiOpen = false }
                    )
                }
            }
        }
    }
}

@Composable
fun FloatingModeToggle(
    current: Mode,
    onCycle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    Box(
        modifier
            .size(58.dp)
            .shadow(12.dp, RoundedCornerShape(50))
            .clip(RoundedCornerShape(50))
            .background(Brush.linearGradient(listOf(AppColors.Accent, AppColors.Accent2)))
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown()
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onCycle()
                    waitForUpOrCancellation()
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(current.icon, fontSize = 22.sp, color = Color.White)
    }
}

@Composable
fun QuickActionsBar(net: NetClient) {
    Row(
        Modifier.fillMaxWidth().background(Color(0xFF0E1424))
            .padding(horizontal = 6.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        QuickBtn("⏮", AppColors.Accent2) { net.mediaKey(3) }
        QuickBtn("⏯", AppColors.Success) { net.mediaKey(1) }
        QuickBtn("⏭", AppColors.Accent2) { net.mediaKey(2) }
        Spacer(Modifier.width(4.dp))
        QuickBtn("🔉", AppColors.Warn)   { net.mediaKey(6) }
        QuickBtn("🔊", AppColors.Warn)   { net.mediaKey(5) }
        QuickBtn("🔇", AppColors.Warn)   { net.mediaKey(7) }
        Spacer(Modifier.weight(1f))
        QuickBtn("🔒", AppColors.Danger) { net.systemCommand(1) }
        QuickBtn("🌙", AppColors.Accent) { net.systemCommand(2) }
    }
}

@Composable
fun QuickBtn(icon: String, tint: Color, onClick: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    Box(
        Modifier.size(42.dp).clip(RoundedCornerShape(10.dp))
            .background(tint.copy(alpha = 0.14f))
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown()
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                    waitForUpOrCancellation()
                }
            },
        contentAlignment = Alignment.Center
    ) { Text(icon, fontSize = 18.sp, color = tint) }
}

// ═════════════════════════════════════════════════════════════
@Composable
fun MediaView(net: NetClient) {
    Box(
        Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF131A2E), Color(0xFF0A0E1A))))
    ) {
        Column(
            Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("🎵", fontSize = 90.sp)
            Spacer(Modifier.height(20.dp))
            Text("التحكم بالوسائط", fontSize = 20.sp, color = AppColors.Text, fontWeight = FontWeight.SemiBold)
            Text("تحكم كامل من مكانك", fontSize = 13.sp, color = AppColors.TextDim)
            Spacer(Modifier.height(44.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(18.dp),
                verticalAlignment = Alignment.CenterVertically) {
                BigMediaBtn("⏮", 84.dp, AppColors.SurfaceHigh) { net.mediaKey(3) }
                BigMediaBtn("⏯", 110.dp, AppColors.Accent)     { net.mediaKey(1) }
                BigMediaBtn("⏭", 84.dp, AppColors.SurfaceHigh) { net.mediaKey(2) }
            }

            Spacer(Modifier.height(40.dp))
            Text("الصوت", fontSize = 14.sp, color = AppColors.TextDim)
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically) {
                BigMediaBtn("🔉", 64.dp, AppColors.Warn) { net.mediaKey(6) }
                BigMediaBtn("🔇", 64.dp, AppColors.Danger) { net.mediaKey(7) }
                BigMediaBtn("🔊", 64.dp, AppColors.Warn) { net.mediaKey(5) }
            }
        }
    }
}

@Composable
fun BigMediaBtn(icon: String, size: Dp, tint: Color, onClick: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    Box(
        Modifier.size(size).clip(RoundedCornerShape(50))
            .background(tint.copy(alpha = 0.18f))
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown()
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                    waitForUpOrCancellation()
                }
            },
        contentAlignment = Alignment.Center
    ) { Text(icon, fontSize = (size.value * 0.4f).sp, color = tint) }
}

@Composable
fun ModeTab(icon: String, active: Boolean, onClick: () -> Unit) {
    TextButton(onClick = onClick,
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
        colors = ButtonDefaults.textButtonColors(
            containerColor = if (active) AppColors.Accent.copy(alpha = 0.18f) else Color.Transparent),
        shape = RoundedCornerShape(10.dp)) {
        Text(icon, fontSize = 18.sp,
            color = if (active) AppColors.Accent else AppColors.TextDim)
    }
}

@Composable
fun SplitView(net: NetClient, onEmoji: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        Box(Modifier.weight(1f)) { TrackpadView(net) }
        HorizontalDivider(color = AppColors.SurfaceHigh, thickness = 2.dp)
        Box(Modifier.weight(1.15f)) { KeyboardView(net, onEmoji) }
    }
}

// ═════════════════════════════════════════════════════════════
//  Trackpad v3 — مع تسريع سلس محسّن
// ═════════════════════════════════════════════════════════════
@Composable
fun TrackpadView(net: NetClient) {
    val baseSens = 2.1f
    var fracX by remember { mutableFloatStateOf(0f) }
    var fracY by remember { mutableFloatStateOf(0f) }

    Box(
        Modifier.fillMaxSize()
            .background(Brush.radialGradient(listOf(Color(0xFF16203A), Color(0xFF0A0E1A))))
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val startTime = System.currentTimeMillis()
                    var lastPos: Offset = down.position
                    var totalMove = 0f
                    var dragging = false
                    var scrollMode = false
                    var lastMoveTime = startTime

                    while (true) {
                        val ev = awaitPointerEvent()
                        val pressedCount = ev.changes.count { it.pressed }
                        val change = ev.changes.firstOrNull { it.id == down.id }
                            ?: ev.changes.firstOrNull() ?: break

                        if (pressedCount >= 2) {
                            val d = change.position - lastPos
                            net.scroll((-d.y * 0.55f).toInt())
                            lastPos = change.position
                            change.consume()
                            scrollMode = true
                        } else if (change.pressed) {
                            val now = System.currentTimeMillis()
                            val dt = (now - lastMoveTime).coerceAtLeast(1)
                            lastMoveTime = now
                            val d = change.position - lastPos
                            totalMove += d.getDistance()
                            if (totalMove > 6f) dragging = true
                            if (dragging) {
                                val speed = d.getDistance() / dt
                                val accel = (1f + speed * 0.55f).coerceIn(1f, 3.2f)
                                val sens = baseSens * accel
                                val mx = d.x * sens + fracX
                                val my = d.y * sens + fracY
                                val ix = mx.toInt()
                                val iy = my.toInt()
                                fracX = mx - ix
                                fracY = my - iy
                                if (ix != 0 || iy != 0) net.mouseMove(ix.toFloat(), iy.toFloat())
                            }
                            lastPos = change.position
                            change.consume()
                        } else {
                            val dur = System.currentTimeMillis() - startTime
                            if (!dragging && !scrollMode && dur < 240) {
                                net.mouseButton(1, true)
                                net.mouseButton(1, false)
                            }
                            break
                        }
                    }
                }
            }
    ) {
        Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("◉", fontSize = 72.sp, color = AppColors.Accent.copy(alpha = 0.14f))
            Spacer(Modifier.height(6.dp))
            Text("اسحب بإصبع للتحكم بالماوس", color = AppColors.TextDim, fontSize = 13.sp)
            Text("نقرة = كليك • إصبعان = تمرير", color = AppColors.TextDim, fontSize = 11.sp)
        }
        Row(Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ActionButton("كليك يسار", Modifier.weight(1f), AppColors.Accent) {
                net.mouseButton(1, true)
                net.mouseButton(1, false)
            }
            ActionButton("كليك يمين", Modifier.weight(1f), AppColors.Accent2) {
                net.mouseButton(2, true)
                net.mouseButton(2, false)
            }
            ActionButton("أوسط", Modifier.weight(1f), AppColors.SurfaceHigh) {
                net.mouseButton(3, true)
                net.mouseButton(3, false)
            }
        }
    }
}

@Composable
fun ActionButton(label: String, modifier: Modifier = Modifier,
                 tint: Color = AppColors.KeySpecial, onClick: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    Box(
        modifier.height(52.dp).clip(RoundedCornerShape(12.dp)).background(tint)
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown()
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                    waitForUpOrCancellation()
                }
            },
        contentAlignment = Alignment.Center
    ) { Text(label, color = AppColors.Text, fontSize = 13.sp, fontWeight = FontWeight.Medium) }
}

// ═════════════════════════════════════════════════════════════
//  Keyboard + Emoji Button
// ═════════════════════════════════════════════════════════════
data class KeySpec(val label: String, val vk: Int = 0, val weight: Float = 1f, val isModifier: Boolean = false)

private val ROW_0 = listOf(
    KeySpec("1", 0x31), KeySpec("2", 0x32), KeySpec("3", 0x33), KeySpec("4", 0x34),
    KeySpec("5", 0x35), KeySpec("6", 0x36), KeySpec("7", 0x37), KeySpec("8", 0x38),
    KeySpec("9", 0x39), KeySpec("0", 0x30)
)
private val ROW_1 = listOf(
    KeySpec("Q", 0x51), KeySpec("W", 0x57), KeySpec("E", 0x45), KeySpec("R", 0x52),
    KeySpec("T", 0x54), KeySpec("Y", 0x59), KeySpec("U", 0x55), KeySpec("I", 0x49),
    KeySpec("O", 0x4F), KeySpec("P", 0x50)
)
private val ROW_2 = listOf(
    KeySpec("A", 0x41), KeySpec("S", 0x53), KeySpec("D", 0x44), KeySpec("F", 0x46),
    KeySpec("G", 0x47), KeySpec("H", 0x48), KeySpec("J", 0x4A), KeySpec("K", 0x4B),
    KeySpec("L", 0x4C)
)
private val ROW_3 = listOf(
    KeySpec("⇧", weight = 1.3f, isModifier = true),
    KeySpec("Z", 0x5A), KeySpec("X", 0x58), KeySpec("C", 0x43), KeySpec("V", 0x56),
    KeySpec("B", 0x42), KeySpec("N", 0x4E), KeySpec("M", 0x4D),
    KeySpec("⌫", 0x08, weight = 1.3f)
)
private val ROW_4 = listOf(
    KeySpec("Esc", 0x1B),
    KeySpec("Ctrl", weight = 1.1f, isModifier = true),
    KeySpec("Alt",  weight = 1.1f, isModifier = true),
    KeySpec("Win",  weight = 1.1f, isModifier = true),
    KeySpec("Space", 0x20, weight = 3.2f),
    KeySpec("◀", 0x25), KeySpec("▲", 0x26), KeySpec("▼", 0x28), KeySpec("▶", 0x27),
    KeySpec("⏎", 0x0D, weight = 1.2f)
)
// ═════════════════════════════════════════════════════════════
//  Arabic Keyboard Data
// ═════════════════════════════════════════════════════════════
private val AR_ROW_0 = listOf("ض", "ص", "ث", "ق", "ف", "غ", "ع", "ه", "خ", "ح")
private val AR_ROW_1 = listOf("ش", "س", "ي", "ب", "ل", "ا", "ت", "ن", "م", "ك")
private val AR_ROW_2 = listOf("⇧", "ط", "ج", "د", "ذ", "ر", "ز", "و", "ظ", "⌫")
private val AR_ROW_3 = listOf("Esc", "Tab", "ة", "ى", "ء", "ؤ", "ئ", "لا", "؟", "⏎")
private val AR_ROW_4 = listOf("Ctrl", "Alt", "Win", "Space", "◀", "▲", "▼", "▶", "،", "؛")

// خريطة الحروف العربية → VK (عشان Ctrl+C وغيرها تشتغل)
private val AR_TO_VK = mapOf(
    "ض" to 0x51, "ص" to 0x57, "ث" to 0x45, "ق" to 0x52, "ف" to 0x54,
    "غ" to 0x59, "ع" to 0x55, "ه" to 0x49, "خ" to 0x4F, "ح" to 0x50,
    "ش" to 0x41, "س" to 0x53, "ي" to 0x44, "ب" to 0x46, "ل" to 0x47,
    "ا" to 0x48, "ت" to 0x4A, "ن" to 0x4B, "م" to 0x4C, "ك" to 0x3B,
    "ط" to 0x5A, "ج" to 0x58, "د" to 0x43, "ذ" to 0x56, "ر" to 0x42,
    "ز" to 0x4E, "و" to 0x4D, "ظ" to 0x2F
)

// VK للأزرار الخاصة (Esc, Tab, arrows...)
private fun vkFor(label: String): Int? = when (label) {
    "Esc"   -> 0x1B
    "Tab"   -> 0x09
    "⌫"     -> 0x08
    "⏎"     -> 0x0D
    "Space" -> 0x20
    "◀"     -> 0x25
    "▲"     -> 0x26
    "▼"     -> 0x28
    "▶"     -> 0x27
    else    -> null
}
@Composable
fun KeyboardView(net: NetClient, onEmoji: () -> Unit) {
    var isArabic by remember { mutableStateOf(false) }
    var shift by remember { mutableStateOf(false) }
    var ctrl by remember { mutableStateOf(false) }
    var alt by remember { mutableStateOf(false) }
    var win by remember { mutableStateOf(false) }

    // إرسال VK + modifiers (للإنجليزي وللاختصارات)
    fun sendVkWithMods(vk: Int) {
        if (shift) net.key(0x10, true)
        if (ctrl)  net.key(0x11, true)
        if (alt)   net.key(0x12, true)
        if (win)   net.key(0x5B, true)
        net.key(vk, true)
        net.key(vk, false)
        if (win)   net.key(0x5B, false)
        if (alt)   net.key(0x12, false)
        if (ctrl)  net.key(0x11, false)
        if (shift) net.key(0x10, false)
        if (shift) shift = false
    }

    // معالجة مفتاح عربي أو زر
    fun handleArKey(label: String) {
        when (label) {
            "⇧"   -> shift = !shift
            "Ctrl" -> ctrl = !ctrl
            "Alt"  -> alt = !alt
            "Win"  -> win = !win
            else -> {
                val vk = vkFor(label)
                when {
                    vk != null -> sendVkWithMods(vk) // أزرار خاصة
                    ctrl || alt || win -> {
                        // اختصار — استخدم الحرف اللاتيني المقابل
                        val shortcutVk = AR_TO_VK[label]
                        if (shortcutVk != null) sendVkWithMods(shortcutVk)
                        else net.sendText(label)
                    }
                    else -> net.sendText(label) // حرف عربي Unicode
                }
                if (shift) shift = false
            }
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(AppColors.Bg)
            .padding(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // ─────────── الشريط العلوي ───────────
        Row(
            Modifier.fillMaxWidth().padding(vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // زر تبديل اللغة
            Box(
                Modifier
                    .height(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(AppColors.Accent.copy(alpha = 0.2f))
                    .clickable { isArabic = !isArabic }
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (isArabic) "ع | EN" else "EN | ع",
                    color = AppColors.Accent,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Text(
                if (isArabic) "كيبورد عربي" else "English Keyboard",
                color = AppColors.TextDim,
                fontSize = 11.sp
            )

            // زر الإيموجي
            Box(
                Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(AppColors.KeySpecial)
                    .clickable { onEmoji() },
                contentAlignment = Alignment.Center
            ) {
                Text("😊", fontSize = 16.sp)
            }
        }

        // ─────────── الصفوف ───────────
        if (isArabic) {
            listOf(AR_ROW_0, AR_ROW_1, AR_ROW_2, AR_ROW_3, AR_ROW_4).forEach { row ->
                ArKeyRow(row, Modifier.weight(1f), shift, ctrl, alt, win, ::handleArKey)
            }
        } else {
            listOf(ROW_0, ROW_1, ROW_2, ROW_3).forEach { row ->
                KeyRow(row, Modifier.weight(1f), shift, ctrl, alt, win,
                    onSend = { sendVkWithMods(it) },
                    onModifier = { name ->
                        when (name) {
                            "⇧" -> shift = !shift
                            "Ctrl" -> ctrl = !ctrl
                            "Alt"  -> alt = !alt
                            "Win"  -> win = !win
                        }
                    })
            }
            // صف 4 بدون زر إيموجي (انتقل للشريط العلوي)
            Row(
                Modifier.weight(1f).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                ROW_4.forEach { k ->
                    if (k.isModifier) {
                        val active = when (k.label) {
                            "Ctrl" -> ctrl; "Alt" -> alt; "Win" -> win; else -> false
                        }
                        KeyCap(k.label, Modifier.weight(k.weight), active, true) {
                            when (k.label) {
                                "Ctrl" -> ctrl = !ctrl
                                "Alt"  -> alt = !alt
                                "Win"  -> win = !win
                            }
                        }
                    } else {
                        KeyCap(k.label, Modifier.weight(k.weight), false, false) {
                            sendVkWithMods(k.vk)
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun ArKeyRow(
    keys: List<String>,
    modifier: Modifier,
    shift: Boolean, ctrl: Boolean, alt: Boolean, win: Boolean,
    onKey: (String) -> Unit
) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        keys.forEach { key ->
            val weight = when (key) {
                "Space" -> 3.2f
                "⇧", "⌫" -> 1.3f
                "لا" -> 1.3f
                "⏎" -> 1.2f
                "Ctrl", "Alt", "Win" -> 1.1f
                "Esc", "Tab" -> 1.1f
                else -> 1f
            }
            val isMod = key in setOf("⇧", "Ctrl", "Alt", "Win")
            val isSpecial = isMod || key in setOf(
                "Esc", "Tab", "⌫", "⏎", "◀", "▲", "▼", "▶", "Space"
            )
            val active = when (key) {
                "⇧"   -> shift
                "Ctrl" -> ctrl
                "Alt"  -> alt
                "Win"  -> win
                else   -> false
            }
            KeyCap(key, Modifier.weight(weight), active, isSpecial) { onKey(key) }
        }
    }
}
@Composable
fun KeyRow(keys: List<KeySpec>, modifier: Modifier = Modifier,
           shift: Boolean, ctrl: Boolean, alt: Boolean, win: Boolean,
           onSend: (Int) -> Unit, onModifier: (String) -> Unit) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        keys.forEach { k ->
            if (k.isModifier) {
                val active = when (k.label) {
                    "⇧" -> shift
                    "Ctrl" -> ctrl
                    "Alt"  -> alt
                    "Win"  -> win
                    else   -> false
                }
                KeyCap(k.label, Modifier.weight(k.weight), active, true) { onModifier(k.label) }
            } else KeyCap(k.label, Modifier.weight(k.weight), false, false) { onSend(k.vk) }
        }
    }
}

@Composable
fun KeyCap(label: String, modifier: Modifier = Modifier,
           active: Boolean = false, isSpecial: Boolean = false,
           onClick: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    Box(
        modifier.fillMaxHeight().clip(RoundedCornerShape(10.dp))
            .background(when {
                active -> AppColors.Accent
                isSpecial -> AppColors.KeySpecial
                else -> AppColors.Key
            })
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown()
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                    waitForUpOrCancellation()
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = AppColors.Text,
            fontSize = if (label.length > 2) 12.sp else 16.sp,
            fontWeight = FontWeight.Medium)
    }
}

