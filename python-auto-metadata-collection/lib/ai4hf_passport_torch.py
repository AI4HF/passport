from ai4hf_passport_base import BaseMetadataCollectionAPI
from ai4hf_passport_models import Model
from typing import Dict, Any
import torch

class TorchMetadataCollectionAPI(BaseMetadataCollectionAPI):
    """
    Torch library implementation for interacting AI4HF Passport Server.
    """
    def __init__(self, passport_server_url: str, study_id: str, organization_id: str, username: str, password: str):
        """
        Initialize the API client with authentication and study details.
        """
        super().__init__(passport_server_url, study_id, organization_id, username, password)

        
    def extract_learning_process(self, model: torch.nn.Module) -> Dict[str, Any]:
        """
        Extracts metadata from a Torch model 
        and structures it into a learning process object.

        :param model: Trained model torch.nn.Module

        :return dictionary: JSON-like dictionary representing the learning process
        """
        learning_process = {
            "implementation": {
                "name": type(model).__name__,
                "software": "PyTorch",
                "description": f"Training and evaluating {type(model).__name__}.",
                "algorithm": {
                    "name": "Neural Network",
                    "objectiveFunction": "Unknown",
                    "type": "Deep Learning",
                    "subType": "Unknown"
                },
                "architecture": []
            },
            "description": f"Training and evaluating {type(model).__name__}."
        }

        # Identify model type based on architecture
        layer_types = [layer.__class__.__name__ for layer in model.modules() if not isinstance(layer, torch.nn.Sequential)]

        if any("LSTM" in layer for layer in layer_types):
            learning_process["implementation"]["algorithm"]["subType"] = "Recurrent Neural Network (LSTM)"
        elif any("GRU" in layer for layer in layer_types):
            learning_process["implementation"]["algorithm"]["subType"] = "Recurrent Neural Network (GRU)"
        elif any("Conv" in layer for layer in layer_types):
            learning_process["implementation"]["algorithm"]["subType"] = "Convolutional Neural Network (CNN)"
        elif any("Transformer" in layer for layer in layer_types):
            learning_process["implementation"]["algorithm"]["subType"] = "Transformer-based Model"
        elif any("Attention" in layer for layer in layer_types):
            learning_process["implementation"]["algorithm"]["subType"] = "Attention-based Model"
        else:
            learning_process["implementation"]["algorithm"]["subType"] = "Feedforward Neural Network (MLP)"

        # Extract model architecture details
        layers_info = []
        for idx, layer in enumerate(model.modules()):
            if not isinstance(layer, torch.nn.Sequential):  # Avoid redundant entries
                layers_info.append({
                    "name": str(idx),
                    "type": layer.__class__.__name__,
                    "activation": getattr(layer, "activation", None).__class__.__name__ if hasattr(layer, "activation") else "None",
                    "params": sum(p.numel() for p in layer.parameters())
                })

        learning_process["implementation"]["architecture"] = layers_info

        return learning_process

    def extract_model(self,
        model: torch.nn.Module,
        model_info: Model
    ) -> Model:
        """
        Extracts metadata for a given PyTorch model and structures it into a Model object.

        :param model: The trained PyTorch model (torch.nn.Module) to extract metadata from.
        :param model_info: The details of the model.

        :return Model: Completed model object.
        """
        
        # Extract model architecture
        layers_info = []
        for name, layer in model.named_children():
            layers_info.append({
                "name": name,
                "type": layer.__class__.__name__,
                "params": sum(p.numel() for p in layer.parameters() if p.requires_grad)
            })
        
        # Identify model type based on architecture
        model_type = "Unknown"
        layer_types = [layer.__class__.__name__ for layer in model.modules() if not isinstance(layer, torch.nn.Sequential)]

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

    def extract_parameters(self, model: torch.nn.Module) -> list[Dict[str, Any]]:
        """
        Extracts parameters from a PyTorch model, including hyperparameters, layer details, weights, biases, 
        and gradient-related properties.

        :param model: The trained PyTorch model.

        :return dictionary: A list of dictionary representing the extracted parameters from the model.
        """
        parameter_list = []

        # Extract detailed information from each layer
        for i, (name, layer) in enumerate(model.named_modules()):
            if i == 0:  # Skip the root model itself
                continue

            parameter_list.append({
                "studyId": self.study_id,
                "name": f"Layer {name} type",
                "dataType": "string",
                "description": f"Layer {name} type",
                "type": "layer",
                "value": str(layer.__class__.__name__)
            })

            # Extract specific layer parameters
            layer_params = layer.__dict__
            for key, value in layer_params.items():
                if isinstance(value, (int, float, str, bool)):
                    parameter_list.append({
                        "studyId": self.study_id,
                        "name": f"Layer {name} {key}",
                        "dataType": str(type(value).__name__),
                        "description": f"Layer {name} {key}",
                        "type": "layer parameter",
                        "value": str(value)
                    })

            # Extract weights and biases
            if hasattr(layer, 'weight') and layer.weight is not None:
                parameter_list.append({
                    "studyId": self.study_id,
                    "name": f"Layer {name} Weights",
                    "dataType": "tensor",
                    "description": f"Weight matrix for Layer {name}",
                    "type": "parameter",
                    "value": str(layer.weight.detach().cpu().numpy().tolist())
                })

            if hasattr(layer, 'bias') and layer.bias is not None:
                parameter_list.append({
                    "studyId": self.study_id,
                    "name": f"Layer {name} Biases",
                    "dataType": "tensor",
                    "description": f"Bias vector for Layer {name}",
                    "type": "parameter",
                    "value": str(layer.bias.detach().cpu().numpy().tolist())
                })

        # Extract trainable parameters
        for param_name, param in model.named_parameters():
            parameter_list.append({
                "studyId": self.study_id,
                "name": f"Parameter {param_name}",
                "dataType": "tensor",
                "description": f"Trainable parameter {param_name}",
                "type": "parameter",
                "value": str(param.detach().cpu().numpy().tolist())
            })

            # Extract gradient-related information
            parameter_list.append({
                "studyId": self.study_id,
                "name": f"Gradient {param_name}",
                "dataType": "tensor",
                "description": f"Gradient of parameter {param_name}",
                "type": "parameter",
                "value": str(param.grad.cpu().numpy().tolist()) if param.grad is not None else "None"
            })

            parameter_list.append({
                "studyId": self.study_id,
                "name": f"Parameter {param_name} requires grad",
                "dataType": "bool",
                "description": f"Indicates if {param_name} requires gradient",
                "type": "parameter",
                "value": str(param.requires_grad)
            })

            parameter_list.append({
                "studyId": self.study_id,
                "name": f"Parameter {param_name} is leaf",
                "dataType": "bool",
                "description": f"Indicates if {param_name} is a leaf tensor",
                "type": "parameter",
                "value": str(param.is_leaf)
            })

        return parameter_list