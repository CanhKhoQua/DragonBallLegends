using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Linq;
using System.Net;
using System.Net.NetworkInformation;
using System.Net.Sockets;
using System.Security.Cryptography;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Media;
using System.Windows.Threading;
using Microsoft.Win32;

namespace DragonBallLegends.Launcher;

public sealed class MainWindow : Window
{
    private sealed record FileIssue(string RelativePath, string Reason);

    private sealed record VerifySummary(int Checked, List<FileIssue> Issues);

    private readonly string root;
    private readonly string runtimeDir;
    private readonly string logsDir;
    private readonly string serverPidFile;
    private readonly string dbPidFile;
    private readonly string activeWorldFile;
    private readonly DispatcherTimer statusTimer;

    private readonly List<Control> busyControls = new();

    private TextBlock dbBadge = null!;
    private TextBlock serverBadge = null!;
    private TextBlock worldBadge = null!;
    private TextBlock javaBadge = null!;
    private TextBlock modeBadge = null!;
    private TextBlock lanInfo = null!;
    private TextBox logBox = null!;
    private ProgressBar progress = null!;
    private RadioButton singleMode = null!;
    private RadioButton hostMode = null!;
    private RadioButton joinMode = null!;
    private TextBox friendAddressBox = null!;
    private ComboBox worldCombo = null!;
    private Button playButton = null!;
    private Button stopButton = null!;

    public MainWindow()
    {
        root = FindRoot(AppContext.BaseDirectory);
        runtimeDir = Path.Combine(root, "runtime");
        logsDir = Path.Combine(root, "logs");
        serverPidFile = Path.Combine(runtimeDir, "offline-server.pid");
        dbPidFile = Path.Combine(runtimeDir, "offline-db.pid");
        activeWorldFile = Path.Combine(runtimeDir, "offline-world.txt");

        Title = "DragonBallLegends Launcher";
        Width = 920;
        Height = 620;
        MinWidth = 860;
        MinHeight = 560;
        WindowStartupLocation = WindowStartupLocation.CenterScreen;
        Background = new SolidColorBrush(Color.FromRgb(245, 247, 251));

        BuildUi();
        RefreshStatus();
        Dispatcher.BeginInvoke(() => FirstRunCheck(), DispatcherPriority.ApplicationIdle);

        statusTimer = new DispatcherTimer { Interval = TimeSpan.FromSeconds(2) };
        statusTimer.Tick += (_, _) => RefreshStatus();
        statusTimer.Start();
    }

    private string SavesRoot => Path.Combine(root, "saves");

    private string SelectedWorldName => SanitizeWorldName(worldCombo?.SelectedItem as string ?? "DefaultWorld");

    private string WorldDir => Path.Combine(SavesRoot, SelectedWorldName);

    private string DbDataDir => Path.Combine(WorldDir, "dbdata");

    private static string FindRoot(string start)
    {
        var dir = new DirectoryInfo(start);
        while (dir != null)
        {
            if (Directory.Exists(Path.Combine(dir.FullName, "server"))
                && Directory.Exists(Path.Combine(dir.FullName, "database")))
            {
                return dir.FullName;
            }
            dir = dir.Parent;
        }
        return start;
    }

    private void BuildUi()
    {
        var shell = new Grid { Margin = new Thickness(24) };
        shell.RowDefinitions.Add(new RowDefinition { Height = GridLength.Auto });
        shell.RowDefinitions.Add(new RowDefinition { Height = GridLength.Auto });
        shell.RowDefinitions.Add(new RowDefinition { Height = GridLength.Auto });
        shell.RowDefinitions.Add(new RowDefinition { Height = new GridLength(1, GridUnitType.Star) });
        Content = shell;

        var header = new Grid { Margin = new Thickness(0, 0, 0, 18) };
        header.ColumnDefinitions.Add(new ColumnDefinition { Width = new GridLength(1, GridUnitType.Star) });
        header.ColumnDefinitions.Add(new ColumnDefinition { Width = GridLength.Auto });
        shell.Children.Add(header);

        var titleStack = new StackPanel();
        header.Children.Add(titleStack);
        titleStack.Children.Add(new TextBlock
        {
            Text = "DragonBallLegends",
            FontSize = 32,
            FontWeight = FontWeights.Bold,
            Foreground = new SolidColorBrush(Color.FromRgb(24, 30, 42))
        });
        titleStack.Children.Add(new TextBlock
        {
            Text = "Offline world launcher",
            FontSize = 14,
            Foreground = new SolidColorBrush(Color.FromRgb(91, 99, 116))
        });

        modeBadge = Badge("Ready", Color.FromRgb(66, 96, 184));
        modeBadge.VerticalAlignment = VerticalAlignment.Center;
        Grid.SetColumn(modeBadge, 1);
        header.Children.Add(modeBadge);

        var card = Card();
        Grid.SetRow(card, 1);
        shell.Children.Add(card);

        var controls = new Grid { Margin = new Thickness(18) };
        controls.ColumnDefinitions.Add(new ColumnDefinition { Width = new GridLength(1, GridUnitType.Star) });
        controls.ColumnDefinitions.Add(new ColumnDefinition { Width = GridLength.Auto });
        card.Child = controls;

        var left = new StackPanel();
        controls.Children.Add(left);

        left.Children.Add(new TextBlock
        {
            Text = "Mode",
            FontWeight = FontWeights.SemiBold,
            FontSize = 13,
            Foreground = new SolidColorBrush(Color.FromRgb(68, 75, 90))
        });

        var modeRow = new StackPanel { Orientation = Orientation.Horizontal, Margin = new Thickness(0, 8, 0, 10) };
        left.Children.Add(modeRow);
        singleMode = ModeRadio("Single Mode", true);
        hostMode = ModeRadio("Host LAN", false);
        joinMode = ModeRadio("Join Friend", false);
        modeRow.Children.Add(singleMode);
        modeRow.Children.Add(hostMode);
        modeRow.Children.Add(joinMode);

        left.Children.Add(new TextBlock
        {
            Text = "World",
            FontWeight = FontWeights.SemiBold,
            FontSize = 13,
            Foreground = new SolidColorBrush(Color.FromRgb(68, 75, 90))
        });

        var worldRow = new StackPanel { Orientation = Orientation.Horizontal, Margin = new Thickness(0, 8, 0, 10) };
        left.Children.Add(worldRow);
        worldCombo = new ComboBox
        {
            Width = 220,
            Height = 32,
            Margin = new Thickness(0, 0, 8, 0)
        };
        worldCombo.SelectionChanged += (_, _) => RefreshStatus();
        worldRow.Children.Add(worldCombo);
        worldRow.Children.Add(SmallButton("New", CreateWorld));
        worldRow.Children.Add(SmallButton("Copy", async () => await DuplicateWorldAsync()));
        LoadWorlds();

        var friendRow = new StackPanel { Orientation = Orientation.Horizontal, Margin = new Thickness(0, 0, 0, 10) };
        left.Children.Add(friendRow);
        friendRow.Children.Add(new TextBlock
        {
            Text = "Friend:",
            VerticalAlignment = VerticalAlignment.Center,
            FontWeight = FontWeights.SemiBold,
            Foreground = new SolidColorBrush(Color.FromRgb(68, 75, 90)),
            Margin = new Thickness(0, 0, 8, 0)
        });
        friendAddressBox = new TextBox
        {
            Width = 220,
            Height = 30,
            Text = "127.0.0.1:14445",
            VerticalContentAlignment = VerticalAlignment.Center
        };
        friendRow.Children.Add(friendAddressBox);

        lanInfo = new TextBlock
        {
            Text = "LAN IP: " + string.Join(", ", GetLanAddresses().DefaultIfEmpty("not detected")),
            Foreground = new SolidColorBrush(Color.FromRgb(91, 99, 116)),
            FontSize = 13
        };
        left.Children.Add(lanInfo);

        var actions = new StackPanel { Orientation = Orientation.Horizontal, VerticalAlignment = VerticalAlignment.Center };
        Grid.SetColumn(actions, 1);
        controls.Children.Add(actions);

        playButton = PrimaryButton("Play");
        playButton.Click += async (_, _) => await StartSelectedModeAsync();
        actions.Children.Add(playButton);

        stopButton = SecondaryButton("Stop");
        stopButton.Click += async (_, _) => await StopAsync();
        actions.Children.Add(stopButton);
        busyControls.Add(playButton);
        busyControls.Add(stopButton);
        busyControls.Add(worldCombo);
        busyControls.Add(singleMode);
        busyControls.Add(hostMode);
        busyControls.Add(joinMode);
        busyControls.Add(friendAddressBox);

        var statusGrid = new Grid { Margin = new Thickness(0, 16, 0, 16) };
        statusGrid.ColumnDefinitions.Add(new ColumnDefinition());
        statusGrid.ColumnDefinitions.Add(new ColumnDefinition());
        statusGrid.ColumnDefinitions.Add(new ColumnDefinition());
        statusGrid.ColumnDefinitions.Add(new ColumnDefinition());
        Grid.SetRow(statusGrid, 2);
        shell.Children.Add(statusGrid);

        dbBadge = StatusPanel(statusGrid, 0, "Database");
        serverBadge = StatusPanel(statusGrid, 1, "Server");
        javaBadge = StatusPanel(statusGrid, 2, "Java");
        worldBadge = StatusPanel(statusGrid, 3, "World");

        var body = new Grid();
        body.ColumnDefinitions.Add(new ColumnDefinition { Width = new GridLength(1, GridUnitType.Star) });
        body.ColumnDefinitions.Add(new ColumnDefinition { Width = new GridLength(220) });
        Grid.SetRow(body, 3);
        shell.Children.Add(body);

        var logCard = Card();
        body.Children.Add(logCard);
        var logStack = new DockPanel { Margin = new Thickness(16) };
        logCard.Child = logStack;

        progress = new ProgressBar
        {
            Height = 6,
            IsIndeterminate = false,
            Minimum = 0,
            Maximum = 100,
            Margin = new Thickness(0, 0, 0, 12)
        };
        DockPanel.SetDock(progress, Dock.Top);
        logStack.Children.Add(progress);

        logBox = new TextBox
        {
            IsReadOnly = true,
            TextWrapping = TextWrapping.Wrap,
            VerticalScrollBarVisibility = ScrollBarVisibility.Auto,
            BorderThickness = new Thickness(0),
            Background = Brushes.Transparent,
            FontFamily = new FontFamily("Consolas"),
            FontSize = 12
        };
        logStack.Children.Add(logBox);

        var sideScroll = new ScrollViewer
        {
            Margin = new Thickness(16, 0, 0, 0),
            VerticalScrollBarVisibility = ScrollBarVisibility.Auto
        };
        Grid.SetColumn(sideScroll, 1);
        body.Children.Add(sideScroll);
        var side = new StackPanel();
        sideScroll.Content = side;
        side.Children.Add(SideButton("Backup World", async () => await BackupWorldAsync()));
        side.Children.Add(SideButton("Restore World", async () => await RestoreWorldAsync()));
        side.Children.Add(SideButton("Export World", async () => await ExportWorldAsync()));
        side.Children.Add(SideButton("Rename World", async () => await RenameWorldAsync()));
        side.Children.Add(SideButton("Reset World", async () => await ResetWorldAsync()));
        side.Children.Add(SideButton("Delete World", async () => await DeleteWorldAsync()));
        side.Children.Add(SideButton("Verify Files", async () => await VerifyFilesAsync()));
        side.Children.Add(SideButton("Diagnostics", () => RunDiagnostics()));
        side.Children.Add(SideButton("Repair Config", () => RepairConfig()));
        side.Children.Add(SideButton("Copy Report", () => CopyErrorReport()));
        side.Children.Add(SideButton("Open Save", () => OpenFolder(Path.Combine(root, "saves"))));
        side.Children.Add(SideButton("Open Logs", () => OpenFolder(logsDir)));
        side.Children.Add(SideButton("Open Folder", () => OpenFolder(root)));
    }

