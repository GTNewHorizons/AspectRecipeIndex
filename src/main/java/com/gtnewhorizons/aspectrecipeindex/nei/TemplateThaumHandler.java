package com.gtnewhorizons.aspectrecipeindex.nei;

import java.util.ArrayList;

import com.gtnewhorizons.aspectrecipeindex.client.ARIClient;

import codechicken.nei.recipe.TemplateRecipeHandler;
import thaumcraft.api.aspects.AspectList;

public abstract class TemplateThaumHandler extends TemplateRecipeHandler {

    protected ARIClient ariClient = ARIClient.getInstance();
    protected ArrayList<AspectList> aspectsAmount = new ArrayList<>();
}
