package com.groupstp.fns.entity;

import javax.persistence.*;
import java.util.UUID;

import com.haulmont.cuba.core.entity.BaseStringIdEntity;
import com.haulmont.cuba.core.entity.HasUuid;

@Table(name = "FNS_TAXPAYER")
@Entity(name = "fns$Taxpayer")
public class Taxpayer extends BaseStringIdEntity {
    private static final long serialVersionUID = 6431604640104121556L;

    @Id
    @Column(name = "INN", nullable = false, length = 12)
    protected String inn;

    @Column(name = "TYPE_")
    protected String type;

    @Column(name = "DESCRIPTION", length = 1024)
    protected String description;

    @Column(name = "FIO")
    protected String fio;

    @Lob
    @Column(name = "OKVED")
    protected String okved;

    @Lob
    @Column(name = "ADD_OKVED")
    protected String addOkved;

    @Column(name = "ADDRESS", length = 1024)
    protected String address;

    public void setInn(String inn) {
        this.inn = inn;
    }

    public String getInn() {
        return inn;
    }

    @Override
    public String getId() {
        return inn;
    }

    @Override
    public void setId(String id) {
        this.inn = id;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAddress() {
        return address;
    }


    public void setType(TaxpayersType type) {
        this.type = type == null ? null : type.getId();
    }

    public TaxpayersType getType() {
        return type == null ? null : TaxpayersType.fromId(type);
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setFio(String fio) {
        this.fio = fio;
    }

    public String getFio() {
        return fio;
    }

    public void setOkved(String okved) {
        this.okved = okved;
    }

    public String getOkved() {
        return okved;
    }

    public void setAddOkved(String addOkved) {
        this.addOkved = addOkved;
    }

    public String getAddOkved() {
        return addOkved;
    }


}