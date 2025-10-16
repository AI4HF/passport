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
    ('0197a6f5-bb48-7855-b248-95697e913f22', 'Amsterdam UMC', 'Address of Amsterdam UMC', 'service-account-admin');

-- Insert into personnel
INSERT INTO personnel (person_id, organization_id, first_name, last_name, email)
VALUES
    ('study_owner', '0197a6f5-bb48-7855-b248-95697e913f22', 'John', 'Doe', 'study_owner@gmail.com'),
    ('data_engineer', '0197a6f5-bb48-7855-b248-95697e913f22', 'Okan', 'Mercan', 'data_engineer@gmail.com'),
    ('data_scientist', '0197a6f5-bb48-7855-b248-95697e913f22', 'Kerem', 'Yilmaz', 'data_scientist@gmail.com'),
    ('quality_assurance_specialist', '0197a6f5-bb48-7855-b248-95697e913f22', 'Anil', 'Sinaci', 'quality_assurance_specialist@gmail.com'),
    ('survey_manager', '0197a6f5-bb48-7855-b248-95697e913f22', 'Senan', 'Postaci', 'survey_manager@gmail.com'),
    ('ml_engineer', '0197a6f5-bb48-7855-b248-95697e913f22', 'Dogukan', 'Cavdaroglu', 'ml_engineer@gmail.com');


-- Insert into study
INSERT INTO study (study_id, name, description, objectives, ethics, owner)
VALUES
    ('0197a6f8-2b78-71e4-81c1-b7b6a744ece3',
     'Risk score for acute HF in the emergency department',
     'Predicting risk factors for acute HF…',
     'Evaluating the risk prediction for acute HF',
     'Approved by Ethical Board on 2023-01-15, Application Number: 123',
     'study_owner'),
    ('2197a6f8-2b78-71e4-81c1-b7b6a744ece4',
     'MAGGIC 1-Year Mortality Risk in Chronic Heart Failure',
     'Create a MAGGIC risk model using the provided cohort.',
     'Develop a calibrated MAGGIC risk score.',
     'Approved by Ethical Board on 2024-10-15, Application Number: 234',
     'study_owner');

-- Insert into population
INSERT INTO population (population_id, study_id, population_url, description, characteristics)
VALUES
    ('0197a6f8-fbc4-7652-ae5d-d52eaa0a48db',
     '0197a6f8-2b78-71e4-81c1-b7b6a744ece3',
     'https://ai4hf.eu/cohorts/study1',
     'Patients hospitalized with a primary discharge diagnosis of heart failure where the primary discharge diagnosis refers to the main reason for admission.',
     'The study population comprised 500 participants, evenly distributed between males and females, with seventy percent ranging between 20-30 years and the rest ranging between 40-50 years old.'),
    ('3197a6f8-2b78-71e4-81c1-b7b6a744ece5',
     '2197a6f8-2b78-71e4-81c1-b7b6a744ece4',
     'https://ai4hf.eu/cohorts/maggic',
     'Patients with heart failure meeting MAGGIC inclusion criteria.',
     'The study population comprised 500 participants, evenly distributed between males and females, with mean age being 28.');

-- Insert into experiment
INSERT INTO experiment (experiment_id, study_id, research_question)
VALUES
    ('0197a6f9-1f49-74a5-ab8a-e64fae0ca141',
     '0197a6f8-2b78-71e4-81c1-b7b6a744ece3',
     'A risk score prediction on subsequent (HF/CV)-rehospitalization within 7 days after hospital discharge.'),
    ('4197a6f8-2b78-71e4-81c1-b7b6a744ece5',
     '2197a6f8-2b78-71e4-81c1-b7b6a744ece4',
     'Can a MAGGIC-based model predict 1-year all cause mortality in Chronic Heart Failure?');

-- Insert into survey
INSERT INTO survey (survey_id, study_id, question, answer, category)
VALUES
    ('0197a6f9-7fd7-775b-bfb6-f371e4fc2509',
     '0197a6f8-2b78-71e4-81c1-b7b6a744ece3',
     'Is this service tested by any third party?',
     'Yes',
     'Testing');

