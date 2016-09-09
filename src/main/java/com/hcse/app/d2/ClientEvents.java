package com.hcse.app.d2;

public interface ClientEvents {
    void onLanch();

    void onCompleted(boolean emtpy);

    void onFailed();

    void onConnectTimeout();

    void onRequestTimeout();
}
