package com.example.kp_chess.repository;

import com.example.kp_chess.model.CompletedExercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CompletedExerciseRepository extends JpaRepository<CompletedExercise, Long> {

    long countByUserId(Long userId);

    /** Атомарная вставка без гонок: повторное прохождение того же упражнения ничего не меняет. */
    @Modifying
    @Query(value = "INSERT INTO completed_exercises (user_id, exercise_type, level) "
            + "VALUES (:userId, :type, :level) ON CONFLICT DO NOTHING", nativeQuery = true)
    int insertIfAbsent(@Param("userId") Long userId, @Param("type") String type, @Param("level") Integer level);
}
