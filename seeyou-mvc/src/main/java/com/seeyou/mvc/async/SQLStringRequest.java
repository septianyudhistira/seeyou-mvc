package com.seeyou.mvc.async;

import com.seeyou.logging.MyLogger;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @author Septian Yudhistira
 * @version 1.0
 * @since 2024-12-08
 */
public class SQLStringRequest extends HttpServletRequestWrapper {
    private static String[] keywords = new String[] {";", "\"", "'", "/*", "*/", "--", "exec", "select", "update", "delete", "insert", "function", "alter", "drop", "create", "shutdown"};
    private static long attempts = 0L;
    private static Pattern[] patterns = new Pattern[] {Pattern.compile("<script>(.*?)</script>", 2), Pattern.compile("src[\r\n]*=[\r\n]*\\'(.*?)\\'", 42), Pattern.compile("src[\r\n]*=[\r\n]*\\\'(.*?)\\\'", 42), Pattern.compile("</script>", 2), Pattern.compile("<script(.*?)>", 42), Pattern.compile("eval\\((.*?)\\)", 42), Pattern.compile("expression\\((.*?)\\)", 42), Pattern.compile("javascript:", 2), Pattern.compile("vbscript:", 2), Pattern.compile("onload(.*?)=", 42)};
    private HttpServletRequest original;
    private Map safeParameterMap;

    public SQLStringRequest(HttpServletRequest request) {
        super(request);
        this.original = request;
    }

    public String getParameter(String paramName) {
        String[] values = this.getParameterValues(paramName);
        return values != null && values.length > 0 ? values[0] : null;
    }

    public String[] getParameterValues(String paramName) {
        return (String[])this.getParameterMap().get(paramName);
    }

    public Map getParameterMap() {
        if (this.safeParameterMap == null) {
            Map originalParameterMap = this.original.getParameterMap();
            this.safeParameterMap = this.getSafeParameterMap(originalParameterMap);
        }
        return this.safeParameterMap;
    }

    private Map getSafeParameterMap(Map parameterMap) {
        Map newMap = new HashMap();
        Iterator iter = parameterMap.keySet().iterator();

        while (iter.hasNext()) {
            String key = (String)iter.next();
            String[] oldValues = (String[])parameterMap.get(key);
            String[] newValues = new String[oldValues.length];

            for (int i = 0; i < oldValues.length; i++) {
                newValues[i] = this.sqlString(oldValues[i]);
            }

            newMap.put(key, newValues);
        }

        return Collections.unmodifiableMap(newMap);
    }

    private String sqlString(String sqlString) {
        StringBuilder sb = new StringBuilder();
        boolean isSqlInjection = false;

        if (sqlString.indexOf(39) != -1) {
            int length = sqlString.length();

            for (int i = 0; i < length; i++) {
                char c = sqlString.charAt(i);
                if (c == '\'') {
                    isSqlInjection = true;
                    sb.append('\'');
                }

                sb.append(c);
            }
        } else {
            sb.append(sqlString);
        }

        if (isSqlInjection) {
            StringBuffer sb1 = new StringBuffer();
            sb1.append("\nPossible SQL Injection attempt #" + ++attempts + " at " + new Date());
            sb1.append("\nRemote Address: " + this.original.getRemoteAddr());
            sb1.append("\nRemote User: " + this.original.getRemoteUser());
            sb1.append("\nSession Id: " + this.original.getRequestedSessionId());
            sb1.append("\nURI: " + this.original.getContextPath() + this.original.getRequestURI() + "value" + sqlString);
            sb1.append("\nParameters via " + this.original.getMethod());
            MyLogger.securityError(sb1.toString());
        }

        String val = sb.toString();
        return Jsoup.clean(val, Whitelist.basic());
    }

    public String getHeader(String name) {
        String value = super.getHeader(name);
        return this.stripXSS(value);
    }

    private String stripXSS(String value) {
        if (value != null) {
            value = value.replaceAll("\u0000", "");
            Pattern[] var2 = patterns;
            int var3 = var2.length;

            for (int var4 = 0; var4 < var3; var4++) {
                Pattern scriptPattern = var2[var4];
                value = scriptPattern.matcher(value).replaceAll("");
            }
        }
        return value;
    }

    public boolean isMultipartRequest(HttpServletRequest request) {
        return "POST".equalsIgnoreCase(request.getMethod()) && request.getContentType() != null && request.getContentType().toLowerCase().startsWith("multipart/");
    }

}
