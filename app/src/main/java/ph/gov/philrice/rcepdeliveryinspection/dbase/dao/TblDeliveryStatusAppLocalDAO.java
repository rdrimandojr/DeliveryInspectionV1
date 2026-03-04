package ph.gov.philrice.rcepdeliveryinspection.dbase.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import ph.gov.philrice.rcepdeliveryinspection.dbase.tables.TblDeliveryStatusAppLocal;

@Dao
public interface TblDeliveryStatusAppLocalDAO {

    @Insert
    void insertDeliveryStatusAppLocal(TblDeliveryStatusAppLocal tblDeliveryStatusAppLocal);

    @Query("delete from tbl_delivery_status_app_local")
    void nukeTable();

    @Query("delete from tbl_delivery_status_app_local where batchTicketNumber =:batchTicketNumber")
    void reset(String batchTicketNumber);

    @Query("select status from tbl_delivery_status_app_local where batchTicketNumber =:batchTicketNumber order by dateCreated desc limit 1")
    int getDeliveryStatus(String batchTicketNumber);

    @Query("select count(batchTicketNumber) from tbl_delivery_status_app_local where batchTicketNumber =:batchTicketNumber limit 1")
    int hasLocalStatus(String batchTicketNumber);

    @Query("select * from tbl_delivery_status_app_local where batchTicketNumber =:batchTicketNumber order by id desc limit 1")
    List<TblDeliveryStatusAppLocal> getLocalDeliveryStatusByBatch(String batchTicketNumber);

    @Query("select * from tbl_delivery_status_app_local where batchTicketNumber =:batchTicketNumber order by id desc limit 1")
    TblDeliveryStatusAppLocal getLocalDeliveryStatusByBatch2(String batchTicketNumber);

    @Query("update tbl_delivery_status_app_local set status = 0  where batchTicketNumber =:batchTicketNumber")
    void updateLocalStatusPending(String batchTicketNumber);

    @Query("update tbl_delivery_status_app_local set status = 1 where batchTicketNumber =:batchTicketNumber")
    void updateLocalStatusPassed(String batchTicketNumber);

    @Query("update tbl_delivery_status_app_local set status = 2 where batchTicketNumber =:batchTicketNumber")
    void updateLocalStatusFailed(String batchTicketNumber);

    @Query("update tbl_delivery_status_app_local set attemptSendCentral = 1 where batchTicketNumber =:batchTicketNumber")
    void setAttemptSendCentral1(String batchTicketNumber);

    @Query("update tbl_delivery_status_app_local set attemptSendLocal = 1 where batchTicketNumber =:batchTicketNumber")
    void setAttemptSendLocal1(String batchTicketNumber);

    @Query("delete from tbl_delivery_status_app_local where batchTicketNumber =:batchTicketNumber")
    void removeBatchData(String batchTicketNumber);

    @Query("select count(batchTicketNumber) from tbl_delivery_status_app_local where batchTicketNumber =:batchTicketNumber")
    int hasDeliveryStatusAppLocal(String batchTicketNumber);

}
