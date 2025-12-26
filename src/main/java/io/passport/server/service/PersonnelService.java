package io.passport.server.service;

import io.passport.server.model.*;
import io.passport.server.repository.PersonnelRepository;
import io.passport.server.repository.StudyOrganizationRepository;
import io.passport.server.repository.StudyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service class for personnel management.
 */
@Service
public class PersonnelService {

    /**
     * Personnel repo access for database management.
     */
    private final PersonnelRepository personnelRepository;

    /**
     * Keycloak service for keycloak user management.
     */
    private final KeycloakService keycloakService;
    private final StudyOrganizationRepository studyOrganizationRepository;
    private final StudyRepository studyRepository;

    /**
     * Lazy service references for limited use in cascade validation
     */
    @Autowired @Lazy private FeatureSetService featureSetService;
    @Autowired @Lazy private FeatureService featureService;
    @Autowired @Lazy private DatasetService datasetService;
    @Autowired @Lazy private DatasetTransformationStepService datasetTransformationStepService;
    @Autowired @Lazy private ModelService modelService;
    @Autowired @Lazy private ModelDeploymentService modelDeploymentService;
    @Autowired @Lazy private PassportService passportService;

    @Autowired
    public PersonnelService(PersonnelRepository personnelRepository,
                            KeycloakService keycloakService,
                            StudyOrganizationRepository studyOrganizationRepository,
                            StudyRepository studyRepository) {
        this.personnelRepository = personnelRepository;
        this.keycloakService = keycloakService;
        this.studyOrganizationRepository = studyOrganizationRepository;
        this.studyRepository = studyRepository;
    }

