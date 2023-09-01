import os
from flask import Flask, request

app = Flask(__name__)

# Define the full path to the directory where you want to save uploaded files
upload_folder = 'insert directory where you want to save images'

@app.route('/upload', methods=['POST'])
def upload_image():
    if 'file' not in request.files:
        return 'No file part', 400

    file = request.files['file']
    if file.filename == '':
        return 'No selected file', 400

    # Save the uploaded file to the specified directory
    file.save(os.path.join(upload_folder, file.filename))

    return 'File uploaded successfully', 200

if __name__ == '__main__':
    app.run(host="0.0.0.0", port=5000, debug=True)