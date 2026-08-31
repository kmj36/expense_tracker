package converter;

import picocli.CommandLine;

public class Range1To12Converter implements CommandLine.ITypeConverter<Integer> {
    @Override
    public Integer convert(String value) throws IllegalArgumentException {
        int num = Integer.parseInt(value);
        if (num < 1 || num > 12) {
            throw new IllegalArgumentException("A month between 1 and 12 is required.");
        }
        return num;
    }
}
