package com.gtnewhorizons.aspectrecipeindex.mixins;

import javax.annotation.Nonnull;

import com.gtnewhorizon.gtnhmixins.builders.IMixins;
import com.gtnewhorizon.gtnhmixins.builders.MixinBuilder;

public enum Mixins implements IMixins {

    // spotless:off
    MAPPING_THREAD_HOOK(new MixinBuilder()
        .addClientMixins("thaumcraft.MappingThreadMixin")
        .addClientMixins("thaumcraft.ClientTickEventsFMLMixin")
        .addRequiredMod(TargetedMod.THAUMCRAFT)
        .setPhase(Phase.LATE));
    // spotless:on

    private final MixinBuilder builder;

    Mixins(MixinBuilder builder) {
        this.builder = builder;
    }

    @Nonnull
    @Override
    public MixinBuilder getBuilder() {
        return this.builder;
    }

}
