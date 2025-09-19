package AM.PM.Homepage.cafeteria.response;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CafeteriaResponse {

    private String cafeteria;
    private String mealType;
    private String corner;
    private LocalDate mealDate;
    private String mealName;
    private String price;

}
