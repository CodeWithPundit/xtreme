// Secrets management for release builds
val secretPropsFile = project.rootProject.file("keystore.properties")
val secretProps = java.util.Properties()

if (secretPropsFile.exists()) {
    secretProps.load(secretPropsFile.inputStream())
} else {
    secretProps.setProperty("KEYSTORE_PASSWORD", System.getenv("KEYSTORE_PASSWORD") ?: "")
    secretProps.setProperty("KEY_ALIAS", System.getenv("KEY_ALIAS") ?: "")
    secretProps.setProperty("KEY_PASSWORD", System.getenv("KEY_PASSWORD") ?: "")
}

ext {
    set("keystorePassword", secretProps.getProperty("KEYSTORE_PASSWORD"))
    set("keyAlias", secretProps.getProperty("KEY_ALIAS"))
    set("keyPassword", secretProps.getProperty("KEY_PASSWORD"))
}