-- Insert into study_personnel
INSERT INTO study_personnel (study_id, personnel_id, role)
VALUES
    ('0197a6f8-2b78-71e4-81c1-b7b6a744ece3', 'study_owner', 'STUDY_OWNER'),
    ('0197a6f8-2b78-71e4-81c1-b7b6a744ece3', 'data_scientist', 'DATA_SCIENTIST'),
    ('0197a6f8-2b78-71e4-81c1-b7b6a744ece3', 'survey_manager', 'SURVEY_MANAGER'),
    ('0197a6f8-2b78-71e4-81c1-b7b6a744ece3', 'quality_assurance_specialist', 'QUALITY_ASSURANCE_SPECIALIST'),
    ('0197a6f8-2b78-71e4-81c1-b7b6a744ece3', 'data_engineer', 'DATA_ENGINEER'),
    ('0197a6f8-2b78-71e4-81c1-b7b6a744ece3', 'ml_engineer', 'ML_ENGINEER'),
    ('2197a6f8-2b78-71e4-81c1-b7b6a744ece4','study_owner','STUDY_OWNER'),
    ('2197a6f8-2b78-71e4-81c1-b7b6a744ece4','data_engineer','DATA_ENGINEER'),
    ('2197a6f8-2b78-71e4-81c1-b7b6a744ece4','data_scientist','DATA_SCIENTIST'),
    ('2197a6f8-2b78-71e4-81c1-b7b6a744ece4','ml_engineer','ML_ENGINEER'),
    ('2197a6f8-2b78-71e4-81c1-b7b6a744ece4','quality_assurance_specialist','QUALITY_ASSURANCE_SPECIALIST');

