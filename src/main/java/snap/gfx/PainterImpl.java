/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.gfx;
import java.util.Arrays;

/**
 * A basic implementation of a painter.
 */
public abstract class PainterImpl extends Painter {

    // The current graphics state
    protected GState      _gstate = new GState();
    
    // The GState stack
    protected GState      _gstates[] = new GState[8];
    
    // The GState stack size
    protected int         _gsize;
    
    // The current marked shape
    Shape                 _mshape;
    
    // Whether marked shape is opaque
    boolean               _opaque = true;

/** Returns the current font. */
public Font getFont()  { return _gstate.font; }

/** Sets the current font. */
public void setFont(Font aFont)  { _gstate.font = aFont; }

/** Returns the current paint. */
public Paint getPaint()  { return _gstate.paint; }

/** Sets the current paint. */
public void setPaint(Paint aPaint)  { _gstate.paint = aPaint; }

/** Returns the current stroke. */
public Stroke getStroke()  { return _gstate.stroke; }

/** Sets the current stroke. */
public void setStroke(Stroke aStroke)  { _gstate.stroke = aStroke; }

/** Returns the opacity. */
public double getOpacity()  { return _gstate.opacity; }

/** Sets the opacity. */
public void setOpacity(double aValue)  { _gstate.opacity = aValue; }

/** Stroke the given shape. */
public void draw(Shape aShape)  { updateMarkedBounds(aShape, false); }

/** Fill the given shape. */
public void fill(Shape aShape)  { updateMarkedBounds(aShape, getPaint().isOpaque()); }

/** Draw image with transform. */
public void drawImage(Image anImg, Transform aTrans)
{
    Shape shp = aTrans.createTransformedShape(new Rect(0,0,anImg.getWidth(),anImg.getHeight()));
    updateMarkedBounds(shp, !anImg.hasAlpha());
}

/** Draw image in rect. */
public void drawImage(Image img, double sx, double sy, double sw, double sh, double dx, double dy, double dw, double dh)
{
    updateMarkedBounds(new Rect(dx,dy,dw,dh), !img.hasAlpha());
}

/** Draw string at location with char spacing. */
public void drawString(String aStr, double aX, double aY, double cs)
{
    Rect rect = getFont().getStringBounds(aStr); rect.offset(aX,aY);
    updateMarkedBounds(rect, false);
}

/**
 * Updates marked bounds.
 */
private void updateMarkedBounds(Shape aShape, boolean isOpaque)
{
    // Get marked shape in global coords
    Shape mshp = _gstate.xform.createTransformedShape(aShape);
    
    // If no marked shape yet, just set
    if(_mshape==null) { _mshape = mshp; _opaque = isOpaque; }
    
    // Otherwise if new shape doesn't fit in current marked bounds, set to new shape (if it ecompasses) or union shape
    else if(!_mshape.contains(mshp)) {
        if(mshp.contains(_mshape))  { _mshape = mshp; _opaque = isOpaque; }
        else { _mshape = _mshape.getBounds().getUnionRect(mshp.getBounds()); _opaque = false; }
    }
}

/**
 * Returns the marked shape.
 */
public Shape getMarkedShape()  { return _mshape; }

/**
 * Returns whether marked shape is opaque.
 */
public boolean isMarkedShapeOpaque()  { return _opaque; }

/**
 * Transform by transform.
 */
public Transform getTransform()  { return _gstate.xform; }

/**
 * Transform by transform.
 */
public void setTransform(Transform aTrans)
{
    if(_gstate.clip!=null) _gstate.clip = _gstate.xform.getInverse().createTransformedShape(_gstate.clip);
    _gstate.xform = aTrans;
    if(_gstate.clip!=null) _gstate.clip = aTrans.createTransformedShape(_gstate.clip);
}

/**
 * Transform by transform.
 */
public void transform(Transform aTrans)
{
    _gstate.xform.multiply(aTrans);
    if(_gstate.clip!=null) _gstate.clip = aTrans.createTransformedShape(_gstate.clip);
}

/**
 * Return clip shape.
 */
public Shape getClip()  { return _gstate.clip; }

/**
 * Clip by shape.
 */
public void clip(Shape aShape)
{
    _gstate.clip = _gstate.clip!=null? Shape.intersect(_gstate.clip, aShape) : aShape;
}

/**
 * Saves the graphics state.
 */
public void save()
{
    if(_gsize==_gstates.length) _gstates = Arrays.copyOf(_gstates, _gstates.length*2);
    _gstates[_gsize++] = _gstate; _gstate = _gstate.clone();
}

/**
 * Restores the graphics state.
 */
public void restore()
{
    _gstate = _gstates[--_gsize];
}

/**
 * Returns the current gstate.
 */
public GState getGState()  { return _gstate; }

/**
 * The graphics state.
 */
public static class GState implements Cloneable {
    
    // Paint
    public Paint         paint = Color.BLACK;
    
    // Font
    public Font          font = Font.Arial12;
    
    // Stroke
    public Stroke        stroke = Stroke.Stroke1;
    
    // Opacity
    public double        opacity = 1;
    
    // Transform
    public Transform     xform = new Transform();
    
    // Clip
    public Shape         clip = NO_CLIP;
    public static Shape  NO_CLIP = new Rect(-5000,-5000,10000,10000);
    
    /** Standard clone implementation. */
    public GState clone()
    {
        GState clone = null; try { clone = (GState)super.clone(); }
        catch(CloneNotSupportedException e) { throw new RuntimeException(e); }
        clone.xform = xform.clone();
        return clone;
    }
}

}