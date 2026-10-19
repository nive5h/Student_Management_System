package com.sms.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * Application Lifecycle Listener that dynamically initializes container attributes
 * such as the JSP Jasper InstanceManager and TldCache when running on standalone runners like Jetty Runner.
 */
@WebListener
public class AppContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext ctx = sce.getServletContext();
        try {
            // 0. Initialize JspFactory default implementation if null
            if (javax.servlet.jsp.JspFactory.getDefaultFactory() == null) {
                try {
                    Class<?> jspFactoryImpl = Class.forName("org.apache.jasper.runtime.JspFactoryImpl");
                    javax.servlet.jsp.JspFactory.setDefaultFactory((javax.servlet.jsp.JspFactory) jspFactoryImpl.newInstance());
                    LoggerUtil.info("Initialized JspFactory default factory.");
                } catch (Throwable t) {
                    System.err.println("Could not init JspFactory: " + t.getMessage());
                }
            }
            // 1. Initialize InstanceManager for Jasper if running under Jetty Runner
            if (ctx.getAttribute("org.apache.tomcat.InstanceManager") == null) {
                try {
                    Class<?> simClass = Class.forName("org.apache.tomcat.SimpleInstanceManager");
                    ctx.setAttribute("org.apache.tomcat.InstanceManager", simClass.newInstance());
                    LoggerUtil.info("Initialized SimpleInstanceManager for JSP engine.");
                } catch (ClassNotFoundException ignored) {
                }
            }

            // 2. Initialize TldCache for Jasper JSTL tag scanning if running under Jetty Runner
            String tldAttr = "org.apache.jasper.compiler.TldCache";
            if (ctx.getAttribute(tldAttr) == null) {
                try {
                    Class<?> scannerClass = Class.forName("org.apache.jasper.servlet.TldScanner");
                    Constructor<?> scanCtor = scannerClass.getConstructor(ServletContext.class, boolean.class, boolean.class, boolean.class);
                    Object scanner = scanCtor.newInstance(ctx, true, true, true);

                    Method scanMethod = scannerClass.getMethod("scan");
                    scanMethod.invoke(scanner);

                    Method getUriMap = scannerClass.getMethod("getUriTldResourcePathMap");
                    Map<?, ?> uriMap = (Map<?, ?>) getUriMap.invoke(scanner);

                    Method getTaglibMap = scannerClass.getMethod("getTldResourcePathTaglibXmlMap");
                    Map<?, ?> taglibMap = (Map<?, ?>) getTaglibMap.invoke(scanner);

                    Class<?> cacheClass = Class.forName("org.apache.jasper.compiler.TldCache");
                    Constructor<?> cacheCtor = cacheClass.getConstructor(ServletContext.class, Map.class, Map.class);
                    Object cache = cacheCtor.newInstance(ctx, uriMap, taglibMap);

                    ctx.setAttribute(tldAttr, cache);
                    LoggerUtil.info("Initialized TldCache dynamically with " + uriMap.size() + " TLDs.");
                } catch (ClassNotFoundException ignored) {
                }
            }
        } catch (Throwable t) {
            System.err.println("AppContextListener ERROR:");
            t.printStackTrace(System.err);
            LoggerUtil.warning("Context initialization notice: " + t.getMessage());
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }
}
