package edu.carole.event.compile;

import edu.carole.operator.BracketOperator;
import edu.carole.operator.Operator;
import edu.carole.util.OpPosition;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public record CompileParams(@NonNull String scriptName,
                            @NonNull String scriptPath,
                            @NonNull String script,
                            @NonNull List<Operator> ops,
                            @NonNull ArrayList<OpPosition> opPositions,
                            @NonNull Stack<BracketOperator> bracketStack,
                            @NonNull ArrayList<String> lines,
                            @NonNull StringBuilder lineBuilder,
                            @NonNull String lineBreak,
                            @Nullable Character currentChar) {
}
