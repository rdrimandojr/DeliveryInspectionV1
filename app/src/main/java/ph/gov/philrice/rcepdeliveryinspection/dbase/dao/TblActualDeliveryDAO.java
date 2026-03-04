package ph.gov.philrice.rcepdeliveryinspection.dbase.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import ph.gov.philrice.rcepdeliveryinspection.dbase.tables.TblActualDelivery;

@Dao
public interface TblActualDeliveryDAO {

    @Insert
    void insertActualDelivery(TblActualDelivery tblActualDelivery);

    @Query("delete from tbl_actual_delivery")
    void nukeTable();

    @Query("delete from tbl_actual_delivery where batchTicketNumber =:batchTicketNumber")
    void reset(String batchTicketNumber);

    @Query("select * from tbl_actual_delivery where batchTicketNumber =:batchTicketNumber")
    List<TblActualDelivery> getActualDeliveryByBatch(String batchTicketNumber);

    @Query("select count(batchTicketNumber) from tbl_actual_delivery where sendLocal = 1 and batchTicketNumber =:batchTicketNumber")
        //if result > 0 ? send data local server : already sent local server
    int sendLocal(String batchTicketNumber);

    @Query("select count(batchTicketNumber) from tbl_actual_delivery where sendLocal = 1")
        //if result > 0 ? send data local server : already sent local server
    int sendLocalAll();

    @Query("select count(batchTicketNumber) from tbl_actual_delivery where sendCentral= 1 and batchTicketNumber =:batchTicketNumber")
        //if result > 0 ? send data central server : already sent central server
    int sendCentral(String batchTicketNumber);

    @Query("select count(batchTicketNumber) from tbl_actual_delivery where sendCentral = 1")
        //if result > 0 ? send data local server : already sent local server
    int sendCentralAll();

    @Query("update tbl_actual_delivery set sendLocal = 0 where batchTicketNumber =:batchTicketNumber")
        //flags current batch to 0 if successfully sent all data to local server
    void successSendingLocal(String batchTicketNumber);

    @Query("update tbl_actual_delivery set sendCentral = 0 where batchTicketNumber =:batchTicketNumber")
        //flags current batch to 0 if successfully sent all data to central server
    void successSendingCentral(String batchTicketNumber);

    @Query("delete from tbl_actual_delivery where batchTicketNumber =:batchTicketNumber")
    void removeBatchData(String batchTicketNumber);

    @Query("select count(batchTicketNumber) from tbl_actual_delivery where batchTicketNumber =:batchTicketNumber and seedTag =:seedTag")
    int hasActualDeliveryData(String batchTicketNumber, String seedTag);

    @Query("select isDownloaded from tbl_actual_delivery where batchTicketNumber =:batchTicketNumber order by id desc limit 1")
    int isDownloaded(String batchTicketNumber);
}
