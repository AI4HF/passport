-- TABLE INITIALIZATION BEGIN

-- Create organization table
CREATE TABLE organization
(
    organization_id       VARCHAR(255) PRIMARY KEY,
    name                  VARCHAR(255),
    address               VARCHAR(255),
    organization_admin_id VARCHAR(255)
);

-- Create personnel table
CREATE TABLE personnel
(
    person_id       VARCHAR(255) PRIMARY KEY,
    organization_id VARCHAR(255) REFERENCES organization (organization_id) ON DELETE CASCADE,
    first_name      VARCHAR(255),
    last_name       VARCHAR(255),
    email           VARCHAR(255)
);

-- Create study table
CREATE TABLE study
(
    study_id    VARCHAR(255) PRIMARY KEY,
    name        VARCHAR(255),
    description TEXT,
    objectives  TEXT,
    ethics      TEXT,
    owner       VARCHAR(255) REFERENCES personnel (person_id)
);

-- Create population table
CREATE TABLE population
(
    population_id   VARCHAR(255) PRIMARY KEY,
    study_id        VARCHAR(255) REFERENCES study (study_id) ON DELETE CASCADE,
    population_url  VARCHAR(255),
    description     TEXT,
    characteristics TEXT
);

-- Create experiment table
CREATE TABLE experiment
(
    experiment_id     VARCHAR(255) PRIMARY KEY,
    study_id          VARCHAR(255) REFERENCES study (study_id) ON DELETE CASCADE,
    research_question TEXT
);

-- Create survey table
CREATE TABLE survey
(
    survey_id VARCHAR(255) PRIMARY KEY,
    study_id  VARCHAR(255) REFERENCES study (study_id) ON DELETE CASCADE,
    question  TEXT,
    answer    TEXT,
    category  VARCHAR(255)
);

-- Create study_personnel table
CREATE TABLE study_personnel
(
    study_id     VARCHAR(255) REFERENCES study (study_id) ON DELETE CASCADE,
    personnel_id VARCHAR(255) REFERENCES personnel (person_id) ON DELETE CASCADE,
    role         VARCHAR(255) NOT NULL,
    PRIMARY KEY (study_id, personnel_id)
);

-- Create study_organization table
CREATE TABLE study_organization
(
    study_id                 VARCHAR(255) REFERENCES study (study_id) ON DELETE CASCADE,
    organization_id          VARCHAR(255) REFERENCES organization (organization_id) ON DELETE CASCADE,
    role                     VARCHAR(255),
    responsible_personnel_id VARCHAR(255) REFERENCES personnel (person_id) ON DELETE CASCADE,
    population_id            VARCHAR(255) REFERENCES population (population_id) ON DELETE CASCADE,
    PRIMARY KEY (study_id, organization_id)
);

-- Create featureset table
CREATE TABLE featureset
(
    featureset_id   VARCHAR(255) PRIMARY KEY,
    experiment_id   VARCHAR(255) REFERENCES experiment (experiment_id) ON DELETE CASCADE,
    title           VARCHAR(255),
    featureset_url  VARCHAR(255),
    description     TEXT,
    created_at      TIMESTAMP,
    created_by      VARCHAR(255) REFERENCES personnel (person_id) ON DELETE CASCADE,
    last_updated_at TIMESTAMP,
    last_updated_by VARCHAR(255) REFERENCES personnel (person_id) ON DELETE CASCADE
);

-- Create feature table
CREATE TABLE feature
(
    feature_id      VARCHAR(255) PRIMARY KEY,
    featureset_id   VARCHAR(255) REFERENCES featureset (featureset_id) ON DELETE CASCADE,
    title           VARCHAR(255),
    description     TEXT,
    data_type       VARCHAR(255),
    isOutcome       BOOLEAN,
    mandatory       BOOLEAN,
    isUnique        BOOLEAN,
    units           VARCHAR(255),
    equipment       VARCHAR(255),
    data_collection VARCHAR(255),
    created_at      TIMESTAMP,
    created_by      VARCHAR(255) REFERENCES personnel (person_id) ON DELETE CASCADE,
    last_updated_at TIMESTAMP,
    last_updated_by VARCHAR(255) REFERENCES personnel (person_id) ON DELETE CASCADE
);

