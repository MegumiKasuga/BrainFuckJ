package edu.carole.compile;

import edu.carole.env.Environment;
import edu.carole.operator.BracketOperator;
import edu.carole.operator.Mark;
import edu.carole.operator.Operator;
import edu.carole.operator.StackMark;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Stack;

public class Compiler {

    private final Environment environment;

    public Compiler(Environment environment) {
        this.environment = environment;
    }

    public Script compile(String scriptName, String scriptPath, String script) {
        LinkedList<Operator> ops = new LinkedList<>();
        Stack<BracketOperator> bracketStack = new Stack<>();
        String lineBreak = System.lineSeparator();
        boolean inAnnotate = false;
        int annotateRightSize = 0;
        ArrayList<Mark> marks = new ArrayList<>(256);
        for (char c : script.toCharArray()) {
            if (inAnnotate) {
                // 处理注释的结束部分
                if (lineBreak.length() == 1) {
                    if (c == lineBreak.charAt(0)) {
                        inAnnotate = false;
                        continue;
                    }
                } else if (lineBreak.length() >= 2) {
                    if (annotateRightSize < lineBreak.length()) {
                        if (c == lineBreak.charAt(annotateRightSize)) {
                            annotateRightSize++;
                        } else {
                            annotateRightSize = 0;
                        }
                        continue;
                    } else {
                        inAnnotate = false;
                        annotateRightSize = 0;
                    }
                } else {
                    continue;
                }
            }
            if (c == '$') {
                Mark mark = new Mark('$', ops.size());
                ops.add(mark);
                marks.add(mark);
                continue;
            }
            if (c == '{') {
                StackMark sm = new StackMark('{', ops.size());
                ops.add(sm);
                continue;
            }
            if (c == '#') {
                // 这里是注释，遇到注释就应当直接跳到下一行了
                inAnnotate = true;
                continue;
            }
            if (c == '[') {
                BracketOperator bop = new BracketOperator('[', true, ops.size());
                bracketStack.push(bop);
                ops.add(bop);
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
                continue;
            }
            Operator o = environment.getOperators().getOperator(c);
            if (o != null) {
                ops.add(o);
            }
        }
        if (!bracketStack.isEmpty()) {
            throw new IllegalStateException("Unclosed Bracket pair.");
        }
        return new Script(scriptName, scriptPath, ops, marks);
    }
}
