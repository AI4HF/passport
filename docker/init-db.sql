-- Create organization table
CREATE TABLE organization (
                              organization_id SERIAL PRIMARY KEY,
                              name VARCHAR(255),
                              address VARCHAR(255)
);

-- Insert dummy organization
INSERT INTO organization (name, address) VALUES
    ('Amsterdam UMC', 'Address of Amsterdam UMC');

-- Create personnel table
CREATE TABLE personnel (
                           person_id VARCHAR(255) PRIMARY KEY,
                           organization_id INTEGER REFERENCES organization(organization_id) ON DELETE CASCADE,
                           first_name VARCHAR(255),
                           last_name VARCHAR(255),
                           role VARCHAR(255),
                           email VARCHAR(255)
);

-- Insert dummy personnel
INSERT INTO personnel (person_id, organization_id, first_name, last_name, role, email) VALUES
    ('service-account-admin', 1, 'John', 'Doe', 'STUDY_OWNER', 'John.doe@emailhost.com');

-- Create study table
CREATE TABLE study (
                       study_id SERIAL PRIMARY KEY,
                       name VARCHAR(255),
                       description TEXT,
                       objectives TEXT,
                       ethics TEXT,
                       owner VARCHAR(255) REFERENCES personnel(person_id)
);

-- Insert dummy study
INSERT INTO study (name, description, objectives, ethics, owner) VALUES
    ('Risk score for acute HF in the emergency department', 'Predicting risk factors for acute HF…', 'Evaluating the risk prediction for acute HF', 'Approved by Ethical Board on 2023-01-15, Application Number: 123', 'service-account-admin');

-- Create population table
CREATE TABLE population (
                            population_id SERIAL PRIMARY KEY,
                            study_id INTEGER REFERENCES study(study_id) ON DELETE CASCADE,
                            population_url VARCHAR(255),
                            description TEXT,
                            characteristics TEXT
);

-- Insert dummy population
INSERT INTO population (study_id, population_url, description, characteristics) VALUES
    (1, 'https://datatools4heart.eu/cohorts/study1', 'Patients hospitalized with a primary discharge diagnosis of heart failure where the primary discharge diagnosis refers to the main reason for admission.', 'The study population comprised 500 participants, evenly distributed between males and females, with seventy percent ranging between 20-30 years and the rest ranging between 40-50 years old.');

-- Create experiment table
CREATE TABLE experiment (
                            experiment_id SERIAL PRIMARY KEY,
                            study_id INTEGER REFERENCES study(study_id) ON DELETE CASCADE,
                            research_question TEXT
);

-- Insert dummy experiment
INSERT INTO experiment (study_id, research_question) VALUES
    (1, 'A risk score prediction on subsequent (HF/CV)-rehospitalization within 7 days after hospital discharge.');

-- Create survey table
CREATE TABLE survey (
                        survey_id SERIAL PRIMARY KEY,
                        study_id INTEGER REFERENCES study(study_id) ON DELETE CASCADE,
                        question TEXT,
                        answer TEXT,
                        category VARCHAR(255)
);

-- Insert dummy survey
INSERT INTO survey (study_id, question, answer, category) VALUES
    (1, 'Is this service tested by any third party?', 'Yes', 'Testing');

-- Create study_personnel table
CREATE TABLE study_personnel (
                                 study_id INTEGER REFERENCES study(study_id) ON DELETE CASCADE,
                                 personnel_id VARCHAR(255) REFERENCES personnel(person_id) ON DELETE CASCADE,
                                 role VARCHAR(255),
                                 PRIMARY KEY (study_id, personnel_id)
);

-- Insert dummy study_personnel
INSERT INTO study_personnel (study_id, personnel_id, role) VALUES
    (1, 'service-account-admin', 'STUDY_OWNER');

-- Create study_organization table
CREATE TABLE study_organization (
                                    study_id INTEGER REFERENCES study(study_id) ON DELETE CASCADE,
                                    organization_id INTEGER REFERENCES organization(organization_id) ON DELETE CASCADE,
                                    role VARCHAR(255),
                                    responsible_personnel_id VARCHAR(255) REFERENCES personnel(person_id) ON DELETE CASCADE,
                                    population_id INTEGER REFERENCES population(population_id) ON DELETE CASCADE,
                                    PRIMARY KEY (study_id, organization_id)
);

-- Insert dummy study_organization
INSERT INTO study_organization (study_id, organization_id, role, responsible_personnel_id, population_id) VALUES
    (1, 1, 'DATA_PROVIDER', 'service-account-admin', 1);

-- Create FeatureSet table
CREATE TABLE featureset (
                            featureset_id SERIAL PRIMARY KEY,
                            experiment_id INTEGER REFERENCES experiment(experiment_id) ON DELETE CASCADE,
                            title VARCHAR(255),
                            featureset_url VARCHAR(255),
                            description TEXT,
                            created_at TIMESTAMP,
                            created_by INTEGER REFERENCES personnel(person_id) ON DELETE CASCADE,
                            last_updated_at TIMESTAMP,
                            last_updated_by INTEGER REFERENCES personnel(person_id) ON DELETE CASCADE
);

