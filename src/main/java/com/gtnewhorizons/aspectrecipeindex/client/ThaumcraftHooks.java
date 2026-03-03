package com.gtnewhorizons.aspectrecipeindex.client;

import org.jetbrains.annotations.ApiStatus;

public class ThaumcraftHooks {

    private static int totalToLoad;
    private static int itemsLoaded;
    private static boolean allDataLoaded;

    /**
     * Internal. Do not use.
     */
    @ApiStatus.Internal
    public static void setAllDataLoaded() {
        allDataLoaded = true;
    }

    /**
     * Internal. Do not use.
     */
    @ApiStatus.Internal
    public static void incrementLoadedItems() {
        itemsLoaded++;
    }

    public static int getItemsLoaded() {
        return itemsLoaded;
    }

    public static int getTotalToLoad() {
        return totalToLoad;
    }

    /**
     * Internal. Do not use.
     */
    @ApiStatus.Internal
    public static void setTotalToLoad(int totalToLoad) {
        ThaumcraftHooks.totalToLoad = totalToLoad;
    }

    public static boolean isDataLoaded() {
        return allDataLoaded;
    }
}
