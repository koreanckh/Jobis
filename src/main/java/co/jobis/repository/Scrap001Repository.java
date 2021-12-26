package co.jobis.repository;

import co.jobis.entity.Scrap001Entity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Scrap001Repository extends JpaRepository<Scrap001Entity, String> {

    public Scrap001Entity findByUserId(String userId);

}
