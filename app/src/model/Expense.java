package model;

import java.math.BigDecimal;
import java.time.LocalDate;

// 비용 컬럼용 클래스
public class Expense {
    public Integer id;
    public LocalDate date;
    public String description;
    public BigDecimal amount;
    public Integer categoryID;

    public Integer getId() {
        return id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Integer getCategoryID() {
        return categoryID;
    }
}
