package uk.gov.companieshouse.docsapp.model.company;

import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;

import java.time.LocalDate;

@Entity
@PrimaryKeyJoinColumn(name = "registrationNumber")
public class LimitedLiabilityPartnership extends Company {
    private int numberOfPartners;

    public LimitedLiabilityPartnership(){}
    public LimitedLiabilityPartnership(String companyName, boolean active) {
        super(companyName, active);
    }

    public int getNumberOfPartners() {
        return numberOfPartners;
    }

    public void setNumberOfPartners(int numberOfPartners) {
        this.numberOfPartners = numberOfPartners;
    }
}
