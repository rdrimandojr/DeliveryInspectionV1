package ph.gov.philrice.rcepdeliveryinspection.objects;

public class RlaDetails {
    int rlaId;
    String coopAccreditation;
    String labNo;
    String lotNo;
    int noOfBags;
    String seedVariety;
    String moaNumber;

    public RlaDetails(int rlaId, String coopAccreditation, String labNo, String lotNo, int noOfBags, String seedVariety, String moaNumber) {
        this.rlaId = rlaId;
        this.coopAccreditation = coopAccreditation;
        this.labNo = labNo;
        this.lotNo = lotNo;
        this.noOfBags = noOfBags;
        this.seedVariety = seedVariety;
        this.moaNumber = moaNumber;
    }

    public int getRlaId() {
        return rlaId;
    }

    public void setRlaId(int rlaId) {
        this.rlaId = rlaId;
    }

    public String getCoopAccreditation() {
        return coopAccreditation;
    }

    public void setCoopAccreditation(String coopAccreditation) {
        this.coopAccreditation = coopAccreditation;
    }

    public String getLabNo() {
        return labNo;
    }

    public void setLabNo(String labNo) {
        this.labNo = labNo;
    }

    public String getLotNo() {
        return lotNo;
    }

    public void setLotNo(String lotNo) {
        this.lotNo = lotNo;
    }

    public int getNoOfBags() {
        return noOfBags;
    }

    public void setNoOfBags(int noOfBags) {
        this.noOfBags = noOfBags;
    }

    public String getSeedVariety() {
        return seedVariety;
    }

    public void setSeedVariety(String seedVariety) {
        this.seedVariety = seedVariety;
    }

    public String getMoaNumber() {
        return moaNumber;
    }

    public void setMoaNumber(String moaNumber) {
        this.moaNumber = moaNumber;
    }
}
