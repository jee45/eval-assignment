package edu.umn.cs.recsys;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import edu.umn.cs.recsys.dao.ItemTagDAO;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import org.lenskit.inject.Transient;
import org.grouplens.lenskit.vectors.MutableSparseVector;

import javax.inject.Inject;
import java.util.Map;
import java.util.Set;

/**
 * A vocabulary of tags.  This is a recommender component that provides access to the set of
 * tags and makes tag vector operations easier.  It normalizes tags to be case-insensitive.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class TagVocabulary {
    private final Map<String, Long> tagMap;

    @Inject
    public TagVocabulary(@Transient ItemTagDAO tagDAO) {
        long id = 1;
        ImmutableMap.Builder<String,Long> bld = ImmutableMap.builder();
        Set<String> seen = Sets.newHashSet();
        for (String tag: tagDAO.getTagVocabulary()) {
            if (!seen.contains(tag)) {
                seen.add(tag);
                bld.put(tag, id);
                id += 1;
            }
        }
        tagMap = bld.build();
    }

    public Long2DoubleOpenHashMap newTagVector() {
        Long2DoubleOpenHashMap tagvec = new Long2DoubleOpenHashMap();
        for (Long tag_id : tagMap.values()){
            tagvec.put((long)tag_id, 0.0);
        }
        return tagvec;
    }

    /**
     * Query whether the vocabulary has a tag.
     * @param tag The tag to query for.
     * @return {@code true} if the vocabulary has the tag.
     */
    public boolean hasTag(String tag) {
        return tagMap.containsKey(tag.toLowerCase());
    }

    /**
     * Get the ID for a tag.
     * @param tag The tag to query for.
     * @return The id for the tag.
     * @throws IllegalArgumentException if the tag is not found.
     */
    public long getTagId(String tag) {
        Long id = tagMap.get(tag.toLowerCase());
        if (id == null) {
            throw new IllegalArgumentException("tag not found: " + tag);
        } else {
            return id;
        }
    }
}