-- Create dataset table
CREATE TABLE dataset
(
    dataset_id       VARCHAR(255) PRIMARY KEY,
    featureset_id    VARCHAR(255) REFERENCES featureset (featureset_id) ON DELETE CASCADE,
    population_id    VARCHAR(255) REFERENCES population (population_id) ON DELETE CASCADE,
    organization_id  VARCHAR(255) REFERENCES organization (organization_id) ON DELETE CASCADE,
    title            VARCHAR(255),
    description      TEXT,
    version          VARCHAR(50),
    reference_entity VARCHAR(255),
    num_of_records   INTEGER,
    synthetic        BOOLEAN,
    created_at       TIMESTAMP,
    created_by       VARCHAR(255) REFERENCES personnel (person_id) ON DELETE CASCADE,
    last_updated_at  TIMESTAMP,
    last_updated_by  VARCHAR(255) REFERENCES personnel (person_id) ON DELETE CASCADE
);

-- Create dataset_transformation table
CREATE TABLE dataset_transformation
(
    data_transformation_id VARCHAR(255) PRIMARY KEY,
    title                  VARCHAR(255),
    description            TEXT
);

-- Create dataset_transformation_step table
CREATE TABLE dataset_transformation_step
(
    step_id                VARCHAR(255) PRIMARY KEY,
    data_transformation_id VARCHAR(255) REFERENCES dataset_transformation (data_transformation_id) ON DELETE CASCADE,
    input_features         VARCHAR(255),
    output_features        VARCHAR(255),
    method                 VARCHAR(255),
    explanation            TEXT,
    created_at             TIMESTAMP,
    created_by             VARCHAR(255) REFERENCES personnel (person_id) ON DELETE CASCADE,
    last_updated_at        TIMESTAMP,
    last_updated_by        VARCHAR(255) REFERENCES personnel (person_id) ON DELETE CASCADE
);

-- Create learning_dataset table
CREATE TABLE learning_dataset
(
    learning_dataset_id    VARCHAR(255) PRIMARY KEY,
    dataset_id             VARCHAR(255) REFERENCES dataset (dataset_id) ON DELETE CASCADE,
    study_id               VARCHAR(255) REFERENCES study (study_id) ON DELETE CASCADE,
    data_transformation_id VARCHAR(255) REFERENCES dataset_transformation (data_transformation_id) ON DELETE CASCADE,
    description            TEXT
);

-- Create feature_dataset_characteristic table
CREATE TABLE feature_dataset_characteristic
(
    dataset_id          VARCHAR(255) REFERENCES dataset (dataset_id) ON DELETE CASCADE,
    feature_id          VARCHAR(255) REFERENCES feature (feature_id) ON DELETE CASCADE,
    characteristic_name VARCHAR(255),
    value               VARCHAR(255),
    value_data_type     VARCHAR(255),
    PRIMARY KEY (dataset_id, feature_id, characteristic_name)
);

-- Create parameter table
CREATE TABLE parameter
(
    parameter_id VARCHAR(255) PRIMARY KEY,
    name         VARCHAR(255),
    study_id     VARCHAR(255) REFERENCES study (study_id) ON DELETE CASCADE,
    description  TEXT,
    data_type    VARCHAR(255)
);

-- Create algorithm table
CREATE TABLE algorithm
(
    algorithm_id       VARCHAR(255) PRIMARY KEY,
    name               VARCHAR(255),
    objective_function TEXT,
    type               VARCHAR(255),
    subtype            VARCHAR(255)
);

-- Create implementation table
CREATE TABLE implementation
(
    implementation_id VARCHAR(255) PRIMARY KEY,
    algorithm_id      VARCHAR(255) REFERENCES algorithm (algorithm_id) ON DELETE CASCADE,
    software          TEXT,
    name              VARCHAR(255),
    description       TEXT
);

-- Create learning_process table
CREATE TABLE learning_process
(
    learning_process_id VARCHAR(255) PRIMARY KEY,
    study_id            VARCHAR(255) REFERENCES study (study_id) ON DELETE CASCADE,
    implementation_id   VARCHAR(255) REFERENCES implementation (implementation_id) ON DELETE CASCADE,
    description         TEXT
);

-- Create learning_stage table
CREATE TABLE learning_stage
(
    learning_stage_id   VARCHAR(255) PRIMARY KEY,
    learning_process_id VARCHAR(255) REFERENCES learning_process (learning_process_id) ON DELETE CASCADE,
    learning_stage_name VARCHAR(255),
    description         TEXT,
    dataset_percentage  INTEGER
);

