package ph.gov.philrice.rcepdeliveryinspection.dbase.dao;

import androidx.room.Dao;

@Dao
public interface InspectionDataDAO {
  /*  @Query("select `deliveryId`,`ticketNumber`,`coopAccreditation`,`sgAccreditation`,`seedTag`,`seedVariety`,`seedClass`,`totalWeight`,`weightPerBag`,`deliveryDate`,`deliverTo`,`coordinates`,`status`,`userId`,`dateCreated` from tbl_delivery order by ticketNumber desc")
    List<InspectionData> getInspectionData();*/
}
