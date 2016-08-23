/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.parse;
import java.lang.reflect.*;

/**
 * A class called when child rules are parsed.
 */
public class ParseHandler <T> {

    // The part generated by this handler
    public T         _part;
    
    // The token where the current part started
    Token            _startToken;
    
    // Whether handler is in use
    boolean          _inUse;
    
    // The backup handler
    ParseHandler     _backupHandler;

/**
 * Called when a child rule has been successfully parsed into given node.
 */
protected void parsedOne(ParseNode aNode)
{
    if(_startToken==null) _startToken = aNode.getStartToken();
    parsedOne(aNode, aNode.getId());
}

/**
 * Called when a child rule has been successfully parsed into given node.
 */
protected void parsedOne(ParseNode aNode, String anId)  { }

/**
 * Called when all child rules have been successfully parsed.
 */
public T parsedAll()  { T part = _part; reset(); return part; }

/**
 * Returns the part.
 */
public T getPart()  { return _part!=null? _part : (_part=createPart()); }

/**
 * Creates the part.
 */
protected T createPart()
{
    try { return getPartClass().newInstance(); }
    catch(Exception e) { throw new RuntimeException(e); }
}

/**
 * Returns the part class.
 */
protected Class<T> getPartClass()  { return getTypeParameterClass(getClass()); }

/**
 * Returns the token where the current part started.
 */
public Token getStartToken()  { return _startToken; }

/**
 * Returns a handler that is not in use.
 */
public synchronized ParseHandler getAvailableHandler()
{
    ParseHandler handler = this;
    while(handler._inUse) handler = handler.getBackupHandler();
    handler._inUse = true;
    return handler;
}

/**
 * Returns a backup handler.
 */
private ParseHandler getBackupHandler()
{
    if(_backupHandler==null) try {
        Constructor constr = getClass().getDeclaredConstructor();
        constr.setAccessible(true);
        _backupHandler = (ParseHandler)constr.newInstance();
    }
    catch(Exception e) { throw new RuntimeException(e); }
    return _backupHandler;
}

/**
 * Resets the handler.
 */
public void reset()  { _part = null; _startToken = null; _inUse = false; }

/** Returns a type parameter class. */
private static Class getTypeParameterClass(Class aClass)
{
    Type type = aClass.getGenericSuperclass();
    if(type instanceof ParameterizedType) { ParameterizedType ptype = (ParameterizedType)type;
        Type type2 = ptype.getActualTypeArguments()[0];
        if(type2 instanceof Class)
            return (Class)type2;
        if(type2 instanceof ParameterizedType) { ParameterizedType ptype2 = (ParameterizedType)type2;
            if(ptype2.getRawType() instanceof Class)
                return (Class)ptype2.getRawType(); }
    }
    
    // Try superclass
    Class scls = aClass.getSuperclass();
    if(scls!=null)
        return getTypeParameterClass(scls);

    // Complain and return null
    System.err.println("ParseHandler.getTypeParameterClass: Type Parameter Not Found for " + aClass.getName());
    return null;
}

}