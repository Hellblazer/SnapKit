/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import java.util.*;
import snap.gfx.*;
import snap.util.SnapUtils;

/**
 * A class to build a form.
 */
public class FormBuilder extends ViewOwner {

    // The root pane
    VBox               _pane = createRootPane();

    // The font
    Font               _font;

/**
 * Returns the padding.
 */
public Insets getPadding()  { return _pane.getPadding(); }

/**
 * Sets the padding.
 */
public void setPadding(Insets theInsets)  { _pane.setPadding(theInsets); }

/**
 * Sets the padding.
 */
public void setPadding(double aTp, double aRt, double aBt, double aLt)  { setPadding(new Insets(aTp, aRt, aBt, aLt)); }

/**
 * Returns the spacing between components.
 */
public double getSpacing()  { return _pane.getSpacing(); }

/**
 * Sets the spacing between components.
 */
public void setSpacing(double aValue)  { _pane.setSpacing(aValue); }

/**
 * Returns the font.
 */
public Font getFont()  { return _font; }

/**
 * Sets the font.
 */
public void setFont(Font aFont)  { _font = aFont; }

/**
 * Adds a label.
 */
public Label addLabel(String aTitle)
{
    Label label = new Label(); label.setText(aTitle);
    if(_font!=null) label.setFont(_font);
    return addView(label);
}

/**
 * Adds a separator.
 */
public Separator addSeparator()
{
    Separator sep = new Separator();
    return addView(sep);
}

/**
 * Adds a text field.
 */
public TextField addTextField(String aName, String aDefault)  { return addTextField(null, aName, aDefault); }

/**
 * Adds a text field.
 */
public TextField addTextField(String aLabel, String aName, String aDefault)
{
    // Create HBox for label and text field
    HBox hbox = new HBox();
    
    // If label is provided, create configure and add
    if(aLabel!=null) {
        Label label = new Label(); label.setText(aLabel);
        if(_font!=null) label.setFont(_font);
        hbox.addChild(label);
    }
    
    // Create TextField and panel and add
    TextField tfield = new TextField(); tfield.setName(aName);
    if(_font!=null) tfield.setFont(_font);
    hbox.addChild(tfield); addView(hbox);
    
    // Add binding
    addViewBinding(tfield, "Text", aName.replace(" ", ""));
    if(aDefault!=null) setValue(aName, aDefault);
    
    // Set FirstFocus
    if(getFirstFocus()==null) setFirstFocus(tfield);
    
    // Return text field
    return tfield;
}

/**
 * Adds an option field.
 */
public ComboBox addComboBox(String aTitle, String options[], String aDefault)
{
    // Create ComboBox and panel and add
    Label label = new Label(); label.setText(aTitle + ":"); //label.setAlignmentX(0);
    ComboBox cbox = new ComboBox(); //cbox.getItems().add(options);
    cbox.setName(aTitle); //if(_font!=null) cbox.setFont(_font);
    HBox hbox = new HBox(); //panel.setAlignmentX(0);
    hbox.addChild(label); hbox.addChild(cbox); addView(hbox);
    
    // Add binding
    addViewBinding(cbox, "SelectedItem", aTitle.replace(" ", ""));
    setValue(aTitle, aDefault);
    
    // Return combobox
    return cbox;
}

/**
 * Adds buttons.
 */
public HBox addButtons(String theTitles[])
{
    String names[] = new String[theTitles.length];
    for(int i=0; i<theTitles.length; i++) names[i] = theTitles[i] + "Button";
    return addButtons(names, theTitles);
}

/**
 * Adds buttons.
 */
public HBox addButtons(String theNames[], String theLabels[])
{
    HBox hbox = new HBox(); hbox.setSpacing(10); //panel.setAlignmentX(0);
    
    // Iterate over options
    for(int i=0, iMax=theNames.length; i<iMax; i++) { String title = theNames[i], text = theLabels[i];
        Button button = new Button(); button.setName(title); button.setText(text);
        hbox.addChild(button);
    }
    
    // Add/return hbox
    return addView(hbox);
}

/**
 * Adds radio buttons.
 */
public List <RadioButton> addRadioButtons(String aTitle, String options[], String aDefault)
{
    List <RadioButton> rbuttons = new ArrayList();
    for(String option : options) rbuttons.add(addRadioButton(aTitle, option, option.equals(aDefault)));
    return rbuttons;
}

/**
 * Adds a radio button.
 */
public RadioButton addRadioButton(String aTitle, String theText, boolean isSelected)
{
    // Create radio button, add to button group and add to panel
    RadioButton rb = new RadioButton(); rb.setName(aTitle); rb.setText(theText);
    if(_font!=null) rb.setFont(_font);
    if(isSelected) { rb.setSelected(true); setValue(aTitle, theText); }
    rb.setToggleGroupName(aTitle);
    
    // Add/return button
    return addView(rb);
}

/**
 * Adds a View.
 */
public <T extends View> T addView(T aView)  { _pane.addChild(aView); return aView; }

/**
 * Removes a View.
 */
public <T extends View> T removeView(T aView)  { _pane.removeChild(aView); return aView; }

/**
 * Show Dialog.
 */
public boolean showPanel(View aView, String aTitle, Image anImage)
{
    DialogBox dbox = new DialogBox(aTitle); dbox.setImage(anImage); dbox.setContent(getUI());
    return dbox.showConfirmDialog(aView);
}

/**
 * Returns the specified value.
 */
public Object getValue(String aKey)  { String key = aKey.replace(" ", ""); return getModelValue(key); }

/**
 * Sets the specified value.
 */
public void setValue(String aKey, Object aValue)  { String key = aKey.replace(" ", ""); setModelValue(key, aValue); }

/**
 * Returns the specified value.
 */
public String getStringValue(String aKey)  { return SnapUtils.stringValue(getValue(aKey)); }

/**
 * Creates the UI.
 */
protected VBox createRootPane()
{
    VBox vbox = new VBox(); vbox.setPadding(new Insets(8)); vbox.setSpacing(20); vbox.setFillWidth(true);
    return vbox;
}

/**
 * Creates the UI.
 */
protected View createUI()  { return _pane; }

/**
 * Responds to UI.
 */
protected void respondUI(ViewEvent anEvent)
{
    // Handle RadioButtons
    if(anEvent.getView() instanceof RadioButton)
        setValue(anEvent.getName(), anEvent.getText());
}

}