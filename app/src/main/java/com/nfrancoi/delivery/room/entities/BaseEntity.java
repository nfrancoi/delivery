package com.nfrancoi.delivery.room.entities;

public interface BaseEntity {

    Long getId() ;
    void setActive(boolean isActive);
    boolean isActive();
}