    private static Border Card() => new()
    {
        Background = Brushes.White,
        CornerRadius = new CornerRadius(10),
        BorderBrush = new SolidColorBrush(Color.FromRgb(224, 229, 238)),
        BorderThickness = new Thickness(1)
    };

    private static TextBlock Badge(string text, Color color) => new()
    {
        Text = text,
        Foreground = Brushes.White,
        Background = new SolidColorBrush(color),
        FontWeight = FontWeights.SemiBold,
        Padding = new Thickness(12, 6, 12, 6)
    };

    private static RadioButton ModeRadio(string text, bool isChecked) => new()
    {
        Content = text,
        IsChecked = isChecked,
        GroupName = "mode",
        FontSize = 14,
        FontWeight = FontWeights.SemiBold,
        Margin = new Thickness(0, 0, 18, 0)
    };

    private static Button PrimaryButton(string text) => new()
    {
        Content = text,
        Width = 120,
        Height = 42,
        Margin = new Thickness(8, 0, 0, 0),
        FontSize = 14,
        FontWeight = FontWeights.Bold,
        Background = new SolidColorBrush(Color.FromRgb(45, 104, 220)),
        Foreground = Brushes.White
    };

    private static Button SecondaryButton(string text) => new()
    {
        Content = text,
        Width = 90,
        Height = 42,
        Margin = new Thickness(8, 0, 0, 0),
        FontSize = 14,
        FontWeight = FontWeights.SemiBold
    };

    private TextBlock StatusPanel(Grid parent, int column, string label)
    {
        var panel = Card();
        panel.Margin = new Thickness(column == 0 ? 0 : 8, 0, column == 3 ? 0 : 8, 0);
        Grid.SetColumn(panel, column);
        parent.Children.Add(panel);

        var stack = new StackPanel { Margin = new Thickness(16, 12, 16, 12) };
        panel.Child = stack;
        stack.Children.Add(new TextBlock
        {
            Text = label,
            Foreground = new SolidColorBrush(Color.FromRgb(91, 99, 116)),
            FontSize = 12
        });
        var value = new TextBlock
        {
            Text = "Checking",
            FontSize = 16,
            FontWeight = FontWeights.Bold,
            Foreground = new SolidColorBrush(Color.FromRgb(24, 30, 42)),
            Margin = new Thickness(0, 4, 0, 0)
        };
        stack.Children.Add(value);
        return value;
    }

    private Button SideButton(string text, Func<Task> action)
    {
        var button = CreateSideButton(text);
        busyControls.Add(button);
        button.Click += async (_, _) =>
        {
            try
            {
                await action();
            }
            catch (Exception ex)
            {
                Log("ERROR: " + ex.Message);
                MessageBox.Show(ex.Message, "DragonBallLegends Launcher", MessageBoxButton.OK, MessageBoxImage.Error);
            }
            finally
            {
                RefreshStatus();
            }
        };
        return button;
    }

    private Button SideButton(string text, Action action)
    {
        var button = CreateSideButton(text);
        busyControls.Add(button);
        button.Click += (_, _) =>
        {
            try
            {
                action();
            }
            catch (Exception ex)
            {
                Log("ERROR: " + ex.Message);
                MessageBox.Show(ex.Message, "DragonBallLegends Launcher", MessageBoxButton.OK, MessageBoxImage.Error);
            }
            finally
            {
                RefreshStatus();
            }
        };
        return button;
    }

    private Button SmallButton(string text, Action action)
    {
        var button = new Button
        {
            Content = text,
            Height = 32,
            MinWidth = 72,
            Margin = new Thickness(0, 0, 8, 0),
            Padding = new Thickness(10, 0, 10, 0),
            FontWeight = FontWeights.SemiBold
        };
        busyControls.Add(button);
        button.Click += (_, _) => action();
        return button;
    }

    private static Button CreateSideButton(string text) => new()
    {
        Content = text,
        Height = 38,
        Margin = new Thickness(0, 0, 0, 10),
        FontWeight = FontWeights.SemiBold
    };

