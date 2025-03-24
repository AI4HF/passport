import sys
import os

# Add 'lib' directory to Python's module search path
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "lib")))

from ai4hf_passport_sklearn import SKLearnMetadataCollectionAPI
from ai4hf_passport_models import LearningStage, EvaluationMeasure, Model, LearningStageType, EvaluationMeasureType
import pandas as pd

# Example usage of sklearn library
dataset = pd.read_csv('../test-data/Social_Network_Ads.csv')
X = dataset.iloc[:, :-1].values
y = dataset.iloc[:, -1].values

from sklearn.model_selection import train_test_split
X_train, X_test, y_train, y_test = train_test_split(X, y, test_size = 0.30, random_state = 0)

from sklearn.preprocessing import StandardScaler
sc = StandardScaler()
X_train = sc.fit_transform(X_train)
X_test = sc.transform(X_test)

from sklearn.tree import DecisionTreeClassifier
classifier = DecisionTreeClassifier(criterion = 'entropy', random_state = 0)
classifier.fit(X_train, y_train)

# Construct an api client for interacting with AI4HF passport server
api_client = SKLearnMetadataCollectionAPI(
        passport_server_url="http://localhost:80/ai4hf/passport/api",
        study_id="initial_study",
        organization_id="initial_organization",
        username="data_scientist",
        password="data_scientist"
    )

# Provide learning stages
learning_stages = [
    LearningStage(learningStageType = LearningStageType.TRAINING,
                  datasetPercentage = 70),
    LearningStage(learningStageType = LearningStageType.TEST,
                  datasetPercentage = 30)
]

# Provide evaluation measures
evaluation_measures = [
    EvaluationMeasure(EvaluationMeasureType.ACCURACY,
                      value = "0.94"),
    EvaluationMeasure(EvaluationMeasureType.F1_SCORE,
                      value = "0.88")
]

# Provide model details
model_info = Model(
    name = "test"
)

#Call this function with your model object
api_client.submit_results_to_ai4hf_passport(classifier, learning_stages, evaluation_measures, model_info)
