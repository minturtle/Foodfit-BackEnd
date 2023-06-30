package Foodfit.BackEnd.Service;

import Foodfit.BackEnd.DTO.DailyAnalysisDTO;
import Foodfit.BackEnd.DTO.PeriodAnalysisDTO;
import Foodfit.BackEnd.DTO.UserDTO;
import Foodfit.BackEnd.Domain.Food;
import Foodfit.BackEnd.Domain.User;
import Foodfit.BackEnd.Domain.UserFood;
import Foodfit.BackEnd.Repository.UserFoodRepository;
import Foodfit.BackEnd.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalysisService {
    private final UserFoodRepository userFoodRepository;
    private final UserRepository userRepository;

    public DailyAnalysisDTO getDailyAnalysis(UserDTO userDTO){
        // 현재시간
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = LocalDateTime.of(today, LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.of(today, LocalTime.MAX);
        User user = userRepository.findById(userDTO.getId())
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다."));


        // UserFood 조회
        List<UserFood> userFoods = Optional.ofNullable(userFoodRepository.findByUserAndDateBetween(user, todayStart, todayEnd))
                .orElse(new ArrayList<>());

        // 영양소
        int totalCalorie = 0;
        double totalProtein = 0.0;
        double totalFat = 0.0;
        double totalSalt = 0.0;

        for (UserFood userFood : userFoods){
            Food food = userFood.getFood();
            double weightRatio = userFood.getWeight() / 100;

            totalCalorie += food.getCalorie()*weightRatio;
            totalProtein += Math.round(food.getProtein()*weightRatio);
            totalFat += Math.round(food.getFat()*weightRatio);
            totalSalt += Math.round(food.getSalt()*weightRatio);
        }
        return new DailyAnalysisDTO(totalCalorie, totalProtein, totalFat, totalSalt);
    }

    public List<PeriodAnalysisDTO> getPeriodAnalysis(UserDTO userDTO, LocalDate startDate, LocalDate endDate, String nutrient) {
        User user = userRepository.findById(userDTO.getId())
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다."));
        List<UserFood> userFoods = userFoodRepository.findByUserAndDateBetween(user, startDate.atStartOfDay(), endDate.atTime(LocalTime.MAX));
        Map<LocalDate, Double> nutrientMap = new HashMap<>();

        for (UserFood userFood : userFoods) {
            LocalDate date = userFood.getDate().toLocalDate();
            double nutrientAmount = getDayAmount(userFood, nutrient);
            nutrientMap.put(date, nutrientMap.getOrDefault(date, 0.0) + nutrientAmount);
        }

        List<PeriodAnalysisDTO> nutrientList = new ArrayList<>();

        for (Map.Entry<LocalDate, Double> entry : nutrientMap.entrySet()) {
            LocalDate date = entry.getKey();
            Double nutrientAmount = entry.getValue();
            PeriodAnalysisDTO dto = new PeriodAnalysisDTO(date, nutrientAmount);
            nutrientList.add(dto);
        }

        return nutrientList;
    }

    private double getDayAmount(UserFood userFood, String nutrient) {
        Food food = userFood.getFood();
        double weightRatio = userFood.getWeight() / 100;

        return switch (nutrient) {
            case "protein" -> food.getProtein() * weightRatio;
            case "fat" -> food.getFat() * weightRatio;
            case "salt" -> food.getSalt() * weightRatio;
            // Handle other nutrients accordingly
            default -> throw new IllegalArgumentException("Invalid nutrient type: " + nutrient);
        };
    }
}
