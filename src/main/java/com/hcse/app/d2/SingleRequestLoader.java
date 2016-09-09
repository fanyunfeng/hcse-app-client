package com.hcse.app.d2;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.hcse.protocol.BaseRequest;
import com.hcse.protocol.d2.message.D2RequestMessage;
import com.hcse.util.Md5Lite;
import com.hcse.util.sstring.RequestFactory;
import com.hcse.util.sstring.SearchStringParser;

public class SingleRequestLoader extends RequestLoader<BaseRequest> {
    private String line;

    public SingleRequestLoader(String line) {
        this.line = line;
    }

    @Override
    public List<? extends BaseRequest> loadRequest(Map<String, String> shortNameMap, RequestFactory<BaseRequest> factory) {
        ArrayList<D2RequestMessage> list = new ArrayList<D2RequestMessage>(1);

        Map<String, String> map = SearchStringParser.parse(line, shortNameMap);

        long tag = Md5Lite.digestLong(line);

        BaseRequest o = SearchStringParser.buildMessage(factory.create(), map);

        if (o != null) {
            o.setTag(tag);
            list.add((D2RequestMessage) o);
        }

        line = null;
        return list;
    }
}
