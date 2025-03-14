# Machine Learning Metadata Extraction Library
This library provides a unified way to extract metadata from machine learning models built using Scikit-learn, TensorFlow/Keras, and PyTorch. It also includes a base API client to facilitate interaction with AI4HF Passport Server. Extracted metadata can be sent to AI4HF Passport Server.

## Usage
Install necessary dependencies:
```
pip install pandas scikit-learn torch tensorflow request pyjwt
```
Deploy the Passport server before running the API client. To deploy the Passport server and a proxy:
```
cd ../../
sh passport/docker/deployment/run.sh
sh passport/docker/proxy/run.sh
```
After completing your machine learning work, provide the trained model object to this library. The library will automatically extract essential metadata from the model, while additional metadata must be supplied by the developer. Ensure that the `learning_stages`, `evaluation_measures`, and `model_info` variables are created as shown in the example below. This code snippet should be executed after your machine learning pipeline. For full examples, refer to the `main_keras.py`, `main_sklearn.py`, and `main_torch.py` files.
```python
# Construct an api client(you can choose one of them: KerasMetadaCollectionAPI, TorchMetadataCollectionAPI or SKLearnMetadataCollectionAPI) for interacting with AI4HF passport server
api_client = KerasMetadataCollectionAPI(
        passport_server_url="http://localhost:80/ai4hf/passport/api",
        study_id="1",
        organization_id="1",
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

# Call this function with your model object
api_client.submit_results_to_ai4hf_passport(model, learning_stages, evaluation_measures, model_info)
```