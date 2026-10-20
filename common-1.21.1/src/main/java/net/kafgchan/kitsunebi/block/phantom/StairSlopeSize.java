package net.kafgchan.kitsunebi.block.phantom;

import net.minecraft.util.StringRepresentable;

/**
 * 階段およびスロープの3規格サイズ
 */
public enum StairSlopeSize implements StringRepresentable {
    HIGH("high", 16.0, 16.0),
    MID("mid", 8.0, 16.0),
    LOW("low", 8.0, 8.0);

    private final String name;
    private final double maxHeight;
    private final double maxDepth;

    StairSlopeSize(String name, double maxHeight, double maxDepth) {
        this.name = name;
        this.maxHeight = maxHeight;
        this.maxDepth = maxDepth;
    }

    public double getMaxHeight() {
        return maxHeight;
    }

    public double getMaxDepth() {
        return maxDepth;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}
