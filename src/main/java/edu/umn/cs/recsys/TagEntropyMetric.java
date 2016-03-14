package edu.umn.cs.recsys;

import com.google.common.collect.Sets;
import edu.umn.cs.recsys.dao.ItemTagDAO;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import org.lenskit.LenskitRecommender;
import org.lenskit.api.Recommender;
import org.lenskit.api.Result;
import org.lenskit.api.ResultList;
import org.lenskit.eval.traintest.AlgorithmInstance;
import org.lenskit.eval.traintest.DataSet;
import org.lenskit.eval.traintest.TestUser;
import org.lenskit.eval.traintest.metrics.MetricColumn;
import org.lenskit.eval.traintest.metrics.MetricResult;
import org.lenskit.eval.traintest.metrics.TypedMetricResult;
import org.lenskit.eval.traintest.recommend.TopNMetric;
import org.lenskit.util.math.Vectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;

/**
 * Metric that measures how long a TopN list actually is.
 */
public class TagEntropyMetric extends TopNMetric<TagEntropyMetric.Context> {
    private static final Logger logger = LoggerFactory.getLogger(TagEntropyMetric.class);

    /**
     * Construct a new tag entropy metric metric.
     */
    public TagEntropyMetric() {
        super(TagEntropyResult.class, TagEntropyResult.class);
    }

    @Nonnull
    @Override
    public MetricResult measureUser(TestUser user, ResultList recommendations, Context context) {
        int n = recommendations.size();


        if (recommendations == null || recommendations.isEmpty()) {
            return MetricResult.empty();
            // no results for this user.
        }
        // get tag data from the context so we can use it
        ItemTagDAO tagDAO = context.getItemTagDAO();
        TagVocabulary vocab = context.getTagVocabulary();

        double entropy = 0;

        // TODO Implement the entropy metric

        // record the entropy in the context for aggregation
        context.addUser(entropy);

        return new TagEntropyResult(entropy);
    }

    @Nullable
    @Override
    public Context createContext(AlgorithmInstance algorithm, DataSet dataSet, Recommender recommender) {
        return new Context((LenskitRecommender) recommender);
    }

    @Nonnull
    @Override
    public MetricResult getAggregateMeasurements(Context context) {
        return new TagEntropyResult(context.getMeanEntropy());
    }

    public static class TagEntropyResult extends TypedMetricResult {
        @MetricColumn("TopN.TagEntropy")
        public final double entropy;

        public TagEntropyResult(double ent) {
            entropy = ent;
        }

    }

    public static class Context {
        private LenskitRecommender recommender;
        private double totalEntropy;
        private int userCount;

        /**
         * Create a new context for evaluating a particular recommender.
         *
         * @param rec The recommender being evaluated.
         */
        public Context(LenskitRecommender rec) {
            recommender = rec;
        }

        /**
         * Get the recommender being evaluated.
         *
         * @return The recommender being evaluated.
         */
        public LenskitRecommender getRecommender() {
            return recommender;
        }

        /**
         * Get the item tag DAO for this evaluation context.
         *
         * @return A DAO providing access to the tag lists of items.
         */
        public ItemTagDAO getItemTagDAO() {
            return recommender.get(ItemTagDAO.class);
        }

        /**
         * Get the tag vocabulary for the current recommender evaluation.
         *
         * @return The tag vocabulary for this evaluation context.
         */
        public TagVocabulary getTagVocabulary() {
            return recommender.get(TagVocabulary.class);
        }

        /**
         * Add the entropy for a user to this context.
         *
         * @param entropy The entropy for one user.
         */
        public void addUser(double entropy) {
            totalEntropy += entropy;
            userCount += 1;
        }

        /**
         * Get the average entropy over all users.
         *
         * @return The average entropy over all users.
         */
        public double getMeanEntropy() {
            return totalEntropy / userCount;
        }
    }
}
