package com.hcse.app.d6.ext;

import com.hcse.protocol.d6.factory.D6ResponseMessageFactory;
import com.hcse.protocol.d6.message.D6ResponseMessageClientDoc;
import com.hcse.protocol.util.packet.FieldsMap;

public class D6ResponseMessageFactory4ExtClient extends D6ResponseMessageFactory {
    public D6ResponseMessageClientDoc createResponseMessageDoc() {
        return new ExtClientDoc(FieldsMap.create(0));
    }
}
