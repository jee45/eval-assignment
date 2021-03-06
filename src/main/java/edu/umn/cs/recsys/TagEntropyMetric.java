package edu.umn.cs.recsys;

import com.google.common.collect.Sets;
import edu.umn.cs.recsys.dao.ItemTagDAO;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
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
import java.util.*;

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

        //make a list for tag probabilities
        Long2DoubleMap tagProbabilitiesList = new Long2DoubleOpenHashMap();


        //Double runningProbabilityTotalForTHisTag
        Double runningProbabilityTotalForThisTag = 0.0;

        //for each movie in the recommendations list
        for (Result movie: recommendations) {


            //get the list of tags for this movie
            //List<String> tagListForThisMovie = tagDAO.getItemTags(movie.getId());
            List<String> tagListForThisMovie = tagDAO.getItemTags(movie.getId());

            Set<String> tagSetForThisMovie = new HashSet<>(tagListForThisMovie);
            //List<String> tagsAlreadySeenInThisMovie =  new ArrayList<String>();

            //for each tag in the tag list
            for (String tag : tagSetForThisMovie) {
                //ignore frequency.
                //if we have not seen this tag for this movie before,
                //if(!tagsAlreadySeenInThisMovie.contains(tag)) {

                //it has now been seen
                //tagsAlreadySeenInThisMovie.add(tag);

                Long tagId = vocab.getTagId(tag);
                runningProbabilityTotalForThisTag = 0.0;


                //if the tag is in the list for tag probailities,
                if (tagProbabilitiesList.containsKey(tagId)) {

                    //runningProbabilityTotalForTHisTag = the stored probablity  for this tag
                    runningProbabilityTotalForThisTag = tagProbabilitiesList.get(tagId);
                }

                //add to runningProbabilityTotalForTHisTag ((1/movieCountInRecomndationList)(1/totalTagCountForThisMovie)) //////// is this right?
                runningProbabilityTotalForThisTag += ((1.0 / recommendations.size()) * (1.0 / tagSetForThisMovie.size()));



                //store the  new runningProbabilityTotalForTHisTag in the list for tag probabilities
                tagProbabilitiesList.put(tagId, runningProbabilityTotalForThisTag);



                //}
            }

        }

        //for each tag in the list for tag probabilities
        for (Long tagId: tagProbabilitiesList.keySet()) {
            //runningProbabilityTotalForTHisTag = the stored probablity for this tag

            runningProbabilityTotalForThisTag = tagProbabilitiesList.get(tagId);


            //entropy -= (runningProbabilityTotalForTHisTag)* logBase2(runningProbabilityTotalForTHisTag)
            //entropy -= (runningProbabilityTotalForThisTag)* (Math.log(runningProbabilityTotalForThisTag));

            entropy -= (runningProbabilityTotalForThisTag)* (Math.log(runningProbabilityTotalForThisTag)/(Math.log(2)));


        }



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
