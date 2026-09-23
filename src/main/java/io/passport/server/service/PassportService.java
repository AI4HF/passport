package io.passport.server.service;

import io.passport.server.model.*;
import io.passport.server.repository.PassportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
    private ModelService modelService;

    @Autowired
    private StudyService studyService;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private LearningProcessParameterService learningProcessParameterService;

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
    private ModelEvaluationService modelEvaluationService;

    @Autowired
    private ModelEvaluationDatasetService modelEvaluationDatasetService;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private ModelFigureService modelFigureService;

    @Autowired
    private QualityCriteriaService qualityCriteriaService;

    @Autowired
    private QualityCriterionService qualityCriterionService;

    @Autowired
    private QualityAssessmentService qualityAssessmentService;

    @Autowired
    private QualityCriterionAssessmentResultService qualityCriterionAssessmentResultService;

    @Autowired
    private DatasetConceptService datasetConceptService;

    @Autowired
    private CatalogueDatasetService catalogueDatasetService;

    @Autowired
    private DatasetDistributionService datasetDistributionService;

    @Autowired
    private CatalogueRegistrationService catalogueRegistrationService;

    private final RoleCheckerService roleCheckerService;
    @Autowired
    private LearningStageParameterService learningStageParameterService;


    @Autowired
    public PassportService(PassportRepository passportRepository, RoleCheckerService roleCheckerService) {
        this.passportRepository = passportRepository;
        this.roleCheckerService = roleCheckerService;
    }

    /**
     * Determines which entities are to be cascaded based on the request from the previous element in the chain
     * Continues the chain by directing to the next entries through the other validation method
     *
     * @param studyId Id of the Study
     * @param sourceResourceType Resource type of the parent element in the Cascade chain
     * @param sourceResourceId Resource id of the parent element in the Cascade chain
     * @param principal Access Token content
     * @return
     */
    public ValidationResult validateCascade(String studyId, String sourceResourceType, String sourceResourceId, Jwt principal) {
        List<Passport> affectedPassports;

        switch (sourceResourceType) {
            case "Model":
                affectedPassports = passportRepository.findByModelId(sourceResourceId);
                break;
            default:
                return new ValidationResult(true, "");
        }

        if (affectedPassports.isEmpty()) {
            return new ValidationResult(true, "");
        }

        boolean hasPermission = roleCheckerService.isUserAuthorizedForStudy(
                studyId,
                principal,
                List.of(Role.QUALITY_ASSURANCE_SPECIALIST)
        );

        if (!hasPermission) {
            return new ValidationResult(false, "Passport");
        }

        return new ValidationResult(true, "Passport");
    }

    public Passport savePassport(Passport passport) {
        return passportRepository.save(passport);
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
     * @param passportWithDetailSelection The passport object with basic info (modelId, studyId, etc.) and selected details of the passport.
     * @return The saved Passport.
     */
    public Passport createPassport(PassportWithDetailSelection passportWithDetailSelection) {
        try {
            Map<String, Object> detailsJson = new HashMap<>();
            if(passportWithDetailSelection.getPassportDetailsSelection().isModelDetails()){
                detailsJson.put("modelDetails", fetchModelDetails(passportWithDetailSelection.getPassport()));
            }
            if(passportWithDetailSelection.getPassportDetailsSelection().isStudyDetails()){
                detailsJson.put("studyDetails", fetchStudyDetails(passportWithDetailSelection.getPassport()));
            }
            if(passportWithDetailSelection.getPassportDetailsSelection().isParameterDetails()){
                detailsJson.put("parameters", fetchParameters(passportWithDetailSelection.getPassport()));
            }
            if(passportWithDetailSelection.getPassportDetailsSelection().isParameterDetails()){
                detailsJson.put("learningStageParameters", fetchLearningStageParameters(passportWithDetailSelection.getPassport()));
            }
            if(passportWithDetailSelection.getPassportDetailsSelection().isParameterDetails()){
                detailsJson.put("learningProcessParameters", fetchLearningProcessParameters(passportWithDetailSelection.getPassport()));
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
            if(passportWithDetailSelection.getPassportDetailsSelection().isQualityCriteria()){
                detailsJson.put("qualityCriteriaWithCriterion", fetchQualityCriteriaWithCriterion(passportWithDetailSelection.getPassport()));
            }
            if(passportWithDetailSelection.getPassportDetailsSelection().isQualityAssessments()){
                detailsJson.put("qualityAssessmentsWithResults", fetchQualityAssessmentsWithResults(passportWithDetailSelection.getPassport()));
            }
            if(passportWithDetailSelection.getPassportDetailsSelection().isDatasets()){
                detailsJson.put("datasetsWithLearningDatasets", fetchDatasetsWithLearningDatasets(passportWithDetailSelection.getPassport()));
            }
            if(passportWithDetailSelection.getPassportDetailsSelection().isLearningProcessDetails()){
                detailsJson.put("learningProcessesWithStages", fetchLearningProcessesWithStages(passportWithDetailSelection.getPassport()));
            }
            if(passportWithDetailSelection.getPassportDetailsSelection().isEvaluationMeasures()){
                detailsJson.put("modelEvaluationsWithMeasures", fetchModelEvaluationsWithMeasures(passportWithDetailSelection.getPassport()));
            }
            if(passportWithDetailSelection.getPassportDetailsSelection().isModelFigures()){
                detailsJson.put("modelFigures", fetchModelFigures(passportWithDetailSelection.getPassport()));
            }
            cleanEmptyStringFieldsDeep(detailsJson, passportWithDetailSelection.getPassportDetailsSelection().isExcludeEmptyFields());
            Passport passport = passportWithDetailSelection.getPassport();
            passport.setDetailsJson(detailsJson);
            passport.setCreatedAt(Instant.now());
            passport.setApprovedAt(Instant.now());
            linkToPreviousVersion(passport);

            return passportRepository.save(passport);
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
     * Chains a new passport onto the newest one the model already has. A passport is never rewritten, so
     * regenerating produces the next version rather than replacing the record that was signed.
     *
     * @param passport The passport being created
     */
    private void linkToPreviousVersion(Passport passport) {
        Optional<Passport> previous = passportRepository.findFirstByModelIdOrderByVersionDesc(passport.getModelId());
        if (previous.isPresent()) {
            passport.setPreviousPassportId(previous.get().getPassportId());
            passport.setVersion(previous.get().getVersion() == null ? 2 : previous.get().getVersion() + 1);
        } else {
            passport.setPreviousPassportId(null);
            passport.setVersion(1);
        }
    }

    /**
     * Stores the signed PDF on the passport the first time it is generated, together with its SHA-256.
     * A passport that already carries a document keeps it: the signed bytes are the record, and producing
     * a different document for the same passport would make the signature meaningless.
     *
     * @param passportId ID of the passport
     * @param signedPdf The signed PDF bytes
     * @return The stored bytes - the ones just stored, or the ones already held
     */
    public byte[] storeSignedPdf(String passportId, byte[] signedPdf) {
        Passport passport = getPassportById(passportId);
        if (passport.getSignedPdf() != null && passport.getSignedPdf().length > 0) {
            return passport.getSignedPdf();
        }
        passport.setSignedPdf(signedPdf);
        passport.setSignedPdfHash(sha256Hex(signedPdf));
        passportRepository.save(passport);
        return signedPdf;
    }

    /**
     * Fetch the stored signed PDF of a passport.
     *
     * @param passportId ID of the passport
     * @return The signed bytes, or empty when the passport has not been generated yet
     */
    public Optional<byte[]> findSignedPdf(String passportId) {
        byte[] signedPdf = getPassportById(passportId).getSignedPdf();
        return (signedPdf == null || signedPdf.length == 0) ? Optional.empty() : Optional.of(signedPdf);
    }

    /**
     * Hex-encoded SHA-256 of the given bytes.
     */
    private String sha256Hex(byte[] bytes) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(bytes);
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 is not available", e);
        }
    }

    /**
     * Fetch methods to obtain pdf generation data
     */
    private ModelWithOwnerNameDTO fetchModelDetails(Passport passport) {
        try {
            Model model = modelService.findModelById(passport.getModelId())
                    .orElseThrow(() -> new RuntimeException("Model not found"));
            ModelWithOwnerNameDTO modelWithOwnerNameDTO = new ModelWithOwnerNameDTO(model);
            modelWithOwnerNameDTO.setOwnerOrganizationName(organizationService.findOrganizationById(model.getOwnerOrganizationId()).orElseThrow().getName());
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

    private List<LearningProcessParameterDTO> fetchLearningProcessParameters(Passport passport) {
        try {
            return learningProcessParameterService.findByStudyId(passport.getStudyId()).stream()
                    .map(LearningProcessParameterDTO::new)
                    .collect(Collectors.toList());
        } catch (RuntimeException e) {
            throw new RuntimeException("Error fetching LearningProcessParameters: " + e.getMessage());
        }
    }

    private List<LearningStageParameterDTO> fetchLearningStageParameters(Passport passport) {
        try {
            return learningStageParameterService.findByStudyId(passport.getStudyId()).stream()
                    .map(LearningStageParameterDTO::new)
                    .collect(Collectors.toList());
        } catch (RuntimeException e) {
            throw new RuntimeException("Error fetching LearningStageParameters: " + e.getMessage());
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
            return linkedArticleService.findLinkedArticleByModelId(passport.getModelId());
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

    /**
     * The quality criteria sets defined for the study, each with the rules it contains.
     */
    private List<Map<String, Object>> fetchQualityCriteriaWithCriterion(Passport passport) {
        try {
            return qualityCriteriaService.getAllQualityCriteriaByStudyId(passport.getStudyId()).stream()
                    .map(qualityCriteria -> {
                        Map<String, Object> criteriaWithCriterion = new HashMap<>();
                        criteriaWithCriterion.put("qualityCriteria", qualityCriteria);
                        criteriaWithCriterion.put("qualityCriterion",
                                qualityCriterionService.findQualityCriterionByQualityCriteriaId(qualityCriteria.getQualityCriteriaId()));
                        return criteriaWithCriterion;
                    })
                    .collect(Collectors.toList());
        } catch (RuntimeException e) {
            throw new RuntimeException("Error fetching Quality Criteria: " + e.getMessage());
        }
    }

    /**
     * The quality assessment runs over the study's datasets, each with its per-criterion results.
     */
    private List<Map<String, Object>> fetchQualityAssessmentsWithResults(Passport passport) {
        try {
            return qualityAssessmentService.getAllQualityAssessmentsByStudyId(passport.getStudyId()).stream()
                    .map(qualityAssessment -> {
                        Map<String, Object> assessmentWithResults = new HashMap<>();
                        assessmentWithResults.put("qualityAssessment", qualityAssessment);
                        assessmentWithResults.put("results",
                                qualityCriterionAssessmentResultService.findResultsByQualityAssessmentId(qualityAssessment.getQualityAssessmentId()));
                        return assessmentWithResults;
                    })
                    .collect(Collectors.toList());
        } catch (RuntimeException e) {
            throw new RuntimeException("Error fetching Quality Assessments: " + e.getMessage());
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
                        datasetWithLearningDatasets.put("concepts", datasetConceptService.findByDatasetId(dataset.getDatasetId()));
                        // the publication record, with what was actually listed where
                        catalogueDatasetService.findByDatasetId(dataset.getDatasetId()).ifPresent(catalogueDataset -> {
                            datasetWithLearningDatasets.put("catalogueDataset", catalogueDataset);
                            datasetWithLearningDatasets.put("distributions",
                                    datasetDistributionService.findByCatalogueDatasetId(catalogueDataset.getCatalogueDatasetId()));
                            datasetWithLearningDatasets.put("catalogueRegistrations",
                                    catalogueRegistrationService.findByCatalogueDatasetId(catalogueDataset.getCatalogueDatasetId()));
                        });
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

    /**
     * Measures belong to an evaluation run rather than to the model, so the passport carries each run
     * with the measures it produced and the learning datasets it was computed over.
     */
    private List<Map<String, Object>> fetchModelEvaluationsWithMeasures(Passport passport) {
        try {
            List<ModelEvaluation> modelEvaluations = modelEvaluationService.findModelEvaluationsByModelId(passport.getModelId());
            return modelEvaluations.stream()
                    .map(modelEvaluation -> {
                        Map<String, Object> modelEvaluationWithMeasures = new HashMap<>();
                        modelEvaluationWithMeasures.put("modelEvaluation", modelEvaluation);
                        modelEvaluationWithMeasures.put("evaluationMeasures",
                                evaluationMeasureService.findEvaluationMeasuresByModelEvaluationId(modelEvaluation.getModelEvaluationId()));
                        modelEvaluationWithMeasures.put("evaluationDatasets",
                                modelEvaluationDatasetService.findByModelEvaluationId(modelEvaluation.getModelEvaluationId())
                                        .stream().map(ModelEvaluationDatasetDTO::new).collect(Collectors.toList()));
                        return modelEvaluationWithMeasures;
                    })
                    .collect(Collectors.toList());
        } catch (RuntimeException e) {
            throw new RuntimeException("Error fetching Model Evaluations: " + e.getMessage());
        }
    }

    private List<ModelFigure> fetchModelFigures(Passport passport) {
        try {
            return modelFigureService.findByModelId(passport.getModelId());
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

    /**
     * Find Passports created or approved by a specific personnel.
     */
    public List<Passport> findByCreatedByOrApprovedBy(String personnelId) {
        return passportRepository.findByCreatedByOrApprovedBy(personnelId);
    }

    /**
     * Resolve the Study ID for a given Passport ID directly via DB query.
     */
    public Optional<String> findStudyIdByPassportId(String passportId) {
        return passportRepository.findStudyIdByPassportId(passportId);
    }

}
