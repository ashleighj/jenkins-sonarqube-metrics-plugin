#!/usr/bin/groovy
package com.travelstart.plugins.exceptions

class DataIntegrityException extends PluginException {

    String rawMessage

    DataIntegrityException(final String rawMessage) {
        super()
        this.rawMessage = rawMessage
        this.message = rawMessage
    }
}
