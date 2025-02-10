package uk.gov.companieshouse.docsapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.companieshouse.docsapp.dao.CompanyRepository;
import uk.gov.companieshouse.docsapp.dao.JpaCompanyRegistry;
import uk.gov.companieshouse.docsapp.model.company.Company;
import uk.gov.companieshouse.docsapp.model.company.LimitedCompany;

import static org.assertj.core.api.Assertions.assertThat;


@ExtendWith(SpringExtension.class)
@DataJpaTest
class CompanyJpaTest {

    @Autowired
    public CompanyRepository companyRepository;

    @Autowired
    public ObjectMapper mapper;

//    @Test
//    void getAllCompaniesTest() {
//        Company company = new LimitedCompany("Test Company", true);
//        companyRepository.
//    }

//    Company savedCompany = jpaCompanyRegistry.addCompany(company);
//    Company foundCompany = jpaCompanyRegistry.getByCompanyName(company.getCompanyName());
//    assertThat(savedCompany.getCompanyName()).isEqualTo(foundCompany.getCompanyName());
//    assertThat(company.getIncorporatedOn()).isNull();
//    assertThat(savedCompany.getIncorporatedOn()).isNotNull();
//    assertThat(foundCompany.getIncorporatedOn()).isNotNull();

}
