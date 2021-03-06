/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import java.util.*;
import java.util.function.Consumer;
import snap.gfx.*;
import snap.util.*;

/**
 * A View to show a list of items.
 */
public class ListView <T> extends ParentView implements View.Selectable <T> {

    // The items
    List <T>              _items = new ArrayList();
    
    // The row height
    double                _rowHeight;
    
    // The cell padding
    Insets                _cellPad = getCellPaddingDefault();
    
    // The SelectionMode
    int                   _selMode;
    
    // The selected index
    int                   _selIndex = -1;
    
    // The Cell Configure method
    Consumer <ListCell<T>>  _cellConf;
    
    // The paint for alternating cells
    Paint                 _altPaint = ALTERNATE_GRAY;
    
    // Whether list distinguishes item under the mouse
    boolean               _targeting;
    
    // The index of the item currently being targeted
    int                   _targetedIndex = -1;
    
    // The paint for targeted Fill and TextFill
    Paint                 _targTextFill = Color.WHITE;
    
    // The index of the first visible cell
    int                   _cellStart = -1, _cellEnd;
    
    // List of items that need to be updated
    Set <T>               _updateItems = new HashSet();
    
    // Value of cell width/height
    double                _sampleWidth = -1, _sampleHeight = -1;
    
    // The layout for cells
    ViewLayout.VBoxLayout _layout = new ViewLayout.VBoxLayout(this);
    
    // Shared CellPadding default
    static Insets         _cellPadDefault = new Insets(2,2,2,4);
    
    // Shared constants for colors
    private static Paint ALTERNATE_GRAY = Color.get("#F8F8F8");
    
