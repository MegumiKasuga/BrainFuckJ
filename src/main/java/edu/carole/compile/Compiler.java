package edu.carole.compile;

import edu.carole.env.Environment;
import edu.carole.env.ExceptionTracer;
import edu.carole.event.ExtendedStage;
import edu.carole.event.Stage;
import edu.carole.event.compile.CompileCharEvent;
import edu.carole.event.compile.CompileParams;
import edu.carole.event.compile.CompilerExecutionEvent;
import edu.carole.event.compile.CustomCompileEvent;
import edu.carole.operator.BracketOperator;
import edu.carole.operator.Mark;
import edu.carole.operator.Operator;
import edu.carole.operator.StackMark;
import edu.carole.util.OpPosition;
import lombok.Getter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Stack;
import java.util.function.BiFunction;
import java.util.function.Function;

public class Compiler {

    @Getter
    private final Environment environment;

    public Compiler(Environment environment) {
        this.environment = environment;
    }

    public Script compile(String scriptName,
                          String scriptPath,
                          String script) throws Exception {
        LinkedList<Operator> ops = new LinkedList<>();
        ArrayList<OpPosition> opPositions = new ArrayList<>();
        Stack<BracketOperator> bracketStack = new Stack<>();
        ArrayList<String> lines = new ArrayList<>();
        String lineBreak = System.lineSeparator();
        boolean inAnnotate = false;
        int annotateRightSize = 0, row = 0, col = 0;
        ArrayList<Mark> marks = new ArrayList<>(256);
        StringBuilder builder = new StringBuilder();
        BiFunction<Character, StringBuilder, CompileParams> paramPacker = (c, b) -> {
            return new CompileParams(scriptName, scriptPath, script,
                    ops, opPositions, bracketStack, lines, b, lineBreak,
                    c);
        };
        postEvent(new CompilerExecutionEvent(row, col, this,
                paramPacker.apply(null, builder), ExtendedStage.PRE, null));
        Outer: for (char c : script.toCharArray()) {
            try {
                postEvent(new CompileCharEvent(row, col, this,
                        paramPacker.apply(c, builder), inAnnotate, Stage.PRE));
                // 处理换行
                if (lineBreak.length() == 1) {
                    if (c == lineBreak.charAt(0)) {
                        inAnnotate = false;
                        row++;
                        col = 0;
                        lines.add(builder.toString());
                        builder = new StringBuilder();
                        postEvent(new CompileCharEvent(row, col, this,
                                paramPacker.apply(c, builder), inAnnotate, Stage.POST));
                        continue;
                    }
                } else if (lineBreak.length() >= 2) {
                    while (true) {
                        if (annotateRightSize < lineBreak.length()) {
                            if (c == lineBreak.charAt(annotateRightSize)) {
                                annotateRightSize++;
                                col++;
                                postEvent(new CompileCharEvent(row, col, this,
                                        paramPacker.apply(c, builder), inAnnotate, Stage.POST));
                                continue Outer;
                            } else {
                                annotateRightSize = 0;
                                break;
                            }
                        } else {
                            inAnnotate = false;
                            annotateRightSize = 0;
                            row++;
                            col = 0;
                            lines.add(builder.toString());
                            builder = new StringBuilder();
                            if (c == lineBreak.charAt(0)) {
                                postEvent(new CompileCharEvent(row, col, this,
                                        paramPacker.apply(c, builder), inAnnotate, Stage.POST));
                                continue;
                            }
                            break;
                        }
                    }
                }
                builder.append(c);
                if (inAnnotate) {
                    postEvent(new CompileCharEvent(row, col, this,
                            paramPacker.apply(c, builder), inAnnotate, Stage.POST));
                    col++;
                    continue;
                }
                if (c == '#') {
                    // 这里是注释，遇到注释就应当直接跳到下一行了
                    inAnnotate = true;
                    postEvent(new CompileCharEvent(row, col, this,
                            paramPacker.apply(c, builder), inAnnotate, Stage.POST));
                    col++;
                    continue;
                }
                if (c == '$') {
                    Mark mark = new Mark('$', ops.size());
                    ops.add(mark);
                    marks.add(mark);
                    opPositions.add(new OpPosition(mark, row, col));
                    postEvent(new CompileCharEvent(row, col, this,
                            paramPacker.apply(c, builder), inAnnotate, Stage.POST));
                    col++;
                    continue;
                }
                if (c == '{') {
                    StackMark sm = new StackMark('{', ops.size());
                    ops.add(sm);
                    opPositions.add(new OpPosition(sm, row, col));
                    postEvent(new CompileCharEvent(row, col, this,
                            paramPacker.apply(c, builder), inAnnotate, Stage.POST));
                    col++;
                    continue;
                }
                if (c == '[') {
                    BracketOperator bop = new BracketOperator('[', true, ops.size());
                    bracketStack.push(bop);
                    ops.add(bop);
                    opPositions.add(new OpPosition(bop, row, col));
                    postEvent(new CompileCharEvent(row, col, this,
                            paramPacker.apply(c, builder), inAnnotate, Stage.POST));
                    col++;
                    continue;
                }
                if (c == ']') {
                    BracketOperator bop = new BracketOperator(']', false, ops.size());
                    if (bracketStack.isEmpty()) {
                        throw new IllegalStateException("Unclosed Bracket pair.");
                    }
                    BracketOperator left = bracketStack.pop();
                    left.setPairPos(bop.getPos());
                    bop.setPairPos(left.getPos());
                    ops.add(bop);
                    opPositions.add(new OpPosition(bop, row, col));
                    postEvent(new CompileCharEvent(row, col, this,
                            paramPacker.apply(c, builder), inAnnotate, Stage.POST));
                    col++;
                    continue;
                }
                Operator o = environment.getOperators().getOperator(c);
                if (o != null) {
                    ops.add(o);
                    opPositions.add(new OpPosition(o, row, col));
                    postEvent(new CompileCharEvent(row, col, this,
                            paramPacker.apply(c, builder), inAnnotate, Stage.POST));
                    col++;
                    continue;
                }
                postEvent(new CustomCompileEvent(row, col, this,
                        paramPacker.apply(c, builder)));
                postEvent(new CompileCharEvent(row, col, this,
                        paramPacker.apply(c, builder), inAnnotate, Stage.POST));
                col++;
            } catch (Exception e) {
                postEvent(new CompileCharEvent(row, col, this,
                        paramPacker.apply(c, builder), inAnnotate, Stage.ERROR));
                dealWithException(e, builder, row, col, paramPacker);
                col++;
            }
        }
        if (!bracketStack.isEmpty()) {
            IllegalStateException ise = new IllegalStateException("Unclosed Bracket pair.");
            dealWithException(ise, builder, row, col, paramPacker);
        }
        if (!builder.isEmpty()) {
            lines.add(builder.toString());
        }
        try {
            Script result = new Script(scriptName, scriptPath, ops,
                    marks, opPositions, lines, environment.getTracer());
            postEvent(new CompilerExecutionEvent(row, col, this,
                    paramPacker.apply(null, builder), ExtendedStage.POST, null));
            return result;
        } catch (Exception e) {
            dealWithException(e, builder, row, col, paramPacker);
            throw e;
        }
    }

    private void postEvent(Object event) {
        environment.getEventBus().post(event);
    }

    private void dealWithException(Exception e, StringBuilder builder, int row, int col,
                                   BiFunction<Character, StringBuilder, CompileParams> paramPacker) throws Exception {
        ExceptionTracer tracer = environment.getTracer();
        Exception e2 = tracer.getCompileTraceMessage(builder.toString(), new OpPosition(null, row, col), e);
        Exception t = e2 == null ? e : e2;
        if (tracer.traceCompile(builder.toString(), col, e)) {
            postEvent(new CompilerExecutionEvent(row, col, this,
                    paramPacker.apply(null, builder), ExtendedStage.IGNORED_ERROR, t));
            environment.getLogger().error("Ignored Compile error:", t);
            environment.getLogger().trace("Error message: ", t);
        } else {
            postEvent(new CompilerExecutionEvent(row, col, this,
                    paramPacker.apply(null, builder), ExtendedStage.FATAL_ERROR, t));
            environment.getLogger().fatal("Compile error:", t);
            environment.getLogger().trace("Error message: ", t);
            throw t;
        }
    }
}
