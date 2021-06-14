package com.willfp.eco.internal.config;

import com.willfp.eco.core.EcoPlugin;
import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public abstract class LoadableYamlConfig extends ConfigWrapper<YamlConfiguration> implements LoadableConfig {
    /**
     * The physical config file, as stored on disk.
     */
    @Getter(AccessLevel.PROTECTED)
    private final File configFile;

    /**
     * Plugin handle.
     */
    @Getter(AccessLevel.PROTECTED)
    private final EcoPlugin plugin;

    /**
     * The full name of the config file (eg config.yml).
     */
    @Getter
    private final String name;

    /**
     * The subdirectory path.
     */
    @Getter(AccessLevel.PROTECTED)
    private final String subDirectoryPath;

    /**
     * The provider of the config.
     */
    @Getter(AccessLevel.PROTECTED)
    private final Class<?> source;

    /**
     * Abstract config.
     *
     * @param configName       The name of the config
     * @param plugin           The plugin.
     * @param subDirectoryPath The subdirectory path.
     * @param source           The class that owns the resource.
     */
    protected LoadableYamlConfig(@NotNull final String configName,
                                 @NotNull final EcoPlugin plugin,
                                 @NotNull final String subDirectoryPath,
                                 @NotNull final Class<?> source) {
        this.plugin = plugin;
        this.name = configName + ".yml";
        this.source = source;
        this.subDirectoryPath = subDirectoryPath;

        File directory = new File(this.getPlugin().getDataFolder(), subDirectoryPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        if (!new File(directory, this.name).exists()) {
            createFile();
        }

        this.configFile = new File(directory, this.name);
        init(YamlConfiguration.loadConfiguration(configFile));
    }

    @Override
    public void createFile() {
        String resourcePath = getResourcePath();
        InputStream in = source.getResourceAsStream(resourcePath);

        File outFile = new File(this.getPlugin().getDataFolder(), resourcePath);
        int lastIndex = resourcePath.lastIndexOf('/');
        File outDir = new File(this.getPlugin().getDataFolder(), resourcePath.substring(0, Math.max(lastIndex, 0)));

        if (!outDir.exists()) {
            outDir.mkdirs();
        }

        try {
            if (!outFile.exists()) {
                OutputStream out = new FileOutputStream(outFile);
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                out.close();
                in.close();
            }
        } catch (IOException ignored) {
        }
    }

    @Override
    public String getResourcePath() {
        String resourcePath;

        if (subDirectoryPath.isEmpty()) {
            resourcePath = name;
        } else {
            resourcePath = subDirectoryPath + name;
        }

        return "/" + resourcePath;
    }

    @Override
    public YamlConfiguration getConfigInJar() {
        InputStream newIn = source.getResourceAsStream(getResourcePath());

        if (newIn == null) {
            throw new NullPointerException(name + " is null?");
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(newIn, StandardCharsets.UTF_8));
        YamlConfiguration newConfig = new YamlConfiguration();

        try {
            newConfig.load(reader);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }

        return newConfig;
    }

    @Override
    public void save() throws IOException {
        this.getHandle().save(this.getConfigFile());
    }
}
