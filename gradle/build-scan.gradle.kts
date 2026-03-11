// Build scan configuration for CI/CD
if (hasProperty("buildScan")) {
    buildScan {
        termsOfServiceUrl.set("https://gradle.com/terms-of-service")
        termsOfServiceAgree.set("yes")
        
        if (System.getenv("CI") == "true") {
            publishAlways()
            tag("CI")
        }
    }
}
