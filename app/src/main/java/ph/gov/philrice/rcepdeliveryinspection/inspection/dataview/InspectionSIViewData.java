package ph.gov.philrice.rcepdeliveryinspection.inspection.dataview;

public class InspectionSIViewData {


    private String batchTicketNumber;
    private String variety;
    private int totalBags;
    private String deliveryDate;
    private String dropoffPoint;
    private int status;
    private String asOf;

    private String province;
    private String municipality;
    private String seed_distribution_mode;

    private int sentLocal;//1 if completed else 0
    private int sentCentral;//1 if completed else 0

    public InspectionSIViewData(String batchTicketNumber, String variety, int totalBags,
                                String deliveryDate, String dropoffPoint, int status, String asOf,
                                String province, String municipality, String seed_distribution_mode) {
        this.batchTicketNumber = batchTicketNumber;
        this.variety = variety;
        this.totalBags = totalBags;
        this.deliveryDate = deliveryDate;
        this.dropoffPoint = dropoffPoint;
        this.status = status;
        this.asOf = asOf;
        this.province = province;
        this.municipality = municipality;
        this.seed_distribution_mode = seed_distribution_mode;
    }

    public String getBatchTicketNumber() {
        return batchTicketNumber;
    }

    public void setBatchTicketNumber(String batchTicketNumber) {
        this.batchTicketNumber = batchTicketNumber;
    }

    public String getVariety() {
        return variety;
    }

    public void setVariety(String variety) {
        this.variety = variety;
    }

    public int getTotalBags() {
        return totalBags;
    }

    public void setTotalBags(int totalBags) {
        this.totalBags = totalBags;
    }

    public String getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(String deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public String getDropoffPoint() {
        return dropoffPoint;
    }

    public void setDropoffPoint(String dropoffPoint) {
        this.dropoffPoint = dropoffPoint;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getAsOf() {
        return asOf;
    }

    public void setAsOf(String asOf) {
        this.asOf = asOf;
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

    public String getSeed_distribution_mode() {
        return seed_distribution_mode;
    }

    public void setSeed_distribution_mode(String seed_distribution_mode) {
        this.seed_distribution_mode = seed_distribution_mode;
    }
}
