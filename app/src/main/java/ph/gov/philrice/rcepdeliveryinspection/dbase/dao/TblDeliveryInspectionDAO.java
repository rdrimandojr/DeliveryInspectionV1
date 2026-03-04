package ph.gov.philrice.rcepdeliveryinspection.dbase.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import ph.gov.philrice.rcepdeliveryinspection.dbase.tables.SeedTagBag;
import ph.gov.philrice.rcepdeliveryinspection.dbase.tables.TblDeliveryInspection;

@Dao
public interface TblDeliveryInspectionDAO {
    @Insert
    void insert(TblDeliveryInspection tblDeliveryInspection);

    @Query("select count(deliveryId) from tbl_delivery_inspection where deliveryId =:deliveryId")
    int isExisting(int deliveryId);

    @Query("select prv_dropoff_id from tbl_delivery_inspection where batchTicketNumber =:batchTicketNumber limit 1")
    String getPrvDropoffId(String batchTicketNumber);

    @Query("select seed_distribution_mode from  tbl_delivery_inspection where batchTicketNumber =:batchTicketNumber limit 1")
    String getSeedDistributionMode(String batchTicketNumber);

    @Query("select * from tbl_delivery_inspection where batchTicketNumber =:batchTicketNumber limit 1")
    List<TblDeliveryInspection> getSingleDelivery(String batchTicketNumber);

    @Query("select prv from tbl_delivery_inspection where batchTicketNumber =:batchTicketNumber limit 1")
    String getPrv(String batchTicketNumber);

    @Query("select moa_number from tbl_delivery_inspection where batchTicketNumber =:batchTicketNumber limit 1")
    String getMoaNumber(String batchTicketNumber);

    @Query("select batchSeries from tbl_delivery_inspection where batchTicketNumber =:batchTicketNumber limit 1")
    String getBatchSeries(String batchTicketNumber);

    @Query("select misBuffer from tbl_delivery_inspection where batchTicketNumber =:batchTicketNumber limit 1")
    int getIsBuffer(String batchTicketNumber);

    @Query("select distinct(batchTicketNumber) from tbl_delivery_inspection")
    List<String> getAllbatchTicket();

    @Query("select distinct(seedTag) from tbl_delivery_inspection where batchTicketNumber =:batchTicketNumber and hasActualDelivery = 0")
    List<String> getSeedTagsByBatch(String batchTicketNumber);

    @Query("select seedTag,sum(totalBagCount) as totalBagCount,case when sum(totalBagCount) < 10 then sum(totalBagCount) else 10 end as samplingLimit from tbl_delivery_inspection where batchTicketNumber =:batchTicketNumber and hasActualDelivery = 0 group by seedTag order by seedTag asc")
    List<SeedTagBag> getSeedTagsByBatch2(String batchTicketNumber);

    //fetch variety by batchticket number and seedtag for unique value
    @Query("select seedVariety from tbl_delivery_inspection where batchTicketNumber =:batchTicketNumber and seedTag =:seedTag limit 1")
    String getSeedVariety(String batchTicketNumber, String seedTag);

    @Query("select totalBagCount from tbl_delivery_inspection where batchTicketNumber =:batchTicketNumber and seedTag =:seedTag")
    int getBagCountPerSeedTag(String batchTicketNumber, String seedTag);

    //already assigned in actual delivery
    @Query("update tbl_delivery_inspection set hasActualDelivery = 1 where batchTicketNumber =:batchTicketNumber and seedTag =:seedTag")
    void setActualDelivery1(String batchTicketNumber, String seedTag);

    //not yet assigned in actual delivery
    @Query("update tbl_delivery_inspection set hasActualDelivery = 0 where batchTicketNumber =:batchTicketNumber and seedTag =:seedTag")
    void setActualDelivery0(String batchTicketNumber, String seedTag);

    @Query("update tbl_delivery_inspection set hasActualDelivery = 0 where batchTicketNumber =:batchTicketNumber")
    void resetSeedTagFlag(String batchTicketNumber);

    //if greater than 1 not yet completed
    @Query("select count(deliveryId) from tbl_delivery_inspection where batchTicketNumber =:batchTicketNumber and hasActualDelivery = 0")
    int isActualDeliveryCompleted(String batchTicketNumber);

    /*@Query("select distinct(seedVariety) from tbl_delivery_inspection where seedVariety not in('NSIC Rc 222','NSIC Rc 216','NSIC Rc 160') and batchTicketNumber =:batchTicketNumber")
    List<String> getVarietiesByBatch(String batchTicketNumber);*/

    @Query("delete from tbl_delivery_inspection")
    void nukeTable();



    /*@Query("select count(variety) from lib_seeds where variety =:variety")
    int checkVariety(String variety); */

}
