package ph.gov.philrice.rcepdeliveryinspection.dbase.tables;

import androidx.annotation.NonNull;

public class SeedTagBag {
    String seedTag;
    int totalBagCount;
    int samplingLimit;

    public SeedTagBag(String seedTag, int totalBagCount, int samplingLimit) {
        this.seedTag = seedTag;
        this.totalBagCount = totalBagCount;
        this.samplingLimit = samplingLimit;
    }

    public String getSeedTag() {
        return seedTag;
    }

    public void setSeedTag(String seedTag) {
        this.seedTag = seedTag;
    }

    public int getTotalBagCount() {
        return totalBagCount;
    }

    public void setTotalBagCount(int totalBagCount) {
        this.totalBagCount = totalBagCount;
    }

    public int getSamplingLimit() {
        return samplingLimit;
    }

    public void setSamplingLimit(int samplingLimit) {
        this.samplingLimit = samplingLimit;
    }

    @NonNull
    @Override
    public String toString() {
        return "SeedTagBag{" +
                "seedTag='" + seedTag + '\'' +
                ", totalBagCount=" + totalBagCount +
                ", samplingLimit=" + samplingLimit +
                '}';
    }
}
