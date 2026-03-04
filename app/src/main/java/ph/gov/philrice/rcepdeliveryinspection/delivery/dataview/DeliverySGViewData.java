package ph.gov.philrice.rcepdeliveryinspection.delivery.dataview;

public class DeliverySGViewData {

    private String batchTicketNumber;
    private String variety;
    private int totalBags;
    private String deliveryDate;
    private String dropoffPoint;
    private int status;
    private String asOf;

    private String province;
    private String municipality;
    private int actualTotal;

    public DeliverySGViewData(String batchTicketNumber, String variety, int totalBags, String deliveryDate, String dropoffPoint, int status, String asOf, String province, String municipality,int actualTotal) {
        this.batchTicketNumber = batchTicketNumber;
        this.variety = variety;
        this.totalBags = totalBags;
        this.deliveryDate = deliveryDate;
        this.dropoffPoint = dropoffPoint;
        this.status = status;
        this.asOf = asOf;
        this.province = province;
        this.municipality = municipality;
        this.actualTotal= actualTotal;
    }

    public int getActualTotal() {
        return actualTotal;
    }

    public void setActualTotal(int actualTotal) {
        this.actualTotal = actualTotal;
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
}
