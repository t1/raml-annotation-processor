package com.github.t1.ramlap.test;

import java.io.Serializable;

import com.github.t1.ramlap.ApiExample;

public class Pojo implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiExample("foo-example")
    private String foo;

    @ApiExample("987")
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
