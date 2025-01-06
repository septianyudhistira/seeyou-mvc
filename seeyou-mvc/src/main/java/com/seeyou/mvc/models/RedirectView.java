package com.seeyou.mvc.models;

/**
 * @author Septian Yudhistira
 * @version 1.0
 * @since 2024-12-08
 */
public class RedirectView {
    private final String redirectView;

    public RedirectView(String redirectView) {
        this.redirectView = redirectView;
    }

    public String getRedirectView() {
        return this.redirectView;
    }
}
