//package uk.gov.companieshouse.docsapp.dao;
//
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
//import org.springframework.stereotype.Component;
//import uk.gov.companieshouse.docsapp.model.company.*;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//import java.util.stream.Collectors;
//
//@Component
//@ConditionalOnProperty(name = "companieshouse.docsapp.test", havingValue = "false", matchIfMissing = true)
//public class JpaCompanyRegistry1 implements CompanyRegistry {
//
//    private CompanyRepository repo;
//
//    public JpaCompanyRegistry1(CompanyRepository repo) {
//        this.repo = repo;
//    }
//
//    @Override
//    public List<Company> getCompanies(String namePattern, Integer yearOfIncorporation, Boolean activeState, Type companyType, Sort sortBy) {
//        org.springframework.data.domain.Sort sort = switch (sortBy) {
//            case NUMBER -> org.springframework.data.domain.Sort.by("registrationNumber");
//            case DATE -> org.springframework.data.domain.Sort.by("incorporatedOn");
//            case NAME -> org.springframework.data.domain.Sort.by("companyName");
//            case null -> null;
//        };
//        List<Company> companies = sort == null ? repo.findAll() : repo.findAll(sort);
//        return companies.stream().filter(company -> {
//            boolean keep = true;
//            if (namePattern != null && !company.getCompanyName().matches(namePattern)) keep = false;
//            if (yearOfIncorporation != null && company.getIncorporatedOn().getYear() != yearOfIncorporation) keep = false;
//            if (activeState != null && company.isActive() != activeState) keep = false;
//            if (companyType != null) {
//                Class<? extends Company> companyClass = switch (companyType) {
//                    case LLP -> LimitedLiabilityPartnership.class;
//                    case LTD -> LimitedCompany.class;
//                    case FOREIGN -> ForeignCompany.class;
//                    case NONPROFIT -> NonProfitOrganization.class;
//                };
//                if (!companyClass.isAssignableFrom(Company.class)) keep = false;
//            }
//            return keep;
//        }).collect(Collectors.toCollection(ArrayList::new));
//    }
//
//    @Override
//    public Company getCompany(String number) {
//        if (!repo.existsById(number)) {
//            throw new IllegalArgumentException("Company not found");
//        }
//        return repo.getReferenceById(number);
//    }
//
//    @Override
//    public void deleteCompany(String number) {
//        if (!repo.existsById(number)) {
//            throw new IllegalArgumentException("Company not found");
//        }
//        repo.deleteById(number);
//    }
//
//    @Override
//    public Company addCompany(Company data) {
//        if (repo.existsByCompanyName(data.getCompanyName())) {
//            throw new IllegalArgumentException("Company name already exists");
//        }
//        // TODO generate company number
//        // TODO set the incorporation date to today
//        return repo.save(data);
//    }
//
//    @Override
//    public void editCompany(String number, Company data) {
//        if (!repo.existsById(number)) {
//            throw new IllegalArgumentException("Company not found");
//        }
//        // TODO force the registration number in the company to be the same as the number given
//        repo.save(data);
//    }
//
//    @Override
//    public void patchCompany(String number, Company data) {
//        Optional<Company> maybeCompany = repo.findById(number);
//        if (maybeCompany.isEmpty()) {
//            throw new IllegalArgumentException("Company not found");
//        } else {
//            Company company = maybeCompany.get();
//            if (!company.getClass().isAssignableFrom(data.getClass())) {
//                throw new IllegalArgumentException("Incompatible data type");
//            }
//            if (data.getCompanyName() != null) company.setCompanyName(data.getCompanyName());
//            if (data.isActive() != null) company.setActive(data.isActive());
//            if (data.getIncorporatedOn() != null) company.setIncorporatedOn(data.getIncorporatedOn());
//            if (data.getRegisteredAddress() != null) company.setRegisteredAddress(data.getRegisteredAddress());
//            switch (company) {
//                case ForeignCompany fc -> {
//                    fc.setCountryOfOrigin(((ForeignCompany) data).getCountryOfOrigin());
//                }
//                case LimitedCompany ltd -> {
//                    ltd.setNumberOfShares(((LimitedCompany) data).getNumberOfShares());
//                    ltd.setPublic(((LimitedCompany) data).isPublic());
//                }
//                case LimitedLiabilityPartnership llp -> {
//                    llp.setNumberOfPartners(((LimitedLiabilityPartnership) data).getNumberOfPartners());
//                }
//                case NonProfitOrganization nop -> {
//                }
//                default -> throw new IllegalArgumentException("Incorrect data type");
//            }
//            repo.save(company);
//        }
//    }
//}
