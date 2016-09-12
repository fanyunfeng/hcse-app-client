package com.hcse.app;

public interface ClientEvents<Context extends RunnerContext> {
    void onLanch(Context ctx);

    void onCompleted(Context ctx, boolean emtpy);

    void onFailed(Context ctx);

    void onConnectTimeout(Context ctx);

    void onRequestTimeout(Context ctx);
}
