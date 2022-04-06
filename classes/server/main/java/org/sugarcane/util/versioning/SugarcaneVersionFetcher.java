package org.sugarcanemc.sugarcane.util.versioning;

import com.destroystokyo.paper.VersionHistoryManager;
import com.destroystokyo.paper.util.VersionFetcher;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.google.gson.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.JoinConfiguration;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import org.sugarcanemc.sugarcane.util.versioning.BranchReader;

public class SugarcaneVersionFetcher implements VersionFetcher {
    private static final java.util.regex.Pattern VER_PATTERN = java.util.regex.Pattern.compile("^([0-9\\.]*)\\-.*R"); // R is an anchor, will always give '-R' at end
    private static final String DOWNLOAD_PAGE = "https://ci.sugarcanemc.org/job/Sugarcane/";
    private static int distance = -2;
    public int distance() { return distance; }
    private static @Nullable String mcVer;

    @Override
    public long getCacheTime() {
        return 720000;
    }

    @Nonnull
    @Override
    public Component getVersionMessage(@Nonnull String serverVersion) {
        String[] parts = serverVersion.substring("git-Sugarcane-".length()).split("[-\\s]");
        final @Nonnull String branch = BranchReader.getBranch();
        final Component updateMessage = getUpdateStatusMessage("SugarcaneMC/Sugarcane", branch, parts[0]);
        final Component history = getHistory();

        return history != null ? Component.join(JoinConfiguration.separator(Component.newline()), history, updateMessage) : updateMessage;
    }

    private static @Nullable String getMinecraftVersion() {
        if (mcVer == null) {
            java.util.regex.Matcher matcher = VER_PATTERN.matcher(org.bukkit.Bukkit.getBukkitVersion());
            if (matcher.find()) {
                String result = matcher.group();
                mcVer = result.substring(0, result.length() - 2); // strip 'R' anchor and trailing '-'
            } else {
                org.bukkit.Bukkit.getLogger().warning("Unable to match version to pattern! Report to Sugarcane!");
                org.bukkit.Bukkit.getLogger().warning("Pattern: " + VER_PATTERN.toString());
                org.bukkit.Bukkit.getLogger().warning("Version: " + org.bukkit.Bukkit.getBukkitVersion());
            }
        }

        return mcVer;
    }

    private static Component getUpdateStatusMessage(@Nonnull String repo, @Nonnull String branch, @Nonnull String versionInfo) {
        String err = "";
        try {
            int jenkinsBuild = Integer.parseInt(versionInfo);
            distance = fetchDistanceFromJenkins(jenkinsBuild, branch);
        } catch (Exception ignored) {
            versionInfo = versionInfo.replace("\"", "");
            //distance = fetchDistanceFromJenkins(jenkinsBuild, branch);
            distance = -2;
            err = ignored.getMessage();
        }

        switch (distance) {
            case -2:
                return Component.text(String.format("* Unknown version (%s), are you running a custom build?", versionInfo), NamedTextColor.RED);
            case -1:
                return Component.text(String.format("* Error obtaining version information (%s)!", err), NamedTextColor.RED);
            case 0:
                return Component.text("* You are running the latest version!", NamedTextColor.GREEN);
            default:
                return Component.text("* You are " + distance + " version(s) behind!", NamedTextColor.YELLOW)
                        .append(Component.newline())
                        .append(Component.text("Download the new version at: ")
                        .append(Component.text(DOWNLOAD_PAGE, NamedTextColor.GOLD)
                        .hoverEvent(Component.text("Click to open", NamedTextColor.WHITE))
                        .clickEvent(ClickEvent.openUrl(DOWNLOAD_PAGE))));
        }
    }

    private static int fetchDistanceFromJenkins(int jenkinsBuild, @Nonnull String branch) {
        try {
            try (BufferedReader reader = Resources.asCharSource(new URL("https://ci.sugarcanemc.org/job/Sugarcane/job/" + URLEncoder.encode(branch, Charsets.UTF_8.name()) + "/lastStableBuild/buildNumber"), Charsets.UTF_8).openBufferedStream()) { // Sugarcane
                return Integer.decode(reader.readLine()) - jenkinsBuild;
            } catch (NumberFormatException ex) {
                ex.printStackTrace();
                return -2;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    // Contributed by Techcable <Techcable@outlook.com> in GH-65
    private static int fetchDistanceFromGitHub(@Nonnull String repo, @Nonnull String branch, @Nonnull String hash) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL("https://api.github.com/repos/" + repo + "/compare/" + branch + "..." + hash).openConnection();
            connection.connect();
            if (connection.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) return -2; // Unknown commit
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), Charsets.UTF_8));
            JsonObject obj = new Gson().fromJson(reader, JsonObject.class);
            String status = obj.get("status").getAsString();
            switch (status) {
                case "identical":
                    return 0;
                case "behind":
                    return obj.get("behind_by").getAsInt();
                default:
                    return -1;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Nullable
    private Component getHistory() {
        final VersionHistoryManager.VersionData data = VersionHistoryManager.INSTANCE.getVersionData();
        if (data == null) {
            return null;
        }

        final String oldVersion = data.getOldVersion();
        if (oldVersion == null) {
            return null;
        }

        return org.bukkit.ChatColor.parseMM("<grey>Previous: %s", oldVersion); // Purpur
    }
}
