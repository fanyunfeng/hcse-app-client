package com.hcse.app.d2;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.hcse.protocol.d2.message.D2RequestMessage;
import com.hcse.util.sstring.RequestFactory;
import com.hcse.util.sstring.SearchStringParser;

public class SingleRequestLoader extends RequestLoader {
    private String line;

    public SingleRequestLoader(String line) {
        this.line = line;
    }

    @Override
    List<D2RequestMessage> LoadRequest(Map<String, String> shortNameMap, RequestFactory factory) {
        ArrayList<D2RequestMessage> list = new ArrayList<D2RequestMessage>(1);

        Object o = SearchStringParser.buildMessage(line, shortNameMap);

        if (o != null) {
            list.add((D2RequestMessage) o);
        }

        line = null;
        return list;
    }
}
