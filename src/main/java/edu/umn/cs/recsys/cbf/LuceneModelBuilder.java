package edu.umn.cs.recsys.cbf;

import edu.umn.cs.recsys.dao.ItemTagDAO;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.lenskit.util.collections.LongUtils;
import org.lenskit.data.dao.ItemDAO;
import org.lenskit.data.dao.ItemNameDAO;
import org.lenskit.knn.item.ModelSize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.IOException;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class LuceneModelBuilder implements Provider<LuceneItemItemModel> {
    private static final Logger logger = LoggerFactory.getLogger(LuceneModelBuilder.class);
    private final ItemDAO dao;
    private final ItemNameDAO nameDAO;
    private final ItemTagDAO tagDAO;
    private final int modelNeighborCount;

    @Inject
    public LuceneModelBuilder(ItemDAO items, ItemNameDAO titles, ItemTagDAO tags, @ModelSize int nnbrs) {
        dao = items;
        nameDAO = titles;
        tagDAO = tags;
        modelNeighborCount = nnbrs;
    }

    @Override
    public LuceneItemItemModel get() {
        Directory dir = new RAMDirectory();

        try {
            writeMovies(dir);
        } catch (IOException e) {
            throw new RuntimeException("I/O error writing movie model", e);
        }
        return new LuceneItemItemModel(dir, LongUtils.packedSet(dao.getItemIds()), modelNeighborCount);
    }

    private void writeMovies(Directory dir) throws IOException {
        Analyzer analyzer = new EnglishAnalyzer(Version.LUCENE_35);
        IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_35, analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);

        IndexWriter writer = new IndexWriter(dir, config);
        try {
            logger.info("Building Lucene movie model");
            for (long movie: dao.getItemIds()) {
                logger.debug("building model for {}", movie);
                Document doc = makeMovieDocument(movie);
                writer.addDocument(doc);
            }
        } finally {
            writer.close();
        }
    }

    private Document makeMovieDocument(long movieId) {
        Document doc = new Document();
        doc.add(new Field("movie", Long.toString(movieId),
                                Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.NO));
        doc.add(new Field("title", nameDAO.getItemName(movieId),
                          Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.YES));
        doc.add(new Field("tags", StringUtils.join(tagDAO.getItemTags(movieId), "\n"),
                          Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.YES));
        return doc;
    }
}
