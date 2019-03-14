package com.groupstp.fns.entity;

import com.haulmont.chile.core.datatypes.impl.EnumClass;

import javax.annotation.Nullable;


public enum TaxpayersType implements EnumClass<String> {

    PERSON("person"),
    ENTERPRENEUR("entrepreneur"),
    LEGAL("legal");

    private String id;

    TaxpayersType(String value) {
        this.id = value;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static TaxpayersType fromId(String id) {
        for (TaxpayersType at : TaxpayersType.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}