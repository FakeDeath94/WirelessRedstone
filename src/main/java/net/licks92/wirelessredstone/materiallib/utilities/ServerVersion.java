package net.licks92.wirelessredstone.materiallib.utilities;

public enum ServerVersion {
    OLDER,
    V1_4_2, V1_4_4, V1_4_5, V1_4_6, V1_4_7,
    V1_5, V1_5_1, V1_5_2,
    V1_6_1, V1_6_2, V1_6_4,
    V1_7_2, V1_7_4, V1_7_5, V1_7_6, V1_7_7, V1_7_8, V1_7_9, V1_7_10,
    V1_8, V1_8_1, V1_8_2, V1_8_3, V1_8_4, V1_8_5, V1_8_6, V1_8_7, V1_8_8, V1_8_9,
    V1_9, V1_9_1, V1_9_2, V1_9_3, V1_9_4,
    V1_10, V1_10_1, V1_10_2,
    V1_11, V1_11_1, V1_11_2,
    V1_12, V1_12_1, V1_12_2,
    V1_13, V1_13_1, V1_13_2,
    V1_14, V1_14_1, V1_14_2, V1_14_3, V1_14_4,
    V1_15, V1_15_1, V1_15_2, V1_15_3, V1_15_4,
    V1_16, V1_16_1, V1_16_2, V1_16_3, V1_16_4,
    V1_17, V1_17_1, V1_17_2, V1_17_3, V1_17_4,
    V1_18, V1_18_1, V1_18_2, V1_18_3, V1_18_4,
    V1_19, V1_19_1, V1_19_2, V1_19_3, V1_19_4,
    V1_20, V1_20_1, V1_20_2, V1_20_3, V1_20_4, V1_20_5,
    V1_21, V1_21_1,
    NEWER;

    private final String versionNumber;

    ServerVersion() {
        this.versionNumber = name().substring(1).replace("_", ".");
    }

    public String versionNumber() {
        return versionNumber;
    }

    public int getOrder() {
        if (this == OLDER) {
            return -1;
        }
        if (this == NEWER) {
            return Integer.MAX_VALUE;
        }
        return ordinal();
    }

    public static ServerVersion getLastKnown() {
        return values()[values().length - 2];
    }

    public boolean isNewer(ServerVersion other) {
        return getOrder() > other.getOrder();
    }

    public boolean isNewerOrSame(ServerVersion other) {
        return getOrder() >= other.getOrder();
    }

    public boolean isOlder(ServerVersion other) {
        return getOrder() < other.getOrder();
    }

    public boolean isBetween(ServerVersion older, ServerVersion newer) {
        return getOrder() >= older.getOrder() && getOrder() <= newer.getOrder();
    }
}
