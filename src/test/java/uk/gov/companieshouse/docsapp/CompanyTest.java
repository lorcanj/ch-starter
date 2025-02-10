package uk.gov.companieshouse.docsapp;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.companieshouse.docsapp.dao.InMemoryCompanyRegistry;
import uk.gov.companieshouse.docsapp.model.company.Company;
import uk.gov.companieshouse.docsapp.model.company.ForeignCompany;
import uk.gov.companieshouse.docsapp.model.company.LimitedCompany;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class CompanyTest {

    @Autowired
    public MockMvc mockMvc;

    @Autowired
    public ObjectMapper mapper;

    @Autowired
    public InMemoryCompanyRegistry inMemoryCompanyRegistry;

    @Test
    void getAllCompaniesTest() throws Exception {
        inMemoryCompanyRegistry.reset();
        MvcResult result = this.mockMvc.perform(get("/companies"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        List<Company> companies = mapper.readValue(result.getResponse().getContentAsString(), new TypeReference<List<Company>>() {});
        List<Company> companiesDirect = inMemoryCompanyRegistry.getCompanies();
        assertThat(companies).isNotEmpty();
        Assertions.assertIterableEquals(companies, companiesDirect);
    }

    @Test
    void getAllCompaniesWithFilter() throws Exception {
        inMemoryCompanyRegistry.reset();
        MvcResult result = this.mockMvc.perform(get("/companies?yearOfIncorporation=2008"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        List<Company> listOfCompanies = mapper.readValue(result.getResponse().getContentAsString(), new TypeReference<List<Company>>() {});
        assertThat(listOfCompanies).hasSize(1);
        Company company = listOfCompanies.getFirst();
        assertThat(company.getRegistrationNumber()).isEqualTo("637399827");
        assertThat(company.getCompanyName()).isEqualTo("Italian Company Ltd");
        assertThat(company.isActive()).isTrue();
        assertThat(company.getIncorporatedOn()).isEqualTo(LocalDate.of(2008, 1, 5));
    }

    @Test
    void searchCompanyNoResults() throws Exception {
        inMemoryCompanyRegistry.reset();
        String searchParameter = "/companies/blah";
        this.mockMvc.perform(get(searchParameter))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andReturn();
    }

    @Test
    void searchCompanyResult() throws Exception {
        inMemoryCompanyRegistry.reset();
        String searchParameter = "/companies/946401763";
        MvcResult result = this.mockMvc.perform(get(searchParameter))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        Company company = mapper.readValue(result.getResponse().getContentAsString(), Company.class);
        assertThat(company.getCompanyName()).isEqualTo("No Profit Ltd");
        assertThat(company.getRegistrationNumber()).isEqualTo("946401763");
        assertThat(company.getIncorporatedOn()).isEqualTo(LocalDate.of(2003, 3, 13));
        assertThat(company.isActive()).isTrue();
    }

    @Test
    void postValidCompany() throws Exception {
        inMemoryCompanyRegistry.reset();
        ForeignCompany foreignCompany = new ForeignCompany("Blah Enterprises", true);
        // maybe do something like this, dunno
        // int beforePost = inMemoryCompanyRegistry.getCompanies().size();
        String json = mapper.writeValueAsString(foreignCompany);
        MvcResult result = this.mockMvc.perform(post("/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andReturn();
        Company company = mapper.readValue(result.getResponse().getContentAsString(), Company.class);
        assertThat(company.getCompanyName()).isEqualTo(foreignCompany.getCompanyName());
        assertThat(company.getIncorporatedOn()).isNotNull();
        assertThat(company.isActive()).isTrue();
    }

    // TODO
//    @Test
//    void postInvalidCompany() throws Exception {
//        ForeignCompany
//    }

    @Test
    void noMatchCompanyNumber() throws Exception {
        inMemoryCompanyRegistry.reset();
        String companyNumber = "Lorcan";
        String searchParameter = "/companies/" + companyNumber;
        this.mockMvc.perform(get(searchParameter))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void patchCompany() throws Exception {
        inMemoryCompanyRegistry.reset();
        LimitedCompany limitedCompany = new LimitedCompany(null, false);
        String json = mapper.writeValueAsString(limitedCompany);
        String searchParameter = "/companies/123456789";
        this.mockMvc.perform(patch(searchParameter)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().is(204));

        Company updatedCompany = inMemoryCompanyRegistry.getCompany("123456789");
        assertThat(updatedCompany.isActive()).isFalse();
        assertThat(updatedCompany.getCompanyName()).isEqualTo("My Company Ltd");
        assertThat(updatedCompany.getIncorporatedOn()).isNotNull();
        assertThat(updatedCompany.getRegistrationNumber()).isNotNull();
    }

    @Test
    void deleteCompanySuccess() throws Exception {
        inMemoryCompanyRegistry.reset();
        String numberToDelete = "123456789";
        String searchParameter = "/companies/" +numberToDelete;
        this.mockMvc.perform(delete(searchParameter))
                .andDo(print())
                .andExpect(status().is2xxSuccessful());
        Company deletedCompany = inMemoryCompanyRegistry.getCompany(numberToDelete);
        assertThat(deletedCompany).isNull();
        inMemoryCompanyRegistry.reset();
    }

    @Test
    void deleteCompanyNoCompany() throws Exception {
        inMemoryCompanyRegistry.reset();
        String numberToDelete = "Hi";
        String searchParameter = "/companies/" + numberToDelete;
        this.mockMvc.perform(delete(searchParameter))
                .andDo(print())
                .andExpect(status().isNotFound());
    }
}
