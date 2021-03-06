/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx;

/**
 * A custom class.
 */
public enum VPos {

    TOP,
    CENTER,
    BOTTOM,
    BASELINE;
        
/**
 * Returns the pos as a double from 0 to 1.
 */
public double asDouble()  { return this==TOP? 0 : this==CENTER? .5 : 1; }

/**
 * Returns the VPos.
 */
public static VPos get(String aStr)
{
    try { VPos.valueOf(aStr); }
    catch(Exception e) { }
    for(VPos p : VPos.values()) if(p.toString().equalsIgnoreCase(aStr)) return p;
    return TOP;
}

}