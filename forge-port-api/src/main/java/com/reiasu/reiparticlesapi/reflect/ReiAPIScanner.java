package com.reiasu.reiparticlesapi.reflect;

import com.reiasu.reiparticlesapi.ReiParticlesConstants;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Classpath scanner using ClassGraph that discovers annotated classes at runtime.
 * <p>
 * Packages to scan must be registered via {@link #registerPacket(Class)} or
 * {@link #registerPacket(String)} before calling {@link #scan()}.
 */
public final class ReiAPIScanner {

    public static final ReiAPIScanner INSTANCE = new ReiAPIScanner();

    private final HashSet<String> needSearchedPacket = new HashSet<>();
    private boolean loaded = false;
    private ScanResult result;
    private final HashSet<SimpleClassInfo> classes = new HashSet<>();

    private ReiAPIScanner() {
    }

    /**
     * Perform the classpath scan. Only runs once; subsequent calls are no-ops.
     */
    public void scan() {
        if (loaded) {
            return;
        }
        loaded = true;

        long start = System.currentTimeMillis();
        ReiParticlesConstants.logger.info("开始使用ClassGraph扫描...");

        String[] packages = needSearchedPacket.toArray(new String[0]);
        result = new ClassGraph()
                .enableClassInfo()
                .enableAnnotationInfo()
                .acceptPackages(packages)
                .scan();

        ClassInfoList allClasses = result.getAllClasses()
                .filter(ci -> !ci.getAnnotations().isEmpty());

        for (ClassInfo ci : allClasses) {
            String className = ci.getName();
            HashSet<String> annotationNames = ci.getAnnotations().stream()
                    .map(ClassInfo::getName)
                    .collect(Collectors.toCollection(HashSet::new));
            classes.add(new SimpleClassInfo(className, annotationNames));
        }

        long end = System.currentTimeMillis();
        ReiParticlesConstants.logger.info("扫描结果处理完毕 耗时:" + (end - start) + "ms");
    }

    /**
     * Return all scanned classes that carry the given annotation.
     */
    public Collection<SimpleClassInfo> getWithAnnotation(Class<? extends Annotation> anno) {
        List<SimpleClassInfo> out = new ArrayList<>();
        for (SimpleClassInfo info : classes) {
            if (info.isAnnotationPresent(anno)) {
                out.add(info);
            }
        }
        return out;
    }

    /**
     * Return loaded {@link Class} objects for all scanned classes that carry the given annotation.
     */
    public Collection<Class<?>> getClassesWithAnnotation(Class<? extends Annotation> anno) {
        List<Class<?>> out = new ArrayList<>();
        for (SimpleClassInfo info : classes) {
            if (info.isAnnotationPresent(anno)) {
                try {
                    out.add(info.toClass());
                } catch (ClassNotFoundException e) {
                    ReiParticlesConstants.logger.warn("Failed to load class: " + info.getType(), e);
                }
            }
        }
        return out;
    }

    /**
     * Manually add a scan result (used internally during scan, or for testing).
     */
    public void inputScanResult(SimpleClassInfo scanResult) {
        classes.add(scanResult);
    }

    /**
     * Mark the scanner as loaded without performing a scan.
     * Used by NeoForge/Forge mod-scan bootstrapping that provides class info externally.
     */
    public void neoLoaded() {
        loaded = true;
    }

    /**
     * Register a package to scan, derived from the given class's package.
     */
    public static void registerPacket(Class<?> main) {
        String packageName = main.getPackageName();
        registerPacket(packageName);
    }

    /**
     * Register a package name to include in the ClassGraph scan.
     */
    public static void registerPacket(String packageName) {
        INSTANCE.needSearchedPacket.add(packageName);
        ReiParticlesConstants.logger.info("注册事件包: " + packageName);
    }
}
