package com.gtnewhorizons.aspectrecipeindex.client.research;

import java.io.File;
import java.io.IOException;
import java.util.List;

import codechicken.nei.config.DataDumper;

public class ResearchIdDumper extends DataDumper {

    public ResearchIdDumper() {
        super("tools.dump.thaumcraftresearch");
    }

    @Override
    public String renderName() {
        return translateN(name);
    }

    @Override
    public int modeCount() {
        return 2;
    }

    @Override
    public String modeButtonText() {
        return translateN(name + ".mode." + getMode());
    }

    @Override
    public String getFileExtension() {
        return getMode() == 0 ? ".csv" : ".json";
    }

    @Override
    public String[] header() {
        return ResearchDirectory.header();
    }

    @Override
    public List<String[]> dump(int mode) {
        return ResearchDirectory.snapshot().stream().map(ResearchDirectory::row).toList();
    }

    @Override
    public void dumpTo(File file) throws IOException {
        ResearchDirectory.export(file.toPath(), getMode() == 0 ? "csv" : "json");
    }
}
