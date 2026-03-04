package ph.gov.philrice.rcepdeliveryinspection.dbase.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import ph.gov.philrice.rcepdeliveryinspection.dbase.tables.TblSendingStatus;

@Dao
public interface TblSendingStatusDAO {
    @Insert
    void insertSendingStatus(TblSendingStatus tblSendingStatus);

    @Query("select status from tbl_sending_status where batchTicketNumber =:batchTicketNumber order by id desc limit 1")
    int getSendingStatus(String batchTicketNumber);

    @Query("select count(id) from tbl_sending_status where batchTicketNumber =:batchTicketNumber order by id desc limit 1")
    int isExistingLocal(String batchTicketNumber);

    @Query("update tbl_sending_status set isLocal =:isLocal, status =:status where batchTicketNumber =:batchTicketNumber ")
    void updateExistingLocal(int isLocal, int status, String batchTicketNumber);

    @Query("select count(id) from tbl_sending_status where status = 1")
    int hasPendingLocal();

}