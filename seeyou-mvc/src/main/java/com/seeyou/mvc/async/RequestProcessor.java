package com.seeyou.mvc.async;

import com.seeyou.logging.MyLogger;
import com.seeyou.mvc.PageController;
import com.seeyou.mvc.annotations.GetMapping;
import com.seeyou.mvc.annotations.PostMapping;
import com.seeyou.mvc.annotations.Verb;
import com.seeyou.mvc.models.ModelAndView;
import com.seeyou.mvc.models.RedirectView;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
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
public class RequestProcessor {
    public static final String REQUEST_METHOD_POST = "POST";
    public static final String CONTENT_TYPE_MULTIPART = "multipart/";
    public static String DEFAULT_INDEX_PATH = "/auth/ecommerceindex";
    private static final int maxUploadFile = 2097152;

    public RequestProcessor(HttpServletRequest requestOrign, HttpServletResponse response) {
        try {
            HttpServletRequest request = new SQLStringRequest(requestOrign);
            ServletContext commonData = requestOrign.getServletContext();
            Map<String, Class> clazzByUri = (Map)commonData.getAttribute("clazzByUri");
            String uri = request.getRequestURI().replace(request.getContextPath(), "").split("\\.")[0].trim();
            if (uri.startsWith("/auth")) {
                HttpSession session = request.getSession(false);
                if (session == null) {
                    response.sendRedirect(request.getContextPath() + "/login");
                } else if (uri.startsWith(DEFAULT_INDEX_PATH)) {
                    response.sendRedirect("/index.html");
                    return;
                }
            }

            Class clazz = null;
            Set<String> keys = clazzByUri.keySet();
            Iterator var9 = keys.iterator();

            label158: {
                String path;
                boolean isContainPathParam;
                do {
                    if (!var9.hasNext()) {
                        break label158;
                    }

                    path = (String)var9.next();
                    isContainPathParam = false;
                    if (path.contains("{")) {
                        String normalPath = path.substring(0, path.indexOf("{"));
                        if (uri.startsWith(normalPath)) {
                            isContainPathParam = !uri.replace(normalPath, "").contains("/");
                        }
                    }
                } while(!uri.equals(path) && !isContainPathParam);

                clazz = (Class)clazzByUri.get(path);
            }

            if (clazz == null) {
                response.setStatus(404);

                try {
                    MyLogger.debug("mvc no clazz handler for uri : " + request.getMethod() + " " + uri);
                    this.forwardToView(request, response, "/404.jsp");
                } catch (Exception var28) {
                }
            } else {
                try {
                    PageController pageController = (PageController)clazz.newInstance();
                    Method validMethod = null;
                    Method[] var35 = pageController.getClass().getMethods();
                    int var37 = var35.length;

                    for(int var13 = 0; var13 < var37; ++var13) {
                        Method method = var35[var13];
                        boolean hasHandler = false;
                        Annotation[] var16 = method.getAnnotations();
                        int var17 = var16.length;

                        for(int var18 = 0; var18 < var17; ++var18) {
                            Annotation annotation = var16[var18];
                            String[] paths = new String[0];
                            Verb httpVerb = (Verb)annotation.annotationType().getAnnotation(Verb.class);
                            if (annotation instanceof PostMapping) {
                                paths = ((PostMapping)annotation).value();
                            } else if (annotation instanceof GetMapping) {
                                paths = ((GetMapping)annotation).value();
                            }

                            String[] var22 = paths;
                            int var23 = paths.length;

                            for(int var24 = 0; var24 < var23; ++var24) {
                                String path = var22[var24];
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
                                ModelAndView modelAndView = (ModelAndView)object;
                                Map<String, Object> allObjects = modelAndView.getAllObjects();
                                Iterator var43 = allObjects.keySet().iterator();

                                while(var43.hasNext()) {
                                    String key = (String)var43.next();
                                    request.setAttribute(key, allObjects.get(key));
                                }

                                String pathJsp = modelAndView.getPathJsp();
                                this.forwardToView(request, response, "/WEB-INF/views/" + pathJsp + ".jsp");
                            } else if (object instanceof String) {
                                String textResponse = (String)object;
                                response.getWriter().print(textResponse);
                            } else if (object instanceof RedirectView) {
                                RedirectView redirectView = (RedirectView)object;
                                String redirect = redirectView.getRedirectView();
                                pageController.redirect(response, redirect);
                            } else if (object instanceof byte[]) {
                                byte[] byt = (byte[])object;
                                response.getOutputStream().write(byt);
                            } else if (object != null) {
                                MyLogger.error("not yet implement response type invoke methode for " + object + ", uri : " + uri);
                            }
                        } catch (Exception var29) {
                            throw new ServletException(var29);
                        }
                    }
                } catch (Exception var30) {
                    throw new ServletException(var30);
                }
            }
        } catch (Exception var31) {
        }

    }

    private void forwardToView(ServletRequest request, ServletResponse response, String viewPath) throws Exception {
        request.getRequestDispatcher(viewPath).forward(request, response);
    }
}
