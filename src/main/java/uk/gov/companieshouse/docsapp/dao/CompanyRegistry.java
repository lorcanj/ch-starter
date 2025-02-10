package uk.gov.companieshouse.docsapp.dao;

import uk.gov.companieshouse.docsapp.model.company.Company;

import java.util.List;

public interface CompanyRegistry {
    enum Sort { NAME, DATE, NUMBER }
    enum Type { FOREIGN, LTD, LLP, NONPROFIT }
    default List<Company> getCompanies() {
        return getCompanies(null);
    }
    default List<Company> getCompanies(Sort sortBy) {
        return getCompanies(null, null, null, null, sortBy);
    }
    List<Company> getCompanies(String namePattern, Integer yearOfIncorporation, Boolean activeStatus, Type companyType, Sort sortBy);
    Company getCompany(String number);
    void deleteCompany(String number);
    Company addCompany(Company data);
    void editCompany(String number, Company data);
    void patchCompany(String number, Company data);
}