    private async Task StartSelectedModeAsync()
    {
        var host = hostMode.IsChecked == true;
        var join = joinMode.IsChecked == true;
        SetBusy(true);
        progress.IsIndeterminate = true;
        ClearLog();
        try
        {
            Log(join ? "Joining friend..." : host ? "Starting Host LAN..." : "Starting Single Mode...");
            Directory.CreateDirectory(runtimeDir);
            Directory.CreateDirectory(logsDir);
            Directory.CreateDirectory(WorldDir);

            if (join)
            {
                var endpoint = ParseEndpoint(friendAddressBox.Text, 14445);
                SaveClientServerSelection("Friend Room", endpoint.Host, endpoint.Port);
                await StartClientAsync();
                modeBadge.Text = "Join Friend";
                modeBadge.Background = new SolidColorBrush(Color.FromRgb(40, 142, 82));
                Log($"Client set to {endpoint.Host}:{endpoint.Port}.");
                return;
            }

            if (!host)
            {
                ResetClientProfile();
            }

            CopyConfig(host);
            await EnsureDatabaseAsync();
            await StartServerAsync();
            SaveClientServerSelection(host ? "Host LAN" : "Single Mode", "127.0.0.1", 14445);
            await StartClientAsync();

            modeBadge.Text = host ? "Host LAN running" : "Single Mode running";
            modeBadge.Background = new SolidColorBrush(Color.FromRgb(40, 142, 82));
            Log("Ready.");
        }
        catch (Exception ex)
        {
            modeBadge.Text = "Error";
            modeBadge.Background = new SolidColorBrush(Color.FromRgb(190, 49, 68));
            Log("ERROR: " + ex.Message);
            MessageBox.Show(ex.Message, "DragonBallLegends Launcher", MessageBoxButton.OK, MessageBoxImage.Error);
        }
        finally
        {
            progress.IsIndeterminate = false;
            SetBusy(false);
            RefreshStatus();
        }
    }

    private async Task EnsureDatabaseAsync()
    {
        var tools = FindMysqlTools();
        MigrateLegacySave();
        if (!Directory.Exists(DbDataDir))
        {
            Log("Creating world database: " + SelectedWorldName);
            var mariadbTemplate = Path.Combine(runtimeDir, "mariadb", "data_template");
            var mysqlTemplate = Path.Combine(runtimeDir, "mysql", "data_template");
            if (Directory.Exists(mariadbTemplate))
            {
                CopyDirectory(mariadbTemplate, DbDataDir);
            }
            else if (Directory.Exists(mysqlTemplate))
            {
                CopyDirectory(mysqlTemplate, DbDataDir);
            }
            else
            {
                Directory.CreateDirectory(DbDataDir);
                await RunProcessAsync(tools.Server, $"--initialize-insecure --datadir=\"{DbDataDir}\"", runtimeDir, "mysql-init.log", false);
            }
        }

        if (await IsPortOpenAsync(3307))
        {
            if (!HasLivePid(dbPidFile, "mysqld", "mariadbd"))
            {
                throw new InvalidOperationException("Port 3307 is already in use by another process. Close it or change the offline DB port.");
            }
            var activeWorld = ReadActiveWorldName();
            if (!activeWorld.Equals(SelectedWorldName, StringComparison.OrdinalIgnoreCase))
            {
                throw new InvalidOperationException($"Database is already running for {activeWorld}. Stop it before starting {SelectedWorldName}.");
            }
            Log("Database already running.");
        }
        else
        {
            Log("Starting database...");
            var dbLog = Path.Combine(logsDir, "database.log");
            var args = $"--no-defaults --datadir=\"{DbDataDir}\" --port=3307 --bind-address=127.0.0.1 --pid-file=\"{dbPidFile}\" --console";
            var process = StartLoggedProcess(tools.Server, args, runtimeDir, dbLog);
            File.WriteAllText(dbPidFile, process.Id.ToString(), Encoding.ASCII);
            File.WriteAllText(activeWorldFile, SelectedWorldName, Encoding.UTF8);
        }

        await WaitForPortAsync(3307, "Database did not start on port 3307.", 40);

        var marker = Path.Combine(WorldDir, ".db_initialized");
        if (!File.Exists(marker))
        {
            Log("Importing offline seed...");
            await RunProcessAsync(tools.Client, "--host=127.0.0.1 --port=3307 --user=root --execute=\"CREATE DATABASE IF NOT EXISTS nroserver CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;\"", root, "mysql-create-db.log", true);
            var seed = Path.Combine(root, "database", "offline_seed.sql").Replace("\\", "/");
            await RunProcessAsync(tools.Client, $"--host=127.0.0.1 --port=3307 --user=root --default-character-set=utf8mb4 --database=nroserver --execute=\"source {seed}\"", root, "mysql-import-seed.log", true);
            File.WriteAllText(marker, "initialized", Encoding.ASCII);
        }

        await ApplyDatabaseMigrationsAsync(tools);
    }

    private async Task ApplyDatabaseMigrationsAsync((string Server, string Client, string? Admin) tools)
    {
        var migrationsDir = Path.Combine(root, "database", "migrations");
        if (!Directory.Exists(migrationsDir))
        {
            return;
        }

        var migrations = Directory.GetFiles(migrationsDir, "*.sql")
            .OrderBy(Path.GetFileName, StringComparer.OrdinalIgnoreCase)
            .ToList();
        if (migrations.Count == 0)
        {
            return;
        }

        Directory.CreateDirectory(Path.Combine(WorldDir, ".migrations"));
        await RunProcessAsync(
            tools.Client,
            "--host=127.0.0.1 --port=3307 --user=root --database=nroserver --execute=\"CREATE TABLE IF NOT EXISTS offline_schema_migrations (filename VARCHAR(255) NOT NULL PRIMARY KEY, checksum CHAR(64) NOT NULL, applied_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;\"",
            root,
            "mysql-migrations.log",
            true);

        foreach (var migration in migrations)
        {
            var name = Path.GetFileName(migration);
            var checksum = Sha256File(migration);
            var marker = Path.Combine(WorldDir, ".migrations", name + ".applied");
            if (File.Exists(marker))
            {
                var appliedChecksum = File.ReadAllText(marker).Trim();
                if (appliedChecksum.Equals("batch-fallback", StringComparison.OrdinalIgnoreCase))
                {
                    continue;
                }
                if (!appliedChecksum.Equals(checksum, StringComparison.OrdinalIgnoreCase))
                {
                    throw new InvalidOperationException($"Migration {name} was changed after it was applied to {SelectedWorldName}. Create a new migration file instead of editing an applied migration.");
                }
                continue;
            }

            Log("Applying DB migration: " + name);
            var sql = migration.Replace("\\", "/");
            await RunProcessAsync(
                tools.Client,
                $"--host=127.0.0.1 --port=3307 --user=root --default-character-set=utf8mb4 --database=nroserver --execute=\"source {sql}\"",
                root,
                "mysql-migrations.log",
                true);
            var escapedName = SqlEscape(name);
            await RunProcessAsync(
                tools.Client,
                $"--host=127.0.0.1 --port=3307 --user=root --database=nroserver --execute=\"REPLACE INTO offline_schema_migrations (filename, checksum) VALUES ('{escapedName}', '{checksum}');\"",
                root,
                "mysql-migrations.log",
                true);
            File.WriteAllText(marker, checksum, Encoding.ASCII);
        }
    }

    private async Task StartServerAsync()
    {
        if (await IsPortOpenAsync(14445))
        {
            if (!HasLivePid(serverPidFile, "java", "javaw"))
            {
                throw new InvalidOperationException("Port 14445 is already in use by another server. Stop it before using this launcher.");
            }
            Log("Server already running.");
            return;
        }

        var jar = Path.Combine(root, "server", "Server.jar");
        if (!File.Exists(jar))
        {
            throw new FileNotFoundException("Missing server\\Server.jar.");
        }

        Log("Starting server...");
        var serverLog = Path.Combine(logsDir, "server.log");
        var java = FindJavaExe();
        if (java == null)
        {
            throw new FileNotFoundException("Java runtime not found. Put Java in runtime\\java or install Java and add it to PATH.");
        }
        var process = StartLoggedProcess(java, "-server -Dfile.encoding=UTF-8 -jar Server.jar", Path.Combine(root, "server"), serverLog);
        File.WriteAllText(serverPidFile, process.Id.ToString(), Encoding.ASCII);
        await WaitForPortAsync(14445, "Game server did not start on port 14445.", 60);
    }

    private Task StartClientAsync()
    {
        var clientDir = Path.Combine(root, "client");
        if (!Directory.Exists(clientDir))
        {
            throw new DirectoryNotFoundException("Missing client folder.");
        }

        var exe = FindClientExe();

        if (exe == null)
        {
            throw new FileNotFoundException("No client exe found in client folder.");
        }

        Log("Launching client...");
        Process.Start(new ProcessStartInfo
        {
            FileName = exe,
            WorkingDirectory = clientDir,
            UseShellExecute = true
        });
        return Task.CompletedTask;
    }

