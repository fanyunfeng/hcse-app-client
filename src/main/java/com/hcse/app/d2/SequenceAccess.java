package com.hcse.app.d2;

import com.hcse.protocol.d2.message.D2RequestMessage;

public interface SequenceAccess<Request extends D2RequestMessage> {
    Request getRequest(int id);
}
