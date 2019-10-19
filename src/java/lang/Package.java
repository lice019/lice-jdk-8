package java.lang;

import java.lang.reflect.AnnotatedElement;
import java.io.InputStream;
import java.util.Enumeration;

import java.util.StringTokenizer;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;
import java.security.AccessController;
import java.security.PrivilegedAction;

import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.JarException;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import sun.net.www.ParseUtil;
import sun.reflect.CallerSensitive;
import sun.reflect.Reflection;

import java.lang.annotation.Annotation;

/**
 * package的作用就是 c++ 的 namespace的作用，防止名字相同的类产生冲突。Java 编译器在编译时，直接根据package指定的信息直接将生成的class 文件生成到对应目录下。
 * 例如package aaa.bbb.ccc 编译器就将该 .java 文件下的各个类生成到 /aaa/bbb/ccc/ 这个目录下。
 * Java中的一个package(包)就是一个类库单元，包内包含有一组类，它们在单一的名称空间之下被组织在了一起。这个名称空间就是包名。
 */
public class Package implements java.lang.reflect.AnnotatedElement {

    //返回包名称
    public String getName() {
        return pkgName;
    }


    //返回此包实现的规范的标题。
    public String getSpecificationTitle() {
        return specTitle;
    }

    //返回此包实现的规范的版本。
    public String getSpecificationVersion() {
        return specVersion;
    }

    //返回拥有并维护实现此包的类的规范的组织、供应商或公司的名称。
    public String getSpecificationVendor() {
        return specVendor;
    }

    //返回此包的标题。
    public String getImplementationTitle() {
        return implTitle;
    }


    public String getImplementationVersion() {
        return implVersion;
    }


    public String getImplementationVendor() {
        return implVendor;
    }

    //如果这个包是密封的，则返回true。
    public boolean isSealed() {
        return sealBase != null;
    }

    //如果此包根据指定的代码源url进行密封，则返回true。
    public boolean isSealed(URL url) {
        return url.equals(sealBase);
    }

    /**
     * Compare this package's specification version with a
     * desired version. It returns true if
     * this packages specification version number is greater than or equal
     * to the desired version number. <p>
     *
     * Version numbers are compared by sequentially comparing corresponding
     * components of the desired and specification strings.
     * Each component is converted as a decimal integer and the values
     * compared.
     * If the specification value is greater than the desired
     * value true is returned. If the value is less false is returned.
     * If the values are equal the period is skipped and the next pair of
     * components is compared.
     *
     * @param desired the version string of the desired version.
     * @return true if this package's version number is greater
     *          than or equal to the desired version number
     *
     * @exception NumberFormatException if the desired or current version
     *          is not of the correct dotted form.
     */
    public boolean isCompatibleWith(String desired)
        throws NumberFormatException
    {
        if (specVersion == null || specVersion.length() < 1) {
            throw new NumberFormatException("Empty version string");
        }

        String [] sa = specVersion.split("\\.", -1);
        int [] si = new int[sa.length];
        for (int i = 0; i < sa.length; i++) {
            si[i] = Integer.parseInt(sa[i]);
            if (si[i] < 0)
                throw NumberFormatException.forInputString("" + si[i]);
        }

        String [] da = desired.split("\\.", -1);
        int [] di = new int[da.length];
        for (int i = 0; i < da.length; i++) {
            di[i] = Integer.parseInt(da[i]);
            if (di[i] < 0)
                throw NumberFormatException.forInputString("" + di[i]);
        }

        int len = Math.max(di.length, si.length);
        for (int i = 0; i < len; i++) {
            int d = (i < di.length ? di[i] : 0);
            int s = (i < si.length ? si[i] : 0);
            if (s < d)
                return false;
            if (s > d)
                return true;
        }
        return true;
    }

    //通过名称来获取包对象
    @CallerSensitive
    public static Package getPackage(String name) {
        ClassLoader l = ClassLoader.getClassLoader(Reflection.getCallerClass());
        if (l != null) {
            return l.getPackage(name);
        } else {
            return getSystemPackage(name);
        }
    }

    //获取目前为呼叫者的 ClassLoader实例所知道的所有软件包。
    @CallerSensitive
    public static Package[] getPackages() {
        ClassLoader l = ClassLoader.getClassLoader(Reflection.getCallerClass());
        if (l != null) {
            return l.getPackages();
        } else {
            return getSystemPackages();
        }
    }

