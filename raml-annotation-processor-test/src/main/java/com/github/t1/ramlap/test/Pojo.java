package com.github.t1.ramlap.test;

import java.io.Serializable;

public class Pojo implements Serializable {
    private static final long serialVersionUID = 1L;

    private String foo;
    private long bar;

    public String getFoo() {
        return foo;
    }

    public void setFoo(String foo) {
        this.foo = foo;
    }

    public long getBar() {
        return bar;
    }

    public void setBar(long bar) {
        this.bar = bar;
    }
}