-- Create learning_process_dataset table
CREATE TABLE learning_process_dataset
(
    learning_process_id VARCHAR(255) REFERENCES learning_process (learning_process_id) ON DELETE CASCADE,
    learning_dataset_id VARCHAR(255) REFERENCES learning_dataset (learning_dataset_id) ON DELETE CASCADE,
    description         TEXT,
    PRIMARY KEY (learning_process_id, learning_dataset_id)
);

-- Create learning_process_parameter table
CREATE TABLE learning_process_parameter
(
    learning_process_id VARCHAR(255) REFERENCES learning_process (learning_process_id) ON DELETE CASCADE,
    parameter_id        VARCHAR(255) REFERENCES parameter (parameter_id) ON DELETE CASCADE,
    type                VARCHAR(255),
    value               TEXT,
    PRIMARY KEY (learning_process_id, parameter_id)
);

-- Create learning_stage_parameter table
CREATE TABLE learning_stage_parameter
(
    learning_stage_id VARCHAR(255) REFERENCES learning_stage (learning_stage_id) ON DELETE CASCADE,
    parameter_id      VARCHAR(255) REFERENCES parameter (parameter_id) ON DELETE CASCADE,
    type              VARCHAR(255),
    value             TEXT,
    PRIMARY KEY (learning_stage_id, parameter_id)
);

-- Create model table
CREATE TABLE model
(
    model_id               VARCHAR(255) PRIMARY KEY,
    learning_process_id    VARCHAR(255) REFERENCES learning_process (learning_process_id) ON DELETE CASCADE,
    study_id               VARCHAR(255) REFERENCES study (study_id) ON DELETE CASCADE,
    experiment_id          VARCHAR(255) REFERENCES experiment (experiment_id) ON DELETE CASCADE,
    name                   VARCHAR(255),
    version                VARCHAR(255),
    tag                    VARCHAR(255),
    model_type             VARCHAR(255),
    product_identifier     VARCHAR(255),
    owner                  VARCHAR(255) REFERENCES organization (organization_id) ON DELETE CASCADE,
    trl_level              VARCHAR(255),
    license                TEXT,
    primary_use            TEXT,
    secondary_use          TEXT,
    intended_users         TEXT,
    counter_indications    TEXT,
    ethical_considerations TEXT,
    limitations            TEXT,
    fairness_constraints   TEXT,
    created_at             TIMESTAMP,
    created_by             VARCHAR(255) REFERENCES personnel (person_id) ON DELETE CASCADE,
    last_updated_at        TIMESTAMP,
    last_updated_by        VARCHAR(255) REFERENCES personnel (person_id) ON DELETE CASCADE
);

-- Create model_parameter table
CREATE TABLE model_parameter
(
    model_id     VARCHAR(255) REFERENCES model (model_id) ON DELETE CASCADE,
    parameter_id VARCHAR(255) REFERENCES parameter (parameter_id) ON DELETE CASCADE,
    type         VARCHAR(255),
    value        TEXT,
    PRIMARY KEY (model_id, parameter_id)
);

-- Create deployment_environment table
CREATE TABLE deployment_environment
(
    environment_id       VARCHAR(255) PRIMARY KEY,
    title                VARCHAR(255),
    description          TEXT,
    hardware_properties  TEXT,
    software_properties  TEXT,
    connectivity_details TEXT
);

-- Create model_deployment table
CREATE TABLE model_deployment
(
    deployment_id       VARCHAR(255) PRIMARY KEY,
    model_id            VARCHAR(255) REFERENCES model (model_id),
    environment_id      VARCHAR(255) REFERENCES deployment_environment (environment_id),
    tags                VARCHAR(255),
    identified_failures TEXT,
    status              VARCHAR(255),
    created_at          TIMESTAMP,
    created_by          VARCHAR(255) REFERENCES personnel (person_id),
    last_updated_at     TIMESTAMP,
    last_updated_by     VARCHAR(255) REFERENCES personnel (person_id)
);

