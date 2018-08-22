package com.travelstart.plugins.exceptions

abstract class PluginException extends RuntimeException {
    String message
    int code
    String body
}