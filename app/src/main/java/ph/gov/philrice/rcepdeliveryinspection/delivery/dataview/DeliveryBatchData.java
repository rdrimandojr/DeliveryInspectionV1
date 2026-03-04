package ph.gov.philrice.rcepdeliveryinspection.delivery.dataview;

public class DeliveryBatchData {

    private int tmpDeliveryBatchDataId;
    private String ticketNumber;
    private String batchTicketNumber;
    private String deliveryDate;
    private String coopAccreditation;
    private String seedTag;
    private String seedVariety;
    private String seedClass;
    private int totalBagCount;
    private int userId;
    private String dateCreated;
    private String dropOffPoint;
    private String region;
    private String province;
    private String municipality;
    //
    private String prv_dropoff_id;
    private String prv;
    private String moa_number;
    private String app_version;
    private String batchSeries;
    private String sg_id;
    private int misBuffer;

    private double transpo_cost_per_bag;

    public DeliveryBatchData(int tmpDeliveryBatchDataId, String ticketNumber,
                             String batchTicketNumber, String deliveryDate,
                             String coopAccreditation, String seedTag, String seedVariety,
                             String seedClass, int totalBagCount, int userId,
                             String dateCreated, String dropOffPoint, String region,
                             String province, String municipality, String prv_dropoff_id,
                             String prv, String moa_number, String app_version,
                             String batchSeries, String sg_id, int misBuffer,
                             double transpo_cost_per_bag) {
        this.tmpDeliveryBatchDataId = tmpDeliveryBatchDataId;
        this.ticketNumber = ticketNumber;
        this.batchTicketNumber = batchTicketNumber;
        this.deliveryDate = deliveryDate;
        this.coopAccreditation = coopAccreditation;
        this.seedTag = seedTag;
        this.seedVariety = seedVariety;
        this.seedClass = seedClass;
        this.totalBagCount = totalBagCount;
        this.userId = userId;
        this.dateCreated = dateCreated;
        this.dropOffPoint = dropOffPoint;
        this.region = region;
        this.province = province;
        this.municipality = municipality;
        this.prv_dropoff_id = prv_dropoff_id;
        this.prv = prv;
        this.moa_number = moa_number;
        this.app_version = app_version;
        this.batchSeries = batchSeries;
        this.sg_id = sg_id;
        this.misBuffer = misBuffer;
        this.transpo_cost_per_bag = transpo_cost_per_bag;
    }


    /*public DeliveryBatchData(int tmpDeliveryBatchDataId, String ticketNumber,
                             String batchTicketNumber, String deliveryDate,
                             String coopAccreditation, String seedTag, String seedVariety,
                             String seedClass, int totalBagCount, int userId, String dateCreated,
                             String dropOffPoint, String region, String province,
                             String municipality, String prv_dropoff_id, String prv,
                             String moa_number, String app_version, String batchSeries,
                             String sg_id, int misBuffer) {
        this.tmpDeliveryBatchDataId = tmpDeliveryBatchDataId;
        this.ticketNumber = ticketNumber;
        this.batchTicketNumber = batchTicketNumber;
        this.deliveryDate = deliveryDate;
        this.coopAccreditation = coopAccreditation;
        this.seedTag = seedTag;
        this.seedVariety = seedVariety;
        this.seedClass = seedClass;
        this.totalBagCount = totalBagCount;
        this.userId = userId;
        this.dateCreated = dateCreated;
        this.dropOffPoint = dropOffPoint;
        this.region = region;
        this.province = province;
        this.municipality = municipality;
        this.prv_dropoff_id = prv_dropoff_id;
        this.prv = prv;
        this.moa_number = moa_number;
        this.app_version = app_version;
        this.batchSeries = batchSeries;
        this.sg_id = sg_id;
        this.misBuffer = misBuffer;
    }*/

    public double getTranspo_cost_per_bag() {
        return transpo_cost_per_bag;
    }

    public void setTranspo_cost_per_bag(double transpo_cost_per_bag) {
        this.transpo_cost_per_bag = transpo_cost_per_bag;
    }

    public int getMisBuffer() {
        return misBuffer;
    }

    public void setMisBuffer(int misBuffer) {
        this.misBuffer = misBuffer;
    }

    public String getSg_id() {
        return sg_id;
    }

    public void setSg_id(String sg_id) {
        this.sg_id = sg_id;
    }

    public int getTmpDeliveryBatchDataId() {
        return tmpDeliveryBatchDataId;
    }

    public void setTmpDeliveryBatchDataId(int tmpDeliveryBatchDataId) {
        this.tmpDeliveryBatchDataId = tmpDeliveryBatchDataId;
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

    public String getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(String deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public String getCoopAccreditation() {
        return coopAccreditation;
    }

    public void setCoopAccreditation(String coopAccreditation) {
        this.coopAccreditation = coopAccreditation;
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

    public String getSeedClass() {
        return seedClass;
    }

    public void setSeedClass(String seedClass) {
        this.seedClass = seedClass;
    }

    public int getTotalBagCount() {
        return totalBagCount;
    }

    public void setTotalBagCount(int totalBagCount) {
        this.totalBagCount = totalBagCount;
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

    public String getDropOffPoint() {
        return dropOffPoint;
    }

    public void setDropOffPoint(String dropOffPoint) {
        this.dropOffPoint = dropOffPoint;
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

    public String getApp_version() {
        return app_version;
    }

    public void setApp_version(String app_version) {
        this.app_version = app_version;
    }

    public String getBatchSeries() {
        return batchSeries;
    }

    public void setBatchSeries(String batchSeries) {
        this.batchSeries = batchSeries;
    }
}
