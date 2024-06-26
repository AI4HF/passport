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
                           organization_id SERIAL REFERENCES organization(organization_id),
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
                         owner SERIAL REFERENCES personnel(person_id)
);

-- Insert dummy study
INSERT INTO study (name, description, objectives, ethics, owner) VALUES
                                                                                    ('Risk score for acute HF in the emergency department', 'Predicting risk factors for acute HF…', 'Evaluating the risk prediction for acute HF', 'Approved by Ethical Board on 2023-01-15, Application Number: 123', 1);
