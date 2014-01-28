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

/**
 * Constants!
 *
 * @author Dries007
 */
public class Constants
{
    private Constants() {}

    public static final String MODID = "CrayCrafting";

    public static final String CHANNEL_STATUS = MODID + "_s";
    public static final String CHANNEL_DATA   = MODID + "_d";

    public static final String STATUS_RESET = "reset";

    public static final String DUMMY_CHARS = "azertyuiopqsdfghjklmwxcvbn"; // Dummy caracters for the map in ShapedOreRecipeType

    public static final String NBT_recipeWidth   = "recipeWidth";
    public static final String NBT_recipeHeight  = "recipeHeight";
    public static final String NBT_input         = "input";
    public static final String NBT_output        = "output";
    public static final String NBT_field_92101_f = "field_92101_f";
    public static final String NBT_map           = "map";
    public static final String NBT_mirror        = "mirror";
    public static final String NBT_oredictname   = "oredictname";
}
