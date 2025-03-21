package org.bitrepository.integrityservice.web;

import java.io.Serializable;

public class PillarDetailsDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private String pillarID;
    private String pillarName;
    private String pillarType;
    private String pillarDeleteFileApprover;

    public PillarDetailsDto() {}

    public PillarDetailsDto(String pillarID, String pillarName, String pillarType, String pillarDeleteFileApprover) {
        this.pillarID = pillarID;
        this.pillarName = pillarName;
        this.pillarType = pillarType;
        this.pillarDeleteFileApprover = pillarDeleteFileApprover;
    }

    public String getPillarID() {
        return pillarID;
    }

    public void setPillarID(String pillarID) {
        this.pillarID = pillarID;
    }

    public String getPillarName() {
        return pillarName;
    }

    public void setPillarName(String pillarName) {
        this.pillarName = pillarName;
    }

    public String getPillarType() {
        return pillarType;
    }

    public void setPillarType(String pillarType) {
        this.pillarType = pillarType;
    }

    public String getPillarDeleteFileApprover() {
        return pillarDeleteFileApprover;
    }

    public void setPillarDeleteFileApprover(String pillarDeleteFileApprover) {
        this.pillarDeleteFileApprover = pillarDeleteFileApprover;
    }

    @Override
    public String toString() {
        return "PillarDetailsDTO{" +
                "pillarID='" + pillarID + '\'' +
                ", pillarName='" + pillarName + '\'' +
                ", pillarType=" + pillarType +
                ", pillarDeleteFileApprover='" + pillarDeleteFileApprover + '\'' +
                '}';
    }
}