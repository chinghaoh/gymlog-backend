package com.gymlog.exercise;

import com.gymlog.common.SearchCriteria;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExerciseService {

    private final ExerciseRepository exerciseRepository;

    private ExerciseDto mapToDto(Exercise exercise) {
        ExerciseDto dto = new ExerciseDto();
        dto.setId(exercise.getId());
        dto.setName(exercise.getName());
        dto.setCategory(exercise.getCategory());
        dto.setEquipment(exercise.getEquipment());
        dto.setDifficulty(exercise.getDifficulty());
        dto.setExerciseType(exercise.getExerciseType());
        dto.setDescription(exercise.getDescription());
        dto.setTargetMuscle(exercise.getTargetMuscle());
        dto.setSecondaryMuscles(exercise.getSecondaryMuscles());
        dto.setGifUrl(exercise.getGifUrl());
        dto.setIsSeeded(exercise.getIsSeeded());
        dto.setIsActive(exercise.getIsActive());
        dto.setAiCreated(exercise.getAiCreated());
        return dto;
    }

    @Transactional(readOnly = true)
    public List<ExerciseDto> getExercises(String category, String equipment,
                                          String difficulty, String name) {

        Specification<Exercise> spec = Specification.where(
                new ExerciseSpecification(new SearchCriteria("isActive", "=", true))
        );

        if (category != null && !category.isBlank()) {
            spec = spec.and(new ExerciseSpecification(
                    new SearchCriteria("category", "=", category)));
        }
        if (equipment != null && !equipment.isBlank()) {
            spec = spec.and(new ExerciseSpecification(
                    new SearchCriteria("equipment", "=", equipment)));
        }
        if (difficulty != null && !difficulty.isBlank()) {
            spec = spec.and(new ExerciseSpecification(
                    new SearchCriteria("difficulty", "=", difficulty)));
        }
        if (name != null && !name.isBlank()) {
            spec = spec.and(new ExerciseSpecification(
                    new SearchCriteria("name", "=", name)));
        }

        return exerciseRepository.findAll(spec)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ExerciseDto getExerciseById(Long id) {
        return mapToDto(findExerciseOrThrow(id));
    }

    @Transactional
    public ExerciseDto createExercise(ExerciseDto dto) {
        Exercise exercise = Exercise.builder()
                .name(dto.getName())
                .category(dto.getCategory())
                .equipment(dto.getEquipment())
                .difficulty(dto.getDifficulty())
                .exerciseType(dto.getExerciseType())
                .description(dto.getDescription())
                .targetMuscle(dto.getTargetMuscle())
                .secondaryMuscles(dto.getSecondaryMuscles())
                .gifUrl(dto.getGifUrl())
                .isSeeded(false)
                .isActive(true)
                .build();
        return mapToDto(exerciseRepository.save(exercise));
    }

    @Transactional
    public ExerciseDto updateExercise(Long id, ExerciseDto dto) {
        Exercise existing = findExerciseOrThrow(id);
        existing.setName(dto.getName());
        existing.setCategory(dto.getCategory());
        existing.setEquipment(dto.getEquipment());
        existing.setDifficulty(dto.getDifficulty());
        existing.setDescription(dto.getDescription());
        existing.setTargetMuscle(dto.getTargetMuscle());
        existing.setSecondaryMuscles(dto.getSecondaryMuscles());
        existing.setGifUrl(dto.getGifUrl());
        return mapToDto(exerciseRepository.save(existing));
    }

    @Transactional
    public void deactivateExercise(Long id) {
        Exercise existing = findExerciseOrThrow(id);
        existing.setIsActive(false);
        exerciseRepository.save(existing);
    }

    @Transactional
    public Exercise saveExerciseEntity(Exercise exercise) {
        return exerciseRepository.save(exercise);
    }

    private Exercise findExerciseOrThrow(Long id) {
        return exerciseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Exercise not found with id: " + id));
    }
}