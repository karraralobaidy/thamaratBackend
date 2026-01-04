package com.earn.earnmoney.Service;
// package com.earn.earnmoney.Service;



// import com.earn.earnmoney.model.Packages;
// import com.earn.earnmoney.repo.PackagesRep;
// import lombok.RequiredArgsConstructor;
// import org.springframework.data.domain.Page;
// import org.springframework.data.domain.PageRequest;
// import org.springframework.data.domain.Pageable;
// import org.springframework.data.domain.Sort;
// import org.springframework.data.jpa.domain.JpaSort;
// import org.springframework.stereotype.Service;

// import java.util.List;
// import java.util.Optional;

// @Service
// @RequiredArgsConstructor
// public class PackagesImpl implements PackagesService {

//     private final PackagesRep packagesRep;

//     @Override
//     public Packages savePackages(Packages user) {
//         return packagesRep.save(user);
//     }

//     @Override
//     public Page<Packages> getAllPackagess(int page, int size, String search) {
//         Pageable pageable = PageRequest.of(page,size, JpaSort.unsafe(Sort.Direction.ASC, "CAST(income AS integer)"));
//         return packagesRep.findByQuery(search,pageable);
//     }
//     @Override
//     public List<Packages> getAllPackagess2() {
//         return packagesRep.findAll();
//     }


//     @Override
//     public Optional<Packages> getPackagesById(Long id) {
//         return packagesRep.findById(id);
//     }

//     @Override
//     public void deletePackages(Long id) {
//          packagesRep.deleteById(id);
//     }

//     @Override
//     public Packages updatePackages(Packages user) {
//         return packagesRep.saveAndFlush(user);
//     }

//     @Override
//     public Packages getPackagesByIncome(String income) {
//         return packagesRep.findByIncome(income);
//     }
// }
