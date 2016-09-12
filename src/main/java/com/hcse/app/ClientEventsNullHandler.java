package com.hcse.app;

public class ClientEventsNullHandler<Context extends RunnerContext> implements ClientEvents<Context> {

    @Override
    public void onLanch(RunnerContext ctx) {

    }

    @Override
    public void onCompleted(RunnerContext ctx, boolean emtpy) {

    }

    @Override
    public void onFailed(RunnerContext ctx) {

    }

    @Override
    public void onConnectTimeout(RunnerContext ctx) {

    }

    @Override
    public void onRequestTimeout(RunnerContext ctx) {

    }
}
