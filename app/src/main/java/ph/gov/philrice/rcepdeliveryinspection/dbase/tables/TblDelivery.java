package ph.gov.philrice.rcepdeliveryinspection.dbase.tables;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tbl_delivery")
public class TblDelivery {
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
    private int actualTotal;

    public TblDelivery(int deliveryId, String ticketNumber, String batchTicketNumber, String coopAccreditation, String sgAccreditation, String seedTag, String seedVariety, int totalBagCount, String deliveryDate, int userId, String dateCreated, String region, String province, String municipality, String dropOffPoint, int actualTotal) {
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
        this.actualTotal = actualTotal;
    }

    public int getActualTotal() {
        return actualTotal;
    }

    public void setActualTotal(int actualTotal) {
        this.actualTotal = actualTotal;
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
}

