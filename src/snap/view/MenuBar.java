package snap.view;
import java.util.List;
import snap.gfx.*;
import snap.util.*;

/**
 * A View to display menus in a menu bar.
 */
public class MenuBar extends ParentView {

    // The layout
    ViewLayout.HBoxLayout  _layout = new ViewLayout.HBoxLayout(this);
    
    // The menubar fill
    Color c1 = new Color("#fefefeff"), c2 = new Color("#f5f5f5ff"), c3 = new Color("#f0f0f0ff");
    Color c4 = new Color("#ebebebff"), c5 = new Color("#e7e7e7ff");
    GradientPaint.Stop stops[] = GradientPaint.getStops(new double[] { 0,.25,.5,.75,1 }, c1, c2, c3, c4, c5);
    GradientPaint MENU_BAR_PAINT = new GradientPaint(0,0,0,1,stops);

/**
 * Creates a new MenuBarNode.
 */
public MenuBar()
{
    setFill(MENU_BAR_PAINT); setPadding(0,10,0,10); _layout.setFillHeight(true);
    setFont(new Font("Arial", 14));
}

/**
 * Returns the child menus.
 */
public List <Menu> getMenus()  { return (List)getChildren(); }

/**
 * Returns the number of menus.
 */
public int getMenuCount()  { return getChildCount(); }

/**
 * Override to return child as Menu.
 */
public Menu getMenu(int anIndex)  { return (Menu)getChild(anIndex); }

/**
 * Adds a Menu.
 */
public void addMenu(Menu aMenu)  { addChild(aMenu); }

/**
 * Remove's the child at the given index from this node's children list.
 */
public View removeMenu(int anIndex)  { return removeChild(anIndex); }

/**
 * Removes the given menu from this node's children list.
 */
public int removeMenu(View aChild)  { return removeChild(aChild); }

/**
 * Returns the menu showing (or null).
 */
public Menu getMenuShowing()  { for(Menu m : getMenus()) if(m.isPopupShowing()) return m; return null; }

/**
 * Override to handle accelerators.
 */
protected void processEvent(ViewEvent anEvent)
{
    for(View item : getChildren()) {
        MenuItem match = getMatchingMenuItem((MenuItem)item, anEvent);
        if(match!=null) {
            match.fireActionEvent(); return; }
    }
}

/**
 * Returns a matching menu item.
 */
public MenuItem getMatchingMenuItem(MenuItem aMI, ViewEvent anEvent)
{
    if(aMI instanceof Menu) { Menu menu = (Menu)aMI;
        for(MenuItem item : menu.getItems()) {
            MenuItem match = getMatchingMenuItem(item, anEvent);
            if(match!=null)
                return match;
        }
    }
    else if(anEvent.getKeyCombo().equals(aMI.getShortcutCombo()))
        return aMI;
    return null;
}

/**
 * Paints background.
 */
protected void paintBack(Painter aPntr)
{
    super.paintBack(aPntr); double w = getWidth(), h = getHeight() - .5;
    aPntr.setColor(Color.LIGHTGRAY); aPntr.setStroke(Stroke.Stroke1); aPntr.drawLine(0,h,w,h);
}

/**
 * Returns the preferred height.
 */
protected double getMinHeightImpl()  { return getFont().getSize() + 10; }

/**
 * Returns the preferred width.
 */
protected double getPrefWidthImpl(double aH)  { return _layout.getPrefWidth(-1); }

/**
 * Returns the preferred height.
 */
protected double getPrefHeightImpl(double aW)  { return _layout.getPrefHeight(-1); }

/**
 * Layout children.
 */
protected void layoutChildren()  { _layout.layoutChildren(); }

/**
 * XML archival.
 */
public XMLElement toXMLView(XMLArchiver anArchiver)
{
    XMLElement e = super.toXMLView(anArchiver); e.setName("MenuBar"); return e;
}

/**
 * XML archival of children.
 */
protected void toXMLChildren(XMLArchiver anArchiver, XMLElement anElement)
{
    // Archive children
    for(int i=0, iMax=getMenuCount(); i<iMax; i++) { Menu child = getMenu(i);
        anElement.add(anArchiver.toXML(child, this)); }    
}

/**
 * XML unarchival for shape children.
 */
protected void fromXMLChildren(XMLArchiver anArchiver, XMLElement anElement)
{
    // Iterate over child elements and unarchive MenuItems
    for(int i=0, iMax=anElement.size(); i<iMax; i++) { XMLElement childXML = anElement.get(i);
        Class cls = anArchiver.getClass(childXML.getName());
        if(cls!=null && Menu.class.isAssignableFrom(cls)) {
            Menu menu = (Menu)anArchiver.fromXML(childXML, this);
            addMenu(menu);
        }
    }
}

}