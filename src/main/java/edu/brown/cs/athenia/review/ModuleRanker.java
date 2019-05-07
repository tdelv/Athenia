package edu.brown.cs.athenia.review;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * ModuleRanker "ranks" a module based on the rating of the module and the date
 * last reviewed. ModuleRanked features the bulk of the ranking algorithm.
 * @author makaylamurphy
 *
 */
public class ModuleRanker {

  // return ordered list of Reviewables
  private List<Reviewable> rankedModules;

  public ModuleRanker() {
  }

  /**
   * Ranks the modules given a list based on rating and dateLastReviewed.
   * @param toRank
   *          : List<Reviewable> of Reviewable Modules to review.
   * @return : ranked list of Reviewable objects.
   */
  public List<Reviewable> rank(List<Reviewable> toRank) {

    rankedModules = new ArrayList<Reviewable>();

    // get separately ranked modules

    List<Reviewable> zerosMods = new ArrayList<Reviewable>();
    List<Reviewable> onesMods = new ArrayList<Reviewable>();
    List<Reviewable> twosMods = new ArrayList<Reviewable>();

    for (Reviewable mod : toRank) {
      if (mod.getRating() == 0) {
        // easy modules
        zerosMods.add(mod);
      } else if (mod.getRating() == 1) {
        // medium modules
        onesMods.add(mod);
      } else if (mod.getRating() == 2) {
        // hard modules
        twosMods.add(mod);
      }
    }

    // sorted trees of each rating
    Map<Date, Reviewable> zerosModsTree = new TreeMap<Date, Reviewable>();
    Map<Date, Reviewable> onesModsTree = new TreeMap<Date, Reviewable>();
    Map<Date, Reviewable> twosModsTree = new TreeMap<Date, Reviewable>();

    for (Reviewable mod : zerosMods) {
      zerosModsTree.put(mod.getDateLastReviewed(), mod);
    }

    for (Reviewable mod : onesMods) {
      onesModsTree.put(mod.getDateLastReviewed(), mod);
    }

    for (Reviewable mod : twosMods) {
      twosModsTree.put(mod.getDateLastReviewed(), mod);
    }

    rankedModules.addAll(twosModsTree.values());
    rankedModules.addAll(onesModsTree.values());
    rankedModules.addAll(zerosModsTree.values());

    // Set<Date> rankedDates
    return rankedModules;
  }
}
