package luutronghieu.doan.service;

import ai.djl.ModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.transform.CenterCrop;
import ai.djl.modality.cv.transform.Normalize;
import ai.djl.modality.cv.transform.Resize;
import ai.djl.modality.cv.transform.ToTensor;
import ai.djl.modality.cv.translator.ImageClassificationTranslator;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.TranslateException;
import ai.djl.translate.Translator;
import lombok.extern.slf4j.Slf4j;
import luutronghieu.doan.dto.ClassificationResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ImageClassificationService {

    @Value("${ai.classification.model.path}")
    private String modelPath;

    @Value("${ai.classification.labels.path}")
    private String labelsPath;

    private ZooModel<Image, Classifications> model;
    private Predictor<Image, Classifications> predictor;
    private List<String> classNames;

    @PostConstruct
    public void initialize() {
        log.info("Initializing MobileNetV2 ONNX model from: {}", modelPath);

        try {
            Path modelFile = Paths.get(modelPath);
            Path labelFile = Paths.get(labelsPath);

            if (!modelFile.toFile().exists()) {
                throw new IOException("Model file not found at: " + modelFile.toAbsolutePath());
            }
            if (!labelFile.toFile().exists()) {
                throw new IOException("Label file not found at: " + labelFile.toAbsolutePath());
            }

            // Load class names from synset.txt
            classNames = java.nio.file.Files.readAllLines(labelFile);

            Translator<Image, Classifications> translator = ImageClassificationTranslator.builder()
                    .addTransform(new Resize(256))
                    .addTransform(new CenterCrop(224, 224))
                    .addTransform(new ToTensor())
                    .addTransform(new Normalize(
                            new float[]{0.485f, 0.456f, 0.406f},
                            new float[]{0.229f, 0.224f, 0.225f}))
                    .optSynset(classNames)
                    .optApplySoftmax(true)
                    .build();

            Criteria<Image, Classifications> criteria = Criteria.builder()
                    .setTypes(Image.class, Classifications.class)
                    .optModelPath(modelFile)
                    .optTranslator(translator)
                    .optEngine("OnnxRuntime")
                    .build();

            log.info("Loading ONNX classification model...");
            this.model = ModelZoo.loadModel(criteria);
            this.predictor = model.newPredictor();
            log.info("MobileNetV2 ONNX model loaded successfully.");

        } catch (Exception e) {
            log.error("Failed to load classification model: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to load classification model: " + e.getMessage(), e);
        }
    }

    public boolean isModelLoaded() {
        return predictor != null;
    }

    public Classifications runInference(byte[] imageBytes) throws IOException, ModelException, TranslateException {
        if (!isModelLoaded()) {
            throw new IllegalStateException("Classification model not loaded");
        }
        Image img = ImageFactory.getInstance().fromInputStream(new ByteArrayInputStream(imageBytes));
        return predictor.predict(img);
    }

    public ClassificationResult convertToClassificationResult(Classifications classifications) {
        long startTime = System.currentTimeMillis();
        ClassificationResult result = new ClassificationResult();
        result.setStatus("success");
        result.setModelName("MobileNetV2");

        List<ClassificationResult.Classification> resultList = classifications.items().stream()
                .map(item -> new ClassificationResult.Classification(item.getClassName(), item.getProbability()))
                .collect(Collectors.toList());

        result.setClassifications(resultList);

        if (!resultList.isEmpty()) {
            result.setMessage("Top classification: " + resultList.get(0).getClassName());
        } else {
            result.setMessage("No classification result");
        }
        result.setInferenceTimeMs(System.currentTimeMillis() - startTime);
        return result;
    }

    @PreDestroy
    public void close() {
        if (predictor != null) {
            predictor.close();
        }
        if (model != null) {
            model.close();
        }
        log.info("Classification model resources released.");
    }
}