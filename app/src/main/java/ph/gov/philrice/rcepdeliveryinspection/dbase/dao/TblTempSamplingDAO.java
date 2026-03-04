package ph.gov.philrice.rcepdeliveryinspection.dbase.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import ph.gov.philrice.rcepdeliveryinspection.dbase.tables.TblTempSampling;

@Dao
public interface TblTempSamplingDAO {
    @Insert
    void insertTempSampling(TblTempSampling tblSampling);

    @Query("select * from tbl_temp_sampling")
    List<TblTempSampling> getTempSamplingData();

    @Query("delete from tbl_temp_sampling")
    void nukeTable();
}
