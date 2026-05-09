package com.gymlog.record;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/records")
@RequiredArgsConstructor
public class PersonalRecordController {

    private final PersonalRecordService personalRecordService;

    @GetMapping
    public ResponseEntity<List<PersonalRecordDto>> getPersonalRecords(
            @RequestParam Long userId,
            @RequestParam(required = false) Long exerciseId) {

        if (exerciseId != null) {
            return ResponseEntity.ok(
                    personalRecordService.getPrsByUserAndExercise(userId, exerciseId));
        }

        return ResponseEntity.ok(
                personalRecordService.getPersonalRecordByUserId(userId));
    }
}
