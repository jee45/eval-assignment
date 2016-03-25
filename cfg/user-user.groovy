
for (nnbrs in [5, 10, 15, 20, 25, 30, 40, 50, 75, 100]) {
    algorithm("UserUser") {
        include 'tag-setup.groovy'
        include 'fallback.groovy'
        // Attributes let you specify additional properties of the algorithm.
        // They go in the output file, so you can do things like plot accuracy
        // by neighborhood size
        attributes["NNbrs"] = nnbrs
        // use the user-user rating predictor
        bind ItemScorer to UserUserItemScorer
        set NeighborhoodSize to nnbrs
        bind VectorSimilarity to PearsonCorrelation
    }
    algorithm("UserUserNorm") {
        include 'tag-setup.groovy'
        include 'fallback.groovy'
        // Attributes let you specify additional properties of the algorithm.
        // They go in the output file, so you can do things like plot accuracy
        // by neighborhood size
        attributes["NNbrs"] = nnbrs
        // use the user-user rating predictor
        bind ItemScorer to UserUserItemScorer
        set NeighborhoodSize to nnbrs
        bind VectorNormalizer to MeanCenteringVectorNormalizer
        bind VectorSimilarity to PearsonCorrelation
    }
}


