package io.passport.server.repository;

import io.passport.server.model.Passport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PassportRepository extends JpaRepository<Passport, Long> {
    List<Passport> findAllByStudyId(Long studyId);
}
