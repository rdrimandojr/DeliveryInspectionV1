package ph.gov.philrice.rcepdeliveryinspection.dbase.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import ph.gov.philrice.rcepdeliveryinspection.dbase.tables.TblDeliveryStatus;

@Dao
public interface TblDeliveryStatusDAO {
    @Insert
    void insertDeliveryStatus(TblDeliveryStatus tblDeliveryStatus);

    @Query("delete from tbl_delivery_status")
    void nukeTable();

    @Query("delete from tbl_delivery_status where batchTicketNumber =:batchTicketNumber and (status != 0 or status != 3)")
    void reset(String batchTicketNumber);

    @Query("select count(deliveryStatusId) from tbl_delivery_status where deliveryStatusId =:deliveryStatusId")
    int checkDeliveryStatus(int deliveryStatusId);

    @Query("select status from tbl_delivery_status where batchTicketNumber =:batchTicketNumber order by deliveryStatusId desc limit 1")
    int getDeliveryStatus(String batchTicketNumber);

    @Query("select dateCreated from tbl_delivery_status where batchTicketNumber =:batchTicketNumber order by deliveryStatusId desc limit 1")
    String getDeliveryAsOf(String batchTicketNumber);
}
