package rca.ne.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rca.ne.server.models.Link;

import java.util.Optional;

@Repository
public interface LinkRepository extends JpaRepository<Link,Long> {
    //find a link by website name and link name
    Optional<Link> findByWebsiteNameAndLinkName(Long websiteId, String linkName);

}
