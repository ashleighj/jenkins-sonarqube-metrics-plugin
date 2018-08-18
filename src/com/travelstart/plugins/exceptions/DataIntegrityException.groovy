#!/usr/bin/groovy
package com.travelstart.plugins.exceptions

class DataIntegrityException extends RuntimeException {

    final static MESSAGE = "Data received is not well formed or doesn't contain the expected values"

    String rawMessage

    DataIntegrityException(final String rawMessage) {
        super(MESSAGE)
        this.rawMessage = rawMessage
    }
}
