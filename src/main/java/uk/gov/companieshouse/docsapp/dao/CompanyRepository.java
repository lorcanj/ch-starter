package uk.gov.companieshouse.docsapp.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.companieshouse.docsapp.model.company.Company;

public interface CompanyRepository extends JpaRepository<Company, String> {
    boolean existsByCompanyName(String companyName);
    Company getByCompanyName(String companyName);
}
