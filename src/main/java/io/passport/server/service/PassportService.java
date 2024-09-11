package io.passport.server.service;

import io.passport.server.model.*;
import io.passport.server.repository.PassportDetailsRepository;
import io.passport.server.repository.PassportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service class for passport management.
 */
@Service
public class PassportService {

    /**
     * Passport repo access for database management.
     */
    private final PassportRepository passportRepository;
    private PassportDetailsRepository passportDetailsRepository;
    @Autowired
    private ModelDeploymentService deploymentService;

    @Autowired
    private DeploymentEnvironmentService environmentService;

    @Autowired
    private ModelService modelService;

    @Autowired
    private StudyService studyService;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private PopulationService populationService;

    @Autowired
    private SurveyService surveyService;

    @Autowired
    private ExperimentService experimentService;

    @Autowired
    private FeatureSetService featureSetService;

    @Autowired
    private FeatureService featureService;

    @Autowired
    private DatasetService datasetService;

    @Autowired
    private LearningDatasetService learningDatasetService;

    @Autowired
    private LearningProcessService learningProcessService;

    @Autowired
    private LearningStageService learningStageService;


    @Autowired
    public PassportService(PassportRepository passportRepository) {
        this.passportRepository = passportRepository;
    }

    /**
     * Return all passports
     * @return
     */
    public List<Passport> getAllPassports() {
        return passportRepository.findAll();
    }

    /**
     * Find a passport by passportId
     * @param passportId ID of the passport
     * @return
     */
    public Optional<Passport> findPassportByPassportId(Long passportId) {
        return passportRepository.findById(passportId);
    }

    /**
     * Find a passport by studyId
     * @param studyId ID of the related study
     * @return
     */
    public List<Passport> findPassportsByStudyId(Long studyId) {
        return passportRepository.findAllByStudyId(studyId);
    }

    /**
     * Save a passport
     * @param passport passport to be saved
     * @return
     */
    public Passport savePassport(Passport passport) {
        Instant now = Instant.now();
        passport.setCreatedAt(now);
        passport.setApprovedAt(now);

        return passportRepository.save(passport);
    }

    /**
     * Delete a passport
     * @param passportId ID of passport to be deleted
     * @return
     */
    public boolean deletePassport(Long passportId) {
        if(passportRepository.existsById(passportId)) {
            passportRepository.deleteById(passportId);
            return true;
        }else{
            return false;
        }
    }

    public void savePassportDetails(PassportDetailsDTO passportDetailsDTO) {
        passportDetailsRepository.save(passportDetailsDTO);
    }

    public Optional<PassportDetailsDTO> findPassportDetailsByPassportId(Long passportId) {
        return passportDetailsRepository.findByPassport_PassportId(passportId);
    }

    /**
     * Creates and stores Passport with detailsJson populated.
     *
     * @param passport The passport object with basic info (deploymentId, studyId, etc.).
     * @return The saved Passport.
     */
    public Passport createPassport(Passport passport) {
        // Populate the detailsJson field with the additional information
        Map<String, Object> detailsJson = new HashMap<>();
        detailsJson.put("deploymentDetails", fetchDeploymentDetails(passport));
        detailsJson.put("environmentDetails", fetchEnvironmentDetails(passport));
        detailsJson.put("modelDetails", fetchModelDetails(passport));
        detailsJson.put("studyDetails", fetchStudyDetails(passport));
        detailsJson.put("parameters", fetchParameters(passport));
        detailsJson.put("populationDetails", fetchPopulationDetails(passport));
        detailsJson.put("surveys", fetchSurveys(passport));
        detailsJson.put("experiments", fetchExperiments(passport));
        detailsJson.put("featureSetsWithFeatures", fetchFeatureSetsWithFeatures(passport));
        detailsJson.put("datasetsWithLearningDatasets", fetchDatasetsWithLearningDatasets(passport));
        detailsJson.put("learningProcessesWithStages", fetchLearningProcessesWithStages(passport));

        // Set the detailsJson in the passport entity
        passport.setDetailsJson(detailsJson);

        // Set timestamps and save passport
        passport.setCreatedAt(Instant.now());
        passport.setApprovedAt(Instant.now());

        return passportRepository.save(passport);
    }

