package converter;

import picocli.CommandLine;

import java.math.BigDecimal;

public class UnsignedBigDecimalCostConverter implements CommandLine.ITypeConverter<BigDecimal> {
    @Override
    public BigDecimal convert(String s) throws Exception {
        BigDecimal parse = new BigDecimal(s);
        if (parse.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("A cost greater than 0 is required.");
        return parse;
    }
}
