
package java.lang.reflect;

import java.security.AccessController;

import sun.reflect.Reflection;
import sun.reflect.ReflectionFactory;

import java.lang.annotation.Annotation;

/**
 * AccessibleObject类是Field，Method和Constructor对象的基类。 它提供了将反射对象标记为在使用它时抑制默认Java语言访问控制检查的功能。
 * 当使用Fields，Methods或Constructors来设置或获取字段，调用方法，或创建和初始化新的类实例时，执行访问检查（对于public，默认（包）访问，受保护和私有成员）
 */
public class AccessibleObject implements AnnotatedElement {

    //用于检查客户端是否存在的权限对象
    //具有足够的权限来击败Java语言访问控制检查。
    static final private java.security.Permission ACCESS_PERMISSION =
            new ReflectPermission("suppressAccessChecks");

    //方便的方法来设置 accessible标志的一系列对象的安全检查（为了效率）。
    public static void setAccessible(AccessibleObject[] array, boolean flag)
            throws SecurityException {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkPermission(ACCESS_PERMISSION);
        for (int i = 0; i < array.length; i++) {
            setAccessible0(array[i], flag);
        }
    }

    //将此对象的 accessible标志设置为指示的布尔值。
    //开放权限，什么修饰符的变量、方法、构造器都可以拿到
    public void setAccessible(boolean flag) throws SecurityException {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkPermission(ACCESS_PERMISSION);
        setAccessible0(this, flag);
    }

    /* Check that you aren't exposing java.lang.Class.<init> or sensitive
       fields in java.lang.Class. */
    //检查是否没有公开java.lang.Class。或java.lang.Class中的敏感字段。
    private static void setAccessible0(AccessibleObject obj, boolean flag)
            throws SecurityException {
        if (obj instanceof Constructor && flag == true) {
            Constructor<?> c = (Constructor<?>) obj;
            if (c.getDeclaringClass() == Class.class) {
                throw new SecurityException("Cannot make a java.lang.Class" +
                        " constructor accessible");
            }
        }
        obj.override = flag;
    }

    //获取此对象的 accessible标志的值。
    //如果可访问，为true；不能访问则为false
    public boolean isAccessible() {
        return override;
    }

    /**
     * 构造函数:仅供Java虚拟机使用。
     */
    protected AccessibleObject() {
    }

    //指示此对象是否覆盖语言级访问检查。初始化“false”。该字段由字段、方法和构造函数使用。
    //注意:出于安全目的，此字段不能在此包之外可见。
    boolean override;

    //子类用于创建字段、方法和构造函数访问器的反射工厂。注意，这在引导过程的早期就被调用了。
    static final ReflectionFactory reflectionFactory =
            AccessController.doPrivileged(
                    new sun.reflect.ReflectionFactory.GetReflectionFactoryAction());

    //返回与此元素相关 联的注释 。
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        throw new AssertionError("All subclasses should override this method");
    }

    //如果此元素上 存在指定类型的注释，则返回true，否则返回false。
    @Override
    public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
        return AnnotatedElement.super.isAnnotationPresent(annotationClass);
    }

    /**
     * 如果此类注释 直接存在或 间接存在，则返回该元素的注释（指定类型）。
     * @throws NullPointerException {@inheritDoc}
     * @since 1.8
     */
    @Override
    public <T extends Annotation> T[] getAnnotationsByType(Class<T> annotationClass) {
        throw new AssertionError("All subclasses should override this method");
    }

    /**
     * 返回 直接存在于此元素上的注释。
     * @since 1.5
     */
    public Annotation[] getAnnotations() {
        return getDeclaredAnnotations();
    }

    /**
     * @throws NullPointerException {@inheritDoc}
     * @since 1.8
     */
    @Override
    public <T extends Annotation> T getDeclaredAnnotation(Class<T> annotationClass) {
        // Only annotations on classes are inherited, for all other
        // objects getDeclaredAnnotation is the same as
        // getAnnotation.
        return getAnnotation(annotationClass);
    }

    /**
     * @throws NullPointerException {@inheritDoc}
     * @since 1.8
     */
    @Override
    public <T extends Annotation> T[] getDeclaredAnnotationsByType(Class<T> annotationClass) {
        // Only annotations on classes are inherited, for all other
        // objects getDeclaredAnnotationsByType is the same as
        // getAnnotationsByType.
        return getAnnotationsByType(annotationClass);
    }

    /**
     * @since 1.5
     */
    public Annotation[] getDeclaredAnnotations() {
        throw new AssertionError("All subclasses should override this method");
    }


    // Shared access checking logic.

    // For non-public members or members in package-private classes,
    // it is necessary to perform somewhat expensive security checks.
    // If the security check succeeds for a given class, it will
    // always succeed (it is not affected by the granting or revoking
    // of permissions); we speed up the check in the common case by
    // remembering the last Class for which the check succeeded.
    //
    // The simple security check for Constructor is to see if
    // the caller has already been seen, verified, and cached.
    // (See also Class.newInstance(), which uses a similar method.)
    //
    // A more complicated security check cache is needed for Method and Field
    // The cache can be either null (empty cache), a 2-array of {caller,target},
    // or a caller (with target implicitly equal to this.clazz).
    // In the 2-array case, the target is always different from the clazz.
    volatile Object securityCheckCache;

    void checkAccess(Class<?> caller, Class<?> clazz, Object obj, int modifiers)
            throws IllegalAccessException {
        if (caller == clazz) {  // quick check
            return;             // ACCESS IS OK
        }
        Object cache = securityCheckCache;  // read volatile
        Class<?> targetClass = clazz;
        if (obj != null
                && Modifier.isProtected(modifiers)
                && ((targetClass = obj.getClass()) != clazz)) {
            // Must match a 2-list of { caller, targetClass }.
            if (cache instanceof Class[]) {
                Class<?>[] cache2 = (Class<?>[]) cache;
                if (cache2[1] == targetClass &&
                        cache2[0] == caller) {
                    return;     // ACCESS IS OK
                }
                // (Test cache[1] first since range check for [1]
                // subsumes range check for [0].)
            }
        } else if (cache == caller) {
            // Non-protected case (or obj.class == this.clazz).
            return;             // ACCESS IS OK
        }

        // If no return, fall through to the slow path.
        slowCheckMemberAccess(caller, clazz, obj, modifiers, targetClass);
    }

    // Keep all this slow stuff out of line:
    void slowCheckMemberAccess(Class<?> caller, Class<?> clazz, Object obj, int modifiers,
                               Class<?> targetClass)
            throws IllegalAccessException {
        Reflection.ensureMemberAccess(caller, clazz, obj, modifiers);

        // Success: Update the cache.
        Object cache = ((targetClass == clazz)
                ? caller
                : new Class<?>[]{caller, targetClass});

        // Note:  The two cache elements are not volatile,
        // but they are effectively final.  The Java memory model
        // guarantees that the initializing stores for the cache
        // elements will occur before the volatile write.
        securityCheckCache = cache;         // write volatile
    }
}
