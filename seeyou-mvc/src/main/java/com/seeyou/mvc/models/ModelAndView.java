package com.seeyou.mvc.models;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Septian Yudhistira
 * @version 1.0
 * @since 2024-12-08
 */
public class ModelAndView {
    private Map<String, Object> map;
    private final String pathJsp;

    public ModelAndView(String pathJsp) {
        this.pathJsp = pathJsp;
        this.map = new HashMap();
    }

    public Map<String, Object> getAllObjects() {
        return this.map;
    }

    public void addObject(String attributeName, Object attributeValue) {
        this.map.put(attributeName, attributeValue);
    }

    public void addAllObjects(Map<String, Object> map) {
        map.putAll(map);
    }

    public Object removeObjects (String attributeName) {
        return this.map.remove(attributeName);
    }

    public void removeAllObjects() {
        this.map.clear();
    }

    public int size() {
        return this.map.size();
    }

    public String getPathJsp() {
        return this.pathJsp;
    }
}
