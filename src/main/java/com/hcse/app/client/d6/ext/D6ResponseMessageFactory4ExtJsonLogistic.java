package com.hcse.app.client.d6.ext;

import com.hcse.d6.protocol.factory.D6ResponseMessageFactory;
import com.hcse.d6.protocol.message.D6ResponseMessageClientDoc;
import com.hcse.protocol.util.packet.FieldsMap;

public class D6ResponseMessageFactory4ExtJsonLogistic extends D6ResponseMessageFactory {
    public D6ResponseMessageClientDoc createResponseMessageDoc() {
        return new ExtClientDoc(FieldsMap.create(1));
    }
}
