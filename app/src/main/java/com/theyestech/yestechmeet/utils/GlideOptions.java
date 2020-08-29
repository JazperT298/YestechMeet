package com.theyestech.yestechmeet.utils;

import com.bumptech.glide.request.RequestOptions;

public class GlideOptions {

    public static RequestOptions getOptions(){

        RequestOptions myOption = new RequestOptions();
        myOption.centerInside();
        myOption.circleCrop();

        return myOption;
    }
}
