-- Create organizations table
CREATE TABLE organizations (
                               id SERIAL PRIMARY KEY,
                               organization_id VARCHAR(255) NOT NULL,
                               name VARCHAR(255),
                               address VARCHAR(255)
);

-- Insert dummy organizations
INSERT INTO organizations (organization_id, name, address) VALUES
                                                               ('org1', 'Organization One', '123 Main St'),
                                                               ('org2', 'Organization Two', '456 Elm St');

-- Create personnel table
CREATE TABLE personnel (
                           id SERIAL PRIMARY KEY,
                           person_id VARCHAR(255) NOT NULL,
                           organization_id INTEGER REFERENCES organizations(id),
                           first_name VARCHAR(255),
                           last_name VARCHAR(255),
                           role VARCHAR(255),
                           email VARCHAR(255)
);

-- Insert dummy personnel
INSERT INTO personnel (person_id, organization_id, first_name, last_name, role, email) VALUES
                                                                                           ('p1', 1, 'John', 'Doe', 'Manager', 'john.doe@example.com'),
                                                                                           ('p2', 2, 'Jane', 'Smith', 'Coordinator', 'jane.smith@example.com');

-- Create studies table
CREATE TABLE studies (
                         id SERIAL PRIMARY KEY,
                         study_id VARCHAR(255) NOT NULL,
                         name VARCHAR(255),
                         description TEXT,
                         objectives TEXT,
                         ethics TEXT,
                         owner_id INTEGER REFERENCES personnel(id)
);

-- Insert dummy studies
INSERT INTO studies (study_id, name, description, objectives, ethics, owner_id) VALUES
                                                                                    ('study1', 'Study One', 'Description of Study One', 'Objectives of Study One', 'Ethics of Study One', 1),
                                                                                    ('study2', 'Study Two', 'Description of Study Two', 'Objectives of Study Two', 'Ethics of Study Two', 2);
