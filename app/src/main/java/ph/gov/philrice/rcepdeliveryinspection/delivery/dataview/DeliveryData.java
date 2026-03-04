package ph.gov.philrice.rcepdeliveryinspection.delivery.dataview;

public class DeliveryData {

    private int deliveryId;
    private String ticketNumber;
    private String coopAccreditation;
    private String sgAccreditation;
    private String seedTag;
    private String seedVariety;
    private String seedClass;
    private String totalWeight;
    private String weightPerBag;
    private String deliveryDate;
    private String deliverTo;
    private String coordinates;
    private String status;
    private String userId;
    private String dateCreated;
    //
    private String province;
    private String municipality;

    public DeliveryData(int deliveryId, String ticketNumber, String coopAccreditation, String sgAccreditation, String seedTag, String seedVariety, String seedClass, String totalWeight, String weightPerBag, String deliveryDate, String deliverTo, String coordinates, String status, String userId, String dateCreated, String province, String municipality) {
        this.deliveryId = deliveryId;
        this.ticketNumber = ticketNumber;
        this.coopAccreditation = coopAccreditation;
        this.sgAccreditation = sgAccreditation;
        this.seedTag = seedTag;
        this.seedVariety = seedVariety;
        this.seedClass = seedClass;
        this.totalWeight = totalWeight;
        this.weightPerBag = weightPerBag;
        this.deliveryDate = deliveryDate;
        this.deliverTo = deliverTo;
        this.coordinates = coordinates;
        this.status = status;
        this.userId = userId;
        this.dateCreated = dateCreated;
        this.province = province;
        this.municipality = municipality;
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

    public String getSeedClass() {
        return seedClass;
    }

    public void setSeedClass(String seedClass) {
        this.seedClass = seedClass;
    }

    public String getTotalWeight() {
        return totalWeight;
    }

    public void setTotalWeight(String totalWeight) {
        this.totalWeight = totalWeight;
    }

    public String getWeightPerBag() {
        return weightPerBag;
    }

    public void setWeightPerBag(String weightPerBag) {
        this.weightPerBag = weightPerBag;
    }

    public String getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(String deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public String getDeliverTo() {
        return deliverTo;
    }

    public void setDeliverTo(String deliverTo) {
        this.deliverTo = deliverTo;
    }

    public String getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(String coordinates) {
        this.coordinates = coordinates;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
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
}
