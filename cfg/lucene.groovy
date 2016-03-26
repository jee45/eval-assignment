/**
 * Created by ad on 3/25/16.
 */

import edu.umn.cs.recsys.cbf.LuceneItemItemModel;
import org.lenskit.knn.NeighborhoodSize;
import org.lenskit.knn.item.model.ItemItemModel;
import org.lenskit.knn.item.ModelSize;
import org.lenskit.knn.item.ItemItemScorer;
import org.grouplens.lenskit.transform.normalize.UserVectorNormalizer;
import org.grouplens.lenskit.transform.normalize.BaselineSubtractingUserVectorNormalizer;
import org.lenskit.baseline.BaselineScorer;
import org.lenskit.baseline.ItemMeanRatingItemScorer;



for (nnbrs in [5, 10, 15, 20, 25, 30, 40, 50, 75, 100]) {
    algorithm("Lucene") {
        attributes["NNbrs"] = nnbrs
        include 'tag-setup.groovy'
        include 'fallback.groovy'
        bind ItemScorer to ItemItemScorer
        bind ItemItemModel to LuceneItemItemModel
        set NeighborhoodSize to nnbrs
        // consider using all 100 movies as neighbors
        set ModelSize to 100
    }

    algorithm("LuceneNorm") {
        5
        attributes["NNbrs"] = nnbrs
        include 'tag-setup.groovy'
        include 'fallback.groovy'
        bind ItemScorer to ItemItemScorer
        bind ItemItemModel to LuceneItemItemModel
        set NeighborhoodSize to nnbrs
        // consider using all 100 movies as neighbors
        set ModelSize to 100
        bind UserVectorNormalizer to BaselineSubtractingUserVectorNormalizer
        within (UserVectorNormalizer) {
            bind (BaselineScorer, ItemScorer) to ItemMeanRatingItemScorer
        }
    }
}