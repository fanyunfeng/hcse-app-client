package com.hcse.app;

import com.hcse.protocol.BaseRequest;

public class CommonClientRunner implements ClientRunner {

    @Override
    public void run(BaseClientConf conf, BaseClient c) {
        CommonClient client = (CommonClient) c;

        while (client.isRunning()) {
            BaseRequest request = client.getRequestQueue().getRequest();

            if (request == null) {
                break;
            }

            client.handleRequest(request);
        }
    }
}
