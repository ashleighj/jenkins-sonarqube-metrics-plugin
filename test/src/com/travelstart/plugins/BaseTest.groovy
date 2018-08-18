package com.travelstart.plugins

import spock.lang.Specification

abstract class BaseTest extends Specification {
    final String packagePath = "test/resources/${getClass().getPackage().getName().replace(".", "/")}"

    def importFile(final String path) {
        final def uri = new File("${packagePath}/${path}").toURI()
        return new File(uri)
    }

}