-- Create passport table
CREATE TABLE passport
(
    passport_id   VARCHAR(255) PRIMARY KEY,
    deployment_id VARCHAR(255) REFERENCES model_deployment (deployment_id) ON DELETE CASCADE,
    study_id      VARCHAR(255) REFERENCES study (study_id) ON DELETE CASCADE,
    created_at    TIMESTAMP,
    created_by    VARCHAR(255) REFERENCES personnel (person_id) ON DELETE CASCADE,
    approved_at   TIMESTAMP,
    approved_by   VARCHAR(255) REFERENCES personnel (person_id) ON DELETE CASCADE,
    details_json  JSONB
);

-- Create audit_log table
CREATE TABLE audit_log
(
    audit_log_id        VARCHAR(255) PRIMARY KEY,
    person_id           VARCHAR(255) REFERENCES personnel (person_id) ON DELETE CASCADE,
    person_name         VARCHAR(255),
    study_id            VARCHAR(255) REFERENCES study (study_id) ON DELETE CASCADE,
    occurred_at         TIMESTAMP,
    action_type         VARCHAR(255),
    affected_relation   VARCHAR(255),
    affected_record_id  VARCHAR(255),
    affected_record     TEXT,
    description         TEXT
);

-- Create audit_log_book table
CREATE TABLE audit_log_book
(
    audit_log_id VARCHAR(255) REFERENCES audit_log (audit_log_id) ON DELETE CASCADE,
    passport_id  VARCHAR(255) REFERENCES passport (passport_id) ON DELETE CASCADE,
    PRIMARY KEY (audit_log_id, passport_id)
);

-- Create evaluation_measure table
CREATE TABLE evaluation_measure
(
    measure_id  VARCHAR(255) PRIMARY KEY,
    model_id    VARCHAR(255) REFERENCES model (model_id) ON DELETE CASCADE,
    name        VARCHAR(255),
    value       VARCHAR(255),
    data_type   VARCHAR(255),
    description TEXT
);

-- TABLE INITIALIZATIONS END - DATA INITIALIZATION BEGIN

-- Insert into organization
INSERT INTO organization (organization_id, name, address, organization_admin_id)
VALUES
    ('initial_organization', 'Amsterdam UMC', 'Address of Amsterdam UMC', 'service-account-admin');

-- Insert into personnel
INSERT INTO personnel (person_id, organization_id, first_name, last_name, email)
VALUES
    ('study_owner', 'initial_organization', 'John', 'Doe', 'study_owner@gmail.com'),
    ('data_engineer', 'initial_organization', 'Okan', 'Mercan', 'data_engineer@gmail.com'),
    ('data_scientist', 'initial_organization', 'Kerem', 'Yilmaz', 'data_scientist@gmail.com'),
    ('quality_assurance_specialist', 'initial_organization', 'Anil', 'Sinaci', 'quality_assurance_specialist@gmail.com'),
    ('survey_manager', 'initial_organization', 'Senan', 'Postaci', 'survey_manager@gmail.com');

-- Insert into study
INSERT INTO study (study_id, name, description, objectives, ethics, owner)
VALUES
    ('initial_study',
     'Risk score for acute HF in the emergency department',
     'Predicting risk factors for acute HF…',
     'Evaluating the risk prediction for acute HF',
     'Approved by Ethical Board on 2023-01-15, Application Number: 123',
     'study_owner');

-- Insert into population
INSERT INTO population (population_id, study_id, population_url, description, characteristics)
VALUES
    ('initial_population',
     'initial_study',
     'https://datatools4heart.eu/cohorts/study1',
     'Patients hospitalized with a primary discharge diagnosis of heart failure where the primary discharge diagnosis refers to the main reason for admission.',
     'The study population comprised 500 participants, evenly distributed between males and females, with seventy percent ranging between 20-30 years and the rest ranging between 40-50 years old.');

-- Insert into experiment
INSERT INTO experiment (experiment_id, study_id, research_question)
VALUES
    ('initial_experiment',
     'initial_study',
     'A risk score prediction on subsequent (HF/CV)-rehospitalization within 7 days after hospital discharge.');

-- Insert into survey
INSERT INTO survey (survey_id, study_id, question, answer, category)
VALUES
    ('initial_survey',
     'initial_study',
     'Is this service tested by any third party?',
     'Yes',
     'Testing');

