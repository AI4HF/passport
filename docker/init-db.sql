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
                           person_id SERIAL PRIMARY KEY,
                           organization_id INTEGER REFERENCES organization(organization_id) ON DELETE CASCADE,
                           first_name VARCHAR(255),
                           last_name VARCHAR(255),
                           role VARCHAR(255),
                           email VARCHAR(255)
);

-- Insert dummy personnel
INSERT INTO personnel (organization_id, first_name, last_name, role, email) VALUES
    (1, 'John', 'Doe', 'Data Scientist', 'John.doe@emailhost.com');

-- Create study table
CREATE TABLE study (
                       study_id SERIAL PRIMARY KEY,
                       name VARCHAR(255),
                       description TEXT,
                       objectives TEXT,
                       ethics TEXT,
                       owner INTEGER REFERENCES personnel(person_id)
);

-- Insert dummy study
INSERT INTO study (name, description, objectives, ethics, owner) VALUES
    ('Risk score for acute HF in the emergency department', 'Predicting risk factors for acute HF…', 'Evaluating the risk prediction for acute HF', 'Approved by Ethical Board on 2023-01-15, Application Number: 123', 1);

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
                                 personnel_id INTEGER REFERENCES personnel(person_id) ON DELETE CASCADE,
                                 role VARCHAR(255),
                                 PRIMARY KEY (study_id, personnel_id)
);

-- Insert dummy study_personnel
INSERT INTO study_personnel (study_id, personnel_id, role) VALUES
    (1, 1, 'Data Scientist');

-- Create study_organization table
CREATE TABLE study_organization (
                                    study_id INTEGER REFERENCES study(study_id) ON DELETE CASCADE,
                                    organization_id INTEGER REFERENCES organization(organization_id) ON DELETE CASCADE,
                                    role VARCHAR(255),
                                    responsible_personnel_id INTEGER REFERENCES personnel(person_id) ON DELETE CASCADE,
                                    population_id INTEGER REFERENCES population(population_id) ON DELETE CASCADE,
                                    PRIMARY KEY (study_id, organization_id)
);

-- Insert dummy study_organization
INSERT INTO study_organization (study_id, organization_id, role, responsible_personnel_id, population_id) VALUES
    (1, 1, 'Data Provider', 1, 1);
