package ph.gov.philrice.rcepdeliveryinspection;

public class Station {

    private int station_id;
    private String station_name;
    private String external_ip;
    private String subdomain_name;
    private String port;
    private int is_active;

    public Station(int station_id, String station_name, String external_ip, String subdomain_name, String port, int is_active) {
        this.station_id = station_id;
        this.station_name = station_name;
        this.external_ip = external_ip;
        this.subdomain_name = subdomain_name;
        this.port = port;
        this.is_active = is_active;
    }

    public int getStation_id() {
        return station_id;
    }

    public void setStation_id(int station_id) {
        this.station_id = station_id;
    }

    public String getStation_name() {
        return station_name;
    }

    public void setStation_name(String station_name) {
        this.station_name = station_name;
    }

    public String getExternal_ip() {
        return external_ip;
    }

    public void setExternal_ip(String external_ip) {
        this.external_ip = external_ip;
    }

    public String getSubdomain_name() {
        return subdomain_name;
    }

    public void setSubdomain_name(String subdomain_name) {
        this.subdomain_name = subdomain_name;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public int getIs_active() {
        return is_active;
    }

    public void setIs_active(int is_active) {
        this.is_active = is_active;
    }
}
