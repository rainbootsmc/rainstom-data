//enableFeaturePreview("VERSION_CATALOGS")

rootProject.name = "rainstom-data"
// DataGenerator
include("DataGenerator")

pluginManagement {
    repositories {
        maven(url = "https://repo.spongepowered.org/repository/maven-public/")
    }
}
