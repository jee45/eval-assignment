import edu.umn.cs.recsys.PopularityItemScorer
import org.lenskit.baseline.GlobalMeanRatingItemScorer
import org.lenskit.baseline.ItemMeanRatingItemScorer
import org.lenskit.baseline.UserMeanBaseline
import org.lenskit.baseline.UserMeanItemScorer

algorithm("GlobalMean") {
    include 'tag-setup.groovy'
    // score items by the global mean
    bind ItemScorer to GlobalMeanRatingItemScorer
    // recommendation is meaningless for this algorithm
    bind ItemRecommender to null
}
algorithm("Popular") {
    include 'tag-setup.groovy'
    // score items by their popularity
    bind ItemScorer to PopularityItemScorer
    // rating prediction is meaningless for this algorithm
    bind RatingPredictor to null
}
algorithm("ItemMean") {
    include 'tag-setup.groovy'
    // score items by their mean rating
    bind ItemScorer to ItemMeanRatingItemScorer
}
algorithm("PersMean") {
    include 'tag-setup.groovy'
    bind ItemScorer to UserMeanItemScorer
    bind (UserMeanBaseline, ItemScorer) to ItemMeanRatingItemScorer
}
