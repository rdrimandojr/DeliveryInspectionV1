package ph.gov.philrice.rcepdeliveryinspection.dbase.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import ph.gov.philrice.rcepdeliveryinspection.dbase.tables.TblTotalCommitment;

@Dao
public interface TblTotalCommitmentDAO {

    @Insert
    void insert(TblTotalCommitment tblTotalCommitment);

    @Query("select count(totalCommitmentId) from tbl_total_commitment where totalCommitmentId =:totalCommitmentId")
    int isExisting(int totalCommitmentId);

    @Query("select * from tbl_total_commitment where totalCommitmentId =:totalCommitmentId")
    List<TblTotalCommitment> getTotalCommitment(int totalCommitmentId);

    @Query("delete from tbl_total_commitment")
    void nukeTable();

    @Query("select total_value from tbl_total_commitment")
    int getTotalCommitment();
}