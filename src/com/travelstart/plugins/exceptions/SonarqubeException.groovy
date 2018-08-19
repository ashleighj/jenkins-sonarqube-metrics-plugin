#!/usr/bin/groovy
package com.travelstart.plugins.exceptions

class SonarqubeException extends RuntimeException implements PluginException {
    String message
    int code
    String body
}
