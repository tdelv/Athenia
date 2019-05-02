package edu.brown.cs.athenia.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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

  public List<Module> getModules() {
    List<Module> modules = new ArrayList<Module>();
    for (Module m : ((Map<String, Module>) moduleMap.values()).values()) {
      modules.add(m);
    }
    return modules;
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
  public List<Module> getModuleListFromTag(Tag tag) {
    List<Module> modules = new ArrayList<Module>();
    for (Module m : ((Map<String, Module>) moduleMap.values()).values()) {
      if (m.hasTag(tag.getTag())) {
        modules.add(m);
      }
    }
    return modules;

  }

}
