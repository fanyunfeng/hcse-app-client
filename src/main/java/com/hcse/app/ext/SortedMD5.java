package com.hcse.app.ext;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.hcse.protocol.BaseResponse;
import com.hcse.protocol.BaseResponseDoc;
import com.hcse.protocol.handler.ResponseHandler;

public class SortedMD5 implements ResponseHandler {

    @Override
    public void reset() {

    }

    @Override
    public void process(BaseResponse res) {
        List<BaseResponseDoc> list = (List<BaseResponseDoc>) res.getDocs();

        Collections.sort(list, new Comparator<BaseResponseDoc>() {
            @Override
            public int compare(BaseResponseDoc o1, BaseResponseDoc o2) {
                if (o1.getWeight() == o2.getWeight()) {
                    if (o1.getMd5Lite() < o2.getMd5Lite()) {
                        return -1;
                    } else if (o1.getMd5Lite() == o2.getMd5Lite()) {
                        return 0;
                    } else {
                        return 1;
                    }
                } else if (o1.getWeight() < o2.getWeight()) {
                    return -1;
                } else {
                    return 1;
                }
            }
        });
    }
}
