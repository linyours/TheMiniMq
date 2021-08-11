package com.linyous.mqtt.spi;

import com.linyous.mqtt.spi.annotation.MiniSPI;
import com.linyous.mqtt.spi.wrapper.MiniHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;

/**
 * 一个接口对应一个MiniExtensionLoader
 * @author Linyous
 * @date 2021/6/21 10:14
 */
public class MiniExtensionLoader<T> {
    private static final Logger logger = LoggerFactory.getLogger(MiniExtensionLoader.class);

    private static final String MINI_DIRECTORY = "META-INF/mini/";

    //缓存接口和MiniExtensionLoader的映射关系
    private static final ConcurrentMap<Class<?>, MiniExtensionLoader<?>> EXTENSION_LOADERS = new ConcurrentHashMap<Class<?>, MiniExtensionLoader<?>>();

    //该正则表达式用来分隔扩展点名，因为扩展点文件中可能有以下形式
    //car1,car2=com.example.CarImpl
    private static final Pattern NAME_SEPARATOR = Pattern.compile("\\s*[,]+\\s*");

    //存储接口
    private final Class<?> type;

    //存储默认扩展点名
    private String cachedDefaultName;

    //缓存扩展点名和扩展点类对象的映射关系
    private final MiniHolder<Map<String, Class<?>>> cachedClasses = new MiniHolder<Map<String, Class<?>>>();

    //扩展点类对象和缓存扩展点名的映射关系
    private final ConcurrentMap<Class<?>, String> cachedNames = new ConcurrentHashMap<Class<?>, String>();

    //存储了扩展点名和对应对象实例的映射关系
    private final ConcurrentMap<String, MiniHolder<Object>> cachedInstances = new ConcurrentHashMap<String, MiniHolder<Object>>();

    //类对象和实例对象的映射关系
    private static final ConcurrentMap<Class<?>, Object> EXTENSION_INSTANCES = new ConcurrentHashMap<Class<?>, Object>();

    private MiniExtensionLoader(Class<?> type) {
        this.type = type;
    }

    public static <T> MiniExtensionLoader<T> getExtensionLoader(Class<T> type) {
        if (type == null)
            //非空判断
            throw new IllegalArgumentException("Extension type == null");
        if (!type.isInterface()) {
            //传入的不是一个接口
            throw new IllegalArgumentException("Extension type(" + type + ") is not interface!");
        }
        if (!withExtensionAnnotation(type)) {
            //传入的接口没有MiniSPI的注解
            throw new IllegalArgumentException("Extension type(" + type +
                    ") is not extension, because WITHOUT @" + MiniSPI.class.getSimpleName() + " Annotation!");
        }
        //去缓存中拿取MiniExtensionLoader
        MiniExtensionLoader<T> loader = (MiniExtensionLoader<T>) EXTENSION_LOADERS.get(type);
        if (loader == null) {
            //如果传入key对应的value已经存在，就返回存在的value，不进行替换。如果不存在，就添加key和value，返回null
            //使用这个方法替代put，是为了防止多线程下出问题
            EXTENSION_LOADERS.putIfAbsent(type, new MiniExtensionLoader<T>(type));
            loader = (MiniExtensionLoader<T>) EXTENSION_LOADERS.get(type);
        }
        return loader;
    }

    /**
     * 拿到扩展点
     * @param name
     * @return
     */
    public T getExtension(String name) {
        //判断扩展点名是否合法
        if (name == null || name.length() == 0)
            throw new IllegalArgumentException("Extension name == null");
        //如果是扩展名是true，返回默认扩展点
        if ("true".equals(name)) {
            return getDefaultExtension();
        }
        //取到扩展点类对应实例的包装类
        MiniHolder<Object> holder = cachedInstances.get(name);
        if (holder == null) {
            //包装类都没有，就先创建一个包装类，这里不会有多线程问题putIfAbsent如果有value就会直接返回value
            //先创建包装类就是为了加锁，可以把锁加到包装类上，而不用加到map上，效率更加高
            cachedInstances.putIfAbsent(name, new MiniHolder<Object>());
            holder = cachedInstances.get(name);
        }
        //获取包装类包装的对象实例
        Object instance = holder.get();
        if (instance == null) {
            //加锁
            synchronized (holder) {
                instance = holder.get();
                if (instance == null) {
                    //创建实例
                    instance = createExtension(name);
                    holder.set(instance);
                }
            }
        }
        return (T) instance;
    }

