package com.app.base.http.entity;

/**
 * Created by yuandong on 2017/5/5.
 */

public class BaseEntity<T> {
    public String reason;
    public int error_code;
    public T result;
}
