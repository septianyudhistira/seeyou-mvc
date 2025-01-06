package com.seeyou.mvc.async;

import com.seeyou.logging.MyLogger;
import com.seeyou.mvc.PageController;
import com.seeyou.mvc.annotations.GetMapping;
import com.seeyou.mvc.annotations.PostMapping;
import com.seeyou.mvc.annotations.Verb;
import com.seeyou.mvc.models.ModelAndView;
import com.seeyou.mvc.models.RedirectView;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @author Septian Yudhistira
 * @version 1.0
 * @since 2024-12-08
 */
public class AsyncRequestProcessor implements Runnable {
    
    public static final String REQUEST_METHOD_POST = "POST";
    public static final String CONTENT_TYPE_MULTIPART = "multipart/";
    private static final String commercePath = "auth/ecommerceindex";
    private static final int maxUploadFile = 2097152;
    private final AsyncContext asyncContext;

    public AsyncRequestProcessor(AsyncContext asyncContext) {
        this.asyncContext = asyncContext;
    }

    @Override
    public void run() {
        try {
            ServletRequest servletRequest = this.asyncContext.getRequest();
            HttpServletRequest requestOrigin = (HttpServletRequest)servletRequest;

            ServletResponse servletResponse = this.asyncContext.getResponse();
            HttpServletResponse response = (HttpServletResponse)servletResponse;
            
            if (requestOrigin.getRequestURI().endsWith(".php")) {
                response.sendRedirect("/error");
                this.asyncContext.complete();
                return;
            }

            ServletContext commonData = servletRequest.getServletContext();
            HttpServletRequest request = new SQLStringRequest(requestOrigin);
            Map<String, Class> clazzByUri = (Map)commonData.getAttribute("clazzByUri");
            String uri = request.getRequestURI().replace(request.getContextPath(), "").split("\\.")[0].trim();

            if (uri.startsWith("/auth")) {
                HttpSession session = request.getSession(false);
                if (session == null) {
                    response.sendRedirect(request.getContextPath() + "/login");
                } else if (uri.startsWith("/auth/ecommerceindex")) {
                    response.sendRedirect("/index.html");
                    this.asyncContext.complete();
                    return;
                }
            }

            Class clazz = null;
            Set<String> keys = clazzByUri.keySet();
            Iterator var11 = keys.iterator();

            while (var11.hasNext()) {
                String path = (String) var11.next();
                boolean isContainPathParam = false;
                if (path.contains("{")) {
                    String normalPath = path.substring(0, path.indexOf("{"));

                    if (uri.startsWith(normalPath)) {
                        isContainPathParam = !uri.replace(normalPath, "").contains("/");
                    }
                }

                if (uri.equals(path) || isContainPathParam) {
                    clazz = (Class) clazzByUri.get(path);
                    break;
                }
            }

            if (clazz == null) {
                response.setStatus(404);

                try {
                    MyLogger.debug("mvc no clazz handler for uri : " + request.getMethod() + " " + uri);
                    this.forwardToView(request, response, "/404.jsp");
                } catch (Exception var30) {
                }
            } else {
                try {
                    PageController pageController = (PageController) clazz.newInstance();
                    Method validMethod = null;
                    Method[] var37 = pageController.getClass().getMethods();
                    int var39 = var37.length;

                    for (int var15 = 0; var15 < var39; ++var15) {
                        Method method = var37[var15];
                        boolean hasHandler = false;
                        Annotation[] var18 = method.getAnnotations();
                        int var19 = var18.length;

                        for (int var20 = 0; var20 < var19; ++var20) {
                            Annotation annotation = var18[var20];
                            String[] paths = new String[0];
                            Verb httpVerb = (Verb)annotation.annotationType().getAnnotation(Verb.class);

                            if (annotation instanceof PostMapping) {
                                paths = ((PostMapping)annotation).value();
                            } else if (annotation instanceof GetMapping) {
                                paths = ((GetMapping)annotation).value();
                            }

                            String[] var24 = paths;
                            int var25 = paths.length;

                            for (int var26 = 0; var26 < var25; ++var26) {
                                String path = var24[var26];
                                String normalPath = path.startsWith("/") ? path : "/" + path;
                                boolean isStartWithParam = false;

                                if (normalPath.contains("{")) {
                                    normalPath = normalPath.substring(0, normalPath.indexOf("{"));
                                    isStartWithParam = !uri.replace(normalPath, "").contains("/");
                                }

                                if (httpVerb != null && httpVerb.value().equals(request.getMethod()) && uri.equals(normalPath) || isStartWithParam) {
                                    hasHandler = true;
                                    break;
                                }
                            }

                            if (hasHandler) {
                                break;
                            }
                        }

                        if (hasHandler) {
                            validMethod = method;
                            break;
                        }
                    }

                    if (validMethod != null) {
                        try {
                            Object object = validMethod.invoke(pageController, request, response);
                            if (object instanceof ModelAndView) {
                                ModelAndView modelAndView = (ModelAndView) object;
                                Map<String, Object> allObjects = modelAndView.getAllObjects();
                                Iterator var45 = allObjects.keySet().iterator();

                                while (var45.hasNext()) {
                                    String key = (String) var45.next();
                                    servletRequest.setAttribute(key, allObjects.get(key));
                                }

                                String pathJsp = modelAndView.getPathJsp();
                                this.forwardToView(servletRequest, servletResponse, "/WEB-INF/views/" + pathJsp + ".jsp");
                            } else if (object instanceof String) {
                                response.getWriter().print((String) object);
                            } else if (object instanceof String) {
                                RedirectView redirectView = (RedirectView) object;
                                String redirect = redirectView.getRedirectView();
                                pageController.redirect(response, redirect);
                            } else if (object instanceof byte[]) {
                                byte[] byt = (byte[]) object;
                                response.getOutputStream().write(byt);
                            } else if (object != null) {
                                MyLogger.error("not yet implement response type invoke methode for " + object + ", uri : " + uri);
                            }
                        } catch (Exception var31) {
                            throw new ServletException(var31);
                        }
                    }
                } catch (Exception var32) {
                    throw new ServletException(var32);
                }
            }
        } catch (Exception var33) {
        }

        this.asyncContext.complete();
    }

    private void forwardToView(ServletRequest request, ServletResponse response, String viewPath) {
        try {
            request.getRequestDispatcher(viewPath).forward(request, response);
        } catch (ServletException | IOException var5){
        }
    }
}