-- Insert into study_personnel
INSERT INTO study_personnel (study_id, personnel_id, role)
VALUES
    ('initial_study', 'study_owner', 'STUDY_OWNER'),
    ('initial_study', 'data_scientist', 'DATA_SCIENTIST'),
    ('initial_study', 'survey_manager', 'SURVEY_MANAGER'),
    ('initial_study', 'quality_assurance_specialist', 'QUALITY_ASSURANCE_SPECIALIST'),
    ('initial_study', 'data_engineer', 'DATA_ENGINEER');

-- Insert into study_organization
INSERT INTO study_organization (study_id, organization_id, role, responsible_personnel_id, population_id)
VALUES
    ('initial_study',
     'initial_organization',
     'STUDY_OWNER,DATA_SCIENTIST,DATA_ENGINEER,DATA_SCIENTIST,SURVEY_MANAGER,QUALITY_ASSURANCE_SPECIALIST',
     'study_owner',
     'initial_population');

-- Insert into featureset
INSERT INTO featureset (
    featureset_id,
    experiment_id,
    title,
    featureset_url,
    description,
    created_at,
    created_by,
    last_updated_at,
    last_updated_by
)
VALUES
    ('initial_featureset',
     'initial_experiment',
     'Feature set for AI4HFsubstudy 2 – Risk score prediction for acute HF in the emergency department.',
     'https://datatools4heart.eu/feature-sets/study1-features',
     'Feature set containing feature information used in risk score prediction for acute HF in the emergency department.',
     '2023-01-01 00:00:00',
     'data_engineer',
     '2023-01-01 00:00:00',
     'data_engineer');

-- Insert into feature
INSERT INTO feature (
    feature_id,
    featureset_id,
    title,
    description,
    data_type,
    isOutcome,
    mandatory,
    isUnique,
    units,
    equipment,
    data_collection,
    created_at,
    created_by,
    last_updated_at,
    last_updated_by
)
VALUES
    ('initial_feature',
     'initial_featureset',
     'age',
     'Age of the patient at reference point (at the time of admission)',
     'integer',
     false,
     true,
     false,
     'years',
     '',
     'Automatic Collection from Government Database',
     '2023-01-01 00:00:00',
     'data_engineer',
     '2023-01-01 00:00:00',
     'data_engineer');

-- Insert into dataset
INSERT INTO dataset (
    dataset_id,
    featureset_id,
    population_id,
    organization_id,
    title,
    description,
    version,
    reference_entity,
    num_of_records,
    synthetic,
    created_at,
    created_by,
    last_updated_at,
    last_updated_by
)
VALUES
    ('initial_dataset',
     'initial_featureset',
     'initial_population',
     'initial_organization',
     'HF Risk Dataset',
     'Dataset for HF Risk Prediction factors',
     '0.1',
     'Encounter',
     1562,
     false,
     '2023-01-01 00:00:00',
     'data_engineer',
     '2023-01-01 00:00:00',
     'data_engineer');

-- Insert into dataset_transformation
INSERT INTO dataset_transformation (
    data_transformation_id,
    title,
    description
)
VALUES
    ('initial_dataset_transformation',
     'Dataset Smoothening and Normalization',
     'Dataset is transformed by smoothening and normalization.');

-- Insert into dataset_transformation_step
INSERT INTO dataset_transformation_step (
    step_id,
    data_transformation_id,
    input_features,
    output_features,
    method,
    explanation,
    created_at,
    created_by,
    last_updated_at,
    last_updated_by
)
VALUES
    ('initial_dataset_transformation_step',
     'initial_dataset_transformation',
     'feature1',
     'feature1_1',
     'Normalization',
     'Decimal values are normalized between 0 and 1.',
     '2023-01-01 00:00:00',
     'data_engineer',
     '2023-01-01 00:00:00',
     'data_engineer');

-- Insert into learning_dataset
INSERT INTO learning_dataset (
    learning_dataset_id,
    dataset_id,
    study_id,
    data_transformation_id,
    description
)
VALUES
    ('initial_learning_dataset',
     'initial_dataset',
     'initial_study',
     'initial_dataset_transformation',
     'Finalized learning dataset for HF Risk Prediction Model Training');

-- Insert into feature_dataset_characteristic
INSERT INTO feature_dataset_characteristic (
    dataset_id,
    feature_id,
    characteristic_name,
    value,
    value_data_type
)
VALUES
    ('initial_dataset',
     'initial_feature',
     'variance',
     '11.2',
     'decimal');

