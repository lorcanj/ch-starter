package uk.gov.companieshouse.docsapp.controller.company;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.companieshouse.docsapp.dao.CompanyRegistry;
import uk.gov.companieshouse.docsapp.model.company.Company;

import java.util.List;
import java.util.Objects;

@RestController
public class CompanyController {

    @Autowired
    CompanyRegistry companyRegistry;

    @GetMapping("/companies")
    public ResponseEntity<List<Company>> getAllCompanies(@RequestParam(required = false) String namePattern, @RequestParam(required = false) Integer yearOfIncorporation, @RequestParam(required = false) Boolean activeState,
            @RequestParam(required = false) CompanyRegistry.Sort sortFilter, @RequestParam(required = false) CompanyRegistry.Type typeFilter) {
        return ResponseEntity.ok(companyRegistry.getCompanies(namePattern, yearOfIncorporation, activeState, typeFilter, sortFilter));
    }

    @PostMapping("/companies")
    public ResponseEntity<Company> createCompany(@RequestBody(required = true) Company company) {
        if (company.getRegistrationNumber() != null || company.getIncorporatedOn() != null){
            return ResponseEntity.notFound().build();
        }

        companyRegistry.addCompany(company);
        return ResponseEntity.ok(company);
    }

    @GetMapping("/companies/{companyNumber}")
    public ResponseEntity<Company> getCompany(@PathVariable(required = true) String companyNumber) {
        Company foundCompany = companyRegistry.getCompany(companyNumber);

        if (foundCompany == null)
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(foundCompany);
    }

    @PutMapping("/companies/{companyNumber}")
    public ResponseEntity<Void> putCompany(@PathVariable(required = true) String companyNumber, @RequestBody(required = true) Company company) {
        if (!Objects.equals(companyNumber, company.getRegistrationNumber()))
            return ResponseEntity.badRequest().build();

        Company foundCompany = companyRegistry.getCompany(companyNumber);
        if (foundCompany == null)
            return ResponseEntity.notFound().build();

        // is bad request
        if (foundCompany.getIncorporatedOn() != company.getIncorporatedOn())
            return ResponseEntity.status(400).build();

        companyRegistry.editCompany(companyNumber, company);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/companies/{companyNumber}")
    public ResponseEntity<Void> patchCompany(@PathVariable(required = true) String companyNumber, @RequestBody(required = true) Company company) {
        if (company.getIncorporatedOn() != null || company.getRegistrationNumber() != null)
            return ResponseEntity.badRequest().build();

        Company foundCompany = companyRegistry.getCompany(companyNumber);

        if (foundCompany == null)
            return ResponseEntity.notFound().build();

        companyRegistry.patchCompany(companyNumber, company);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/companies/{companyNumber}")
    public ResponseEntity<Void> deleteCompany(@PathVariable(required = true) String companyNumber) {
        Company foundCompany = companyRegistry.getCompany(companyNumber);
        if (foundCompany == null)
            return ResponseEntity.notFound().build();

        companyRegistry.deleteCompany(companyNumber);
        return ResponseEntity.noContent().build();
    }
}