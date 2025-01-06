package com.seeyou.mvc.utils;

import com.seeyou.logging.MyLogger;
import com.seeyou.mvc.PageController;
import com.seeyou.mvc.annotations.GetMapping;
import com.seeyou.mvc.annotations.PostMapping;
import com.seeyou.mvc.annotations.Verb;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletRegistration;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Septian Yudhistira
 * @version 1.0
 * @since 2025-01-06
 */
public class InitController {
    private Map<String, Class> clazzByUri;

    public InitController(ServletContextEvent evt, int maxUploadSizeInMb, String uploadLocation) {
        ServletContext context = evt.getServletContext();
        ServletRegistration.Dynamic registration = evt.getServletContext().addServlet("appController", DefaultAppController.class);
        registration.setMultipartConfig(new MultipartConfigElement(uploadLocation, (long)maxUploadSizeInMb, (long)maxUploadSizeInMb, 0));
        registration.addMapping(new String[]{"/"});
        MyLogger.info("Register app controller location upload " + maxUploadSizeInMb + " " + uploadLocation);
        this.clazzByUri = new HashMap();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        String packageName = "";

        DocumentBuilder dBuilder;
        try {
            dBuilder = dbFactory.newDocumentBuilder();
        } catch (ParserConfigurationException var31) {
            MyLogger.error(var31.getMessage(), var31);
            return;
        }

        int i;
        try {
            InputStream is = classLoader.getResourceAsStream("pages.xml");
            Document doc = dBuilder.parse(is);
            NodeList controllerList = doc.getElementsByTagName("package");
            i = 0;

            while(true) {
                if (i >= controllerList.getLength()) {
                    is.close();
                    break;
                }

                Element controllerDescriptor = (Element)controllerList.item(i);
                packageName = controllerDescriptor.getAttribute("name");
                MyLogger.debug("Controller package name :  " + packageName);
                ++i;
            }
        } catch (Exception var35) {
            MyLogger.error(var35.getMessage(), var35);
            return;
        }

        try {
            Class[] classes = Utils.getClasses(packageName);
            Class[] var37 = classes;
            int var38 = classes.length;

            for(i = 0; i < var38; ++i) {
                Class clazz = var37[i];

                try {
                    PageController pageController = (PageController)clazz.newInstance();
                    Method[] var16 = pageController.getClass().getMethods();
                    int var17 = var16.length;

                    for(int var18 = 0; var18 < var17; ++var18) {
                        Method method = var16[var18];

                        try {
                            Annotation[] var20 = method.getAnnotations();
                            int var21 = var20.length;

                            for(int var22 = 0; var22 < var21; ++var22) {
                                Annotation annotation = var20[var22];
                                String[] paths = new String[0];
                                if (annotation instanceof PostMapping) {
                                    paths = ((PostMapping)annotation).value();
                                } else if (annotation instanceof GetMapping) {
                                    paths = ((GetMapping)annotation).value();
                                }

                                Verb httpVerb = (Verb)annotation.annotationType().getAnnotation(Verb.class);
                                String[] var26 = paths;
                                int var27 = paths.length;

                                for(int var28 = 0; var28 < var27; ++var28) {
                                    String path1 = var26[var28];
                                    String normalPath = path1.startsWith("/") ? path1 : "/" + path1;
                                    if (this.clazzByUri.containsKey(normalPath)) {
                                        MyLogger.error("Duplicate path : " + normalPath + " clazz " + pageController);
                                    }

                                    this.clazzByUri.put(normalPath, clazz);
                                    MyLogger.debug("Registered controller Mapped [" + normalPath + "], method=[" + httpVerb.value() + "] with " + clazz);
                                }
                            }
                        } catch (Exception var32) {
                            MyLogger.error(var32.getMessage(), var32);
                        }
                    }
                } catch (Exception var33) {
                }
            }
        } catch (Exception var34) {
        }

        context.setAttribute("clazzByUri", this.clazzByUri);
    }
}