    // Constants for properties
    public static final String Items_Prop = "Items";
    public static final String SelectedItem_Prop = "SelectedItem";
    public static final String SelectedIndex_Prop = "SelectedIndex";
    public static final String CellPadding_Prop = "CellPadding";

/**
 * Creates a new ListNode.
 */
public ListView()
{
    enableEvents(MousePressed, KeyPressed, Action);
    setFocusable(true); setFocusWhenPressed(true);
    setFill(Color.WHITE);
    _layout.setFillWidth(true);
}

/**
 * Returns the items.
 */
public List <T> getItems()  { return _items; }

/**
 * Sets the items.
 */
public void setItems(List <T> theItems)
{
    if(equalsItems(theItems)) return;
    T sitem = getSelectedItem();
    _items.clear();
    if(theItems!=null) _items.addAll(theItems);
    setSelectedItem(sitem);
    relayout();
    _sampleWidth = _sampleHeight = -1;
    relayoutParent();
}

/**
 * Sets the items.
 */
public void setItems(T ... theItems)  { setItems(theItems!=null? Arrays.asList(theItems) : null); }

/** Returns the selection mode. Replace with MultipleChoice. SingleInterval. */
//public int getSelectionMode()  { return _selMode; }
//public void setSelectionMode(int aMode)  { firePropertyChange("SelectionMode", _selMode, _selMode = aMode); }

/**
 * Returns the selected index.
 */
public int getSelectedIndex()  { return _selIndex; }

/**
 * Sets the selected index.
 */
public void setSelectedIndex(int anIndex)
{
    if(anIndex==_selIndex) return;
    updateIndex(_selIndex);
    firePropChange("SelectedIndex", _selIndex, _selIndex = anIndex);
    updateIndex(_selIndex);
}

/**
 * Returns the minimum selected index.
 */
public int getSelectedIndexMin()
{
    int indexes[] = getSelectedIndices();
    int min = Integer.MAX_VALUE; for(int i : indexes) min = Math.min(min, i);
    return min!=Integer.MAX_VALUE? min : -1;
}

/**
 * Returns the maximum selected index.
 */
public int getSelectedIndexMax()
{
    int indexes[] = getSelectedIndices();
    int max = -1; for(int i : indexes) max = Math.max(max, i);
    return max;
}

/**
 * Returns the selected indices.
 */
public int[] getSelectedIndices()  { return _selIndex>=0? new int[] { _selIndex } : new int[0]; }

/**
 * Sets the selection interval.
 */
public void setSelectionInterval(int aStart, int anEnd)
{
    int min = Math.min(aStart,anEnd), max = Math.max(aStart,anEnd), len = max-min+1;
    int indexes[] = new int[len]; for(int i=0;i<len;i++) indexes[i] = i + min;
    setSelectedIndex(min);
}

/**
 * Returns the selected item.
 */
public T getSelectedItem()  { return _selIndex>=0 && _selIndex<_items.size()? _items.get(_selIndex) : null; }

/**
 * Sets the selected index.
 */
public void setSelectedItem(T anItem)
{
    int index = _items.indexOf(anItem);
    setSelectedIndex(index);
}

/**
 * Returns the list items as a single string with items separated by newlines.
 */
public String getItemsString()
{
    List <T> items = getItems(); if(items==null) return null;
    return ListUtils.joinStrings(items, "\n");
}

/**
 * Sets the list items as a single string with items separated by newlines.
 */
public void setItemsString(String aString)
{
    String items[] = aString!=null? aString.split("\n") : new String[0];
    for(int i=0; i<items.length; i++) items[i] = items[i].trim();
    setItems((T)items);
}

/**
 * Returns the row height.
 */
public double getRowHeight()
{
    if(_rowHeight>0) return _rowHeight;
    if(_sampleHeight<0) calcSampleSize(); return _sampleHeight;
}

/**
 * Sets the row height.
 */
public void setRowHeight(double aValue)  { _rowHeight = aValue; }

/**
 * Returns the cell padding.
 */
public Insets getCellPadding()  { return _cellPad; }

/**
 * Sets the cell padding.
 */
public void setCellPadding(Insets aPadding)
{
    if(aPadding==null) aPadding = getCellPaddingDefault();
    if(aPadding.equals(_cellPad)) return;
    firePropChange(CellPadding_Prop, _cellPad, _cellPad=aPadding);
    relayout();
}

/**
 * Returns the default cell padding.
 */
public Insets getCellPaddingDefault()  { return _cellPadDefault; }

/**
 * Returns the row at given Y location.
 */
public int getRowAt(double aY)
{
    int index = (int)(aY/getRowHeight());
    return Math.min(index, getItems().size()-1);
}

/**
 * Called to set method for rendering.
 */
public Consumer<ListCell<T>> getCellConfigure()  { return _cellConf; }

/**
 * Called to set method for rendering.
 */
public void setCellConfigure(Consumer<ListCell<T>> aCC)  { _cellConf = aCC; }

/**
 * Returns the paint for alternating cells.
 */
public Paint getAltPaint()  { return _altPaint; }

/**
 * Sets the paint for alternating cells.
 */
public void setAltPaint(Paint aPaint)  { _altPaint = aPaint; }

/**
 * Returns whether list shows visual cue for item under the mouse.
 */
public boolean isTargeting()  { return _targeting; }

/**
 * Sets whether list shows visual cue for item under the mouse.
 */
public void setTargeting(boolean aValue)
{
    if(aValue==_targeting) return;
    _targeting = aValue;
    if(_targeting) enableEvents(MouseMoved, MouseExited);
    else disableEvents(MouseMoved, MouseExited);
}

/**
 * Returns the index of the currently targeted cell.
 */
public int getTargetedIndex()  { return _targetedIndex; }

/**
 * Sets the index of the currently targeted cell.
 */
protected void setTargetedIndex(int anIndex)
{
    if(anIndex==_targetedIndex) return;
    updateIndex(_targetedIndex);
    _targetedIndex = anIndex;
    updateIndex(_targetedIndex);
}

/**
 * Called to update items in list that have changed.
 */
public void updateItems(T ... theItems)
{
    synchronized (_updateItems) {
        if(theItems!=null && theItems.length>0) Collections.addAll(_updateItems, theItems);
        else _updateItems.addAll(getItems());
    }
    relayout();
}

/**
 * Called to update items in the list that have changed, by index.
 */
public void updateIndex(int anIndex)
{
    T item = anIndex>=0 && anIndex<getItems().size()? getItems().get(anIndex) : null;
    if(item!=null) updateItems(item);
}

/**
 * Updates item at index (required to be in visible range).
 */
protected void updateCellAt(int anIndex)
{
    int cindex = anIndex - _cellStart;
    ListCell cell = createCell(anIndex), oldCell = getCell(cindex);
    configureCell(cell); cell.setBounds(oldCell.getBounds());
    cell.layout();
    removeChild(cindex); addChild(cell, cindex);
}

/**
 * Returns the cell at given index.
 */
public ListCell <T> getCell(int anIndex)  { return (ListCell)getChild(anIndex); }

/**
 * Returns the preferred width.
 */
protected double getPrefWidthImpl(double aH)  { if(_sampleWidth<0) calcSampleSize(); return _sampleWidth; }

/**
 * Returns the preferred height.
 */
protected double getPrefHeightImpl(double aW)  { return getRowHeight()*_items.size(); }

/**
 * Override to layout children with VBox layout.
 */
protected void layoutChildren()
{
    // Update CellStart/CellEnd for current ClipBounds
    Rect clip = getClipBoundsAll(); if(clip==null) clip = getBoundsInside(); double rh = getRowHeight();
    
    // Update CellStart/CellEnd for current ClipBounds
    _cellStart = (int)Math.max(clip.getY()/rh,0);
    _cellEnd = (int)(clip.getMaxY()/rh);
    
    // Remove cells before and/or after new visible range
    while(getChildCount()>0 && getCell(0).getRow()<_cellStart) removeChild(0);
    for(int i=getChildCount()-1; i>=0 && getCell(i).getRow()>_cellEnd; i--) removeChild(i);
    
    // Update cells in visible range: If row cell already set, update it, otherwise create, configure and add
    for(int i=_cellStart,cindex=0;i<=_cellEnd;i++,cindex++) {
        if(cindex<getChildCount()) {
            T item = i<getItems().size()? getItems().get(i) : null;
            ListCell cell = getCell(cindex);
            if(i<cell.getRow()) {
                ListCell cell2 = createCell(i); addChild(cell2,cindex);
                configureCell(cell2);
                cell.setBounds(0,i*rh,getWidth(),rh); cell.layout();
            }
            else if(item!=cell.getItem())
                updateCellAt(i);
        }
        else {
            ListCell cell = createCell(i); addChild(cell);
            configureCell(cell);
            cell.setBounds(0,i*rh,getWidth(),rh); cell.layout();
        }
    }
    
    // Update items
    T items[] = null; synchronized (_updateItems) { items = (T[])_updateItems.toArray(); _updateItems.clear(); }
    for(T item : items) {
        int index = getItems().indexOf(item);
        if(index>=_cellStart && index<=_cellEnd)
            updateCellAt(index);
    }
    
    // Do real layout
    _layout.layoutChildren();
}

/**
 * Creates a cell for item at index.
 */
protected ListCell createCell(int anIndex)
{
    T item = anIndex>=0 && anIndex<_items.size()? _items.get(anIndex) : null;
    ListCell cell = new ListCell(this, item, anIndex, getColIndex(), _selIndex==anIndex);
    cell.setPadding(getCellPadding());
    cell.setPrefHeight(getRowHeight());
    return cell;
}

/**
 * Called to configure a cell.
 */
protected void configureCell(ListCell <T> aCell)
{
    // Get item and set text
    Object item = aCell.getItem();
    if(getItemKey()!=null) item = GFXEnv.getEnv().getKeyChainValue(item, getItemKey());
    aCell.setText(item!=null? item.toString() : null);
    
    // Set Fill/TextFill based on selection
    if(aCell.isSelected()) { aCell.setFill(ViewUtils.getSelectFill());aCell.setTextFill(ViewUtils.getSelectTextFill());}
    else if(isTargeting() && aCell.getRow()==getTargetedIndex())  {
        aCell.setFill(ViewUtils.getTargetFill()); aCell.setTextFill(ViewUtils.getTargetTextFill()); }
    else if(aCell.getRow()%2==0) { aCell.setFill(_altPaint); aCell.setTextFill(Color.BLACK); }
    else { aCell.setFill(null); aCell.setTextFill(Color.BLACK); }

    // If cell configure set, call it
    Consumer cconf = getCellConfigure();
    if(cconf!=null) cconf.accept(aCell);
}

/**
 * Returns text for item.
 */
public String getText(T anItem)
{
    ListCell cell = new ListCell(this, anItem, 0, 0, false);
    configureCell(cell);
    return cell.getText();
}

/**
 * Returns the column index.
 */
protected int getColIndex()  { return 0; }

/**
 * Returns the insets.
 */
public Insets getInsetsAll()
{
    Insets ins = super.getInsetsAll();
    if(_cellStart>0) ins = Insets.add(ins, _cellStart*getRowHeight(), 0, 0, 0);
    return ins;
}

/**
 * Override to reset cells.
 */
public void setY(double aValue)  { if(aValue==getY()) return; super.setY(aValue); relayout(); }

/**
 * Override to reset cells.
 */
public void setHeight(double aValue)  { if(aValue==getHeight()) return; super.setHeight(aValue); relayout(); }

/**
 * Override to see if paint exposes missing cells. If so, request layout.
 * Should only happen under rare circumstances, like when a Scroller containing ListView grows.
 */
public void paintAll(Painter aPntr)
{
    super.paintAll(aPntr);
    Rect clip = aPntr.getClipBounds(); double rh = getRowHeight();
    int cellStart = (int)Math.max(clip.getY()/rh,0), cellEnd = (int)(clip.getMaxY()/rh);
    if(cellStart!=_cellStart || cellEnd!=_cellEnd)
        relayout();
}

/**
 * Calculates sample width and height from items.
 */
protected void calcSampleSize()
{
    ListCell cell = new ListCell(this, null, 0, getColIndex(), false);
    cell.setFont(getFont());
    cell.setPadding(getCellPadding());
    for(int i=0,iMax=Math.min(getItems().size(),30);i<iMax;i++) {
        cell._item = i<getItems().size()? getItems().get(i) : null; cell._row = i;
        for(int j=cell.getChildCount()-1;j>=0;j--) { View child = cell.getChild(j);
            if(child!=cell._strView && child!=cell._graphic) cell.removeChild(j); }
        configureCell(cell);
        _sampleWidth = Math.max(_sampleWidth, cell.getPrefWidth());
        _sampleHeight = Math.max(_sampleHeight, cell.getPrefHeight());
    }
    if(_sampleWidth<0) _sampleWidth = 100;
    if(_sampleHeight<0) _sampleHeight = Math.ceil(getFont().getLineHeight()+4);
}

/**
 * Process events.
 */
protected void processEvent(ViewEvent anEvent)
{
    // Handle MousePressed
    if(anEvent.isMousePressed()) {
        int index = getRowAt(anEvent.getY());
        setSelectedIndex(index);
        fireActionEvent();
    }
    
    // Handle MouseExited
    if(anEvent.isMouseExited())
        setTargetedIndex(-1);
        
    // Handle MouseMoved
    if(anEvent.isMouseMoved() && isTargeting()) {
        int index = getRowAt(anEvent.getY()); if(index>=getItems().size()) index = -1;
        setTargetedIndex(index);
    }
    
    // Handle KeyPressed
    if(anEvent.isKeyPressed()) {
        int kcode = anEvent.getKeyCode();
        switch(kcode) {
            case KeyCode.UP: if(getSelectedIndex()>0)setSelectedIndex(getSelectedIndex()-1); anEvent.consume(); break;
            case KeyCode.DOWN: if(getSelectedIndex()<getItems().size()-1)
                setSelectedIndex(getSelectedIndex()+1); anEvent.consume(); break;
            case KeyCode.ENTER: fireActionEvent(); anEvent.consume(); break;
        }
    }
}

/**
 * Override to return white.
 */
public Paint getDefaultFill()  { return Color.WHITE; }

/**
 * Override to make list same width as ScrollView.
 */
public boolean isScrollFitWidth()  { return true; }

/**
 * Returns a mapped property name.
 */
public String getValuePropName()  { return getBinding("SelectedIndex")!=null? "SelectedIndex" : "SelectedItem"; }

/**
 * Returns whether given items are equal to set items.
 */
protected boolean equalsItems(List theItems)
{
    return ListUtils.equalsId(theItems, _items) || SnapUtils.equals(theItems,_items);
}

/**
 * XML archival.
 */
@Override
public XMLElement toXMLView(XMLArchiver anArchiver)
{
    // Archive basic view attributes
    XMLElement e = super.toXMLView(anArchiver);
    
    // Archive selection mode
    //if(getSelectionMode()==SELECT_SINGLE) e.add("selection", "single-interval");
    //else if(getSelectionMode()==SELECT_MULTIPLE) e.add("selection", "multiple-interval");
    
    // Archive SelectedIndex
    if(getSelectedIndex()>=0) e.add("SelectedIndex", getSelectedIndex());
    
    // Archive Items
    if(getItems()!=null) for(int i=0, iMax=getItems().size(); i<iMax; i++) {
        XMLElement item = new XMLElement("item");
        item.add("text", getItems().get(i));
        e.add(item);
    }
    
    // Return element
    return e;
}

/**
 * XML unarchival.
 */
@Override
public void fromXMLView(XMLArchiver anArchiver, XMLElement anElement)
{
    // Unarchive basic view attributes
    super.fromXMLView(anArchiver, anElement);
    
    // Set selectionMode
    //String selection = anElement.getAttributeValue("selection", "single");
    //if(selection.equals("single")) setSelectionMode(SELECT_SINGLE);
    //else if(selection.equals("single-interval")) setSelectionMode(SELECT_INTERVAL); else setSelMode(SELECT_MULTIPLE);

    // Unarchive SelectedIndex
    if(anElement.hasAttribute("SelectedIndex")) setSelectedIndex(anElement.getAttributeIntValue("SelectedIndex"));
    
    // Unarchive items
    if(anElement.indexOf("item")>=0) {
        List items = new ArrayList();
        for(int i=anElement.indexOf("item"); i>=0; i=anElement.indexOf("item", i+1))
            items.add(anElement.get(i).getAttributeValue("text"));
        setItems(items);
    }
}

}