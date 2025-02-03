-- Create organization table
CREATE TABLE organization
(
    organization_id       SERIAL PRIMARY KEY,
    name                  VARCHAR(255),
    address               VARCHAR(255),
    organization_admin_id VARCHAR(255)
);

-- Insert dummy organization
INSERT INTO organization (name, address, organization_admin_id)
VALUES ('Amsterdam UMC', 'Address of Amsterdam UMC', 'service-account-admin');

-- Create personnel table
CREATE TABLE personnel
(
    person_id       VARCHAR(255) PRIMARY KEY,
    organization_id INTEGER REFERENCES organization (organization_id) ON DELETE CASCADE,
    first_name      VARCHAR(255),
    last_name       VARCHAR(255),
    email           VARCHAR(255)
);

-- Insert dummy personnel
INSERT INTO personnel (person_id, organization_id, first_name, last_name, email)
VALUES ('study_owner', 1, 'John', 'Doe', 'study_owner@gmail.com'),
       ('data_engineer', 1, 'Okan', 'Mercan','data_engineer@gmail.com'),
       ('data_scientist', 1, 'Kerem', 'Yilmaz', 'data_scientist@gmail.com'),
       ('quality_assurance_specialist', 1, 'Anil', 'Sinaci',
        'quality_assurance_specialist@gmail.com'),
       ('survey_manager', 1, 'Senan', 'Postaci', 'survey_manager@gmail.com');

-- Create study table
CREATE TABLE study
(
    study_id    SERIAL PRIMARY KEY,
    name        VARCHAR(255),
    description TEXT,
    objectives  TEXT,
    ethics      TEXT,
    owner       VARCHAR(255) REFERENCES personnel (person_id)
);

-- Insert dummy study
INSERT INTO study (name, description, objectives, ethics, owner)
VALUES ('Risk score for acute HF in the emergency department', 'Predicting risk factors for acute HF…',
        'Evaluating the risk prediction for acute HF',
        'Approved by Ethical Board on 2023-01-15, Application Number: 123', 'study_owner');

-- Create population table
CREATE TABLE population
(
    population_id   SERIAL PRIMARY KEY,
    study_id        INTEGER REFERENCES study (study_id) ON DELETE CASCADE,
    population_url  VARCHAR(255),
    description     TEXT,
    characteristics TEXT
);

-- Insert dummy population
INSERT INTO population (study_id, population_url, description, characteristics)
VALUES (1, 'https://datatools4heart.eu/cohorts/study1',
        'Patients hospitalized with a primary discharge diagnosis of heart failure where the primary discharge diagnosis refers to the main reason for admission.',
        'The study population comprised 500 participants, evenly distributed between males and females, with seventy percent ranging between 20-30 years and the rest ranging between 40-50 years old.');

-- Create experiment table
CREATE TABLE experiment
(
    experiment_id     SERIAL PRIMARY KEY,
    study_id          INTEGER REFERENCES study (study_id) ON DELETE CASCADE,
    research_question TEXT
);

-- Insert dummy experiment
INSERT INTO experiment (study_id, research_question)
VALUES (1, 'A risk score prediction on subsequent (HF/CV)-rehospitalization within 7 days after hospital discharge.');

-- Create survey table
CREATE TABLE survey
(
    survey_id SERIAL PRIMARY KEY,
    study_id  INTEGER REFERENCES study (study_id) ON DELETE CASCADE,
    question  TEXT,
    answer    TEXT,
    category  VARCHAR(255)
);

-- Insert dummy survey
INSERT INTO survey (study_id, question, answer, category)
VALUES (1, 'Is this service tested by any third party?', 'Yes', 'Testing');

-- Create study_personnel table
CREATE TABLE study_personnel
(
    study_id     INTEGER REFERENCES study (study_id) ON DELETE CASCADE,
    personnel_id VARCHAR(255) REFERENCES personnel (person_id) ON DELETE CASCADE,
    role       VARCHAR(255) NOT NULL,
    PRIMARY KEY (study_id, personnel_id)
);

