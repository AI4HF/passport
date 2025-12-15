package io.passport.server.service;

import io.passport.server.model.*;
import io.passport.server.repository.PassportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.lang.reflect.Field;
import java.util.*;
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
    private LinkedArticleService linkedArticleService;

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
    private EvaluationMeasureService evaluationMeasureService;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private ModelFigureService modelFigureService;


    @Autowired
    public PassportService(PassportRepository passportRepository) {
        this.passportRepository = passportRepository;
    }

    /**
     * Find a passport by studyId
     * @param studyId ID of the related study
     * @return
     */
    public List<Passport> findPassportsByStudyId(String studyId) {
        return passportRepository.findAllByStudyId(studyId);
    }

    /**
     * Delete a passport
     * @param passportId ID of passport to be deleted
     * @return
     */
    public boolean deletePassport(String passportId) {
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
     * @param passportWithDetailSelection The passport object with basic info (deploymentId, studyId, etc.) and selected details of the passport.
     * @return The saved Passport.
     */
    public Passport createPassport(PassportWithDetailSelection passportWithDetailSelection) {
        try {
            Map<String, Object> detailsJson = new HashMap<>();
            if(passportWithDetailSelection.getPassportDetailsSelection().isModelDeploymentDetails()){
                detailsJson.put("deploymentDetails", fetchDeploymentDetails(passportWithDetailSelection.getPassport()));
            }
            if(passportWithDetailSelection.getPassportDetailsSelection().isEnvironmentDetails()){
                detailsJson.put("environmentDetails", fetchEnvironmentDetails(passportWithDetailSelection.getPassport()));
            }
            if(passportWithDetailSelection.getPassportDetailsSelection().isModelDetails()){
                detailsJson.put("modelDetails", fetchModelDetails(passportWithDetailSelection.getPassport()));
            }
            if(passportWithDetailSelection.getPassportDetailsSelection().isStudyDetails()){
                detailsJson.put("studyDetails", fetchStudyDetails(passportWithDetailSelection.getPassport()));
            }
            if(passportWithDetailSelection.getPassportDetailsSelection().isParameterDetails()){
                detailsJson.put("parameters", fetchParameters(passportWithDetailSelection.getPassport()));
            }
            if(passportWithDetailSelection.getPassportDetailsSelection().isPopulationDetails()){
                detailsJson.put("populationDetails", fetchPopulationDetails(passportWithDetailSelection.getPassport()));
            }
            if(passportWithDetailSelection.getPassportDetailsSelection().isSurveyDetails()){
                detailsJson.put("surveys", fetchSurveys(passportWithDetailSelection.getPassport()));
            }
            if(passportWithDetailSelection.getPassportDetailsSelection().isExperimentDetails()){
                detailsJson.put("experiments", fetchExperiments(passportWithDetailSelection.getPassport()));
            }
            if(passportWithDetailSelection.getPassportDetailsSelection().isLinkedArticleDetails()){
                detailsJson.put("linkedArticles", fetchLinkedArticles(passportWithDetailSelection.getPassport()));
            }
            if(passportWithDetailSelection.getPassportDetailsSelection().isFeatureSets()){
                detailsJson.put("featureSetsWithFeatures", fetchFeatureSetsWithFeatures(passportWithDetailSelection.getPassport()));
            }
            if(passportWithDetailSelection.getPassportDetailsSelection().isDatasets()){
                detailsJson.put("datasetsWithLearningDatasets", fetchDatasetsWithLearningDatasets(passportWithDetailSelection.getPassport()));
            }
            if(passportWithDetailSelection.getPassportDetailsSelection().isLearningProcessDetails()){
                detailsJson.put("learningProcessesWithStages", fetchLearningProcessesWithStages(passportWithDetailSelection.getPassport()));
            }
            if(passportWithDetailSelection.getPassportDetailsSelection().isEvaluationMeasures()){
                detailsJson.put("evaluationMeasures", fetchEvaluationMeasures(passportWithDetailSelection.getPassport()));
            }
            if(passportWithDetailSelection.getPassportDetailsSelection().isModelFigures()){
                detailsJson.put("modelFigures", fetchModelFigures(passportWithDetailSelection.getPassport()));
            }
            cleanEmptyStringFieldsDeep(detailsJson, passportWithDetailSelection.getPassportDetailsSelection().isExcludeEmptyFields());
            passportWithDetailSelection.getPassport().setDetailsJson(detailsJson);
            passportWithDetailSelection.getPassport().setCreatedAt(Instant.now());
            passportWithDetailSelection.getPassport().setApprovedAt(Instant.now());

            return passportRepository.save(passportWithDetailSelection.getPassport());
        } catch (RuntimeException e) {
            throw new RuntimeException("Error creating passport: " + e.getMessage());
        }
    }


    /**
     * Fetch Passport by ID.
     */
    public Passport getPassportById(String passportId) {
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

    private ModelWithOwnerNameDTO fetchModelDetails(Passport passport) {
        try {
            ModelDeployment deployment = deploymentService.findModelDeploymentByDeploymentId(passport.getDeploymentId())
                    .orElseThrow(() -> new RuntimeException("Model Deployment not found"));
            Model model = modelService.findModelById(deployment.getModelId())
                    .orElseThrow(() -> new RuntimeException("Model not found"));
            ModelWithOwnerNameDTO modelWithOwnerNameDTO = new ModelWithOwnerNameDTO(model);
            modelWithOwnerNameDTO.setOwner(organizationService.findOrganizationById(model.getOwner()).orElseThrow().getName());
            return modelWithOwnerNameDTO;
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
    private List<LinkedArticle> fetchLinkedArticles(Passport passport) {
        try {
            return linkedArticleService.findLinkedArticleByStudyId(passport.getStudyId());
        } catch (RuntimeException e) {
            throw new RuntimeException("Error fetching Linked Articles: " + e.getMessage());
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

    private List<EvaluationMeasure> fetchEvaluationMeasures(Passport passport) {
        try {
            String modelId = this.fetchDeploymentDetails(passport).getModelId();
            return evaluationMeasureService.findEvaluationMeasuresByModelId(modelId);
        } catch (RuntimeException e) {
            throw new RuntimeException("Error fetching Evaluation Measures: " + e.getMessage());
        }
    }

    private List<ModelFigure> fetchModelFigures(Passport passport) {
        try {
            String modelId = this.fetchDeploymentDetails(passport).getModelId();
            return modelFigureService.findByModelId(modelId);
        } catch (RuntimeException e) {
            throw new RuntimeException("Error fetching Model Figures: " + e.getMessage());
        }
    }


    private void cleanEmptyStringFieldsDeep(Object node, boolean excludeEmptyStringFields) {
        if (node == null) return;

        // Case 1: Map
        if (node instanceof Map<?, ?>) {
            Map<String, Object> map = (Map<String, Object>) node;
            Iterator<Map.Entry<String, Object>> it = map.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Object> entry = it.next();
                Object value = entry.getValue();

                if (value == null) {
                    // If value is null and the map schema expects String, we cannot infer type, so only replace if exclude=false
                    if (!excludeEmptyStringFields) entry.setValue("N/A");
                    continue;
                }

                if (value instanceof Map || value instanceof Collection<?>) {
                    cleanEmptyStringFieldsDeep(value, excludeEmptyStringFields);
                } else if (value instanceof String) {
                    String s = (String) value;
                    if (s.isBlank()) {
                        if (excludeEmptyStringFields) it.remove();
                        else entry.setValue("N/A");
                    }
                } else {
                    // Handle nested POJO (e.g., Feature)
                    cleanEmptyStringFieldsDeep(value, excludeEmptyStringFields);
                }
            }
        }

        // Case 2: Collection (List, Set, etc.)
        else if (node instanceof Collection<?>) {
            Collection coll = (Collection) node;
            List<Object> cleaned = new ArrayList<>(coll.size());
            for (Object item : coll) {
                if (item == null) {
                    if (!excludeEmptyStringFields) cleaned.add("N/A");
                    continue;
                }

                if (item instanceof String) {
                    String s = (String) item;
                    if (s.isBlank()) {
                        if (!excludeEmptyStringFields) cleaned.add("N/A");
                    } else cleaned.add(s);
                }
                else if (item instanceof Map || item instanceof Collection<?>) {
                    cleanEmptyStringFieldsDeep(item, excludeEmptyStringFields);
                    cleaned.add(item);
                }
                else {
                    cleanEmptyStringFieldsDeep(item, excludeEmptyStringFields); // handle POJO inside list
                    cleaned.add(item);
                }
            }
            coll.clear();
            coll.addAll(cleaned);
        }

        // Case 3: POJO (e.g. Feature, Dataset, etc.)
        else {
            Class<?> clazz = node.getClass();

            // Skip Java built-in immutable types
            if (clazz.isPrimitive() ||
                    clazz.getName().startsWith("java.") ||
                    clazz.isEnum()) {
                return;
            }

            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                try {
                    Object value = field.get(node);

                    // Null field handling: if type is String and value is null → "N/A"
                    if (value == null) {
                        if (field.getType() == String.class && !excludeEmptyStringFields) {
                            field.set(node, "N/A");
                        }
                        continue;
                    }

                    if (value instanceof String) {
                        String s = (String) value;
                        if (s.isBlank()) {
                            if (excludeEmptyStringFields) field.set(node, null);
                            else field.set(node, "N/A");
                        }
                    } else {
                        cleanEmptyStringFieldsDeep(value, excludeEmptyStringFields);
                    }

                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Error while cleaning empty string fields: " + e.getMessage());
                }
            }
        }
    }

}