    //获取指定类的包。
    static Package getPackage(Class<?> c) {
        String name = c.getName();
        int i = name.lastIndexOf('.');
        if (i != -1) {
            name = name.substring(0, i);
            ClassLoader cl = c.getClassLoader();
            if (cl != null) {
                return cl.getPackage(name);
            } else {
                return getSystemPackage(name);
            }
        } else {
            return null;
        }
    }

    /**
     * Return the hash code computed from the package name.
     * @return the hash code computed from the package name.
     */
    public int hashCode(){
        return pkgName.hashCode();
    }

    /**
     * Returns the string representation of this Package.
     * Its value is the string "package " and the package name.
     * If the package title is defined it is appended.
     * If the package version is defined it is appended.
     * @return the string representation of the package.
     */
    public String toString() {
        String spec = specTitle;
        String ver =  specVersion;
        if (spec != null && spec.length() > 0)
            spec = ", " + spec;
        else
            spec = "";
        if (ver != null && ver.length() > 0)
            ver = ", version " + ver;
        else
            ver = "";
        return "package " + pkgName + spec + ver;
    }

    private Class<?> getPackageInfo() {
        if (packageInfo == null) {
            try {
                packageInfo = Class.forName(pkgName + ".package-info", false, loader);
            } catch (ClassNotFoundException ex) {
                // store a proxy for the package info that has no annotations
                class PackageInfoProxy {}
                packageInfo = PackageInfoProxy.class;
            }
        }
        return packageInfo;
    }