-- Insert dummy study_personnel with list of roles
INSERT INTO study_personnel (study_id, personnel_id, role)
VALUES (1, 'study_owner','STUDY_OWNER'),
       (1, 'data_scientist', 'DATA_SCIENTIST'),
       (1, 'survey_manager', 'SURVEY_MANAGER'),
       (1, 'quality_assurance_specialist', 'QUALITY_ASSURANCE_SPECIALIST'),
       (1, 'data_engineer', 'DATA_ENGINEER');


-- Create study_organization table
CREATE TABLE study_organization
(
    study_id                 INTEGER REFERENCES study (study_id) ON DELETE CASCADE,
    organization_id          INTEGER REFERENCES organization (organization_id) ON DELETE CASCADE,
    role                     VARCHAR(255),
    responsible_personnel_id VARCHAR(255) REFERENCES personnel (person_id) ON DELETE CASCADE,
    population_id            INTEGER REFERENCES population (population_id) ON DELETE CASCADE,
    PRIMARY KEY (study_id, organization_id)
);

-- Insert dummy study_organization
INSERT INTO study_organization (study_id, organization_id, role, responsible_personnel_id, population_id)
VALUES (1, 1, 'STUDY_OWNER,DATA_SCIENTIST,DATA_ENGINEER,DATA_SCIENTIST,SURVEY_MANAGER,QUALITY_ASSURANCE_SPECIALIST',
        'study_owner', 1);

-- Create FeatureSet table
CREATE TABLE featureset
(
    featureset_id   SERIAL PRIMARY KEY,
    experiment_id   INTEGER REFERENCES experiment (experiment_id) ON DELETE CASCADE,
    title           VARCHAR(255),
    featureset_url  VARCHAR(255),
    description     TEXT,
    created_at      TIMESTAMP,
    created_by      VARCHAR(255) REFERENCES personnel (person_id) ON DELETE CASCADE,
    last_updated_at TIMESTAMP,
    last_updated_by VARCHAR(255) REFERENCES personnel (person_id) ON DELETE CASCADE
);

-- Insert dummy FeatureSet
INSERT INTO featureset (experiment_id, title, featureset_url, description, created_at, created_by, last_updated_at,
                        last_updated_by)
VALUES (1, 'Feature set for AI4HFsubstudy 2 – Risk score prediction for acute HF in the emergency department.',
        'https://datatools4heart.eu/feature-sets/study1-features',
        'Feature set containing feature information used in risk score prediction for acute HF in the emergency department.',
        '2023-01-01 00:00:00', 'data_engineer', '2023-01-01 00:00:00', 'data_engineer');

