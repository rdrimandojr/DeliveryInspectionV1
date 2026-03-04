package ph.gov.philrice.rcepdeliveryinspection.inspection.dataview;

public class SamplingData {

    private String ticketNumber;
    private float bagWeight;
    private int bagNumber;
    private String bagSequenceNumber;
    private String timeStamp;

    public SamplingData(String ticketNumber, float bagWeight, int bagNumber, String bagSequenceNumber, String timeStamp) {
        this.ticketNumber = ticketNumber;
        this.bagWeight = bagWeight;
        this.bagNumber = bagNumber;
        this.bagSequenceNumber = bagSequenceNumber;
        this.timeStamp = timeStamp;
    }

    public String getTicketNumber() {
        return ticketNumber;
    }

    public void setTicketNumber(String ticketNumber) {
        this.ticketNumber = ticketNumber;
    }

    public float getBagWeight() {
        return bagWeight;
    }

    public void setBagWeight(float bagWeight) {
        this.bagWeight = bagWeight;
    }

    public int getBagNumber() {
        return bagNumber;
    }

    public void setBagNumber(int bagNumber) {
        this.bagNumber = bagNumber;
    }

    public String getBagSequenceNumber() {
        return bagSequenceNumber;
    }

    public void setBagSequenceNumber(String bagSequenceNumber) {
        this.bagSequenceNumber = bagSequenceNumber;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }
}
