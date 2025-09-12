package AM.PM.Homepage.cafeteria.domain;

import AM.PM.Homepage.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class CafeteriaMenu extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cafeteria_id")
    private Long id;

    @Column(name = "cafeteria_name")
    private String cafeteria;

    @Column(name = "meal_date")
    private LocalDate mealDate;

    @Column(name = "meal_price")
    private Integer mealPrice;

    @Column(name = "meal_name")
    private String mealName;

    @Column(name = "meal_type")
    private String mealType;




}
