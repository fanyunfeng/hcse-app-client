package com.hcse.app.d2;

import java.util.List;

import com.hcse.protocol.d2.message.D2RequestMessage;

public class RequestQueue {
    public void setRequestList(List<D2RequestMessage> list) {

    }

    public D2RequestMessage getRequest() {
        String searchStr = "[S](([TX:TI:MP3]))&([CL:CC:001])";

        D2RequestMessage request = new D2RequestMessage("127.0.0.1", 1, 15, 0, searchStr);

        request.setIndent(false);
        request.setSort(1);

        request.setServiceAddress("index://192.168.46.104:3000");

        return request;
    }
}
