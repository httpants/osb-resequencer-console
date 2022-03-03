package au.com.cyberavenue.osb.resequencer.repository.seqretryprocessor;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import au.com.cyberavenue.osb.resequencer.entity.seqretryprocessor.GlobalConfig;

@Repository
public interface GlobalConfigRepository extends JpaRepository<GlobalConfig, String> {

    @Query("SELECT gc FROM GlobalConfig gc WHERE id = '" + GlobalConfig.ID + "'")
    Optional<GlobalConfig> getGlobalConfig();
}
