/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx;
import java.awt.color.ColorSpace;
import snap.util.*;

/**
 * This class represents an RGBA color.
 */
public class Color implements Paint, XMLArchiver.Archivable {
    
    // RGBA components
    double     _red, _green, _blue, _alpha = 1;
    
    // Common Colors
    public static Color BLACK = new Color(0d);
    public static Color BLUE = new Color(0f, 0f, 1f);
    public static Color CYAN = new Color(1f, 0f, 0f, 0f, 1f);
    public static Color DARKGRAY = new Color(.333f);
    public static Color GRAY = new Color(.5f);
    public static Color GREEN = new Color(0f, 1f, 0f);
    public static Color LIGHTGRAY = new Color(.753f);
    public static Color MAGENTA = new Color(0f, 1f, 0f, 0f, 1f);
    public static Color ORANGE = new Color(1f, 200/255f, 0f);
    public static Color PINK = new Color(1f, 175/255f, 175/255f);
    public static Color RED = new Color(1f, 0f, 0f);
    public static Color WHITE = new Color(1d);
    public static Color YELLOW = new Color(0f, 0f, 1f, 0f, 1f);
    public static Color CLEAR = new Color(0f, 0f, 0f, 0f);
    public static Color LIGHTBLUE = new Color(.333f, .333f, 1f);
    public static Color CLEARWHITE = new Color(1f, 1f, 1f, 0f);
    public static Color CRIMSON = Color.get("#dc143c");

/**
 * Creates a plain black opaque color.
 */
public Color() { }

/**
 * Creates a plain black opaque color.
 */
public Color(int aRGBA)  { this(aRGBA>>16 & 0xff, aRGBA>>8 & 0Xff, aRGBA & 0xff, aRGBA>>24 & 0xff); }

/**
 * Creates a color with the given gray value (0-1).
 */
public Color(double g)  { _red = g; _green = g; _blue = g; }

/**
 * Creates a color with the given gray and alpha values (0-1).
 */
public Color(double g, double a)  { _red = g; _green = g; _blue = g; _alpha = a; }

/**
 * Creates a color with the given red, green blue values (0-1).
 */
public Color(double r, double g, double b)  { _red = r; _green = g; _blue = b; }

/**
 * Creates a color with the given red, green blue values (0-1).
 */
public Color(int r, int g, int b)  { _red = r/255f; _green = g/255f; _blue = b/255f; }

/**
 * Creates a color with the given red, green blue values (0-1).
 */
public Color(int r, int g, int b, int a)  { _red = r/255f; _green = g/255f; _blue = b/255f; _alpha = a/255f; }

/**
 * Creates a color with the given red, green, blue values (0-1).
 */
public Color(double r, double g, double b, double a)  { _red = r; _green = g; _blue = b; _alpha = a; }

/**
 * Creates a color with the given cyan, magenta, yellow, black and alpha values (0-1). Bogus right now.
 */
public Color(double c, double m, double y, double k, double a)  { _red =1-c; _green =1-m; _blue =1-y; _alpha = a; }

/**
 * Creates a new color from the given hex string.
 */
public Color(String aHexString)
{
    int start = aHexString.charAt(0)=='#'? 1 : 0;
    _red = Integer.decode("0x" + aHexString.substring(start, start + 2)).intValue()/255f;
    _green = Integer.decode("0x" + aHexString.substring(start + 2, start + 4)).intValue()/255f;
    _blue = Integer.decode("0x" + aHexString.substring(start + 4, start + 6)).intValue()/255f;
    if(aHexString.length() >= start + 8)
        _alpha = Integer.decode("0x" + aHexString.substring(start + 6, start + 8)).intValue()/255f;
}

/**
 * Returns the red component in the range 0-1.
 */
public double getRed()  { return _red; }

/**
 * Returns the green component in the range 0-1.
 */
public double getGreen()  { return _green; }

/**
 * Returns the blue component in the range 0-1.
 */
public double getBlue()  { return _blue; }

/**
 * Returns the alpha component in the range 0-1.
 */
public double getAlpha()  { return _alpha; }

/**
 * Returns the red component in the range 0-255.
 */
public int getRedInt()  { return (int)Math.round(_red*255); }

/**
 * Returns the green component in the range 0-255.
 */
public int getGreenInt()  { return (int)Math.round(_green*255); }

/**
 * Returns the blue component in the range 0-255.
 */
public int getBlueInt()  { return (int)Math.round(_blue*255); }

/**
 * Returns the alpha component in the range 0-255.
 */
public int getAlphaInt()  { return (int)Math.round(_alpha*255); }

/**
 * Returns the color as an int.
 */
public int getRGB()
{
    int r = getRedInt(), g = getGreenInt(), b = getBlueInt();
    return ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | ((b & 0xFF) << 0);
}

/**
 * Returns the color as an int.
 */
public int getRGBA()
{
    int r = getRedInt(), g = getGreenInt(), b = getBlueInt(), a = getAlphaInt();
    return ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | ((b & 0xFF) << 0);
}

/**
 * Returns a color brighter than this color (blended with white).
 */
public Color brighter()  { return blend(WHITE, .3); }

/**
 * Returns a color darker than this color (blended with black).
 */
public Color darker()  { return blend(BLACK, .3); }

/**
 * Returns a color darker than this color (by this given fraction).
 */
public Color blend(Color aColor, double aFraction)
{
    // Return real objects if zero or 1
    if(aFraction==0) return this; else if(aFraction==1) return aColor;
    
    // Get blended components and return new color
    double r = _red + (aColor._red - _red)*aFraction;
    double g = _green + (aColor._green - _green)*aFraction;
    double b = _blue + (aColor._blue - _blue)*aFraction;
    double a = _alpha + (aColor._alpha - _alpha)*aFraction;
    return new Color(r, g, b, a);
}

/**
 * Returns whether paint is defined in terms independent of primitive to be filled.
 */
public boolean isAbsolute()  { return true; }

/**
 * Returns whether paint is opaque.
 */
public boolean isOpaque()  { return _alpha>=1; }
    
/**
 * Returns an absolute paint for given bounds of primitive to be filled.
 */
public Color copyFor(Rect aRect)  { return this; }

/**
 * Standard equals implementation.
 */
public boolean equals(Object anObj)
{
    // Check identity and get other
    if(anObj==this) return true;
    Color other = anObj instanceof Color? (Color)anObj : null; if(other==null) return false;
    
    // Check components
    if(other._red!=_red) return false;
    if(other._green!=_green) return false;
    if(other._blue!=_blue) return false;
    if(other._alpha!=_alpha) return false;
    return true; // Return true since all checks passed
}

/**
 * Standard Hashcode implementation.
 */
public int hashCode()  { return getRGBA(); }

/**
 * Returns a string representation of this color.
 */
public String toString()  { return "{" + _red + " " + _green + " " + _blue + "}"; }

/**
 * Returns a hex string representation of this color.
 */
public String toHexString()
{
    // Allocate string buffer and get integer rgba components
    StringBuffer sb = new StringBuffer();
    int r = getRedInt(), g = getGreenInt(), b = getBlueInt(), a = getAlphaInt();
    
    // Add r, g, b components (and alpha, if not full) and return string
    if(r<16) sb.append('0'); sb.append(Integer.toHexString(r));
    if(g<16) sb.append('0'); sb.append(Integer.toHexString(g));
    if(b<16) sb.append('0'); sb.append(Integer.toHexString(b));
    if(a<255) { if(a<16) sb.append('0'); sb.append(Integer.toHexString(a)); }
    return sb.toString();
}

/**
 * Returns a color value for a given object.
 */
public static Color get(Object anObj)
{
    // Handle colors
    if(anObj instanceof Color) return (Color)anObj;
    
    // Handle string
    if(anObj instanceof String) { String cs = (String)anObj; cs.trim();
        
        // Try normal string constructor
        try { return new Color(cs); }
        catch(Exception e) { }
        
        // Try to lookup color by name
        cs = cs.toUpperCase();
        switch(cs) {
            case "WHITE": return Color.WHITE;
            case "BLACK": return Color.BLACK;
            case "RED": return Color.RED;
            case "GREEN": return Color.GREEN;
            case "BLUE": return Color.BLUE;
            case "CYAN": return Color.CYAN;
            case "MAGENTA": return Color.MAGENTA;
            case "YELLOW": return Color.YELLOW;
            case "ORANGE": return Color.ORANGE;
            case "PINK": return Color.PINK;
            case "CRIMSON": return Color.CRIMSON;
            case "GRAY": return Color.GRAY;
            case "CLEAR": return Color.CLEAR;
            case "CLEARWHITE": return Color.CLEARWHITE;
            case "LIGHTGRAY": case "LIGHT_GRAY": return Color.LIGHTGRAY;
            case "DARKGRAY": case "DARK_GRAY": return Color.DARKGRAY;
            case "RANDOM": return getRandom();
            default: //try { return (Color)Color.class.getField(cs).get(null); } catch(Exception e) { }
        }
        
        // Look in colors list
        for(int i=0; i<_colors.length; i+=2) if(cs.equals(_colors[i])) return get(_colors[i+1]);
        
        // Handle "LIGHT " or "DARK " anything
        if(cs.startsWith("LIGHT ")) { Color c = get(cs.substring(6));
            return c!=null? c.brighter() : null; }
        if(cs.startsWith("DARK ")) { Color c = get(cs.substring(5));
            return c!=null? c.darker() : null; }
    }
    
    // Treat numbers as 32bit RGBA ints
    if(anObj instanceof Number) { Number number = (Number)anObj; int rgba = number.intValue();
        float comps[] = new float[4]; for(int i=0; i<4; ++i) { comps[i] = (rgba & 0xff) / 255f; rgba >>= 8; }
        return new Color(comps[3], comps[2], comps[1], comps[0]);
    }
    
    // Return null
    return null;
}

/** Returns a color value for a given object. */
public static Color colorValue(Object anObj) { return get(anObj); }

/**
 * Converts this color to a CIELab triplet
 */
public float[] toLab()  { return rgbToLab(_red, _green, _blue); }

/**
 * Converts an RGB triplet to a CIELab triplet 
 */
public static float[] rgbToLab(double r, double g, double b)
{
    // Get the standard rgb space and convert from RGB to XYZ conversion space
    ColorSpace rgbSpace = ColorSpace.getInstance(ColorSpace.CS_sRGB);
    float xyz[] = rgbSpace.toCIEXYZ(new float[] { (float)r, (float)g, (float)b});
    
    // This is the D50 whitepoint as defined by awt
    double d50[] = {.9642, 1.0, .8249};
    
    // Convert from XYZ to LAB
    double fy = LABTransformFn(xyz[1]/d50[1]);
    float lab[] = new float[3];
    lab[0] = (float)(116 * fy - 16);
    lab[1] = (float)(500 * (LABTransformFn(xyz[0]/d50[0]) - fy));
    lab[2] = (float)(200 * (fy - LABTransformFn(xyz[2]/d50[2])));
    return lab;
}

/**
 * Private function used by RGB->LAB conversions
 */
private static double LABTransformFn(double t)  { return t>0.008856 ? Math.pow(t, 1d/3) : 7.787*t+16d/16; }

/**
 * Returns a random color.
 */
public static Color getRandom()
{
    return new Color(MathUtils.randomFloat(1), MathUtils.randomFloat(1), MathUtils.randomFloat(1));
}
    
/**
 * XML archival.
 */
public XMLElement toXML(XMLArchiver anArchiver)
{
    XMLElement e = new XMLElement("color");
    e.add("value", "#" + toHexString());
    return e;
}

/**
 * XML unarchival.
 */
public Object fromXML(XMLArchiver anArchiver, XMLElement anElement)
{
    String color = anElement.getAttributeValue("color");
    if(color!=null) return new Color(color);

    String hex = anElement.getAttributeValue("value");
    int start = hex.charAt(0)=='#'? 1 : 0;
    _red = Integer.decode("0x" + hex.substring(start, start + 2)).intValue()/255f;
    _green = Integer.decode("0x" + hex.substring(start + 2, start + 4)).intValue()/255f;
    _blue = Integer.decode("0x" + hex.substring(start + 4, start + 6)).intValue()/255f;
    if(hex.length()>=start+8) _alpha = Integer.decode("0x" + hex.substring(start + 6, start + 8)).intValue()/255f;
    return this;
}

// Some colors:
static String _colors[] = {
    "BEIGE", "#F5F5DC", "BROWN", "#A52A2A", "CRIMSON", "#DC143C", "FUCHSIA", "#FF00FF", "GOLD", "#FFD700",
    "GOLDENROD", "#DAA520", "HOTPINK", "#FF69B4", "INDIGO", "#4B0082", "IVORY", "#FFFFF0",
    "KHAKI", "#F0E68C", "LAVENDER", "#E6E6FA", "LIME", "#00FF00", "MAROON", "#800000", "NAVY", "#000080",
    "OLIVE", "#808000", "PLUM", "#DDA0DD", "POWDERBLUE", "#B0E0E6", "PURPLE", "#800080",
    "SALMON", "#FA8072", "SILVER", "#C0C0C0", "SKYBLUE", "#87CEEB", "TAN", "#D2B48C", "TEAL", "#008080",
    "VIOLET", "#EE82EE"
};

}