package com.travelstart.plugins.exceptions

class SonarqubeException extends RuntimeException implements Serializable{
    String message
    int code
    String body
}
