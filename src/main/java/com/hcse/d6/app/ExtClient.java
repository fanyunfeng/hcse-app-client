package com.hcse.d6.app;

import com.hcse.d6.protocol.factory.D6ResponseMessageFactory;

public class ExtClient extends Client {

    protected D6ResponseMessageFactory createFactory() {
        if (version != 3) {
            if (app.equals("base")) {
                return new D6ResponseMessageFactory4ExtClient();
            } else {
                return new D6ResponseMessageFactory4ExtJsonLogistic();
            }
        } else {
            return new D6ResponseMessageFactory4ExtJsonClient();
        }
    }

}
