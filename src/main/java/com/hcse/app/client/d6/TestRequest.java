package com.hcse.app.client.d6;

import java.util.ArrayList;
import java.util.List;

public class TestRequest {
    private List<TestRequestItem> requestItemList;

    public TestRequest(List<TestRequestItem> list) {
        this.requestItemList = list;
    }

    public List<TestRequestItem> getRequestItemList() {
        return requestItemList;
    }

    public void setRequestItemList(List<TestRequestItem> requestItemList) {
        this.requestItemList = requestItemList;
    }

    public int size() {
        return requestItemList.size();
    }

    public static TestRequest createSimple(int mid, String md5Lite) {
        ArrayList<TestRequestItem> list = new ArrayList<TestRequestItem>(1);

        list.add(new TestRequestItem(mid, md5Lite));

        return new TestRequest(list);
    }
}
