package com.travelstart.plugins.exceptions

class GithubException extends RuntimeException implements PluginException {
    String message
    int code
    String body
}
