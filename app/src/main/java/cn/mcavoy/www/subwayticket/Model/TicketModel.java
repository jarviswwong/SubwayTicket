package cn.mcavoy.www.subwayticket.Model;


import java.util.ArrayList;
import java.util.List;

public class TicketModel {

    private List<TicketsEntity> ticketsEntities = new ArrayList<>();

    public List<TicketsEntity> getTicketsEntities() {
        return ticketsEntities;
    }

    public void setTicketsEntities(List<TicketsEntity> ticketsEntities) {
        this.ticketsEntities = ticketsEntities;
    }

    public static class TicketsEntity {
        private String id;
        private String ownerId;
        private String oStationName;
        private String tStationName;
        private String ticketNum;
        private String ticketPrice;
        private String payDate;
        private String ticketStatus;

        public String getOwnerId() {
            return ownerId;
        }

        public String getId() {
            return this.id;
        }

        public String getoStationName() {
            return this.oStationName;
        }

        public String gettStationName() {
            return this.tStationName;
        }

        public String getPayDate() {
            return payDate;
        }

        public String getTicketNum() {
            return ticketNum;
        }

        public String getTicketPrice() {
            return ticketPrice;
        }

        public String getTicketStatus() {
            return ticketStatus;
        }

        public void setId(String id) {
            this.id = id;
        }

        public void setoStationName(String oStationName) {
            this.oStationName = oStationName;
        }

        public void setPayDate(String payDate) {
            this.payDate = payDate;
        }

        public void setTicketNum(String ticketNum) {
            this.ticketNum = ticketNum;
        }

        public void setTicketPrice(String ticketPrice) {
            this.ticketPrice = ticketPrice;
        }

        public void setTicketStatus(String ticketStatus) {
            this.ticketStatus = ticketStatus;
        }

        public void settStationName(String tStationName) {
            this.tStationName = tStationName;
        }

        public void setOwnerId(String ownerId) {
            this.ownerId = ownerId;
        }
    }
}
