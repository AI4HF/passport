package io.passport.server.service;

import io.passport.server.model.SoftwareAgent;
import io.passport.server.repository.SoftwareAgentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service class for software agent management.
 */
@Service
public class SoftwareAgentService {

    /**
     * SoftwareAgent repo access for database management.
     */
    private final SoftwareAgentRepository softwareAgentRepository;

    @Autowired
    public SoftwareAgentService(SoftwareAgentRepository softwareAgentRepository) {
        this.softwareAgentRepository = softwareAgentRepository;
    }

    /**
     * Get all software agents
     */
    public List<SoftwareAgent> getAllSoftwareAgents() {
        return softwareAgentRepository.findAll();
    }

    /**
     * Find a software agent by softwareAgentId
     * @param softwareAgentId ID of the software agent
     * @return
     */
    public Optional<SoftwareAgent> findSoftwareAgentById(String softwareAgentId) {
        return softwareAgentRepository.findById(softwareAgentId);
    }

    /**
     * Find the software agent bound to a Keycloak service-account client.
     * @param keycloakClientId Client id carried by the machine-to-machine access token
     * @return
     */
    public Optional<SoftwareAgent> findSoftwareAgentByKeycloakClientId(String keycloakClientId) {
        return softwareAgentRepository.findByKeycloakClientId(keycloakClientId);
    }

    /**
     * Save a software agent
     * @param softwareAgent software agent to be saved
     * @return
     */
    public SoftwareAgent saveSoftwareAgent(SoftwareAgent softwareAgent) {
        return softwareAgentRepository.save(softwareAgent);
    }

    /**
     * Update a software agent
     * @param softwareAgentId ID of the software agent
     * @param updatedSoftwareAgent software agent to be updated
     * @return
     */
    public Optional<SoftwareAgent> updateSoftwareAgent(String softwareAgentId, SoftwareAgent updatedSoftwareAgent) {
        Optional<SoftwareAgent> oldSoftwareAgent = softwareAgentRepository.findById(softwareAgentId);
        if (oldSoftwareAgent.isPresent()) {
            SoftwareAgent softwareAgent = oldSoftwareAgent.get();
            softwareAgent.setName(updatedSoftwareAgent.getName());
            softwareAgent.setVersion(updatedSoftwareAgent.getVersion());
            softwareAgent.setDescription(updatedSoftwareAgent.getDescription());
            softwareAgent.setKeycloakClientId(updatedSoftwareAgent.getKeycloakClientId());
            SoftwareAgent savedSoftwareAgent = softwareAgentRepository.save(softwareAgent);
            return Optional.of(savedSoftwareAgent);
        } else {
            return Optional.empty();
        }
    }

    /**
     * Delete a software agent
     * @param softwareAgentId ID of the software agent to be deleted
     * @return
     */
    public boolean deleteSoftwareAgent(String softwareAgentId) {
        if (softwareAgentRepository.existsById(softwareAgentId)) {
            softwareAgentRepository.deleteById(softwareAgentId);
            return true;
        } else {
            return false;
        }
    }
}
