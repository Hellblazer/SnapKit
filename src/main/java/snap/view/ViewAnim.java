/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.view;
import java.util.*;
import java.util.function.Consumer;
import snap.gfx.Pos;
import snap.util.*;

/**
 * A class to animate View attributes.
 */
public class ViewAnim {
    
    // The View
    View                 _view;
    
    // The parent anim
    ViewAnim             _parent;

    // The start/end times
    int                  _start, _end;
    
    // The anim keys
    List <String>        _keys = new ArrayList();
    
    // The start values
    Map <String,Object>  _startVals = new HashMap();
    
    // The end values
    Map <String,Object>  _endVals = new HashMap();
    
    // The nested anims
    List <ViewAnim>      _anims = new ArrayList();
    
    // The loop count
    int                  _loopCount;
    
    // The root view currently playing this anim
    RootView             _rview;
    
    // A runnable to be called on each anim frame
    Consumer <ViewAnim>  _onFrame;
    
    // A runnable to be called when anim is finished
    Consumer <ViewAnim>  _onFinish;
    
    // The start time, current time
    int                  _startTime = -1, _time;
    
    // Whether this anim was suspended because it's not visible
    boolean              _suspended;
    
/**
 * Creates a new ViewAnim.
 */
public ViewAnim(View aView, int aStart, int anEnd)  { _view = aView; _start = aStart; _end = anEnd; }

/**
 * Returns the View.
 */
public View getView()  { return _view; }

/**
 * Returns whether this ViewAnim is the root one.
 */
public boolean isRoot()  { return _parent==null; } 

/**
 * Returns the root ViewAnim.
 */
public ViewAnim getRoot(int aTime)  { return _parent!=null? _parent.getRoot(aTime) : getAnim(aTime); } 

/**
 * Returns the parent ViewAnim.
 */
public ViewAnim getParent()  { return _parent; } 

/**
 * Returns the start value.
 */
public int getStart()  { return _start; }

/**
 * Returns the end value.
 */
public int getEnd()  { return _end; }

/**
 * Returns the len value.
 */
public int getLen()  { return _end - _start; }

/**
 * Returns the keys.
 */
public List <String> getKeys()  { return _keys; }

/**
 * Returns the start value for given key.
 */
public Object getStartVal(String aKey)
{
    Object val = _startVals.get(aKey);
    if(val==null) {
        val = _view.getValue(aKey);
        _startVals.put(aKey, val);
    }
    return val;
}

/**
 * Sets the start value for given key.
 */
public ViewAnim setStartVal(String aKey, Object aVal)
{
    ListUtils.addUnique(_keys, aKey);
    _startVals.put(aKey, aVal); return this;
}

/**
 * Returns the end value for given key.
 */
public Object getEndVal(String aKey)  { return _endVals.get(aKey); }

/**
 * Returns the list of child anims.
 */
public List <ViewAnim> getAnims()  { return _anims; }

/**
 * Returns the anim for the given start/end.
 */
public ViewAnim getAnim(int aTime)
{
    if(aTime<=getEnd()) return this;
    for(ViewAnim anim : _anims) {
        if(aTime==anim.getEnd()) return anim; }
    ViewAnim anim = new ViewAnim(_view, getEnd(), aTime);
    _anims.add(anim); anim._parent = this;
    return anim;
}

/**
 * Whether anim is playing.
 */
public boolean isPlaying()  { return _rview!=null; }

/**
 * Returns whether anim is suspended.
 */
public boolean isSuspended()  { return _suspended; }

/**
 * Sets whether anim is suspended.
 */
public void setSuspended(boolean aValue)  { _suspended = aValue; }

/**
 * Returns the start time.
 */
public int getStartTime()  { return _startTime; }

/**
 * Sets the start time.
 */
protected void setStartTime(int aTime)
{
    _startTime = aTime;
    for(ViewAnim va : _anims) va.setStartTime(aTime);
}

/**
 * Sets the time, returns whether anim has completed.
 */
public boolean setTime(int aTime)
{
    // Set new time
    int oldTime = _time; _time = aTime - _startTime; if(aTime==oldTime) return false;
    
    // If anim is completed, but there is a LoopCount, call again with new loop corrected time
    boolean completed = _time >=_end;
    if(completed && _loopCount>0 && _time<_loopCount*_end) {
        getRoot(0).setTime(_time%_end); return false; }
    
    // If new values need to be set, set them
    boolean needsUpdate = !(oldTime<=_start && _time<=_start || oldTime>=_end && _time>=_end);
    if(needsUpdate) for(String key : getKeys())
        setTime(_time, key);
        
    // Forward on to anims
    for(ViewAnim a : _anims) {
        if(_time>a.getStart())
            completed &= a.setTime(aTime);
        else completed = false;
    }
    
    // If on frame set, call it
    if(_onFrame!=null)
        _onFrame.accept(this);
    
    // If completed and root anim, stop
    if(completed && isRoot())
        stop();
    
    // If completed and there is an OnFinish, trigger it
    if(completed && needsUpdate && _onFinish!=null)
        _onFinish.accept(this);
    
    // Return whether completed
    return completed;
}

/**
 * Returns the current time.
 */
public int getTime()  { return _time; }

/**
 * Sets the time.
 */
public void setTime(int aTime, String aKey)
{
    Object val = getValue(aKey, aTime);
    _view.setValue(aKey, val);
}

/**
 * Returns the interpolated value.
 */
public Object interpolate(Object aVal1, Object aVal2, double aRatio)
{
    if(aVal1 instanceof Number && aVal2 instanceof Number) {
        double val1 = ((Number)aVal1).doubleValue(), val2 = ((Number)aVal2).doubleValue();
        return Interpolator.EASE_BOTH.getValue(aRatio, val1, val2);
    }
    return null;
}

/**
 * Returns the value for given key at current time.
 */
public Object getValue(String aKey)  { return getValue(aKey, _time); }

/**
 * Returns the value for given key and time.
 */
public Object getValue(String aKey, int aTime)
{
    Object fromVal = getStartVal(aKey), toVal = getEndVal(aKey), val = null;
    if(aTime<=getStart()) val = fromVal;
    else if(aTime>=getEnd()) val = toVal;
    else val = interpolate(fromVal, toVal, (aTime-getStart())/(double)getLen());
    return val;
}

/**
 * Sets the X value.
 */
public ViewAnim setX(double aVal)  { return setValue(View.X_Prop, aVal); }

/**
 * Sets the Y value.
 */
public ViewAnim setY(double aVal)  { return setValue(View.Y_Prop, aVal); }

/**
 * Sets the Width value.
 */
public ViewAnim setWidth(double aVal)  { return setValue(View.Width_Prop, aVal); }

/**
 * Sets the Height value.
 */
public ViewAnim setHeight(double aVal)  { return setValue(View.Height_Prop, aVal); }

/**
 * Sets the TransX value.
 */
public ViewAnim setTransX(double aVal)  { return setValue(View.TransX_Prop, aVal); }

/**
 * Sets the TransY value.
 */
public ViewAnim setTransY(double aVal)  { return setValue(View.TransY_Prop, aVal); }

/**
 * Sets the Rotate value.
 */
public ViewAnim setRotate(double aVal)  { return setValue(View.Rotate_Prop, aVal); }

/**
 * Sets the Scale X/Y values.
 */
public ViewAnim setScale(double aVal)  { return setScaleX(aVal).setScaleY(aVal); }

/**
 * Sets the ScaleX value.
 */
public ViewAnim setScaleX(double aVal)  { return setValue(View.ScaleX_Prop, aVal); }

/**
 * Sets the ScaleY value.
 */
public ViewAnim setScaleY(double aVal)  { return setValue(View.ScaleY_Prop, aVal); }

/**
 * Returns the end value for given key.
 */
public ViewAnim setValue(String aKey, Object aValue)  { return setValue(aKey, null, aValue); }

/**
 * Returns the end value for given key.
 */
public ViewAnim setValue(String aKey, Object aVal0, Object aVal1)
{
    ListUtils.addUnique(_keys, aKey);
    if(aVal0!=null) setStartVal(aKey, aVal0);
    _endVals.put(aKey, aVal1);
    return this;
}

/**
 * Sets the loop count.
 */
public ViewAnim setLoops()  { return setLoopCount(Short.MAX_VALUE); }

/**
 * Sets the loop count.
 */
public ViewAnim setLoopCount(int aValue)  { _loopCount = aValue; return this; }

/**
 * Returns the consumer to be called on each frame.
 */
public Consumer <ViewAnim> getOnFrame()  { return _onFrame; }

/**
 * Sets the consumer to be called on each frame.
 */
public ViewAnim setOnFrame(Consumer <ViewAnim> aCall)  { _onFrame = aCall; return this; }
    
/**
 * Returns the on finished.
 */
public Consumer <ViewAnim> getOnFinish()  { return _onFinish; }

/**
 * Sets the on finished.
 */
public ViewAnim setOnFinish(Consumer <ViewAnim> aFinish)  { _onFinish = aFinish; return this; }
    
/**
 * Play the anim.
 */
public void play()
{
    if(_parent!=null) { _parent.play(); return; }
    RootView rview = _view.getRootView();
    if(rview!=null) { rview.playAnim(_view); _suspended = false; }
    else _suspended = true;
}

/**
 * Stop the anim.
 */
public void stop()
{
    if(_parent!=null) { _parent.stop(); return; }
    if(_rview!=null) _rview.stopAnim(_view);
    _suspended = false; _startTime = -1;
}

/**
 * Suspend the anim (will autostart when conditions are right).
 */
public void suspend()
{
    _suspended = true;
    if(_parent!=null) { _parent.suspend(); return; }
    if(_rview!=null) _rview.stopAnim(_view);
}

/**
 * Clears the anim.
 */
public ViewAnim clear()
{
    stop(); _loopCount = 0; _onFinish = null;
    _keys.clear(); _startVals.clear(); _endVals.clear(); _anims.clear();
    return this;
}

/**
 * Standard toString implementation.
 */
public String toString()
{
    StringBuffer sb = StringUtils.toString(this, "Start", "End");
    String keys = ListUtils.joinStrings(getKeys(), ","); if(keys.length()>0) StringUtils.toStringAdd(sb, "Keys", keys);
    if(_loopCount==Short.MAX_VALUE) StringUtils.toStringAdd(sb, "Loops", "true");
    else if(_loopCount>0) StringUtils.toStringAdd(sb, "LoopCount", _loopCount);
    for(ViewAnim va : _anims) sb.append("\n    " + va.toString().replace("\n", "\n    "));
    return sb.toString();
}

/**
 * Sets the alignment.
 */
public static void setAlign(View aView, Pos aPos, int aTime)
{
    ParentView par = aView instanceof ParentView? (ParentView)aView : null; if(par==null) return;
    View child0 = par.getChildCount()>0? par.getChild(0) : null; if(child0==null) return;
    double x0 = child0.getX(), y0 = child0.getY();
    par.setAlign(aPos); par.layoutDeep();
    double x1 = child0.getX(), y1 = child0.getY();
    for(View child : par.getChildren()) {
        child.setTransX(x0-x1); child.setTransY(y0-y1);
        child.getAnimCleared(aTime).setTransX(0).setTransY(0).play();
    }
}

}