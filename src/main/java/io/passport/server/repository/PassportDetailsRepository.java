package io.passport.server.repository;

import io.passport.server.model.PassportDetailsDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PassportDetailsRepository extends JpaRepository<PassportDetailsDTO, Long> {

    Optional<PassportDetailsDTO> findByPassport_PassportId(Long passportId);
}
