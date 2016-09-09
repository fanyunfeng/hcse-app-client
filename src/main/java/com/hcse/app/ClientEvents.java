package com.hcse.app;

public interface ClientEvents {
    void onLanch();

    void onCompleted(boolean emtpy);

    void onFailed();

    void onConnectTimeout();

    void onRequestTimeout();
}