    private async Task StopAsync()
    {
        SetBusy(true);
        progress.IsIndeterminate = true;
        try
        {
            Log("Stopping offline processes...");
            var tools = FindMysqlTools(required: false);
            if (tools.Admin != null && HasLivePid(dbPidFile, "mysqld", "mariadbd"))
            {
                await RunProcessAsync(tools.Admin, "--host=127.0.0.1 --port=3307 --user=root shutdown", root, "mysql-shutdown.log", false, 10);
            }
            else if (await IsPortOpenAsync(3307))
            {
                Log("Database port is open, but it is not owned by this launcher. Leaving it running.");
            }
            KillPidFile(serverPidFile, "server", "java", "javaw");
            KillPidFile(dbPidFile, "database", "mysqld", "mariadbd");
            TryDelete(activeWorldFile);
            modeBadge.Text = "Stopped";
            modeBadge.Background = new SolidColorBrush(Color.FromRgb(91, 99, 116));
            Log("Stopped.");
        }
        catch (Exception ex)
        {
            Log("Stop warning: " + ex.Message);
        }
        finally
        {
            progress.IsIndeterminate = false;
            SetBusy(false);
            RefreshStatus();
        }
    }

    private async Task BackupWorldAsync()
    {
        var worldName = SelectedWorldName;
        var worldDir = WorldDir;
        var dbDataDir = DbDataDir;
        if (!Directory.Exists(dbDataDir))
        {
            MessageBox.Show($"{worldName} has not been created yet.", "DragonBallLegends Launcher", MessageBoxButton.OK, MessageBoxImage.Information);
            return;
        }
        if (await IsPortOpenAsync(3307))
        {
            MessageBox.Show("Stop the server/database before backing up.", "DragonBallLegends Launcher", MessageBoxButton.OK, MessageBoxImage.Warning);
            return;
        }

        var backupDir = Path.Combine(root, "saves", "backups");
        Directory.CreateDirectory(backupDir);
        var backup = Path.Combine(backupDir, $"{worldName}-{DateTime.Now:yyyyMMdd-HHmmss}.zip");
        Log("Creating backup...");
        await Task.Run(() => System.IO.Compression.ZipFile.CreateFromDirectory(worldDir, backup));
        Log("Backup created: " + backup);
        MessageBox.Show("Backup created:\n" + backup, "DragonBallLegends Launcher", MessageBoxButton.OK, MessageBoxImage.Information);
    }

    private async Task RestoreWorldAsync()
    {
        var worldName = SelectedWorldName;
        var worldDir = WorldDir;
        if (await IsPortOpenAsync(3307))
        {
            MessageBox.Show("Stop the server/database before restoring a world.", "DragonBallLegends Launcher", MessageBoxButton.OK, MessageBoxImage.Warning);
            return;
        }

        var dialog = new OpenFileDialog
        {
            Title = "Restore World Backup",
            Filter = "World backup (*.zip)|*.zip",
            InitialDirectory = Directory.Exists(Path.Combine(SavesRoot, "backups")) ? Path.Combine(SavesRoot, "backups") : SavesRoot
        };
        if (dialog.ShowDialog(this) != true)
        {
            return;
        }

        var result = MessageBox.Show($"Restore backup into {worldName}? Existing data in this world will be replaced.", "Restore World", MessageBoxButton.YesNo, MessageBoxImage.Warning);
        if (result != MessageBoxResult.Yes)
        {
            return;
        }

        SetBusy(true);
        progress.IsIndeterminate = true;
        try
        {
            Log("Restoring world from: " + dialog.FileName);
            await Task.Run(() =>
            {
                if (Directory.Exists(worldDir))
                {
                    Directory.Delete(worldDir, true);
                }
                SafeExtractZip(dialog.FileName, worldDir);
            });
            Log("World restored: " + worldName);
        }
        finally
        {
            progress.IsIndeterminate = false;
            SetBusy(false);
            RefreshStatus();
        }
    }

    private async Task ExportWorldAsync()
    {
        var worldName = SelectedWorldName;
        var worldDir = WorldDir;
        if (!Directory.Exists(worldDir))
        {
            MessageBox.Show("The selected world has not been created yet.", "DragonBallLegends Launcher", MessageBoxButton.OK, MessageBoxImage.Information);
            return;
        }
        if (await IsPortOpenAsync(3307))
        {
            MessageBox.Show("Stop the server/database before exporting a world.", "DragonBallLegends Launcher", MessageBoxButton.OK, MessageBoxImage.Warning);
            return;
        }

        var dialog = new SaveFileDialog
        {
            Title = "Export World",
            Filter = "World backup (*.zip)|*.zip",
            FileName = $"{worldName}-{DateTime.Now:yyyyMMdd-HHmmss}.zip"
        };
        if (dialog.ShowDialog(this) != true)
        {
            return;
        }

        SetBusy(true);
        progress.IsIndeterminate = true;
        try
        {
            Log("Exporting world...");
            await Task.Run(() =>
            {
                if (File.Exists(dialog.FileName))
                {
                    File.Delete(dialog.FileName);
                }
                System.IO.Compression.ZipFile.CreateFromDirectory(worldDir, dialog.FileName);
            });
            Log("World exported: " + dialog.FileName);
        }
        finally
        {
            progress.IsIndeterminate = false;
            SetBusy(false);
            RefreshStatus();
        }
    }

    private async Task RenameWorldAsync()
    {
        if (await IsPortOpenAsync(3307))
        {
            MessageBox.Show("Stop the server/database before renaming a world.", "DragonBallLegends Launcher", MessageBoxButton.OK, MessageBoxImage.Warning);
            return;
        }
        if (!Directory.Exists(WorldDir))
        {
            MessageBox.Show("The selected world has not been created yet.", "DragonBallLegends Launcher", MessageBoxButton.OK, MessageBoxImage.Information);
            return;
        }

        var oldName = SelectedWorldName;
        var oldDir = WorldDir;
        var newName = PromptWorldName("Rename World", "New world name:", oldName);
        if (newName == null)
        {
            return;
        }
        newName = SanitizeWorldName(newName);
        if (oldName.Equals(newName, StringComparison.OrdinalIgnoreCase))
        {
            return;
        }
        var dest = Path.Combine(SavesRoot, newName);
        if (Directory.Exists(dest))
        {
            MessageBox.Show("That world already exists.", "DragonBallLegends Launcher", MessageBoxButton.OK, MessageBoxImage.Information);
            return;
        }

        Directory.Move(oldDir, dest);
        LoadWorlds(newName);
        Log($"World renamed: {oldName} -> {newName}");
        RefreshStatus();
    }

    private async Task ResetWorldAsync()
    {
        var worldName = SelectedWorldName;
        var worldDir = WorldDir;
        var result = MessageBox.Show($"Delete {SelectedWorldName} save? Account/player data in this world will be reset.", "Reset World", MessageBoxButton.YesNo, MessageBoxImage.Warning);
        if (result != MessageBoxResult.Yes)
        {
            return;
        }
        await StopAsync();
        try
        {
            if (Directory.Exists(worldDir))
            {
                Directory.Delete(worldDir, true);
            }
        }
        catch (IOException ex)
        {
            throw new IOException("Could not reset the selected world because some database files are still locked. Close the game/server and try again.", ex);
        }
        Log($"{worldName} reset. Start Single Mode to import a fresh seed.");
        LoadWorlds(worldName);
        RefreshStatus();
    }

    private void LoadWorlds(string? preferred = null)
    {
        Directory.CreateDirectory(SavesRoot);
        var selected = SanitizeWorldName(preferred ?? worldCombo.SelectedItem as string ?? "DefaultWorld");
        var worlds = Directory.GetDirectories(SavesRoot)
            .Select(Path.GetFileName)
            .Where(name => !string.IsNullOrWhiteSpace(name))
            .Select(name => name!)
            .Where(name => !name.Equals("backups", StringComparison.OrdinalIgnoreCase))
            .Distinct(StringComparer.OrdinalIgnoreCase)
            .OrderBy(name => name.Equals("DefaultWorld", StringComparison.OrdinalIgnoreCase) ? 0 : 1)
            .ThenBy(name => name, StringComparer.OrdinalIgnoreCase)
            .ToList();
        if (!worlds.Any(name => name.Equals("DefaultWorld", StringComparison.OrdinalIgnoreCase)))
        {
            worlds.Insert(0, "DefaultWorld");
        }

        worldCombo.Items.Clear();
        foreach (var world in worlds)
        {
            worldCombo.Items.Add(world);
        }

        worldCombo.SelectedItem = worlds.FirstOrDefault(name => name.Equals(selected, StringComparison.OrdinalIgnoreCase))
            ?? worlds.FirstOrDefault(name => name.Equals("DefaultWorld", StringComparison.OrdinalIgnoreCase))
            ?? "DefaultWorld";
    }