-- Insert dummy FeatureSet
INSERT INTO featureset (experiment_id, title, featureset_url, description, created_at, created_by, last_updated_at, last_updated_by) VALUES
    (1, 'Feature set for AI4HFsubstudy 2 – Risk score prediction for acute HF in the emergency department.', 'https://datatools4heart.eu/feature-sets/study1-features', 'Feature set containing feature information used in risk score prediction for acute HF in the emergency department.', '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 1);

-- Create Feature table
CREATE TABLE feature (
                         feature_id SERIAL PRIMARY KEY,
                         featureset_id INTEGER REFERENCES featureset(featureset_id) ON DELETE CASCADE,
                         title VARCHAR(255),
                         description TEXT,
                         data_type VARCHAR(255),
                         feature_type VARCHAR(255),
                         mandatory BOOLEAN,
                         isUnique BOOLEAN,
                         units VARCHAR(255),
                         equipment VARCHAR(255),
                         data_collection VARCHAR(255),
                         created_at TIMESTAMP,
                         created_by INTEGER REFERENCES personnel(person_id) ON DELETE CASCADE,
                         last_updated_at TIMESTAMP,
                         last_updated_by INTEGER REFERENCES personnel(person_id) ON DELETE CASCADE
);

-- Insert dummy Feature
INSERT INTO feature (featureset_id, title, description, data_type, feature_type, mandatory, isUnique, units, equipment, data_collection, created_at, created_by, last_updated_at, last_updated_by) VALUES
    (1, 'age', 'Age of the patient at reference point (at the time of admission)', 'integer', 'numerical', true, false, 'years', '', 'Automatic Collection from Government Database', '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 1);

-- Create Dataset table
CREATE TABLE dataset (
                         dataset_id SERIAL PRIMARY KEY,
                         featureset_id INTEGER REFERENCES featureset(featureset_id) ON DELETE CASCADE,
                         population_id INTEGER REFERENCES population(population_id) ON DELETE CASCADE,
                         organization_id INTEGER REFERENCES organization(organization_id) ON DELETE CASCADE,
                         title VARCHAR(255),
                         description TEXT,
                         version VARCHAR(50),
                         reference_entity VARCHAR(255),
                         num_of_records INTEGER,
                         synthetic BOOLEAN,
                         created_at TIMESTAMP,
                         created_by INTEGER REFERENCES personnel(person_id) ON DELETE CASCADE,
                         last_updated_at TIMESTAMP,
                         last_updated_by INTEGER REFERENCES personnel(person_id) ON DELETE CASCADE
);

-- Insert dummy Dataset
INSERT INTO dataset (featureset_id, population_id, organization_id, title, description, version, reference_entity, num_of_records, synthetic, created_at, created_by, last_updated_at, last_updated_by) VALUES
    (1, 1, 1, 'HF Risk Dataset', 'Dataset for HF Risk Prediction factors', '0.1', 'Encounter', 1562, false, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 1);

-- Create DatasetTransformation table
CREATE TABLE dataset_transformation (
                                        data_transformation_id SERIAL PRIMARY KEY,
                                        title VARCHAR(255),
                                        description TEXT
);

-- Insert dummy DatasetTransformation
INSERT INTO dataset_transformation (title, description) VALUES
    ('Dataset Smoothening and Normalization', 'Dataset is transformed by smoothening and normalization.');

-- Create DatasetTransformationStep table
CREATE TABLE dataset_transformation_step (
                                             step_id SERIAL PRIMARY KEY,
                                             data_transformation_id INTEGER REFERENCES dataset_transformation(data_transformation_id) ON DELETE CASCADE,
                                             input_features VARCHAR(255),
                                             output_features VARCHAR(255),
                                             method VARCHAR(255),
                                             explanation TEXT,
                                             created_at TIMESTAMP,
                                             created_by INTEGER REFERENCES personnel(person_id) ON DELETE CASCADE,
                                             last_updated_at TIMESTAMP,
                                             last_updated_by INTEGER REFERENCES personnel(person_id) ON DELETE CASCADE
);

-- Insert dummy DatasetTransformationStep
INSERT INTO dataset_transformation_step (data_transformation_id, input_features, output_features, method, explanation, created_at, created_by, last_updated_at, last_updated_by) VALUES
    (1, 'feature1', 'feature1_1', 'Normalization', 'Decimal values are normalized between 0 and 1.', '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 1);

-- Create LearningDataset table
CREATE TABLE learning_dataset (
                                  learning_dataset_id SERIAL PRIMARY KEY,
                                  dataset_id INTEGER REFERENCES dataset(dataset_id) ON DELETE CASCADE,
                                  data_transformation_id INTEGER REFERENCES dataset_transformation(data_transformation_id) ON DELETE CASCADE,
                                  description TEXT
);

-- Insert dummy LearningDataset
INSERT INTO learning_dataset (dataset_id, data_transformation_id, description) VALUES
    (1, 1, 'Finalized learning dataset for HF Risk Prediction Model Teaching');

-- Create FeatureDatasetCharacteristic table
CREATE TABLE feature_dataset_characteristic (
                                                dataset_id INTEGER REFERENCES dataset(dataset_id) ON DELETE CASCADE,
                                                feature_id INTEGER REFERENCES feature(feature_id) ON DELETE CASCADE,
                                                characteristic_name VARCHAR(255),
                                                value DECIMAL,
                                                value_data_type VARCHAR(255),
                                                PRIMARY KEY (dataset_id, feature_id)
);

-- Insert dummy FeatureDatasetCharacteristic
INSERT INTO feature_dataset_characteristic (dataset_id, feature_id, characteristic_name, value, value_data_type) VALUES
    (1, 1, 'variance', 11.2, 'decimal');

-- Create parameter table
CREATE TABLE parameter (
                            parameter_id SERIAL PRIMARY KEY,
                            name VARCHAR(255),
                            description TEXT,
                            data_type VARCHAR(255)
);

-- Insert dummy parameter
INSERT INTO parameter (parameter_id, name, description, data_type) VALUES
    (1, 'test_parameter', 'test_description', 'string');