using System.Runtime.InteropServices;
using System.Text;

namespace RemoteInputServer;

internal static class InputDispatcher
{
    public static void Process(byte[] data, int len)
    {
        if (len < 1) return;

        switch (data[0])
        {
            case 0x01 when len >= 5:
                MouseMove(BitConverter.ToInt16(data, 1), BitConverter.ToInt16(data, 3));
                break;

            case 0x02 when len >= 3:
                MouseButton(data[1], data[2] == 1);
                break;

            case 0x03 when len >= 3:
                MouseScroll(BitConverter.ToInt16(data, 1));
                break;

            case 0x04 when len >= 4:
                Key(BitConverter.ToUInt16(data, 1), data[3] == 1);
                break;

            case 0x05 when len >= 2:
                MediaKey(data[1]);
                break;

            case 0x06 when len >= 2:
                SystemCommand(data[1]);
                break;

            case 0x07 when len >= 2:                          // ⚡ Unicode Text (Emoji)
                SendUnicodeText(Encoding.UTF8.GetString(data, 1, len - 1));
                break;

            case 0x08 when len >= 2:                          // ⚡ Audio Command
                AudioController.Handle(data[1]);
                break;
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  P/Invoke
    // ═══════════════════════════════════════════════════════════
    [DllImport("user32.dll", SetLastError = true)]
    private static extern uint SendInput(uint nInputs, INPUT[] pInputs, int cbSize);

    [DllImport("user32.dll", SetLastError = false)]
    private static extern bool LockWorkStation();

    [DllImport("powrprof.dll", CharSet = CharSet.Auto, ExactSpelling = true)]
    private static extern bool SetSuspendState(bool hibernate, bool forceCritical, bool disableWakeEvent);

    [StructLayout(LayoutKind.Sequential)]
    private struct INPUT { public uint type; public InputUnion U; }

    [StructLayout(LayoutKind.Explicit)]
    private struct InputUnion
    {
        [FieldOffset(0)] public MOUSEINPUT mi;
        [FieldOffset(0)] public KEYBDINPUT ki;
    }

    [StructLayout(LayoutKind.Sequential)]
    private struct MOUSEINPUT
    {
        public int dx, dy;
        public uint mouseData, dwFlags, time;
        public IntPtr dwExtraInfo;
    }

    [StructLayout(LayoutKind.Sequential)]
    private struct KEYBDINPUT
    {
        public ushort wVk, wScan;
        public uint dwFlags, time;
        public IntPtr dwExtraInfo;
    }

    private const uint INPUT_MOUSE = 0, INPUT_KEYBOARD = 1;

    private const uint MOUSEEVENTF_MOVE       = 0x0001;
    private const uint MOUSEEVENTF_LEFTDOWN   = 0x0002;
    private const uint MOUSEEVENTF_LEFTUP     = 0x0004;
    private const uint MOUSEEVENTF_RIGHTDOWN  = 0x0008;
    private const uint MOUSEEVENTF_RIGHTUP    = 0x0010;
    private const uint MOUSEEVENTF_MIDDLEDOWN = 0x0020;
    private const uint MOUSEEVENTF_MIDDLEUP   = 0x0040;
    private const uint MOUSEEVENTF_WHEEL      = 0x0800;

    private const uint KEYEVENTF_KEYUP        = 0x0002;
    private const uint KEYEVENTF_EXTENDEDKEY  = 0x0001;
    private const uint KEYEVENTF_UNICODE      = 0x0004;

    private static readonly INPUT[] _buf = new INPUT[1];

    private static void Send(INPUT inp)
    {
        _buf[0] = inp;
        SendInput(1, _buf, Marshal.SizeOf<INPUT>());
    }

    public static void MouseMove(int dx, int dy)
        => Send(new INPUT { type = INPUT_MOUSE,
            U = new InputUnion { mi = new MOUSEINPUT { dx = dx, dy = dy, dwFlags = MOUSEEVENTF_MOVE } } });

    public static void MouseButton(byte button, bool down)
    {
        uint flag = button switch
        {
            1 => down ? MOUSEEVENTF_LEFTDOWN   : MOUSEEVENTF_LEFTUP,
            2 => down ? MOUSEEVENTF_RIGHTDOWN  : MOUSEEVENTF_RIGHTUP,
            3 => down ? MOUSEEVENTF_MIDDLEDOWN : MOUSEEVENTF_MIDDLEUP,
            _ => 0
        };
        if (flag == 0) return;
        Send(new INPUT { type = INPUT_MOUSE, U = new InputUnion { mi = new MOUSEINPUT { dwFlags = flag } } });
    }

    public static void MouseScroll(int amount)
        => Send(new INPUT { type = INPUT_MOUSE,
            U = new InputUnion { mi = new MOUSEINPUT { mouseData = (uint)amount, dwFlags = MOUSEEVENTF_WHEEL } } });

    public static void Key(ushort vk, bool down)
    {
        uint flags = down ? 0 : KEYEVENTF_KEYUP;
        if (vk is 0x25 or 0x26 or 0x27 or 0x28 or 0x2E or 0x24 or 0x23
              or 0x21 or 0x22 or 0x2D or 0x5B or 0x5C)
            flags |= KEYEVENTF_EXTENDEDKEY;

        Send(new INPUT { type = INPUT_KEYBOARD,
            U = new InputUnion { ki = new KEYBDINPUT { wVk = vk, dwFlags = flags } } });
    }

    // ⚡ Unicode input — للإيموجي والعربية وأي حرف
    public static void SendUnicodeText(string text)
    {
        foreach (var ch in text)
        {
            Send(new INPUT { type = INPUT_KEYBOARD,
                U = new InputUnion { ki = new KEYBDINPUT { wScan = ch, dwFlags = KEYEVENTF_UNICODE } } });
            Send(new INPUT { type = INPUT_KEYBOARD,
                U = new InputUnion { ki = new KEYBDINPUT { wScan = ch, dwFlags = KEYEVENTF_UNICODE | KEYEVENTF_KEYUP } } });
        }
    }

    private static void MediaKey(byte code)
    {
        ushort vk = code switch
        {
            1 => 0xB3, 2 => 0xB0, 3 => 0xB1, 4 => 0xB2,
            5 => 0xAF, 6 => 0xAE, 7 => 0xAD, _ => 0
        };
        if (vk == 0) return;
        Key(vk, true); Key(vk, false);
    }

    private static void SystemCommand(byte cmd)
    {
        switch (cmd)
        {
            case 1: LockWorkStation(); break;
            case 2: SetSuspendState(false, false, false); break;
            case 3: Run("shutdown", "/s /t 3"); break;
            case 4: Run("shutdown", "/r /t 3"); break;
            case 5: Run("shutdown", "/a"); break;
        }
    }

    private static void Run(string file, string args)
        => System.Diagnostics.Process.Start(new System.Diagnostics.ProcessStartInfo
        {
            FileName = file, Arguments = args, CreateNoWindow = true, UseShellExecute = false
        });
}