package com.hcse.d6.app;

import com.hcse.d6.protocol.message.D6ResponseMessageClientDoc;
import com.hcse.protocol.util.packet.FieldsMap;

public class ExtClientDoc extends D6ResponseMessageClientDoc {
    public ExtClientDoc(FieldsMap fileMap) {
        super(fileMap);

        FieldsMap extFileMap = (FieldsMap) fileMap.clone();

        extFileMap.addField("XA");
        extFileMap.addField("XB");
        extFileMap.addField("XC");
        extFileMap.addField("XD");

        this.doc.setPrototype(fileMap);
    }
}
