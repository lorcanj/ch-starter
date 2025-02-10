package uk.gov.companieshouse.docsapp.dao;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.docsapp.model.company.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(name = "companieshouse.docsapp.test", havingValue = "false", matchIfMissing = true)
public class JpaCompanyRegistry implements CompanyRegistry {

    private static final String NOTFOUND = "Company not found";
    private static final String ALREADYEXISTS = "Company already exists";

    // inject the repo to our JpaCompanyRegistry for testing
    public final CompanyRepository repo;
    public JpaCompanyRegistry(CompanyRepository repo) {
        this.repo = repo;
    }

    // org.springframework.data.domain.Sort.by
    @Override
    public List<Company> getCompanies(String namePattern, Integer yearOfIncorporation, Boolean activeState, Type companyType, Sort sortBy) {
        org.springframework.data.domain.Sort sort = switch (sortBy) {
            case NAME -> org.springframework.data.domain.Sort.by("companyName");
            case NUMBER -> org.springframework.data.domain.Sort.by("companyNumber");
            case DATE -> org.springframework.data.domain.Sort.by("incorporatedOn");
            case null -> null;
        };
        // why sorting before filtering ?
        // should do it the other way around
        List<Company> companies = sort == null ? repo.findAll() : repo.findAll(sort);
        return companies.stream().filter(company ->  {
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
    }

    public Company getByCompanyName(String companyName) {
        try {
            return repo.getByCompanyName(companyName);
        }
        catch (EntityNotFoundException e) {
            throw new IllegalArgumentException(NOTFOUND);
        }
    }

    @Override
    public Company getCompany(String number) {
        try {
            return repo.findById(number).get();
        }
        catch(EntityNotFoundException e) {
            throw new IllegalArgumentException(NOTFOUND);
        }
    }

    @Override
    public void deleteCompany(String number) {
        try {
            repo.deleteById(number);
        } catch(EntityNotFoundException e) {
            throw new IllegalArgumentException(NOTFOUND);
        }
    }

    @Override
    public Company addCompany(Company data) {
        if (data.getRegistrationNumber() != null && repo.existsById(data.getRegistrationNumber()))
            throw new IllegalArgumentException(ALREADYEXISTS);
        if (data.getCompanyName() != null && repo.existsByCompanyName(data.getCompanyName()))
            throw new IllegalArgumentException(ALREADYEXISTS);

        data.setIncorporatedOn(LocalDate.now());
        data.setRegistrationNumber(UUID.randomUUID().toString());
        return repo.save(data);
    }

    // this is put, which replaces the full object in the db
    @Override
    public void editCompany(String number, Company data) {
        if (!number.equals(data.getRegistrationNumber()))
            throw new IllegalArgumentException(NOTFOUND);
        if (!repo.existsById(number))
            throw new IllegalArgumentException(NOTFOUND);
        Company dbCompany = repo.getReferenceById(number);
        if (!Objects.equals(data.getRegistrationNumber(), dbCompany.getRegistrationNumber()))
            throw new IllegalArgumentException("Registration numbers do not match");
        repo.save(data);
    }

    // here we are updating the
    @Override
    public void patchCompany(String number, Company data) {
        Optional<Company> maybeCompany = repo.findById(number);
        if (maybeCompany.isEmpty())
            throw new IllegalArgumentException(NOTFOUND);

        Company company = maybeCompany.get();
        // check that the company from the repo is the same class or a superclass
        // of the company that we're passing into the function
        if (!company.getClass().isAssignableFrom(data.getClass()))
            throw new IllegalArgumentException("Incompatible data type");

        switch (company) {
            case ForeignCompany fc ->
                fc.setCountryOfOrigin(((ForeignCompany) data).getCountryOfOrigin());

            case LimitedCompany ltd -> {
                ltd.setNumberOfShares(((LimitedCompany) data).getNumberOfShares());
                ltd.setPublic(((LimitedCompany) data).isPublic());
            }

            case LimitedLiabilityPartnership llp ->
                llp.setNumberOfPartners(((LimitedLiabilityPartnership) data).getNumberOfPartners());

            case NonProfitOrganization nop -> {}

            default -> throw new IllegalArgumentException("Incorrect data type");
        }
        repo.save(company);
    }
}
