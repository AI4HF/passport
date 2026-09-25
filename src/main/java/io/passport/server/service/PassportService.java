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
import java.util.stream.Stream;

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
    private LearningProcessDatasetService learningProcessDatasetService;

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
            PassportDetails selection = passportWithDetailSelection.getPassportDetailsSelection();
            ModelScope scope = resolveModelScope(passportWithDetailSelection.getPassport());
            if(selection.isModelDetails()){
                detailsJson.put("modelDetails", fetchModelDetails(scope));
            }
            if(selection.isStudyDetails()){
                detailsJson.put("studyDetails", fetchStudyDetails(passportWithDetailSelection.getPassport()));
            }
            if(selection.isParameterDetails()){
                List<LearningProcessParameter> learningProcessParameters = fetchLearningProcessParameters(scope);
                List<LearningStageParameter> learningStageParameters = fetchLearningStageParameters(scope);
                detailsJson.put("parameters", fetchParameters(learningProcessParameters, learningStageParameters));
                detailsJson.put("learningStageParameters", learningStageParameters.stream()
                        .map(LearningStageParameterDTO::new).collect(Collectors.toList()));
                detailsJson.put("learningProcessParameters", learningProcessParameters.stream()
                        .map(LearningProcessParameterDTO::new).collect(Collectors.toList()));
            }
            if(selection.isPopulationDetails()){
                detailsJson.put("populationDetails", fetchPopulationDetails(scope));
            }
            if(selection.isSurveyDetails()){
                detailsJson.put("surveys", fetchSurveys(passportWithDetailSelection.getPassport()));
            }
            if(selection.isExperimentDetails()){
                detailsJson.put("experiments", fetchExperiments(scope));
            }
            if(selection.isLinkedArticleDetails()){
                detailsJson.put("linkedArticles", fetchLinkedArticles(passportWithDetailSelection.getPassport()));
            }
            if(selection.isFeatureSets()){
                detailsJson.put("featureSetsWithFeatures", fetchFeatureSetsWithFeatures(scope));
            }
            if(selection.isQualityCriteria()){
                detailsJson.put("qualityCriteriaWithCriterion", fetchQualityCriteriaWithCriterion(scope));
            }
            if(selection.isQualityAssessments()){
                detailsJson.put("qualityAssessmentsWithResults", fetchQualityAssessmentsWithResults(scope));
            }
            if(selection.isDatasets()){
                detailsJson.put("datasetsWithLearningDatasets", fetchDatasetsWithLearningDatasets(scope));
            }
            if(selection.isLearningProcessDetails()){
                detailsJson.put("learningProcessesWithStages", fetchLearningProcessesWithStages(scope));
            }
            if(selection.isEvaluationMeasures()){
                detailsJson.put("modelEvaluationsWithMeasures", fetchModelEvaluationsWithMeasures(passportWithDetailSelection.getPassport()));
            }
            if(selection.isModelFigures()){
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
     * What a passport describes: the model, and the learning datasets it was trained or evaluated on,
     * with the datasets those were prepared from. Every data section of the passport is drawn from this
     * rather than from the whole study, so a passport only shows what its model actually used.
     */
    private record ModelScope(Model model, List<LearningDataset> learningDatasets, List<Dataset> datasets) {}

    /**
     * Resolves the model of the passport and the learning datasets it used - those linked to its learning
     * process, then those its evaluations were computed over - together with their datasets.
     *
     * @param passport The passport being created
     * @return The model scope
     */
    private ModelScope resolveModelScope(Passport passport) {
        Model model = modelService.findModelById(passport.getModelId())
                .orElseThrow(() -> new RuntimeException("Model not found"));

        Set<String> learningDatasetIds = new LinkedHashSet<>();
        if (model.getLearningProcessId() != null) {
            learningProcessDatasetService.findByLearningProcessId(model.getLearningProcessId())
                    .forEach(learningProcessDataset -> learningDatasetIds.add(learningProcessDataset.getId().getLearningDatasetId()));
        }
        modelEvaluationService.findModelEvaluationsByModelId(model.getModelId())
                .forEach(modelEvaluation -> modelEvaluationDatasetService.findByModelEvaluationId(modelEvaluation.getModelEvaluationId())
                        .forEach(modelEvaluationDataset -> learningDatasetIds.add(modelEvaluationDataset.getId().getLearningDatasetId())));

        List<LearningDataset> learningDatasets = learningDatasetIds.stream()
                .map(learningDatasetService::findLearningDatasetByLearningDatasetId)
                .flatMap(Optional::stream)
                .collect(Collectors.toList());
        List<Dataset> datasets = learningDatasets.stream()
                .map(LearningDataset::getDatasetId)
                .distinct()
                .map(datasetService::findDatasetByDatasetId)
                .flatMap(Optional::stream)
                .collect(Collectors.toList());
        return new ModelScope(model, learningDatasets, datasets);
    }

    /**
     * Fetch methods to obtain pdf generation data
     */
    private ModelWithOwnerNameDTO fetchModelDetails(ModelScope scope) {
        try {
            ModelWithOwnerNameDTO modelWithOwnerNameDTO = new ModelWithOwnerNameDTO(scope.model());
            modelWithOwnerNameDTO.setOwnerOrganizationName(organizationService.findOrganizationById(scope.model().getOwnerOrganizationId()).orElseThrow().getName());
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

    /**
     * The definitions of the parameters the model's learning process and stages set.
     */
    private List<Parameter> fetchParameters(List<LearningProcessParameter> learningProcessParameters,
                                            List<LearningStageParameter> learningStageParameters) {
        try {
            return Stream.concat(
                            learningProcessParameters.stream().map(parameter -> parameter.getId().getParameterId()),
                            learningStageParameters.stream().map(parameter -> parameter.getId().getParameterId()))
                    .distinct()
                    .map(parameterService::findParameterById)
                    .flatMap(Optional::stream)
                    .collect(Collectors.toList());
        } catch (RuntimeException e) {
            throw new RuntimeException("Error fetching Parameters: " + e.getMessage());
        }
    }

    private List<LearningProcessParameter> fetchLearningProcessParameters(ModelScope scope) {
        try {
            if (scope.model().getLearningProcessId() == null) {
                return List.of();
            }
            return learningProcessParameterService.findByLearningProcessId(scope.model().getLearningProcessId());
        } catch (RuntimeException e) {
            throw new RuntimeException("Error fetching LearningProcessParameters: " + e.getMessage());
        }
    }

    private List<LearningStageParameter> fetchLearningStageParameters(ModelScope scope) {
        try {
            if (scope.model().getLearningProcessId() == null) {
                return List.of();
            }
            return learningStageService.findLearningStagesByProcessId(scope.model().getLearningProcessId()).stream()
                    .flatMap(learningStage -> learningStageParameterService.findByLearningStageId(learningStage.getLearningStageId()).stream())
                    .collect(Collectors.toList());
        } catch (RuntimeException e) {
            throw new RuntimeException("Error fetching LearningStageParameters: " + e.getMessage());
        }
    }

    /**
     * The populations the model's datasets were drawn from.
     */
    private List<Population> fetchPopulationDetails(ModelScope scope) {
        try {
            return scope.datasets().stream()
                    .map(Dataset::getPopulationId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .map(populationService::findPopulationById)
                    .flatMap(Optional::stream)
                    .collect(Collectors.toList());
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

    /**
     * The research question the model answers.
     */
    private List<Experiment> fetchExperiments(ModelScope scope) {
        try {
            if (scope.model().getExperimentId() == null) {
                return List.of();
            }
            return experimentService.findExperimentById(scope.model().getExperimentId()).stream().collect(Collectors.toList());
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

    /**
     * The feature sets the model's datasets conform to, each with its features.
     */
    private List<Map<String, Object>> fetchFeatureSetsWithFeatures(ModelScope scope) {
        try {
            return scope.datasets().stream()
                    .map(Dataset::getFeaturesetId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .map(featureSetService::findFeatureSetByFeatureSetId)
                    .flatMap(Optional::stream)
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
     * The quality assessment runs over the model's datasets.
     */
    private List<QualityAssessment> findQualityAssessments(ModelScope scope) {
        return scope.datasets().stream()
                .flatMap(dataset -> qualityAssessmentService.findQualityAssessmentsByDatasetId(dataset.getDatasetId()).stream())
                .collect(Collectors.toList());
    }

    /**
     * The quality criteria sets the model's datasets were assessed against, each with the rules it contains.
     */
    private List<Map<String, Object>> fetchQualityCriteriaWithCriterion(ModelScope scope) {
        try {
            return findQualityAssessments(scope).stream()
                    .map(QualityAssessment::getQualityCriteriaId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .map(qualityCriteriaService::findQualityCriteriaById)
                    .flatMap(Optional::stream)
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
     * The quality assessment runs over the model's datasets, each with its per-criterion results and the
     * dataset and center it was run against, so the section reads on its own.
     */
    private List<Map<String, Object>> fetchQualityAssessmentsWithResults(ModelScope scope) {
        try {
            return findQualityAssessments(scope).stream()
                    .map(qualityAssessment -> {
                        Map<String, Object> assessmentWithResults = new HashMap<>();
                        assessmentWithResults.put("qualityAssessment", qualityAssessment);
                        datasetService.findDatasetByDatasetId(qualityAssessment.getDatasetId()).ifPresent(dataset -> {
                            assessmentWithResults.put("datasetTitle", dataset.getTitle());
                            organizationService.findOrganizationById(dataset.getOrganizationId())
                                    .ifPresent(organization -> assessmentWithResults.put("organizationName", organization.getName()));
                        });
                        assessmentWithResults.put("results",
                                qualityCriterionAssessmentResultService.findResultsByQualityAssessmentId(qualityAssessment.getQualityAssessmentId()));
                        return assessmentWithResults;
                    })
                    .collect(Collectors.toList());
        } catch (RuntimeException e) {
            throw new RuntimeException("Error fetching Quality Assessments: " + e.getMessage());
        }
    }

    /**
     * The model's datasets, each listing only the learning datasets the model used.
     */
    private List<Map<String, Object>> fetchDatasetsWithLearningDatasets(ModelScope scope) {
        try {
            return scope.datasets().stream()
                    .map(dataset -> {
                        Map<String, Object> datasetWithLearningDatasets = new HashMap<>();
                        datasetWithLearningDatasets.put("dataset", dataset);
                        datasetWithLearningDatasets.put("learningDatasets", scope.learningDatasets().stream()
                                .filter(learningDataset -> dataset.getDatasetId().equals(learningDataset.getDatasetId()))
                                .collect(Collectors.toList()));
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

    /**
     * The learning process that produced the model, with its stages.
     */
    private List<Map<String, Object>> fetchLearningProcessesWithStages(ModelScope scope) {
        try {
            if (scope.model().getLearningProcessId() == null) {
                return List.of();
            }
            return learningProcessService.findLearningProcessById(scope.model().getLearningProcessId()).stream()
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
