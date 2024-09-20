package io.passport.server.service;

import io.passport.server.model.*;
import io.passport.server.repository.PassportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    /**
     * Passport pdf generation data.
     */
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
     * Find a passport by studyId
     * @param studyId ID of the related study
     * @return
     */
    public List<Passport> findPassportsByStudyId(Long studyId) {
        return passportRepository.findAllByStudyId(studyId);
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

    /**
     * Creates and stores Passport with detailsJson populated.
     *
     * @param passport The passport object with basic info (deploymentId, studyId, etc.).
     * @return The saved Passport.
     */
    public Passport createPassport(Passport passport) {
        try {
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

            passport.setDetailsJson(detailsJson);
            passport.setCreatedAt(Instant.now());
            passport.setApprovedAt(Instant.now());

            return passportRepository.save(passport);
        } catch (RuntimeException e) {
            throw new RuntimeException("Error creating passport: " + e.getMessage());
        }
    }


    /**
     * Fetch Passport by ID.
     */
    public Passport getPassportById(Long passportId) {
        return passportRepository.findById(passportId)
                .orElseThrow(() -> new RuntimeException("Passport not found"));
    }

    /**
     * Fetch methods to obtain pdf generation data
     */
    private ModelDeployment fetchDeploymentDetails(Passport passport) {
        try {
            return deploymentService.findModelDeploymentByDeploymentId(passport.getDeploymentId())
                    .orElseThrow(() -> new RuntimeException("Model Deployment not found"));
        } catch (RuntimeException e) {
            System.err.println("Error fetching Model Deployment: " + e.getMessage());
            throw e;
        }
    }

    private DeploymentEnvironment fetchEnvironmentDetails(Passport passport) {
        try {
            ModelDeployment deployment = deploymentService.findModelDeploymentByDeploymentId(passport.getDeploymentId())
                    .orElseThrow(() -> new RuntimeException("Model Deployment not found"));
            return environmentService.findDeploymentEnvironmentById(deployment.getEnvironmentId())
                    .orElseThrow(() -> new RuntimeException("Deployment Environment not found"));
        } catch (RuntimeException e) {
            System.err.println("Error fetching Deployment Environment: " + e.getMessage());
            throw e;
        }
    }

    private Model fetchModelDetails(Passport passport) {
        try {
            ModelDeployment deployment = deploymentService.findModelDeploymentByDeploymentId(passport.getDeploymentId())
                    .orElseThrow(() -> new RuntimeException("Model Deployment not found"));
            return modelService.findModelById(deployment.getModelId())
                    .orElseThrow(() -> new RuntimeException("Model not found"));
        } catch (RuntimeException e) {
            throw new RuntimeException("Error fetching Model: " + e.getMessage());
        }
    }

    private Study fetchStudyDetails(Passport passport) {
        try {
            return studyService.findStudyByStudyId(passport.getStudyId())
                    .orElseThrow(() -> new RuntimeException("Study not found"));
        } catch (RuntimeException e) {
            throw new RuntimeException("Error fetching Study: " + e.getMessage());
        }
    }

    private List<Parameter> fetchParameters(Passport passport) {
        try {
            return parameterService.findParametersByStudyId(passport.getStudyId());
        } catch (RuntimeException e) {
            throw new RuntimeException("Error fetching Parameters: " + e.getMessage());
        }
    }

    private List<Population> fetchPopulationDetails(Passport passport) {
        try {
            return populationService.findPopulationByStudyId(passport.getStudyId());
        } catch (RuntimeException e) {
            throw new RuntimeException("Error fetching Population details: " + e.getMessage());
        }
    }

    private List<Survey> fetchSurveys(Passport passport) {
        try {
            return surveyService.findSurveysByStudyId(passport.getStudyId());
        } catch (RuntimeException e) {
            throw new RuntimeException("Error fetching Surveys: " + e.getMessage());
        }
    }

    private List<Experiment> fetchExperiments(Passport passport) {
        try {
            return experimentService.findExperimentByStudyId(passport.getStudyId());
        } catch (RuntimeException e) {
            throw new RuntimeException("Error fetching Experiments: " + e.getMessage());
        }
    }

    private List<Map<String, Object>> fetchFeatureSetsWithFeatures(Passport passport) {
        try {
            List<FeatureSet> featureSets = featureSetService.getAllFeatureSetsByStudyId(passport.getStudyId());
            return featureSets.stream()
                    .map(featureSet -> {
                        Map<String, Object> featureSetWithFeatures = new HashMap<>();
                        featureSetWithFeatures.put("featureSet", featureSet);
                        featureSetWithFeatures.put("features", featureService.findByFeaturesetId(featureSet.getFeaturesetId()));
                        return featureSetWithFeatures;
                    })
                    .collect(Collectors.toList());
        } catch (RuntimeException e) {
            throw new RuntimeException("Error fetching Feature Sets and Features: " + e.getMessage());
        }
    }

    private List<Map<String, Object>> fetchDatasetsWithLearningDatasets(Passport passport) {
        try {
            List<Dataset> datasets = datasetService.getAllDatasetsByStudyId(passport.getStudyId());
            return datasets.stream()
                    .map(dataset -> {
                        Map<String, Object> datasetWithLearningDatasets = new HashMap<>();
                        datasetWithLearningDatasets.put("dataset", dataset);
                        datasetWithLearningDatasets.put("learningDatasets", learningDatasetService.findByDatasetId(dataset.getDatasetId()));
                        return datasetWithLearningDatasets;
                    })
                    .collect(Collectors.toList());
        } catch (RuntimeException e) {
            throw new RuntimeException("Error fetching Datasets and Learning Datasets: " + e.getMessage());
        }
    }

    private List<Map<String, Object>> fetchLearningProcessesWithStages(Passport passport) {
        try {
            List<LearningProcess> learningProcesses = learningProcessService.getAllLearningProcessByStudyId(passport.getStudyId());
            return learningProcesses.stream()
                    .map(learningProcess -> {
                        Map<String, Object> learningProcessWithStages = new HashMap<>();
                        learningProcessWithStages.put("learningProcess", learningProcess);
                        learningProcessWithStages.put("learningStages", learningStageService.findLearningStagesByProcessId(learningProcess.getLearningProcessId()));
                        return learningProcessWithStages;
                    })
                    .collect(Collectors.toList());
        } catch (RuntimeException e) {
            throw new RuntimeException("Error fetching Learning Processes and Stages: " + e.getMessage());
        }
    }

}
