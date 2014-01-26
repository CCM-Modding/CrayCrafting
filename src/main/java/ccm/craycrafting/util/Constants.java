/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 Dries K. Aka Dries007 and the CCM modding crew.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package ccm.craycrafting.util;

import ccm.craycrafting.CrayCrafting;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraftforge.oredict.ShapedOreRecipe;

import java.lang.reflect.Field;

public class Constants
{
    public static final String MODID = "CrayCrafting";

    public static final String DUMMY_CHARS = "azertyuiopqsdfghjklmwxcvbn"; // Dummy caracters for the map

    public static final String NBT_shapedRecipes       = "shapedRecipes";
    public static final String NBT_shapelessRecipes    = "shapelessRecipes";
    public static final String NBT_shapedOreRecipes    = "shapedOreRecipes";
    public static final String NBT_shapelessOreRecipes = "shapelessOreRecipes";

    public static final String NBT_recipeWidth   = "recipeWidth";
    public static final String NBT_recipeHeight  = "recipeHeight";
    public static final String NBT_input         = "input";
    public static final String NBT_oldOutput     = "oldOutput";
    public static final String NBT_newOutput     = "newOutput";
    public static final String NBT_field_92101_f = "field_92101_f";
    public static final String NBT_map           = "map";
    public static final String NBT_mirror        = "mirror";
    public static final String NBT_oredictname   = "oredictname";

    public static final Field ShapedRecipes_field_92101_f;
    public static final Field ShapedOreRecipe_width;
    public static final Field ShapedOreRecipe_height;
    public static final Field ShapedOreRecipe_mirror;

    static
    {
        try
        {
            ShapedRecipes_field_92101_f = ShapedRecipes.class.getDeclaredFields()[5];
            ShapedRecipes_field_92101_f.setAccessible(true);

            ShapedOreRecipe_mirror = ShapedOreRecipe.class.getDeclaredField("mirrored");
            ShapedOreRecipe_mirror.setAccessible(true);

            ShapedOreRecipe_width = ShapedOreRecipe.class.getDeclaredField("width");
            ShapedOreRecipe_width.setAccessible(true);

            ShapedOreRecipe_height = ShapedOreRecipe.class.getDeclaredField("height");
            ShapedOreRecipe_height.setAccessible(true);
        }
        catch (NoSuchFieldException e)
        {
            CrayCrafting.logger.severe("This is going to be a problem later, so I'm stopping it here.");
            throw new RuntimeException(e);
        }
    }
}
