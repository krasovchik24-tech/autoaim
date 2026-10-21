package com.example.autoaim;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class AutoAimConfig {
    public enum TargetMode {
        PLAYERS("Игроки"), MOBS("Мобы"), ALL("Все");

        public final String label;

        TargetMode(String label) {
            this.label = label;
        }

        public TargetMode next() {
            TargetMode[] v = values();
            return v[(ordinal() + 1) % v.length];
        }
    }

    public static final double MIN_RANGE = 2.0, MAX_RANGE = 12.0;
    public static final double MIN_SPEED = 1.0, MAX_SPEED = 60.0;

    public boolean enabled = false;
    public double range = 6.0;
    public double speed = 15.0; // градусов за тик
    public TargetMode target = TargetMode.PLAYERS;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("autoaim.json");
    }

    public static AutoAimConfig load() {
        try {
            Path p = path();
            if (Files.exists(p)) {
                try (Reader r = Files.newBufferedReader(p)) {
                    AutoAimConfig c = GSON.fromJson(r, AutoAimConfig.class);
                    if (c != null) {
                        if (c.target == null) c.target = TargetMode.PLAYERS;
                        return c;
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return new AutoAimConfig();
    }

    public void save() {
        try (Writer w = Files.newBufferedWriter(path())) {
            GSON.toJson(this, w);
        } catch (IOException ignored) {
        }
    }
}
