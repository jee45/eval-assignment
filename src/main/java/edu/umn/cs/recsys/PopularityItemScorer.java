package edu.umn.cs.recsys;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import org.grouplens.grapht.annotation.DefaultProvider;
import org.lenskit.api.Result;
import org.lenskit.api.ResultMap;
import org.lenskit.basic.AbstractItemScorer;
import org.lenskit.inject.Shareable;
import org.lenskit.inject.Transient;
import org.lenskit.data.dao.EventDAO;
import org.lenskit.data.dao.ItemDAO;
import org.lenskit.data.events.Event;
import org.lenskit.results.Results;
import org.lenskit.util.io.ObjectStream;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Provider;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Score items by popularity (rating count).
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@Shareable
@DefaultProvider(PopularityItemScorer.Builder.class)
public class PopularityItemScorer extends AbstractItemScorer implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Long2DoubleMap itemPopularity;

    private PopularityItemScorer(Long2DoubleMap pops) {
        itemPopularity = pops;
    }

    @Override
    public ResultMap scoreWithDetails(long user, @Nonnull Collection<Long> items) {

        List<Result> results = new ArrayList<>();

        for (long item: items) {
            results.add(Results.create(item, itemPopularity.getOrDefault(item, 0.0)));
        }
        return Results.newResultMap(results);
    }

    public static class Builder implements Provider<PopularityItemScorer> {
        private final EventDAO eventDAO;
        private final ItemDAO itemDAO;

        @Inject
        public Builder(@Transient EventDAO edao, @Transient ItemDAO idao) {
            eventDAO = edao;
            itemDAO = idao;
        }

        @Override
        public PopularityItemScorer get() {
            Long2DoubleOpenHashMap vec = new Long2DoubleOpenHashMap();
            ObjectStream<Event> stream = eventDAO.streamEvents();
            try {
                for (Event e: stream) {
                    vec.addTo(e.getItemId(), 1);
                }
            } finally {
                stream.close();
            }
            return new PopularityItemScorer(vec);
        }
    }
}
