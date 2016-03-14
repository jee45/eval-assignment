package edu.umn.cs.recsys.dao;

import it.unimi.dsi.fastutil.longs.LongSet;

import java.util.List;
import java.util.Set;

/**
 * Data access object providing access to item tags.
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public interface ItemTagDAO {
    /**
     * Get the IDs of all tagged items.
     * @return The IDs of all tagged items.
     */
    LongSet getTaggedItems();

    /**
     * Get the tags for a particular item.  A tag can appear multiple times if multiple users have
     * applied it.
     * @param item The item.
     * @return The item's tags.
     */
    List<String> getItemTags(long item);

    /**
     * Get all known tags.
     * @return The set of known tags.
     */
    Set<String> getTagVocabulary();
}
