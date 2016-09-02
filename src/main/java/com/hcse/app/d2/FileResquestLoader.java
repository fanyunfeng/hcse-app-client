package com.hcse.app.d2;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.hcse.protocol.d2.message.D2RequestMessage;
import com.hcse.util.sstring.RequestFactory;
import com.hcse.util.sstring.SearchStringParser;

public class FileResquestLoader extends RequestLoader {
    private String filename;

    public FileResquestLoader(String filename) {
        this.filename = filename;
    }

    @Override
    List<D2RequestMessage> LoadRequest(Map<String, String> shortNameMap, RequestFactory factory) {
        List<D2RequestMessage> ret = new ArrayList<D2RequestMessage>();
        List<Object> list = SearchStringParser.loadRequest(filename, shortNameMap, factory);

        for (Object o : list) {
            ret.add((D2RequestMessage) o);
        }

        filename = null;
        return ret;
    }

}
