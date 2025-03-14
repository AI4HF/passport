from ai4hf_passport_base import BaseMetadataCollectionAPI
from typing import Dict, Any
from tensorflow.keras.models import Model
import ai4hf_passport_models

class KerasMetadataCollectionAPI(BaseMetadataCollectionAPI):
    """
    Keras library implementation for interacting AI4HF Passport Server.
    """
    def __init__(self, passport_server_url: str, study_id: str, organization_id: str, username: str, password: str):
        """
        Initialize the API client with authentication and study details.
        """
        super().__init__(passport_server_url, study_id, organization_id, username, password)

        
    def extract_learning_process(self, model: Model) -> Dict[str, Any]:
        """
        Extracts metadata from a Keras model 
        and structures it into a learning process object.

        :param model: Trained model tensorflow.keras.models

        :return dictionary: JSON-like dictionary representing the learning process
        """
        learning_process = {
            "implementation": {
                "name": model.__class__.__name__,
                "software": "TensorFlow/Keras",
                "description": f"Training and evaluating {model.__class__.__name__}.",
                "algorithm": {
                    "name": "Neural Network",
                    "objectiveFunction": getattr(model, "loss", "Unknown"),
                    "type": "Deep Learning",
                    "subType": "Unknown"
                },
                "hyperparameters": {},
                "architecture": []
            },
            "description": f"Training and evaluating {model.__class__.__name__}."
        }

        # Extract architecture
        learning_process["implementation"]["architecture"] = [
                {
                    "name": layer.name,
                    "type": layer.__class__.__name__,
                    "activation": getattr(layer, "activation", None).__name__ if hasattr(layer, "activation") else "None",
                    "params": layer.count_params()
                }
                for layer in model.layers
            ]
        
        # Extract hyperparameters
        learning_process["implementation"]["hyperparameters"] = {
            "loss_function": getattr(model, "loss", "Unknown"),
            "optimizer": getattr(model.optimizer, "get_config", lambda: "Unknown")(),
        }

        # Infer subtype based on architecture
        layer_types = [layer["type"] for layer in learning_process["implementation"]["architecture"]]

        if any("LSTM" in layer for layer in layer_types):
            learning_process["implementation"]["algorithm"]["subType"] = "Recurrent Neural Network (LSTM)"
        elif any("Conv" in layer for layer in layer_types):
            learning_process["implementation"]["algorithm"]["subType"] = "Convolutional Neural Network (CNN)"
        elif any("Transformer" in layer for layer in layer_types):
            learning_process["implementation"]["algorithm"]["subType"] = "Transformer-based Model"
        elif any("GRU" in layer for layer in layer_types):
            learning_process["implementation"]["algorithm"]["subType"] = "Recurrent Neural Network (GRU)"
        elif any("Attention" in layer for layer in layer_types):
            learning_process["implementation"]["algorithm"]["subType"] = "Attention-based Model"
        else:
            learning_process["implementation"]["algorithm"]["subType"] = "Feedforward Neural Network (MLP)"

        return learning_process
    
    def extract_model(self,
        model: Model,
        model_info: ai4hf_passport_models.Model
    ) -> ai4hf_passport_models.Model:
        """
        Extracts metadata for a given machine learning model and structures it into a Model object.

        :param model: The trained keras model (tensorflow.keras.models) to extract metadata from.
        :param model_info: The details of the model.

        :return Model: Completed model object.
        """
        # Determine model type based on layers
        layer_types = [layer.__class__.__name__ for layer in model.layers]
        model_type = "Unknown"
        if any("LSTM" in layer for layer in layer_types):
            model_type = "Recurrent Neural Network (LSTM)"
        elif any("GRU" in layer for layer in layer_types):
            model_type = "Recurrent Neural Network (GRU)"
        elif any("Conv" in layer for layer in layer_types):
            model_type = "Convolutional Neural Network (CNN)"
        elif any("Transformer" in layer for layer in layer_types):
            model_type = "Transformer-based Model"
        elif any("Attention" in layer for layer in layer_types):
            model_type = "Attention-based Model"
        else:
            model_type = "Feedforward Neural Network (MLP)"

        # Compile the model
        extracted_model = ai4hf_passport_models.Model(
            learningProcessId = model_info.learningProcessId,
            studyId = model_info.studyId,
            name = (model_info.name or model.__class__.__name__),
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

    def extract_parameters(self, model: Model) -> list[Dict[str, Any]]:
        """
        Extracts parameters from a Keras model, including hyperparameters, layer details, weights, and biases.

        :param model: The trained Keras model.

        :return dictionary: A list of dictionary representing the extracted parameters from the model.
        """
        parameter_list = []

        # Extract optimizer, loss, and metrics (if compiled)
        if model.optimizer:
            optimizer_config = model.optimizer.get_config()
            for key, value in optimizer_config.items():
                parameter_list.append({
                    "studyId": self.study_id,
                    "name": f"{key}",
                    "dataType": str(type(value).__name__),
                    "description": f"Optimizer of the model",
                    "type": "hyperparameter",
                    "value": str(value)
                })

        if hasattr(model, "loss") and model.loss:
            parameter_list.append({
                "studyId": self.study_id,
                "name": "Loss Function",
                "dataType": str(type(model.loss).__name__),
                "description": "Loss function of the model",
                "type": "hyperparameter",
                "value": str(model.loss)
            })

        if hasattr(model, "metrics") and model.metrics:
            for metric in model.metrics:
                parameter_list.append({
                    "studyId": self.study_id,
                    "name": f"{metric.name}",
                    "dataType": "string",
                    "description": "Evaluation metric of the model",
                    "type": "hyperparameter",
                    "value": str(metric.name)
                })

        # Extract information from each layer
        for i, layer in enumerate(model.layers):
            layer_config = layer.get_config()
            parameter_list.append({
                "studyId": self.study_id,
                "name": f"Layer {i}",
                "dataType": "string",
                "description": f"Layer {i} {layer.__class__.__name__}",
                "type": "hyperparameter",
                "value": str(layer.__class__.__name__)
            })

            # Extract specific layer parameters (neurons, activation, etc.)
            for key, value in layer_config.items():
                parameter_list.append({
                    "studyId": self.study_id,
                    "name": f"Layer {i} {key}",
                    "dataType": str(type(value).__name__),
                    "description": f"Layer {i} {key}",
                    "type": "hyperparameter",
                    "value": str(value)
                })

            # Extract weights and biases
            weights = layer.get_weights()
            if len(weights) > 0:
                parameter_list.append({
                    "studyId": self.study_id,
                    "name": f"Layer {i} Weights",
                    "dataType": "numpy.ndarray",
                    "description": f"Weight matrix for Layer {i}",
                    "type": "parameter",
                    "value": str(weights[0].tolist())  # Convert to list for serialization
                })

            if len(weights) > 1:
                parameter_list.append({
                    "studyId": self.study_id,
                    "name": f"Layer {i} Biases",
                    "dataType": "numpy.ndarray",
                    "description": f"Bias vector for Layer {i}",
                    "type": "parameter",
                    "value": str(weights[1].tolist())  # Convert to list for serialization
                })

        return parameter_list