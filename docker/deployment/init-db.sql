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
    feature_type    VARCHAR(255),
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
    PRIMARY KEY (dataset_id, feature_id)
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
    value               VARCHAR(255),
    PRIMARY KEY (learning_process_id, parameter_id)
);

-- Create learning_stage_parameter table
CREATE TABLE learning_stage_parameter
(
    learning_stage_id VARCHAR(255) REFERENCES learning_stage (learning_stage_id) ON DELETE CASCADE,
    parameter_id      VARCHAR(255) REFERENCES parameter (parameter_id) ON DELETE CASCADE,
    type              VARCHAR(255),
    value             VARCHAR(255),
    PRIMARY KEY (learning_stage_id, parameter_id)
);

-- Create model table
CREATE TABLE model
(
    model_id               VARCHAR(255) PRIMARY KEY,
    learning_process_id    VARCHAR(255) REFERENCES learning_process (learning_process_id) ON DELETE CASCADE,
    study_id               VARCHAR(255) REFERENCES study (study_id) ON DELETE CASCADE,
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
    value        VARCHAR(255),
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
    feature_type,
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
     'numerical',
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
INSERT INTO parameter (
    parameter_id,
    name,
    study_id,
    description,
    data_type
)
VALUES
    ('initial_parameter',
     'test_parameter',
     'initial_study',
     'test_description',
     'string');

-- Insert into algorithm
INSERT INTO algorithm (
    algorithm_id,
    name,
    objective_function,
    type,
    subtype
)
VALUES
    ('initial_algorithm',
     'Simple Linear Regression',
     'Placeholder Objective Function',
     'Regression',
     'Simple Linear Regression');

-- (Optional) more algorithm inserts if needed

-- Insert into implementation
INSERT INTO implementation (
    implementation_id,
    algorithm_id,
    software,
    name,
    description
)
VALUES
    ('initial_implementation',
     'initial_algorithm',
     'test_software',
     'test_name',
     'test_description');

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
     'test_description');

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
     'test_name',
     'test_description',
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
     'This is a dummy description for the Learning Process Dataset relation.');

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
     'string',
     'Dummy value for Learning Process Parameter');

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
     'string',
     'Dummy value for Learning Stage Parameter');

-- Insert into model
INSERT INTO model (
    model_id,
    learning_process_id,
    study_id,
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
     'test_name',
     'test_version',
     'test_tag',
     'test_model_type',
     'test_product_identifier',
     'initial_organization',
     'test_trl_level',
     'test_license',
     'test_primary_use',
     'test_secondary_use',
     'test_intended_users',
     'test_counter_indications',
     'test_ethical_considerations',
     'test_limitations',
     'test_fariness_constraints',
     '2023-01-01 00:00:00',
     'data_scientist',
     '2023-01-02 00:00:00',
     'data_scientist');

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
     'string',
     'Dummy value for Model Parameter');

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
         "name": "test_name",
         "version": "test_version",
         "modelType": "test_model_type",
         "productIdentifier": "test_product_identifier"
       },
       "studyDetails": {
         "id": "initial_study",
         "name": "Risk score for acute HF in the emergency department",
         "description": "Predicting risk factors for acute HF…",
         "objectives": "Evaluating the risk prediction for acute HF",
         "ethics": "Approved by Ethical Board on 2023-01-15, Application Number: 123"
       }
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
     'test_name',
     'test_value',
     'test_data_type',
     'test_description');