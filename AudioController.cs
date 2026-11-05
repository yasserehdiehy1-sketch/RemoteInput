using System.Runtime.InteropServices;
using NAudio.CoreAudioApi;

namespace RemoteInputServer;

internal static class AudioController
{
    public static void Handle(byte cmd)
    {
        switch (cmd)
        {
            case 1: ToggleMute();       break;  // Mute PC
            case 2: VolumeUp();         break;
            case 3: VolumeDown();       break;
            case 4: NextOutputDevice(); break;  // تبديل جهاز الإخراج
            case 5: GetState();         break;  // قراءة الحالة (غير مطبّق إرسال عكسي هنا)
        }
    }

    private static MMDevice? DefaultDevice()
    {
        try
        {
            var en = new MMDeviceEnumerator();
            return en.GetDefaultAudioEndpoint(DataFlow.Render, Role.Multimedia);
        }
        catch { return null; }
    }

    public static void ToggleMute()
    {
        var d = DefaultDevice();
        if (d == null) return;
        d.AudioEndpointVolume.Mute = !d.AudioEndpointVolume.Mute;
    }

    public static void VolumeUp()
    {
        var d = DefaultDevice();
        if (d == null) return;
        var v = d.AudioEndpointVolume.MasterVolumeLevelScalar;
        d.AudioEndpointVolume.MasterVolumeLevelScalar = Math.Min(1f, v + 0.05f);
    }

    public static void VolumeDown()
    {
        var d = DefaultDevice();
        if (d == null) return;
        var v = d.AudioEndpointVolume.MasterVolumeLevelScalar;
        d.AudioEndpointVolume.MasterVolumeLevelScalar = Math.Max(0f, v - 0.05f);
    }

    // ⚡ التبديل بين أجهزة الإخراج (سماعات → شاشة → إلخ)
    public static void NextOutputDevice()
    {
        try
        {
            var en = new MMDeviceEnumerator();
            var devices = en.EnumerateAudioEndPoints(DataFlow.Render, DeviceState.Active).ToList();
            if (devices.Count < 2) return;

            var current = en.GetDefaultAudioEndpoint(DataFlow.Render, Role.Multimedia);
            int idx = devices.FindIndex(d => d.ID == current.ID);
            int next = (idx + 1) % devices.Count;

            PolicyConfig.SetDefaultEndpoint(devices[next].ID, ERole.eConsole);
            PolicyConfig.SetDefaultEndpoint(devices[next].ID, ERole.eMultimedia);
            PolicyConfig.SetDefaultEndpoint(devices[next].ID, ERole.eCommunications);
        }
        catch { }
    }

    public static void GetState() { /* لتوسعة مستقبلية */ }
}

// ═════════════════════════════════════════════════════════════
//  IPolicyConfig — COM interop لتبديل الجهاز الافتراضي
// ═════════════════════════════════════════════════════════════
internal enum ERole { eConsole, eMultimedia, eCommunications }

[ComImport, Guid("870AF99C-171D-4F9E-AF0D-E63DF40C2BC9")]
internal class CPolicyConfigClient { }

[ComImport, Guid("F8679F50-850A-41CF-9C72-430F290290C8"),
 InterfaceType(ComInterfaceType.InterfaceIsIUnknown)]
internal interface IPolicyConfig
{
    int GetMixFormat(string pszDeviceName, IntPtr ppFormat);
    int GetDeviceFormat(string pszDeviceName, bool bDefault, IntPtr ppFormat);
    int ResetDeviceFormat(string pszDeviceName);
    int SetDeviceFormat(string pszDeviceName, IntPtr pEndpointFormat, IntPtr mixFormat);
    int GetProcessingPeriod(string pszDeviceName, bool bDefault, IntPtr pmftDefaultPeriod, IntPtr pmftMinimumPeriod);
    int SetProcessingPeriod(string pszDeviceName, IntPtr pmftPeriod);
    int GetShareMode(string pszDeviceName, IntPtr pMode);
    int SetShareMode(string pszDeviceName, IntPtr mode);
    int GetPropertyValue(string pszDeviceName, bool bFxStore, IntPtr key, IntPtr pv);
    int SetPropertyValue(string pszDeviceName, bool bFxStore, IntPtr key, IntPtr pv);
    int SetDefaultEndpoint(string pszDeviceName, ERole role);
    int SetEndpointVisibility(string pszDeviceName, bool bVisible);
}

internal static class PolicyConfig
{
    public static void SetDefaultEndpoint(string deviceId, ERole role)
    {
        var pc = (IPolicyConfig)new CPolicyConfigClient();
        pc.SetDefaultEndpoint(deviceId, role);
        Marshal.ReleaseComObject(pc);
    }
}