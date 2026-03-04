package ph.gov.philrice.rcepdeliveryinspection.dbase.tables;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

//This table is the receiver of deliver to be inspected
@Entity(tableName = "tbl_delivery_inspection")
public class TblDeliveryInspection {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int deliveryId;
    private String ticketNumber;
    private String batchTicketNumber;
    private String coopAccreditation;
    private String sgAccreditation;
    private String seedTag;
    private String seedVariety;
    private int totalBagCount;
    private String deliveryDate;
    private int userId;
    private String dateCreated;
    private String region;
    private String province;
    private String municipality;
    private String dropOffPoint;
    //
    private String prv_dropoff_id;
    private String prv;
    private String moa_number;
    private String batchSeries;
    private int hasActualDelivery;
    private int actualTotal;
    private int misBuffer;

    private String seed_distribution_mode;

    public TblDeliveryInspection(int deliveryId, String ticketNumber, String batchTicketNumber,
                                 String coopAccreditation, String sgAccreditation, String seedTag,
                                 String seedVariety, int totalBagCount, String deliveryDate,
                                 int userId, String dateCreated, String region, String province,
                                 String municipality, String dropOffPoint, String prv_dropoff_id,
                                 String prv, String moa_number, String batchSeries,
                                 int hasActualDelivery, int actualTotal, int misBuffer,String seed_distribution_mode) {
        this.deliveryId = deliveryId;
        this.ticketNumber = ticketNumber;
        this.batchTicketNumber = batchTicketNumber;
        this.coopAccreditation = coopAccreditation;
        this.sgAccreditation = sgAccreditation;
        this.seedTag = seedTag;
        this.seedVariety = seedVariety;
        this.totalBagCount = totalBagCount;
        this.deliveryDate = deliveryDate;
        this.userId = userId;
        this.dateCreated = dateCreated;
        this.region = region;
        this.province = province;
        this.municipality = municipality;
        this.dropOffPoint = dropOffPoint;
        this.prv_dropoff_id = prv_dropoff_id;
        this.prv = prv;
        this.moa_number = moa_number;
        this.batchSeries = batchSeries;
        this.hasActualDelivery = hasActualDelivery;
        this.actualTotal = actualTotal;
        this.misBuffer = misBuffer;
        this.seed_distribution_mode = seed_distribution_mode;
    }

    public int getMisBuffer() {
        return misBuffer;
    }

    public void setMisBuffer(int misBuffer) {
        this.misBuffer = misBuffer;
    }

    public int getActualTotal() {
        return actualTotal;
    }

    public void setActualTotal(int actualTotal) {
        this.actualTotal = actualTotal;
    }

    public int getHasActualDelivery() {
        return hasActualDelivery;
    }

    public void setHasActualDelivery(int hasActualDelivery) {
        this.hasActualDelivery = hasActualDelivery;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDeliveryId() {
        return deliveryId;
    }

    public void setDeliveryId(int deliveryId) {
        this.deliveryId = deliveryId;
    }

    public String getTicketNumber() {
        return ticketNumber;
    }

    public void setTicketNumber(String ticketNumber) {
        this.ticketNumber = ticketNumber;
    }

    public String getBatchTicketNumber() {
        return batchTicketNumber;
    }

    public void setBatchTicketNumber(String batchTicketNumber) {
        this.batchTicketNumber = batchTicketNumber;
    }

    public String getCoopAccreditation() {
        return coopAccreditation;
    }

    public void setCoopAccreditation(String coopAccreditation) {
        this.coopAccreditation = coopAccreditation;
    }

    public String getSgAccreditation() {
        return sgAccreditation;
    }

    public void setSgAccreditation(String sgAccreditation) {
        this.sgAccreditation = sgAccreditation;
    }

    public String getSeedTag() {
        return seedTag;
    }

    public void setSeedTag(String seedTag) {
        this.seedTag = seedTag;
    }

    public String getSeedVariety() {
        return seedVariety;
    }

    public void setSeedVariety(String seedVariety) {
        this.seedVariety = seedVariety;
    }

    public int getTotalBagCount() {
        return totalBagCount;
    }

    public void setTotalBagCount(int totalBagCount) {
        this.totalBagCount = totalBagCount;
    }

    public String getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(String deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getMunicipality() {
        return municipality;
    }

    public void setMunicipality(String municipality) {
        this.municipality = municipality;
    }

    public String getDropOffPoint() {
        return dropOffPoint;
    }

    public void setDropOffPoint(String dropOffPoint) {
        this.dropOffPoint = dropOffPoint;
    }

    public String getPrv_dropoff_id() {
        return prv_dropoff_id;
    }

    public void setPrv_dropoff_id(String prv_dropoff_id) {
        this.prv_dropoff_id = prv_dropoff_id;
    }

    public String getPrv() {
        return prv;
    }

    public void setPrv(String prv) {
        this.prv = prv;
    }

    public String getMoa_number() {
        return moa_number;
    }

    public void setMoa_number(String moa_number) {
        this.moa_number = moa_number;
    }

    public String getBatchSeries() {
        return batchSeries;
    }

    public void setBatchSeries(String batchSeries) {
        this.batchSeries = batchSeries;
    }

    public String getSeed_distribution_mode() {
        return seed_distribution_mode;
    }

    public void setSeed_distribution_mode(String seed_distribution_mode) {
        this.seed_distribution_mode = seed_distribution_mode;
    }
}

