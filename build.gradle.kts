plugins {
    id("maven-publish")
    id("net.fabricmc.fabric-loom") version "1.17.21" apply false
    id("net.fabricmc.fabric-loom-remap") version "1.17.21" apply false
    id("com.replaymod.preprocess") version "c5abb4fb12"
}

preprocess {
    strictExtraMappings.set(false)

    val mc12101 = createNode("1.21.1", 1_21_01, "mojang")
    val mc12103 = createNode("1.21.3", 1_21_03, "mojang")
    val mc12104 = createNode("1.21.4", 1_21_04, "mojang")
    val mc12105 = createNode("1.21.5", 1_21_05, "mojang")
    val mc12106 = createNode("1.21.6", 1_21_06, "mojang")
    val mc12108 = createNode("1.21.8", 1_21_08, "mojang")
    val mc12109 = createNode("1.21.9", 1_21_09, "mojang")
    val mc12111 = createNode("1.21.11", 1_21_11, "mojang")
    val mc260100 = createNode("26.1", 260100, "mojang")
    val mc260200 = createNode("26.2", 260200, "mojang")
    val mc260300 = createNode("26.3", 260300, "mojang")

    mc12101.link(mc12103, null)
    mc12103.link(mc12104, null)
    mc12104.link(mc12105, null)
    mc12105.link(mc12106, null)
    mc12106.link(mc12108, null)
    mc12108.link(mc12109, null)
    mc12109.link(mc12111, null)
    mc12111.link(mc260100, null)
    mc260100.link(mc260200, null)
    mc260200.link(mc260300, null)

    for (node in getNodes()) {
        findProject(node.project)
            ?.ext
            ?.set("mcVersion", node.mcVersion)
    }
}
