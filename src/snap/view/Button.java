package snap.view;
import snap.gfx.*;

/**
 * An RMShape subclass for JButton.
 */
public class Button extends ButtonBase {

    // Whether this button is the window default button
    boolean              _default;
    
    // Whether this button is the window cancel button
    boolean              _cancel;
    
    // Constants for properties
    public static final String DefaultButton_Prop = "DefaultButton";
    public static final String CancelButton_Prop = "CancelButton";

/**
 * Creates a new Button.
 */
public Button()  { }

/**
 * Creates a new Button with given text.
 */
public Button(String aStr)  { setText(aStr); }

/**
 * Returns whether button is window default button.
 */
public boolean isDefaultButton()  { return _default; }

/**
 * Sets whether button is window default button.
 */
public void setDefaultButton(boolean aValue)
{
    if(aValue==_default) return;
    firePropChange(DefaultButton_Prop, _default, _default=aValue);
}

/**
 * Returns whether button is window cancel button.
 */
public boolean isCancelButton()  { return _cancel; }

/**
 * Sets whether button is window cancel button.
 */
public void setCancelButton(boolean aValue)
{
    if(aValue==_cancel) return;
    firePropChange(CancelButton_Prop, _cancel, _cancel=aValue);
}

/**
 * Paint Button.
 */
public void paintFront(Painter aPntr)
{
    if(isShowBorder() && isDefaultButton()) {
        aPntr.clearRect(0,0,getWidth(),getHeight());
        int state = isPressed()? Painter.BUTTON_PRESSED : _targeted? Painter.BUTTON_OVER : Painter.BUTTON_NORMAL;
        paintDefaultButton(aPntr, 0,0,getWidth(),getHeight(), state);
    }
    else super.paintFront(aPntr);
}

/**
 * Initializes newly created object.
 */
public void initUI(ViewOwner anOwner)
{
    super.initUI(anOwner);  // Do normal init
    
    // Register default or cancel buttons
    if(isDefaultButton())
        anOwner.addKeyActionEvent(getName(), "ENTER");
    else if(isCancelButton())
        anOwner.addKeyActionEvent(getName(), "ESCAPE");
}
    
/**
 * Draws a button for the given rect with an option for pressed.
 */
public static void paintDefaultButton(Painter aPntr, double x, double y, double w, double h, int aState)
{
    RoundRect rect = new RoundRect(x,y,w,h,3); aPntr.setPaint(back); aPntr.fill(rect);  // Background grad
    rect.setRect(x+.5,y+.5,w-1,h); aPntr.setColor(_c6); aPntr.draw(rect);             // Paint outer bottom ring lt gray
    rect.setRect(x+1.5,y+1.5,w-3,h-4); aPntr.setPaint(ring1); aPntr.draw(rect);    // Paint inner ring light gray
    rect.setRect(x+.5,y+.5,w-1,h-1); aPntr.setPaint(ring2); aPntr.draw(rect);           // Paint outer ring
    
    // Handle BUTTON_OVER, BUTTON_PRESSED
    if(aState==BUTTON_OVER) { aPntr.setPaint(_over); rect.setRect(x,y,w,h); aPntr.fill(rect); }
    else if(aState==BUTTON_PRESSED) { aPntr.setPaint(_prsd); rect.setRect(x,y,w,h); aPntr.fill(rect); }
}

// Outer ring and outer lighted ring
static Color _c6 = Color.get("#ffffffBB");
static Color _over = Color.get("#FFFFFF50"), _prsd = Color.get("#0000001A");

// Button background gradient for default buttons (light gray top to dark gray bottom)
static Color ring2 = Color.get("#83a6b6");
static Color _b1 = Color.get("#b1daed"), _b2 = Color.get("#9ec7db");
static GradientPaint back = new GradientPaint(.5, 0, .5, 1,GradientPaint.getStops(0, _b1, 1, _b2));

// Button background gradient for default buttons (light gray top to dark gray bottom)
static Color _b3 = Color.get("#d6ecf6"), _b4 = Color.get("#a5d1e4");
static GradientPaint ring1 = new GradientPaint(.5, 0, .5, 1, GradientPaint.getStops(0, _b3, 1, _b4));

}