    private void CreateWorld()
    {
        var name = PromptWorldName("New World", "World name:", "NewWorld");
        if (name == null)
        {
            return;
        }
        name = SanitizeWorldName(name);
        var path = Path.Combine(SavesRoot, name);
        if (Directory.Exists(path))
        {
            MessageBox.Show("That world already exists.", "DragonBallLegends Launcher", MessageBoxButton.OK, MessageBoxImage.Information);
            return;
        }
        Directory.CreateDirectory(path);
        LoadWorlds(name);
        Log("World created: " + name);
    }

    private async Task DuplicateWorldAsync()
    {
        if (await IsPortOpenAsync(3307))
        {
            MessageBox.Show("Stop the server/database before duplicating a world.", "DragonBallLegends Launcher", MessageBoxButton.OK, MessageBoxImage.Warning);
            return;
        }
        if (!Directory.Exists(WorldDir))
        {
            MessageBox.Show("The selected world has not been created yet.", "DragonBallLegends Launcher", MessageBoxButton.OK, MessageBoxImage.Information);
            return;
        }

        var sourceName = SelectedWorldName;
        var name = PromptWorldName("Duplicate World", "New world name:", sourceName + " Copy");
        if (name == null)
        {
            return;
        }
        name = SanitizeWorldName(name);
        var dest = Path.Combine(SavesRoot, name);
        if (Directory.Exists(dest))
        {
            MessageBox.Show("That world already exists.", "DragonBallLegends Launcher", MessageBoxButton.OK, MessageBoxImage.Information);
            return;
        }

        SetBusy(true);
        progress.IsIndeterminate = true;
        try
        {
            var source = WorldDir;
            Log($"Duplicating {sourceName} to {name}...");
            await Task.Run(() => CopyDirectory(source, dest));
            LoadWorlds(name);
            Log("World duplicated: " + name);
        }
        finally
        {
            progress.IsIndeterminate = false;
            SetBusy(false);
            RefreshStatus();
        }
    }

    private async Task DeleteWorldAsync()
    {
        var worldName = SelectedWorldName;
        var worldDir = WorldDir;
        var result = MessageBox.Show($"Delete {worldName}? Account/player data in this world will be removed.", "Delete World", MessageBoxButton.YesNo, MessageBoxImage.Warning);
        if (result != MessageBoxResult.Yes)
        {
            return;
        }

        await StopAsync();
        try
        {
            if (Directory.Exists(worldDir))
            {
                Directory.Delete(worldDir, true);
            }
        }
        catch (IOException ex)
        {
            throw new IOException("Could not delete the selected world because some database files are still locked. Close the game/server and try again.", ex);
        }
        LoadWorlds("DefaultWorld");
        Log("World deleted: " + worldName);
        RefreshStatus();
    }

    private string? PromptWorldName(string title, string label, string defaultValue)
    {
        var dialog = new Window
        {
            Title = title,
            Width = 360,
            Height = 160,
            ResizeMode = ResizeMode.NoResize,
            WindowStartupLocation = WindowStartupLocation.CenterOwner,
            Owner = this,
            Background = new SolidColorBrush(Color.FromRgb(245, 247, 251))
        };
        var panel = new StackPanel { Margin = new Thickness(18) };
        dialog.Content = panel;
        panel.Children.Add(new TextBlock
        {
            Text = label,
            FontWeight = FontWeights.SemiBold,
            Margin = new Thickness(0, 0, 0, 8)
        });
        var input = new TextBox
        {
            Text = defaultValue,
            Height = 30,
            Margin = new Thickness(0, 0, 0, 14)
        };
        panel.Children.Add(input);
        var row = new StackPanel
        {
            Orientation = Orientation.Horizontal,
            HorizontalAlignment = HorizontalAlignment.Right
        };
        panel.Children.Add(row);
        var ok = new Button { Content = "OK", Width = 76, Height = 30, Margin = new Thickness(0, 0, 8, 0), IsDefault = true };
        var cancel = new Button { Content = "Cancel", Width = 76, Height = 30, IsCancel = true };
        row.Children.Add(ok);
        row.Children.Add(cancel);
        ok.Click += (_, _) => dialog.DialogResult = true;
        input.SelectAll();
        input.Focus();

        if (dialog.ShowDialog() != true)
        {
            return null;
        }

        var value = SanitizeWorldName(input.Text);
        return string.IsNullOrWhiteSpace(value) ? null : value;
    }

    private static string SanitizeWorldName(string name)
    {
        var invalid = Path.GetInvalidFileNameChars();
        var builder = new StringBuilder();
        foreach (var ch in name.Trim())
        {
            if (invalid.Contains(ch))
            {
                continue;
            }
            if (char.IsLetterOrDigit(ch) || ch == ' ' || ch == '-' || ch == '_')
            {
                builder.Append(ch);
            }
            if (builder.Length >= 48)
            {
                break;
            }
        }
        var value = builder.ToString().Trim();
        return string.IsNullOrWhiteSpace(value) ? "DefaultWorld" : value;
    }

    private void RunDiagnostics()
    {
        Log("Running diagnostics...");
        Log("Root: " + root);
        LogBuildInfo();
        Log(File.Exists(Path.Combine(root, "server", "Server.jar")) ? "OK server\\Server.jar" : "MISSING server\\Server.jar");
        Log(File.Exists(Path.Combine(root, "database", "offline_seed.sql")) ? "OK database\\offline_seed.sql" : "MISSING database\\offline_seed.sql");
        Log(Directory.Exists(Path.Combine(root, "client")) ? "OK client folder" : "MISSING client folder");
        Log(FindClientExe() != null ? "OK client executable" : "MISSING client executable");

        var java = FindJavaExe();
        Log(java != null ? "OK Java: " + java : "MISSING Java runtime");

        try
        {
            var tools = FindMysqlTools(required: false);
            Log(File.Exists(tools.Server) ? "OK database server: " + tools.Server : "MISSING mysqld/mariadbd");
            Log(File.Exists(tools.Client) ? "OK mysql client: " + tools.Client : "MISSING mysql.exe");
        }
        catch (Exception ex)
        {
            Log("Database runtime warning: " + ex.Message);
        }

        Log(File.Exists(Path.Combine(root, "config", "config.single.properties")) ? "OK single config" : "MISSING single config");
        Log(File.Exists(Path.Combine(root, "config", "config.host-lan.properties")) ? "OK host config" : "MISSING host config");
        var migrationsDir = Path.Combine(root, "database", "migrations");
        var migrationCount = Directory.Exists(migrationsDir) ? Directory.GetFiles(migrationsDir, "*.sql").Length : 0;
        Log($"DB migrations: {migrationCount}");
        Log(Directory.Exists(DbDataDir) ? $"OK world exists: {SelectedWorldName}" : $"{SelectedWorldName} will be created on first play");
        Log("Port 3307: " + (IsPortOpenAsync(3307).GetAwaiter().GetResult() ? "open" : "closed"));
        Log("Port 14445: " + (IsPortOpenAsync(14445).GetAwaiter().GetResult() ? "open" : "closed"));
        Log("Diagnostics complete.");
    }

