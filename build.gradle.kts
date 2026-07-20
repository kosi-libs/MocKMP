tasks.register("check") {
    dependsOn(gradle.includedBuild("mockmp").task(":check"))
    dependsOn(gradle.includedBuild("tests-projects").task(":check"))
}
