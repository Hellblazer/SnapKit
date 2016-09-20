/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.util;
import java.lang.reflect.*;
import java.util.*;
import snap.web.WebFile;

/**
 * Utility methods for Class.
 */
public class ClassUtils {

    // An array of primitive type classes and sister array of it's non-primitive matches
    static Class _primitives[] = { boolean.class, char.class, byte.class, short.class, int.class, long.class,
        float.class, double.class, void.class };
    static Class _primMappings[] = { Boolean.class, Character.class, Byte.class, Short.class, Integer.class, Long.class,
        Float.class, Double.class, Void.class };

/**
 * Returns the class for an object.
 */
public static Class getClass(Object anObj)
{
    return anObj instanceof Class? (Class)anObj : anObj!=null? anObj.getClass() : null;
}

/**
 * Returns a class for a given name.
 */
public static Class getClass(String aName)  { return getClass(aName, ClassUtils.class.getClassLoader()); }

/**
 * Returns a class for a given name, using the class loader of the given class.
 */
public static Class getClass(String aName, Class aClass)  { return getClass(aName, aClass.getClassLoader()); }

/**
 * Returns a class for a given name, using the class loader of the given class.
 */
public static Class getClass(String aName, ClassLoader aClassLoader)
{
    // Handle arrays, either coded or uncoded (e.g. [I, [D, [LClassName; or  int[], double[] or ClassName[])
    if(aName.startsWith("[")) return getCodedClass(aName, aClassLoader);
    if(aName.endsWith("[]")) {
        Class cls = getClass(aName.substring(0, aName.length()-2));
        return cls!=null? Array.newInstance(cls,0).getClass() : null; }
    
    // Handle primitive classes
    Class pcls = getPrimitiveClass(aName); if(pcls!=null) return pcls;
    
    // Do normal Class.forName, but return null instead of throwing exception
    try { return Class.forName(aName, false, aClassLoader); }
    catch(ClassNotFoundException e) { return null; }
    catch(NoClassDefFoundError t) { System.err.println("ClassUtils.getClass: " + t); return null; }
    catch(Throwable t) { System.err.println("ClassUtils.getClass: " + t); return null; }
}

/**
 * Returns whether name is a primitive class name.
 */
public static boolean isPrimitiveClassName(String aName)  { return getPrimitiveClass(aName)!=null; }

/**
 * Returns a primitive class for name.
 */
public static Class getPrimitiveClass(String aName)
{
    if(aName.length()>7 || !Character.isLowerCase(aName.charAt(0)) || aName.indexOf('.')>0) return null;
    String tp = aName.intern();
    return tp=="boolean"? boolean.class : tp=="char"? char.class : tp=="void"? void.class :
        tp=="byte"? byte.class : tp=="short"? short.class : tp=="int"? int.class :
        tp=="long"? long.class : tp=="float"? float.class : tp=="double"? double.class : null;
}

/**
 * Returns an array class.
 */
public static Class getCodedClass(String aName, ClassLoader aClassLoader)
{
    char c = aName.charAt(0);
    switch(c) {
        case 'B': return byte.class; case 'C': return char.class;
        case 'D': return double.class; case 'F': return float.class;
        case 'I': return int.class; case 'J': return long.class;
        case 'S': return short.class; case 'Z': return boolean.class; case 'V': return void.class;
        case 'L': int end = aName.indexOf(';',1);
        return getClass(aName.substring(1,end), aClassLoader);
        case '[': Class cls = getCodedClass(aName.substring(1), aClassLoader);
            return cls!=null? Array.newInstance(cls,0).getClass() : null;
    }
    throw new RuntimeException("ClassUtils.getCodedPrimitiveClass: Not a coded class " + aName);
}

/**
 * Returns a class code.
 */
public static String getClassCoded(Class aClass)
{
    if(aClass.isArray()) return "[" + getClassCoded(aClass.getComponentType());
    if(aClass==byte.class) return "B"; if(aClass==char.class) return "C";
    if(aClass==double.class) return "D"; if(aClass==float.class) return "F";
    if(aClass==int.class) return "I"; if(aClass==long.class) return "J";
    if(aClass==short.class) return "S"; if(aClass==boolean.class) return "Z";
    if(aClass==void.class) return "V";
    return "L" + aClass.getName() + ";";
}

/**
 * Returns a class code.
 */
public static String getClassCoded(String aClassName)
{
    Class pclass = getPrimitiveClass(aClassName);
    if(pclass!=null) return getClassCoded(pclass);
    return "L" + aClassName + ";";
}

/**
 * Returns non primitive type for primitive.
 */
public static Class toPrimitive(Class aClass)
{
    for(int i=0; i<_primitives.length; i++) if(aClass==_primMappings[i]) return _primitives[i];
    return aClass;
}

/**
 * Returns primitive type for non-primitive.
 */
public static Class fromPrimitive(Class aClass)
{
    for(int i=0; i<_primitives.length; i++) if(aClass==_primitives[i]) return _primMappings[i];
    return aClass;
}

/**
 * Returns the given object as instance of given class, if it is.
 */
public static <T> T getInstance(Object anObj, Class <T> aClass)  { return aClass.isInstance(anObj)? (T)anObj : null; }

/**
 * Returns a new instance of a given object.
 */
public static <T> T newInstance(T anObject)
{
    try { return (T)getClass(anObject).newInstance(); }
    catch(Exception e) { throw new RuntimeException(e); }
}

/**
 * Returns a new instance of a given class.
 */
public static <T> T newInstance(Class <T> aClass)
{
    try { return aClass.newInstance(); }
    catch(Exception e) { throw new RuntimeException(e); }
}

/**
 * Returns whether a given class could be assigned a value from the second given class (accounting for auto-boxing).
 */
public static boolean isAssignable(Class aClass1, Class aClass2)
{
    if(aClass2==null) return !aClass1.isPrimitive();
    if(aClass1.isPrimitive() || aClass2.isPrimitive())
        return isAssignablePrimitive(aClass1, aClass2);
    return aClass1.isAssignableFrom(aClass2);
}

/**
 * Returns whether a given primitive class could be assigned a value from the second given class.
 */
public static boolean isAssignablePrimitive(Class aClass1, Class aClass2)
{
    Class c1 = toPrimitive(aClass1), c2 = toPrimitive(aClass2);
    if(c1==Object.class) return true;
    if(c1==float.class || c1==double.class || c1==Number.class)
        return c2==c1||c2==byte.class||c2==char.class||c2==short.class||c2==int.class||c2==long.class||c2==float.class;
    if(c1==byte.class || c1==char.class || c1==short.class || c1==int.class || c1==long.class)
        return c2==c1 || c2==byte.class || c2==char.class || c2==short.class || c2==int.class;
    return c1.isAssignableFrom(c2);
}

/**
 * Returns whether second batch of classes is assignable to first batch of classes (accounting for auto-boxing).
 */
public static boolean isAssignable(Class theClasses1[], Class theClasses2[], int aCount)
{
    if(theClasses1==null) return theClasses2==null || theClasses2.length==0;
    if(theClasses2==null) return theClasses1.length==0;
    for(int i=0; i<aCount; i++)
        if(theClasses2[i]!=null && !isAssignable(theClasses1[i], theClasses2[i]))
            return false;
    return true;
}

/**
 * Returns whether arg classes are compatible.
 */
public static boolean isCompatible(Class params[], Class theClasses[], boolean isVarArgs)
{
    // Handle Var args
    if(isVarArgs) {
        
        // If standard args don't match return false
        if(theClasses.length<params.length-1 || !isAssignable(params, theClasses, params.length-1))
            return false;
        
        // Get VarArgClass
        Class varArgArrayClass = params[params.length-1];
        Class varArgClass = varArgArrayClass.getComponentType();
        
        // If only one arg and it is of array class, return true
        Class argClass = theClasses.length==params.length? theClasses[params.length-1] : null;
        if(argClass!=null && argClass.isArray() && varArgArrayClass.isAssignableFrom(argClass))
            return true;

        // If any var args don't match, return false
        for(int i=params.length-1; i<theClasses.length; i++)
            if(theClasses[i]!=null && !isAssignable(varArgClass, theClasses[i]))
                return false;
        return true;
    }
    
    // Handle normal method
    return params.length==theClasses.length && isAssignable(params, theClasses, params.length);
}

/**
 * Returns a rating of a method for given possible arg classes.
 */
private static int getRating(Class theParamTypes[], Class theClasses[], boolean isVarArgs)
{
    // Iterate over classes and add score based on matching classes
    // This is a punt - need to groc the docs on this: https://docs.oracle.com/javase/specs/jls/se7/html/jls-15.html
    Class classes[] = theParamTypes; int clen = classes.length; if(isVarArgs) clen--; int rating = 0;
    for(int i=0, iMax=clen; i<iMax; i++) {
        Class cls1 = classes[i], cls2 = theClasses[i];
        if(cls1==cls2) rating += 1000;
        else if(cls2!=null && cls1.isAssignableFrom(cls2)) rating += 100;
        else if(isAssignable(cls1, cls2)) rating += 10;
    }
    
    // If varargs, check remaining args
    if(isVarArgs) {
        
        // Get VarArgClass
        Class varArgArrayClass = theParamTypes[clen];
        Class varArgClass = varArgArrayClass.getComponentType();
        
        // If only one arg and it is of array class, return true
        Class argClass = theClasses.length==theParamTypes.length? theClasses[theParamTypes.length-1] : null;
        if(argClass!=null && argClass.isArray() && varArgArrayClass.isAssignableFrom(argClass))
            rating += 1000;

        // If any var args don't match, return false
        else for(int i=clen; i<theClasses.length; i++)
            if(theClasses[i]!=null && !isAssignable(varArgClass, theClasses[i]))
                rating += 1000;
    }
    
    // Return rating
    return rating;
}

/**
 * Returns a field for a parent class and a name.
 */
public static Field getField(Class aClass, String aName)
{
    Class cls = aClass.isPrimitive()? fromPrimitive(aClass) : aClass;
    Field field = getDeclaredField(cls, aName);
    if(field!=null)
        return field;
    
    // Check superclass
    Class sclass = cls.getSuperclass();
    if(sclass!=null)
        field = getField(sclass, aName);
    if(field!=null)
        return field;
    
    // Check interfaces
    for(Class c : cls.getInterfaces()) {
        field = getField(c, aName);
        if(field!=null)
            return field; }
    
    // Return null since not found
    return null;
}

/**
 * Returns a field for a parent class and a name.
 */
public static Field getDeclaredField(Class aClass, String aName)
{
    Field fields[] = aClass.getDeclaredFields();
    for(Field field : fields)
        if(field.getName().equals(aName))
            return field;
    return null;
}

/**
 * Returns all methods for given class and subclasses that start with given prefix.
 */
public static Method[] getMethods(Class aClass, String aPrefix)
{
    List meths = new ArrayList();
    getMethods(aClass, aPrefix, meths, true);
    return (Method[])meths.toArray(new Method[0]);
}

/**
 * Returns the method for given class, name and parameter types.
 */
private static void getMethods(Class aClass, String aPrefix, List theMethods, boolean doPrivate)
{
    Class cls = aClass.isPrimitive()? fromPrimitive(aClass) : aClass;
    Method methods[] = cls.getDeclaredMethods();
    for(Method meth : methods) {
        if(meth.isSynthetic()) continue;
        if(meth.getName().startsWith(aPrefix) && (doPrivate || !Modifier.isPrivate(meth.getModifiers())))
            theMethods.add(meth);
    }
    
    // If interface, recurse for extended interfaces
    if(cls.isInterface()) {
        for(Class cl : cls.getInterfaces())
            getMethods(cl, aPrefix, theMethods, false); }
            
    // Otherwise, recurse for extended superclass
    else if((cls=cls.getSuperclass())!=null)
        getMethods(cls, aPrefix, theMethods, false);
}

/**
 * Returns the method for given class, name and parameter types.
 */
public static Constructor getConstructor(Class aClass, Class theClasses[])
{
    // Get methods that match name/args (just return if null, no args or singleton)
    if(aClass.isInterface()) return null;
    Constructor methods[] = getConstructors(aClass, theClasses, null);
    if(methods==null) return null;
    if(theClasses.length==0 || methods.length==1) return methods[0];
    
    // Rate compatible constructors and return the most compatible
    Constructor method = null; int rating = 0;
    for(Constructor meth : methods) {
        int rtg = getRating(meth.getParameterTypes(), theClasses, meth.isVarArgs());
        if(rtg>rating) { method = meth; rating = rtg; }}
    return method;
}

/**
 * Returns the method for given class, name and parameter types.
 */
private static Constructor[] getConstructors(Class aClass, Class theClasses[], Constructor theResult[])
{
    Class cls = aClass.isPrimitive()? fromPrimitive(aClass) : aClass;
    Constructor methods[] = getDeclaredConstructors(cls, theClasses, theResult);
    if((cls=cls.getSuperclass())!=null)
        methods = getConstructors(cls, theClasses, methods);
    return methods;
}

/**
 * Returns the declared method for a given class, name and parameter types array.
 */
private static Constructor[] getDeclaredConstructors(Class aClass, Class theClasses[], Constructor theResult[])
{
    // Iterate over declared constructors and if compatible, add to results
    Constructor methods[] = aClass.getDeclaredConstructors();
    for(Constructor meth : methods)
        if(isCompatible(meth.getParameterTypes(), theClasses, meth.isVarArgs())) {
            theResult = theResult!=null? Arrays.copyOf(theResult, theResult.length+1) : new Constructor[1];
            theResult[theResult.length-1] = meth; }
    return theResult;
}

/**
 * Returns a method for given name and classes.
 */
public static Method getMethod(Object anObj, String aName, Class ... theClasses)
{
    Class cls = anObj instanceof Class? (Class)anObj : anObj.getClass(); if(cls==null) return null;
    try { return getMethod(cls, aName, theClasses); }
    catch(NoSuchMethodError e) { return null; }
}

/**
 * Returns the method for given class, name and parameter types.
 */
public static Method getMethod(Class aClass, String aName, Class ... theClasses)
{
    // Get methods with compatible name/args (just return if null, no args or singleton)
    Method methods[] = getMethods(aClass, aName, theClasses, null);
    if(methods==null) return null;
    if(theClasses.length==0 || methods.length==1) return methods[0];
    
    // Rate compatible methods and return the most compatible
    Method method = null; int rating = 0;
    for(Method meth : methods) {
        int rtg = getRating(meth.getParameterTypes(), theClasses, meth.isVarArgs());
        if(rtg>rating) { method = meth; rating = rtg; }}
    return method;
}

/**
 * Returns the method for given class, name and parameter types.
 */
private static Method[] getMethods(Class aClass, String aName, Class theClasses[], Method theResult[])
{
    // Make sure class is non-primitive
    Class cls = aClass.isPrimitive()? fromPrimitive(aClass) : aClass;
    
    // Check declared methods
    Method methods[] = getDeclaredMethods(cls, aName, theClasses, theResult);
    
    // If interface, check extended interfaces
    if(cls.isInterface()) {
        for(Class cl : cls.getInterfaces())
            methods = getMethods(cl, aName, theClasses, methods); }
            
    // Otherwise, check superclass
    else if((cls=cls.getSuperclass())!=null)
        methods = getMethods(cls, aName, theClasses, methods);
        
    // Return methods
    return methods;
}

/**
 * Returns compatible declared methods for a given class, name and parameter types array.
 */
private static Method[] getDeclaredMethods(Class aClass, String aName, Class theClasses[], Method theResult[])
{
    // Get class methods and intern name
    Method methods[] = aClass.getDeclaredMethods();
    String name = aName.intern();
    
    // Iterate over methods and if compatible, add to results
    for(Method meth : methods) {
        if(meth.isSynthetic()) continue; 
        if(meth.getName()==name && isCompatible(meth.getParameterTypes(), theClasses, meth.isVarArgs())) {
            theResult = theResult!=null? Arrays.copyOf(theResult, theResult.length+1) : new Method[1];
            theResult[theResult.length-1] = meth;
        }
    }
    return theResult;
}

/**
 * Returns a declared inner class for a given class and a name, checking super classes as well.
 */
public static Class getClass(Class aClass, String aName)
{
    // Make sure class is non-primitive
    Class cls = aClass.isPrimitive()? fromPrimitive(aClass) : aClass;
    
    // Check declared inner classes
    Class cls2 = getDeclaredClass(cls, aName);
    if(cls2!=null)
        return cls2;
    
    // Check superclass
    Class sclass = cls.getSuperclass();
    if(sclass!=null)
        cls2 = getClass(sclass, aName);
    if(cls2!=null)
        return cls2;
        
    // Check interfaces
    for(Class c : cls.getInterfaces()) {
        cls2 = getClass(c, aName);
        if(cls2!=null)
            return cls2; }
            
    // Return null since class not found
    return null;
}

/**
 * Returns a class for a parent class and a name.
 */
public static Class getDeclaredClass(Class aClass, String aName)
{
    for(Class cls : aClass.getDeclaredClasses())
        if(cls.getSimpleName().equals(aName))
            return cls;
    return null;
}

/**
 * Returns the common ancestor class for two objects.
 */
public static Class getCommonClass(Object anObj1, Object anObj2)
{
    // Bail if either object is null
    if(anObj1==null || anObj2==null) return null;
    
    // Get the classes for the objects
    Class c1 = getClass(anObj1);
    Class c2 = getClass(anObj2);
    
    // If either is assignable from the other, return that class
    if(c1.isAssignableFrom(c2)) return c1;
    if(c2.isAssignableFrom(c1)) return c2;
    
    // Recurse by swapping args and using superclass of second
    return getCommonClass(c2.getSuperclass(), c1);
}

/**
 * Returns the common ancestor class for a list of objects.
 */
public static Class getCommonClass(List aList)
{
    // Get class for first object, iterate over remaining classes and return common class
    Class cclass = aList.size()>0? getClass(aList.get(0)) : null;
    for(int i=1, iMax=aList.size(); i<iMax; i++)
        cclass = getCommonClass(cclass, aList.get(i));
    return cclass;
}

/**
 * Returns the class compiled from this java file.
 */
public static Class getClass(WebFile aFile)
{
    String cname = getClassName(aFile);
    try { return aFile.getSite().getClassLoader().loadClass(cname); }
    catch(Throwable e) { System.err.println("ClassUtils.getClass(" + aFile.getName() + "): " + e); return null; }
}

/**
 * Returns the class name.
 */
public static String getClassName(WebFile aFile)
{
    String fpath = aFile.getPath();
    return fpath.substring(1, fpath.length() - ".class".length()).replace('/', '.');
}

}