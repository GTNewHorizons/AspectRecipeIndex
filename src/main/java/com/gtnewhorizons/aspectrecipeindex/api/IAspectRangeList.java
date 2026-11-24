package com.gtnewhorizons.aspectrecipeindex.api;

import thaumcraft.api.aspects.Aspect;

/**
 * Implemented by custom {@link thaumcraft.api.aspects.AspectList} subclasses whose contents are generated lazily within
 * a bounded range instead of being fixed at registration time, such as Gadomancy's {@code RandomizedAspectList}
 * (randomized infusion essentia costs). Lets the infusion recipe handler show the true possible cost range in tooltips
 * instead of only whatever the current roll produced.
 */
public interface IAspectRangeList {

    /**
     * @return the minimum amount this aspect is ever required in; {@code 0} if the aspect may be absent entirely (a
     *         zero roll is possible); {@code -1} if the aspect is not part of this list at all
     */
    int getMinAmount(Aspect aspect);

    /**
     * @return the maximum amount this aspect is ever required in, or {@code -1} if the aspect is not part of this list
     *         at all
     */
    int getMaxAmount(Aspect aspect);
}
