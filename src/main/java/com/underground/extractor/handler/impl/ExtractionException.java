package com.underground.extractor.handler.impl;

import java.io.Serial;

class ExtractionException extends Exception {
    @Serial
    private static final long serialVersionUID = -5108931481040742838L;

    ExtractionException(String msg) {
        super(msg);
    }

    public ExtractionException(String msg, Exception e) {
        super(msg, e);
    }
}
