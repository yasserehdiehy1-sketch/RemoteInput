using System.Net;
using System.Net.Sockets;
using System.Net.WebSockets;
using System.Drawing;
using System.Windows.Forms;
using QRCoder;

namespace RemoteInputServer;

internal static class Program
{
    private const int Port = 8765;
    private static NotifyIcon? _tray;

    [STAThread]
    private static void Main()
    {
        ServicePointManager.Expect100Continue = false;
        ServicePointManager.UseNagleAlgorithm = false;

        ApplicationConfiguration.Initialize();

        _tray = new NotifyIcon
        {
            Icon = SystemIcons.Application,
            Visible = true,
            Text = "RemoteInput Server v3.0"
        };

        var menu = new ContextMenuStrip();
        menu.Items.Add("📷 عرض كود QR", null, (_, _) => ShowQrWindow());
        menu.Items.Add("📋 نسخ عنوان الاتصال", null, (_, _) => CopyAddress());
        menu.Items.Add(new ToolStripSeparator());
        menu.Items.Add("🚪 خروج", null, (_, _) => { _tray.Visible = false; Application.Exit(); });
        _tray.ContextMenuStrip = menu;
        _tray.DoubleClick += (_, _) => ShowQrWindow();

        _tray.ShowBalloonTip(3000, "RemoteInput",
            $"السيرفر يعمل على المنفذ {Port}\nانقر مرتين لعرض QR", ToolTipIcon.Info);

        // بدء السيرفر في Thread منفصل
        Task.Run(StartServerAsync);

        Application.Run();
    }

    private static async Task StartServerAsync()
    {
        var listener = new HttpListener();
        listener.Prefixes.Add($"http://+:{Port}/");
        try { listener.Start(); }
        catch (Exception ex)
        {
            _tray?.ShowBalloonTip(5000, "خطأ", "فشل: " + ex.Message, ToolTipIcon.Error);
            return;
        }

        while (true)
        {
            var ctx = await listener.GetContextAsync();
            if (ctx.Request.IsWebSocketRequest)
            {
                var wsc = await ctx.AcceptWebSocketAsync(null);
                _ = Task.Run(() => HandleClient(wsc.WebSocket));
            }
            else
            {
                ctx.Response.StatusCode = 400;
                ctx.Response.Close();
            }
        }
    }

    private static async Task HandleClient(WebSocket ws)
    {
        var buffer = new byte[32 * 1024]; // ⚡ مخزن أكبر للـ audio المستقبلي
        try
        {
            while (ws.State == WebSocketState.Open)
            {
                var result = await ws.ReceiveAsync(new ArraySegment<byte>(buffer), CancellationToken.None);
                if (result.MessageType == WebSocketMessageType.Close) break;
                if (result.MessageType == WebSocketMessageType.Binary && result.Count > 0)
                    InputDispatcher.Process(buffer, result.Count);
            }
        }
        catch { }
    }

    private static string GetLocalIp()
    {
        foreach (var ip in Dns.GetHostAddresses(Dns.GetHostName()))
            if (ip.AddressFamily == AddressFamily.InterNetwork)
                return ip.ToString();
        return "127.0.0.1";
    }

    private static void ShowQrWindow()
    {
        var url = $"ws://{GetLocalIp()}:{Port}";
        var qrGen = new QRCodeGenerator();
        var data = qrGen.CreateQrCode(url, QRCodeGenerator.ECCLevel.M);
        var qr = new QRCode(data);
        using var bmp = qr.GetGraphic(12, Color.Black, Color.White, true);

        var frm = new Form
        {
            Text = "امسح الكود من التطبيق",
            Size = new Size(460, 560),
            StartPosition = FormStartPosition.CenterScreen,
            BackColor = Color.FromArgb(10, 14, 26),
            FormBorderStyle = FormBorderStyle.FixedSingle,
            MaximizeBox = false
        };

        var pic = new PictureBox
        {
            Image = bmp,
            SizeMode = PictureBoxSizeMode.Zoom,
            Dock = DockStyle.Fill,
            Padding = new Padding(30)
        };
        var lbl = new Label
        {
            Text = url,
            ForeColor = Color.FromArgb(79, 140, 255),
            Font = new Font("Segoe UI", 14, FontStyle.Bold),
            TextAlign = ContentAlignment.MiddleCenter,
            Dock = DockStyle.Bottom,
            Height = 60
        };
        frm.Controls.Add(pic);
        frm.Controls.Add(lbl);
        frm.ShowDialog();
    }

    private static void CopyAddress()
    {
        var url = $"ws://{GetLocalIp()}:{Port}";
        Clipboard.SetText(url);
        _tray?.ShowBalloonTip(2000, "تم النسخ", url, ToolTipIcon.Info);
    }
}