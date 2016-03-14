import edu.umn.cs.recsys.TagVocabulary
import edu.umn.cs.recsys.dao.CSVItemTagDAO
import edu.umn.cs.recsys.dao.ItemTagDAO
import edu.umn.cs.recsys.dao.TagFile
import org.grouplens.lenskit.data.text.CSVFileItemNameDAOProvider
import org.grouplens.lenskit.data.text.ItemFile
import org.lenskit.data.dao.ItemDAO
import org.lenskit.data.dao.MapItemNameDAO
import org.lenskit.data.dao.UserEventDAO

// use our tag data
bind ItemTagDAO to CSVItemTagDAO
// and our movie titles
bind MapItemNameDAO toProvider CSVFileItemNameDAOProvider
// configure input files for both of those
set TagFile to new File("data/movie-tags.csv")
set ItemFile to new File("data/movie-titles.csv")
// need tag vocab & DAO to be roots for diversity metric to use them
config.addRoot ItemTagDAO
config.addRoot ItemDAO
config.addRoot TagVocabulary
config.addRoot UserEventDAO
