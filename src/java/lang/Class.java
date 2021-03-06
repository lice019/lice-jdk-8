package java.lang;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Member;
import java.lang.reflect.Field;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.AnnotatedType;
import java.lang.ref.SoftReference;
import java.io.InputStream;
import java.io.ObjectStreamField;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.Objects;

import sun.misc.Unsafe;
import sun.reflect.CallerSensitive;
import sun.reflect.ConstantPool;
import sun.reflect.Reflection;
import sun.reflect.ReflectionFactory;
import sun.reflect.generics.factory.CoreReflectionFactory;
import sun.reflect.generics.factory.GenericsFactory;
import sun.reflect.generics.repository.ClassRepository;
import sun.reflect.generics.repository.MethodRepository;
import sun.reflect.generics.repository.ConstructorRepository;
import sun.reflect.generics.scope.ClassScope;
import sun.security.util.SecurityConstants;

import java.lang.annotation.Annotation;
import java.lang.reflect.Proxy;

import sun.reflect.annotation.*;
import sun.reflect.misc.ReflectUtil;

/**
 * Class：表示类的类表示正在运行的Java应用程序中的类和接口，简单的说，就是.class文件的对象模型。
 * Class可以使用forName("类的全路径")，将.class的类文件字节内容加载到内存中，并存储在Class类中。然后可以同Class的对象来创建相应的实例对象。
 * final修饰，该类不可以被继承
 * <p>
 * 注意：ClassLoader类加载器是由jvm初始化的。
 *
 * @param <T>
 */
public final class Class<T> implements java.io.Serializable, GenericDeclaration, Type, AnnotatedElement {
    private static final int ANNOTATION = 0x00002000;
    private static final int ENUM = 0x00004000;
    private static final int SYNTHETIC = 0x00001000;

    //本地方法
    private static native void registerNatives();

    //该类义加载就执行jvm中由C语言实现的本地方法
    static {
        registerNatives();
    }

    /**
     * Class的私有构造器，不能被new。只能由java虚拟机创建Class对象。
     *
     * @param loader
     */
    private Class(ClassLoader loader) {
        //初始化类加载器的final字段。非空的初始值
        //防止将来JIT优化假定最后一个字段为null。
        classLoader = loader;
    }

    //toString输出字节码的权限类名，并判断是否为接口或简单的Class类
    public String toString() {
        return (isInterface() ? "interface " : (isPrimitive() ? "" : "class "))
                + getName();
    }

    //返回描述此 类的字符串，包括有关修饰符和类型参数的信息。 jdk1.8的接口
    public String toGenericString() {
        if (isPrimitive()) {
            return toString();
        } else {
            StringBuilder sb = new StringBuilder();

            // Class modifiers are a superset of interface modifiers
            int modifiers = getModifiers() & Modifier.classModifiers();
            if (modifiers != 0) {
                sb.append(Modifier.toString(modifiers));
                sb.append(' ');
            }

            if (isAnnotation()) {
                sb.append('@');
            }
            if (isInterface()) { // Note: all annotation types are interfaces
                sb.append("interface");
            } else {
                if (isEnum())
                    sb.append("enum");
                else
                    sb.append("class");
            }
            sb.append(' ');
            sb.append(getName());

            TypeVariable<?>[] typeparms = getTypeParameters();
            if (typeparms.length > 0) {
                boolean first = true;
                sb.append('<');
                for (TypeVariable<?> typeparm : typeparms) {
                    if (!first)
                        sb.append(',');
                    sb.append(typeparm.getTypeName());
                    first = false;
                }
                sb.append('>');
            }

            return sb.toString();
        }
    }


    //返回与给定字符串名称的类或接口相关联的 类对象。
    //Class clazz = Class.forName("java.lang.Thread");
    //返回的clazz是含有Thread类对象，clazz包含了Thread.class类字节码文件的所有信息
    @CallerSensitive
    public static Class<?> forName(String className)
            throws ClassNotFoundException {
        //Reflection在sun包下，
        //Reflection.getCallerClass()为jvm本地方法
        Class<?> caller = Reflection.getCallerClass();
        return forName0(className, true, ClassLoader.getClassLoader(caller), caller);
    }


