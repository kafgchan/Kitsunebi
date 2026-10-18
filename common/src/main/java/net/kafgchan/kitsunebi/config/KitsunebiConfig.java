package net.kafgchan.kitsunebi.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

/**
 * Kitsunebi 総合設定（Config）
 * 妖術射程、最大破壊ブロック数、サーバー負荷上限、演出ON/OFFを管理
 */
public class KitsunebiConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File("config/kitsunebi.json");

    public int swordSlashRange = 64;
    public int swordDanceRadius = 16;
    public int maxVeinBlocks = 128;
    public int tunnelLength = 32;
    public int maxTreeFellLogs = 512;
    public int blocksPerTick = 64;
    public int maxAttackTargets = 128;
    public boolean enableParticles = true;

    private static KitsunebiConfig INSTANCE = new KitsunebiConfig();

    public static KitsunebiConfig get() {
        return INSTANCE;
    }

    public static void load() {
        try {
            if (CONFIG_FILE.exists()) {
                try (FileReader reader = new FileReader(CONFIG_FILE)) {
                    INSTANCE = GSON.fromJson(reader, KitsunebiConfig.class);
                    if (INSTANCE == null) INSTANCE = new KitsunebiConfig();
                }
            } else {
                save();
            }
        } catch (Exception e) {
            INSTANCE = new KitsunebiConfig();
        }
    }

    public static void save() {
        try {
            File dir = CONFIG_FILE.getParentFile();
            if (dir != null && !dir.exists()) {
                dir.mkdirs();
            }
            try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
                GSON.toJson(INSTANCE, writer);
            }
        } catch (Exception ignored) {
        }
    }
}
