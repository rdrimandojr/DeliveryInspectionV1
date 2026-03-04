package ph.gov.philrice.rcepdeliveryinspection.dbase.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import ph.gov.philrice.rcepdeliveryinspection.dbase.tables.TblCommitment;

@Dao
public interface TblCommitmentDAO {

    @Insert
    void insert(TblCommitment tblCommitment);

    @Query("select count(commitmentId) from tbl_commitment where commitmentId =:commitmentId")
    int isExisting(int commitmentId);

    @Query("select * from tbl_commitment where commitmentId =:commitmentId")
    List<TblCommitment> getCommitment(int commitmentId);

    @Query("delete from tbl_commitment")
    void nukeTable();
}