    private async Task VerifyFilesAsync()
    {
        var manifest = Path.Combine(root, "install_manifest.tsv");
        if (!File.Exists(manifest))
        {
            MessageBox.Show("install_manifest.tsv is missing. Rebuild the offline pack to enable file verification.", "DragonBallLegends Launcher", MessageBoxButton.OK, MessageBoxImage.Information);
            return;
        }

        SetBusy(true);
        progress.IsIndeterminate = true;
        try
        {
            Log("Verifying installed files...");
            var summary = await Task.Run(() => VerifyInstallManifest(manifest));
            Log($"Verify complete: {summary.Checked} checked, {summary.Issues.Count} issue(s).");
            foreach (var issue in summary.Issues.Take(40))
            {
                Log($"{issue.Reason}: {issue.RelativePath}");
            }
            if (summary.Issues.Count > 40)
            {
                Log($"More issues hidden: {summary.Issues.Count - 40}");
            }

            var message = summary.Issues.Count == 0
                ? "All install files verified."
                : $"{summary.Issues.Count} file issue(s) found. Use Copy Report when asking for help.";
            MessageBox.Show(message, "DragonBallLegends Launcher", MessageBoxButton.OK, summary.Issues.Count == 0 ? MessageBoxImage.Information : MessageBoxImage.Warning);
        }
        finally
        {
            progress.IsIndeterminate = false;
            SetBusy(false);
            RefreshStatus();
        }
    }

    private void FirstRunCheck()
    {
        var missing = new List<string>();
        if (!File.Exists(Path.Combine(root, "server", "Server.jar"))) missing.Add("server\\Server.jar");
        if (!File.Exists(Path.Combine(root, "database", "offline_seed.sql"))) missing.Add("database\\offline_seed.sql");
        if (FindClientExe() == null) missing.Add("client executable");
        if (FindJavaExe() == null) missing.Add("Java runtime");
        try
        {
            FindMysqlTools();
        }
        catch
        {
            missing.Add("portable database runtime");
        }

        if (missing.Count == 0)
        {
            Log("First-run check passed.");
            return;
        }

        Log("First-run check found missing components: " + string.Join(", ", missing));
        RunDiagnostics();
        MessageBox.Show("Some required game files are missing. Check the launcher log for details.", "DragonBallLegends Launcher", MessageBoxButton.OK, MessageBoxImage.Warning);
    }

    private void RepairConfig()
    {
        Directory.CreateDirectory(runtimeDir);
        Directory.CreateDirectory(logsDir);
        Directory.CreateDirectory(WorldDir);
        Directory.CreateDirectory(Path.Combine(root, "saves", "backups"));
        CopyConfig(hostMode.IsChecked == true);
        Log("Repair complete: folders created and selected mode config applied.");
    }

    private void CopyErrorReport()
    {
        var builder = new StringBuilder();
        builder.AppendLine("DragonBallLegends Launcher Report");
        builder.AppendLine("Time: " + DateTime.Now.ToString("yyyy-MM-dd HH:mm:ss"));
        builder.AppendLine("Root: " + root);
        builder.AppendLine("World: " + SelectedWorldName);
        AppendBuildInfo(builder);
        builder.AppendLine("Java: " + (FindJavaExe() ?? "missing"));
        builder.AppendLine("Client: " + (FindClientExe() ?? "missing"));
        builder.AppendLine("DB port 3307: " + (IsPortOpenAsync(3307).GetAwaiter().GetResult() ? "open" : "closed"));
        builder.AppendLine("Server port 14445: " + (IsPortOpenAsync(14445).GetAwaiter().GetResult() ? "open" : "closed"));
        builder.AppendLine();
        builder.AppendLine("Launcher Log:");
        builder.AppendLine(logBox.Text);
        AppendTail(builder, Path.Combine(logsDir, "server.log"), "server.log");
        AppendTail(builder, Path.Combine(logsDir, "database.log"), "database.log");
        AppendTail(builder, Path.Combine(logsDir, "mysql-import-seed.log"), "mysql-import-seed.log");
        AppendTail(builder, Path.Combine(root, "server", "data", "config", "config.properties"), "config.properties", redactSensitive: true);
        Clipboard.SetText(builder.ToString());
        Log("Copied report to clipboard.");
    }

    private string? FindClientExe()
    {
        var clientDir = Path.Combine(root, "client");
        if (!Directory.Exists(clientDir))
        {
            return null;
        }

        return Directory.GetFiles(clientDir, "*.exe")
            .FirstOrDefault(p =>
            {
                var name = Path.GetFileName(p);
                return !name.Equals("UnityCrashHandler64.exe", StringComparison.OrdinalIgnoreCase)
                    && !name.Equals("UnityCrashHandler32.exe", StringComparison.OrdinalIgnoreCase);
            });
    }

    private void CopyConfig(bool host)
    {
        var src = Path.Combine(root, "config", host ? "config.host-lan.properties" : "config.single.properties");
        var dest = Path.Combine(root, "server", "data", "config", "config.properties");
        if (!File.Exists(src))
        {
            throw new FileNotFoundException("Missing config: " + src);
        }
        Directory.CreateDirectory(Path.GetDirectoryName(dest)!);
        File.Copy(src, dest, true);
        if (host)
        {
            RewriteHostLanServerList(dest);
        }
        Log("Config: " + (host ? "Host LAN" : "Single Mode"));
    }

    private void RewriteHostLanServerList(string configPath)
    {
        var hostIp = GetLanAddressValues().FirstOrDefault();
        if (string.IsNullOrWhiteSpace(hostIp))
        {
            Log("LAN IP not detected; keeping bundled Host LAN server list.");
            return;
        }

        var lines = File.ReadAllLines(configPath, Encoding.UTF8).ToList();
        var replacement = $"server.sv1              = DragonBallLegends LAN:{hostIp}:14445:0,0,0";
        var replaced = false;
        for (var i = 0; i < lines.Count; i++)
        {
            if (lines[i].TrimStart().StartsWith("server.sv1", StringComparison.OrdinalIgnoreCase))
            {
                lines[i] = replacement;
                replaced = true;
                break;
            }
        }
        if (!replaced)
        {
            lines.Add(replacement);
        }
        File.WriteAllLines(configPath, lines, Encoding.UTF8);
        Log("Host LAN advertises: " + hostIp + ":14445");
    }

