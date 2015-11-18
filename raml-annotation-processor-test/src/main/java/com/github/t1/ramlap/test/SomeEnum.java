package com.github.t1.ramlap.test;

import io.swagger.annotations.ApiModel;

@ApiModel(value = "some enum", description = "some-enum-descr")
public enum SomeEnum {
    A,
    B;
}
