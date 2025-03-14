import json
from enum import Enum

class Algorithm:
    def __init__(self, name: str, objectiveFunction: str, type: str, subType: str, algorithmId: str):
        """
        Initialize the Algorithm object from arguments.
        """
        self.name = name
        self.objectiveFunction = objectiveFunction
        self.type = type
        self.subType = subType
        self.algorithmId = algorithmId

    def __str__(self):
        return json.dumps({"algorithmId": self.algorithmId, "name": self.name, "objectiveFunction": self.objectiveFunction, "type": self.type, 
                          "subType": self.subType})
    
class Implementation:
    def __init__(self, name: str, software: str, description: str, algorithmId: str, implementationId: str):
        """
        Initialize the Implementation object from arguments.
        """
        self.name = name
        self.software = software
        self.description = description
        self.algorithmId = algorithmId
        self.implementationId = implementationId

    def __str__(self):
        return json.dumps({"name": self.name, "software": self.software, "description": self.description, "algorithmId": self.algorithmId, 
                          "implementationId": self.implementationId})

class LearningProcess:
    def __init__(self, learningProcessId: str, studyId: str, implementationId: str, description: str):
        """
        Initialize the LearningProcess object from arguments.
        """
        self.learningProcessId = learningProcessId
        self.studyId = studyId
        self.implementationId = implementationId
        self.description = description
    
    def __str__(self):
        return json.dumps({"learningProcessId": self.learningProcessId, "studyId": self.studyId, "implementationId": self.implementationId, "description": self.description})

class LearningStageType(Enum):
    """
        Type of learning stages. It contains three type: TEST, VALIDATION, TRAINING
    """
    TEST = "test"
    VALIDATION = "validation"
    TRAINING = "training"

class LearningStage:
    def __init__(self, learningStageType: LearningStageType, datasetPercentage: int):
        """
        Initialize the LearningStage object from learning stage type and dataset percentage.
        """
        if learningStageType == LearningStageType.TEST:
            self.learningStageName = "Model Testing"
            self.description = "Trains the model on the dataset"
        elif learningStageType == LearningStageType.TRAINING:
            self.learningStageName = "Model Training"
            self.description = "Training the model on the dataset"
        elif learningStageType == LearningStageType.VALIDATION:
            self.learningStageName = "Model Validation"
            self.description = "Validation dataset of the model"
        
        self.datasetPercentage = datasetPercentage
        self.learningStageId = None
        self.learningProcessId = None

    def __str__(self):
        return json.dumps({"learningStageId": self.learningStageId, "learningProcessId": self.learningProcessId, "learningStageName": self.learningStageName, "datasetPercentage": self.datasetPercentage, "description": self.description})
    
class Model:
    def __init__(self,  
                 version: str = "",
                 tag: str = "",
                 productIdentifier: str = "",
                 trlLevel:str = "",
                 license: str = "",
                 primaryUse: str = "",
                 secondaryUse: str = "",
                 intendedUsers: str = "",
                 counterIndications: str = "",
                 ethicalConsiderations: str = "",
                 limitations: str = "",
                 fairnessConstraints: str = "",
                 createdBy: str = None,
                 lastUpdatedBy: str = None,
                 modelId: str = None, 
                 learningProcessId: str = None, 
                 studyId: str = None,
                 name: str = None,
                 owner: str = None,
                 modelType: str = None):
        """
        Initialize the Model object from arguments.
        """
        self.modelId = modelId
        self.learningProcessId = learningProcessId
        self.studyId = studyId
        self.name = name
        self.version = version
        self.tag = tag
        self.modelType = modelType
        self.productIdentifier = productIdentifier
        self.owner = owner
        self.trlLevel = trlLevel
        self.license = license
        self.primaryUse = primaryUse
        self.secondaryUse = secondaryUse
        self.intendedUsers = intendedUsers
        self.counterIndications = counterIndications
        self.ethicalConsiderations = ethicalConsiderations
        self.limitations = limitations
        self.fairnessConstraints = fairnessConstraints
        self.createdBy = createdBy
        self.lastUpdatedBy = lastUpdatedBy

    def __str__(self):
        return json.dumps({"modelId": self.modelId, 
                           "learningProcessId": self.learningProcessId, 
                           "studyId": self.studyId, 
                           "name": self.name, 
                           "version": self.version,
                           "tag": self.tag,
                           "modelType": self.modelType,
                           "productIdentifier": self.productIdentifier,
                           "owner": self.owner,
                           "trlLevel": self.trlLevel,
                           "license": self.license,
                           "primaryUse": self.primaryUse,
                           "secondaryUse": self.secondaryUse,
                           "intendedUsers": self.intendedUsers,
                           "counterIndications": self.counterIndications,
                           "ethicalConsiderations": self.ethicalConsiderations,
                           "limitations": self.limitations,
                           "fairnessConstraints": self.fairnessConstraints,
                           "createdBy": self.createdBy,
                           "lastUpdatedBy": self.lastUpdatedBy})


class EvaluationMeasureType(Enum):
    """
        Type of evaluation metrics. It contains basic ones for regression and classification algorithms
    """
    TRUE_POSITIVES = "true_positives"
    TRUE_NEGATIVES = "true_negatives"
    FALSE_POSITIVES = "false_positives"
    FALSE_NEGATIVES = "false_negatives"
    ACCURACY = "accuracy"
    AVERAGE_ACCURACY = "average_accuracy"
    ERROR_RATE = "error_rate"
    PRECISION = "precision"
    RECALL = "recall"
    SPECIFICITY = "specificity"
    FALSE_POSITIVE_RATE = "false_positive_rate"
    FALSE_NEGATIVE_RATE = "false_negative_rate"
    F1_SCORE = "f1_score"
    ROC = "roc"
    AUC = "auc"
    MAE = "mae"
    MSE = "mse"
    RMSE = "rmse"
    RMSLE = "rmsle"
    R_SQUARED = "r_squared"
    ADJUSTED_R_SQUARED = "adjusted_r_squared"

class EvaluationMeasure:
    def __init__(self, evaluationMeasureType: EvaluationMeasureType, value: str):
        """
        Initialize the EvaluationMeasure object from evaluation measure type and the value.
        """
        self.name = str(evaluationMeasureType)
        self.value = value
        self.dataType = "float"
        self.description = f"{evaluationMeasureType} of the model"
        self.measureId = None
        self.modelId = None

    def __str__(self):
        return json.dumps({"measureId": self.measureId, "modelId": self.modelId, "name": self.name, "value": self.value, "dataType": self.dataType, "description": self.description})
    
class Parameter:
    def __init__(self, parameterId: str, studyId: str, name: str, dataType: str, description: str):
        """
        Initialize the Parameter object from arguments.
        """
        self.parameterId = parameterId
        self.studyId = studyId
        self.name = name
        self.dataType = dataType
        self.description = description

    def __str__(self):
        return json.dumps({"parameterId": self.parameterId, "studyId": self.studyId, "name": self.name, "dataType": self.dataType, "description": self.description})

class ModelParameter:
    def __init__(self, modelId: str, parameterId: str, type: str, value: str):
        """
        Initialize the ModelParameter object from arguments.
        """
        self.modelId = modelId
        self.parameterId = parameterId
        self.type = type
        self.value = value
    
    def __str__(self):
        return json.dumps({"modelId": self.modelId, "parameterId": self.parameterId, "type": self.type, "value": self.value})

