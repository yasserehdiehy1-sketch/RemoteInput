# RemoteInput
A Wi-Fi-based remote control application for PC.
# 🎯 RemoteInput v3.0 — Full Features Documentation

---

## 📱 Overview

**RemoteInput** is an application that transforms your Android phone or tablet into a **Mouse + Keyboard + Media Remote + Audio Control Center** for your PC (Windows 10/11), operating entirely over **Wi-Fi** — no cables, no Bluetooth, no hassle.

**The Goal:** Fully control your PC from your bed, couch, or anywhere in the house.

---

# 🎨 1. UI/UX & Design Features

| Feature | Description |
|--------|-------|
| **Dark Neon Theme** | `Midnight Blue` background + neon accents (blue/purple) — eye-friendly at night |
| **4 Semantic Colors** | 🟢 Green = Active • 🔴 Red = Danger/Alert • 🟡 Yellow = Audio • 🔵 Blue = Interaction |
| **Radial Gradient Trackpad** | Soft circular gradient to highlight the touch interaction area |
| **Rounded Corners** | 10-20dp corners for a modern, sleek appearance |
| **Haptic Feedback** | Subtle vibrations on every click for a tactile, realistic feel |
| **FAB Shadow** | 12dp elevation shadow adding a smooth 3D depth effect |
| **Keep Screen On** | Utilizes `FLAG_KEEP_SCREEN_ON` to prevent dimming during active use |

---

# 🖱 2. Mouse Mode (Trackpad) Features

| Feature | Description |
|--------|-------|
| **Single-Finger Control** | Intuitive cursor movement across your PC screen |
| **Tap to Click** | Quick tap (< 240ms) triggers a standard left-click |
| **Continuous Drag** | Smooth drag-and-drop mechanism for long-distance scrolling |
| **Two-Finger Scroll** | Seamless vertical scrolling up and down pages |
| **Smart Acceleration Curve** | Slow movement = high precision • Fast flick = dynamic acceleration up to 3.2x |
| **Sub-pixel Smoothing** | Eliminates jittery jumps for ultra-smooth tracking |
| **Adjustable Sensitivity** | Custom base sensitivity configuration `baseSens = 2.1f` |
| **3 Dedicated Buttons** | Large bottom buttons mapped to Left, Right, and Middle clicks |

---

# ⌨ 3. Keyboard Features

| Feature | Description |
|--------|-------|
| **Full 5-Row Layout** | Numbers + Letters + Symbols + Control keys included |
| **Sticky Modifiers** | Shift, Ctrl, Alt, and Win keys lock until a secondary key is pressed |
| **One-Tap Shift** | Auto-releases after typing a single uppercase character |
| **Quick Shortcuts** | Reliable consecutive tap handling for `Ctrl+C` / `Ctrl+V` |
| **Directional Arrows** | Dedicated layout for ◀ ▲ ▼ ▶ navigation |
| **Special Keys** | Native support for Esc, Tab, Enter, Backspace, and Space |
| **Emoji Toggle 😊** | Dedicated switch at the bottom row for quick expressions |
| **Extended Key Mapping** | Properly passes Delete, Win, and arrow key arguments |

---

# 😊 4. Emoji Selector Features

| Feature | Description |
|--------|-------|
| **Dedicated Key** | Instant access directly from the main keyboard layout |
| **128 Emojis** | Categorized collection containing faces, hearts, hands, and symbols |
| **8-Column Grid** | Well-organized visual panel optimized for rapid choice |
| **Unicode Encoding** | Relies on `KEYEVENTF_UNICODE` for universal standard compatibility |
| **Multilingual Input** | Successfully passes any standard Unicode sequence (including Arabic script) |
| **Dismiss on Tap** | Translucent darkened background overlay that closes upon clicking |

---

# ◫ 5. Split-Screen Mode Features

| Feature | Description |
|--------|-------|
| **Top Half Trackpad** | Live mouse navigation zone |
| **Bottom Half Keyboard** | Full text input panel layout |
| **Visual Separator** | Clear 2dp contrast divider line splitting the interface |
| **Smart Proportion Ratio** | 1 : 1.15 allocation (making the keyboard slightly larger for comfortable typing) |
| **Simultaneous Input** | Point, click, and type concurrently without missing a frame |

---

# 🎵 6. Media Mode Features

| Feature | Description |
|--------|-------|
| **Immersive Layout** | Gradient aesthetics embedded with a bold 🎵 visual identity anchor |
| **Massive Circular Buttons** | High-visibility keys ⏮ ⏯ ⏭ sized between 84-110dp |
| **Audio Controls** | Quick accessibility for 🔉 🔇 🔊 mappings |
| **Robust Haptic Cues** | Specialized long-press vibrations mapped across media triggers |
| **Broad App Compatibility** | Works out-of-the-box with YouTube, Netflix, Spotify, VLC, and more |

---

# 🎚 7. Audio Control Center Features

| Feature | Description |
|--------|-------|
| **3 Categorized Cards** | PC Volume • Tablet Volume • Playback Profile management |
| **PC Mute Control** | Interacts cleanly via `AudioEndpointVolume.Mute` APIs |
| **Precision Increments** | Steps volume up or down accurately at 5% intervals |
| **Output Device Toggling** | Instantly cycles output: Speakers → Monitor → Headset |
| **Local Mute Toggle** | Direct silence override for the local tablet interface |
| **Dynamic Color Indicators** | 🟢 Green = Enabled • 🔴 Red = Muted/Disabled |

---

# ⚡ 8. Quick Actions Bar