    /**
     * 创建扩展点
     * @param name
     * @return
     */
    private T createExtension(String name) {
        //获得类对象
        Class<?> clazz = getExtensionClasses().get(name);
        if (clazz == null) {
            throw new IllegalStateException("can not get clazz");
        }
        try {
            T instance = (T) EXTENSION_INSTANCES.get(clazz);
            if (instance == null) {
                //反射生成扩展点，并加入缓存
                EXTENSION_INSTANCES.putIfAbsent(clazz, clazz.newInstance());
                instance = (T) EXTENSION_INSTANCES.get(clazz);
            }
//            //依赖注入
//            injectExtension(instance);
            //AOP对象
//            Set<Class<?>> wrapperClasses = cachedWrapperClasses;
//            if (wrapperClasses != null && !wrapperClasses.isEmpty()) {
//                for (Class<?> wrapperClass : wrapperClasses) {
//                    instance = injectExtension((T) wrapperClass.getConstructor(type).newInstance(instance));
//                }
//            }
            return instance;
        } catch (Throwable t) {
            throw new IllegalStateException("Extension instance(name: " + name + ", class: " +
                    type + ")  could not be instantiated: " + t.getMessage(), t);
        }
    }

    /**
     * 获取默认扩展点
     * @return
     */
    public T getDefaultExtension() {
        getExtensionClasses();
        if (null == cachedDefaultName || cachedDefaultName.length() == 0
                || "true".equals(cachedDefaultName)) {
            return null;
        }
        return getExtension(cachedDefaultName);
    }

    /**
     * 获得所有的扩展点名和扩展点对象类的map，同时带解析所有扩展点对象类的功能
     * @return
     */
    private Map<String, Class<?>> getExtensionClasses() {
        //由于缓存被包装了一层，所以要先拿出被包装缓存
        //至于为什么要加这个包装？主要是为了加锁
        Map<String, Class<?>> classes = cachedClasses.get();
        //双重检测锁
        if (classes == null) {
            synchronized (cachedClasses) {
                classes = cachedClasses.get();
                if (classes == null) {
                    //加载所有该接口的扩展点对象类
                    classes = loadExtensionClasses();
                    cachedClasses.set(classes);
                }
            }
        }
        return classes;
    }

    /**
     * 加载所有该接口的扩展点对象类
     * @return
     */
    private Map<String, Class<?>> loadExtensionClasses() {
        //获得接口上的MiniSPI
        final MiniSPI defaultAnnotation = type.getAnnotation(MiniSPI.class);
        if (defaultAnnotation != null) {
            // 拿到MiniSPI注解中的Value，该value可以为默认的扩展点
            String value = defaultAnnotation.value();
            if ((value = value.trim()).length() > 0) {
                String[] names = NAME_SEPARATOR.split(value);
                if (names.length > 1) {
                    throw new IllegalStateException("more than 1 default extension name on extension " + type.getName()
                            + ": " + Arrays.toString(names));
                }
                if (names.length == 1) cachedDefaultName = names[0];
            }
        }
        Map<String, Class<?>> extensionClasses = new HashMap<String, Class<?>>();
        loadDirectory(extensionClasses, MINI_DIRECTORY);
        return extensionClasses;
    }

    /**
     * 加载扩展点文件
     * @param extensionClasses
     * @param dir
     */
    private void loadDirectory(Map<String, Class<?>> extensionClasses, String dir) {
        //组合文件名
        String fileName = dir + type.getName();
        try {
            Enumeration<URL> urls;
            //拿到类加载器
            ClassLoader classLoader = findClassLoader();
            if (classLoader != null) {
                urls = classLoader.getResources(fileName);
            } else {
                urls = ClassLoader.getSystemResources(fileName);
            }
            if (urls != null) {
                while (urls.hasMoreElements()) {
                    java.net.URL resourceURL = urls.nextElement();
                    loadResource(extensionClasses, classLoader, resourceURL);
                }
            }
        } catch (Throwable t) {
            logger.error("Exception when load extension class(interface: " +
                    type + ", description file: " + fileName + ").", t);
        }
    }

