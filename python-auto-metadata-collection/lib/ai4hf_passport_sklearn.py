from ai4hf_passport_base import BaseMetadataCollectionAPI
from ai4hf_passport_models import Model
from typing import Dict, Any
from sklearn.base import BaseEstimator
import numpy as np

class SKLearnMetadataCollectionAPI(BaseMetadataCollectionAPI):
    """
    Scikit-learn library implementation for interacting AI4HF Passport Server.
    """
    def __init__(self, passport_server_url: str, study_id: str, organization_id: str, username: str, password: str):
        """
        Initialize the API client with authentication and study details.
        """
        super().__init__(passport_server_url, study_id, organization_id, username, password)

    def extract_learning_process(self, model: BaseEstimator) -> Dict[str, Any]:
        """
        Extracts metadata from a Scikit-learn model 
        and structures it into a learning process object.

        :param model: Trained model BaseEstimator

        :return dictionary: JSON-like dictionary representing the learning process
        """
        learning_process = {
            "implementation": {
                "name": type(model).__name__,
                "software": "Scikit-learn",
                "description": f"Training and evaluating {type(model).__name__}.",
                "algorithm": {
                    "name": "Unknown",
                    "objectiveFunction": "Unknown",
                    "type": "Unknown",
                    "subType": "Unknown"
                }
            },
            "description": f"Training and evaluating {type(model).__name__}."
        }

        # **Handle Scikit-learn models**
        learning_process["implementation"]["algorithm"]["name"] = getattr(model, "_estimator_type", "Unknown").capitalize()
        learning_process["implementation"]["algorithm"]["type"] = getattr(model, "_estimator_type", "Unknown").capitalize()
        learning_process["implementation"]["algorithm"]["subType"] = model.__class__.__name__
        
        # Extract scoring function if applicable
        if hasattr(model, "criterion"):
            learning_process["implementation"]["algorithm"]["objectiveFunction"] = (
                model.criterion if isinstance(model.criterion, str) else str(model.criterion)
            )
        elif hasattr(model, "scoring"):
            learning_process["implementation"]["algorithm"]["objectiveFunction"] = (
                model.scoring if isinstance(model.scoring, str) else str(model.scoring)
            )
        
        return learning_process
    
    def extract_model(self,
        model: BaseEstimator,
        model_info: Model
    ) -> Model:
        """
        Extracts metadata for a given machine learning model and structures it into a Model object.

        :param model: The trained Scikit-learn model (BaseEstimator) to extract metadata from.
        :param model_info: The details of the model.

        :return Model: Completed model object.
        """
        # Infer model type based on estimator type
        model_type = getattr(model, "_estimator_type", "Unknown").capitalize()

        # Compile the model
        extracted_model = Model(
            learningProcessId = model_info.learningProcessId,
            studyId = model_info.studyId,
            name = (model_info.name or type(model).__name__),
            version = model_info.version,
            tag =  model_info.tag,
            modelType = model_type,
            productIdentifier = model_info.productIdentifier,
            owner = model_info.owner,
            trlLevel = model_info.trlLevel,
            license = model_info.license,
            primaryUse = model_info.primaryUse,
            secondaryUse = model_info.secondaryUse,
            intendedUsers = model_info.intendedUsers,
            counterIndications = model_info.counterIndications,
            ethicalConsiderations = model_info.ethicalConsiderations,
            limitations = model_info.limitations,
            fairnessConstraints = model_info.fairnessConstraints,
            createdBy = model_info.createdBy,
            lastUpdatedBy = model_info.lastUpdatedBy
        )

        return extracted_model

    def extract_parameters(self,
        model: BaseEstimator
    ) -> list[Dict[str, Any]]:
        """
        Extracts parameters for a given machine learning model and structures it into list of JSON-like dictionaries.

        :param model: The trained Scikit-learn model (BaseEstimator) to extract parameters from.

        :return dictionary: A list of dictionary representing the extracted parameters from the model.
        """

        parameter_list = []

        # Extract hyperparameters
        model_hyperparameters = model.get_params()
        for name, value in model_hyperparameters.items():
            parameter_list.append({
            "studyId": self.study_id,
            "name": name,
            "dataType": str(type(value).__name__),
            "description": f"{name} parameter of the model",
            "type": "hyperparameter",
            "value": str(value)
            })

        # Extract coefficients (Linear Models: LogisticRegression, LinearRegression, SVM)
        if hasattr(model, "coef_"):
            coef_array = np.atleast_1d(model.coef_)  # Ensure it's an array
            for i, coef in enumerate(coef_array.flatten()):  # Flatten in case of multi-class classification
                parameter_list.append({
                    "studyId": self.study_id,
                    "name": f"Coefficient {i}",
                    "dataType": str(type(coef).__name__),
                    "description": f"Coefficient for feature {i}",
                    "type": "parameter",
                    "value": str(coef)
                })

        # Extract intercept (for linear models)
        if hasattr(model, "intercept_"):
            intercept_array = np.atleast_1d(model.intercept_)
            for i, intercept in enumerate(intercept_array):
                parameter_list.append({
                    "studyId": self.study_id,
                    "name": f"Intercept {i}",
                    "dataType": str(type(intercept).__name__),
                    "description": f"Intercept value for Intercept {i}",
                    "type": "parameter",
                    "value": str(intercept)
                })

        # Extract feature importances (for tree-based models)
        if hasattr(model, "feature_importances_"):
            feature_importances = model.feature_importances_
            for i, importance in enumerate(feature_importances):
                parameter_list.append({
                    "studyId": self.study_id,
                    "name": f"Feature {i} importance",
                    "dataType": str(type(importance).__name__),
                    "description": f"Feature importance for feature {i}",
                    "type": "parameter",
                    "value": str(importance)
                })

        # Extract support vectors (for SVM models)
        if hasattr(model, "support_vectors_"):
            support_vectors = model.support_vectors_
            for i, vector in enumerate(support_vectors):
                parameter_list.append({
                    "studyId": self.study_id,
                    "name": f"Support vector {i}",
                    "dataType": "numpy.ndarray",
                    "description": f"Support vector at index {i}",
                    "type": "parameter",
                    "value": str(vector.tolist())  # Convert NumPy array to list for serialization
                })

        # Extract cluster centers (for clustering models like KMeans)
        if hasattr(model, "cluster_centers_"):
            cluster_centers = model.cluster_centers_
            for i, center in enumerate(cluster_centers):
                parameter_list.append({
                    "studyId": self.study_id,
                    "name": f"Cluster center {i}",
                    "dataType": "numpy.ndarray",
                    "description": f"Center of cluster {i}",
                    "type": "parameter",
                    "value": str(center.tolist())  # Convert NumPy array to list
                })

        # Extract nearest neighbors (for KNN models)
        if hasattr(model, "_fit_X"):
            training_instances = model._fit_X
            for i, instance in enumerate(training_instances):
                parameter_list.append({
                    "studyId": self.study_id,
                    "name": f"Nearest neighbor {i}",
                    "dataType": "numpy.ndarray",
                    "description": f"Training instance {i} used for KNN",
                    "type": "parameter",
                    "value": str(instance.tolist())
                })

        # Extract neural network weights (for MLPClassifier/MLPRegressor)
        if hasattr(model, "coefs_"):
            for i, layer_weights in enumerate(model.coefs_):
                parameter_list.append({
                    "studyId": self.study_id,
                    "name": f"Layer {i} weights",
                    "dataType": "numpy.ndarray",
                    "description": f"Weights for layer {i}",
                    "type": "parameter",
                    "value": str(layer_weights.tolist())  # Convert NumPy array to list
                })

        # Extract neural network biases
        if hasattr(model, "intercepts_"):
            for i, layer_bias in enumerate(model.intercepts_):
                parameter_list.append({
                    "studyId": self.study_id,
                    "name": f"Layer {i} biases",
                    "dataType": "numpy.ndarray",
                    "description": f"Biases for layer {i}",
                    "type": "parameter",
                    "value": str(layer_bias.tolist())  # Convert NumPy array to list
                })

        return parameter_list