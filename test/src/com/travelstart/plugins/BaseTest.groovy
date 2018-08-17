package com.travelstart.plugins

abstract class BaseTest {
    String packagePath = "test/resources/${getClass().getPackage().getName().replace(".", "/")}"

    def importFile(final String path) {
        final def uri = new File("${packagePath}/${path}").toURI()
        return new File(uri)
    }

}
