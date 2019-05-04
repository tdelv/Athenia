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

  public List<Reviewable> rank(List<Reviewable> toRank) {

    rankedModules = new ArrayList<Reviewable>();
    Map<Date, Reviewable> modTree = new TreeMap<Date, Reviewable>();

    for (Reviewable mod : toRank) {
      modTree.put(mod.getDateLastReviewed(), mod);
    }

    // Set<Date> rankedDates
    return null;
  }
}
