package uk.gov.companieshouse.docsapp.model.company;

import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;

@Entity
@PrimaryKeyJoinColumn(name = "registrationNumber")
public class NonProfitOrganization extends Company {

    public NonProfitOrganization(){}
    public NonProfitOrganization(String companyName, boolean active) {
        super(companyName, active);
    }

}