-- Create Feature table
CREATE TABLE feature
(
    feature_id      SERIAL PRIMARY KEY,
    featureset_id   INTEGER REFERENCES featureset (featureset_id) ON DELETE CASCADE,
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

-- Insert dummy Feature
INSERT INTO feature (featureset_id, title, description, data_type, feature_type, mandatory, isUnique, units, equipment,
                     data_collection, created_at, created_by, last_updated_at, last_updated_by)
VALUES (1, 'age', 'Age of the patient at reference point (at the time of admission)', 'integer', 'numerical', true,
        false, 'years', '', 'Automatic Collection from Government Database', '2023-01-01 00:00:00', 'data_engineer',
        '2023-01-01 00:00:00', 'data_engineer');

-- Create Dataset table
CREATE TABLE dataset
(
    dataset_id       SERIAL PRIMARY KEY,
    featureset_id    INTEGER REFERENCES featureset (featureset_id) ON DELETE CASCADE,
    population_id    INTEGER REFERENCES population (population_id) ON DELETE CASCADE,
    organization_id  INTEGER REFERENCES organization (organization_id) ON DELETE CASCADE,
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

-- Insert dummy Dataset
INSERT INTO dataset (featureset_id, population_id, organization_id, title, description, version, reference_entity,
                     num_of_records, synthetic, created_at, created_by, last_updated_at, last_updated_by)
VALUES (1, 1, 1, 'HF Risk Dataset', 'Dataset for HF Risk Prediction factors', '0.1', 'Encounter', 1562, false,
        '2023-01-01 00:00:00', 'data_engineer', '2023-01-01 00:00:00', 'data_engineer');

-- Create DatasetTransformation table
CREATE TABLE dataset_transformation
(
    data_transformation_id SERIAL PRIMARY KEY,
    title                  VARCHAR(255),
    description            TEXT
);

-- Insert dummy DatasetTransformation
INSERT INTO dataset_transformation (title, description)
VALUES ('Dataset Smoothening and Normalization', 'Dataset is transformed by smoothening and normalization.');

-- Create DatasetTransformationStep table
CREATE TABLE dataset_transformation_step
(
    step_id                SERIAL PRIMARY KEY,
    data_transformation_id INTEGER REFERENCES dataset_transformation (data_transformation_id) ON DELETE CASCADE,
    input_features         VARCHAR(255),
    output_features        VARCHAR(255),
    method                 VARCHAR(255),
    explanation            TEXT,
    created_at             TIMESTAMP,
    created_by             VARCHAR(255) REFERENCES personnel (person_id) ON DELETE CASCADE,
    last_updated_at        TIMESTAMP,
    last_updated_by        VARCHAR(255) REFERENCES personnel (person_id) ON DELETE CASCADE
);

-- Insert dummy DatasetTransformationStep
INSERT INTO dataset_transformation_step (data_transformation_id, input_features, output_features, method, explanation,
                                         created_at, created_by, last_updated_at, last_updated_by)
VALUES (1, 'feature1', 'feature1_1', 'Normalization', 'Decimal values are normalized between 0 and 1.',
        '2023-01-01 00:00:00', 'data_engineer', '2023-01-01 00:00:00', 'data_engineer');

-- Create LearningDataset table
CREATE TABLE learning_dataset
(
    learning_dataset_id    SERIAL PRIMARY KEY,
    dataset_id             INTEGER REFERENCES dataset (dataset_id) ON DELETE CASCADE,
    study_id               INTEGER REFERENCES study (study_id) ON DELETE CASCADE,
    data_transformation_id INTEGER REFERENCES dataset_transformation (data_transformation_id) ON DELETE CASCADE,
    description            TEXT
);

-- Insert dummy LearningDataset
INSERT INTO learning_dataset (dataset_id, study_id, data_transformation_id, description)
VALUES (1, 1, 1, 'Finalized learning dataset for HF Risk Prediction Model Training');

-- Create FeatureDatasetCharacteristic table
CREATE TABLE feature_dataset_characteristic
(
    dataset_id          INTEGER REFERENCES dataset (dataset_id) ON DELETE CASCADE,
    feature_id          INTEGER REFERENCES feature (feature_id) ON DELETE CASCADE,
    characteristic_name VARCHAR(255),
    value               VARCHAR(255),
    value_data_type     VARCHAR(255),
    PRIMARY KEY (dataset_id, feature_id)
);

-- Insert dummy FeatureDatasetCharacteristic
INSERT INTO feature_dataset_characteristic (dataset_id, feature_id, characteristic_name, value, value_data_type)
VALUES (1, 1, 'variance', '11.2', 'decimal');

-- Create parameter table
CREATE TABLE parameter
(
    parameter_id SERIAL PRIMARY KEY,
    name         VARCHAR(255),
    study_id     INTEGER REFERENCES study (study_id) ON DELETE CASCADE,
    description  TEXT,
    data_type    VARCHAR(255)
);

-- Insert dummy parameter
INSERT INTO parameter (name, study_id, description, data_type)
VALUES ('test_parameter', 1, 'test_description', 'string');


-- Create algorithm table
CREATE TABLE algorithm
(
    algorithm_id       SERIAL PRIMARY KEY,
    name               VARCHAR(255),
    objective_function TEXT,
    type               VARCHAR(255),
    subtype            VARCHAR(255)
);

-- Insert dummy algorithms
INSERT INTO algorithm (name, objective_function, type, subtype)
VALUES ('Simple Linear Regression', 'Placeholder Objective Function', 'Regression', 'Simple Linear Regression'),
       ('Multiple Linear Regression', 'Placeholder Objective Function', 'Regression', 'Multiple Linear Regression'),
       ('Polynomial Regression', 'Placeholder Objective Function', 'Regression', 'Polynomial Regression'),
       ('Support Vector Regression (SVR)', 'Placeholder Objective Function', 'Regression',
        'Support Vector Regression (SVR)'),
       ('Decision Tree Regression', 'Placeholder Objective Function', 'Regression', 'Decision Tree Regression'),
       ('Random Forest Regression', 'Placeholder Objective Function', 'Regression', 'Random Forest Regression'),
       ('Logistic Regression', 'Placeholder Objective Function', 'Classification', 'Logistic Regression'),
       ('K-Nearest Neighbours (K-NN)', 'Placeholder Objective Function', 'Classification',
        'K-Nearest Neighbours (K-NN)'),
       ('Support Vector Machine (SVM)', 'Placeholder Objective Function', 'Classification',
        'Support Vector Machine (SVM)'),
       ('Kernel SVM', 'Placeholder Objective Function', 'Classification', 'Kernel SVM'),
       ('Naive Bayes', 'Placeholder Objective Function', 'Classification', 'Naive Bayes'),
       ('Decision Tree Classification', 'Placeholder Objective Function', 'Classification',
        'Decision Tree Classification'),
       ('Random Forest Classification', 'Placeholder Objective Function', 'Classification',
        'Random Forest Classification'),
       ('K-Means Clustering', 'Placeholder Objective Function', 'Clustering', 'K-Means Clustering'),
       ('Hierarchical Clustering', 'Placeholder Objective Function', 'Clustering', 'Hierarchical Clustering'),
       ('Apriori', 'Placeholder Objective Function', 'Association Rule Learning', 'Apriori'),
       ('Eclat', 'Placeholder Objective Function', 'Association Rule Learning', 'Eclat'),
       ('Upper Confidence Bounds (UCB)', 'Placeholder Objective Function', 'Reinforcement Learning',
        'Upper Confidence Bounds (UCB)'),
       ('Thompson Sampling', 'Placeholder Objective Function', 'Reinforcement Learning', 'Thompson Sampling'),
       ('Artificial Neural Networks (ANN)', 'Placeholder Objective Function', 'Deep Learning',
        'Artificial Neural Networks (ANN)'),
       ('Convolutional Neural Networks (CNN)', 'Placeholder Objective Function', 'Deep Learning',
        'Convolutional Neural Networks (CNN)'),
       ('Recurrent Neural Networks (RNN)', 'Placeholder Objective Function', 'Deep Learning',
        'Recurrent Neural Networks (RNN)'),
       ('Principal Component Analysis (PCA)', 'Placeholder Objective Function', 'Dimensionality Reduction',
        'Principal Component Analysis (PCA)'),
       ('Linear Discriminant Analysis (LDA)', 'Placeholder Objective Function', 'Dimensionality Reduction',
        'Linear Discriminant Analysis (LDA)'),
       ('Kernel PCA', 'Placeholder Objective Function', 'Dimensionality Reduction', 'Kernel PCA');

-- Create model implementation table
CREATE TABLE implementation
(
    implementation_id SERIAL PRIMARY KEY,
    algorithm_id      INTEGER REFERENCES algorithm (algorithm_id) ON DELETE CASCADE,
    software          TEXT,
    name              VARCHAR(255),
    description       TEXT
);

-- Insert dummy implementation
INSERT INTO implementation (algorithm_id, software, name, description)
VALUES (1, 'test_software', 'test_name', 'test_description');

-- Create model LearningProcess table
CREATE TABLE learning_process
(
    learning_process_id SERIAL PRIMARY KEY,
    study_id            INTEGER REFERENCES study (study_id) ON DELETE CASCADE,
    implementation_id   INTEGER REFERENCES implementation (implementation_id) ON DELETE CASCADE,
    description         TEXT
);

-- Insert dummy LearningProcess
INSERT INTO learning_process (study_id, implementation_id, description)
VALUES (1, 1, 'test_description');

-- Create model LearningStage table
CREATE TABLE learning_stage
(
    learning_stage_id   SERIAL PRIMARY KEY,
    learning_process_id INTEGER REFERENCES learning_process (learning_process_id) ON DELETE CASCADE,
    learning_stage_name VARCHAR(255),
    description         TEXT,
    dataset_percentage  INTEGER
);

-- Insert dummy LearningStage
INSERT INTO learning_stage (learning_process_id, learning_stage_name, description, dataset_percentage)
VALUES (1, 'test_name', 'test_description', 50);

-- Create LearningProcessDataset table
CREATE TABLE learning_process_dataset
(
    learning_process_id INTEGER REFERENCES learning_process (learning_process_id) ON DELETE CASCADE,
    learning_dataset_id INTEGER REFERENCES learning_dataset (learning_dataset_id) ON DELETE CASCADE,
    description         TEXT,
    PRIMARY KEY (learning_process_id, learning_dataset_id)
);

-- Insert dummy LearningProcessDataset
INSERT INTO learning_process_dataset (learning_process_id, learning_dataset_id, description)
VALUES (1, 1, 'This is a dummy description for the Learning Process Dataset relation.');

-- Create LearningProcessParameter table
CREATE TABLE learning_process_parameter
(
    learning_process_id INTEGER REFERENCES learning_process (learning_process_id) ON DELETE CASCADE,
    parameter_id        INTEGER REFERENCES parameter (parameter_id) ON DELETE CASCADE,
    type                VARCHAR(255),
    value               VARCHAR(255),
    PRIMARY KEY (learning_process_id, parameter_id)
);

-- Insert dummy LearningProcessParameter
INSERT INTO learning_process_parameter (learning_process_id, parameter_id, type, value)
VALUES (1, 1, 'string', 'Dummy value for Learning Process Parameter');

-- Create LearningStageParameter table
CREATE TABLE learning_stage_parameter
(
    learning_stage_id INTEGER REFERENCES learning_stage (learning_stage_id) ON DELETE CASCADE,
    parameter_id      INTEGER REFERENCES parameter (parameter_id) ON DELETE CASCADE,
    type              VARCHAR(255),
    value             VARCHAR(255),
    PRIMARY KEY (learning_stage_id, parameter_id)
);

-- Insert dummy LearningStageParameter
INSERT INTO learning_stage_parameter (learning_stage_id, parameter_id, type, value)
VALUES (1, 1, 'string', 'Dummy value for Learning Stage Parameter');

-- Create model table
CREATE TABLE model
(
    model_id               SERIAL PRIMARY KEY,
    learning_process_id    INTEGER REFERENCES learning_process (learning_process_id) ON DELETE CASCADE,
    study_id               INTEGER REFERENCES study (study_id) ON DELETE CASCADE,
    name                   VARCHAR(255),
    version                VARCHAR(255),
    tag                    VARCHAR(255),
    model_type             VARCHAR(255),
    product_identifier     VARCHAR(255),
    owner                  INTEGER REFERENCES organization (organization_id) ON DELETE CASCADE,
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

-- Insert dummy model
INSERT INTO model (learning_process_id, study_id, name, version, tag, model_type, product_identifier,
                   owner, trl_level, license, primary_use, secondary_use, intended_users, counter_indications,
                   ethical_considerations, limitations, fairness_constraints, created_at, created_by,
                   last_updated_at, last_updated_by)
VALUES (1, 1, 'test_name', 'test_version', 'test_tag', 'test_model_type', 'test_product_identifier', 1,
        'test_trl_level', 'test_license', 'test_primary_use', 'test_secondary_use', 'test_intended_users',
        'test_counter_indications', 'test_ethical_considerations', 'test_limitations', 'test_fariness_constraints',
        '2023-01-01 00:00:00', 'data_scientist', '2023-01-02 00:00:00', 'data_scientist');


-- Create deployment_environment table
CREATE TABLE deployment_environment
(
    environment_id       SERIAL PRIMARY KEY,
    title                VARCHAR(255),
    description          TEXT,
    hardware_properties  TEXT,
    software_properties  TEXT,
    connectivity_details TEXT
);

-- Insert dummy deployment_environment
INSERT INTO deployment_environment (title, description, hardware_properties, software_properties, connectivity_details)
VALUES ('Production Environment', 'Main Production Environment', 'Disk: 512 GB, RAM: 32 GB',
        'OS: Windows, Cloud Services: Google Cloud Platform',
        'Secure HTTPS communication is established using TLS/SSL protocols. The environment is configured with a firewall allowing communication on ports 80 and 443. API endpoints are accessible via a private subnet, and external access is restricted to authorized IP addresses. Communication between services is encrypted, and access control is managed through role-based authentication');

-- Create model_deployment table
CREATE TABLE model_deployment
(
    deployment_id       SERIAL PRIMARY KEY,
    model_id            INTEGER REFERENCES model (model_id),
    environment_id      INTEGER REFERENCES deployment_environment (environment_id),
    tags                VARCHAR(255),
    identified_failures TEXT,
    status              VARCHAR(255),
    created_at          TIMESTAMP,
    created_by          VARCHAR(255) REFERENCES personnel (person_id),
    last_updated_at     TIMESTAMP,
    last_updated_by     VARCHAR(255) REFERENCES personnel (person_id)
);

-- Insert dummy model_deployment
INSERT INTO model_deployment (model_id, environment_id, tags, identified_failures, status, created_at, created_by,
                              last_updated_at, last_updated_by)
VALUES (1, 1, 'Production', 'Instances of false positives in predicting rare events.', 'RUNNING', '2023-01-01 00:00:00',
        'data_scientist', '2023-01-01 00:00:00', 'data_scientist');

-- Create passport table
CREATE TABLE passport
(
    passport_id   SERIAL PRIMARY KEY,
    deployment_id INTEGER REFERENCES model_deployment (deployment_id) ON DELETE CASCADE,
    study_id      INTEGER REFERENCES study (study_id) ON DELETE CASCADE,
    created_at    TIMESTAMP,
    created_by    VARCHAR(255) REFERENCES personnel (person_id) ON DELETE CASCADE,
    approved_at   TIMESTAMP,
    approved_by   VARCHAR(255) REFERENCES personnel (person_id) ON DELETE CASCADE,
    details_json  JSONB
);

-- Insert dummy passport with proper example data in details_json
INSERT INTO passport (deployment_id, study_id, created_at, created_by, approved_at, approved_by, details_json)
VALUES
    (1, 1, '2023-01-01 00:00:00', 'quality_assurance_specialist', '2023-01-01 00:00:00', 'quality_assurance_specialist',
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
         "studyId": 1,
         "name": "Risk score for acute HF in the emergency department",
         "description": "Predicting risk factors for acute HF…",
         "objectives": "Evaluating the risk prediction for acute HF",
         "ethics": "Approved by Ethical Board on 2023-01-15, Application Number: 123"
       },
       "parameters": [
         {
           "name": "test_parameter",
           "description": "test_description",
           "dataType": "string"
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
           "researchQuestion": "A risk score prediction on subsequent HF rehospitalization within 7 days after discharge."
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
             "title": "Feature set for AI4HFsubstudy 2",
             "description": "Feature set containing feature information used in risk score prediction for acute HF."
           },
           "features": [
             {
               "title": "age",
               "description": "Age of the patient at the time of admission",
               "dataType": "integer",
               "featureType": "numerical",
               "mandatory": true,
               "isUnique": false,
               "units": "years",
               "dataCollection": "Automatic Collection"
             }
           ]
         }
       ],
       "learningProcessesWithStages": [
         {
           "learningProcess": {
             "description": "test_description"
           },
           "learningStages": [
             {
               "learningStageName": "test_name",
               "description": "test_description",
               "datasetPercentage": 50
             }
           ]
         }
       ]
     }');
