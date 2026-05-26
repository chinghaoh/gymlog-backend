package com.gymlog.exercise;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/admin/migrate")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class GifMigrationController {

    private final ExerciseRepository exerciseRepository;
    private final S3Client s3Client;

    @Value("${workoutx.api.key}")
    private String apiKey;

    @Value("${aws.s3.bucket:gymlog-frontend}")
    private String bucketName;

    @Value("${app.cloudfront-url:https://d29bpvkdmyn1cv.cloudfront.net}")
    private String cloudfrontUrl;

    @PostMapping("/gifs")
    public ResponseEntity<String> migrateGifs() throws Exception {
        List<Exercise> exercises = exerciseRepository.findAll()
                .stream()
                .filter(e -> e.getGifUrl() != null && e.getGifUrl().contains("workoutxapp.com"))
                .toList();

        log.info("Found {} exercises to migrate", exercises.size());

        HttpClient httpClient = HttpClient.newHttpClient();
        int success = 0;
        int failed = 0;

        for (Exercise exercise : exercises) {
            try {
                String gifUrl = exercise.getGifUrl();
                String filename = gifUrl.substring(gifUrl.lastIndexOf('/') + 1);

                String downloadUrl = gifUrl + "?api-key=" + apiKey;
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(downloadUrl))
                        .GET()
                        .build();

                HttpResponse<byte[]> response = httpClient.send(
                        request, HttpResponse.BodyHandlers.ofByteArray());

                if (response.statusCode() != 200) {
                    log.warn("Failed to download GIF for {}: status {}", exercise.getName(), response.statusCode());
                    failed++;
                    continue;
                }

                String s3Key = "gifs/" + filename;
                s3Client.putObject(
                        PutObjectRequest.builder()
                                .bucket(bucketName)
                                .key(s3Key)
                                .contentType("image/gif")
                                .build(),
                        RequestBody.fromBytes(response.body())
                );

                exercise.setGifUrl(cloudfrontUrl + "/" + s3Key);
                exerciseRepository.save(exercise);

                success++;
                log.info("Migrated {} → {}", exercise.getName(), s3Key);

                // rate limit — 300ms between requests
                Thread.sleep(300);

            } catch (Exception e) {
                log.error("Error migrating GIF for {}: ", exercise.getName(), e);
                failed++;
            }
        }

        String result = "Migration complete — success: " + success + ", failed: " + failed;
        log.info(result);
        return ResponseEntity.ok(result);
    }
}