    /**
     * 解析文件
     * @param extensionClasses
     * @param classLoader
     * @param resourceURL
     */
    private void loadResource(Map<String, Class<?>> extensionClasses, ClassLoader classLoader, java.net.URL resourceURL) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(resourceURL.openStream(), "utf-8"));
            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    //以#号开头的为注释，拿到#号之前的内容
                    final int ci = line.indexOf('#');
                    if (ci >= 0) line = line.substring(0, ci);
                    line = line.trim();
                    if (line.length() > 0) {
                        try {
                            String name = null;
                            //分隔等号之前和等号之后的字符串
                            int i = line.indexOf('=');
                            if (i > 0) {
                                name = line.substring(0, i).trim();
                                line = line.substring(i + 1).trim();
                            }
                            if (line.length() > 0) {
                                //根据字符串加载类
                                loadClass(extensionClasses, resourceURL, Class.forName(line, true, classLoader), name);
                            }
                        } catch (Throwable t) {
                            logger.error("Failed to load extension class(interface: " + type + ", class line: " + line + ") in " + resourceURL + ", cause: " + t.getMessage(), t);
                        }
                    }
                }
            } finally {
                reader.close();
            }
        } catch (Throwable t) {
            logger.error("Exception when load extension class(interface: " +
                    type + ", class file: " + resourceURL + ") in " + resourceURL, t);
        }
    }

    /**
     * 根据字符串加载类
     * @param extensionClasses
     * @param resourceURL
     * @param clazz
     * @param name
     * @throws NoSuchMethodException
     */
    private void loadClass(Map<String, Class<?>> extensionClasses, java.net.URL resourceURL, Class<?> clazz, String name) throws NoSuchMethodException {
        //判断加载到类是不是实现了接口
        if (!type.isAssignableFrom(clazz)) {
            throw new IllegalStateException("Error when load extension class(interface: " +
                    type + ", class line: " + clazz.getName() + "), class "
                    + clazz.getName() + "is not subtype of interface.");
        }
//        if (clazz.isAnnotationPresent(Adaptive.class)) {
//            if (cachedAdaptiveClass == null) {
//                cachedAdaptiveClass = clazz;
//            } else if (!cachedAdaptiveClass.equals(clazz)) {
//                throw new IllegalStateException("More than 1 adaptive class found: "
//                        + cachedAdaptiveClass.getClass().getName()
//                        + ", " + clazz.getClass().getName());
//            }
//        } else if (isWrapperClass(clazz)) {
//            Set<Class<?>> wrappers = cachedWrapperClasses;
//            if (wrappers == null) {
//                cachedWrapperClasses = new ConcurrentHashSet<Class<?>>();
//                wrappers = cachedWrapperClasses;
//            }
//            wrappers.add(clazz);
//        } else {
             //拿到构造器
            clazz.getConstructor();
            //处理没有name的情况
//            if (name == null || name.length() == 0) {
//                name = findAnnotationName(clazz);
//                if (name == null || name.length() == 0) {
//                    if (clazz.getSimpleName().length() > type.getSimpleName().length()
//                            && clazz.getSimpleName().endsWith(type.getSimpleName())) {
//                        name = clazz.getSimpleName().substring(0, clazz.getSimpleName().length() - type.getSimpleName().length()).toLowerCase();
//                    } else {
//                        throw new IllegalStateException("No such extension name for the class " + clazz.getName() + " in the config " + resourceURL);
//                    }
//                }
//            }
            String[] names = NAME_SEPARATOR.split(name);
            if (names != null && names.length > 0) {
//                Activate activate = clazz.getAnnotation(Activate.class);
//                if (activate != null) {
//                    cachedActivates.put(names[0], activate);
//                }
                for (String n : names) {
                    if (!cachedNames.containsKey(clazz)) {
                        cachedNames.put(clazz, n);
                    }
                    Class<?> c = extensionClasses.get(n);
                    if (c == null) {
                        extensionClasses.put(n, clazz);
                    } else if (c != clazz) {
                        throw new IllegalStateException("Duplicate extension " + type.getName() + " name " + n + " on " + c.getName() + " and " + clazz.getName());
                    }
                }
            }
//        }
    }

    /**
     * 返回MiniExtensionLoader的类加载器
     * @return
     */
    private static ClassLoader findClassLoader() {
        return MiniExtensionLoader.class.getClassLoader();
    }

    /**
     * 该方法用来判断接口是否包含MiniSPI注解
     * @param type 接口
     * @param <T>
     * @return
     */
    private static <T> boolean withExtensionAnnotation(Class<T> type) {
        return type.isAnnotationPresent(MiniSPI.class);
    }
}
