package edu.brown.cs.athenia.review;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import edu.brown.cs.athenia.data.Language;
import edu.brown.cs.athenia.data.modules.FreeNote;
import edu.brown.cs.athenia.data.modules.Module;
import edu.brown.cs.athenia.data.modules.Tag;
import edu.brown.cs.athenia.data.modules.module.Conjugation;
import edu.brown.cs.athenia.data.modules.module.StorageType;
import edu.brown.cs.athenia.main.Athenia;

/**
 * ReviewMode calls .review() for each of Athenia's modules and sets the ranks
 * by calling ModuleRanker. Uses an algorithm to generate a sorted list of
 * modules. Can filter the result by tag.
 * @author makaylamurphy
 *
 */
public class ReviewMode {

  public ReviewMode(Athenia project, List<Tag> tags, Date startDateCreated,
      Date endDateCreated) {
    Language currLang = project.getCurrLanguage();
    Collection<Module> conjugations = currLang.getModuleMap(StorageType.CONJUGATION)
            .values();
    Collection<Module> freeNotes = currLang.getModuleMap(StorageType.FREE_NOTE)
            .values();

  }

}
