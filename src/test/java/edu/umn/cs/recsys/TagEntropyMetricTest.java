package edu.umn.cs.recsys;

import com.google.common.collect.Lists;
import edu.umn.cs.recsys.dao.CSVItemTagDAO;
import edu.umn.cs.recsys.dao.ItemTagDAO;
import edu.umn.cs.recsys.dao.TagFile;
import org.grouplens.lenskit.data.text.EventFile;
import org.grouplens.lenskit.data.text.Formats;
import org.grouplens.lenskit.data.text.TextEventDAO;
import org.junit.Before;
import org.junit.Test;
import org.lenskit.LenskitConfiguration;
import org.lenskit.LenskitRecommender;
import org.lenskit.api.ItemScorer;
import org.lenskit.api.RecommenderBuildException;
import org.lenskit.api.Result;
import org.lenskit.api.ResultList;
import org.lenskit.baseline.ItemMeanRatingItemScorer;
import org.lenskit.data.events.Event;
import org.lenskit.data.history.History;
import org.lenskit.data.history.UserHistory;
import org.lenskit.eval.traintest.TestUser;
import org.lenskit.results.Results;

import java.io.File;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeTrue;

public class TagEntropyMetricTest {
    File tagFile = new File("data/movie-tags.csv");
    File ratingsFile = new File("data/ratings.csv");
    TagEntropyMetric metric;
    LenskitRecommender recommender;

    @Before
    public void createMetric() throws RecommenderBuildException {
        assumeTrue("movie tag file available", tagFile.exists());
        assumeTrue("ratings file available", ratingsFile.exists());
        metric = new TagEntropyMetric();

        // build a recommender
        LenskitConfiguration config = new LenskitConfiguration();
        config.addRoot(TagVocabulary.class);
        config.addRoot(ItemTagDAO.class);
        config.addComponent(CSVItemTagDAO.class);
        config.set(TagFile.class).to(tagFile);
        config.addComponent(TextEventDAO.class);
        config.set(EventFile.class).to(ratingsFile);
        config.addComponent(Formats.csvRatings());
        config.bind(ItemScorer.class).to(ItemMeanRatingItemScorer.class);
        recommender = LenskitRecommender.build(config);
    }

    @Test
    public void testLifecycle() {
        TagEntropyMetric.Context ctx = metric.createContext(null, null, recommender);
        assertThat(ctx, not(nullValue()));
        TagEntropyMetric.TagEntropyResult result = (TagEntropyMetric.TagEntropyResult) metric.getAggregateMeasurements(ctx);
        assertThat(result, not(nullValue()));
    }

    /**
     * Test the measurement of a single user with a single movie.
     */
    @Test
    public void testSimpleUser() {
        TagEntropyMetric.Context ctx = metric.createContext(null, null, recommender);
        assertThat(ctx, not(nullValue()));


        // movie 812 has 50 tags
        TestUser tu = createUser(42);
        ResultList recs = createRecommendations(812);

        TagEntropyMetric.TagEntropyResult result = (TagEntropyMetric.TagEntropyResult) metric.measureUser(tu, recs, ctx);
        assertThat(result, not(nullValue()));

        double expected = movieTagEntropy(50);

        assertThat(result.entropy, closeTo(expected, 0.0001));

        TagEntropyMetric.TagEntropyResult aggResult = (TagEntropyMetric.TagEntropyResult) metric.getAggregateMeasurements(ctx);
        assertThat(aggResult, not(nullValue()));
        // average of 1 user's entropy is that entropy
        assertThat(aggResult.entropy, closeTo(expected, 0.0001));
    }

    /**
     * Test the aggregation of two users, each with a single movie.
     */
    @Test
    public void testTwoUsers() {
        TagEntropyMetric.Context ctx = metric.createContext(null, null, recommender);
        assertThat(ctx, not(nullValue()));

        // movie 812 has 50 tags
        TestUser tu = createUser(42);
        ResultList recs = createRecommendations(812);

        TagEntropyMetric.TagEntropyResult result = (TagEntropyMetric.TagEntropyResult) metric.measureUser(tu, recs, ctx);
        assertThat(result, not(nullValue()));
        assertThat(result.entropy, closeTo(movieTagEntropy(50), 0.0001));

        // movie 664 has 40 tags
        tu = createUser(39);
        recs = createRecommendations(664);

        result =  (TagEntropyMetric.TagEntropyResult) metric.measureUser(tu, recs, ctx);
        assertThat(result, not(nullValue()));
        assertThat(result.entropy, closeTo(movieTagEntropy(40), 0.0001));

        TagEntropyMetric.TagEntropyResult aggResult = (TagEntropyMetric.TagEntropyResult) metric.getAggregateMeasurements(ctx);
        assertThat(aggResult, not(nullValue()));
        // average of 1 user's entropy is that entropy
        assertThat(aggResult.entropy,
                closeTo((movieTagEntropy(50) + movieTagEntropy(40)) / 2, 0.0001));
    }

    /**
     * Test a user with two movies.
     */
    @Test
    public void testTwoMovies() {
        TagEntropyMetric.Context ctx = metric.createContext(null, null, recommender);
        assertThat(ctx, not(nullValue()));

        // movie 812 has 50 tags
        // movie 664 has 40 tags
        TestUser tu = createUser(42);
        ResultList recs = createRecommendations(812, 664);

        double expected = 6.415995;
        TagEntropyMetric.TagEntropyResult result =  (TagEntropyMetric.TagEntropyResult) metric.measureUser(tu, recs, ctx);
        assertThat(result, not(nullValue()));
        assertThat(result.entropy, closeTo(expected, 0.0001));

        TagEntropyMetric.TagEntropyResult aggResult =  (TagEntropyMetric.TagEntropyResult) metric.getAggregateMeasurements(ctx);
        assertThat(aggResult, not(nullValue()));
        // average of 1 user's entropy is that entropy
        assertThat(aggResult.entropy,
                closeTo(expected, 0.0001));
    }

    private double movieTagEntropy(int ntags) {
        // probability of 1 tag
        double prob = 1.0 / ntags;
        // entropy of ntags such tags, 1 movie
        return -ntags * prob * (Math.log(prob) / Math.log(2));
    }

    private ResultList createRecommendations(long... items) {
        List<Result> recs = Lists.newArrayList();
        for (long item: items) {
            recs.add(Results.create(item, 0));
        }
        return Results.newResultList(recs);

    }
    private TestUser createUser(long uid) {
        UserHistory<Event> train = History.forUser(uid);
        UserHistory<Event> test = History.forUser(uid);
        return new TestUser(train, test);
    }
}
