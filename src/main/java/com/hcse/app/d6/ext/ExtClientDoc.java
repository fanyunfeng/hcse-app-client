package com.hcse.app.d6.ext;

import com.hcse.app.ExtClient;
import com.hcse.protocol.d6.message.D6ResponseMessageClientDoc;
import com.hcse.protocol.util.packet.FieldsMap;

public class ExtClientDoc extends D6ResponseMessageClientDoc {
    public ExtClientDoc(FieldsMap fileMap) {
        super(fileMap);

        String[] extFieldName = ExtClient.getInstance().getExtFieldName();

        if (extFieldName == null) {
            return;
        }

        FieldsMap extFileMap = (FieldsMap) fileMap.clone();

        for (String i : extFieldName) {
            extFileMap.addField(i);
        }

        // extFileMap.addField("XA");
        // extFileMap.addField("XB");
        // extFileMap.addField("XC");
        // extFileMap.addField("XD");
        this.doc.setPrototype(extFileMap);
    }
}