    private void ResetClientProfile()
    {
        foreach (var profile in new[]
        {
            Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.UserProfile), "AppData", "LocalLow", "NRO", "DragonBall"),
            Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.UserProfile), "AppData", "LocalLow", "NRO", "DragonBallLegends")
        })
        {
            if (!Directory.Exists(profile))
            {
                continue;
            }
            foreach (var name in new[] { "NRlink2", "svselect", "acc", "pass", "userAo0", "userAo1", "userAo2", "userAo3" })
            {
                var file = Path.Combine(profile, name);
                if (File.Exists(file))
                {
                    File.Delete(file);
                }
            }
        }
    }

    private void SaveClientServerSelection(string name, string host, int port)
    {
        var serverList = $"{name}:{host}:{port}:0,0,0";
        var encoded = EncodeServerList(serverList, "69");
        foreach (var profile in GetClientProfiles())
        {
            Directory.CreateDirectory(profile);
            WriteRmsString(Path.Combine(profile, "NRlink2"), encoded);
            File.WriteAllBytes(Path.Combine(profile, "svselect"), new byte[] { 0 });
        }
        Log($"Client server: {name} {host}:{port}");
    }

    private static IEnumerable<string> GetClientProfiles()
    {
        var localLow = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.UserProfile), "AppData", "LocalLow");
        yield return Path.Combine(localLow, "NRO", "DragonBall");
        yield return Path.Combine(localLow, "NRO", "DragonBallLegends");
    }

    private static void WriteRmsString(string path, string value)
    {
        var bytes = Encoding.UTF8.GetBytes(value);
        if (bytes.Length > short.MaxValue)
        {
            throw new InvalidOperationException("RMS string is too long.");
        }
        using var stream = new FileStream(path, FileMode.Create, FileAccess.Write, FileShare.None);
        stream.WriteByte((byte)(bytes.Length >> 8));
        stream.WriteByte((byte)(bytes.Length & 0xFF));
        stream.Write(bytes, 0, bytes.Length);
    }

    private static string EncodeServerList(string input, string key)
    {
        var inputBytes = Encoding.UTF8.GetBytes(input);
        var keyBytes = Encoding.UTF8.GetBytes(key);
        var parts = new string[inputBytes.Length];
        for (var i = 0; i < inputBytes.Length; i++)
        {
            parts[i] = (inputBytes[i] ^ keyBytes[i % keyBytes.Length]).ToString("X2");
        }
        return string.Join("-", parts);
    }

    private static (string Host, int Port) ParseEndpoint(string value, int defaultPort)
    {
        value = (value ?? string.Empty).Trim();
        if (string.IsNullOrWhiteSpace(value))
        {
            throw new InvalidOperationException("Enter friend IP or host.");
        }
        var host = value;
        var port = defaultPort;
        var colon = value.LastIndexOf(':');
        if (colon > 0 && colon < value.Length - 1 && int.TryParse(value[(colon + 1)..], out var parsedPort))
        {
            host = value[..colon];
            port = parsedPort;
        }
        if (port <= 0 || port > 65535)
        {
            throw new InvalidOperationException("Friend port is invalid.");
        }
        return (host.Trim(), port);
    }

    private void MigrateLegacySave()
    {
        var legacy = Path.Combine(runtimeDir, "dbdata");
        if (!Directory.Exists(DbDataDir) && Directory.Exists(legacy))
        {
            Log("Migrating legacy save to " + SelectedWorldName + "...");
            CopyDirectory(legacy, DbDataDir);
            var legacyMarker = Path.Combine(runtimeDir, ".db_initialized");
            if (File.Exists(legacyMarker))
            {
                File.Copy(legacyMarker, Path.Combine(WorldDir, ".db_initialized"), true);
            }
        }
    }

    private string ReadActiveWorldName()
    {
        if (!File.Exists(activeWorldFile))
        {
            return "unknown world";
        }
        var value = File.ReadAllText(activeWorldFile, Encoding.UTF8).Trim();
        return string.IsNullOrWhiteSpace(value) ? "unknown world" : SanitizeWorldName(value);
    }

    private (string Server, string Client, string? Admin) FindMysqlTools(bool required = true)
    {
        var mysqlRoot = Directory.Exists(Path.Combine(runtimeDir, "mariadb"))
            ? Path.Combine(runtimeDir, "mariadb")
            : Path.Combine(runtimeDir, "mysql");
        var bin = Path.Combine(mysqlRoot, "bin");
        var server = Path.Combine(bin, File.Exists(Path.Combine(bin, "mariadbd.exe")) ? "mariadbd.exe" : "mysqld.exe");
        var client = Path.Combine(bin, "mysql.exe");
        var admin = Path.Combine(bin, "mysqladmin.exe");
        if (required && (!File.Exists(server) || !File.Exists(client)))
        {
            throw new FileNotFoundException("Missing portable MySQL/MariaDB runtime under runtime\\mysql or runtime\\mariadb.");
        }
        return (server, client, File.Exists(admin) ? admin : null);
    }

    private string? FindJavaExe()
    {
        var bundled = Path.Combine(runtimeDir, "java", "bin", "java.exe");
        if (File.Exists(bundled))
        {
            return bundled;
        }

        var javaHome = Environment.GetEnvironmentVariable("JAVA_HOME");
        if (!string.IsNullOrWhiteSpace(javaHome))
        {
            var fromHome = Path.Combine(javaHome, "bin", "java.exe");
            if (File.Exists(fromHome))
            {
                return fromHome;
            }
        }

        return ResolveExeOnPath("java.exe");
    }

    private static string? ResolveExeOnPath(string exe)
    {
        var paths = (Environment.GetEnvironmentVariable("PATH") ?? string.Empty)
            .Split(Path.PathSeparator, StringSplitOptions.RemoveEmptyEntries);
        foreach (var path in paths)
        {
            try
            {
                var candidate = Path.Combine(path.Trim(), exe);
                if (File.Exists(candidate))
                {
                    return candidate;
                }
            }
            catch
            {
                // Ignore malformed PATH entries.
            }
        }
        return null;
    }

    private static string Sha256File(string path)
    {
        using var stream = File.OpenRead(path);
        return Convert.ToHexString(SHA256.HashData(stream)).ToLowerInvariant();
    }

    private VerifySummary VerifyInstallManifest(string manifest)
    {
        var issues = new List<FileIssue>();
        var checkedCount = 0;
        foreach (var line in File.ReadLines(manifest, Encoding.UTF8))
        {
            if (string.IsNullOrWhiteSpace(line) || line.StartsWith("#", StringComparison.Ordinal))
            {
                continue;
            }

            var parts = line.Split('\t');
            if (parts.Length < 2)
            {
                issues.Add(new FileIssue("install_manifest.tsv", "Invalid manifest line"));
                continue;
            }

            var expectedHash = parts[0].Trim();
            var relativePath = parts.Length >= 3 ? parts[2].Trim() : parts[1].Trim();
            long? expectedLength = null;
            if (parts.Length >= 3 && long.TryParse(parts[1], out var parsedLength))
            {
                expectedLength = parsedLength;
            }

            var file = SafePathUnderRoot(relativePath);
            checkedCount++;
            if (file == null || !File.Exists(file))
            {
                issues.Add(new FileIssue(relativePath, "Missing"));
                continue;
            }

            if (expectedLength.HasValue && new FileInfo(file).Length != expectedLength.Value)
            {
                issues.Add(new FileIssue(relativePath, "Size changed"));
                continue;
            }

            var actualHash = Sha256File(file);
            if (!actualHash.Equals(expectedHash, StringComparison.OrdinalIgnoreCase))
            {
                issues.Add(new FileIssue(relativePath, "Hash changed"));
            }
        }
        return new VerifySummary(checkedCount, issues);
    }

    private string? SafePathUnderRoot(string relativePath)
    {
        try
        {
            relativePath = relativePath.Replace('/', Path.DirectorySeparatorChar);
            var full = Path.GetFullPath(Path.Combine(root, relativePath));
            var rootFull = Path.GetFullPath(root).TrimEnd(Path.DirectorySeparatorChar) + Path.DirectorySeparatorChar;
            return full.StartsWith(rootFull, StringComparison.OrdinalIgnoreCase) ? full : null;
        }
        catch
        {
            return null;
        }
    }

    private void LogBuildInfo()
    {
        var buildInfo = Path.Combine(root, "build_info.txt");
        if (!File.Exists(buildInfo))
        {
            Log("Build info: missing");
            return;
        }
        foreach (var line in File.ReadLines(buildInfo, Encoding.UTF8).Take(8))
        {
            Log("Build info: " + line);
        }
    }

    private void AppendBuildInfo(StringBuilder builder)
    {
        var buildInfo = Path.Combine(root, "build_info.txt");
        builder.AppendLine("Build info:");
        if (!File.Exists(buildInfo))
        {
            builder.AppendLine("missing");
            return;
        }
        builder.AppendLine(File.ReadAllText(buildInfo, Encoding.UTF8));
    }

    private static string SqlEscape(string value)
    {
        return value.Replace("\\", "\\\\").Replace("'", "''");
    }

    private static void AppendTail(StringBuilder builder, string path, string label, int maxChars = 12000, bool redactSensitive = false)
    {
        builder.AppendLine();
        builder.AppendLine(label + ":");
        if (!File.Exists(path))
        {
            builder.AppendLine("missing");
            return;
        }

        var text = File.ReadAllText(path, Encoding.UTF8);
        if (redactSensitive)
        {
            text = RedactSensitiveText(text);
        }
        if (text.Length > maxChars)
        {
            text = text[^maxChars..];
        }
        builder.AppendLine(text);
    }

    private static string RedactSensitiveText(string text)
    {
        var builder = new StringBuilder();
        foreach (var line in text.Split(new[] { "\r\n", "\n" }, StringSplitOptions.None))
        {
            var separator = line.IndexOf('=');
            if (separator > 0)
            {
                var key = line[..separator].Trim().ToLowerInvariant();
                if (key.Contains("pass") || key.Contains("password") || key.Contains("secret") ||
                    key.Contains("token") || key.Contains("cookie") || key.Contains("bank") ||
                    key.Contains("apikey") || key.Contains("api_key") || key.Contains("sitekey"))
                {
                    builder.AppendLine(line[..(separator + 1)] + " <redacted>");
                    continue;
                }
            }
            builder.AppendLine(line);
        }
        return builder.ToString();
    }

    private static void SafeExtractZip(string zipPath, string destinationDir)
    {
        Directory.CreateDirectory(destinationDir);
        var rootFull = Path.GetFullPath(destinationDir).TrimEnd(Path.DirectorySeparatorChar) + Path.DirectorySeparatorChar;
        using var archive = System.IO.Compression.ZipFile.OpenRead(zipPath);
        foreach (var entry in archive.Entries)
        {
            var target = Path.GetFullPath(Path.Combine(destinationDir, entry.FullName));
            if (!target.StartsWith(rootFull, StringComparison.OrdinalIgnoreCase))
            {
                throw new InvalidOperationException("Backup contains an unsafe path: " + entry.FullName);
            }
            if (string.IsNullOrEmpty(entry.Name))
            {
                Directory.CreateDirectory(target);
                continue;
            }
            Directory.CreateDirectory(Path.GetDirectoryName(target)!);
            using var input = entry.Open();
            using var output = new FileStream(target, FileMode.Create, FileAccess.Write, FileShare.None);
            input.CopyTo(output);
        }
    }

    private Process StartLoggedProcess(string file, string args, string workingDir, string logFile)
    {
        Directory.CreateDirectory(Path.GetDirectoryName(logFile)!);
        var process = new Process
        {
            StartInfo = new ProcessStartInfo
            {
                FileName = file,
                Arguments = args,
                WorkingDirectory = workingDir,
                UseShellExecute = false,
                CreateNoWindow = true,
                RedirectStandardOutput = true,
                RedirectStandardError = true,
                StandardOutputEncoding = Encoding.UTF8,
                StandardErrorEncoding = Encoding.UTF8
            },
            EnableRaisingEvents = true
        };
        process.OutputDataReceived += (_, e) => AppendProcessLine(logFile, e.Data);
        process.ErrorDataReceived += (_, e) => AppendProcessLine(logFile, e.Data);
        process.Start();
        process.BeginOutputReadLine();
        process.BeginErrorReadLine();
        return process;
    }

    private async Task RunProcessAsync(string file, string args, string workingDir, string logName, bool throwOnFail, int timeoutSeconds = 0)
    {
        var logFile = Path.Combine(logsDir, logName);
        var process = StartLoggedProcess(file, args, workingDir, logFile);
        if (timeoutSeconds > 0)
        {
            var waitTask = process.WaitForExitAsync();
            var completed = await Task.WhenAny(waitTask, Task.Delay(TimeSpan.FromSeconds(timeoutSeconds)));
            if (completed != waitTask)
            {
                TryKill(process);
                if (throwOnFail)
                {
                    throw new TimeoutException($"{Path.GetFileName(file)} timed out. See logs\\{logName}.");
                }
                return;
            }
        }
        else
        {
            await process.WaitForExitAsync();
        }
        if (throwOnFail && process.ExitCode != 0)
        {
            throw new InvalidOperationException($"{Path.GetFileName(file)} failed. See logs\\{logName}.");
        }
    }

    private void AppendProcessLine(string logFile, string? line)
    {
        if (string.IsNullOrEmpty(line))
        {
            return;
        }
        File.AppendAllText(logFile, line + Environment.NewLine, Encoding.UTF8);
        Dispatcher.Invoke(() =>
        {
            if (logBox.LineCount < 500)
            {
                Log(line);
            }
        });
    }

    private static async Task<bool> IsPortOpenAsync(int port)
    {
        using var client = new TcpClient();
        try
        {
            var connect = client.ConnectAsync(IPAddress.Loopback, port);
            var done = await Task.WhenAny(connect, Task.Delay(300));
            return done == connect && client.Connected;
        }
        catch
        {
            return false;
        }
    }

    private static async Task WaitForPortAsync(int port, string error, int seconds)
    {
        for (var i = 0; i < seconds; i++)
        {
            if (await IsPortOpenAsync(port))
            {
                return;
            }
            await Task.Delay(1000);
        }
        throw new TimeoutException(error);
    }

    private void KillPidFile(string path, string label, params string[] allowedProcessNames)
    {
        if (!File.Exists(path) || !int.TryParse(File.ReadAllText(path).Trim(), out var pid))
        {
            return;
        }
        try
        {
            var process = Process.GetProcessById(pid);
            if (allowedProcessNames.Length > 0 && !allowedProcessNames.Any(name => process.ProcessName.Equals(name, StringComparison.OrdinalIgnoreCase)))
            {
                Log($"Skipping {label} PID {pid}; process name is {process.ProcessName}.");
                TryDelete(path);
                return;
            }
            process.Kill(true);
            process.WaitForExit(5000);
            Log($"Stopped {label} PID {pid}.");
        }
        catch
        {
            // Stale PID or already stopped.
        }
        TryDelete(path);
    }

    private static bool HasLivePid(string path, params string[] processNames)
    {
        if (!File.Exists(path) || !int.TryParse(File.ReadAllText(path).Trim(), out var pid))
        {
            return false;
        }
        try
        {
            using var process = Process.GetProcessById(pid);
            if (process.HasExited)
            {
                return false;
            }
            return processNames.Length == 0 || processNames.Any(name => process.ProcessName.Equals(name, StringComparison.OrdinalIgnoreCase));
        }
        catch
        {
            return false;
        }
    }

    private static void TryKill(Process process)
    {
        try
        {
            if (!process.HasExited)
            {
                process.Kill(true);
            }
        }
        catch
        {
            // Best effort cleanup only.
        }
    }

    private void RefreshStatus()
    {
        _ = RefreshStatusAsync().ContinueWith(task =>
        {
            if (task.Exception != null)
            {
                Dispatcher.Invoke(() => Log("Status warning: " + task.Exception.GetBaseException().Message));
            }
        });
    }

    private async Task RefreshStatusAsync()
    {
        var db = await IsPortOpenAsync(3307);
        var server = await IsPortOpenAsync(14445);
        var javaReady = FindJavaExe() != null;
        dbBadge.Text = db ? "Running" : "Stopped";
        serverBadge.Text = server ? "Running" : "Stopped";
        javaBadge.Text = javaReady ? "Ready" : "Missing";
        worldBadge.Text = Directory.Exists(DbDataDir) ? SelectedWorldName : "Not created";
        dbBadge.Foreground = db ? Brushes.ForestGreen : Brushes.Firebrick;
        serverBadge.Foreground = server ? Brushes.ForestGreen : Brushes.Firebrick;
        javaBadge.Foreground = javaReady ? Brushes.ForestGreen : Brushes.Firebrick;
        worldBadge.Foreground = Directory.Exists(DbDataDir) ? Brushes.ForestGreen : Brushes.Firebrick;
        lanInfo.Text = "LAN IP: " + string.Join(", ", GetLanAddresses().DefaultIfEmpty("not detected"));
    }

    private static IEnumerable<string> GetLanAddresses()
    {
        return GetLanAddressValues()
            .Select(address => address + ":14445");
    }

    private static IEnumerable<string> GetLanAddressValues()
    {
        return NetworkInterface.GetAllNetworkInterfaces()
            .Where(n => n.OperationalStatus == OperationalStatus.Up)
            .SelectMany(n => n.GetIPProperties().UnicastAddresses)
            .Where(a => a.Address.AddressFamily == AddressFamily.InterNetwork && !IPAddress.IsLoopback(a.Address))
            .Select(a => a.Address.ToString())
            .Distinct();
    }

    private void Log(string message)
    {
        logBox.AppendText($"[{DateTime.Now:HH:mm:ss}] {message}{Environment.NewLine}");
        logBox.ScrollToEnd();
    }

    private void ClearLog()
    {
        logBox.Clear();
    }

    private void SetBusy(bool busy)
    {
        foreach (var control in busyControls)
        {
            control.IsEnabled = !busy;
        }
    }

    private static void OpenFolder(string path)
    {
        Directory.CreateDirectory(path);
        Process.Start(new ProcessStartInfo
        {
            FileName = "explorer.exe",
            Arguments = $"\"{path}\"",
            UseShellExecute = true
        });
    }

    private static void CopyDirectory(string source, string dest)
    {
        Directory.CreateDirectory(dest);
        foreach (var dir in Directory.GetDirectories(source, "*", SearchOption.AllDirectories))
        {
            Directory.CreateDirectory(dir.Replace(source, dest));
        }
        foreach (var file in Directory.GetFiles(source, "*", SearchOption.AllDirectories))
        {
            File.Copy(file, file.Replace(source, dest), true);
        }
    }

    private static void TryDelete(string path)
    {
        try
        {
            if (File.Exists(path))
            {
                File.Delete(path);
            }
        }
        catch
        {
            // Best effort cleanup only.
        }
    }
}
