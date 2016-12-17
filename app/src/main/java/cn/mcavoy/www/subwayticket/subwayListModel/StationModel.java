package cn.mcavoy.www.subwayticket.subwayListModel;

import java.util.ArrayList;
import java.util.List;

import cn.mcavoy.www.subwayticket.widget.Indexable;

public class StationModel {

    private List<StationsEntity> stationsEntities = new ArrayList<>();

    public void setStationsEntities(List<StationsEntity> stationsEntities) {
        this.stationsEntities = stationsEntities;
    }

    public List<StationsEntity> getStationsEntities() {
        return stationsEntities;
    }


    public static class StationsEntity implements Indexable {

        private String id;

        private String stationName;

        private String metroLine;

        private String metroLineSecond;

        public String getSortLetters() {
            return sortLetters;
        }

        @Override
        public String getIndex() {
            return sortLetters;
        }

        public void setSortLetters(String sortLetters) {
            this.sortLetters = sortLetters;
        }

        private String sortLetters;

        public void setId(String id) {
            this.id = id;
        }

        public void setStationName(String stationName) {
            this.stationName = stationName;
        }

        public void setMetroLine(String metroLine) {
            this.metroLine = metroLine;
        }

        public void setMetroLineSecond(String metroLineSecond) {
            this.metroLineSecond = metroLineSecond;
        }

        public String getId() {
            return id;
        }

        public String getStationName() {
            return stationName;
        }

        public String getMetroLine() {
            return metroLine;
        }

        public String getMetroLineSecond() {
            return metroLineSecond;
        }
    }
}
