package devfikr.skripsi.ubnav.model;

/**
 * Created by Fikry-PC on 12/21/2017.
 */

public class Interchange {
    private String name, description;
    private int category, available;
    private double lat, lng;
    private long id;

    public Interchange(String name, String description, int category, int available, double lat, double lng, long id) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.available = available;
        this.lat = lat;
        this.lng = lng;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public int getAvailable() {
        return available;
    }

    public void setAvailable(int available) {
        this.available = available;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(long lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(long lng) {
        this.lng = lng;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
