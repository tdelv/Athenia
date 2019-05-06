class ModuleMap {

    static set moduleMap(dummy) {
        console.log("initializeing module map");
        ModuleMap.mm = new Map();
    }

    static get moduleMap() {
        console.log("accessing the module map");
        return ModuleMap.mm;
    }
}