    @CallerSensitive
    public static Class<?> forName(String name, boolean initialize,
                                   ClassLoader loader)
            throws ClassNotFoundException {
        Class<?> caller = null;
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            // Reflective call to get caller class is only needed if a security manager
            // is present.  Avoid the overhead of making this call otherwise.
            caller = Reflection.getCallerClass();
            if (sun.misc.VM.isSystemDomainLoader(loader)) {
                ClassLoader ccl = ClassLoader.getClassLoader(caller);
                if (!sun.misc.VM.isSystemDomainLoader(ccl)) {
                    sm.checkPermission(
                            SecurityConstants.GET_CLASSLOADER_PERMISSION);
                }
            }
        }
        return forName0(name, initialize, loader, caller);
    }

    /**
     * 在进行系统加载程序访问检查的安全性检查之后调用。
     */
    /**
     * @param name       全路径类名
     * @param initialize 初始标识
     * @param loader     ClassLoader类加载器
     * @param caller     Class实例对象
     * @return
     * @throws ClassNotFoundException
     */
    private static native Class<?> forName0(String name, boolean initialize,
                                            ClassLoader loader,
                                            Class<?> caller)
            throws ClassNotFoundException;

    //通过Class实例来创建一个相应类的实例
    @CallerSensitive
    public T newInstance()
            throws InstantiationException, IllegalAccessException {
        //获取程序安全管理器
        if (System.getSecurityManager() != null) {
            checkMemberAccess(Member.PUBLIC, Reflection.getCallerClass(), false);
        }

        // NOTE: the following code may not be strictly correct under
        // the current Java memory model.

        // Constructor lookup
        if (cachedConstructor == null) {
            if (this == Class.class) {
                throw new IllegalAccessException(
                        "Can not call newInstance() on the Class for java.lang.Class"
                );
            }
            try {
                Class<?>[] empty = {};
                final Constructor<T> c = getConstructor0(empty, Member.DECLARED);
                // Disable accessibility checks on the constructor
                // since we have to do the security check here anyway
                // (the stack depth is wrong for the Constructor's
                // security check to work)
                java.security.AccessController.doPrivileged(
                        new java.security.PrivilegedAction<Void>() {
                            public Void run() {
                                c.setAccessible(true);
                                return null;
                            }
                        });
                cachedConstructor = c;
            } catch (NoSuchMethodException e) {
                throw (InstantiationException)
                        new InstantiationException(getName()).initCause(e);
            }
        }
        Constructor<T> tmpConstructor = cachedConstructor;
        // Security check (same as in java.lang.reflect.Constructor)
        int modifiers = tmpConstructor.getModifiers();
        if (!Reflection.quickCheckMemberAccess(this, modifiers)) {
            Class<?> caller = Reflection.getCallerClass();
            if (newInstanceCallerCache != caller) {
                Reflection.ensureMemberAccess(caller, this, null, modifiers);
                newInstanceCallerCache = caller;
            }
        }
        // Run constructor
        try {
            return tmpConstructor.newInstance((Object[]) null);
        } catch (InvocationTargetException e) {
            Unsafe.getUnsafe().throwException(e.getTargetException());
            // Not reached
            return null;
        }
    }

    private volatile transient Constructor<T> cachedConstructor;
    private volatile transient Class<?> newInstanceCallerCache;


    //判断是否为实例对象
    public native boolean isInstance(Object obj);



    //确定由此 类对象表示的类或接口是否与由指定的Class 类表示的类或接口相同或是超类或 类接口。
    public native boolean isAssignableFrom(Class<?> cls);



    //判断是否为接口
    public native boolean isInterface();


    //确定此 类对象是否表示数组类。
    public native boolean isArray();


    //确定指定 类对象表示一个基本类型。 Boolean、Int、Long等
    public native boolean isPrimitive();


    //clazz是否为注解
    public boolean isAnnotation() {
        return (getModifiers() & ANNOTATION) != 0;
    }


    //如果这个类是一个合成类，返回true ; 返回false其他。
    public boolean isSynthetic() {
        return (getModifiers() & SYNTHETIC) != 0;
    }

    //返回由 类对象表示的实体（类，接口，数组类，原始类型或空白）的名称，作为 String 。例如：java.lang.String、byte，int等
    public String getName() {
        String name = this.name;
        if (name == null)
            this.name = name = getName0();
        return name;
    }

    // cache the name to reduce the number of calls into the VM
    //缓存名称以减少对VM的调用
    private transient String name;

    private native String getName0();


    //获取一个类加载器，可以将.class文件，加载到内存中
    @CallerSensitive
    public ClassLoader getClassLoader() {
        //从JVM虚拟机中获取一个ClassLoader类加载器对象
        ClassLoader cl = getClassLoader0();
        //判断是否为null，为null，直接返回null
        if (cl == null)
            return null;
        //获取程序安全管理器，确保程序的安全执行
        SecurityManager sm = System.getSecurityManager();
        //判断程序是否安全
        if (sm != null) {
            //检查类加载器的许可,被许可则返回
            ClassLoader.checkClassLoaderPermission(cl, Reflection.getCallerClass());
        }
        return cl;
    }

    // Package-private to allow ClassLoader access
    //包私有，以允许类加载器访问
    ClassLoader getClassLoader0() {
        return classLoader;
    }

    //非私有构造函数在JVM中初始化
    //这个字段是从反射访问中过滤的，即getDeclaredField将抛出NoSuchFieldException
    private final ClassLoader classLoader;

    //返回一个 TypeVariable对象的数组，它们以声明顺序表示由此 GenericDeclaration对象表示的通用声明声明的类型变量。
    @SuppressWarnings("unchecked")
    public TypeVariable<Class<T>>[] getTypeParameters() {
        //类信息存储库，获取通用的信息
        ClassRepository info = getGenericInfo();
        //判断是否为null
        if (info != null)
            //返回参数类型数组
            return (TypeVariable<Class<T>>[]) info.getTypeParameters();
        else
            //返回参数类型数组
            return (TypeVariable<Class<T>>[]) new TypeVariable<?>[0];
    }


    //通过clazz对象获取相应类对象的父类
    public native Class<? super T> getSuperclass();


    //返回 Type表示此所表示的实体（类，接口，基本类型或void）的直接超类 类
    //应该是Object
    public Type getGenericSuperclass() {
        ClassRepository info = getGenericInfo();
        if (info == null) {
            return getSuperclass();
        }
        //如果是接口，直接返回null
        if (isInterface()) {
            return null;
        }

        return info.getSuperclass();
    }


    //获取此类的所在的包名。
    public Package getPackage() {
        return Package.getPackage(this);
    }

    //获取该类的接口的Class类对象
    public Class<?>[] getInterfaces() {
        //获取反射数据类，ReflectionData在sun包中
        ReflectionData<T> rd = reflectionData();
        if (rd == null) {
            // no cloning required
            //不需要克隆,直接返回
            return getInterfaces0();
        } else {
            Class<?>[] interfaces = rd.interfaces;
            //rd.interfaces为null，从本地获取
            if (interfaces == null) {
                interfaces = getInterfaces0();
                rd.interfaces = interfaces;
            }
            // defensively copy before handing over to user code
            //防御复制之前，移交给用户代码
            return interfaces.clone();
        }
    }

    //获取该类的接口
    private native Class<?>[] getInterfaces0();

    //返回 Type表示通过由该对象所表示的类或接口直接实现的接口秒。
    public Type[] getGenericInterfaces() {
        ClassRepository info = getGenericInfo();
        return (info == null) ? getInterfaces() : info.getSuperInterfaces();
    }


    //返回 类数组的组件类型的Class
    public native Class<?> getComponentType();


    //返回此类或接口的Java语言修饰符(已编码),用一个整数表示
    public native int getModifiers();


    //获取该类的签名者。
    public native Object[] getSigners();


    //设置该类的签名者。
    native void setSigners(Object[] signers);


    //如果此类对象表示方法中的本地或匿名类，则返回表示基础类的即时封闭方法的方法对象。
    @CallerSensitive
    public Method getEnclosingMethod() throws SecurityException {
        EnclosingMethodInfo enclosingInfo = getEnclosingMethodInfo();

        if (enclosingInfo == null)
            return null;
        else {
            if (!enclosingInfo.isMethod())
                return null;

            MethodRepository typeInfo = MethodRepository.make(enclosingInfo.getDescriptor(),
                    getFactory());
            Class<?> returnType = toClass(typeInfo.getReturnType());
            Type[] parameterTypes = typeInfo.getParameterTypes();
            Class<?>[] parameterClasses = new Class<?>[parameterTypes.length];

            // Convert Types to Classes; returned types *should*
            // be class objects since the methodDescriptor's used
            // don't have generics information
            for (int i = 0; i < parameterClasses.length; i++)
                parameterClasses[i] = toClass(parameterTypes[i]);

            // Perform access check
            Class<?> enclosingCandidate = enclosingInfo.getEnclosingClass();
            enclosingCandidate.checkMemberAccess(Member.DECLARED,
                    Reflection.getCallerClass(), true);
            /*
             * Loop over all declared methods; match method name,
             * number of and type of parameters, *and* return
             * type.  Matching return type is also necessary
             * because of covariant returns, etc.
             */
            for (Method m : enclosingCandidate.getDeclaredMethods()) {
                if (m.getName().equals(enclosingInfo.getName())) {
                    Class<?>[] candidateParamClasses = m.getParameterTypes();
                    if (candidateParamClasses.length == parameterClasses.length) {
                        boolean matches = true;
                        for (int i = 0; i < candidateParamClasses.length; i++) {
                            if (!candidateParamClasses[i].equals(parameterClasses[i])) {
                                matches = false;
                                break;
                            }
                        }

                        if (matches) { // finally, check return type
                            if (m.getReturnType().equals(returnType))
                                return m;
                        }
                    }
                }
            }

            throw new InternalError("Enclosing method not found");
        }
    }

    private native Object[] getEnclosingMethod0();

    private EnclosingMethodInfo getEnclosingMethodInfo() {
        Object[] enclosingInfo = getEnclosingMethod0();
        if (enclosingInfo == null)
            return null;
        else {
            return new EnclosingMethodInfo(enclosingInfo);
        }
    }

    private final static class EnclosingMethodInfo {
        private Class<?> enclosingClass;
        private String name;
        private String descriptor;

        private EnclosingMethodInfo(Object[] enclosingInfo) {
            if (enclosingInfo.length != 3)
                throw new InternalError("Malformed enclosing method information");
            try {
                // The array is expected to have three elements:

                // the immediately enclosing class
                enclosingClass = (Class<?>) enclosingInfo[0];
                assert (enclosingClass != null);

                // the immediately enclosing method or constructor's
                // name (can be null).
                name = (String) enclosingInfo[1];

                // the immediately enclosing method or constructor's
                // descriptor (null iff name is).
                descriptor = (String) enclosingInfo[2];
                assert ((name != null && descriptor != null) || name == descriptor);
            } catch (ClassCastException cce) {
                throw new InternalError("Invalid type in enclosing method information", cce);
            }
        }

        boolean isPartial() {
            return enclosingClass == null || name == null || descriptor == null;
        }

        boolean isConstructor() {
            return !isPartial() && "<init>".equals(name);
        }

        boolean isMethod() {
            return !isPartial() && !isConstructor() && !"<clinit>".equals(name);
        }

        Class<?> getEnclosingClass() {
            return enclosingClass;
        }

        String getName() {
            return name;
        }

        String getDescriptor() {
            return descriptor;
        }

    }

    private static Class<?> toClass(Type o) {
        if (o instanceof GenericArrayType)
            return Array.newInstance(toClass(((GenericArrayType) o).getGenericComponentType()),
                    0)
                    .getClass();
        return (Class<?>) o;
    }

    //如果此类对象表示构造函数中的本地或匿名类，则返回表示底层类的立即封闭构造函数的Constructor对象。
    @CallerSensitive
    public Constructor<?> getEnclosingConstructor() throws SecurityException {
        EnclosingMethodInfo enclosingInfo = getEnclosingMethodInfo();

        if (enclosingInfo == null)
            return null;
        else {
            if (!enclosingInfo.isConstructor())
                return null;

            ConstructorRepository typeInfo = ConstructorRepository.make(enclosingInfo.getDescriptor(),
                    getFactory());
            Type[] parameterTypes = typeInfo.getParameterTypes();
            Class<?>[] parameterClasses = new Class<?>[parameterTypes.length];

            // Convert Types to Classes; returned types *should*
            // be class objects since the methodDescriptor's used
            // don't have generics information
            for (int i = 0; i < parameterClasses.length; i++)
                parameterClasses[i] = toClass(parameterTypes[i]);

            // Perform access check
            Class<?> enclosingCandidate = enclosingInfo.getEnclosingClass();
            enclosingCandidate.checkMemberAccess(Member.DECLARED,
                    Reflection.getCallerClass(), true);
            /*
             * Loop over all declared constructors; match number
             * of and type of parameters.
             */
            for (Constructor<?> c : enclosingCandidate.getDeclaredConstructors()) {
                Class<?>[] candidateParamClasses = c.getParameterTypes();
                if (candidateParamClasses.length == parameterClasses.length) {
                    boolean matches = true;
                    for (int i = 0; i < candidateParamClasses.length; i++) {
                        if (!candidateParamClasses[i].equals(parameterClasses[i])) {
                            matches = false;
                            break;
                        }
                    }

                    if (matches)
                        return c;
                }
            }

            throw new InternalError("Enclosing constructor not found");
        }
    }


    /**
     * 如果Class字节码对象表示的类，是另一个类或接口的成员变量，则返回此类在哪个类中作为成员变量的类的类对象（Class）；
     * 如果该类没有被其他类在内部声明为成员变量，则返回null
     * @return
     * @throws SecurityException
     */
    @CallerSensitive
    public Class<?> getDeclaringClass() throws SecurityException {
        final Class<?> candidate = getDeclaringClass0();

        if (candidate != null)
            candidate.checkPackageAccess(
                    ClassLoader.getClassLoader(Reflection.getCallerClass()), true);
        return candidate;
    }

    private native Class<?> getDeclaringClass0();

    //返回底层类的即时封闭类。
    @CallerSensitive
    public Class<?> getEnclosingClass() throws SecurityException {
        // There are five kinds of classes (or interfaces):
        // a) Top level classes
        // b) Nested classes (static member classes)
        // c) Inner classes (non-static member classes)
        // d) Local classes (named classes declared within a method)
        // e) Anonymous classes


        // JVM Spec 4.8.6: A class must have an EnclosingMethod
        // attribute if and only if it is a local class or an
        // anonymous class.
        EnclosingMethodInfo enclosingInfo = getEnclosingMethodInfo();
        Class<?> enclosingCandidate;

        if (enclosingInfo == null) {
            // This is a top level or a nested class or an inner class (a, b, or c)
            enclosingCandidate = getDeclaringClass();
        } else {
            Class<?> enclosingClass = enclosingInfo.getEnclosingClass();
            // This is a local class or an anonymous class (d or e)
            if (enclosingClass == this || enclosingClass == null)
                throw new InternalError("Malformed enclosing method information");
            else
                enclosingCandidate = enclosingClass;
        }

        if (enclosingCandidate != null)
            enclosingCandidate.checkPackageAccess(
                    ClassLoader.getClassLoader(Reflection.getCallerClass()), true);
        return enclosingCandidate;
    }

   //返回源代码中给出的基础类的简单名称。
    public String getSimpleName() {
        if (isArray())
            return getComponentType().getSimpleName() + "[]";

        //获取简单的二进制名称
        String simpleName = getSimpleBinaryName();
        if (simpleName == null) { // top level class
            simpleName = getName();
            return simpleName.substring(simpleName.lastIndexOf(".") + 1); // strip the package name
        }
        // According to JLS3 "Binary Compatibility" (13.1) the binary
        // name of non-package classes (not top level) is the binary
        // name of the immediately enclosing class followed by a '$' followed by:
        // (for nested and inner classes): the simple name.
        // (for local classes): 1 or more digits followed by the simple name.
        // (for anonymous classes): 1 or more digits.

        // Since getSimpleBinaryName() will strip the binary name of
        // the immediatly enclosing class, we are now looking at a
        // string that matches the regular expression "\$[0-9]*"
        // followed by a simple name (considering the simple of an
        // anonymous class to be the empty string).

        // Remove leading "\$[0-9]*" from the name
        int length = simpleName.length();
        if (length < 1 || simpleName.charAt(0) != '$')
            throw new InternalError("Malformed class name");
        int index = 1;
        while (index < length && isAsciiDigit(simpleName.charAt(index)))
            index++;
        // Eventually, this is the empty string iff this is an anonymous class
        return simpleName.substring(index);
    }

    //为此类型的名称返回一个内容丰富的字符串。
    public String getTypeName() {
        if (isArray()) {
            try {
                Class<?> cl = this;
                int dimensions = 0;
                while (cl.isArray()) {
                    dimensions++;
                    cl = cl.getComponentType();
                }
                StringBuilder sb = new StringBuilder();
                sb.append(cl.getName());
                for (int i = 0; i < dimensions; i++) {
                    sb.append("[]");
                }
                return sb.toString();
            } catch (Throwable e) { /*FALLTHRU*/ }
        }
        return getName();
    }

    /**
     * Character.isDigit answers {@code true} to some non-ascii
     * digits.  This one does not.
     */
    private static boolean isAsciiDigit(char c) {
        return '0' <= c && c <= '9';
    }

    //返回由Java语言规范定义的基础类的规范名称。
    public String getCanonicalName() {
        if (isArray()) {
            String canonicalName = getComponentType().getCanonicalName();
            if (canonicalName != null)
                return canonicalName + "[]";
            else
                return null;
        }
        if (isLocalOrAnonymousClass())
            return null;
        Class<?> enclosingClass = getEnclosingClass();
        if (enclosingClass == null) { // top level class
            return getName();
        } else {
            String enclosingName = enclosingClass.getCanonicalName();
            if (enclosingName == null)
                return null;
            return enclosingName + "." + getSimpleName();
        }
    }

    //是否为匿名类
    public boolean isAnonymousClass() {
        return "".equals(getSimpleName());
    }

    //该类是否为本地类
    public boolean isLocalClass() {
        return isLocalOrAnonymousClass() && !isAnonymousClass();
    }

    //是否为成员类
    public boolean isMemberClass() {
        return getSimpleBinaryName() != null && !isLocalOrAnonymousClass();
    }

    //获取简单的二进制名称
    private String getSimpleBinaryName() {
        Class<?> enclosingClass = getEnclosingClass();
        if (enclosingClass == null) // top level class
            return null;
        // Otherwise, strip the enclosing class' name
        try {
            return getName().substring(enclosingClass.getName().length());
        } catch (IndexOutOfBoundsException ex) {
            throw new InternalError("Malformed class name", ex);
        }
    }

    //是否为本地或匿名类
    private boolean isLocalOrAnonymousClass() {
        // JVM Spec 4.8.6: A class must have an EnclosingMethod
        // attribute if and only if it is a local class or an
        // anonymous class.
        return getEnclosingMethodInfo() != null;
    }

    //得到该类及其父类所有的public的内部类。
    @CallerSensitive
    public Class<?>[] getClasses() {
        checkMemberAccess(Member.PUBLIC, Reflection.getCallerClass(), false);

        // Privileged so this implementation can look at DECLARED classes,
        // something the caller might not have privilege to do.  The code here
        // is allowed to look at DECLARED classes because (1) it does not hand
        // out anything other than public members and (2) public member access
        // has already been ok'd by the SecurityManager.

        return java.security.AccessController.doPrivileged(
                new java.security.PrivilegedAction<Class<?>[]>() {
                    public Class<?>[] run() {
                        List<Class<?>> list = new ArrayList<>();
                        Class<?> currentClass = Class.this;
                        while (currentClass != null) {
                            Class<?>[] members = currentClass.getDeclaredClasses();
                            for (int i = 0; i < members.length; i++) {
                                if (Modifier.isPublic(members[i].getModifiers())) {
                                    list.add(members[i]);
                                }
                            }
                            currentClass = currentClass.getSuperclass();
                        }
                        return list.toArray(new Class<?>[0]);
                    }
                });
    }


    //获得某个类的所有的公共（public）的字段，包括父类中的字段。
    //获取该类的指定的所有public修饰的成员变量字段，返回一个Field类型的数组
    @CallerSensitive
    public Field[] getFields() throws SecurityException {
        checkMemberAccess(Member.PUBLIC, Reflection.getCallerClass(), true);
        return copyFields(privateGetPublicFields(null));
    }


    //获取该类的指定的所有public修饰的成员方法，返回一个Method类型的数组
    @CallerSensitive
    public Method[] getMethods() throws SecurityException {
        checkMemberAccess(Member.PUBLIC, Reflection.getCallerClass(), true);
        return copyMethods(privateGetPublicMethods());
    }

    //获取该类的指定的所有public修饰的构造器，返回一个Constructor类型的数组
    @CallerSensitive
    public Constructor<?>[] getConstructors() throws SecurityException {
        checkMemberAccess(Member.PUBLIC, Reflection.getCallerClass(), true);
        return copyConstructors(privateGetDeclaredConstructors(true));
    }


    //根据变量的名称，返回一个变量对象Field，必须是public修饰的
    @CallerSensitive
    public Field getField(String name)
            throws NoSuchFieldException, SecurityException {
        checkMemberAccess(Member.PUBLIC, Reflection.getCallerClass(), true);
        //调用本地的方法
        Field field = getField0(name);
        if (field == null) {
            throw new NoSuchFieldException(name);
        }
        return field;
    }

    //根据方法名称和方法中参数的数据类型，获取该类的方法对象Method,方法必须是public修饰的,包括继承的
    //由于java中的方法可以重载，所以必须要指定参数得数据类型
    //Method method = clazz.getMethod("update",String.class,Integer.class);
    @CallerSensitive
    public Method getMethod(String name, Class<?>... parameterTypes)
            throws NoSuchMethodException, SecurityException {
        //检查是否为public修饰的
        checkMemberAccess(Member.PUBLIC, Reflection.getCallerClass(), true);
        //调用本地方法
        Method method = getMethod0(name, parameterTypes, true);
        if (method == null) {
            throw new NoSuchMethodException(getName() + "." + name + argumentTypesToString(parameterTypes));
        }
        return method;
    }


    //根据参数获取该的构造器对象，构造器必须是public的，包括继承的
    @CallerSensitive
    public Constructor<T> getConstructor(Class<?>... parameterTypes)
            throws NoSuchMethodException, SecurityException {
        checkMemberAccess(Member.PUBLIC, Reflection.getCallerClass(), true);
        return getConstructor0(parameterTypes, Member.PUBLIC);
    }

    //得到该类所有的内部类，除去父类的。
    @CallerSensitive
    public Class<?>[] getDeclaredClasses() throws SecurityException {
        checkMemberAccess(Member.DECLARED, Reflection.getCallerClass(), false);
        return getDeclaredClasses0();
    }


    //获得某个类的所有声明的字段，即包括public、private和proteced，但是不包括父类的申明字段。
    @CallerSensitive
    public Field[] getDeclaredFields() throws SecurityException {
        checkMemberAccess(Member.DECLARED, Reflection.getCallerClass(), true);
        return copyFields(privateGetDeclaredFields(false));
    }


    //返回 Class 对象表示的类或接口声明的所有方法，包括公共、保护、默认（包）访问和私有方法，但不包括继承的方法。
    @CallerSensitive
    public Method[] getDeclaredMethods() throws SecurityException {
        checkMemberAccess(Member.DECLARED, Reflection.getCallerClass(), true);
        return copyMethods(privateGetDeclaredMethods(false));
    }


    //获取class表的类对象的所有权限的构造器，不包括父类继承的
    @CallerSensitive
    public Constructor<?>[] getDeclaredConstructors() throws SecurityException {
        checkMemberAccess(Member.DECLARED, Reflection.getCallerClass(), true);
        return copyConstructors(privateGetDeclaredConstructors(false));
    }



    //获取class表的类对象的所有权限的成员变量，包括父类继承的
    @CallerSensitive
    public Field getDeclaredField(String name)
            throws NoSuchFieldException, SecurityException {
        checkMemberAccess(Member.DECLARED, Reflection.getCallerClass(), true);
        Field field = searchFields(privateGetDeclaredFields(false), name);
        if (field == null) {
            throw new NoSuchFieldException(name);
        }
        return field;
    }



    //获取class表的类对象的所有权限的方法，不包括父类继承的
    @CallerSensitive
    public Method getDeclaredMethod(String name, Class<?>... parameterTypes)
            throws NoSuchMethodException, SecurityException {
        checkMemberAccess(Member.DECLARED, Reflection.getCallerClass(), true);
        Method method = searchMethods(privateGetDeclaredMethods(false), name, parameterTypes);
        if (method == null) {
            throw new NoSuchMethodException(getName() + "." + name + argumentTypesToString(parameterTypes));
        }
        return method;
    }



    //根据参数获取一个不管什么权限的构造器，不包括父类继承的
    @CallerSensitive
    public Constructor<T> getDeclaredConstructor(Class<?>... parameterTypes)
            throws NoSuchMethodException, SecurityException {
        checkMemberAccess(Member.DECLARED, Reflection.getCallerClass(), true);
        return getConstructor0(parameterTypes, Member.DECLARED);
    }

    //查找具有给定名称的资源流。
    public InputStream getResourceAsStream(String name) {
        name = resolveName(name);
        ClassLoader cl = getClassLoader0();
        if (cl == null) {
            // A system class.
            return ClassLoader.getSystemResourceAsStream(name);
        }
        return cl.getResourceAsStream(name);
    }

    //查找具有给定名称的资源。
    public java.net.URL getResource(String name) {
        name = resolveName(name);
        ClassLoader cl = getClassLoader0();
        if (cl == null) {
            // A system class.
            return ClassLoader.getSystemResource(name);
        }
        return cl.getResource(name);
    }


    /**
     * 当内部域为空时返回保护域
     */
    private static java.security.ProtectionDomain allPermDomain;


    //返回 ProtectionDomain 。
    public java.security.ProtectionDomain getProtectionDomain() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(SecurityConstants.GET_PD_PERMISSION);
        }
        java.security.ProtectionDomain pd = getProtectionDomain0();
        if (pd == null) {
            if (allPermDomain == null) {
                java.security.Permissions perms =
                        new java.security.Permissions();
                perms.add(SecurityConstants.ALL_PERMISSION);
                allPermDomain =
                        new java.security.ProtectionDomain(null, perms);
            }
            pd = allPermDomain;
        }
        return pd;
    }


    /**
     * Returns the ProtectionDomain of this class.
     */
    private native java.security.ProtectionDomain getProtectionDomain0();

    /*
     * Return the Virtual Machine's Class object for the named
     * primitive type.
     */
    static native Class<?> getPrimitiveClass(String name);

    /*
     * Check if client is allowed to access members.  If access is denied,
     * throw a SecurityException.
     *
     * This method also enforces package access.
     *
     * <p> Default policy: allow all clients access with normal Java access
     * control.
     */
    private void checkMemberAccess(int which, Class<?> caller, boolean checkProxyInterfaces) {
        final SecurityManager s = System.getSecurityManager();
        if (s != null) {
            /* Default policy allows access to all {@link Member#PUBLIC} members,
             * as well as access to classes that have the same class loader as the caller.
             * In all other cases, it requires RuntimePermission("accessDeclaredMembers")
             * permission.
             */
            final ClassLoader ccl = ClassLoader.getClassLoader(caller);
            final ClassLoader cl = getClassLoader0();
            if (which != Member.PUBLIC) {
                if (ccl != cl) {
                    s.checkPermission(SecurityConstants.CHECK_MEMBER_ACCESS_PERMISSION);
                }
            }
            this.checkPackageAccess(ccl, checkProxyInterfaces);
        }
    }

    /*
     * Checks if a client loaded in ClassLoader ccl is allowed to access this
     * class under the current package access policy. If access is denied,
     * throw a SecurityException.
     */
    private void checkPackageAccess(final ClassLoader ccl, boolean checkProxyInterfaces) {
        final SecurityManager s = System.getSecurityManager();
        if (s != null) {
            final ClassLoader cl = getClassLoader0();

            if (ReflectUtil.needsPackageAccessCheck(ccl, cl)) {
                String name = this.getName();
                int i = name.lastIndexOf('.');
                if (i != -1) {
                    // skip the package access check on a proxy class in default proxy package
                    String pkg = name.substring(0, i);
                    if (!Proxy.isProxyClass(this) || ReflectUtil.isNonPublicProxyClass(this)) {
                        s.checkPackageAccess(pkg);
                    }
                }
            }
            // check package access on the proxy interfaces
            if (checkProxyInterfaces && Proxy.isProxyClass(this)) {
                ReflectUtil.checkProxyPackageAccess(ccl, this.getInterfaces());
            }
        }
    }

    /**
     * Add a package name prefix if the name is not absolute Remove leading "/"
     * if name is absolute
     */
    private String resolveName(String name) {
        if (name == null) {
            return name;
        }
        if (!name.startsWith("/")) {
            Class<?> c = this;
            while (c.isArray()) {
                c = c.getComponentType();
            }
            String baseName = c.getName();
            int index = baseName.lastIndexOf('.');
            if (index != -1) {
                name = baseName.substring(0, index).replace('.', '/')
                        + "/" + name;
            }
        } else {
            name = name.substring(1);
        }
        return name;
    }

    /**
     * Atomic operations support.--原子操作支持。
     */
    private static class Atomic {
        // initialize Unsafe machinery here, since we need to call Class.class instance method
        // and have to avoid calling it in the static initializer of the Class class...
        private static final Unsafe unsafe = Unsafe.getUnsafe();
        // offset of Class.reflectionData instance field
        private static final long reflectionDataOffset;
        // offset of Class.annotationType instance field
        private static final long annotationTypeOffset;
        // offset of Class.annotationData instance field
        private static final long annotationDataOffset;

        static {
            Field[] fields = Class.class.getDeclaredFields0(false); // bypass caches
            reflectionDataOffset = objectFieldOffset(fields, "reflectionData");
            annotationTypeOffset = objectFieldOffset(fields, "annotationType");
            annotationDataOffset = objectFieldOffset(fields, "annotationData");
        }

        private static long objectFieldOffset(Field[] fields, String fieldName) {
            Field field = searchFields(fields, fieldName);
            if (field == null) {
                throw new Error("No " + fieldName + " field found in java.lang.Class");
            }
            return unsafe.objectFieldOffset(field);
        }

        static <T> boolean casReflectionData(Class<?> clazz,
                                             SoftReference<ReflectionData<T>> oldData,
                                             SoftReference<ReflectionData<T>> newData) {
            return unsafe.compareAndSwapObject(clazz, reflectionDataOffset, oldData, newData);
        }

        static <T> boolean casAnnotationType(Class<?> clazz,
                                             AnnotationType oldType,
                                             AnnotationType newType) {
            return unsafe.compareAndSwapObject(clazz, annotationTypeOffset, oldType, newType);
        }

        static <T> boolean casAnnotationData(Class<?> clazz,
                                             AnnotationData oldData,
                                             AnnotationData newData) {
            return unsafe.compareAndSwapObject(clazz, annotationDataOffset, oldData, newData);
        }
    }

    /**
     * Reflection support.
     */

    // Caches for certain reflective results
    private static boolean useCaches = true;

    // reflection data that might get invalidated when JVM TI RedefineClasses() is called
    //调用JVM TI redefineclass()时可能失效的反射数据
    //将可能丢失的数据封装在ReflectionData中
    private static class ReflectionData<T> {
        volatile Field[] declaredFields;
        volatile Field[] publicFields;
        volatile Method[] declaredMethods;
        volatile Method[] publicMethods;
        volatile Constructor<T>[] declaredConstructors;
        volatile Constructor<T>[] publicConstructors;
        // Intermediate results for getFields and getMethods
        volatile Field[] declaredPublicFields;
        volatile Method[] declaredPublicMethods;
        volatile Class<?>[] interfaces;

        // Value of classRedefinedCount when we created this ReflectionData instance
        final int redefinedCount;

        ReflectionData(int redefinedCount) {
            this.redefinedCount = redefinedCount;
        }
    }

    private volatile transient SoftReference<ReflectionData<T>> reflectionData;

    // Incremented by the VM on each call to JVM TI RedefineClasses()
    // that redefines this class or a superclass.
    private volatile transient int classRedefinedCount = 0;

    // Lazily create and cache ReflectionData
    private ReflectionData<T> reflectionData() {
        SoftReference<ReflectionData<T>> reflectionData = this.reflectionData;
        int classRedefinedCount = this.classRedefinedCount;
        ReflectionData<T> rd;
        if (useCaches &&
                reflectionData != null &&
                (rd = reflectionData.get()) != null &&
                rd.redefinedCount == classRedefinedCount) {
            return rd;
        }
        // else no SoftReference or cleared SoftReference or stale ReflectionData
        // -> create and replace new instance
        return newReflectionData(reflectionData, classRedefinedCount);
    }

    private ReflectionData<T> newReflectionData(SoftReference<ReflectionData<T>> oldReflectionData,
                                                int classRedefinedCount) {
        if (!useCaches) return null;

        while (true) {
            ReflectionData<T> rd = new ReflectionData<>(classRedefinedCount);
            // try to CAS it...
            if (Atomic.casReflectionData(this, oldReflectionData, new SoftReference<>(rd))) {
                return rd;
            }
            // else retry
            oldReflectionData = this.reflectionData;
            classRedefinedCount = this.classRedefinedCount;
            if (oldReflectionData != null &&
                    (rd = oldReflectionData.get()) != null &&
                    rd.redefinedCount == classRedefinedCount) {
                return rd;
            }
        }
    }

    // Generic signature handling
    private native String getGenericSignature0();

    // Generic info repository; lazily initialized
    private volatile transient ClassRepository genericInfo;

    // accessor for factory
    private GenericsFactory getFactory() {
        // create scope and factory
        return CoreReflectionFactory.make(this, ClassScope.make(this));
    }

    // accessor for generic info repository;
    // generic info is lazily initialized
    private ClassRepository getGenericInfo() {
        ClassRepository genericInfo = this.genericInfo;
        if (genericInfo == null) {
            String signature = getGenericSignature0();
            if (signature == null) {
                genericInfo = ClassRepository.NONE;
            } else {
                genericInfo = ClassRepository.make(signature, getFactory());
            }
            this.genericInfo = genericInfo;
        }
        return (genericInfo != ClassRepository.NONE) ? genericInfo : null;
    }

    // Annotations handling
    native byte[] getRawAnnotations();

    // Since 1.8
    native byte[] getRawTypeAnnotations();

    static byte[] getExecutableTypeAnnotationBytes(Executable ex) {
        return getReflectionFactory().getExecutableTypeAnnotationBytes(ex);
    }

    native ConstantPool getConstantPool();

    //
    //
    // java.lang.reflect.Field handling
    //
    //

    // Returns an array of "root" fields. These Field objects must NOT
    // be propagated to the outside world, but must instead be copied
    // via ReflectionFactory.copyField.
    private Field[] privateGetDeclaredFields(boolean publicOnly) {
        checkInitted();
        Field[] res;
        ReflectionData<T> rd = reflectionData();
        if (rd != null) {
            res = publicOnly ? rd.declaredPublicFields : rd.declaredFields;
            if (res != null) return res;
        }
        // No cached value available; request value from VM
        res = Reflection.filterFields(this, getDeclaredFields0(publicOnly));
        if (rd != null) {
            if (publicOnly) {
                rd.declaredPublicFields = res;
            } else {
                rd.declaredFields = res;
            }
        }
        return res;
    }

    // Returns an array of "root" fields. These Field objects must NOT
    // be propagated to the outside world, but must instead be copied
    // via ReflectionFactory.copyField.
    private Field[] privateGetPublicFields(Set<Class<?>> traversedInterfaces) {
        checkInitted();
        Field[] res;
        ReflectionData<T> rd = reflectionData();
        if (rd != null) {
            res = rd.publicFields;
            if (res != null) return res;
        }

        // No cached value available; compute value recursively.
        // Traverse in correct order for getField().
        List<Field> fields = new ArrayList<>();
        if (traversedInterfaces == null) {
            traversedInterfaces = new HashSet<>();
        }

        // Local fields
        Field[] tmp = privateGetDeclaredFields(true);
        addAll(fields, tmp);

        // Direct superinterfaces, recursively
        for (Class<?> c : getInterfaces()) {
            if (!traversedInterfaces.contains(c)) {
                traversedInterfaces.add(c);
                addAll(fields, c.privateGetPublicFields(traversedInterfaces));
            }
        }

        // Direct superclass, recursively
        if (!isInterface()) {
            Class<?> c = getSuperclass();
            if (c != null) {
                addAll(fields, c.privateGetPublicFields(traversedInterfaces));
            }
        }

        res = new Field[fields.size()];
        fields.toArray(res);
        if (rd != null) {
            rd.publicFields = res;
        }
        return res;
    }

    private static void addAll(Collection<Field> c, Field[] o) {
        for (int i = 0; i < o.length; i++) {
            c.add(o[i]);
        }
    }


    //
    //
    // java.lang.reflect.Constructor handling
    //
    //

    // Returns an array of "root" constructors. These Constructor
    // objects must NOT be propagated to the outside world, but must
    // instead be copied via ReflectionFactory.copyConstructor.
    private Constructor<T>[] privateGetDeclaredConstructors(boolean publicOnly) {
        checkInitted();
        Constructor<T>[] res;
        ReflectionData<T> rd = reflectionData();
        if (rd != null) {
            res = publicOnly ? rd.publicConstructors : rd.declaredConstructors;
            if (res != null) return res;
        }
        // No cached value available; request value from VM
        if (isInterface()) {
            @SuppressWarnings("unchecked")
            Constructor<T>[] temporaryRes = (Constructor<T>[]) new Constructor<?>[0];
            res = temporaryRes;
        } else {
            res = getDeclaredConstructors0(publicOnly);
        }
        if (rd != null) {
            if (publicOnly) {
                rd.publicConstructors = res;
            } else {
                rd.declaredConstructors = res;
            }
        }
        return res;
    }

    //
    //
    // java.lang.reflect.Method handling
    //
    //

    // Returns an array of "root" methods. These Method objects must NOT
    // be propagated to the outside world, but must instead be copied
    // via ReflectionFactory.copyMethod.
    private Method[] privateGetDeclaredMethods(boolean publicOnly) {
        checkInitted();
        Method[] res;
        ReflectionData<T> rd = reflectionData();
        if (rd != null) {
            res = publicOnly ? rd.declaredPublicMethods : rd.declaredMethods;
            if (res != null) return res;
        }
        // No cached value available; request value from VM
        res = Reflection.filterMethods(this, getDeclaredMethods0(publicOnly));
        if (rd != null) {
            if (publicOnly) {
                rd.declaredPublicMethods = res;
            } else {
                rd.declaredMethods = res;
            }
        }
        return res;
    }

    static class MethodArray {
        // Don't add or remove methods except by add() or remove() calls.
        private Method[] methods;
        private int length;
        private int defaults;

        MethodArray() {
            this(20);
        }

        MethodArray(int initialSize) {
            if (initialSize < 2)
                throw new IllegalArgumentException("Size should be 2 or more");

            methods = new Method[initialSize];
            length = 0;
            defaults = 0;
        }

        boolean hasDefaults() {
            return defaults != 0;
        }

        void add(Method m) {
            if (length == methods.length) {
                methods = Arrays.copyOf(methods, 2 * methods.length);
            }
            methods[length++] = m;

            if (m != null && m.isDefault())
                defaults++;
        }

        void addAll(Method[] ma) {
            for (int i = 0; i < ma.length; i++) {
                add(ma[i]);
            }
        }

        void addAll(MethodArray ma) {
            for (int i = 0; i < ma.length(); i++) {
                add(ma.get(i));
            }
        }

        void addIfNotPresent(Method newMethod) {
            for (int i = 0; i < length; i++) {
                Method m = methods[i];
                if (m == newMethod || (m != null && m.equals(newMethod))) {
                    return;
                }
            }
            add(newMethod);
        }

        void addAllIfNotPresent(MethodArray newMethods) {
            for (int i = 0; i < newMethods.length(); i++) {
                Method m = newMethods.get(i);
                if (m != null) {
                    addIfNotPresent(m);
                }
            }
        }

        /* Add Methods declared in an interface to this MethodArray.
         * Static methods declared in interfaces are not inherited.
         */
        void addInterfaceMethods(Method[] methods) {
            for (Method candidate : methods) {
                if (!Modifier.isStatic(candidate.getModifiers())) {
                    add(candidate);
                }
            }
        }

        int length() {
            return length;
        }

        Method get(int i) {
            return methods[i];
        }

        Method getFirst() {
            for (Method m : methods)
                if (m != null)
                    return m;
            return null;
        }

        void removeByNameAndDescriptor(Method toRemove) {
            for (int i = 0; i < length; i++) {
                Method m = methods[i];
                if (m != null && matchesNameAndDescriptor(m, toRemove)) {
                    remove(i);
                }
            }
        }

        private void remove(int i) {
            if (methods[i] != null && methods[i].isDefault())
                defaults--;
            methods[i] = null;
        }

        private boolean matchesNameAndDescriptor(Method m1, Method m2) {
            return m1.getReturnType() == m2.getReturnType() &&
                    m1.getName() == m2.getName() && // name is guaranteed to be interned
                    arrayContentsEq(m1.getParameterTypes(),
                            m2.getParameterTypes());
        }

        void compactAndTrim() {
            int newPos = 0;
            // Get rid of null slots
            for (int pos = 0; pos < length; pos++) {
                Method m = methods[pos];
                if (m != null) {
                    if (pos != newPos) {
                        methods[newPos] = m;
                    }
                    newPos++;
                }
            }
            if (newPos != methods.length) {
                methods = Arrays.copyOf(methods, newPos);
            }
        }

        /* Removes all Methods from this MethodArray that have a more specific
         * default Method in this MethodArray.
         *
         * Users of MethodArray are responsible for pruning Methods that have
         * a more specific <em>concrete</em> Method.
         */
        void removeLessSpecifics() {
            if (!hasDefaults())
                return;

            for (int i = 0; i < length; i++) {
                Method m = get(i);
                if (m == null || !m.isDefault())
                    continue;

                for (int j = 0; j < length; j++) {
                    if (i == j)
                        continue;

                    Method candidate = get(j);
                    if (candidate == null)
                        continue;

                    if (!matchesNameAndDescriptor(m, candidate))
                        continue;

                    if (hasMoreSpecificClass(m, candidate))
                        remove(j);
                }
            }
        }

        Method[] getArray() {
            return methods;
        }

        // Returns true if m1 is more specific than m2
        static boolean hasMoreSpecificClass(Method m1, Method m2) {
            Class<?> m1Class = m1.getDeclaringClass();
            Class<?> m2Class = m2.getDeclaringClass();
            return m1Class != m2Class && m2Class.isAssignableFrom(m1Class);
        }
    }


    // Returns an array of "root" methods. These Method objects must NOT
    // be propagated to the outside world, but must instead be copied
    // via ReflectionFactory.copyMethod.
    private Method[] privateGetPublicMethods() {
        checkInitted();
        Method[] res;
        ReflectionData<T> rd = reflectionData();
        if (rd != null) {
            res = rd.publicMethods;
            if (res != null) return res;
        }

        // No cached value available; compute value recursively.
        // Start by fetching public declared methods
        MethodArray methods = new MethodArray();
        {
            Method[] tmp = privateGetDeclaredMethods(true);
            methods.addAll(tmp);
        }
        // Now recur over superclass and direct superinterfaces.
        // Go over superinterfaces first so we can more easily filter
        // out concrete implementations inherited from superclasses at
        // the end.
        MethodArray inheritedMethods = new MethodArray();
        for (Class<?> i : getInterfaces()) {
            inheritedMethods.addInterfaceMethods(i.privateGetPublicMethods());
        }
        if (!isInterface()) {
            Class<?> c = getSuperclass();
            if (c != null) {
                MethodArray supers = new MethodArray();
                supers.addAll(c.privateGetPublicMethods());
                // Filter out concrete implementations of any
                // interface methods
                for (int i = 0; i < supers.length(); i++) {
                    Method m = supers.get(i);
                    if (m != null &&
                            !Modifier.isAbstract(m.getModifiers()) &&
                            !m.isDefault()) {
                        inheritedMethods.removeByNameAndDescriptor(m);
                    }
                }
                // Insert superclass's inherited methods before
                // superinterfaces' to satisfy getMethod's search
                // order
                supers.addAll(inheritedMethods);
                inheritedMethods = supers;
            }
        }
        // Filter out all local methods from inherited ones
        for (int i = 0; i < methods.length(); i++) {
            Method m = methods.get(i);
            inheritedMethods.removeByNameAndDescriptor(m);
        }
        methods.addAllIfNotPresent(inheritedMethods);
        methods.removeLessSpecifics();
        methods.compactAndTrim();
        res = methods.getArray();
        if (rd != null) {
            rd.publicMethods = res;
        }
        return res;
    }


    //
    // Helpers for fetchers of one field, method, or constructor
    //

    private static Field searchFields(Field[] fields, String name) {
        String internedName = name.intern();
        for (int i = 0; i < fields.length; i++) {
            if (fields[i].getName() == internedName) {
                return getReflectionFactory().copyField(fields[i]);
            }
        }
        return null;
    }

    private Field getField0(String name) throws NoSuchFieldException {
        // Note: the intent is that the search algorithm this routine
        // uses be equivalent to the ordering imposed by
        // privateGetPublicFields(). It fetches only the declared
        // public fields for each class, however, to reduce the number
        // of Field objects which have to be created for the common
        // case where the field being requested is declared in the
        // class which is being queried.
        Field res;
        // Search declared public fields
        if ((res = searchFields(privateGetDeclaredFields(true), name)) != null) {
            return res;
        }
        // Direct superinterfaces, recursively
        Class<?>[] interfaces = getInterfaces();
        for (int i = 0; i < interfaces.length; i++) {
            Class<?> c = interfaces[i];
            if ((res = c.getField0(name)) != null) {
                return res;
            }
        }
        // Direct superclass, recursively
        if (!isInterface()) {
            Class<?> c = getSuperclass();
            if (c != null) {
                if ((res = c.getField0(name)) != null) {
                    return res;
                }
            }
        }
        return null;
    }

    private static Method searchMethods(Method[] methods,
                                        String name,
                                        Class<?>[] parameterTypes) {
        Method res = null;
        String internedName = name.intern();
        for (int i = 0; i < methods.length; i++) {
            Method m = methods[i];
            if (m.getName() == internedName
                    && arrayContentsEq(parameterTypes, m.getParameterTypes())
                    && (res == null
                    || res.getReturnType().isAssignableFrom(m.getReturnType())))
                res = m;
        }

        return (res == null ? res : getReflectionFactory().copyMethod(res));
    }

    private Method getMethod0(String name, Class<?>[] parameterTypes, boolean includeStaticMethods) {
        MethodArray interfaceCandidates = new MethodArray(2);
        Method res = privateGetMethodRecursive(name, parameterTypes, includeStaticMethods, interfaceCandidates);
        if (res != null)
            return res;

        // Not found on class or superclass directly
        interfaceCandidates.removeLessSpecifics();
        return interfaceCandidates.getFirst(); // may be null
    }

    private Method privateGetMethodRecursive(String name,
                                             Class<?>[] parameterTypes,
                                             boolean includeStaticMethods,
                                             MethodArray allInterfaceCandidates) {
        // Note: the intent is that the search algorithm this routine
        // uses be equivalent to the ordering imposed by
        // privateGetPublicMethods(). It fetches only the declared
        // public methods for each class, however, to reduce the
        // number of Method objects which have to be created for the
        // common case where the method being requested is declared in
        // the class which is being queried.
        //
        // Due to default methods, unless a method is found on a superclass,
        // methods declared in any superinterface needs to be considered.
        // Collect all candidates declared in superinterfaces in {@code
        // allInterfaceCandidates} and select the most specific if no match on
        // a superclass is found.

        // Must _not_ return root methods
        Method res;
        // Search declared public methods
        if ((res = searchMethods(privateGetDeclaredMethods(true),
                name,
                parameterTypes)) != null) {
            if (includeStaticMethods || !Modifier.isStatic(res.getModifiers()))
                return res;
        }
        // Search superclass's methods
        if (!isInterface()) {
            Class<? super T> c = getSuperclass();
            if (c != null) {
                if ((res = c.getMethod0(name, parameterTypes, true)) != null) {
                    return res;
                }
            }
        }
        // Search superinterfaces' methods
        Class<?>[] interfaces = getInterfaces();
        for (Class<?> c : interfaces)
            if ((res = c.getMethod0(name, parameterTypes, false)) != null)
                allInterfaceCandidates.add(res);
        // Not found
        return null;
    }

    private Constructor<T> getConstructor0(Class<?>[] parameterTypes,
                                           int which) throws NoSuchMethodException {
        Constructor<T>[] constructors = privateGetDeclaredConstructors((which == Member.PUBLIC));
        for (Constructor<T> constructor : constructors) {
            if (arrayContentsEq(parameterTypes,
                    constructor.getParameterTypes())) {
                return getReflectionFactory().copyConstructor(constructor);
            }
        }
        throw new NoSuchMethodException(getName() + ".<init>" + argumentTypesToString(parameterTypes));
    }

    //
    // Other helpers and base implementation
    //

    private static boolean arrayContentsEq(Object[] a1, Object[] a2) {
        if (a1 == null) {
            return a2 == null || a2.length == 0;
        }

        if (a2 == null) {
            return a1.length == 0;
        }

        if (a1.length != a2.length) {
            return false;
        }

        for (int i = 0; i < a1.length; i++) {
            if (a1[i] != a2[i]) {
                return false;
            }
        }

        return true;
    }

    private static Field[] copyFields(Field[] arg) {
        Field[] out = new Field[arg.length];
        ReflectionFactory fact = getReflectionFactory();
        for (int i = 0; i < arg.length; i++) {
            out[i] = fact.copyField(arg[i]);
        }
        return out;
    }

    private static Method[] copyMethods(Method[] arg) {
        Method[] out = new Method[arg.length];
        ReflectionFactory fact = getReflectionFactory();
        for (int i = 0; i < arg.length; i++) {
            out[i] = fact.copyMethod(arg[i]);
        }
        return out;
    }

    private static <U> Constructor<U>[] copyConstructors(Constructor<U>[] arg) {
        Constructor<U>[] out = arg.clone();
        ReflectionFactory fact = getReflectionFactory();
        for (int i = 0; i < out.length; i++) {
            out[i] = fact.copyConstructor(out[i]);
        }
        return out;
    }

    private native Field[] getDeclaredFields0(boolean publicOnly);

    private native Method[] getDeclaredMethods0(boolean publicOnly);

    private native Constructor<T>[] getDeclaredConstructors0(boolean publicOnly);

    private native Class<?>[] getDeclaredClasses0();

    private static String argumentTypesToString(Class<?>[] argTypes) {
        StringBuilder buf = new StringBuilder();
        buf.append("(");
        if (argTypes != null) {
            for (int i = 0; i < argTypes.length; i++) {
                if (i > 0) {
                    buf.append(", ");
                }
                Class<?> c = argTypes[i];
                buf.append((c == null) ? "null" : c.getName());
            }
        }
        buf.append(")");
        return buf.toString();
    }

    /**
     * use serialVersionUID from JDK 1.1 for interoperability
     */
    private static final long serialVersionUID = 3206093459760846163L;


    /**
     * Class Class is special cased within the Serialization Stream Protocol.
     * <p>
     * A Class instance is written initially into an ObjectOutputStream in the
     * following format:
     * <pre>
     *      {@code TC_CLASS} ClassDescriptor
     *      A ClassDescriptor is a special cased serialization of
     *      a {@code java.io.ObjectStreamClass} instance.
     * </pre>
     * A new handle is generated for the initial time the class descriptor
     * is written into the stream. Future references to the class descriptor
     * are written as references to the initial class descriptor instance.
     *
     * @see java.io.ObjectStreamClass
     */
    private static final ObjectStreamField[] serialPersistentFields =
            new ObjectStreamField[0];


    /**
     * Returns the assertion status that would be assigned to this
     * class if it were to be initialized at the time this method is invoked.
     * If this class has had its assertion status set, the most recent
     * setting will be returned; otherwise, if any package default assertion
     * status pertains to this class, the most recent setting for the most
     * specific pertinent package default assertion status is returned;
     * otherwise, if this class is not a system class (i.e., it has a
     * class loader) its class loader's default assertion status is returned;
     * otherwise, the system class default assertion status is returned.
     * <p>
     * Few programmers will have any need for this method; it is provided
     * for the benefit of the JRE itself.  (It allows a class to determine at
     * the time that it is initialized whether assertions should be enabled.)
     * Note that this method is not guaranteed to return the actual
     * assertion status that was (or will be) associated with the specified
     * class when it was (or will be) initialized.
     *
     * @return the desired assertion status of the specified class.
     * @see java.lang.ClassLoader#setClassAssertionStatus
     * @see java.lang.ClassLoader#setPackageAssertionStatus
     * @see java.lang.ClassLoader#setDefaultAssertionStatus
     * @since 1.4
     */
    public boolean desiredAssertionStatus() {
        ClassLoader loader = getClassLoader();
        // If the loader is null this is a system class, so ask the VM
        if (loader == null)
            return desiredAssertionStatus0(this);

        // If the classloader has been initialized with the assertion
        // directives, ask it. Otherwise, ask the VM.
        synchronized (loader.assertionLock) {
            if (loader.classAssertionStatus != null) {
                return loader.desiredAssertionStatus(getName());
            }
        }
        return desiredAssertionStatus0(this);
    }

    // Retrieves the desired assertion status of this class from the VM
    private static native boolean desiredAssertionStatus0(Class<?> clazz);

    /**
     * Returns true if and only if this class was declared as an enum in the
     * source code.
     *
     * @return true if and only if this class was declared as an enum in the
     * source code
     * @since 1.5
     */
    public boolean isEnum() {
        // An enum must both directly extend java.lang.Enum and have
        // the ENUM bit set; classes for specialized enum constants
        // don't do the former.
        return (this.getModifiers() & ENUM) != 0 &&
                this.getSuperclass() == java.lang.Enum.class;
    }

    // Fetches the factory for reflective objects
    private static ReflectionFactory getReflectionFactory() {
        if (reflectionFactory == null) {
            reflectionFactory =
                    java.security.AccessController.doPrivileged
                            (new sun.reflect.ReflectionFactory.GetReflectionFactoryAction());
        }
        return reflectionFactory;
    }

    private static ReflectionFactory reflectionFactory;

    // To be able to query system properties as soon as they're available
    private static boolean initted = false;

    private static void checkInitted() {
        if (initted) return;
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            public Void run() {
                // Tests to ensure the system properties table is fully
                // initialized. This is needed because reflection code is
                // called very early in the initialization process (before
                // command-line arguments have been parsed and therefore
                // these user-settable properties installed.) We assume that
                // if System.out is non-null then the System class has been
                // fully initialized and that the bulk of the startup code
                // has been run.

                if (System.out == null) {
                    // java.lang.System not yet fully initialized
                    return null;
                }

                // Doesn't use Boolean.getBoolean to avoid class init.
                String val =
                        System.getProperty("sun.reflect.noCaches");
                if (val != null && val.equals("true")) {
                    useCaches = false;
                }

                initted = true;
                return null;
            }
        });
    }

    /**
     * Returns the elements of this enum class or null if this
     * Class object does not represent an enum type.
     *
     * @return an array containing the values comprising the enum class
     * represented by this Class object in the order they're
     * declared, or null if this Class object does not
     * represent an enum type
     * @since 1.5
     */
    public T[] getEnumConstants() {
        T[] values = getEnumConstantsShared();
        return (values != null) ? values.clone() : null;
    }

    /**
     * Returns the elements of this enum class or null if this
     * Class object does not represent an enum type;
     * identical to getEnumConstants except that the result is
     * uncloned, cached, and shared by all callers.
     */
    T[] getEnumConstantsShared() {
        if (enumConstants == null) {
            if (!isEnum()) return null;
            try {
                final Method values = getMethod("values");
                java.security.AccessController.doPrivileged(
                        new java.security.PrivilegedAction<Void>() {
                            public Void run() {
                                values.setAccessible(true);
                                return null;
                            }
                        });
                @SuppressWarnings("unchecked")
                T[] temporaryConstants = (T[]) values.invoke(null);
                enumConstants = temporaryConstants;
            }
            // These can happen when users concoct enum-like classes
            // that don't comply with the enum spec.
            catch (InvocationTargetException | NoSuchMethodException |
                    IllegalAccessException ex) {
                return null;
            }
        }
        return enumConstants;
    }

    private volatile transient T[] enumConstants = null;

    /**
     * Returns a map from simple name to enum constant.  This package-private
     * method is used internally by Enum to implement
     * {@code public static <T extends Enum<T>> T valueOf(Class<T>, String)}
     * efficiently.  Note that the map is returned by this method is
     * created lazily on first use.  Typically it won't ever get created.
     */
    Map<String, T> enumConstantDirectory() {
        if (enumConstantDirectory == null) {
            T[] universe = getEnumConstantsShared();
            if (universe == null)
                throw new IllegalArgumentException(
                        getName() + " is not an enum type");
            Map<String, T> m = new HashMap<>(2 * universe.length);
            for (T constant : universe)
                m.put(((Enum<?>) constant).name(), constant);
            enumConstantDirectory = m;
        }
        return enumConstantDirectory;
    }

    private volatile transient Map<String, T> enumConstantDirectory = null;

    /**
     * Casts an object to the class or interface represented
     * by this {@code Class} object.
     *
     * @param obj the object to be cast
     * @return the object after casting, or null if obj is null
     * @throws ClassCastException if the object is not
     *                            null and is not assignable to the type T.
     * @since 1.5
     */
    @SuppressWarnings("unchecked")
    public T cast(Object obj) {
        if (obj != null && !isInstance(obj))
            throw new ClassCastException(cannotCastMsg(obj));
        return (T) obj;
    }

    private String cannotCastMsg(Object obj) {
        return "Cannot cast " + obj.getClass().getName() + " to " + getName();
    }

    /**
     * Casts this {@code Class} object to represent a subclass of the class
     * represented by the specified class object.  Checks that the cast
     * is valid, and throws a {@code ClassCastException} if it is not.  If
     * this method succeeds, it always returns a reference to this class object.
     *
     * <p>This method is useful when a client needs to "narrow" the type of
     * a {@code Class} object to pass it to an API that restricts the
     * {@code Class} objects that it is willing to accept.  A cast would
     * generate a compile-time warning, as the correctness of the cast
     * could not be checked at runtime (because generic types are implemented
     * by erasure).
     *
     * @param <U>   the type to cast this class object to
     * @param clazz the class of the type to cast this class object to
     * @return this {@code Class} object, cast to represent a subclass of
     * the specified class object.
     * @throws ClassCastException if this {@code Class} object does not
     *                            represent a subclass of the specified class (here "subclass" includes
     *                            the class itself).
     * @since 1.5
     */
    @SuppressWarnings("unchecked")
    public <U> Class<? extends U> asSubclass(Class<U> clazz) {
        if (clazz.isAssignableFrom(this))
            return (Class<? extends U>) this;
        else
            throw new ClassCastException(this.toString());
    }

    /**
     * @throws NullPointerException {@inheritDoc}
     * @since 1.5
     */
    @SuppressWarnings("unchecked")
    public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
        Objects.requireNonNull(annotationClass);

        return (A) annotationData().annotations.get(annotationClass);
    }

    /**
     * {@inheritDoc}
     *
     * @throws NullPointerException {@inheritDoc}
     * @since 1.5
     */
    @Override
    public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
        return GenericDeclaration.super.isAnnotationPresent(annotationClass);
    }

    /**
     * @throws NullPointerException {@inheritDoc}
     * @since 1.8
     */
    @Override
    public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationClass) {
        Objects.requireNonNull(annotationClass);

        AnnotationData annotationData = annotationData();
        return AnnotationSupport.getAssociatedAnnotations(annotationData.declaredAnnotations,
                this,
                annotationClass);
    }

    /**
     * @since 1.5
     */
    public Annotation[] getAnnotations() {
        return AnnotationParser.toArray(annotationData().annotations);
    }

    /**
     * @throws NullPointerException {@inheritDoc}
     * @since 1.8
     */
    @Override
    @SuppressWarnings("unchecked")
    public <A extends Annotation> A getDeclaredAnnotation(Class<A> annotationClass) {
        Objects.requireNonNull(annotationClass);

        return (A) annotationData().declaredAnnotations.get(annotationClass);
    }

    /**
     * @throws NullPointerException {@inheritDoc}
     * @since 1.8
     */
    @Override
    public <A extends Annotation> A[] getDeclaredAnnotationsByType(Class<A> annotationClass) {
        Objects.requireNonNull(annotationClass);

        return AnnotationSupport.getDirectlyAndIndirectlyPresent(annotationData().declaredAnnotations,
                annotationClass);
    }

    /**
     * @since 1.5
     */
    public Annotation[] getDeclaredAnnotations() {
        return AnnotationParser.toArray(annotationData().declaredAnnotations);
    }

    // annotation data that might get invalidated when JVM TI RedefineClasses() is called
    private static class AnnotationData {
        final Map<Class<? extends Annotation>, Annotation> annotations;
        final Map<Class<? extends Annotation>, Annotation> declaredAnnotations;

        // Value of classRedefinedCount when we created this AnnotationData instance
        final int redefinedCount;

        AnnotationData(Map<Class<? extends Annotation>, Annotation> annotations,
                       Map<Class<? extends Annotation>, Annotation> declaredAnnotations,
                       int redefinedCount) {
            this.annotations = annotations;
            this.declaredAnnotations = declaredAnnotations;
            this.redefinedCount = redefinedCount;
        }
    }

    // Annotations cache
    @SuppressWarnings("UnusedDeclaration")
    private volatile transient AnnotationData annotationData;

    private AnnotationData annotationData() {
        while (true) { // retry loop
            AnnotationData annotationData = this.annotationData;
            int classRedefinedCount = this.classRedefinedCount;
            if (annotationData != null &&
                    annotationData.redefinedCount == classRedefinedCount) {
                return annotationData;
            }
            // null or stale annotationData -> optimistically create new instance
            AnnotationData newAnnotationData = createAnnotationData(classRedefinedCount);
            // try to install it
            if (Atomic.casAnnotationData(this, annotationData, newAnnotationData)) {
                // successfully installed new AnnotationData
                return newAnnotationData;
            }
        }
    }

    private AnnotationData createAnnotationData(int classRedefinedCount) {
        Map<Class<? extends Annotation>, Annotation> declaredAnnotations =
                AnnotationParser.parseAnnotations(getRawAnnotations(), getConstantPool(), this);
        Class<?> superClass = getSuperclass();
        Map<Class<? extends Annotation>, Annotation> annotations = null;
        if (superClass != null) {
            Map<Class<? extends Annotation>, Annotation> superAnnotations =
                    superClass.annotationData().annotations;
            for (Map.Entry<Class<? extends Annotation>, Annotation> e : superAnnotations.entrySet()) {
                Class<? extends Annotation> annotationClass = e.getKey();
                if (AnnotationType.getInstance(annotationClass).isInherited()) {
                    if (annotations == null) { // lazy construction
                        annotations = new LinkedHashMap<>((Math.max(
                                declaredAnnotations.size(),
                                Math.min(12, declaredAnnotations.size() + superAnnotations.size())
                        ) * 4 + 2) / 3
                        );
                    }
                    annotations.put(annotationClass, e.getValue());
                }
            }
        }
        if (annotations == null) {
            // no inherited annotations -> share the Map with declaredAnnotations
            annotations = declaredAnnotations;
        } else {
            // at least one inherited annotation -> declared may override inherited
            annotations.putAll(declaredAnnotations);
        }
        return new AnnotationData(annotations, declaredAnnotations, classRedefinedCount);
    }

    // Annotation types cache their internal (AnnotationType) form

    @SuppressWarnings("UnusedDeclaration")
    private volatile transient AnnotationType annotationType;

    boolean casAnnotationType(AnnotationType oldType, AnnotationType newType) {
        return Atomic.casAnnotationType(this, oldType, newType);
    }

    AnnotationType getAnnotationType() {
        return annotationType;
    }

    Map<Class<? extends Annotation>, Annotation> getDeclaredAnnotationMap() {
        return annotationData().declaredAnnotations;
    }

    /* Backing store of user-defined values pertaining to this class.
     * Maintained by the ClassValue class.
     */
    transient ClassValue.ClassValueMap classValueMap;

    /**
     * Returns an {@code AnnotatedType} object that represents the use of a
     * type to specify the superclass of the entity represented by this {@code
     * Class} object. (The <em>use</em> of type Foo to specify the superclass
     * in '...  extends Foo' is distinct from the <em>declaration</em> of type
     * Foo.)
     *
     * <p> If this {@code Class} object represents a type whose declaration
     * does not explicitly indicate an annotated superclass, then the return
     * value is an {@code AnnotatedType} object representing an element with no
     * annotations.
     *
     * <p> If this {@code Class} represents either the {@code Object} class, an
     * interface type, an array type, a primitive type, or void, the return
     * value is {@code null}.
     *
     * @return an object representing the superclass
     * @since 1.8
     */
    public AnnotatedType getAnnotatedSuperclass() {
        if (this == Object.class ||
                isInterface() ||
                isArray() ||
                isPrimitive() ||
                this == Void.TYPE) {
            return null;
        }

        return TypeAnnotationParser.buildAnnotatedSuperclass(getRawTypeAnnotations(), getConstantPool(), this);
    }

    //如果此 类对象表示注释类型，则返回true。 jdk1.5出现的
    public AnnotatedType[] getAnnotatedInterfaces() {
        return TypeAnnotationParser.buildAnnotatedInterfaces(getRawTypeAnnotations(), getConstantPool(), this);
    }
}
