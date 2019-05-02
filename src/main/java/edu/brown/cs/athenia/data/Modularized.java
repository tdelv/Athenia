package edu.brown.cs.athenia.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import edu.brown.cs.athenia.data.modules.Module;
import edu.brown.cs.athenia.data.modules.Tag;
import edu.brown.cs.athenia.data.modules.module.StorageType;

public abstract class Modularized {

  private Map<StorageType, Map<String, Module>> moduleMap;

  public Modularized() {

    this.moduleMap = new HashMap<>();
    for (StorageType type : StorageType.values()) {
      this.moduleMap.put(type, new HashMap<>());
    }
  }

  public Map<String, Module> getModuleMap(StorageType type) {
    return Collections.unmodifiableMap(moduleMap.get(type));
  }

  public Module getModule(StorageType type, String id) {
    return moduleMap.get(type).get(id);
  }

  public int getModuleCount(StorageType type) {
    return moduleMap.get(type).size();
  }

  // TODO : how does this work? i have actually no idea how to make an instant
  // of any module yet... (jason)
  public void addModule(StorageType type, Module module) {
    moduleMap.get(type).put(module.getId(), module);
  }

  // TODO : somehow pull the list of modules listed under a specific tag --
  // jason DONE
  // i guess this would be a lot of database stuff?
  // or some means of storing the modules in the tag object itself
  public Map<StorageType, Map<String, Module>> getModuleListFromTag(Tag tag) {
    // what will be returned
    Map<StorageType, Map<String, Module>> returnModuleMap = new HashMap<StorageType, Map<String, Module>>();
    // for every type
    for (StorageType type : StorageType.values()) {
      // module map that starts with all same modules
      Map<String, Module> typedModules = moduleMap.get(type);
      // for every module
      for (Module m : typedModules.values()) {
        // if doesn't have tag remove from Map of returnable modules
        if (!m.hasTag(tag.getTag())) {
          typedModules.remove(m.getId());
        }
      }
      // put every map of typed modules in with types map
      returnModuleMap.put(type, typedModules);
    }
    return returnModuleMap;
  }

}
