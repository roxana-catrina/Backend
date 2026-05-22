package Licenta.Licenta.Dto;

public class VideoCallSignal {

    /**
     * Tipul semnalului:
     * "call-offer"     – apelant trimite oferta SDP
     * "call-answer"    – destinatar acceptă și trimite răspunsul SDP
     * "ice-candidate"  – candidat ICE pentru negocierea conexiunii P2P
     * "call-rejected"  – destinatar refuză apelul
     * "call-ended"     – oricare parte încheie apelul
     */
    private String type;

    private String fromUserId;
    private String toUserId;
    private String fromUserName;

    /** RTCSessionDescription (offer / answer) – structură { type, sdp } */
    private Object sdp;

    /** RTCIceCandidate – structură { candidate, sdpMid, sdpMLineIndex } */
    private Object candidate;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(String fromUserId) {
        this.fromUserId = fromUserId;
    }

    public String getToUserId() {
        return toUserId;
    }

    public void setToUserId(String toUserId) {
        this.toUserId = toUserId;
    }

    public String getFromUserName() {
        return fromUserName;
    }

    public void setFromUserName(String fromUserName) {
        this.fromUserName = fromUserName;
    }

    public Object getSdp() {
        return sdp;
    }

    public void setSdp(Object sdp) {
        this.sdp = sdp;
    }

    public Object getCandidate() {
        return candidate;
    }

    public void setCandidate(Object candidate) {
        this.candidate = candidate;
    }
}
