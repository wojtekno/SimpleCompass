package com.nowak.wjw.simplecompass.log;

import org.jetbrains.annotations.NotNull;

import timber.log.Timber;

public class MyDebugTree extends Timber.DebugTree {

    @Override
    protected void log(int priority, String tag, @NotNull String message, Throwable t) {
        String myTag = "MY_TAG_".concat(tag);
        super.log(priority, myTag, message, t);
    }
}