    /**
     * Deletes a personnel with reassignment logic.
     */
    @Transactional
    public boolean deletePersonnelWithReassignment(String personnelId) {

        if (studyRepository.existsByOwner(personnelId)) {
            throw new IllegalStateException("Deletion failed: User is the Owner of one or more studies.");
        }
        if (studyOrganizationRepository.existsByResponsiblePersonnelId(personnelId)) {
            throw new IllegalStateException("Deletion failed: User is assigned as Responsible Personnel for one or more studies.");
        }

        Optional<Personnel> personnelOpt = personnelRepository.findById(personnelId);
        if (personnelOpt.isEmpty()) {
            return false;
        }
        String organizationId = personnelOpt.get().getOrganizationId();

        List<FeatureSet> featureSets = featureSetService.findByCreatedByOrLastUpdatedBy(personnelId);
        for (FeatureSet fs : featureSets) {
            reassignFeatureSet(fs, personnelId, organizationId);
        }

        List<Feature> features = featureService.findByCreatedByOrLastUpdatedBy(personnelId);
        for (Feature f : features) {
            reassignFeature(f, personnelId, organizationId);
        }

        List<Dataset> datasets = datasetService.findByCreatedByOrLastUpdatedBy(personnelId);
        for (Dataset ds : datasets) {
            reassignDataset(ds, personnelId, organizationId);
        }

        List<DatasetTransformationStep> steps = datasetTransformationStepService.findByCreatedByOrLastUpdatedBy(personnelId);
        for (DatasetTransformationStep step : steps) {
            reassignDatasetTransformationStep(step, personnelId, organizationId);
        }

        List<Model> models = modelService.findByCreatedByOrLastUpdatedBy(personnelId);
        for (Model m : models) {
            reassignModel(m, personnelId, organizationId);
        }

        List<ModelDeployment> deployments = modelDeploymentService.findByCreatedByOrLastUpdatedBy(personnelId);
        for (ModelDeployment md : deployments) {
            reassignModelDeployment(md, personnelId, organizationId);
        }

        List<Passport> passports = passportService.findByCreatedByOrApprovedBy(personnelId);
        for (Passport p : passports) {
            reassignPassport(p, personnelId, organizationId);
        }

        boolean isKeycloakUserDeleted = keycloakService.deleteUser(personnelId);
        if (isKeycloakUserDeleted) {
            personnelRepository.deleteById(personnelId);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Helper to reassign a Passport to the responsible personnel.
     */
    private void reassignPassport(Passport p, String deletedPersonnelId, String organizationId) {
        Optional<String> studyIdOpt = passportService.findStudyIdByPassportId(p.getPassportId());

        if (studyIdOpt.isEmpty()) return;
        String studyId = studyIdOpt.get();

        Optional<StudyOrganization> studyOrgOpt = studyOrganizationRepository
                .findByStudyIdAndOrganizationId(studyId, organizationId);

        if (studyOrgOpt.isPresent() && studyOrgOpt.get().getResponsiblePersonnelId() != null) {
            String newResponsibleId = studyOrgOpt.get().getResponsiblePersonnelId();

            if (newResponsibleId.equals(deletedPersonnelId)) return;

            boolean updated = false;
            if (deletedPersonnelId.equals(p.getCreatedBy())) {
                p.setCreatedBy(newResponsibleId);
                updated = true;
            }
            if (deletedPersonnelId.equals(p.getApprovedBy())) {
                p.setApprovedBy(newResponsibleId);
                updated = true;
            }

            if (updated) {
                passportService.savePassport(p);
            }
        }
    }

    /**
     * Helper to reassign a Feature Set to the responsible personnel.
     */
    private void reassignFeatureSet(FeatureSet fs, String deletedPersonnelId, String organizationId) {
        String studyId = featureSetService.findStudyIdByFeatureSetId(fs.getFeaturesetId());

        Optional<StudyOrganization> studyOrgOpt = studyOrganizationRepository.findByStudyIdAndOrganizationId(studyId, organizationId);
        if (studyOrgOpt.isPresent() && studyOrgOpt.get().getResponsiblePersonnelId() != null) {
            String newResponsibleId = studyOrgOpt.get().getResponsiblePersonnelId();
            if (newResponsibleId.equals(deletedPersonnelId)) return;

            boolean updated = false;
            if (deletedPersonnelId.equals(fs.getCreatedBy())) { fs.setCreatedBy(newResponsibleId); updated = true; }
            if (deletedPersonnelId.equals(fs.getLastUpdatedBy())) { fs.setLastUpdatedBy(newResponsibleId); updated = true; }
            if (updated) featureSetService.saveFeatureSet(fs);
        }
    }

    /**
     * Helper to reassign a Feature the responsible personnel.
     */
    private void reassignFeature(Feature f, String deletedPersonnelId, String organizationId) {
        Optional<String> studyIdOpt = featureService.findStudyIdByFeatureId(f.getFeatureId());
        if (studyIdOpt.isEmpty()) return;
        String studyId = studyIdOpt.get();

        Optional<StudyOrganization> studyOrgOpt = studyOrganizationRepository.findByStudyIdAndOrganizationId(studyId, organizationId);
        if (studyOrgOpt.isPresent() && studyOrgOpt.get().getResponsiblePersonnelId() != null) {
            String newResponsibleId = studyOrgOpt.get().getResponsiblePersonnelId();
            if (newResponsibleId.equals(deletedPersonnelId)) return;

            boolean updated = false;
            if (deletedPersonnelId.equals(f.getCreatedBy())) { f.setCreatedBy(newResponsibleId); updated = true; }
            if (deletedPersonnelId.equals(f.getLastUpdatedBy())) { f.setLastUpdatedBy(newResponsibleId); updated = true; }
            if (updated) featureService.saveFeature(f);
        }
    }

    /**
     * Helper to reassign a Dataset to the responsible personnel.
     */
    private void reassignDataset(Dataset ds, String deletedPersonnelId, String organizationId) {
        Optional<String> studyIdOpt = datasetService.findStudyIdByDatasetId(ds.getDatasetId());
        if (studyIdOpt.isEmpty()) return;
        String studyId = studyIdOpt.get();

        Optional<StudyOrganization> studyOrgOpt = studyOrganizationRepository.findByStudyIdAndOrganizationId(studyId, organizationId);
        if (studyOrgOpt.isPresent() && studyOrgOpt.get().getResponsiblePersonnelId() != null) {
            String newResponsibleId = studyOrgOpt.get().getResponsiblePersonnelId();
            if (newResponsibleId.equals(deletedPersonnelId)) return;

            boolean updated = false;
            if (deletedPersonnelId.equals(ds.getCreatedBy())) { ds.setCreatedBy(newResponsibleId); updated = true; }
            if (deletedPersonnelId.equals(ds.getLastUpdatedBy())) { ds.setLastUpdatedBy(newResponsibleId); updated = true; }
            if (updated) datasetService.saveDataset(ds, newResponsibleId);
        }
    }

    /**
     * Helper to reassign a Transformation Step to the responsible personnel.
     */
    private void reassignDatasetTransformationStep(DatasetTransformationStep step, String deletedPersonnelId, String organizationId) {
        Optional<String> studyIdOpt = datasetTransformationStepService.findStudyIdByStepId(step.getStepId());
        if (studyIdOpt.isEmpty()) return;
        String studyId = studyIdOpt.get();

        Optional<StudyOrganization> studyOrgOpt = studyOrganizationRepository.findByStudyIdAndOrganizationId(studyId, organizationId);
        if (studyOrgOpt.isPresent() && studyOrgOpt.get().getResponsiblePersonnelId() != null) {
            String newResponsibleId = studyOrgOpt.get().getResponsiblePersonnelId();
            if (newResponsibleId.equals(deletedPersonnelId)) return;

            boolean updated = false;
            if (deletedPersonnelId.equals(step.getCreatedBy())) { step.setCreatedBy(newResponsibleId); updated = true; }
            if (deletedPersonnelId.equals(step.getLastUpdatedBy())) { step.setLastUpdatedBy(newResponsibleId); updated = true; }
            if (updated) datasetTransformationStepService.saveDatasetTransformationStep(step);
        }
    }

    /**
     * Helper to reassign a Model to the responsible personnel.
     */
    private void reassignModel(Model m, String deletedPersonnelId, String organizationId) {
        Optional<String> studyIdOpt = modelService.findStudyIdByModelId(m.getModelId());

        if (studyIdOpt.isEmpty()) return;
        String studyId = studyIdOpt.get();

        Optional<StudyOrganization> studyOrgOpt = studyOrganizationRepository
                .findByStudyIdAndOrganizationId(studyId, organizationId);

        if (studyOrgOpt.isPresent() && studyOrgOpt.get().getResponsiblePersonnelId() != null) {
            String newResponsibleId = studyOrgOpt.get().getResponsiblePersonnelId();

            if (newResponsibleId.equals(deletedPersonnelId)) return;

            boolean updated = false;
            if (deletedPersonnelId.equals(m.getCreatedBy())) {
                m.setCreatedBy(newResponsibleId);
                updated = true;
            }
            if (deletedPersonnelId.equals(m.getLastUpdatedBy())) {
                m.setLastUpdatedBy(newResponsibleId);
                updated = true;
            }

            if (updated) {
                modelService.saveModel(m);
            }
        }
    }

    /**
     * Helper to reassign a Model Deployment to the responsible personnel.
     */
    private void reassignModelDeployment(ModelDeployment md, String deletedPersonnelId, String organizationId) {
        Optional<String> studyIdOpt = modelDeploymentService.findStudyIdByDeploymentId(md.getDeploymentId());

        if (studyIdOpt.isEmpty()) return;
        String studyId = studyIdOpt.get();

        Optional<StudyOrganization> studyOrgOpt = studyOrganizationRepository
                .findByStudyIdAndOrganizationId(studyId, organizationId);

        if (studyOrgOpt.isPresent() && studyOrgOpt.get().getResponsiblePersonnelId() != null) {
            String newResponsibleId = studyOrgOpt.get().getResponsiblePersonnelId();

            if (newResponsibleId.equals(deletedPersonnelId)) return;

            boolean updated = false;
            if (deletedPersonnelId.equals(md.getCreatedBy())) {
                md.setCreatedBy(newResponsibleId);
                updated = true;
            }
            if (deletedPersonnelId.equals(md.getLastUpdatedBy())) {
                md.setLastUpdatedBy(newResponsibleId);
                updated = true;
            }

            if (updated) {
                modelDeploymentService.saveModelDeployment(md);
            }
        }
    }

    /**
     * Get all personnel
     */
    public List<Personnel> getAllPersonnel() {
        return personnelRepository.findAll();
    }

    /**
     * Find a personnel by personnelId
     * @param personnelId ID of the personnel
     * @return
     */
    public Optional<Personnel> findPersonnelById(String personnelId) {
        return personnelRepository.findById(personnelId);
    }

    /**
     * Find personnel by organizationId
     * @param organizationId ID of the organization
     * @return
     */
    public List<Personnel> findPersonnelByOrganizationId(String organizationId) {
        return personnelRepository.findByOrganizationId(organizationId);
    }

    /**
     * Save a personnel
     * @param personnelDTO personnel to be saved
     * @return
     */
    public Optional<Personnel> savePersonnel(PersonnelDTO personnelDTO) {
        Optional<String> keycloakUserId;
        if(personnelDTO.getIsStudyOwner()){
            keycloakUserId = this.keycloakService
                    .createUserAndReturnId(personnelDTO.getCredentials().username, personnelDTO.getCredentials().password, Role.STUDY_OWNER);
        }
        else {
            keycloakUserId = this.keycloakService
                    .createUserAndReturnId(personnelDTO.getCredentials().username, personnelDTO.getCredentials().password, null);
        }
        if(keycloakUserId.isPresent()) {
            Personnel personnel = personnelDTO.getPersonnel();
            personnel.setPersonId(keycloakUserId.get());
            Personnel savedPersonnel = personnelRepository.save(personnel);
            return Optional.of(savedPersonnel);
        }else{
            return Optional.empty();
        }
    }

    /**
     * Update a personnel
     * @param personnelId ID of the personnel
     * @param updatedPersonnel personnel to be updated
     * @return
     */
    public Optional<Personnel> updatePersonnel(String personnelId, Personnel updatedPersonnel) {
        Optional<Personnel> oldPersonnel = personnelRepository.findById(personnelId);
        if (oldPersonnel.isPresent()) {
            Personnel personnel = oldPersonnel.get();
            personnel.setFirstName(updatedPersonnel.getFirstName());
            personnel.setLastName(updatedPersonnel.getLastName());
            personnel.setEmail(updatedPersonnel.getEmail());
            personnel.setOrganizationId(updatedPersonnel.getOrganizationId());
            Personnel savedPersonnel = personnelRepository.save(personnel);
            return Optional.of(savedPersonnel);
        } else {
            return Optional.empty();
        }
    }
}
