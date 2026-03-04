package ph.gov.philrice.rcepdeliveryinspection.dbase.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import ph.gov.philrice.rcepdeliveryinspection.dbase.tables.LibSeeds;

@Dao
public interface LibSeedsDAO {
    @Insert
    void insertSeed(LibSeeds libSeeds);

    @Query("delete from lib_seeds")
    void nukeTable();

    @Query("select count(variety) from lib_seeds where variety =:variety")
    int checkVariety(String variety);

    @Query("select * from lib_seeds order by variety collate nocase asc")
    List<LibSeeds> getSeeds();

}