    /**
     * @throws NullPointerException {@inheritDoc}
     * @since 1.5
     */
    public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
        return getPackageInfo().getAnnotation(annotationClass);
    }

    /**
     * {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     * @since 1.5
     */
    @Override
    public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
        return AnnotatedElement.super.isAnnotationPresent(annotationClass);
    }

    /**
     * @throws NullPointerException {@inheritDoc}
     * @since 1.8
     */
    @Override
    public  <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationClass) {
        return getPackageInfo().getAnnotationsByType(annotationClass);
    }

    /**
     * @since 1.5
     */
    public Annotation[] getAnnotations() {
        return getPackageInfo().getAnnotations();
    }

    /**
     * @throws NullPointerException {@inheritDoc}
     * @since 1.8
     */
    @Override
    public <A extends Annotation> A getDeclaredAnnotation(Class<A> annotationClass) {
        return getPackageInfo().getDeclaredAnnotation(annotationClass);
    }

    /**
     * @throws NullPointerException {@inheritDoc}
     * @since 1.8
     */
    @Override
    public <A extends Annotation> A[] getDeclaredAnnotationsByType(Class<A> annotationClass) {
        return getPackageInfo().getDeclaredAnnotationsByType(annotationClass);
    }

    /**
     * @since 1.5
     */
    public Annotation[] getDeclaredAnnotations()  {
        return getPackageInfo().getDeclaredAnnotations();
    }

    /**
     * Construct a package instance with the specified version
     * information.
     * @param name the name of the package
     * @param spectitle the title of the specification
     * @param specversion the version of the specification
     * @param specvendor the organization that maintains the specification
     * @param impltitle the title of the implementation
     * @param implversion the version of the implementation
     * @param implvendor the organization that maintains the implementation
     */
    Package(String name,
            String spectitle, String specversion, String specvendor,
            String impltitle, String implversion, String implvendor,
            URL sealbase, ClassLoader loader)
    {
        pkgName = name;
        implTitle = impltitle;
        implVersion = implversion;
        implVendor = implvendor;
        specTitle = spectitle;
        specVersion = specversion;
        specVendor = specvendor;
        sealBase = sealbase;
        this.loader = loader;
    }

    /*
     * Construct a package using the attributes from the specified manifest.
     *
     * @param name the package name
     * @param man the optional manifest for the package
     * @param url the optional code source url for the package
     */
    private Package(String name, Manifest man, URL url, ClassLoader loader) {
        String path = name.replace('.', '/').concat("/");
        String sealed = null;
        String specTitle= null;
        String specVersion= null;
        String specVendor= null;
        String implTitle= null;
        String implVersion= null;
        String implVendor= null;
        URL sealBase= null;
        Attributes attr = man.getAttributes(path);
        if (attr != null) {
            specTitle   = attr.getValue(Name.SPECIFICATION_TITLE);
            specVersion = attr.getValue(Name.SPECIFICATION_VERSION);
            specVendor  = attr.getValue(Name.SPECIFICATION_VENDOR);
            implTitle   = attr.getValue(Name.IMPLEMENTATION_TITLE);
            implVersion = attr.getValue(Name.IMPLEMENTATION_VERSION);
            implVendor  = attr.getValue(Name.IMPLEMENTATION_VENDOR);
            sealed      = attr.getValue(Name.SEALED);
        }
        attr = man.getMainAttributes();
        if (attr != null) {
            if (specTitle == null) {
                specTitle = attr.getValue(Name.SPECIFICATION_TITLE);
            }
            if (specVersion == null) {
                specVersion = attr.getValue(Name.SPECIFICATION_VERSION);
            }
            if (specVendor == null) {
                specVendor = attr.getValue(Name.SPECIFICATION_VENDOR);
            }
            if (implTitle == null) {
                implTitle = attr.getValue(Name.IMPLEMENTATION_TITLE);
            }
            if (implVersion == null) {
                implVersion = attr.getValue(Name.IMPLEMENTATION_VERSION);
            }
            if (implVendor == null) {
                implVendor = attr.getValue(Name.IMPLEMENTATION_VENDOR);
            }
            if (sealed == null) {
                sealed = attr.getValue(Name.SEALED);
            }
        }
        if ("true".equalsIgnoreCase(sealed)) {
            sealBase = url;
        }
        pkgName = name;
        this.specTitle = specTitle;
        this.specVersion = specVersion;
        this.specVendor = specVendor;
        this.implTitle = implTitle;
        this.implVersion = implVersion;
        this.implVendor = implVendor;
        this.sealBase = sealBase;
        this.loader = loader;
    }

    /*
     * Returns the loaded system package for the specified name.
     */
    static Package getSystemPackage(String name) {
        synchronized (pkgs) {
            Package pkg = pkgs.get(name);
            if (pkg == null) {
                name = name.replace('.', '/').concat("/");
                String fn = getSystemPackage0(name);
                if (fn != null) {
                    pkg = defineSystemPackage(name, fn);
                }
            }
            return pkg;
        }
    }

    /*
     * Return an array of loaded system packages.
     */
    static Package[] getSystemPackages() {
        // First, update the system package map with new package names
        String[] names = getSystemPackages0();
        synchronized (pkgs) {
            for (int i = 0; i < names.length; i++) {
                defineSystemPackage(names[i], getSystemPackage0(names[i]));
            }
            return pkgs.values().toArray(new Package[pkgs.size()]);
        }
    }

    private static Package defineSystemPackage(final String iname,
                                               final String fn)
    {
        return AccessController.doPrivileged(new PrivilegedAction<Package>() {
            public Package run() {
                String name = iname;
                // Get the cached code source url for the file name
                URL url = urls.get(fn);
                if (url == null) {
                    // URL not found, so create one
                    File file = new File(fn);
                    try {
                        url = ParseUtil.fileToEncodedURL(file);
                    } catch (MalformedURLException e) {
                    }
                    if (url != null) {
                        urls.put(fn, url);
                        // If loading a JAR file, then also cache the manifest
                        if (file.isFile()) {
                            mans.put(fn, loadManifest(fn));
                        }
                    }
                }
                // Convert to "."-separated package name
                name = name.substring(0, name.length() - 1).replace('/', '.');
                Package pkg;
                Manifest man = mans.get(fn);
                if (man != null) {
                    pkg = new Package(name, man, url, null);
                } else {
                    pkg = new Package(name, null, null, null,
                                      null, null, null, null, null);
                }
                pkgs.put(name, pkg);
                return pkg;
            }
        });
    }

    /*
     * Returns the Manifest for the specified JAR file name.
     */
    private static Manifest loadManifest(String fn) {
        try (FileInputStream fis = new FileInputStream(fn);
             JarInputStream jis = new JarInputStream(fis, false))
        {
            return jis.getManifest();
        } catch (IOException e) {
            return null;
        }
    }

    // The map of loaded system packages
    private static Map<String, Package> pkgs = new HashMap<>(31);

    // Maps each directory or zip file name to its corresponding url
    private static Map<String, URL> urls = new HashMap<>(10);

    // Maps each code source url for a jar file to its manifest
    private static Map<String, Manifest> mans = new HashMap<>(10);

    private static native String getSystemPackage0(String name);
    private static native String[] getSystemPackages0();

    /*
     * Private storage for the package name and attributes.
     */
    private final String pkgName;
    private final String specTitle;
    private final String specVersion;
    private final String specVendor;
    private final String implTitle;
    private final String implVersion;
    private final String implVendor;
    private final URL sealBase;
    private transient final ClassLoader loader;
    private transient Class<?> packageInfo;
}
