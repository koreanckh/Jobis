package co.jobis.repository;

import co.jobis.entity.Scrap002Entity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Scrap002Repository extends JpaRepository<Scrap002Entity, String> {

    public Scrap002Entity findByUserId(String userId);

}
