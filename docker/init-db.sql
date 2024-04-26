-- Create organizations table
CREATE TABLE organizations (
                               id SERIAL PRIMARY KEY,
                               organization_id VARCHAR(255) NOT NULL,
                               name VARCHAR(255),
                               address VARCHAR(255)
);

-- Insert dummy organizations
INSERT INTO organizations (organization_id, name, address) VALUES
                                                               ('ai4hf_aumc', 'Amsterdam UMC', 'Address of Amsterdam UMC');

-- Create personnel table
CREATE TABLE personnel (
                           id SERIAL PRIMARY KEY,
                           person_id VARCHAR(255) NOT NULL,
                           organization_id VARCHAR(255) REFERENCES organizations(organization_id),
                           first_name VARCHAR(255),
                           last_name VARCHAR(255),
                           role VARCHAR(255),
                           email VARCHAR(255)
);

-- Insert dummy personnel
INSERT INTO personnel (person_id, organization_id, first_name, last_name, role, email) VALUES
                                                                                           ('person1', 'ai4hf_aumc', 'John', 'Doe', 'Data Scientist', 'John.doe@emailhost.com');

-- Create studies table
CREATE TABLE studies (
                         id SERIAL PRIMARY KEY,
                         study_id VARCHAR(255) NOT NULL,
                         name VARCHAR(255),
                         description TEXT,
                         objectives TEXT,
                         ethics TEXT,
                         owner_id VARCHAR(255) REFERENCES personnel(person_id)
);

-- Insert dummy studies
INSERT INTO studies (study_id, name, description, objectives, ethics, owner_id) VALUES
                                                                                    ('study1', 'Risk score for acute HF in the emergency department', 'Predicting risk factors for acute HF…', 'Evaluating the risk prediction for acute HF', 'Approved by Ethical Board on 2023-01-15, Application Number: 123', 'person1');