-- Insert into parameter
INSERT INTO parameter (parameter_id, name, study_id, description, data_type)
VALUES ('initial_parameter', 'Number of Folds', 'initial_study', 'Number of folds for Gradient-boosted trees.', 'int');

-- Insert into algorithm
INSERT INTO algorithm (algorithm_id, name, objective_function, type, subtype)
VALUES ('simple_linear_regression', 'Simple Linear Regression', 'Placeholder Objective Function', 'Regression', 'Simple Linear Regression'),
       ('multiple_linear_regression', 'Multiple Linear Regression', 'Placeholder Objective Function', 'Regression', 'Multiple Linear Regression'),
       ('polynomial_regression', 'Polynomial Regression', 'Placeholder Objective Function', 'Regression', 'Polynomial Regression'),
       ('support_vector_regression_svr', 'Support Vector Regression (SVR)', 'Placeholder Objective Function', 'Regression', 'Support Vector Regression (SVR)'),
       ('decision_tree_regression', 'Decision Tree Regression', 'Placeholder Objective Function', 'Regression', 'Decision Tree Regression'),
       ('random_forest_regression', 'Random Forest Regression', 'Placeholder Objective Function', 'Regression', 'Random Forest Regression'),
       ('logistic_regression', 'Logistic Regression', 'Placeholder Objective Function', 'Classification', 'Logistic Regression'),
       ('k_nearest_neighbours_knn', 'K-Nearest Neighbours (K-NN)', 'Placeholder Objective Function', 'Classification', 'K-Nearest Neighbours (K-NN)'),
       ('support_vector_machine_svm', 'Support Vector Machine (SVM)', 'Placeholder Objective Function', 'Classification', 'Support Vector Machine (SVM)'),
       ('kernel_svm', 'Kernel SVM', 'Placeholder Objective Function', 'Classification', 'Kernel SVM'),
       ('naive_bayes', 'Naive Bayes', 'Placeholder Objective Function', 'Classification', 'Naive Bayes'),
       ('decision_tree_classification', 'Decision Tree Classification', 'Placeholder Objective Function', 'Classification', 'Decision Tree Classification'),
       ('random_forest_classification', 'Random Forest Classification', 'Placeholder Objective Function', 'Classification', 'Random Forest Classification'),
       ('k_means_clustering', 'K-Means Clustering', 'Placeholder Objective Function', 'Clustering', 'K-Means Clustering'),
       ('hierarchical_clustering', 'Hierarchical Clustering', 'Placeholder Objective Function', 'Clustering', 'Hierarchical Clustering'),
       ('apriori', 'Apriori', 'Placeholder Objective Function', 'Association Rule Learning', 'Apriori'),
       ('eclat', 'Eclat', 'Placeholder Objective Function', 'Association Rule Learning', 'Eclat'),
       ('upper_confidence_bounds_ucb', 'Upper Confidence Bounds (UCB)', 'Placeholder Objective Function', 'Reinforcement Learning', 'Upper Confidence Bounds (UCB)'),
       ('thompson_sampling', 'Thompson Sampling', 'Placeholder Objective Function', 'Reinforcement Learning', 'Thompson Sampling'),
       ('artificial_neural_networks_ann', 'Artificial Neural Networks (ANN)', 'Placeholder Objective Function', 'Deep Learning', 'Artificial Neural Networks (ANN)'),
       ('convolutional_neural_networks_cnn', 'Convolutional Neural Networks (CNN)', 'Placeholder Objective Function', 'Deep Learning', 'Convolutional Neural Networks (CNN)'),
       ('recurrent_neural_networks_rnn', 'Recurrent Neural Networks (RNN)', 'Placeholder Objective Function', 'Deep Learning', 'Recurrent Neural Networks (RNN)'),
       ('principal_component_analysis_pca', 'Principal Component Analysis (PCA)', 'Placeholder Objective Function', 'Dimensionality Reduction', 'Principal Component Analysis (PCA)'),
       ('linear_discriminant_analysis_lda', 'Linear Discriminant Analysis (LDA)', 'Placeholder Objective Function', 'Dimensionality Reduction', 'Linear Discriminant Analysis (LDA)'),
       ('kernel_pca', 'Kernel PCA', 'Placeholder Objective Function', 'Dimensionality Reduction', 'Kernel PCA');