Stays pinned globally at the top of the interface — functional across **all modes**:

| Button | Functionality |
|------|---------|
| ⏮ | Previous Track |
| ⏯ | Play / Pause |
| ⏭ | Next Track |
| 🔉 | Volume Down |
| 🔊 | Volume Up |
| 🔇 | Mute Audio |
| 🔒 | Lock Computer |
| 🌙 | System Sleep |

---

# 🔄 9. Floating Action Button (FAB Switcher)

| Feature | Description |
|--------|-------|
| **Floating Layout** | Anchored perfectly at the bottom-right for thumb accessibility |
| **Vibrant Gradient** | Transitions smoothly from energetic Blue to Deep Purple |
| **12dp Depth Shadow** | Creates clear separation from the background layout |
| **Cycle Navigation** | Seamlessly loop modes: Mouse → Keyboard → Split → Mouse |
| **Adaptive Icons** | Changes shape dynamically: 🖱 ⌨ ◫ reflecting active context |
| **Firm Vibrations** | Strong long-press feedback confirming system mode changes |

---

# 📷 10. Connection & Pairing Features

| Feature | Description |
|--------|-------|
| **WebSocket Binary** | Highly compact payloads resulting in faster response times |
| **QR Code Pairing** | Scan a code to connect instantly, eliminating manual IP typing |
| **Auto-Generated QR** | Displays immediately on double-clicking the system tray desktop app icon |
| **Persistent Storage** | Powered by `DataStore` to auto-fill the last used IP on launch |
| **One-Tap Link Copy** | Instantly copy server connection details from the tray menu |
| **Network Address Discovery**| Scans and logs all accessible local host addresses clearly |

---

# 🖥 11. Server System Tray Features

| Feature | Description |
|--------|-------|
| **Tray Operation** | Runs invisibly in the taskbar without annoying terminal windows |
| **Context Menu** | Fully interactive right-click menu setup |
| **Balloon Notifications** | Real-time status updates on connection success or failures |
| **Auto Hide** | Tucks away smoothly to save real estate on your desktop |
| **Admin Privilege Check** | Validates privileges to guarantee Windows `SendInput` hooks work safely |
| **Graceful Shutdown** | Safe port closing routines via the tray exit prompt |

---

# 🚀 12. Speed & Latency Optimizations

| Optimization | Technical Impact |
|--------|-------|
| **`UseNagleAlgorithm = false`** | Disables packet buffering for instant, real-time message transmission |
| **`Expect100Continue = false`** | Accelerates initial handshakes by removing 50ms of overhead |
| **32KB Dedicated Buffer** | Easily processes large Emoji and multi-byte text character chunks |
| **Reused `ByteBuffer`** | Drastically minimizes garbage collection pauses for smooth frame rates |
| **`Task.Run` Client Threads** | Ensures asynchronous traffic handling so the server loop never freezes |
| **WebSocket Binary Frame** | Shrinks payloads to a mere 5-8 bytes compared to 80+ bytes for JSON |
| **Ultra-Low Latency** | Clocking in at under 15ms over standard 5GHz local wireless networks |
| **20-Second KeepAlive Ping** | Sends background health checks to ensure the socket stays open |

---

# 🎯 13. System Control Integration

| Feature | Description |
|--------|-------|
| **Lock Workstation** | Triggers native `LockWorkStation()` API securely |
| **Put PC to Sleep** | Leverages `SetSuspendState()` for fast low-power standby |
| **System Shutdown** | Runs `shutdown /s /t 3` with a safe delay window |
| **System Reboot** | Runs `shutdown /r /t 3` to restart the host cleanly |
| **Abort Shutdown** | Emergency override via `shutdown /a` command string |
| **Extended Keys** | Flawless forwarding rules for hardware specific triggers like Delete |

---

# 🌐 14. Network Framework

| Feature | Description |
|--------|-------|
| **Pure Wi-Fi Architecture**| Zero reliance on physical cables or flaky Bluetooth adapters |
| **Local Area LAN Scope** | Works entirely offline without requiring active internet access |
| **Whole-Home Range** | Same local subnet = total control from any room in the house |
| **Explicit Host Feedback** | Exposes host server IP metrics immediately on setup |
| **Auto Reconnect Loop** | Built-in `retryOnConnectionFailure = true` logic for connection drops |

---

# 📊 Feature Summary Dashboard

| Metric Category | Value Metric Count |
|------|:---:|
| Primary UI Modes | **5** (Mouse, Keyboard, Split, Media, Audio) |
| Quick Bar Buttons | **8** |
| Bundled Emojis | **128** |
| Total Keyboard Rows | **5** |
| Independent Audio Controls | **6** |
| Remote OS Commands | **5** |
| Optimization Profiles | **8+** |
| Haptic Feedback Profiles | **3 Varieties** |
| System Theme Palette | **11 Colors** |

---

# 🎬 Before vs After Experience Breakdown

**Before:** Want to skip a video or change a song? You have to get out of bed → walk to your desk → adjust it → walk back.

**After RemoteInput v3.0:**
1. Open the app on your phone or tablet.
2. Scan the QR code for instant pairing.
3. Stream YouTube while relaxing comfortably in bed.
4. Tweak volumes directly from the global Quick Bar panel.
5. Type a search query using the full keyboard layout.
6. Drop a quick expression response 😊.

💎 The Verdict

RemoteInput v3.0 transforms your phone or tablet into a fully-featured, professional command center for your PC — packing a mouse, keyboard, media remote, audio panel, and system remote with sub-15ms latency, all from the comfort of your bed.
