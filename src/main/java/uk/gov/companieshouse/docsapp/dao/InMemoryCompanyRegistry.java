package uk.gov.companieshouse.docsapp.dao;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.docsapp.model.company.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(name = "companieshouse.docsapp.test", havingValue = "true", matchIfMissing = false)
public class InMemoryCompanyRegistry implements CompanyRegistry {
    final HashMap<String, Company> companies = new HashMap<>();
    final Random companyNumberGenerator = new Random();

    public InMemoryCompanyRegistry() {
        reset();
    }

    public void reset() {
        companies.clear();
        Object[][] companyData = new Object[][] {
                {"My Company Ltd", "123456789", true, LocalDate.of(2010, 4, 20)},
                {"Italian Company Ltd", "637399827", true, LocalDate.of(2008, 1, 5)},
                {"We Are Partners LLP", "194745294", true, LocalDate.of(2023, 9, 1)},
                {"No Profit Ltd", "946401763", true, LocalDate.of(2003, 3, 13)}
        };
        for (Object[] row : companyData) {
            Company company = new LimitedCompany((String)row[0], (boolean)row[2]);
            company.setRegistrationNumber((String)row[1]);
            company.setIncorporatedOn((LocalDate)row[3]);
            companies.put(company.getRegistrationNumber(), company);
        }
    }

    @Override
    public List<Company> getCompanies(String namePattern, Integer yearOfIncorporation, Boolean activeState, Type companyType, Sort sortBy) {
        List<Company> result = companies.values().stream().filter(company -> {
            boolean keep = true;
            if (namePattern != null && !company.getCompanyName().matches(namePattern)) keep = false;
            if (yearOfIncorporation != null && company.getIncorporatedOn().getYear() != yearOfIncorporation) keep = false;
            if (activeState != null && !Objects.equals(company.isActive(), activeState)) keep = false;
            if (companyType != null) {
                Class<? extends Company> companyClass = switch (companyType) {
                    case LLP -> LimitedLiabilityPartnership.class;
                    case LTD -> LimitedCompany.class;
                    case FOREIGN -> ForeignCompany.class;
                    case NONPROFIT -> NonProfitOrganization.class;
                };
                if (!companyClass.isAssignableFrom(Company.class)) keep = false;
            }
            return keep;
        }).collect(Collectors.toCollection(ArrayList::new));

        if (sortBy != null) switch (sortBy) {
            case NAME -> result.sort(Comparator.comparing(Company::getCompanyName));
            case DATE -> result.sort(Comparator.comparing(Company::getIncorporatedOn));
            case NUMBER -> result.sort(Comparator.comparing(Company::getRegistrationNumber));
        }
        return result;
    }

    @Override
    public Company getCompany(String number) {
        return companies.getOrDefault(number, null);
    }

    @Override
    public void deleteCompany(String number) {
        companies.remove(number);
    }

    @Override
    public Company addCompany(Company data) {
        String number;
        do {
            number = String.valueOf(companyNumberGenerator.nextInt());
        } while (companies.containsKey(number));
        data.setRegistrationNumber(number);
        data.setIncorporatedOn(LocalDate.now());
        companies.put(number, data);
        return data;
    }

    @Override
    public void editCompany(String number, Company data) {
        if (!number.equals(data.getRegistrationNumber())) data.setRegistrationNumber(number);
        companies.put(number, data);
    }

    @Override
    public void patchCompany(String number, Company data) {
        Company company = companies.get(number);
        if (company != null) {
            if (data.getCompanyName() != null) company.setCompanyName(data.getCompanyName());
            if (data.isActive() != null) company.setActive(data.isActive());
            if (data.getIncorporatedOn() != null) company.setIncorporatedOn(data.getIncorporatedOn());
            if (data.getRegisteredAddress() != null) company.setRegisteredAddress(data.getRegisteredAddress());
        }
    }

}
