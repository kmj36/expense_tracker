package converter;

import picocli.CommandLine;

public class UnsignedIntAndNonZeroConverter implements CommandLine.ITypeConverter<Integer> {
    @Override
    public Integer convert(String s) throws IllegalArgumentException {
        int parse = Integer.parseUnsignedInt(s);
        if (parse < 1)
            throw new IllegalArgumentException("A number greater than 0 required.");
        return parse;
    }
}
