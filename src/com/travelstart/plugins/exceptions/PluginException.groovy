package com.travelstart.plugins.exceptions

interface PluginException {
    String getMessage()
    void setMessage(final String message)

    int getCode()
    void setCode(final int code)

    String getBody()
    void setBody(final String body)
}