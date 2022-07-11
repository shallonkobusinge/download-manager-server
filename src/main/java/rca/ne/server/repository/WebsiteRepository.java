package rca.ne.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rca.ne.server.models.Website;

import java.util.Optional;

@Repository
public interface WebsiteRepository extends JpaRepository<Website, Long> {
    Optional<Website> findByName(String name);

}

