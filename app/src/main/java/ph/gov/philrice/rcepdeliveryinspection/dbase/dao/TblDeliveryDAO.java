package ph.gov.philrice.rcepdeliveryinspection.dbase.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import ph.gov.philrice.rcepdeliveryinspection.dbase.tables.TblDelivery;

@Dao
public interface TblDeliveryDAO {
    @Insert
    void insertDelivery(TblDelivery tblDelivery);

    @Query("delete from tbl_delivery")
    void nukeTable();

    @Query("select * from tbl_delivery order by deliveryId desc")
    List<TblDelivery> getDeliveries();

    @Query("select count(ticketNumber) from tbl_delivery where deliveryId =:deliveryId")
    int checkDelivery(int deliveryId);

    @Query("select * from tbl_delivery where ticketNumber =:ticketNumber")
    List<TblDelivery> getDeliveryByTicket(String ticketNumber);

    @Query("select * from tbl_delivery where batchTicketNumber =:batchTicketNumber limit 1")
    List<TblDelivery> getSingleDelivery(String batchTicketNumber);

    @Query("select distinct(batchTicketNumber) from tbl_delivery")
    List<String> getAllbatchTicket();

    @Query("select distinct(seedVariety) from tbl_delivery where seedVariety not in('NSIC Rc 222','NSIC Rc 216','NSIC Rc 160') and batchTicketNumber =:batchTicketNumber")
    List<String> getVarietiesByBatch(String batchTicketNumber);

    @Query("select distinct(seedTag) from tbl_delivery where batchTicketNumber =:batchTicketNumber")
    List<String> getSeedTagsByBatch(String batchTicketNumber);

}