-- Insert into study_organization
INSERT INTO study_organization (study_id, organization_id, role, responsible_personnel_id, population_id)
VALUES
    ('0197a6f8-2b78-71e4-81c1-b7b6a744ece3',
     '0197a6f5-bb48-7855-b248-95697e913f22',
     'STUDY_OWNER,DATA_SCIENTIST,DATA_ENGINEER,DATA_SCIENTIST,SURVEY_MANAGER,QUALITY_ASSURANCE_SPECIALIST,ML_ENGINEER',
     'study_owner',
     '0197a6f8-fbc4-7652-ae5d-d52eaa0a48db'),
    ('2197a6f8-2b78-71e4-81c1-b7b6a744ece4',
     '0197a6f5-bb48-7855-b248-95697e913f22',
     'STUDY_OWNER,DATA_ENGINEER,DATA_SCIENTIST,ML_ENGINEER,QUALITY_ASSURANCE_SPECIALIST',
     'study_owner',
     '3197a6f8-2b78-71e4-81c1-b7b6a744ece5');

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
    ('0197a6f9-d45e-70ec-b744-b7ff2bd87b3d',
     '0197a6f9-1f49-74a5-ab8a-e64fae0ca141',
     'Feature set for AI4HFsubstudy 2 – Risk score prediction for acute HF in the emergency department.',
     'https://ai4hf.eu/feature-sets/study1-features',
     'Feature set containing feature information used in risk score prediction for acute HF in the emergency department.',
     '2023-01-01 00:00:00',
     'data_engineer',
     '2023-01-01 00:00:00',
     'data_engineer'),
    ('6197a6f8-2b78-71e4-81c1-b7b6a744ece8',
     '4197a6f8-2b78-71e4-81c1-b7b6a744ece5',
     'MAGGIC Score Predictors',
     'https://ai4hf.eu/feature-sets/maggic-v1',
     'Canonical predictors used in MAGGIC score calculation and model training.',
     '2025-10-15 00:00:00','data_engineer','2025-10-15 00:00:00','data_engineer');

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
    ('0197a6fa-177f-7360-b2ce-272f2eb675b7',
     '0197a6f9-d45e-70ec-b744-b7ff2bd87b3d',
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


INSERT INTO feature (
    feature_id, featureset_id, title, description, data_type,
    isOutcome, mandatory, isUnique, units, equipment, data_collection,
    created_at, created_by, last_updated_at, last_updated_by
)
VALUES
    ('feat_gender', '6197a6f8-2b78-71e4-81c1-b7b6a744ece8', 'gender',
    'Gender of the patient', 'string', false, true, false, NULL, '', 'EHR',
    '2025-07-24 00:00:00','data_engineer','2025-07-24 00:00:00','data_engineer'),

    ('feat_age', '6197a6f8-2b78-71e4-81c1-b7b6a744ece8', 'age',
    'Age of the patient at reference point', 'integer', false, true, false, 'years', '', 'EHR',
    '2025-07-24 00:00:00','data_engineer','2025-07-24 00:00:00','data_engineer'),

    ('feat_nyha', '6197a6f8-2b78-71e4-81c1-b7b6a744ece8', 'nyha',
    'The latest value of the New York Heart Assessment as LOINC Code', 'string',
    false, true, false, NULL, '', 'EHR/Clinical assessment',
    '2025-07-24 00:00:00','data_engineer','2025-07-24 00:00:00','data_engineer'),

    ('feat_systolic_bp', '6197a6f8-2b78-71e4-81c1-b7b6a744ece8', 'systolic_blood_pressure',
    'Average systolic blood pressure (mmHg) over the 3 years preceding reference time point',
    'decimal', false, true, false, 'mmHg', '', 'Vitals',
    '2025-07-24 00:00:00','data_engineer','2025-07-24 00:00:00','data_engineer'),

    ('feat_bmi', '6197a6f8-2b78-71e4-81c1-b7b6a744ece8', 'bmi',
    'Average Body Mass Index (kg/m²) over the 3 years preceding reference time point',
    'decimal', false, true, false, 'kg/m2', '', 'Vitals',
    '2025-07-24 00:00:00','data_engineer','2025-07-24 00:00:00','data_engineer'),

    ('feat_creatinine', '6197a6f8-2b78-71e4-81c1-b7b6a744ece8', 'creatinine',
    'Creatinine [Mass/volume] in Serum or Plasma (mg/L) – 3-year average',
    'decimal', false, true, false, 'mg/L', '', 'Lab results',
    '2025-07-24 00:00:00','data_engineer','2025-07-24 00:00:00','data_engineer'),

    ('feat_hf_18m', '6197a6f8-2b78-71e4-81c1-b7b6a744ece8', 'heart_failure_ge_18_months',
    'Heart failure diagnosed ≥ 18 months before reference point', 'boolean',
    false, false, false, NULL, '', 'EHR/conditions',
    '2025-07-24 00:00:00','data_engineer','2025-07-24 00:00:00','data_engineer'),

    ('feat_copd', '6197a6f8-2b78-71e4-81c1-b7b6a744ece8', 'chronic_obstructive_pulmonary_disease',
    'Presence of chronic obstructive pulmonary disease (COPD)', 'boolean',
    false, false, false, NULL, '', 'EHR/conditions',
    '2025-07-24 00:00:00','data_engineer','2025-07-24 00:00:00','data_engineer'),

    ('feat_diabetes', '6197a6f8-2b78-71e4-81c1-b7b6a744ece8', 'diabetes',
    'Presence of diabetes mellitus', 'boolean',
    false, false, false, NULL, '', 'EHR/conditions',
    '2025-07-24 00:00:00','data_engineer','2025-07-24 00:00:00','data_engineer'),

    ('feat_beta_blocker', '6197a6f8-2b78-71e4-81c1-b7b6a744ece8', 'beta_blocker_use',
    'Administration of beta-blocker medication', 'boolean',
    false, false, false, NULL, '', 'Medication administration',
    '2025-07-24 00:00:00','data_engineer','2025-07-24 00:00:00','data_engineer'),

    ('feat_ace_arb', '6197a6f8-2b78-71e4-81c1-b7b6a744ece8', 'ace_inhibitor_or_arb_use',
    'Administration of ACE inhibitor or ARB medication', 'boolean',
    false, false, false, NULL, '', 'Medication administration',
    '2025-07-24 00:00:00','data_engineer','2025-07-24 00:00:00','data_engineer'),

    ('feat_lvef', '6197a6f8-2b78-71e4-81c1-b7b6a744ece8', 'lvef',
    'Most recent left ventricular ejection fraction before reference point',
    'decimal', false, true, false, 'percent', '', 'Echocardiography',
    '2025-07-24 00:00:00','data_engineer','2025-07-24 00:00:00','data_engineer'),

    ('feat_smoker', '6197a6f8-2b78-71e4-81c1-b7b6a744ece8', 'smoker',
    'Most recent recorded smoking status before reference point (1 = current smoker, 0 = former/never)',
    'boolean', false, false, false, NULL, '', 'EHR/Questionnaire',
    '2025-07-24 00:00:00','data_engineer','2025-07-24 00:00:00','data_engineer');

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
    ('0197a6fa-6507-775b-99d9-f8808e10052d',
     '0197a6f9-d45e-70ec-b744-b7ff2bd87b3d',
     '0197a6f8-fbc4-7652-ae5d-d52eaa0a48db',
     '0197a6f5-bb48-7855-b248-95697e913f22',
     'HF Risk Dataset',
     'Dataset for HF Risk Prediction factors',
     '0.1',
     'Encounter',
     1562,
     false,
     '2023-01-01 00:00:00',
     'data_engineer',
     '2023-01-01 00:00:00',
     'data_engineer'),
    ('9197a6f8-2b78-71e4-81c1-b7b6a744ece9',
     '6197a6f8-2b78-71e4-81c1-b7b6a744ece8',
     '3197a6f8-2b78-71e4-81c1-b7b6a744ece5',
     '0197a6f5-bb48-7855-b248-95697e913f22',
     'MAGGIC Dataset v1',
     'Dataset extracted from MAGGIC data.',
     '1.0',
     'Patient',
     500,
     false,
     '2025-10-15 00:00:00','data_engineer','2025-10-15 00:00:00','data_engineer');

-- Insert into dataset_transformation
INSERT INTO dataset_transformation (
    data_transformation_id,
    title,
    description
)
VALUES
    ('0197a6fa-6507-775b-99d9-f8808e10052d_transformation',
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
    ('0197a6fa-6507-775b-99d9-f8808e10052d_transformation_step',
     '0197a6fa-6507-775b-99d9-f8808e10052d_transformation',
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
    ('0197a6fc-7af4-7c75-b8ff-fbdc815fdafb',
     '0197a6fa-6507-775b-99d9-f8808e10052d',
     '0197a6f8-2b78-71e4-81c1-b7b6a744ece3',
     '0197a6fa-6507-775b-99d9-f8808e10052d_transformation',
     'Finalized learning dataset for HF Risk Prediction Model Training'),
    ('b197a6fc-7af4-7c75-b8ff-fbdc815fdacf',
     '9197a6f8-2b78-71e4-81c1-b7b6a744ece9',
     '2197a6f8-2b78-71e4-81c1-b7b6a744ece4',
     '0197a6fa-6507-775b-99d9-f8808e10052d_transformation',
     'Finalized learning dataset derived from MAGGIC Dataset v1 for 1-year mortality prediction.');

-- Insert into feature_dataset_characteristic
INSERT INTO feature_dataset_characteristic (
    dataset_id,
    feature_id,
    characteristic_name,
    value,
    value_data_type
)
VALUES
    ('0197a6fa-6507-775b-99d9-f8808e10052d',
     '0197a6fa-177f-7360-b2ce-272f2eb675b7',
     'variance',
     '11.2',
     'decimal');

-- Insert into parameter
INSERT INTO parameter (parameter_id, name, study_id, description, data_type)
VALUES ('0197a70e-aa2e-76dc-b4f7-68a3cd35c3a1', 'Number of Folds', '0197a6f8-2b78-71e4-81c1-b7b6a744ece3', 'Number of folds for Gradient-boosted trees.', 'int');

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
VALUES ('0197a70f-3f85-7142-8ab0-b7ae916bfdf1', 'simple_linear_regression', 'Spark MLlib', 'Gradient-boosted Tree Regression', 'Implementation of Gradient-boosted tree regression with Spark MLlib v3.5');


-- Insert into learning_process
INSERT INTO learning_process (
    learning_process_id,
    study_id,
    implementation_id,
    description
)
VALUES
    ('0197a70f-a49d-71df-8f6d-d205da111e28',
     '0197a6f8-2b78-71e4-81c1-b7b6a744ece3',
     '0197a70f-3f85-7142-8ab0-b7ae916bfdf1',
     'ML process which uses SparkMLlib based Gradient-boosted Tree Regression implementation to process the parameterised data.'),
    ('b197a70f-a49d-71df-8f6d-d205da111e31',
     '2197a6f8-2b78-71e4-81c1-b7b6a744ece4',
     '0197a70f-3f85-7142-8ab0-b7ae916bfdf1',
     'MAGGIC-MLP learning process trained on the MAGGIC Dataset for 1-year mortality risk prediction using multi-layer perceptron regression.');

-- Insert into learning_stage
INSERT INTO learning_stage (
    learning_stage_id,
    learning_process_id,
    learning_stage_name,
    description,
    dataset_percentage
)
VALUES
    ('0197a717-bd62-704f-9886-b566545a2725',
     '0197a70f-a49d-71df-8f6d-d205da111e28',
     'Training', 'Training stage/phase',
     50),
    ('b197a717-bd62-704f-9886-b566545a2733',
     'b197a70f-a49d-71df-8f6d-d205da111e31',
     'Training',
     'Model training stage using 70% of the MAGGIC Dataset for multi-layer perceptron calibration.',
     70),
    ('b297a717-bd62-704f-9886-b566545a2734',
     'b197a70f-a49d-71df-8f6d-d205da111e31',
     'Validation',
     'Model validation stage using 15% of the MAGGIC Dataset to tune hyperparameters and prevent overfitting.',
     15),
    ('b397a717-bd62-704f-9886-b566545a2735',
     'b197a70f-a49d-71df-8f6d-d205da111e31',
     'Testing',
     'Final evaluation on the remaining 15% of the MAGGIC Dataset for performance assessment.',
     15);

-- Insert into learning_process_dataset
INSERT INTO learning_process_dataset (
    learning_process_id,
    learning_dataset_id,
    description
)
VALUES
    ('0197a70f-a49d-71df-8f6d-d205da111e28',
     '0197a6fc-7af4-7c75-b8ff-fbdc815fdafb',
     'Building a HF risk prediction model with Gradient-boosted tree regression.'),
    ('b197a70f-a49d-71df-8f6d-d205da111e31',
     'b197a6fc-7af4-7c75-b8ff-fbdc815fdacf',
     'MAGGIC-MLP model trained for predicting 1-year all-cause mortality using structured Chronic Heart Failure data.');

-- Insert into learning_process_parameter
INSERT INTO learning_process_parameter (
    learning_process_id,
    parameter_id,
    type,
    value
)
VALUES
    ('0197a70f-a49d-71df-8f6d-d205da111e28',
     '0197a70e-aa2e-76dc-b4f7-68a3cd35c3a1',
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
    ('0197a717-bd62-704f-9886-b566545a2725',
     '0197a70e-aa2e-76dc-b4f7-68a3cd35c3a1',
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
    ('0197a718-9800-7558-8565-5f760c97c8f0',
     '0197a70f-a49d-71df-8f6d-d205da111e28',
     '0197a6f8-2b78-71e4-81c1-b7b6a744ece3',
     '0197a6f9-1f49-74a5-ab8a-e64fae0ca141',
     'HF risk prediction model 1 (for 7-day readmission risk)', '1.0', 'Production', 'prediction', 'AI4HFModel001', '0197a6f5-bb48-7855-b248-95697e913f22',
     'TRL4', 'RAIL-<DAMS>', 'Predicting 7-day readmission risk for Heart Failure patients.', 'Early intervention recommendations.', 'Healthcare providers, Data scientists',
     'Not recommended for cases with incomplete patient history.', 'Privacy and consent considerations', 'This model may have limited accuracy when applied to patients with rare or unique medical conditions due to insufficient representation in the training data.',
     'Efforts have been made to ensure that the model predictions are fair across different demographic groups. However, it may exhibit biases in certain subpopulations.',
     '2023-01-01 00:00:00', 'data_scientist', '2023-01-02 00:00:00', 'data_scientist'),
    ('b197a718-9800-7558-8565-5f760c97c8f9',
     'b197a70f-a49d-71df-8f6d-d205da111e31',
     '2197a6f8-2b78-71e4-81c1-b7b6a744ece4',
     '4197a6f8-2b78-71e4-81c1-b7b6a744ece5',
     'MAGGIC-MLP Model (v1.0)',
     '1.0',
     'Production',
     'Classification',
     'AI4HF_MAGGIC_MLP_001',
     '0197a6f5-bb48-7855-b248-95697e913f22',
     'TRL6',
     'AI4HF-Research License v1.0',
     'Predicting 1-year mortality risk in chronic heart failure patients based on MAGGIC feature set.',
     'Clinical risk stratification and care prioritization.',
     'Clinicians, cardiologists, and data science researchers.',
     'Not suitable for pediatric Chronic Heart Failure or congenital heart disease populations.',
     'Ethically approved for retrospective analysis; patient identifiers are anonymized.',
     'Performance may degrade in cohorts with limited lab or echocardiographic data.',
     'Bias mitigation methods were applied across gender and age subgroups.',
     '2025-07-24 00:00:00','data_scientist','2025-10-15 00:00:00','data_scientist');

-- Insert into model_parameter
INSERT INTO model_parameter (
    model_id,
    parameter_id,
    type,
    value
)
VALUES
    ('0197a718-9800-7558-8565-5f760c97c8f0',
     '0197a70e-aa2e-76dc-b4f7-68a3cd35c3a1',
     'int',
     '3'),
    ('b197a718-9800-7558-8565-5f760c97c8f9',
     '0197a70e-aa2e-76dc-b4f7-68a3cd35c3a1',
     'int',
     '5');

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
    ('0197a718-ced2-73af-8ca9-d5ff45e2fa18',
     'Production Environment',
     'Main Production Environment',
     'Disk: 512 GB, RAM: 32 GB',
     'OS: Windows, Cloud Services: Google Cloud Platform',
     'Secure HTTPS communication is established using TLS/SSL protocols. The environment is configured with a firewall allowing communication on ports 80 and 443. API endpoints are accessible via a private subnet, and external access is restricted to authorized IP addresses. Communication between services is encrypted, and access control is managed through role-based authentication'),
    ('b197a718-ced2-73af-8ca9-d5ff45e2fa25',
     'Clinical Validation Environment',
     'Dedicated environment for MAGGIC-MLP model validation under clinical conditions.',
     'RAM: 64 GB, CPU: 16 cores, GPU: 1x NVIDIA A100 40GB',
     'OS: Ubuntu 22.04, Frameworks: TensorFlow 2.15, PyTorch 2.2, Spark 3.5',
     'Secure hospital intranet connection with role-based VPN access and encrypted endpoints.');

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
    ('0197a717-f048-7bc2-802a-c1300736d9fc',
     '0197a718-9800-7558-8565-5f760c97c8f0',
     '0197a718-ced2-73af-8ca9-d5ff45e2fa18',
     'Production',
     'Instances of false positives in predicting rare events.',
     'RUNNING',
     '2023-01-01 00:00:00',
     'data_scientist',
     '2023-01-01 00:00:00',
     'data_scientist'),
    ('b197a717-f048-7bc2-802a-c1300736d9ff',
     'b197a718-9800-7558-8565-5f760c97c8f9',
     'b197a718-ced2-73af-8ca9-d5ff45e2fa25',
     'Validation,ProductionCandidate',
     'Model occasionally overestimates low-risk cases with missing LVEF values.',
     'VALIDATING',
     '2025-10-15 00:00:00',
     'data_scientist',
     '2025-10-15 00:00:00',
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
    ('0197a71a-20fd-73ab-b3d1-65af71b25fd7',
     '0197a717-f048-7bc2-802a-c1300736d9fc',
     '0197a6f8-2b78-71e4-81c1-b7b6a744ece3',
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
         "id": "0197a6f8-2b78-71e4-81c1-b7b6a744ece3",
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
           "populationUrl": "https://ai4hf.eu/cohorts/study1",
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
             "featuresetURL": "https://ai4hf.eu/feature-sets/study1-features",
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
       ],
       "evaluationMeasures": [
        {
          "name": "Accuracy",
          "dataType": "float",
          "value": "0.77",
          "description": "Accuracy of Model with ID model1"
        }
       ]
     }'),
    ('b197a71a-20fd-73ab-b3d1-65af71b25ff1',
     'b197a717-f048-7bc2-802a-c1300736d9ff',
     '2197a6f8-2b78-71e4-81c1-b7b6a744ece4',
     '2025-10-15 00:00:00',
     'quality_assurance_specialist',
     '2025-10-16 00:00:00',
     'quality_assurance_specialist',
     '{
       "deploymentDetails": {
         "tags": "Validation, ProductionCandidate",
         "identifiedFailures": "Model occasionally overestimates low-risk cases with missing LVEF values.",
         "status": "VALIDATING"
       },
       "environmentDetails": {
         "title": "Clinical Validation Environment",
         "description": "Dedicated environment for MAGGIC-MLP model validation under clinical conditions.",
         "hardwareProperties": "RAM: 64 GB, CPU: 16 cores, GPU: 1x NVIDIA A100 40GB",
         "softwareProperties": "OS: Ubuntu 22.04, Frameworks: TensorFlow 2.15, PyTorch 2.2, Spark 3.5",
         "connectivityDetails": "Secure hospital intranet connection with role-based VPN access and encrypted endpoints."
       },
       "modelDetails": {
         "name": "MAGGIC-MLP Model (v1.0)",
         "version": "1.0",
         "modelType": "Classification",
         "productIdentifier": "AI4HF_MAGGIC_MLP_001"
       },
       "studyDetails": {
         "id": "2197a6f8-2b78-71e4-81c1-b7b6a744ece4",
         "name": "MAGGIC 1-Year Mortality Risk in Chronic Heart Failure",
         "description": "Create a MAGGIC risk model using the provided cohort.",
         "objectives": "Develop a calibrated MAGGIC risk score.",
         "ethics": "Approved by Ethical Board on 2024-10-15, Application Number: 234"
       },
       "datasetsWithLearningDatasets": [
         {
           "dataset": {
             "title": "MAGGIC Dataset v1",
             "description": "Dataset extracted from MAGGIC data.",
             "version": "1.0",
             "referenceEntity": "Patient",
             "numOfRecords": 500,
             "synthetic": false
           },
           "learningDatasets": [
             {
               "description": "Finalized learning dataset derived from MAGGIC Dataset v1 for 1-year mortality prediction."
             }
           ]
         }
       ],
       "featureSetsWithFeatures": [
         {
           "featureSet": {
             "title": "MAGGIC Score Predictors",
             "description": "Canonical predictors used in MAGGIC score calculation and model training.",
             "featuresetURL": "https://ai4hf.eu/feature-sets/maggic-v1",
             "createdAt": "2025-10-15 00:00:00",
             "createdBy": "data_engineer",
             "lastUpdatedAt": "2025-10-15 00:00:00",
             "lastUpdatedBy": "data_engineer"
           },
           "features": [
             { "title": "gender", "description": "Gender of the patient", "dataType": "string", "isOutcome": false, "mandatory": true, "isUnique": false, "dataCollection": "EHR" },
             { "title": "age", "description": "Age of the patient at reference point", "dataType": "integer", "isOutcome": false, "mandatory": true, "isUnique": false, "units": "years", "dataCollection": "EHR" },
             { "title": "nyha", "description": "The latest value of the New York Heart Assessment as LOINC Code", "dataType": "string", "isOutcome": false, "mandatory": true, "dataCollection": "EHR/Clinical assessment" },
             { "title": "systolic_blood_pressure", "description": "Average systolic blood pressure (mmHg) over 3 years preceding reference time point", "dataType": "decimal", "isOutcome": false, "mandatory": true, "units": "mmHg", "dataCollection": "Vitals" },
             { "title": "bmi", "description": "Average Body Mass Index (kg/m²) over 3 years preceding reference time point", "dataType": "decimal", "isOutcome": false, "mandatory": true, "units": "kg/m2", "dataCollection": "Vitals" },
             { "title": "creatinine", "description": "Creatinine [Mass/volume] in Serum or Plasma (mg/L) – 3-year average", "dataType": "decimal", "isOutcome": false, "mandatory": true, "units": "mg/L", "dataCollection": "Lab results" },
             { "title": "heart_failure_ge_18_months", "description": "Heart failure diagnosed ≥18 months before reference point", "dataType": "boolean", "isOutcome": false, "dataCollection": "EHR/conditions" },
             { "title": "chronic_obstructive_pulmonary_disease", "description": "Presence of chronic obstructive pulmonary disease (COPD)", "dataType": "boolean", "isOutcome": false, "dataCollection": "EHR/conditions" },
             { "title": "diabetes", "description": "Presence of diabetes mellitus", "dataType": "boolean", "isOutcome": false, "dataCollection": "EHR/conditions" },
             { "title": "beta_blocker_use", "description": "Administration of beta-blocker medication", "dataType": "boolean", "isOutcome": false, "dataCollection": "Medication administration" },
             { "title": "ace_inhibitor_or_arb_use", "description": "Administration of ACE inhibitor or ARB medication", "dataType": "boolean", "isOutcome": false, "dataCollection": "Medication administration" },
             { "title": "lvef", "description": "Most recent left ventricular ejection fraction before reference point", "dataType": "decimal", "isOutcome": false, "units": "percent", "dataCollection": "Echocardiography" },
             { "title": "smoker", "description": "Most recent smoking status (1=current smoker, 0=non-smoker)", "dataType": "boolean", "isOutcome": false, "dataCollection": "EHR/Questionnaire" }
           ]
         }
       ],
       "learningProcessesWithStages": [
         {
           "learningProcess": {
             "description": "MAGGIC-MLP learning process trained on the MAGGIC Dataset for 1-year mortality risk prediction using multi-layer perceptron regression."
           },
           "learningStages": [
             { "learningStageName": "Training", "description": "Model training stage using 70% of the MAGGIC Dataset for calibration.", "datasetPercentage": 70 },
             { "learningStageName": "Validation", "description": "Model validation stage using 15% of the MAGGIC Dataset.", "datasetPercentage": 15 },
             { "learningStageName": "Testing", "description": "Testing stage on 15% of the MAGGIC Dataset for performance evaluation.", "datasetPercentage": 15 }
           ]
         }
       ],
       "evaluationMeasures": [
         {
           "name": "AUC",
           "dataType": "float",
           "value": "0.89",
           "description": "Area under the ROC curve for 1-year mortality prediction using MAGGIC-MLP."
         },
         {
           "name": "Accuracy",
           "dataType": "float",
           "value": "0.83",
           "description": "Overall classification accuracy of the MAGGIC-MLP model."
         },
         {
           "name": "F1-score",
           "dataType": "float",
           "value": "0.81",
           "description": "F1-score reflecting precision and recall balance for the MAGGIC-MLP model."
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
    ('0197a71a-60cc-7845-ae3b-6db704f691a4',
     '0197a718-9800-7558-8565-5f760c97c8f0',
     'Accuracy', '0.77', 'float', 'Accuracy of Model with ID model1'),
    ('b197a71a-60cc-7845-ae3b-6db704f691a9',
     'b197a718-9800-7558-8565-5f760c97c8f9',
     'AUC',
     '0.89',
     'float',
     'Area under the ROC curve for 1-year mortality prediction using MAGGIC-MLP.'),
    ('b297a71a-60cc-7845-ae3b-6db704f691b0',
     'b197a718-9800-7558-8565-5f760c97c8f9',
     'Accuracy',
     '0.83',
     'float',
     'Overall classification accuracy of the MAGGIC-MLP model.'),
    ('b397a71a-60cc-7845-ae3b-6db704f691b1',
     'b197a718-9800-7558-8565-5f760c97c8f9',
     'F1-Score',
     '0.81',
     'float',
     'F1-score reflecting precision and recall balance for the MAGGIC-MLP model.');