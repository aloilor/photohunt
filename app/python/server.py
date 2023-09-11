import os
import sys
from flask import Flask, request
import numpy as np
from PIL import Image
import requests
import matplotlib.pyplot as plt

from keras.preprocessing import image
from keras.applications.resnet50 import ResNet50, preprocess_input, decode_predictions

import ssl
ssl._create_default_https_context = ssl._create_unverified_context

model_resnet50 = ResNet50(weights='imagenet')

target_size = (224, 224)




app = Flask(__name__)

# Define the full path to the directory where you want to save uploaded files
upload_folder = './'

@app.route('/upload', methods=['POST'])
def upload_image():
    if 'file' not in request.files:
        return 'No file part', 400

    file = request.files['file']
    if file.filename == '':
        return 'No selected file', 400

    
    # Save the uploaded file to the specified directory
    file.save(os.path.join(upload_folder, file.filename))

    check = check_image(file)

    if check: return 'Object found!', 200
    else: return 'Wrong object!', 250


@app.route('/image_recognition', methods = ['PUT'])
def check_image(file):
    
    """Run model_resnet50 prediction on image
    Args:
        model_resnet50: keras model_resnet50
        img: PIL format image
        target_size: (w,h) tuple
        top_n: # of top predictions to return
    Returns:
    list of predicted labels and their probabilities
    """

    objectToCheck = file.filename.split("-")[1].split(".")[0]

    #print(objectToCheck)
    #print(upload_folder+file.filename)
    
    predictions = []
    
    img = Image.open(upload_folder+file.filename)
    top_n = 5
    if img.size != target_size:
        img = img.resize(target_size)

    img = img.rotate(-90)
    img.show()

    x = image.img_to_array(img)
    x = np.expand_dims(x, axis=0)
    x = preprocess_input(x)
    preds = model_resnet50.predict(x)
    preds = decode_predictions(preds, top=top_n)[0]
    for it in preds:
        predictions.append(it[1]) 
        if (objectToCheck in it[1]): return True
    print(predictions)
    return False

    

if __name__ == '__main__':
    app.run(host="0.0.0.0", port=5000, debug=True)