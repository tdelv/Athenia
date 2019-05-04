package edu.brown.cs.athenia.review;

import java.util.ArrayList;
import java.util.List;

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

    return null;
  }
}
