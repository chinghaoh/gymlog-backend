package com.gymlog.record;

import com.gymlog.set.WorkoutSet;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Comparator;


import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PersonalRecordService {

    private final PersonalRecordRepository personalRecordRepository;

    private PersonalRecordDto mapToDto(PersonalRecord pr) {
        PersonalRecordDto dto = new PersonalRecordDto();
        dto.setId(pr.getId());
        dto.setUserId(pr.getUser().getId());
        dto.setExerciseId(pr.getExercise().getId());
        dto.setExerciseName(pr.getExercise().getName());
        dto.setCategory(pr.getExercise().getCategory());
        dto.setWorkoutId(pr.getWorkout().getId());
        dto.setWeight(pr.getWeight());
        dto.setReps(pr.getReps());
        dto.setAchievedAt(pr.getAchievedAt());
        return dto;
    }

    @Transactional
    private boolean isNewPersonalRecord(WorkoutSet set, Optional<PersonalRecord> currentPr) {
        if (currentPr.isEmpty()) {
            return true;
        }
        return set.getWeight().compareTo(currentPr.get().getWeight()) > 0;
    }

    private void savePersonalRecord(WorkoutSet set,
                                    Optional<PersonalRecord> currentPr) {

        if (currentPr.isPresent()) {
            PersonalRecord pr = currentPr.get();
            pr.setWeight(set.getWeight());
            pr.setReps(set.getReps());
            pr.setWorkout(set.getWorkout());
            pr.setAchievedAt(java.time.LocalDateTime.now());
            personalRecordRepository.save(pr);
        } else {
            PersonalRecord pr = PersonalRecord.builder()
                    .user(set.getWorkout().getUser())
                    .exercise(set.getExercise())
                    .workout(set.getWorkout())
                    .weight(set.getWeight())
                    .reps(set.getReps())
                    .build();
            personalRecordRepository.save(pr);
        }
    }

    @Transactional
    public boolean checkForPersonalRecord(WorkoutSet savedSet) {

        Long userId = savedSet.getWorkout().getUser().getId();
        Long exerciseId = savedSet.getExercise().getId();

        Optional<PersonalRecord> currentPr = personalRecordRepository
                .findByUserIdAndExerciseId(userId, exerciseId);

        boolean isNewPr = isNewPersonalRecord(savedSet, currentPr);

        if (isNewPr) {
            savePersonalRecord(savedSet, currentPr);
            return true;
        }
        return false;
    }

    @Transactional(readOnly = true)
    public List<PersonalRecordDto> getPersonalRecordByUserId(Long userId){
        return personalRecordRepository
                .findByUserIdOrderByAchievedAtDesc(userId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PersonalRecordDto> getPrsByUserAndExercise(
            Long userId, Long exerciseId) {
        return personalRecordRepository
                .findByUserIdAndExerciseIdOrderByAchievedAtDesc(userId, exerciseId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

}