-- Insert into implementation
INSERT INTO implementation (implementation_id, algorithm_id, software, name, description)
VALUES ('initial_implementation', 'simple_linear_regression', 'Spark MLlib', 'Gradient-boosted Tree Regression', 'Implementation of Gradient-boosted tree regression with Spark MLlib v3.5');


-- Insert into learning_process
INSERT INTO learning_process (
    learning_process_id,
    study_id,
    implementation_id,
    description
)
VALUES
    ('initial_learning_process',
     'initial_study',
     'initial_implementation',
     'ML process which uses SparkMLlib based Gradient-boosted Tree Regression implementation to process the parameterised data.');

-- Insert into learning_stage
INSERT INTO learning_stage (
    learning_stage_id,
    learning_process_id,
    learning_stage_name,
    description,
    dataset_percentage
)
VALUES
    ('initial_learning_stage',
     'initial_learning_process',
     'Training', 'Training stage/phase',
     50);

-- Insert into learning_process_dataset
INSERT INTO learning_process_dataset (
    learning_process_id,
    learning_dataset_id,
    description
)
VALUES
    ('initial_learning_process',
     'initial_learning_dataset',
     'Building a HF risk prediction model with Gradient-boosted tree regression.');

-- Insert into learning_process_parameter
INSERT INTO learning_process_parameter (
    learning_process_id,
    parameter_id,
    type,
    value
)
VALUES
    ('initial_learning_process',
     'initial_parameter',
     'int',
     '1');

-- Insert into learning_stage_parameter
INSERT INTO learning_stage_parameter (
    learning_stage_id,
    parameter_id,
    type,
    value
)
VALUES
    ('initial_learning_stage',
     'initial_parameter',
     'int',
     '2');

-- Insert into model
INSERT INTO model (
    model_id,
    learning_process_id,
    study_id,
    experiment_id,
    name,
    version,
    tag,
    model_type,
    product_identifier,
    owner,
    trl_level,
    license,
    primary_use,
    secondary_use,
    intended_users,
    counter_indications,
    ethical_considerations,
    limitations,
    fairness_constraints,
    created_at,
    created_by,
    last_updated_at,
    last_updated_by
)
VALUES
    ('initial_model',
     'initial_learning_process',
     'initial_study',
     'initial_experiment',
     'HF risk prediction model 1 (for 7-day readmission risk)', '1.0', 'Production', 'prediction', 'AI4HFModel001', 'initial_organization',
     'TRL4', 'RAIL-<DAMS>', 'Predicting 7-day readmission risk for Heart Failure patients.', 'Early intervention recommendations.', 'Healthcare providers, Data scientists',
     'Not recommended for cases with incomplete patient history.', 'Privacy and consent considerations', 'This model may have limited accuracy when applied to patients with rare or unique medical conditions due to insufficient representation in the training data.',
     'Efforts have been made to ensure that the model predictions are fair across different demographic groups. However, it may exhibit biases in certain subpopulations.',
     '2023-01-01 00:00:00', 'data_scientist', '2023-01-02 00:00:00', 'data_scientist');

-- Insert into model_parameter
INSERT INTO model_parameter (
    model_id,
    parameter_id,
    type,
    value
)
VALUES
    ('initial_model',
     'initial_parameter',
     'int',
     '3');

-- Insert into deployment_environment
INSERT INTO deployment_environment (
    environment_id,
    title,
    description,
    hardware_properties,
    software_properties,
    connectivity_details
)
VALUES
    ('initial_deployment_environment',
     'Production Environment',
     'Main Production Environment',
     'Disk: 512 GB, RAM: 32 GB',
     'OS: Windows, Cloud Services: Google Cloud Platform',
     'Secure HTTPS communication is established using TLS/SSL protocols. The environment is configured with a firewall allowing communication on ports 80 and 443. API endpoints are accessible via a private subnet, and external access is restricted to authorized IP addresses. Communication between services is encrypted, and access control is managed through role-based authentication');

-- Insert into model_deployment
INSERT INTO model_deployment (
    deployment_id,
    model_id,
    environment_id,
    tags,
    identified_failures,
    status,
    created_at,
    created_by,
    last_updated_at,
    last_updated_by
)
VALUES
    ('initial_model_deployment',
     'initial_model',
     'initial_deployment_environment',
     'Production',
     'Instances of false positives in predicting rare events.',
     'RUNNING',
     '2023-01-01 00:00:00',
     'data_scientist',
     '2023-01-01 00:00:00',
     'data_scientist');