    /**
     * Fetch Passport by ID.
     */
    public Passport getPassportById(Long passportId) {
        return passportRepository.findById(passportId)
                .orElseThrow(() -> new RuntimeException("Passport not found"));
    }


    private ModelDeployment fetchDeploymentDetails(Passport passport) {
        return deploymentService.findModelDeploymentByDeploymentId(passport.getDeploymentId()).orElseThrow(() -> new RuntimeException("Study not found"));
    }

    private DeploymentEnvironment fetchEnvironmentDetails(Passport passport) {
        return environmentService.findDeploymentEnvironmentById(deploymentService.findModelDeploymentByDeploymentId(passport.getDeploymentId()).orElseThrow(() -> new RuntimeException("Study not found")).getEnvironmentId()).orElseThrow(()->new RuntimeException("Study not found"));
    }

    private Model fetchModelDetails(Passport passport) {
        return modelService.findModelById(deploymentService.findModelDeploymentByDeploymentId(passport.getDeploymentId()).orElseThrow(() -> new RuntimeException("Study not found")).getModelId()).orElseThrow(()->new RuntimeException("Study not found"));
    }

    private Study fetchStudyDetails(Passport passport) {
        return studyService.findStudyByStudyId(passport.getStudyId()).orElseThrow(() -> new RuntimeException("Study not found"));
    }

    private List<Parameter> fetchParameters(Passport passport) {
        return parameterService.findParametersByStudyId(passport.getStudyId());
    }

    private List<Population> fetchPopulationDetails(Passport passport) {
        return populationService.findPopulationByStudyId(passport.getStudyId());
    }

    private List<Survey> fetchSurveys(Passport passport) {
        return surveyService.findSurveysByStudyId(passport.getStudyId());
    }

    private List<Experiment> fetchExperiments(Passport passport) {
        return experimentService.findExperimentByStudyId(passport.getStudyId());
    }

    private List<Map<String, Object>> fetchFeatureSetsWithFeatures(Passport passport) {
        List<FeatureSet> featureSets = featureSetService.getAllFeatureSetsByStudyId(passport.getStudyId());
        return featureSets.stream()
                .map(featureSet -> {
                    Map<String, Object> featureSetWithFeatures = new HashMap<>();
                    featureSetWithFeatures.put("featureSet", featureSet);
                    featureSetWithFeatures.put("features", featureService.findByFeaturesetId(featureSet.getFeaturesetId()));
                    return featureSetWithFeatures;
                })
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> fetchDatasetsWithLearningDatasets(Passport passport) {
        List<Dataset> datasets = datasetService.getAllDatasetsByStudyId(passport.getStudyId());
        return datasets.stream()
                .map(dataset -> {
                    Map<String, Object> datasetWithLearningDatasets = new HashMap<>();
                    datasetWithLearningDatasets.put("dataset", dataset);
                    datasetWithLearningDatasets.put("learningDatasets", learningDatasetService.findByDatasetId(dataset.getDatasetId()));
                    return datasetWithLearningDatasets;
                })
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> fetchLearningProcessesWithStages(Passport passport) {
        List<LearningProcess> learningProcesses = learningProcessService.getAllLearningProcessByStudyId(passport.getStudyId());
        return learningProcesses.stream()
                .map(learningProcess -> {
                    Map<String, Object> learningProcessWithStages = new HashMap<>();
                    learningProcessWithStages.put("learningProcess", learningProcess);
                    learningProcessWithStages.put("learningStages", learningStageService.findLearningStagesByProcessId(learningProcess.getLearningProcessId()));
                    return learningProcessWithStages;
                })
                .collect(Collectors.toList());
    }
}
