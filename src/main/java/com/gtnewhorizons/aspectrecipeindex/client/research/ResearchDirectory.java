package com.gtnewhorizons.aspectrecipeindex.client.research;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import net.minecraft.item.Item;

import com.google.gson.GsonBuilder;

import thaumcraft.api.research.ResearchCategories;

public class ResearchDirectory {

    public record Entry(String id, String category, String title, List<String> parents, List<String> hiddenParents,
            boolean hidden, boolean lost, boolean concealed, boolean virtual, String categoryName, String description,
            List<String> siblings, List<String> flags, Icon icon) {

        public Entry {
            parents = List.copyOf(parents);
            hiddenParents = List.copyOf(hiddenParents);
            siblings = List.copyOf(siblings);
            flags = List.copyOf(flags);
        }
    }

    public record Icon(String itemId, int meta, String nbt, String texture) {}

    public static List<Entry> snapshot() {
        var entries = new ArrayList<Entry>();
        for (var category : ResearchCategories.researchCategories.values()) {
            for (var item : category.research.values()) {
                var flags = new ArrayList<String>();
                if (item.isHidden()) flags.add("hidden");
                if (item.isLost()) flags.add("lost");
                if (item.isConcealed()) flags.add("concealed");
                if (item.isVirtual()) flags.add("virtual");
                if (item.isSecondary()) flags.add("secondary");
                if (item.isSpecial()) flags.add("special");
                if (item.isStub()) flags.add("stub");
                if (item.isRound()) flags.add("round");
                if (item.isAutoUnlock()) flags.add("autoUnlock");
                Icon icon = null;
                if (item.icon_item != null) {
                    var stack = item.icon_item;
                    Object name = Item.itemRegistry.getNameForObject(stack.getItem());
                    if (name != null) icon = new Icon(
                            name.toString(),
                            stack.getItemDamage(),
                            stack.hasTagCompound() ? stack.getTagCompound().toString() : null,
                            null);
                } else if (item.icon_resource != null) icon = new Icon(null, 0, null, item.icon_resource.toString());
                entries.add(
                        new Entry(
                                item.key,
                                item.category,
                                item.getName(),
                                item.parents == null ? List.of() : Arrays.asList(item.parents),
                                item.parentsHidden == null ? List.of() : Arrays.asList(item.parentsHidden),
                                item.isHidden(),
                                item.isLost(),
                                item.isConcealed(),
                                item.isVirtual(),
                                ResearchCategories.getCategoryName(item.category),
                                item.getText(),
                                item.siblings == null ? List.of() : Arrays.asList(item.siblings),
                                flags,
                                icon));
            }
        }
        entries.sort(Comparator.comparing(Entry::id));
        return List.copyOf(entries);
    }

    public static void export(Path path, String format) throws IOException {
        List<Entry> entries = snapshot();
        String content;
        if ("json".equals(format))
            content = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(entries) + "\n";
        else if ("csv".equals(format)) {
            var csv = new StringBuilder();
            appendRow(csv, header());
            for (var entry : entries) appendRow(csv, row(entry));
            content = csv.toString();
        } else throw new IllegalArgumentException("Unknown research directory format");
        Files.createDirectories(path.toAbsolutePath().getParent());
        Files.writeString(path, content, StandardCharsets.UTF_8);
    }

    private static String cell(String value) {
        return "\"" + (value == null ? "" : value.replace("\"", "\"\"")) + "\"";
    }

    public static String[] header() {
        return new String[] { "id", "category", "title", "parents", "hiddenParents", "hidden", "lost", "concealed",
                "virtual", "categoryName", "description", "siblings", "flags", "itemId", "meta", "nbt", "texture" };
    }

    public static String[] row(Entry entry) {
        Icon icon = entry.icon();
        return new String[] { entry.id(), entry.category(), entry.title(), String.join(";", entry.parents()),
                String.join(";", entry.hiddenParents()), Boolean.toString(entry.hidden()),
                Boolean.toString(entry.lost()), Boolean.toString(entry.concealed()), Boolean.toString(entry.virtual()),
                entry.categoryName(), entry.description(), String.join(";", entry.siblings()),
                String.join(";", entry.flags()), icon == null ? null : icon.itemId(),
                icon == null ? null : Integer.toString(icon.meta()), icon == null ? null : icon.nbt(),
                icon == null ? null : icon.texture() };
    }

    private static void appendRow(StringBuilder csv, String[] values) {
        for (int index = 0; index < values.length; index++) {
            if (index != 0) csv.append(',');
            csv.append(cell(values[index]));
        }
        csv.append("\r\n");
    }
}