-- Insert into passport
INSERT INTO passport (
    passport_id,
    deployment_id,
    study_id,
    created_at,
    created_by,
    approved_at,
    approved_by,
    details_json
)
VALUES
    ('initial_passport',
     'initial_model_deployment',
     'initial_study',
     '2023-01-01 00:00:00',
     'quality_assurance_specialist',
     '2023-01-01 00:00:00',
     'quality_assurance_specialist',
     '{
       "deploymentDetails": {
         "tags": "Production",
         "identifiedFailures": "Instances of false positives in predicting rare events.",
         "status": "RUNNING"
       },
       "environmentDetails": {
         "title": "Production Environment",
         "description": "Main Production Environment",
         "hardwareProperties": "Disk: 512 GB, RAM: 32 GB",
         "softwareProperties": "OS: Windows, Cloud Services: Google Cloud Platform",
         "connectivityDetails": "Secure HTTPS communication is established using TLS/SSL protocols."
       },
       "modelDetails": {
         "name": "HF Risk Score",
         "version": "1.0",
         "modelType": "Classification",
         "productIdentifier": "PID001"
       },
       "studyDetails": {
         "id": "initial_study",
         "name": "Risk score for acute HF in the emergency department",
         "description": "Predicting risk factors for acute HF…",
         "objectives": "Evaluating the risk prediction for acute HF",
         "ethics": "Approved by Ethical Board on 2023-01-15, Application Number: 123"
       },
       "parameters": [
         {
           "name": "Number of Estimators",
           "description": "Number of Trees in Random Forest",
           "dataType": "int"
         }
       ],
       "populationDetails": [
         {
           "populationUrl": "https://datatools4heart.eu/cohorts/study1",
           "description": "Patients hospitalized with a primary discharge diagnosis of heart failure.",
           "characteristics": "500 participants, 70% aged 20-30 years."
         }
       ],
       "surveys": [
         {
           "question": "Is this service tested by any third party?",
           "answer": "Yes",
           "category": "Testing"
         }
       ],
       "experiments": [
         {
           "researchQuestion": "A risk score prediction on subsequent (HF/CV)-rehospitalization within 7 days after hospital discharge."
         }
       ],
       "datasetsWithLearningDatasets": [
         {
           "dataset": {
             "title": "HF Risk Dataset",
             "description": "Dataset for HF Risk Prediction factors",
             "version": "0.1",
             "referenceEntity": "Encounter",
             "numOfRecords": 1562,
             "synthetic": false
           },
           "learningDatasets": [
             {
               "description": "Finalized learning dataset for HF Risk Prediction Model Teaching"
             }
           ]
         }
       ],
       "featureSetsWithFeatures": [
         {
           "featureSet": {
             "title": "Feature set for AI4HFsubstudy 2 - Risk score prediction for acute HF in the emergency department. ",
             "description": "Feature set containing feature information used in risk score prediction for acute HF in the emergency department.",
             "featuresetURL": "https://datatools4heart.eu/feature-sets/study1-features",
             "createdAt": "1970-01-20 10:35:20",
             "createdBy": "data_engineer",
             "lastUpdatedAt": "1970-01-20 10:35:20",
             "lastUpdatedBy": "data_engineer"
           },
           "features": [
             {
               "title": "age",
               "description": "Age of the patient at reference point (at the time of admission)",
               "dataType": "integer",
               "isOutcome": false,
               "mandatory": true,
               "isUnique": false,
               "units": "years",
               "dataCollection": "Automatic Collection from Government Database"
             }
           ]
         }
       ],
       "learningProcessesWithStages": [
         {
           "learningProcess": {
             "description": "This model generates a risk score that helps clinicians evaluate potential heart failure risks."
           },
           "learningStages": [
             {
               "learningStageName": "Model Training",
               "description": "Trains the model on the dataset",
               "datasetPercentage": 50
             }
           ]
         }
       ]
     }');

-- Insert into evaluation_measure
INSERT INTO evaluation_measure (
    measure_id,
    model_id,
    name,
    value,
    data_type,
    description
)
VALUES
    ('initial_evaluation_measure',
     'initial_model',
     'Accuracy', '0.77', 'float', 'Accuracy of Model with ID model1');