package co.jobis.repository;

import co.jobis.entity.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<MemberEntity, String> {

    MemberEntity findByUserId(String userId);
    Optional<MemberEntity> findByUserIdAndPassword(String userId, String password);

}
