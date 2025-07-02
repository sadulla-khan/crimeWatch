package com.example.crime;

import android.content.res.AssetManager;
import ai.onnxruntime.*;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CrimePredictor {

    private OrtEnvironment env;
    private OrtSession session;

    public CrimePredictor(AssetManager assetManager) throws Exception {
        env = OrtEnvironment.getEnvironment();

        // Load model from assets
        InputStream inputStream = assetManager.open("crime_model.onnx");
        byte[] modelBytes = new byte[inputStream.available()];
        inputStream.read(modelBytes);
        inputStream.close();

        session = env.createSession(modelBytes);
    }

    public float predictCrime(float[] inputFeatures) throws Exception {
        float[][] input2D = new float[][]{ inputFeatures }; // wrap into 2D array
        OnnxTensor inputTensor = OnnxTensor.createTensor(env, input2D);


        Map<String, OnnxTensor> inputs = new HashMap<>();
        inputs.put("input", inputTensor);

        OrtSession.Result output = session.run(inputs);
        float[][] outputArray = (float[][]) output.get(0).getValue();

        return outputArray[0][0];  // Your predicted crime count
    }
}