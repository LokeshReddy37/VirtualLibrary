package com.Lokesh.Book.email;

public enum EmailTemplateName {

    ACTIVATE_ACCOUNT("acctivate_account");

    private final String name;


    EmailTemplateName(String name) {
        this.name = name;
    }
}
